package com.example.findlynew

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private var currentQuery: String = ""
    private var currentCategory: String = "Semua Kategori"
    private lateinit var adapter: BarangAdapter
    private lateinit var listPost: List<Barang>
    private lateinit var tvEmptyState: android.widget.TextView
    private lateinit var rvBarang: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvBarang = findViewById(R.id.rv_barang)
        tvEmptyState = findViewById(R.id.tv_empty_state)
        val etSearch = findViewById<android.widget.EditText>(R.id.et_search)
        val btnFilter = findViewById<android.widget.ImageView>(R.id.btn_filter)
        rvBarang.layoutManager = GridLayoutManager(this, 2)
        
        val dbHelper = DatabaseHelper(this)
        listPost = dbHelper.getAllPosts().filter { it.status != "HAPUS" }
        adapter = BarangAdapter(listPost)
        rvBarang.adapter = adapter

        // Setup PopupMenu for btnFilter
        btnFilter.setOnClickListener { view ->
            val wrapper = android.view.ContextThemeWrapper(this, R.style.CustomPopupMenuTheme)
            val popup = androidx.appcompat.widget.PopupMenu(wrapper, view)
            popup.menu.add("Semua Kategori")
            popup.menu.add("Elektronik")
            popup.menu.add("Uang")
            popup.menu.add("Alat Tulis/Buku")
            popup.menu.add("Barang Pribadi")
            
            popup.setOnMenuItemClickListener { item ->
                currentCategory = item.title.toString()
                applyFilters()
                true
            }
            popup.show()
        }

        // Logika Search Berdasarkan Keakuratan
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim()?.lowercase() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Initial Filter Apply
        applyFilters()

        // Setup Bottom Navbar
        val navHome = findViewById<android.widget.ImageButton>(R.id.nav_home)
        val navPostAdd = findViewById<android.widget.ImageButton>(R.id.nav_post_add)
        val navProfile = findViewById<android.widget.ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {
            // Already in Home
        }

        navPostAdd.setOnClickListener {
            startActivity(android.content.Intent(this, PostActivity::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data
        val dbHelper = DatabaseHelper(this)
        listPost = dbHelper.getAllPosts().filter { it.status != "HAPUS" }
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = listPost

        // Filter kategori
        if (currentCategory != "Semua Kategori") {
            filteredList = filteredList.filter { it.kategori.equals(currentCategory, ignoreCase = true) }
        }

        // Filter & Score pencarian
        if (currentQuery.isNotEmpty()) {
            val scoredItems = filteredList.mapNotNull { barang ->
                var score = 0
                val queryWords = currentQuery.split("\\s+".toRegex())
                
                for (word in queryWords) {
                    if (word.isBlank()) continue
                    
                    val nameMatch = barang.nama.lowercase().contains(word)
                    val descMatch = barang.deskripsi.lowercase().contains(word)
                    
                    if (nameMatch) score += 3
                    if (descMatch) score += 1
                }
                
                if (barang.nama.lowercase() == currentQuery) {
                    score += 5
                }
                
                if (score > 0) Pair(barang, score) else null
            }
            
            filteredList = scoredItems.sortedByDescending { it.second }.map { it.first }
        }

        adapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            tvEmptyState.visibility = android.view.View.VISIBLE
            rvBarang.visibility = android.view.View.GONE
            
            if (currentQuery.isNotEmpty() || currentCategory != "Semua Kategori") {
                tvEmptyState.text = "Tidak menemukan barang yang sesuai"
            } else {
                tvEmptyState.text = "Maaf, belum ada barang yang dilaporkan"
            }
        } else {
            tvEmptyState.visibility = android.view.View.GONE
            rvBarang.visibility = android.view.View.VISIBLE
        }
    }
}
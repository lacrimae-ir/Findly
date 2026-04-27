package com.example.findlynew

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvBarang = findViewById<RecyclerView>(R.id.rv_barang)
        val tvEmptyState = findViewById<android.widget.TextView>(R.id.tv_empty_state)
        val etSearch = findViewById<android.widget.EditText>(R.id.et_search)
        rvBarang.layoutManager = GridLayoutManager(this, 2)
        
        val dbHelper = DatabaseHelper(this)
        val listPost = dbHelper.getAllPosts()
        val adapter = BarangAdapter(listPost)
        rvBarang.adapter = adapter
        
        if (listPost.isEmpty()) {
            tvEmptyState.visibility = android.view.View.VISIBLE
            rvBarang.visibility = android.view.View.GONE
        } else {
            tvEmptyState.visibility = android.view.View.GONE
            rvBarang.visibility = android.view.View.VISIBLE
        }

        // Logika Search Berdasarkan Keakuratan
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                
                if (query.isEmpty()) {
                    adapter.updateData(listPost)
                    if (listPost.isEmpty()) {
                        tvEmptyState.text = "Maaf, belum ada barang yang dilaporkan"
                        tvEmptyState.visibility = android.view.View.VISIBLE
                        rvBarang.visibility = android.view.View.GONE
                    } else {
                        tvEmptyState.visibility = android.view.View.GONE
                        rvBarang.visibility = android.view.View.VISIBLE
                    }
                    return
                }

                // Kalkulasi Score Keakuratan
                val scoredItems = listPost.mapNotNull { barang ->
                    var score = 0
                    val queryWords = query.split("\\s+".toRegex())
                    
                    for (word in queryWords) {
                        if (word.isBlank()) continue
                        
                        val nameMatch = barang.nama.lowercase().contains(word)
                        val descMatch = barang.deskripsi.lowercase().contains(word)
                        
                        if (nameMatch) score += 3
                        if (descMatch) score += 1
                    }
                    
                    // Bonus jika sama persis di nama
                    if (barang.nama.lowercase() == query) {
                        score += 5
                    }
                    
                    if (score > 0) Pair(barang, score) else null
                }
                
                // Urutkan dari score tertinggi
                val sortedItems = scoredItems.sortedByDescending { it.second }.map { it.first }
                adapter.updateData(sortedItems)

                if (sortedItems.isEmpty()) {
                    tvEmptyState.text = "Tidak menemukan barang yang sesuai"
                    tvEmptyState.visibility = android.view.View.VISIBLE
                    rvBarang.visibility = android.view.View.GONE
                } else {
                    tvEmptyState.visibility = android.view.View.GONE
                    rvBarang.visibility = android.view.View.VISIBLE
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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
}
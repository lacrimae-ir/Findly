package com.example.findlynew

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.inputmethod.InputMethodManager

class SeeAllActivity : AppCompatActivity() {

    private var currentQuery: String = ""
    private var currentCategory: String = "Semua Kategori"
    private var isLatestFirst: Boolean = true
    private lateinit var adapter: BarangAdapter
    private lateinit var listPost: List<Barang>
    private lateinit var tvEmptyState: android.widget.TextView
    private lateinit var rvBarang: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var btnSortDate: android.widget.TextView
    private lateinit var filterCatAll: android.widget.TextView
    private lateinit var filterCatElektronik: android.widget.TextView
    private lateinit var filterCatUang: android.widget.TextView
    private lateinit var filterCatBuku: android.widget.TextView
    private lateinit var filterCatPakaian: android.widget.TextView
    private lateinit var filterCatAksesoris: android.widget.TextView
    private lateinit var filterCatLainnya: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_see_all)

        val selectedCategory = intent.getStringExtra("CATEGORY")

        val openSearch = intent.getBooleanExtra("OPEN_SEARCH", false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvBarang = findViewById(R.id.rv_barang)
        tvEmptyState = findViewById(R.id.tv_empty_state)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        val etSearch = findViewById<android.widget.EditText>(R.id.et_search)

        if (openSearch) {

            etSearch.requestFocus()

            val imm = getSystemService(INPUT_METHOD_SERVICE)
                    as InputMethodManager

            imm.showSoftInput(
                etSearch,
                InputMethodManager.SHOW_IMPLICIT
            )

        }

        rvBarang.layoutManager = GridLayoutManager(this, 2)

        val dbHelper = DatabaseHelper(this)
        val sessionManager = SessionManager(this)
        val email = sessionManager.getUserEmail() ?: "anonymous"
        listPost = emptyList()

        btnSortDate = findViewById(R.id.btnSortDate)
        filterCatAll = findViewById(R.id.filterCatAll)
        filterCatElektronik = findViewById(R.id.filterCatElektronik)
        filterCatUang = findViewById(R.id.filterCatUang)
        filterCatBuku = findViewById(R.id.filterCatBuku)
        filterCatPakaian = findViewById(R.id.filterCatPakaian)
        filterCatAksesoris = findViewById(R.id.filterCatAksesoris)
        filterCatLainnya = findViewById(R.id.filterCatLainnya)

        val catViews = listOf(
            filterCatAll to "Semua Kategori",
            filterCatElektronik to "Elektronik",
            filterCatUang to "Uang",
            filterCatBuku to "Alat Tulis/Buku",
            filterCatPakaian to "Pakaian",
            filterCatAksesoris to "Aksesoris",
            filterCatLainnya to "Lainnya"
        )

        fun updateCategoryUI() {
            for ((view, cat) in catViews) {
                val isSelected = if (cat == "Semua Kategori") {
                    currentCategory == "Semua Kategori" || currentCategory == "Semua"
                } else {
                    currentCategory.equals(cat, ignoreCase = true)
                }
                if (isSelected) {
                    view.setBackgroundResource(R.drawable.bg_survey_button_selected)
                    view.setTextColor(android.graphics.Color.WHITE)
                } else {
                    view.setBackgroundResource(R.drawable.bg_survey_button_unselected)
                    view.setTextColor(android.graphics.Color.parseColor("#5B7FFF"))
                }
            }
        }

        // Initialize sort state
        if (isLatestFirst) {
            btnSortDate.text = "Terbaru (Latest)"
        } else {
            btnSortDate.text = "Terlama (Earliest)"
        }

        btnSortDate.setOnClickListener {
            isLatestFirst = !isLatestFirst
            if (isLatestFirst) {
                btnSortDate.text = "Terbaru (Latest)"
            } else {
                btnSortDate.text = "Terlama (Earliest)"
            }
            applyFilters()
        }

        for ((view, cat) in catViews) {
            view.setOnClickListener {
                currentCategory = cat
                updateCategoryUI()
                applyFilters()
            }
        }

        selectedCategory?.let { category ->
            currentCategory = category
        }
        updateCategoryUI()

        adapter = BarangAdapter(listPost)
        rvBarang.adapter = adapter

        // Setup Pull-to-Refresh
        swipeRefresh.setColorSchemeColors(
            android.graphics.Color.parseColor("#4B8BF5"),
            android.graphics.Color.parseColor("#5B9BFF"),
            android.graphics.Color.parseColor("#3A7AE4")
        )
        swipeRefresh.setOnRefreshListener {
            loadPostsFromFirebase()
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

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || 
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    dbHelper.insertSearchQuery(email, query)
                }
            }
            false
        }

        // Initial Filter Apply
        applyFilters()

        // Setup Bottom Navbar
        val navHome = findViewById<android.widget.ImageButton>(R.id.nav_home)
        val navPostAdd = findViewById<android.widget.ImageButton>(R.id.nav_post_add)
        val navProfile = findViewById<android.widget.ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {

            val intent = android.content.Intent(this, MainActivity::class.java)

            intent.flags =
                android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
            finish()
        }

        navPostAdd.setOnClickListener {
            startActivity(android.content.Intent(this, PostActivity::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }

        val btnNotification = findViewById<android.widget.ImageView>(R.id.btn_notification)
        btnNotification.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationActivity::class.java))
        }

        // Setup Notification Channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "FINDLY_NOTIFICATIONS",
                "Findly Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPostsFromFirebase()
        updateNotificationBadge()
    }

    private fun loadPostsFromFirebase() {
        swipeRefresh.isRefreshing = true
        FirebaseManager.getAllPosts { posts ->
            runOnUiThread {
                listPost = posts.filter { it.status != "HAPUS" && !it.del }
                applyFilters()
                swipeRefresh.isRefreshing = false
                updateNotificationBadge()
            }
        }
    }

    private fun applyFilters() {
        var filteredList = listPost

        // Filter kategori
        if (currentCategory != "Semua Kategori" && currentCategory != "Semua") {
            filteredList = filteredList.filter { it.kategori.equals(currentCategory, ignoreCase = true) }
        }

        val dateFormat = java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.US)

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

            filteredList = scoredItems.sortedWith(Comparator { a, b ->
                val scoreCompare = b.second.compareTo(a.second)
                if (scoreCompare != 0) {
                    scoreCompare
                } else {
                    val dateA = try { dateFormat.parse(a.first.tanggal) } catch (e: Exception) { null }
                    val dateB = try { dateFormat.parse(b.first.tanggal) } catch (e: Exception) { null }
                    when {
                        dateA == null && dateB == null -> 0
                        dateA == null -> 1
                        dateB == null -> -1
                        else -> {
                            if (isLatestFirst) dateB.compareTo(dateA) else dateA.compareTo(dateB)
                        }
                    }
                }
            }).map { it.first }
        } else {
            // Sort strictly by date (latest first vs earliest first)
            filteredList = filteredList.sortedWith(Comparator { a, b ->
                val dateA = try { dateFormat.parse(a.tanggal) } catch (e: Exception) { null }
                val dateB = try { dateFormat.parse(b.tanggal) } catch (e: Exception) { null }
                when {
                    dateA == null && dateB == null -> 0
                    dateA == null -> 1
                    dateB == null -> -1
                    else -> {
                        if (isLatestFirst) dateB.compareTo(dateA) else dateA.compareTo(dateB)
                    }
                }
            })
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

    private fun updateNotificationBadge() {
        val sessionManager = SessionManager(this)
        val email = sessionManager.getUserEmail() ?: ""
        if (email.isEmpty()) return

        val dbHelper = DatabaseHelper(this)
        val unreadCount = dbHelper.getUnreadNotificationCount(email)

        val tvBadge = findViewById<android.widget.TextView>(R.id.tvNotificationBadge)
        if (unreadCount > 0) {
            tvBadge.visibility = android.view.View.VISIBLE
            if (unreadCount > 99) {
                tvBadge.text = "99+"
            } else {
                tvBadge.text = unreadCount.toString()
            }
        } else {
            tvBadge.visibility = android.view.View.GONE
        }
    }
}
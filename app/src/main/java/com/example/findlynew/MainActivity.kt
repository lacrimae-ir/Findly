package com.example.findlynew

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvRecent: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvCategories = findViewById(R.id.rv_categories)
        rvRecent = findViewById(R.id.rv_recent)

        val dbHelper = DatabaseHelper(this)

        val recentPosts = dbHelper.getAllPosts()
            .filter { it.status != "HAPUS" }
            .take(5)

        rvRecent.adapter = RecentAdapter(recentPosts)

        rvRecent.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvRecent.adapter = RecentAdapter(recentPosts)

        val etSearch = findViewById<EditText>(R.id.et_search)

        etSearch.isFocusable = false
        etSearch.isFocusableInTouchMode = false
        etSearch.isCursorVisible = false

        etSearch.setOnClickListener {
            val intent = Intent(this, SeeAllActivity::class.java)
            intent.putExtra("OPEN_SEARCH", true)
            startActivity(intent)
        }

        rvCategories.layoutManager = GridLayoutManager(this, 3)

        val categoryList = listOf(
            Category("Elektronik", R.drawable.ic_electronic),
            Category("Uang", R.drawable.ic_money),
            Category("Alat Tulis\n/Buku", R.drawable.ic_book),
            Category("Pakaian", R.drawable.ic_clothes),
            Category("Aksesoris", R.drawable.ic_accesories),
            Category("Lainnya", R.drawable.ic_others)
        )

        rvCategories.adapter = CategoryAdapter(categoryList) { category ->

            val intent = Intent(this, SeeAllActivity::class.java)
            intent.putExtra("CATEGORY", category.name)
            startActivity(intent)

        }

        val tvSeeAll = findViewById<TextView>(R.id.tv_see_all)

        tvSeeAll.setOnClickListener {
            val intent = Intent(this, SeeAllActivity::class.java)
            startActivity(intent)
        }

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

        val dbHelper = DatabaseHelper(this)

        val recentPosts = dbHelper.getAllPosts()
            .filter { it.status != "HAPUS" }
            .take(5)

        rvRecent.adapter = RecentAdapter(recentPosts)
    }
}
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

        rvRecent.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        loadRecentPosts()

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
            val cleanCategory = category.name.replace("\n", "")
            intent.putExtra("CATEGORY", cleanCategory)
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

        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            loadRecentPosts()
        }

        // Start background notification service
        try {
            val serviceIntent = Intent(this, NotificationService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        validateSession()
        loadRecentPosts()
        updateNotificationBadge()
    }

    private fun loadRecentPosts() {
        FirebaseManager.getAllPosts { posts ->
            checkNewNotifications(posts)
            val sortedRecent = posts
                .filter { it.status != "HAPUS" && !it.del }
                .sortedByDescending { it.id }
                .take(5)
            runOnUiThread {
                rvRecent.adapter = RecentAdapter(sortedRecent)
                updateNotificationBadge()
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh).isRefreshing = false
            }
        }
    }

    private fun checkNewNotifications(posts: List<Barang>) {
        val sessionManager = SessionManager(this)
        val email = sessionManager.getUserEmail() ?: ""
        if (email.isEmpty()) return

        val dbHelper = DatabaseHelper(this)
        val preferences = dbHelper.getUserPreferences(email)
        val detailText = dbHelper.getPreferenceDetail(email)

        // Read currently saved notif IDs to see which ones are new
        val savedNotifIds = dbHelper.getNotificationIds(email).map { it.first }.toSet()

        for (post in posts) {
            if (post.status != "HAPUS" && !post.del && !savedNotifIds.contains(post.id)) {
                val points = calculatePoints(post, preferences, detailText)
                val categoryMatched = preferences.any { it.equals(post.kategori, ignoreCase = true) }
                if (points >= 15 || categoryMatched) {
                    // It's a new match!
                    dbHelper.insertNotification(email, post.id)
                    if (sessionManager.isPushNotificationsEnabled()) {
                        triggerSystemNotification(post.nama, post.id)
                    }
                }
            }
        }
    }

    private fun calculatePoints(post: Barang, categories: Set<String>, detailText: String): Int {
        var points = 0
        val categoryMatched = categories.any { it.equals(post.kategori, ignoreCase = true) }
        if (categoryMatched) {
            points += 10
        }
        if (detailText.isNotEmpty()) {
            val cleanDetail = detailText.trim()
            val postNameLower = post.nama.lowercase()
            val postDescLower = post.deskripsi.lowercase()
            if (postNameLower.contains(cleanDetail.lowercase())) points += 15
            if (postDescLower.contains(cleanDetail.lowercase())) points += 10
            val stopWords = setOf("dan", "atau", "saya", "yang", "di", "ke", "dari", "untuk", "dengan", "ini", "itu", "sering", "mencari", "ada", "bisa", "pada", "adalah")
            val words = cleanDetail.lowercase().split(Regex("[\\s,\\.\\-\\?]+")).filter { it.length >= 3 && !stopWords.contains(it) }
            for (word in words) {
                if (postNameLower.contains(word)) points += 5
                if (postDescLower.contains(word)) points += 2
            }
        }
        return points
    }

    private fun triggerSystemNotification(namaBarang: String, postId: String) {
        val intent = android.content.Intent(this, ItemDetailActivity::class.java).apply {
            putExtra("EXTRA_POST_ID", postId)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(), 
            intent, 
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = androidx.core.app.NotificationCompat.Builder(this, "FINDLY_NOTIFICATIONS")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Barang temuan baru cocok dengan Anda!")
            .setContentText(namaBarang)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = androidx.core.app.NotificationManagerCompat.from(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } else {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
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

    private fun validateSession() {
        val sessionManager = SessionManager(this)
        val uid = sessionManager.getUserUid() ?: ""
        val localHash = sessionManager.getPasswordHash() ?: ""
        
        if (!sessionManager.isLoggedIn() || uid.isEmpty()) return

        FirebaseManager.getUserById(uid) { user ->
            runOnUiThread {
                if (user != null) {
                    if (localHash.isNotEmpty()) {
                        if (user.password != localHash) {
                            forceLogout("Sesi Anda telah berakhir karena password telah diubah.")
                        }
                    } else {
                        // Save the password hash for existing logged-in users upgrading the app
                        sessionManager.savePasswordHash(user.password)
                    }
                } else {
                    forceLogout("Akun tidak ditemukan. Silakan login kembali.")
                }
            }
        }
    }

    private fun forceLogout(message: String) {
        val sessionManager = SessionManager(this)
        try {
            stopService(Intent(this, NotificationService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sessionManager.logout()
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
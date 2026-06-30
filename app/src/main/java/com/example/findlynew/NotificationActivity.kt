package com.example.findlynew

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            loadNotifications(false)
        }

        loadNotifications(true)
    }

    private fun loadNotifications(showDialog: Boolean) {
        val sessionManager = SessionManager(this)
        val email = sessionManager.getUserEmail() ?: ""
        val dbHelper = DatabaseHelper(this)
        val preferences = dbHelper.getUserPreferences(email)
        val detailText = dbHelper.getPreferenceDetail(email)

        val progressDialog = if (showDialog) {
            android.app.ProgressDialog(this).apply {
                setMessage("Memuat notifikasi...")
                setCancelable(false)
                show()
            }
        } else null

        FirebaseManager.getAllPosts { allPosts ->
            runOnUiThread {
                progressDialog?.dismiss()
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh).isRefreshing = false
                
                // 1. Cek semua post baru dari Firebase yang memenuhi gerbang 15 Poin atau kecocokan kategori
                for (post in allPosts) {
                    if (post.status != "HAPUS" && !post.del) {
                        val points = calculatePoints(post, preferences, detailText)
                        val categoryMatched = preferences.any { it.equals(post.kategori, ignoreCase = true) }
                        if (points >= 15 || categoryMatched) {
                            dbHelper.insertNotification(email, post.id)
                        }
                    }
                }

                // 2. Ambil seluruh riwayat notifikasi lokal yang tersimpan secara kronologis (postId, isRead)
                val localNotifs = dbHelper.getNotificationIds(email)

                // 3. Petakan ID tersebut ke data barang aktif
                val matchingItems = localNotifs.mapNotNull { pair ->
                    val postId = pair.first
                    val isRead = pair.second
                    val post = allPosts.find { it.id == postId && it.status != "HAPUS" && !it.del }
                    if (post != null) {
                        val points = calculatePoints(post, preferences, detailText)
                        val categoryMatched = preferences.any { it.equals(post.kategori, ignoreCase = true) }
                        if (points >= 15 || categoryMatched) {
                            NotificationItem(post, points, isRead)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }

                // Tandai semua notifikasi untuk email ini sebagai telah dibaca
                dbHelper.markNotificationsAsRead(email)

                val tvNotificationCount = findViewById<TextView>(R.id.tvNotificationCount)
                tvNotificationCount.text = "Anda memiliki ${matchingItems.size} Notifikasi hari ini."

                val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
                rvNotifications.layoutManager = LinearLayoutManager(this@NotificationActivity)
                val adapter = NotificationAdapter(matchingItems)
                rvNotifications.adapter = adapter
            }
        }
    }

    private fun calculatePoints(post: Barang, categories: Set<String>, detailText: String): Int {
        var points = 0

        // 1. Kategori Sama = 10 Poin
        val categoryMatched = categories.any { it.equals(post.kategori, ignoreCase = true) }
        if (categoryMatched) {
            points += 10
        }

        if (detailText.isNotEmpty()) {
            val cleanDetail = detailText.trim()
            val postNameLower = post.nama.lowercase()
            val postDescLower = post.deskripsi.lowercase()

            // 2. Kecocokan Kalimat Penuh
            if (postNameLower.contains(cleanDetail.lowercase())) {
                points += 15
            }
            if (postDescLower.contains(cleanDetail.lowercase())) {
                points += 10
            }

            // 3. Kecocokan Per Kata
            val stopWords = setOf("dan", "atau", "saya", "yang", "di", "ke", "dari", "untuk", "dengan", "ini", "itu", "sering", "mencari", "ada", "bisa", "pada", "adalah")
            val words = cleanDetail.lowercase().split(Regex("[\\s,\\.\\-\\?]+"))
                .filter { it.length >= 3 && !stopWords.contains(it) }

            for (word in words) {
                if (postNameLower.contains(word)) {
                    points += 5
                }
                if (postDescLower.contains(word)) {
                    points += 2
                }
            }
        }

        return points
    }
}

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

        val sessionManager = SessionManager(this)
        val email = sessionManager.getUserEmail() ?: ""
        val preferences = sessionManager.getUserPreferences(email)

        val dbHelper = DatabaseHelper(this)
        val allPosts = dbHelper.getAllPosts()
        
        // Filter posts that match user preferences and are not deleted
        val matchingPosts = allPosts.filter { post ->
            post.status != "HAPUS" && preferences.any { it.equals(post.kategori, ignoreCase = true) }
        }

        val tvNotificationCount = findViewById<TextView>(R.id.tvNotificationCount)
        tvNotificationCount.text = "You have ${matchingPosts.size} Notifications today."

        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        val adapter = NotificationAdapter(matchingPosts)
        rvNotifications.adapter = adapter
    }
}

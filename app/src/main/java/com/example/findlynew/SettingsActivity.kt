package com.example.findlynew

import android.os.Bundle
import android.content.Intent
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sessionManager = SessionManager(this)

        // Inisialisasi View baru (Tombol Back)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Inisialisasi View lainnya
        val tvSettingsName = findViewById<TextView>(R.id.tvSettingsName)
        val menuEditProfile = findViewById<RelativeLayout>(R.id.menuEditProfile)
        val switchNotifications = findViewById<SwitchCompat>(R.id.switchNotifications)
        val menuCustomerService = findViewById<RelativeLayout>(R.id.menuCustomerService)

        // Aksi untuk Tombol Back ke Profile
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Menutup halaman settings dan kembali ke profile
        }

        loadSettingsData()

        // Aksi Menu Edit Profile
        menuEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Inisialisasi status switch notifikasi dari Session
        switchNotifications.isChecked = sessionManager.isPushNotificationsEnabled()

        // Aksi Toggle Switch Push Notifications
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setPushNotificationsEnabled(isChecked)
            if (isChecked) {
                Toast.makeText(this, "Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifikasi Dimatikan", Toast.LENGTH_SHORT).show()
            }
        }

        // Aksi Menu Customer Service
        menuCustomerService.setOnClickListener {
            try {
                val phoneNumber = "6281387282456"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://wa.me/$phoneNumber")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSettingsData()
    }

    private fun loadSettingsData() {
        val tvSettingsName = findViewById<TextView>(R.id.tvSettingsName)
        val ivSettingsProfile = findViewById<android.widget.ImageView>(R.id.ivSettingsProfile)

        tvSettingsName.text = sessionManager.getUserName()
        val email = sessionManager.getUserEmail() ?: ""
        val profilePicUrl = sessionManager.getProfilePic(email)
        if (!profilePicUrl.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(profilePicUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(ivSettingsProfile)
        } else {
            ivSettingsProfile.setImageResource(R.drawable.profile)
        }
    }
}
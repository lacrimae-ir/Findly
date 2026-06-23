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

        // Set nama pengguna dari session manager
        tvSettingsName.text = sessionManager.getUserName()

        // Aksi Menu Edit Profile
        menuEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Aksi Toggle Switch Push Notifications
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Notifikasi Diaktifkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifikasi Dimatikan", Toast.LENGTH_SHORT).show()
            }
        }

        // Aksi Menu Customer Service
        menuCustomerService.setOnClickListener {
            Toast.makeText(this, "Menuju halaman Customer Service", Toast.LENGTH_SHORT).show()
        }
    }
}
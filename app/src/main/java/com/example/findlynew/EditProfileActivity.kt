package com.example.findlynew

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = SessionManager(this)

        // Inisialisasi View
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvEditProfileName = findViewById<TextView>(R.id.tvEditProfileName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etMobileNumber = findViewById<TextInputEditText>(R.id.etMobileNumber)
        val etEditPassword = findViewById<TextInputEditText>(R.id.etEditPassword)
        val etEditConfirmPassword = findViewById<TextInputEditText>(R.id.etEditConfirmPassword)
        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)

        // Set Data Awal dari Session (jika ada)
        tvEditProfileName.text = sessionManager.getUserName()
        etEmail.setText(sessionManager.getUserEmail())
        // etMobileNumber.setText("Isi dengan nomor jika tersimpan di session")

        // 1. Aksi Tombol Back -> Kembali ke SettingsActivity
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Aksi Tombol Save Changes
        btnSaveChanges.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etMobileNumber.text.toString().trim()
            val password = etEditPassword.text.toString().trim()
            val confirmPassword = etEditConfirmPassword.text.toString().trim()

            // Validasi Sederhana
            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Email dan Nomor Telepon harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isNotEmpty() && password != confirmPassword) {
                etEditConfirmPassword.error = "Password tidak cocok!"
                return@setOnClickListener
            }

            // Jalankan proses update (simpan ke API lokal / session)
            Toast.makeText(this, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()

            // Bawa kembali ke SettingsActivity
            finish()
        }
    }
}
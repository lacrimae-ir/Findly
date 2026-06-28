package com.example.findlynew
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        databaseHelper = DatabaseHelper(this)

        // Inisialisasi View
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnUpdatePassword = findViewById<Button>(R.id.btnUpdatePassword)

        // Logika tombol kembali
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Menutup activity dan kembali ke halaman sebelumnya
        }

        // Logika tombol update password
        btnUpdatePassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 1. Validasi jika input kosong
            if (email.isEmpty()) {
                etEmail.error = "Email tidak boleh kosong"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password tidak boleh kosong"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // 2. Validasi minimal panjang karakter (opsional, contoh: 8 karakter)
            if (password.length < 8) {
                etPassword.error = "Password minimal harus 8 karakter"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // 3. Validasi apakah kedua password cocok
            if (password != confirmPassword) {
                etConfirmPassword.error = "Password tidak cocok!"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // Jika semua validasi lolos
            performUpdatePassword(email, password)
        }
    }

    private fun performUpdatePassword(
        email: String,
        password: String
    ) {

        val success = databaseHelper.updatePassword(email, password)

        if (success) {

            Toast.makeText(
                this,
                "Password berhasil diperbarui!",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, LoginActivity::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()

        } else {

            Toast.makeText(
                this,
                "Email tidak ditemukan!",
                Toast.LENGTH_SHORT
            ).show()

        }
    }
}
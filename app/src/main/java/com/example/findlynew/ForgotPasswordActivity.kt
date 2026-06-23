package com.example.findlynew
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inisialisasi View
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnUpdatePassword = findViewById<Button>(R.id.btnUpdatePassword)

        // Logika tombol kembali
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Menutup activity dan kembali ke halaman sebelumnya
        }

        // Logika tombol update password
        btnUpdatePassword.setOnClickListener {
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // 1. Validasi jika input kosong
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
            performUpdatePassword(password)
        }
    }

    private fun performUpdatePassword(password: String) {
        // 1. Tulis logika integrasi API / Firebase kamu di sini

        // 2. Tampilkan pesan sukses
        Toast.makeText(this, "Password berhasil diperbarui!", Toast.LENGTH_SHORT).show()

        // 3. Berpindah ke Halaman Login
        // Ganti 'LoginActivity' dengan nama kelas Activity Login yang sudah kamu buat
        val intent = Intent(this, LoginActivity::class.java)

        // Bendera (Flags) ini berfungsi untuk membersihkan tumpukan halaman (activity stack),
        // sehingga pengguna tidak bisa pencet tombol 'Back' untuk kembali ke halaman ganti password.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        // 4. Tutup semua activity sebelumnya
        finishAffinity()
    }
}
package com.example.findlynew

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val btnMahasiswa = findViewById<Button>(R.id.btnMahasiswa)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isNameFilled = etNama.text.toString().trim().isNotEmpty()
                val isEmailFilled = etEmail.text.toString().trim().isNotEmpty()
                val isPasswordFilled = etPassword.text.toString().trim().isNotEmpty()
                val isConfirmPasswordFilled = etConfirmPassword.text.toString().trim().isNotEmpty()

                if (isNameFilled && isEmailFilled && isPasswordFilled && isConfirmPasswordFilled) {
                    btnMasuk.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5B7FFF"))
                } else {
                    btnMasuk.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#AFC3F2"))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etNama.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)

        btnMasuk.setOnClickListener {
            val name = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) {
                etNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }

            if (email.isEmpty() || !email.contains("@")) {
                etEmail.error = "Masukkan email yang benar"
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                etPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Password tidak cocok"
                return@setOnClickListener
            }

            if (databaseHelper.checkEmailExists(email)) {
                Toast.makeText(this, "Email sudah terdaftar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isInserted = databaseHelper.insertUser(name, email, password)
            if (isInserted) {
                // Auto login after registration
                sessionManager.saveLoginSession(name, email)
                Toast.makeText(this, "Registrasi berhasil, langsung masuk", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registrasi gagal", Toast.LENGTH_SHORT).show()
            }
        }

        btnMahasiswa.setOnClickListener {
            // Arahkan ke halaman Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // pindah ke login
        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
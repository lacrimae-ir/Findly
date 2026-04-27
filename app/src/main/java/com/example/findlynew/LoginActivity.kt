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

class LoginActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        val etLoginEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etLoginPassword = findViewById<EditText>(R.id.etLoginPassword)
        val txtDaftar = findViewById<TextView>(R.id.txtDaftar)
        val txtLupaPassword = findViewById<TextView>(R.id.txtLupaPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEmailFilled = etLoginEmail.text.toString().trim().isNotEmpty()
                val isPasswordFilled = etLoginPassword.text.toString().trim().isNotEmpty()

                if (isEmailFilled && isPasswordFilled) {
                    btnLogin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5B7FFF"))
                } else {
                    btnLogin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#AFC3F2"))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etLoginEmail.addTextChangedListener(textWatcher)
        etLoginPassword.addTextChangedListener(textWatcher)

        // Pindah ke halaman Sign Up
        txtDaftar.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        // Lupa Password (sementara kosong)
        txtLupaPassword.setOnClickListener {
            // TODO: buat halaman forgot password
            Toast.makeText(this, "Fitur lupa password belum tersedia", Toast.LENGTH_SHORT).show()
        }

        // Login sukses, masuk ke MainActivity
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString().trim()
            val password = etLoginPassword.text.toString().trim()

            if (email.isEmpty() || !email.contains("@")) {
                etLoginEmail.error = "Masukkan email yang benar"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etLoginPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            val isValid = databaseHelper.checkUser(email, password)
            if (isValid) {
                // Get name and save to session
                val name = databaseHelper.getUserName(email)
                sessionManager.saveLoginSession(name, email)
                
                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Email atau Password salah", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.example.findlynew

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val txtDaftar = findViewById<TextView>(R.id.txtDaftar)
        val txtLupaPassword = findViewById<TextView>(R.id.txtLupaPassword)
        val btnLogin = findViewById<android.widget.Button>(R.id.btnLogin)

        // Pindah ke halaman Sign Up
        txtDaftar.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Lupa Password (sementara kosong)
        txtLupaPassword.setOnClickListener {
            // TODO: buat halaman forgot password
        }

        // Login sukses, masuk ke MainActivity
        btnLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
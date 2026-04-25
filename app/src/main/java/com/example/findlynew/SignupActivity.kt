package com.example.findlynew

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val btnMahasiswa = findViewById<Button>(R.id.btnMahasiswa)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        btnMasuk.setOnClickListener {
            // TODO: masuk ke dashboard
        }

        btnMahasiswa.setOnClickListener {
            // TODO: masuk sebagai mahasiswa
        }

        // pindah ke login
        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
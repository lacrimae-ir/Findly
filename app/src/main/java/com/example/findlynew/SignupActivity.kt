package com.example.findlynew

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val btnMahasiswa = findViewById<Button>(R.id.btnMahasiswa)

        btnMasuk.setOnClickListener {
            // TODO: proses signup/login
        }

        btnMahasiswa.setOnClickListener {
            // TODO: masuk sebagai mahasiswa
        }
    }
}
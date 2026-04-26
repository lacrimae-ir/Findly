package com.example.findlynew

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvBarang = findViewById<RecyclerView>(R.id.rv_barang)
        rvBarang.layoutManager = GridLayoutManager(this, 2)
        
        val dummyList = listOf(
            Barang("4/13/2026", "DITEMUKAN", "DOMPET BALENCIAGA", android.R.drawable.ic_menu_gallery),
            Barang("4/13/2026", "DICARI", "IPONG 71", android.R.drawable.ic_menu_gallery),
            Barang("4/13/2026", "KEMBALI", "Jam Mitochiba", android.R.drawable.ic_menu_gallery),
            Barang("4/13/2026", "KEMBALI", "MacPrasasti", android.R.drawable.ic_menu_gallery),
            Barang("4/13/2026", "KEMBALI", "Sepatu Bata", android.R.drawable.ic_menu_gallery),
            Barang("4/13/2026", "DITEMUKAN", "Kacamata", android.R.drawable.ic_menu_gallery)
        )
        
        val adapter = BarangAdapter(dummyList)
        rvBarang.adapter = adapter

        // Setup Bottom Navbar
        val navHome = findViewById<android.widget.ImageButton>(R.id.nav_home)
        val navProfile = findViewById<android.widget.ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {
            // Already in Home
        }

        navProfile.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
    }
}
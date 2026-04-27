package com.example.findlynew

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        // Find views
        val ivFoto = findViewById<ImageView>(R.id.iv_detail_foto)
        val tvNama = findViewById<TextView>(R.id.tv_detail_nama)
        val tvKategori = findViewById<TextView>(R.id.tv_detail_kategori)
        val tvTanggal = findViewById<TextView>(R.id.tv_detail_tanggal)
        val tvLokasi = findViewById<TextView>(R.id.tv_detail_lokasi)
        val tvDeskripsi = findViewById<TextView>(R.id.tv_detail_deskripsi)
        val tvUploader = findViewById<TextView>(R.id.tv_detail_uploader)
        val tvKontak = findViewById<TextView>(R.id.tv_detail_kontak)

        // Get intent extras if any, or set mockup data for Balenciaga Wallet
        val namaBarang = intent.getStringExtra("EXTRA_NAMA") ?: "DOMPET BALENCIAGA"
        val statusBarang = intent.getStringExtra("EXTRA_STATUS") ?: "DITEMUKAN"
        val tanggalBarang = intent.getStringExtra("EXTRA_TANGGAL") ?: "4/13/2026"
        val imageResId = intent.getIntExtra("EXTRA_IMAGE", android.R.drawable.ic_menu_gallery)

        // Mockup data for specific fields not in the dashboard
        val deskripsiMock = "Dompet kulit warna hitam. Di dalamnya terdapat beberapa kartu identitas dan sedikit uang tunai. Ditemukan tergeletak di meja kantin."
        val lokasiMock = "Kantin Utama"
        val kategoriMock = "Barang Pribadi"
        val uploaderMock = "Budi Santoso"
        val kontakMock = "+6281234567890"

        // Set data
        ivFoto.setImageResource(imageResId)
        tvNama.text = namaBarang
        tvKategori.text = "Kategori: $kategoriMock"
        tvTanggal.text = "$statusBarang pada: $tanggalBarang"
        tvLokasi.text = "Lokasi: $lokasiMock"
        tvDeskripsi.text = deskripsiMock
        tvUploader.text = uploaderMock
        tvKontak.text = kontakMock

        // Setup Bottom Navbar
        val navHome = findViewById<ImageButton>(R.id.nav_home)
        val navPostAdd = findViewById<ImageButton>(R.id.nav_post_add)
        val navProfile = findViewById<ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        navPostAdd.setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}

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

        // Get intent extras
        val postId = intent.getIntExtra("EXTRA_POST_ID", -1)
        
        val dbHelper = DatabaseHelper(this)
        val barang = dbHelper.getPostById(postId)
        
        if (barang != null) {
            val uploaderName = dbHelper.getUserNameById(barang.userId)
            
            ivFoto.setImageURI(android.net.Uri.parse(barang.gambar))
            tvNama.text = barang.nama
            tvKategori.text = "Kategori: ${barang.kategori}"
            tvTanggal.text = "${barang.status} pada: ${barang.tanggal}"
            tvLokasi.text = "Lokasi: ${barang.lokasi}"
            tvDeskripsi.text = barang.deskripsi
            tvUploader.text = uploaderName
            tvKontak.text = barang.kontak
        } else {
            // Handle error, data not found
            tvNama.text = "Data Tidak Ditemukan"
            tvKategori.text = "Kategori: -"
            tvTanggal.text = "- pada: -"
            tvLokasi.text = "Lokasi: -"
            tvDeskripsi.text = "-"
            tvUploader.text = "-"
            tvKontak.text = "-"
        }

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

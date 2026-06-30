package com.example.findlynew

data class Barang(
    val id: String = "",
    val userId: String = "",
    val nama: String = "",
    val lokasi: String = "",
    val status: String = "",
    val kategori: String = "",
    val tanggal: String = "",
    val deskripsi: String = "",
    val kontak: String = "",
    val gambar: String = "",
    val selesai: Int = 0,
    val del: Boolean = false
)
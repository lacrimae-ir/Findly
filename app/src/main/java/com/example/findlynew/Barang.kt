package com.example.findlynew

data class Barang(
    val id: Int = 0,
    val userId: Int = 0,
    val nama: String,
    val lokasi: String,
    val status: String,
    val kategori: String,
    val tanggal: String,
    val deskripsi: String,
    val kontak: String,
    val gambar: String
)
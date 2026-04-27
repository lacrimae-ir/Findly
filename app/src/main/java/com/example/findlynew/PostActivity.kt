package com.example.findlynew

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class PostActivity : AppCompatActivity() {
    private lateinit var ivUpload: ImageView
    private var imageUri: Uri? = null

    // Buka Galeri Foto
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri : Uri? ->
        try {
            uri?.let {
                if (::ivUpload.isInitialized) {
                    ivUpload.setImageURI(it)
                    imageUri = it // save uri gambar untuk nanti di-upload ke database

                    // Hapus BG Setelah Upload
                    ivUpload.background = null

                    Toast.makeText(this, "Gambar Terpilih!", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e : Exception){
            e.printStackTrace()
            Toast.makeText(this, "Gagal Memuat Gambar", Toast.LENGTH_LONG).show()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Inisialisasi Komponen
        val etNamaBarang = findViewById<EditText>(R.id.et_nama_barang)
        val etLokasiBarang = findViewById<EditText>(R.id.et_lokasi_barang)
        val autoCompleteTipe = findViewById<AutoCompleteTextView>(R.id.auto_complete_tipe)
        val autoCompleteKategori = findViewById<AutoCompleteTextView>(R.id.auto_complete_kategori)
        val etTanggal = findViewById<EditText>(R.id.et_tanggal_barang)
        val etDeskripsiBarang = findViewById<EditText>(R.id.et_deskripsi_barang)
        val etKontakBarang = findViewById<EditText>(R.id.et_kontak_barang)
        ivUpload = findViewById<ImageView>(R.id.iv_upload_gambar)
        val btnSubmitForm = findViewById<Button>(R.id.btn_submit_form)

        // Logika Dropdown Tipe (Dicari/Ditemukan)
        val opsiTipe = arrayOf("Kehilangan (Lost)", "Penemuan (Found)")
        val adapterTipe = ArrayAdapter(this, R.layout.item_dropdown, opsiTipe)
        autoCompleteTipe.setAdapter(adapterTipe)

        // Logika Dropdown Kategori Barang
        val opsiKategori = arrayOf("Elektronik", "Uang", "Alat Tulis/Buku", "Barang Pribadi")
        val adapterKategori = ArrayAdapter(this, R.layout.item_dropdown, opsiKategori)
        autoCompleteKategori.setAdapter(adapterKategori)

        // Logika Tanggal (Date Picker)
        etTanggal.setFocusable(false) // Keyboard Tidak Muncul
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, {_, year, month, day ->
                val formattedDate = "$day/${month + 1}/$year" // Indeks Bulan Mulai dari 0
                etTanggal.setText(formattedDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        // Logika Upload Gambar
        ivUpload.setOnClickListener {
            getImage.launch("image/*") // Membuka Galeri
        }

        // Logika Button Submit
        btnSubmitForm.setOnClickListener {
            val nama = etNamaBarang.text.toString()
            val lokasi = etLokasiBarang.text.toString()
            val tipe = autoCompleteTipe.text.toString()
            val kategori = autoCompleteKategori.text.toString()
            val tanggal = etTanggal.text.toString()
            val deskripsi = etDeskripsiBarang.text.toString()
            val kontak = etKontakBarang.text.toString()

            when {
                nama.isEmpty() -> {
                    etNamaBarang.error = "Nama barang tidak boleh kosong"
                    etNamaBarang.requestFocus()
                }
                lokasi.isEmpty() -> {
                    etLokasiBarang.error = "Lokasi harus diisi"
                    etLokasiBarang.requestFocus()
                }
                tipe.isEmpty() || tipe == "Pilih Opsi" -> {
                    Toast.makeText(this, "Silakan pilih Dicari/Ditemukan terlebih dahulu", Toast.LENGTH_LONG).show()
                }
                kategori.isEmpty() || kategori == "Pilih Kategori" -> {
                    Toast.makeText(this, "Silakan pilih kategori barang terlebih dahulu", Toast.LENGTH_LONG).show()
                }
                tanggal.isEmpty() -> {
                    etTanggal.setHintTextColor(Color.RED)
                    Toast.makeText(this, "Silahkan pilih tanggal kejadian", Toast.LENGTH_LONG).show()
                }
                deskripsi.isEmpty() -> {
                    etDeskripsiBarang.error = "Deskripsi tidak boleh kosong"
                    etDeskripsiBarang.requestFocus()
                }
                deskripsi.length < 10 -> {
                    etDeskripsiBarang.error = "Deskripsi minimal 10 karakter"
                    etDeskripsiBarang.requestFocus()
                }
                kontak.isEmpty() -> {
                    etKontakBarang.error = "Nomor kontak tidak boleh kosong"
                    etKontakBarang.requestFocus()
                }
                kontak.length < 10 -> {
                    etKontakBarang.error = "Nomor kontak tidak valid"
                    etKontakBarang.requestFocus()
                }
                imageUri == null -> {
                    Toast.makeText(this, "Harap unggah foto", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Laporan $tipe Berhasil Diposting!", Toast.LENGTH_LONG).show()

                    // Balik ke Home Habis Submit
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        // Setup Bottom Navbar
        val navHome = findViewById<ImageButton>(R.id.nav_home)
        val navPostAdd = findViewById<ImageButton>(R.id.nav_post_add)
        val navProfile = findViewById<ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        
        navPostAdd.setOnClickListener {
            // Already in Post
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}

package com.example.findlynew
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*

class PostFragment : Fragment() {
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

                    Toast.makeText(requireContext(), "Gambar Terpilih!", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e : Exception){
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal Memuat Gambar", Toast.LENGTH_LONG).show()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        // Inisialisasi Komponen
        val etNamaBarang = view.findViewById<EditText>(R.id.et_nama_barang)
        val etLokasiBarang = view.findViewById<EditText>(R.id.et_lokasi_barang)
        val autoCompleteKategori = view.findViewById<AutoCompleteTextView>(R.id.auto_complete_kategori)
        val etTanggal = view.findViewById<EditText>(R.id.et_tanggal_barang)
        val etDeskripsiBarang = view.findViewById<EditText>(R.id.et_deskripsi_barang)
        ivUpload = view.findViewById<ImageView>(R.id.iv_upload_gambar)
        val btnSubmitForm = view.findViewById<Button>(R.id.btn_submit_form)

        // Logika Dropdown Kategori (Spinner)
        val opsiKategori = arrayOf("Kehilangan (Lost)", "Penemuan (Found)")
        val adapterKategori = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, opsiKategori)
        autoCompleteKategori.setAdapter(adapterKategori)

        // Logika Tanggal (Date Picker)
        etTanggal.setFocusable(false) // Keyboard Tidak Muncul
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), {_, year, month, day ->
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
            val kategori = autoCompleteKategori.text.toString()
            val tanggal = etTanggal.text.toString()
            val deskripsi = etDeskripsiBarang.text.toString()

            when {
                nama.isEmpty() -> {
                    etNamaBarang.error = "Nama barang tidak boleh kosong"
                    etNamaBarang.requestFocus()
                }
                lokasi.isEmpty() -> {
                    etLokasiBarang.error = "Lokasi harus diisi"
                    etLokasiBarang.requestFocus()
                }
                kategori.isEmpty() || kategori == "Pilih Kategori" -> {
                    Toast.makeText(requireContext(), "Silakan pilih kategori terlebih dahulu", Toast.LENGTH_LONG).show()
                }
                tanggal.isEmpty() -> {
                    etTanggal.setHintTextColor(Color.RED)
                    Toast.makeText(requireContext(), "Silahkan pilih tanggal kejadian", Toast.LENGTH_LONG).show()
                }
                deskripsi.isEmpty() -> {
                    etDeskripsiBarang.error = "Deskripsi tidak boleh kosong"
                    etDeskripsiBarang.requestFocus()
                }
                deskripsi.length < 10 -> {
                    etDeskripsiBarang.error = "Deskripsi minimal 10 karakter"
                    etDeskripsiBarang.requestFocus()
                }
                imageUri == null -> {
                    Toast.makeText(requireContext(), "Harap unggah foto", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(requireContext(), "Laporan $kategori Berhasil Diposting!", Toast.LENGTH_LONG).show()

                    // Balik ke Home Habis Submit
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
            }
        }
        return view
    }
}
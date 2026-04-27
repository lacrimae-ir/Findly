package com.example.findlynew

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.util.TypedValue
import android.view.ViewTreeObserver
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.bottomsheet.BottomSheetBehavior
class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        // Find views
        val ivFoto = findViewById<PhotoView>(R.id.iv_detail_foto)
        val tvNama = findViewById<TextView>(R.id.tv_detail_nama)
        val tvKategori = findViewById<TextView>(R.id.tv_detail_kategori)
        val tvTanggal = findViewById<TextView>(R.id.tv_detail_tanggal)
        val tvLokasi = findViewById<TextView>(R.id.tv_detail_lokasi)
        val tvDeskripsi = findViewById<TextView>(R.id.tv_detail_deskripsi)
        val tvUploader = findViewById<TextView>(R.id.tv_detail_uploader)
        val tvKontak = findViewById<TextView>(R.id.tv_detail_kontak)

        // Setup Bottom Sheet
        val bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator)
        
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        coordinatorLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                coordinatorLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val offsetPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics
                ).toInt()
                val layoutParams = bottomSheet.layoutParams
                layoutParams.height = coordinatorLayout.height - offsetPx
                bottomSheet.layoutParams = layoutParams
            }
        })

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

            // Check if current user is the owner
            val sessionManager = SessionManager(this)
            val currentUserEmail = sessionManager.getUserEmail()
            val currentUserId = if (currentUserEmail != null) dbHelper.getUserIdByEmail(currentUserEmail) else -1

            val llOwnerActions = findViewById<LinearLayout>(R.id.ll_owner_actions)
            val btnHapus = findViewById<Button>(R.id.btn_hapus)
            val btnKembali = findViewById<Button>(R.id.btn_kembali)

            if (currentUserId == barang.userId) {
                llOwnerActions.visibility = View.VISIBLE

                if (barang.status.equals("Kembali", ignoreCase = true)) {
                    btnKembali.visibility = View.GONE
                    val params = btnHapus.layoutParams as LinearLayout.LayoutParams
                    params.marginEnd = 0
                    btnHapus.layoutParams = params
                } else {
                    btnKembali.visibility = View.VISIBLE
                }

                btnHapus.setOnClickListener {
                    showCustomConfirmDialog("Konfirmasi", "Anda yakin ingin menghapus barang ini?") {
                        if (dbHelper.updatePostStatus(postId, "HAPUS")) {
                            Toast.makeText(this, "Barang berhasil dihapus", Toast.LENGTH_SHORT).show()
                            finish() // close activity and return
                        } else {
                            Toast.makeText(this, "Gagal menghapus barang", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                btnKembali.setOnClickListener {
                    showCustomConfirmDialog("Konfirmasi", "Anda yakin ingin menandai barang ini sudah kembali?") {
                        if (dbHelper.updatePostStatus(postId, "Kembali")) {
                            Toast.makeText(this, "Status barang berhasil diubah", Toast.LENGTH_SHORT).show()
                            btnKembali.visibility = View.GONE
                            val params = btnHapus.layoutParams as LinearLayout.LayoutParams
                            params.marginEnd = 0
                            btnHapus.layoutParams = params
                            tvTanggal.text = "Kembali pada: ${barang.tanggal}"
                        } else {
                            Toast.makeText(this, "Gagal mengubah status", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                llOwnerActions.visibility = View.GONE
            }
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

    private fun showCustomConfirmDialog(title: String, message: String, onOkClicked: () -> Unit) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_confirm)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_dialog_message)
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_dialog_cancel)
        val btnOk = dialog.findViewById<TextView>(R.id.btn_dialog_ok)
        
        tvTitle.text = title
        tvMessage.text = message
        
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener {
            dialog.dismiss()
            onOkClicked()
        }
        
        dialog.show()
    }
}

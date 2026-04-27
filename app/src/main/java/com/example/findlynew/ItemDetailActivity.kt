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
import android.view.MotionEvent
import android.view.VelocityTracker
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
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

        // Setup Bottom Sheet — behavior hanya untuk posisi awal, TIDAK untuk drag
        val bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator)
        val dragOverlay = findViewById<View>(R.id.drag_overlay)
        val floatingWrapper = findViewById<LinearLayout>(R.id.floating_actions_wrapper)

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = false  // KITA yang handle drag, bukan behavior
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        // collapsedTranslationY = jarak sheet harus turun agar hanya peekHeight yang terlihat
        var collapsedTranslationY = 0f

        // Helper: update semua elemen sesuai posisi translationY saat ini
        fun syncUI(translation: Float) {
            bottomSheet.translationY = translation
            dragOverlay.y = bottomSheet.y
            val progress = if (collapsedTranslationY > 0f)
                (translation / collapsedTranslationY).coerceIn(0f, 1f) else 0f
            floatingWrapper.translationY = floatingWrapper.height * progress
        }

        // Animasi snap ke target translationY
        fun snapTo(targetTranslation: Float) {
            val startTranslation = bottomSheet.translationY
            ValueAnimator.ofFloat(startTranslation, targetTranslation).apply {
                duration = 280
                interpolator = DecelerateInterpolator()
                addUpdateListener { syncUI(animatedValue as Float) }
                start()
            }
        }

        // VelocityTracker untuk deteksi flick
        var velocityTracker: VelocityTracker? = null
        var dragStartRawY = 0f
        var dragStartTranslation = 0f

        dragOverlay.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    velocityTracker?.recycle()
                    velocityTracker = VelocityTracker.obtain()
                    velocityTracker?.addMovement(event)
                    dragStartRawY = event.rawY
                    dragStartTranslation = bottomSheet.translationY
                }
                MotionEvent.ACTION_MOVE -> {
                    velocityTracker?.addMovement(event)
                    val deltaY = event.rawY - dragStartRawY
                    val newTranslation = (dragStartTranslation + deltaY)
                        .coerceIn(0f, collapsedTranslationY)
                    syncUI(newTranslation)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    velocityTracker?.addMovement(event)
                    velocityTracker?.computeCurrentVelocity(1000)
                    val vy = velocityTracker?.yVelocity ?: 0f
                    val progress = if (collapsedTranslationY > 0f)
                        bottomSheet.translationY / collapsedTranslationY else 0f

                    // Tentukan arah snap berdasarkan velocity atau posisi
                    val shouldCollapse = when {
                        vy > 300f  -> true   // flick ke bawah
                        vy < -300f -> false  // flick ke atas
                        else -> progress >= 0.3f  // slow drag: threshold 30%
                    }
                    snapTo(if (shouldCollapse) collapsedTranslationY else 0f)

                    velocityTracker?.recycle()
                    velocityTracker = null
                }
            }
            true  // konsumsi semua touch, jangan forward ke behavior
        }

        // Hitung tinggi sheet dan collapsedTranslationY setelah layout selesai
        coordinatorLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                coordinatorLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val offsetPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics
                ).toInt()
                val peekPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics
                ).toInt()
                val layoutParams = bottomSheet.layoutParams
                layoutParams.height = coordinatorLayout.height - offsetPx
                bottomSheet.layoutParams = layoutParams

                bottomSheet.post {
                    // Jarak yang harus ditempuh dari expanded ke peek position
                    collapsedTranslationY = (bottomSheet.height - peekPx).toFloat()
                    syncUI(0f)  // mulai di posisi expanded
                }
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

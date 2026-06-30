package com.example.findlynew

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var profileImageUri: Uri? = null

    private val getProfileImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        try {
            uri?.let {
                val destinationUri = Uri.fromFile(java.io.File(cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg"))
                val options = com.yalantis.ucrop.UCrop.Options().apply {
                    setCircleDimmedLayer(true) // Tampilan crop melingkar premium
                    setShowCropGrid(false)
                    setShowCropFrame(false)
                    setCropFrameColor(android.graphics.Color.TRANSPARENT)
                    setToolbarColor(android.graphics.Color.parseColor("#236CDF"))
                    setStatusBarColor(android.graphics.Color.parseColor("#1B52B7"))
                    setToolbarWidgetColor(android.graphics.Color.WHITE)
                    setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                    setCompressionQuality(90)
                }
                com.yalantis.ucrop.UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                    .withOptions(options)
                    .start(this@EditProfileActivity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal Memuat Gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == com.yalantis.ucrop.UCrop.REQUEST_CROP) {
            val resultUri = com.yalantis.ucrop.UCrop.getOutput(data!!)
            resultUri?.let {
                val ivEditProfilePicture = findViewById<ShapeableImageView>(R.id.ivEditProfilePicture)
                com.bumptech.glide.Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(ivEditProfilePicture)
                profileImageUri = it
                Toast.makeText(this, "Foto Profil Berhasil Dipotong!", Toast.LENGTH_SHORT).show()
            }
        } else if (resultCode == com.yalantis.ucrop.UCrop.RESULT_ERROR) {
            val cropError = com.yalantis.ucrop.UCrop.getError(data!!)
            cropError?.printStackTrace()
            Toast.makeText(this, "Gagal memotong gambar", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = SessionManager(this)

        // Inisialisasi View
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tvEditProfileName = findViewById<TextView>(R.id.tvEditProfileName)
        val ivEditProfilePicture = findViewById<ShapeableImageView>(R.id.ivEditProfilePicture)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etMobileNumber = findViewById<TextInputEditText>(R.id.etMobileNumber)
        val etEditPassword = findViewById<TextInputEditText>(R.id.etEditPassword)
        val etEditConfirmPassword = findViewById<TextInputEditText>(R.id.etEditConfirmPassword)
        val btnSaveChanges = findViewById<Button>(R.id.btnSaveChanges)

        // Set Data Awal dari Session (jika ada)
        val email = sessionManager.getUserEmail() ?: ""
        tvEditProfileName.text = sessionManager.getUserName()
        etEmail.setText(email)

        val profilePhone = sessionManager.getPhone(email)
        if (!profilePhone.isNullOrEmpty()) {
            etMobileNumber.setText(profilePhone)
        }

        val profilePicUrl = sessionManager.getProfilePic(email)
        if (!profilePicUrl.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(profilePicUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(ivEditProfilePicture)
        }

        // Aksi Klik Foto Profil untuk Upload
        ivEditProfilePicture.setOnClickListener {
            getProfileImage.launch("image/*")
        }
        val ivEditPencil = findViewById<android.widget.ImageView>(R.id.ivEditPencil)
        ivEditPencil.setOnClickListener {
            getProfileImage.launch("image/*")
        }

        // 1. Aksi Tombol Back -> Kembali ke SettingsActivity
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Aksi Tombol Save Changes
        btnSaveChanges.setOnClickListener {
            val emailText = etEmail.text.toString().trim()
            val phone = etMobileNumber.text.toString().trim()
            val password = etEditPassword.text.toString().trim()
            val confirmPassword = etEditConfirmPassword.text.toString().trim()

            // Validasi Sederhana
            if (emailText.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Email dan Nomor Telepon harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isNotEmpty() && password != confirmPassword) {
                etEditConfirmPassword.error = "Password tidak cocok!"
                return@setOnClickListener
            }

            val progressSave = android.app.ProgressDialog(this@EditProfileActivity).apply {
                setMessage("Menyimpan perubahan...")
                setCancelable(false)
                show()
            }

            val uid = sessionManager.getUserUid() ?: ""

            // local success helper
            val finalizeChanges = { finalProfilePic: String?, newPasswordHash: String? ->
                if (finalProfilePic != null) {
                    sessionManager.saveProfilePic(email, finalProfilePic)
                }
                if (newPasswordHash != null) {
                    sessionManager.savePasswordHash(newPasswordHash)
                }
                sessionManager.savePhone(email, phone)
                progressSave.dismiss()
                Toast.makeText(this@EditProfileActivity, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }

            val saveFirebaseData = { finalProfilePic: String? ->
                // Update phone in firebase
                FirebaseManager.updatePhone(uid, phone) { phoneSuccess ->
                    runOnUiThread {
                        if (phoneSuccess) {
                            val onPhoneAndPicDone = { newPasswordHash: String? ->
                                if (finalProfilePic != null) {
                                    FirebaseManager.updateProfilePic(uid, finalProfilePic) { picSuccess ->
                                        runOnUiThread {
                                            finalizeChanges(finalProfilePic, newPasswordHash)
                                        }
                                    }
                                } else {
                                    finalizeChanges(null, newPasswordHash)
                                }
                            }

                            if (password.isNotEmpty()) {
                                FirebaseManager.updatePassword(email, password) { newHash ->
                                    runOnUiThread {
                                        if (newHash != null) {
                                            onPhoneAndPicDone(newHash)
                                        } else {
                                            progressSave.dismiss()
                                            Toast.makeText(this@EditProfileActivity, "Gagal memperbarui password", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                onPhoneAndPicDone(null)
                            }
                        } else {
                            progressSave.dismiss()
                            Toast.makeText(this@EditProfileActivity, "Gagal menyimpan perubahan ke Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            if (profileImageUri != null) {
                progressSave.setMessage("Mengunggah foto profil...")
                DriveImageUploader.uploadImage(this@EditProfileActivity, profileImageUri!!, "profile") { driveUrl ->
                    runOnUiThread {
                        if (driveUrl != null) {
                            saveFirebaseData(driveUrl)
                        } else {
                            progressSave.dismiss()
                            Toast.makeText(this@EditProfileActivity, "Gagal mengunggah foto profil ke Google Drive", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                saveFirebaseData(null)
            }
        }
    }
}
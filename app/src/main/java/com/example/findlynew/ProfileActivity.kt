package com.example.findlynew

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        val menuSettings = findViewById<LinearLayout>(R.id.menuSettings)
        val menuPreferencesProfile = findViewById<LinearLayout>(R.id.menuPreferencesProfile)
        val menuLogout = findViewById<LinearLayout>(R.id.menuLogout)

        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            loadProfileData()
        }

        loadProfileData()

        // Menu Actions
        menuSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        menuPreferencesProfile.setOnClickListener {
            startActivity(Intent(this, SurveyPreferencesActivity::class.java))
        }

        // Logout
        menuLogout.setOnClickListener {
            try {
                stopService(Intent(this, NotificationService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            // Clear entire activity stack so user can't go back
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Setup Bottom Navbar
        val navHome = findViewById<android.widget.ImageButton>(R.id.nav_home)
        val navPostAdd = findViewById<android.widget.ImageButton>(R.id.nav_post_add)
        val navProfile = findViewById<android.widget.ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        navPostAdd.setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
            finish()
        }

        navProfile.setOnClickListener {
            // Already in Profile
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val ivProfilePicture = findViewById<android.widget.ImageView>(R.id.ivProfilePicture)
        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipe_refresh)

        val email = sessionManager.getUserEmail() ?: ""
        tvProfileName.text = sessionManager.getUserName()
        tvProfileEmail.text = email

        val profilePicUrl = sessionManager.getProfilePic(email)
        if (!profilePicUrl.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(profilePicUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .circleCrop()
                .into(ivProfilePicture)
        } else {
            ivProfilePicture.setImageResource(R.drawable.profile)
        }

        val uid = sessionManager.getUserUid() ?: ""
        if (uid.isNotEmpty()) {
            val localHash = sessionManager.getPasswordHash() ?: ""
            FirebaseManager.getUserById(uid) { user ->
                runOnUiThread {
                    swipeRefresh.isRefreshing = false
                    if (user != null) {
                        if (localHash.isNotEmpty()) {
                            if (user.password != localHash) {
                                forceLogout("Sesi Anda telah berakhir karena password telah diubah.")
                                return@runOnUiThread
                            }
                        } else {
                            sessionManager.savePasswordHash(user.password)
                        }

                        sessionManager.saveUserName(user.username)
                        sessionManager.savePhone(user.email, user.phone)
                        if (user.profilePic.isNotEmpty()) {
                            sessionManager.saveProfilePic(user.email, user.profilePic)
                        }

                        tvProfileName.text = user.username
                        if (user.profilePic.isNotEmpty()) {
                            com.bumptech.glide.Glide.with(this@ProfileActivity)
                                .load(user.profilePic)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .circleCrop()
                                .into(ivProfilePicture)
                        }
                    }
                }
            }
        } else {
            swipeRefresh.isRefreshing = false
        }
    }

    private fun forceLogout(message: String) {
        try {
            stopService(Intent(this, NotificationService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sessionManager.logout()
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
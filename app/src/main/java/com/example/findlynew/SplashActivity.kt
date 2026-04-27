package com.example.findlynew

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sessionManager = SessionManager(this)

        // Delay 2.5 detik
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                val email = sessionManager.getUserEmail() ?: ""
                val dbHelper = DatabaseHelper(this)
                
                // Verifikasi apakah user masih ada di database (berguna saat db di-reset)
                if (dbHelper.checkEmailExists(email)) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    sessionManager.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            } else {
                startActivity(Intent(this, SignupActivity::class.java))
            }
            finish()
        }, 2500)
    }
}
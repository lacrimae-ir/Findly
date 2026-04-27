package com.example.findlynew

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Setup Bottom Navbar
        val navHome = findViewById<android.widget.ImageButton>(R.id.nav_home)
        val navPostAdd = findViewById<android.widget.ImageButton>(R.id.nav_post_add)
        val navProfile = findViewById<android.widget.ImageButton>(R.id.nav_profile)

        navHome.setOnClickListener {
            startActivity(android.content.Intent(this, MainActivity::class.java))
        }

        navPostAdd.setOnClickListener {
            startActivity(android.content.Intent(this, PostActivity::class.java))
        }

        navProfile.setOnClickListener {
            // Already in Profile
        }
    }
}
package com.example.findlynew

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SurveyPreferencesDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_preferences_detail)

        val etPreferenceDetail = findViewById<EditText>(R.id.etPreferenceDetail)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)

        val sessionManager = SessionManager(this)
        val dbHelper = DatabaseHelper(this)
        val email = sessionManager.getUserEmail() ?: ""

        // Load existing detail if available
        if (email.isNotEmpty()) {
            val existingDetail = dbHelper.getPreferenceDetail(email)
            if (existingDetail.isNotEmpty()) {
                etPreferenceDetail.setText(existingDetail)
            }
        }

        btnSimpan.setOnClickListener {
            val detailText = etPreferenceDetail.text.toString().trim()

            if (email.isNotEmpty()) {
                dbHelper.savePreferenceDetail(email, detailText)
                sessionManager.setSurveyCompleted(email, true)
            }

            Toast.makeText(this, "Preferensi berhasil disimpan!", Toast.LENGTH_SHORT).show()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}

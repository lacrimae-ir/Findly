package com.example.findlynew

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SurveyPreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey_preferences)

        val buttons = listOf(
            findViewById<Button>(R.id.btnElektronik),
            findViewById<Button>(R.id.btnUang),
            findViewById<Button>(R.id.btnAlatTulis),
            findViewById<Button>(R.id.btnPakaian),
            findViewById<Button>(R.id.btnAksesoris),
            findViewById<Button>(R.id.btnOthers)
        )

        val sessionManager = SessionManager(this)
        val dbHelper = DatabaseHelper(this)
        val email = sessionManager.getUserEmail() ?: ""
        val savedPreferences = dbHelper.getUserPreferences(email)

        val selectedStates = mutableMapOf<Button, Boolean>()

        for (button in buttons) {
            val category = button.text.toString()
            val isSelectedInitially = savedPreferences.any { it.equals(category, ignoreCase = true) }
            selectedStates[button] = isSelectedInitially

            if (isSelectedInitially) {
                button.setBackgroundResource(R.drawable.bg_survey_button_selected)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.bg_survey_button_unselected)
                button.setTextColor(Color.parseColor("#5B7FFF"))
            }

            button.setOnClickListener {
                val isSelected = selectedStates[button] ?: false
                if (isSelected) {
                    // Deselect
                    button.setBackgroundResource(R.drawable.bg_survey_button_unselected)
                    button.setTextColor(Color.parseColor("#5B7FFF"))
                    selectedStates[button] = false
                } else {
                    // Select
                    button.setBackgroundResource(R.drawable.bg_survey_button_selected)
                    button.setTextColor(Color.WHITE)
                    selectedStates[button] = true
                }
            }
        }

        val btnLanjut = findViewById<Button>(R.id.btnLanjut)
        btnLanjut.setOnClickListener {
            if (email.isNotEmpty()) {
                val currentPreferences = selectedStates.filter { it.value }.keys.map { button ->
                    val text = button.text.toString().trim()
                    when (text.uppercase(java.util.Locale.ROOT)) {
                        "ELEKTRONIK" -> "Elektronik"
                        "UANG" -> "Uang"
                        "ALAT TULIS / BUKU", "ALAT TULIS/BUKU" -> "Alat Tulis/Buku"
                        "PAKAIAN" -> "Pakaian"
                        "AKSESORIS" -> "Aksesoris"
                        "OTHERS", "LAINNYA" -> "Lainnya"
                        else -> text
                    }
                }.toSet()
                dbHelper.saveUserPreferences(email, currentPreferences)
            }
            val intent = Intent(this, SurveyPreferencesDetailActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

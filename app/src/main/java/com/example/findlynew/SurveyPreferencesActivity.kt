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
        val email = sessionManager.getUserEmail() ?: ""
        val savedPreferences = sessionManager.getUserPreferences(email)

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
                val currentPreferences = selectedStates.filter { it.value }.keys.map { it.text.toString() }.toSet()
                sessionManager.saveUserPreferences(email, currentPreferences)
                sessionManager.setSurveyCompleted(email, true)
            }
            // If opened from settings, we might just want to finish(). But for simplicity, going to MainActivity is fine.
            // Or better, check if the calling activity was SettingsActivity. If so, just finish().
            // But going to MainActivity is safe. Let's just finish() if it's not from Login/Signup.
            // A simple way is to check if it's from Settings. Let's just finish() to go back to previous screen.
            // Wait, LoginActivity calls finish() after starting SurveyPreferencesActivity. 
            // So if we just finish(), the app will close if coming from LoginActivity!
            // Let's explicitly go to MainActivity if we are coming from Login (where we might not have a backstack).
            // Since LoginActivity starts this and calls finish(), we MUST start MainActivity.
            // However, from SettingsActivity we don't call finish(). 
            // So we can start MainActivity with Intent.FLAG_ACTIVITY_CLEAR_TOP.
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}

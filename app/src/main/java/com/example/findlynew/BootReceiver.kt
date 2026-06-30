package com.example.findlynew

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sessionManager = SessionManager(context)
            if (sessionManager.isLoggedIn()) {
                val serviceIntent = Intent(context, NotificationService::class.java)
                try {
                    context.startService(serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

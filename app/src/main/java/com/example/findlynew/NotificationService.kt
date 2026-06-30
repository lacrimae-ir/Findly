package com.example.findlynew

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationService : Service() {

    private var databaseListener: ValueEventListener? = null
    private val postsRef = FirebaseDatabase.getInstance().getReference("posts")

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startFirebaseListener()
        return START_STICKY
    }

    private fun startFirebaseListener() {
        if (databaseListener != null) return

        databaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sessionManager = SessionManager(this@NotificationService)
                val email = sessionManager.getUserEmail() ?: ""
                if (email.isEmpty()) return

                val dbHelper = DatabaseHelper(this@NotificationService)
                val preferences = dbHelper.getUserPreferences(email)
                val detailText = dbHelper.getPreferenceDetail(email)

                val savedNotifIds = dbHelper.getNotificationIds(email).map { it.first }.toSet()

                val posts = ArrayList<Barang>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Barang::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }

                for (post in posts) {
                    if (post.status != "HAPUS" && !post.del && !savedNotifIds.contains(post.id)) {
                        val points = calculatePoints(post, preferences, detailText)
                        val categoryMatched = preferences.any { it.equals(post.kategori, ignoreCase = true) }
                        if (points >= 15 || categoryMatched) {
                            dbHelper.insertNotification(email, post.id)
                            if (sessionManager.isPushNotificationsEnabled()) {
                                triggerSystemNotification(post.nama, post.id)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        postsRef.addValueEventListener(databaseListener!!)
    }

    private fun calculatePoints(post: Barang, categories: Set<String>, detailText: String): Int {
        var points = 0
        val categoryMatched = categories.any { it.equals(post.kategori, ignoreCase = true) }
        if (categoryMatched) {
            points += 10
        }

        val cleanNama = post.nama.lowercase()
        val cleanDetail = detailText.lowercase()

        if (cleanDetail.isNotEmpty()) {
            if (cleanNama.contains(cleanDetail)) {
                points += 15
            } else {
                val words = cleanDetail.split(Regex("\\s+")).filter { it.length > 2 }
                for (word in words) {
                    if (cleanNama.contains(word)) {
                        points += 5
                        break
                    }
                }
            }

            val cleanDeskripsi = post.deskripsi.lowercase()
            if (cleanDeskripsi.contains(cleanDetail)) {
                points += 10
            } else {
                val words = cleanDetail.split(Regex("\\s+")).filter { it.length > 2 }
                for (word in words) {
                    if (cleanDeskripsi.contains(word)) {
                        points += 2
                        break
                    }
                }
            }
        }

        return points
    }

    private fun triggerSystemNotification(namaBarang: String, postId: String) {
        val intent = Intent(this, ItemDetailActivity::class.java).apply {
            putExtra("EXTRA_POST_ID", postId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            postId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "FINDLY_NOTIFICATIONS")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Barang temuan baru cocok dengan Anda!")
            .setContentText(namaBarang)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Findly Notifications"
            val descriptionText = "Notifikasi untuk barang temuan baru"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("FINDLY_NOTIFICATIONS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseListener?.let {
            postsRef.removeEventListener(it)
        }
    }
}

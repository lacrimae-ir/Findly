package com.example.findlynew

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList
import java.util.HashSet

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        initializeInMemory(context)
    }

    companion object {
        private const val DATABASE_VERSION = 11
        private const val DATABASE_NAME = "FindlyDB.db"

        // Tabel Preferences
        private const val TABLE_PREFERENCES = "preferences"
        private const val COLUMN_PREF_ID = "pref_id"
        private const val COLUMN_PREF_EMAIL = "user_email"
        private const val COLUMN_PREF_CATEGORY = "category_name"

        // Tabel Preference Details
        private const val TABLE_PREF_DETAILS = "preference_details"
        private const val COLUMN_DET_ID = "detail_id"
        private const val COLUMN_DET_EMAIL = "user_email"
        private const val COLUMN_DET_TEXT = "additional_info"

        // Tabel Notifications
        private const val TABLE_NOTIFS = "notifications_store"
        private const val COLUMN_NOTIF_ID = "notif_store_id"
        private const val COLUMN_NOTIF_EMAIL = "user_email"
        private const val COLUMN_NOTIF_POST_ID = "post_id"
        private const val COLUMN_NOTIF_TIMESTAMP = "timestamp"
        private const val COLUMN_NOTIF_IS_READ = "is_read"

        // Tabel Search History
        private const val TABLE_SEARCH = "search_history"
        private const val COLUMN_SEARCH_ID = "search_id"
        private const val COLUMN_SEARCH_EMAIL = "user_email"
        private const val COLUMN_SEARCH_QUERY = "query"
        private const val COLUMN_SEARCH_TIMESTAMP = "timestamp"

        // In-Memory Data for Mock/Transition to Firebase
        data class TempUser(
            val id: String,
            val name: String,
            val email: String,
            var password: String
        )

        private val usersList = mutableListOf<TempUser>().apply {
            add(TempUser("1", "Admin Findly", "admin@findly.com", "password123"))
        }
        private val postsList = mutableListOf<Barang>()
        private var isInMemoryInitialized = false

        private fun initializeInMemory(context: Context) {
            if (isInMemoryInitialized) return
            val file = java.io.File(context.filesDir, "itemplaceholder.png")
            if (!file.exists()) {
                try {
                    val resId = context.resources.getIdentifier("itemplaceholder", "drawable", context.packageName)
                    if (resId != 0) {
                        val inputStream = context.resources.openRawResource(resId)
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val imagePath = file.absolutePath

            // Initial 5 Dummy Posts
            postsList.add(Barang("1", "1", "Dompet Kulit Cokelat", "Kantin FISIP", "Hilang", "Uang", "28/6/2026", "Dompet kulit berwarna cokelat merk Eiger, berisi KTP atas nama Rian, KTM, dan beberapa uang tunai.", "081234567890", imagePath, 0, false))
            postsList.add(Barang("2", "1", "Kunci Motor Honda", "Parkiran Gd. FIK", "Ditemukan", "Lainnya", "29/6/2026", "Ditemukan gantungan kunci motor Honda dengan gantungan boneka kecil di parkiran motor FIK.", "089876543210", imagePath, 0, false))
            postsList.add(Barang("3", "1", "Laptop Asus TUF", "Perpustakaan Pusat", "Hilang", "Elektronik", "25/6/2026", "Laptop Asus TUF Gaming warna hitam, tertinggal di meja lantai 2 perpustakaan pusat. Ada stiker anime di cover depan.", "081211223344", imagePath, 0, false))
            postsList.add(Barang("4", "1", "Tumbler Hydro Flask", "Gd. Rektorat Lt. 3", "Ditemukan", "Lainnya", "30/6/2026", "Ditemukan tumbler Hydro Flask warna biru muda di selasar depan ruang rapat rektorat.", "085544332211", imagePath, 0, false))
            postsList.add(Barang("5", "1", "Kacamata Hitam", "Masjid UPNVJ", "Hilang", "Aksesoris", "27/6/2026", "Kacamata minus dengan frame hitam bulat. Terakhir diletakkan di tempat wudhu laki-laki masjid.", "087788990011", imagePath, 0, false))

            isInMemoryInitialized = true
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTablePreferences = ("CREATE TABLE " + TABLE_PREFERENCES + "("
                + COLUMN_PREF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PREF_EMAIL + " TEXT,"
                + COLUMN_PREF_CATEGORY + " TEXT" + ")")
        db.execSQL(createTablePreferences)

        val createTablePrefDetails = ("CREATE TABLE " + TABLE_PREF_DETAILS + "("
                + COLUMN_DET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DET_EMAIL + " TEXT UNIQUE,"
                + COLUMN_DET_TEXT + " TEXT" + ")")
        db.execSQL(createTablePrefDetails)

        val createTableNotifs = ("CREATE TABLE " + TABLE_NOTIFS + "("
                + COLUMN_NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTIF_EMAIL + " TEXT,"
                + COLUMN_NOTIF_POST_ID + " TEXT,"
                + COLUMN_NOTIF_TIMESTAMP + " INTEGER,"
                + COLUMN_NOTIF_IS_READ + " INTEGER DEFAULT 0,"
                + "UNIQUE(" + COLUMN_NOTIF_EMAIL + ", " + COLUMN_NOTIF_POST_ID + ") ON CONFLICT IGNORE)")
        db.execSQL(createTableNotifs)

        val createTableSearch = ("CREATE TABLE " + TABLE_SEARCH + "("
                + COLUMN_SEARCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SEARCH_EMAIL + " TEXT,"
                + COLUMN_SEARCH_QUERY + " TEXT,"
                + COLUMN_SEARCH_TIMESTAMP + " INTEGER" + ")")
        db.execSQL(createTableSearch)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREF_DETAILS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH)
        db.execSQL("DROP TABLE IF EXISTS posts")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // Preferences Operations (SQLite)
    fun saveUserPreferences(email: String, preferences: Set<String>): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PREFERENCES, "$COLUMN_PREF_EMAIL = ?", arrayOf(email))
            for (pref in preferences) {
                val values = ContentValues().apply {
                    put(COLUMN_PREF_EMAIL, email)
                    put(COLUMN_PREF_CATEGORY, pref)
                }
                db.insert(TABLE_PREFERENCES, null, values)
            }
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getUserPreferences(email: String): Set<String> {
        val preferences = HashSet<String>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PREFERENCES,
            arrayOf(COLUMN_PREF_CATEGORY),
            "$COLUMN_PREF_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val categoryIndex = cursor.getColumnIndex(COLUMN_PREF_CATEGORY)
            if (categoryIndex != -1) {
                do {
                    preferences.add(cursor.getString(categoryIndex))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return preferences
    }

    fun savePreferenceDetail(email: String, details: String): Boolean {
        val db = this.writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_DET_EMAIL, email)
                put(COLUMN_DET_TEXT, details)
            }
            val affected = db.insertWithOnConflict(
                TABLE_PREF_DETAILS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            return affected != -1L
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.close()
        }
    }

    fun getPreferenceDetail(email: String): String {
        var detail = ""
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PREF_DETAILS,
            arrayOf(COLUMN_DET_TEXT),
            "$COLUMN_DET_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val textIndex = cursor.getColumnIndex(COLUMN_DET_TEXT)
            if (textIndex != -1) {
                detail = cursor.getString(textIndex) ?: ""
            }
        }
        cursor.close()
        db.close()
        return detail
    }

    fun insertNotification(email: String, postId: String): Boolean {
        val db = this.writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_NOTIF_EMAIL, email)
                put(COLUMN_NOTIF_POST_ID, postId)
                put(COLUMN_NOTIF_TIMESTAMP, System.currentTimeMillis())
            }
            val affected = db.insert(TABLE_NOTIFS, null, values)
            return affected != -1L
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.close()
        }
    }

    fun getNotificationIds(email: String): List<Pair<String, Boolean>> {
        val list = ArrayList<Pair<String, Boolean>>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NOTIFS,
            arrayOf(COLUMN_NOTIF_POST_ID, COLUMN_NOTIF_IS_READ),
            "$COLUMN_NOTIF_EMAIL = ?",
            arrayOf(email),
            null, null,
            "$COLUMN_NOTIF_TIMESTAMP DESC"
        )
        if (cursor.moveToFirst()) {
            val postIndex = cursor.getColumnIndex(COLUMN_NOTIF_POST_ID)
            val readIndex = cursor.getColumnIndex(COLUMN_NOTIF_IS_READ)
            if (postIndex != -1 && readIndex != -1) {
                do {
                    val postId = cursor.getString(postIndex)
                    val isRead = cursor.getInt(readIndex) == 1
                    list.add(Pair(postId, isRead))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return list
    }

    fun getUnreadNotificationCount(email: String): Int {
        var count = 0
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_NOTIFS WHERE $COLUMN_NOTIF_EMAIL = ? AND $COLUMN_NOTIF_IS_READ = 0",
            arrayOf(email)
        )
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    fun markNotificationsAsRead(email: String) {
        val db = this.writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_NOTIF_IS_READ, 1)
            }
            db.update(TABLE_NOTIFS, values, "$COLUMN_NOTIF_EMAIL = ?", arrayOf(email))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    // Search History Operations (SQLite)
    fun insertSearchQuery(email: String, query: String): Boolean {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return false
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_SEARCH, "$COLUMN_SEARCH_EMAIL = ? AND $COLUMN_SEARCH_QUERY = ?", arrayOf(email, trimmedQuery))
            val values = ContentValues().apply {
                put(COLUMN_SEARCH_EMAIL, email)
                put(COLUMN_SEARCH_QUERY, trimmedQuery)
                put(COLUMN_SEARCH_TIMESTAMP, System.currentTimeMillis())
            }
            db.insert(TABLE_SEARCH, null, values)
            
            // Limit search history to 10 entries per user
            val deleteQuery = "DELETE FROM $TABLE_SEARCH WHERE $COLUMN_SEARCH_ID NOT IN (" +
                    "SELECT $COLUMN_SEARCH_ID FROM $TABLE_SEARCH " +
                    "WHERE $COLUMN_SEARCH_EMAIL = ? " +
                    "ORDER BY $COLUMN_SEARCH_TIMESTAMP DESC LIMIT 10)"
            db.execSQL(deleteQuery, arrayOf(email))
            
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getSearchHistory(email: String): List<String> {
        val history = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_SEARCH,
            arrayOf(COLUMN_SEARCH_QUERY),
            "$COLUMN_SEARCH_EMAIL = ?",
            arrayOf(email),
            null, null,
            "$COLUMN_SEARCH_TIMESTAMP DESC"
        )
        if (cursor.moveToFirst()) {
            val queryIndex = cursor.getColumnIndex(COLUMN_SEARCH_QUERY)
            if (queryIndex != -1) {
                do {
                    history.add(cursor.getString(queryIndex))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return history
    }

    fun deleteSearchQuery(email: String, query: String): Boolean {
        val db = this.writableDatabase
        val success = db.delete(TABLE_SEARCH, "$COLUMN_SEARCH_EMAIL = ? AND $COLUMN_SEARCH_QUERY = ?", arrayOf(email, query))
        db.close()
        return success > 0
    }

    fun clearSearchHistory(email: String): Boolean {
        val db = this.writableDatabase
        val success = db.delete(TABLE_SEARCH, "$COLUMN_SEARCH_EMAIL = ?", arrayOf(email))
        db.close()
        return success > 0
    }

    // In-Memory User Operations
    fun insertUser(name: String, email: String, password: String): Boolean {
        if (checkEmailExists(email)) return false
        val newId = if (usersList.isEmpty()) "1" else ((usersList.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
        usersList.add(TempUser(newId, name, email, password))
        return true
    }

    fun checkUser(email: String, password: String): Boolean {
        return usersList.any { it.email.equals(email, ignoreCase = true) && it.password == password }
    }

    fun checkEmailExists(email: String): Boolean {
        return usersList.any { it.email.equals(email, ignoreCase = true) }
    }

    fun getUserName(email: String): String {
        return usersList.find { it.email.equals(email, ignoreCase = true) }?.name ?: ""
    }

    fun getUserIdByEmail(email: String): String {
        return usersList.find { it.email.equals(email, ignoreCase = true) }?.id ?: ""
    }

    fun getUserNameById(userId: String): String {
        return usersList.find { it.id == userId }?.name ?: "Unknown"
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val user = usersList.find { it.email.equals(email, ignoreCase = true) }
        return if (user != null) {
            user.password = newPassword
            true
        } else {
            false
        }
    }

    // In-Memory Post Operations
    fun insertPost(userId: String, nama: String, lokasi: String, status: String, kategori: String, tanggal: String, deskripsi: String, kontak: String, gambar: String): Boolean {
        val newId = if (postsList.isEmpty()) "1" else ((postsList.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
        postsList.add(Barang(newId, userId, nama, lokasi, status, kategori, tanggal, deskripsi, kontak, gambar, 0, false))
        return true
    }

    fun getAllPosts(): List<Barang> {
        return ArrayList(postsList)
    }

    fun getPostById(postId: String): Barang? {
        return postsList.find { it.id == postId }
    }

    fun updatePostStatus(postId: String, newStatus: String): Boolean {
        val post = postsList.find { it.id == postId }
        return if (post != null) {
            val index = postsList.indexOf(post)
            val updatedPost = post.copy(status = newStatus)
            postsList[index] = updatedPost
            true
        } else {
            false
        }
    }

    fun updatePostSelesai(postId: String, selesai: Int): Boolean {
        val post = postsList.find { it.id == postId }
        return if (post != null) {
            val index = postsList.indexOf(post)
            val updatedPost = post.copy(selesai = selesai)
            postsList[index] = updatedPost
            true
        } else {
            false
        }
    }
}

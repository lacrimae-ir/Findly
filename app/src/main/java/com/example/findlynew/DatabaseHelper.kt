package com.example.findlynew

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "FindlyDB.db"

        // Tabel User
        private const val TABLE_USER = "users"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_USER_NAME = "user_name"
        private const val COLUMN_USER_EMAIL = "user_email"
        private const val COLUMN_USER_PASSWORD = "user_password"

        // Tabel Post
        private const val TABLE_POST = "posts"
        private const val COLUMN_POST_ID = "post_id"
        private const val COLUMN_POST_USER_ID = "user_id"
        private const val COLUMN_POST_NAMA_BARANG = "nama_barang"
        private const val COLUMN_POST_LOKASI = "lokasi"
        private const val COLUMN_POST_STATUS = "status"
        private const val COLUMN_POST_KATEGORI = "kategori"
        private const val COLUMN_POST_TANGGAL = "tanggal"
        private const val COLUMN_POST_DESKRIPSI = "deskripsi"
        private const val COLUMN_POST_KONTAK = "kontak"
        private const val COLUMN_POST_GAMBAR = "gambar"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableUser = ("CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT" + ")")
        db.execSQL(createTableUser)

        val createTablePost = ("CREATE TABLE " + TABLE_POST + "("
                + COLUMN_POST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_POST_USER_ID + " INTEGER,"
                + COLUMN_POST_NAMA_BARANG + " TEXT,"
                + COLUMN_POST_LOKASI + " TEXT,"
                + COLUMN_POST_STATUS + " TEXT,"
                + COLUMN_POST_KATEGORI + " TEXT,"
                + COLUMN_POST_TANGGAL + " TEXT,"
                + COLUMN_POST_DESKRIPSI + " TEXT,"
                + COLUMN_POST_KONTAK + " TEXT,"
                + COLUMN_POST_GAMBAR + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_POST_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + "))")
        db.execSQL(createTablePost)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POST)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER)
        onCreate(db)
    }

    fun insertUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_NAME, name)
        values.put(COLUMN_USER_EMAIL, email)
        values.put(COLUMN_USER_PASSWORD, password)

        val success = db.insert(TABLE_USER, null, values)
        db.close()
        return (Integer.parseInt("$success") != -1)
    }

    fun checkUser(email: String, password: String): Boolean {
        val columns = arrayOf(COLUMN_USER_ID)
        val db = this.readableDatabase
        val selection = "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"
        val selectionArgs = arrayOf(email, password)

        val cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null)
        val cursorCount = cursor.count
        cursor.close()
        db.close()

        return cursorCount > 0
    }

    fun checkEmailExists(email: String): Boolean {
        val columns = arrayOf(COLUMN_USER_ID)
        val db = this.readableDatabase
        val selection = "$COLUMN_USER_EMAIL = ?"
        val selectionArgs = arrayOf(email)

        val cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null)
        val cursorCount = cursor.count
        cursor.close()
        db.close()

        return cursorCount > 0
    }

    fun getUserName(email: String): String {
        var name = ""
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_USER_NAME)
        val selection = "$COLUMN_USER_EMAIL = ?"
        val selectionArgs = arrayOf(email)
        val cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null)
        
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(COLUMN_USER_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        cursor.close()
        db.close()
        return name
    }

    fun getUserIdByEmail(email: String): Int {
        var id = -1
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_USER_ID)
        val selection = "$COLUMN_USER_EMAIL = ?"
        val selectionArgs = arrayOf(email)
        val cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null)
        
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_USER_ID)
            if (idIndex != -1) {
                id = cursor.getInt(idIndex)
            }
        }
        cursor.close()
        db.close()
        return id
    }

    fun getUserNameById(userId: Int): String {
        var name = "Unknown"
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_USER_NAME)
        val selection = "$COLUMN_USER_ID = ?"
        val selectionArgs = arrayOf(userId.toString())
        val cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null)
        
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(COLUMN_USER_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        cursor.close()
        db.close()
        return name
    }

    fun insertPost(userId: Int, nama: String, lokasi: String, status: String, kategori: String, tanggal: String, deskripsi: String, kontak: String, gambar: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_POST_USER_ID, userId)
        values.put(COLUMN_POST_NAMA_BARANG, nama)
        values.put(COLUMN_POST_LOKASI, lokasi)
        values.put(COLUMN_POST_STATUS, status)
        values.put(COLUMN_POST_KATEGORI, kategori)
        values.put(COLUMN_POST_TANGGAL, tanggal)
        values.put(COLUMN_POST_DESKRIPSI, deskripsi)
        values.put(COLUMN_POST_KONTAK, kontak)
        values.put(COLUMN_POST_GAMBAR, gambar)

        val success = db.insert(TABLE_POST, null, values)
        db.close()
        return (Integer.parseInt("$success") != -1)
    }

    fun getAllPosts(): List<Barang> {
        val postList = ArrayList<Barang>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_POST ORDER BY $COLUMN_POST_ID DESC"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_POST_ID)
                val userIdIndex = cursor.getColumnIndex(COLUMN_POST_USER_ID)
                val namaIndex = cursor.getColumnIndex(COLUMN_POST_NAMA_BARANG)
                val lokasiIndex = cursor.getColumnIndex(COLUMN_POST_LOKASI)
                val statusIndex = cursor.getColumnIndex(COLUMN_POST_STATUS)
                val kategoriIndex = cursor.getColumnIndex(COLUMN_POST_KATEGORI)
                val tanggalIndex = cursor.getColumnIndex(COLUMN_POST_TANGGAL)
                val deskripsiIndex = cursor.getColumnIndex(COLUMN_POST_DESKRIPSI)
                val kontakIndex = cursor.getColumnIndex(COLUMN_POST_KONTAK)
                val gambarIndex = cursor.getColumnIndex(COLUMN_POST_GAMBAR)

                if (idIndex != -1 && userIdIndex != -1 && namaIndex != -1 && lokasiIndex != -1 && 
                    statusIndex != -1 && kategoriIndex != -1 && tanggalIndex != -1 && 
                    deskripsiIndex != -1 && kontakIndex != -1 && gambarIndex != -1) {
                    
                    val barang = Barang(
                        id = cursor.getInt(idIndex),
                        userId = cursor.getInt(userIdIndex),
                        nama = cursor.getString(namaIndex),
                        lokasi = cursor.getString(lokasiIndex),
                        status = cursor.getString(statusIndex),
                        kategori = cursor.getString(kategoriIndex),
                        tanggal = cursor.getString(tanggalIndex),
                        deskripsi = cursor.getString(deskripsiIndex),
                        kontak = cursor.getString(kontakIndex),
                        gambar = cursor.getString(gambarIndex)
                    )
                    postList.add(barang)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return postList
    }

    fun getPostById(postId: Int): Barang? {
        var barang: Barang? = null
        val db = this.readableDatabase
        val selection = "$COLUMN_POST_ID = ?"
        val selectionArgs = arrayOf(postId.toString())
        val cursor = db.query(TABLE_POST, null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_POST_ID)
            val userIdIndex = cursor.getColumnIndex(COLUMN_POST_USER_ID)
            val namaIndex = cursor.getColumnIndex(COLUMN_POST_NAMA_BARANG)
            val lokasiIndex = cursor.getColumnIndex(COLUMN_POST_LOKASI)
            val statusIndex = cursor.getColumnIndex(COLUMN_POST_STATUS)
            val kategoriIndex = cursor.getColumnIndex(COLUMN_POST_KATEGORI)
            val tanggalIndex = cursor.getColumnIndex(COLUMN_POST_TANGGAL)
            val deskripsiIndex = cursor.getColumnIndex(COLUMN_POST_DESKRIPSI)
            val kontakIndex = cursor.getColumnIndex(COLUMN_POST_KONTAK)
            val gambarIndex = cursor.getColumnIndex(COLUMN_POST_GAMBAR)

            if (idIndex != -1 && userIdIndex != -1 && namaIndex != -1 && lokasiIndex != -1 && 
                statusIndex != -1 && kategoriIndex != -1 && tanggalIndex != -1 && 
                deskripsiIndex != -1 && kontakIndex != -1 && gambarIndex != -1) {
                
                barang = Barang(
                    id = cursor.getInt(idIndex),
                    userId = cursor.getInt(userIdIndex),
                    nama = cursor.getString(namaIndex),
                    lokasi = cursor.getString(lokasiIndex),
                    status = cursor.getString(statusIndex),
                    kategori = cursor.getString(kategoriIndex),
                    tanggal = cursor.getString(tanggalIndex),
                    deskripsi = cursor.getString(deskripsiIndex),
                    kontak = cursor.getString(kontakIndex),
                    gambar = cursor.getString(gambarIndex)
                )
            }
        }
        cursor.close()
        db.close()
        return barang
    }

    fun updatePostStatus(postId: Int, newStatus: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_POST_STATUS, newStatus)

        val success = db.update(TABLE_POST, values, "$COLUMN_POST_ID=?", arrayOf(postId.toString()))
        db.close()
        return success > 0
    }
}

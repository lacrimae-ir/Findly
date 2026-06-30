package com.example.findlynew

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mindrot.jbcrypt.BCrypt

object FirebaseManager {

    private const val DATABASE_URL = "https://database-findly-default-rtdb.asia-southeast1.firebasedatabase.app/"

    private val databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).reference

    // USER OPERATIONS
    fun checkEmailExists(email: String, callback: (Boolean) -> Unit) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun insertUser(user: User, callback: (String?) -> Unit) {
        val uid = databaseReference.child("users").push().key ?: return callback(null)
        
        // Hash password with BCrypt
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val userWithUid = user.copy(uid = uid, password = hashedPassword)
        
        databaseReference.child("users").child(uid).setValue(userWithUid)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(uid)
                } else {
                    callback(null)
                }
            }
    }

    fun checkUser(email: String, passwordEntered: String, callback: (User?) -> Unit) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var foundUser: User? = null
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            if (user != null) {
                                try {
                                    if (BCrypt.checkpw(passwordEntered, user.password)) {
                                        foundUser = user
                                        break
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        callback(foundUser)
                    } else {
                        callback(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun updatePassword(email: String, passwordEntered: String, callback: (String?) -> Unit) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val hashedPassword = BCrypt.hashpw(passwordEntered, BCrypt.gensalt())
                        val childrenCount = snapshot.childrenCount
                        var counter = 0
                        var updated = false
                        for (userSnapshot in snapshot.children) {
                            userSnapshot.ref.child("password").setValue(hashedPassword)
                                .addOnCompleteListener { task ->
                                    counter++
                                    if (task.isSuccessful) {
                                        updated = true
                                    }
                                    if (counter.toLong() == childrenCount) {
                                        callback(if (updated) hashedPassword else null)
                                    }
                                }
                        }
                    } else {
                        callback(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun updateProfilePic(uid: String, profilePicUrl: String, callback: (Boolean) -> Unit) {
        if (uid.isEmpty()) return callback(false)
        databaseReference.child("users").child(uid).child("profilePic").setValue(profilePicUrl)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun updatePhone(uid: String, phone: String, callback: (Boolean) -> Unit) {
        if (uid.isEmpty()) return callback(false)
        databaseReference.child("users").child(uid).child("phone").setValue(phone)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun getUserNameById(userId: String, callback: (String) -> Unit) {
        databaseReference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    callback(user?.username ?: "Unknown")
                }
                override fun onCancelled(error: DatabaseError) {
                    callback("Unknown")
                }
            })
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        databaseReference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    callback(user)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun getUserIdByEmail(email: String, callback: (String) -> Unit) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val firstChild = snapshot.children.firstOrNull()
                        val user = firstChild?.getValue(User::class.java)
                        callback(user?.uid ?: "")
                    } else {
                        callback("")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback("")
                }
            })
    }

    // POST OPERATIONS
    fun insertPost(barang: Barang, callback: (Boolean) -> Unit) {
        val postId = databaseReference.child("posts").push().key ?: return callback(false)
        val barangWithId = barang.copy(id = postId)
        databaseReference.child("posts").child(postId).setValue(barangWithId)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun getAllPosts(callback: (List<Barang>) -> Unit) {
        databaseReference.child("posts")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Barang>()
                    for (postSnapshot in snapshot.children) {
                        val barang = postSnapshot.getValue(Barang::class.java)
                        if (barang != null && !barang.del) {
                            list.add(barang)
                        }
                    }
                    callback(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    fun getPostById(postId: String, callback: (Barang?) -> Unit) {
        databaseReference.child("posts").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val barang = snapshot.getValue(Barang::class.java)
                    if (barang != null && !barang.del) {
                        callback(barang)
                    } else {
                        callback(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun updatePostStatus(postId: String, newStatus: String, callback: (Boolean) -> Unit) {
        val updates = mapOf(
            "status" to newStatus,
            "del" to (newStatus == "HAPUS")
        )
        databaseReference.child("posts").child(postId).updateChildren(updates)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun updatePostSelesai(postId: String, selesai: Int, callback: (Boolean) -> Unit) {
        databaseReference.child("posts").child(postId).child("selesai").setValue(selesai)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }
}

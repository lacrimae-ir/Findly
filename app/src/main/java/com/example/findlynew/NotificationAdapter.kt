package com.example.findlynew

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class NotificationAdapter(private val notifications: List<Barang>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubtitle: TextView = view.findViewById(R.id.tvNotificationSubtitle)
        val ivImage: ImageView = view.findViewById(R.id.ivNotificationImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val barang = notifications[position]
        holder.tvSubtitle.text = "${barang.nama} - ${barang.tanggal}"
        
        // Load image if any
        if (barang.gambar.isNotEmpty()) {
            try {
                val file = File(barang.gambar)
                if (file.exists()) {
                    holder.ivImage.setImageURI(Uri.fromFile(file))
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun getItemCount() = notifications.size
}

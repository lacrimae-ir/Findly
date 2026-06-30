package com.example.findlynew

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

data class NotificationItem(
    val post: Barang,
    val points: Int,
    val isRead: Boolean = false
)

class NotificationAdapter(private val notifications: List<NotificationItem>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvNotificationSubtitle)
        val ivImage: ImageView = view.findViewById(R.id.ivNotificationImage)
        val vNotificationDot: View = view.findViewById(R.id.vNotificationDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        val barang = item.post
        
        // Dynamic title based on similarity points
        if (item.points >= 25) {
            holder.tvTitle.text = "Sangat Cocok (${item.points} Pts)"
            holder.tvTitle.setTextColor(android.graphics.Color.parseColor("#4CD964")) // Green for highly relevant
        } else if (item.points >= 15) {
            holder.tvTitle.text = "Cocok (${item.points} Pts)"
            holder.tvTitle.setTextColor(android.graphics.Color.parseColor("#FF9500")) // Orange for matching
        } else {
            holder.tvTitle.text = "Rekomendasi Kategori"
            holder.tvTitle.setTextColor(android.graphics.Color.parseColor("#2266E2")) // Blue for category match
        }

        holder.vNotificationDot.visibility = if (!item.isRead && item.points >= 15) View.VISIBLE else View.GONE

        holder.tvSubtitle.text = "${barang.nama} - ${barang.tanggal}"
        
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, ItemDetailActivity::class.java).apply {
                putExtra("EXTRA_POST_ID", barang.id)
            }
            context.startActivity(intent)
        }

        // Load image if any
        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(barang.gambar)
            .placeholder(R.drawable.itemplaceholder)
            .error(R.drawable.itemplaceholder)
            .into(holder.ivImage)
    }

    override fun getItemCount() = notifications.size
}

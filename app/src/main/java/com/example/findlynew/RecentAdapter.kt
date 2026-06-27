package com.example.findlynew

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentAdapter(
    private val listBarang: List<Barang>
) : RecyclerView.Adapter<RecentAdapter.RecentViewHolder>() {

    class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTanggal: TextView = itemView.findViewById(R.id.tv_tanggal)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        val ivBarang: ImageView = itemView.findViewById(R.id.iv_barang)
        val tvNamaBarang: TextView = itemView.findViewById(R.id.tv_nama_barang)
        val btnSeeDetails: TextView = itemView.findViewById(R.id.btn_see_details)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent, parent, false)

        return RecentViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {

        val barang = listBarang[position]

        holder.tvTanggal.text = barang.tanggal

        holder.tvStatus.text = barang.status

        when (barang.status.uppercase()) {

            "HILANG" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#E53935"))
            }

            "DITEMUKAN" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            }

            "KEMBALI" -> {
                holder.tvStatus.text = "HILANG"
                holder.tvStatus.setTextColor(Color.parseColor("#E53935"))
            }

            else -> {
                holder.tvStatus.setTextColor(Color.BLACK)
            }
        }

        holder.tvNamaBarang.text = barang.nama

        if (barang.gambar.isNotEmpty()) {
            holder.ivBarang.setImageURI(Uri.parse(barang.gambar))
        }

        holder.btnSeeDetails.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, ItemDetailActivity::class.java)

            intent.putExtra("EXTRA_POST_ID", barang.id)

            context.startActivity(intent)

        }

        // Klik seluruh card juga bisa
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, ItemDetailActivity::class.java)

            intent.putExtra("EXTRA_POST_ID", barang.id)

            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return listBarang.size
    }

}
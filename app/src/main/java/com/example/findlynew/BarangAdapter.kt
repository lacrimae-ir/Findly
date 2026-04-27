package com.example.findlynew

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BarangAdapter(private var listBarang: List<Barang>) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: TextView = itemView.findViewById(R.id.tv_tanggal)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        val ivBarang: ImageView = itemView.findViewById(R.id.iv_barang)
        val tvNamaBarang: TextView = itemView.findViewById(R.id.tv_nama_barang)
        val btnSeeDetails: TextView = itemView.findViewById(R.id.btn_see_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_barang, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = listBarang[position]
        holder.tvTanggal.text = barang.tanggal
        
        holder.tvStatus.text = barang.status
        if (barang.status.equals("Kembali", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")) // Red
        }

        holder.ivBarang.setImageURI(android.net.Uri.parse(barang.gambar))
        holder.tvNamaBarang.text = barang.nama
        
        // Handle click
        holder.btnSeeDetails.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, ItemDetailActivity::class.java)
            intent.putExtra("EXTRA_POST_ID", barang.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listBarang.size
    }

    fun updateData(newList: List<Barang>) {
        listBarang = newList
        notifyDataSetChanged()
    }
}

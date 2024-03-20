package com.example.evoting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.evoting.R
import com.example.evoting.database.PemilihDBHelper
import com.example.evoting.model.Pemilih

class PemilihAdapter(private var pemilih: ArrayList<Pemilih>, context: Context) :
    RecyclerView.Adapter<PemilihAdapter.PemilihViewHolder>() {

    // Mendeklarasikan objek PemilihDBHelper untuk mengakses database
    private val db: PemilihDBHelper = PemilihDBHelper(context)

    // Mendeklarasikan daftar pemilih yang akan ditampilkan di RecyclerView
    private var filteredPemilihList: ArrayList<Pemilih> = ArrayList()

    // Deklarasi listener untuk menangani klik pada item RecyclerView
    private lateinit var listener: OnItemClickListener

    // Interface untuk menangani klik pada item RecyclerView
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Fungsi untuk mengatur listener OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    // ViewHolder untuk menampilkan item pemilih dalam RecyclerView
    class PemilihViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Mendeklarasikan TextView untuk menampilkan NIK dan Nama pemilih
        val nik: TextView = itemView.findViewById(R.id.tvNik)
        val nama: TextView = itemView.findViewById(R.id.tvNama)

        // Fungsi untuk mengikat data pemilih ke ViewHolder dan menangani klik item
        fun bind(pemilih: Pemilih, listener: OnItemClickListener) {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    // Menginisialisasi filteredPemilihList dengan semua pemilih awal saat adapter dibuat
    init {
        filteredPemilihList.addAll(pemilih)
    }

    // Membuat ViewHolder baru saat RecyclerView memerlukannya
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PemilihViewHolder {
        // Membuat tampilan dari layout pemilih_item.xml menggunakan LayoutInflater
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pemilih_item, parent, false)
        // Mengembalikan ViewHolder baru dengan tampilan yang dibuat
        return PemilihViewHolder(view)
    }

    // Mendapatkan jumlah item dalam filteredPemilihList
    override fun getItemCount(): Int = filteredPemilihList.size

    // Mengikat data pemilih ke ViewHolder saat RecyclerView meminta data untuk ditampilkan
    override fun onBindViewHolder(holder: PemilihViewHolder, position: Int) {
        val pemilihItem = filteredPemilihList[position]
        holder.nik.text = pemilihItem.nik
        holder.nama.text = pemilihItem.nama
        holder.bind(pemilihItem, listener)
    }

    // Fungsi untuk memfilter daftar pemilih berdasarkan teks yang diberikan
    fun filter(text: String) {
        filteredPemilihList = if (text.isEmpty()) {
            pemilih
        } else {
            pemilih.filter { pemilih ->
                pemilih.nama.contains(text, ignoreCase = true) ||
                        pemilih.nik.contains(text, ignoreCase = true) ||
                        pemilih.noHp.contains(text, ignoreCase = true) ||
                        pemilih.lokasiRumah.contains(text, ignoreCase = true)
            } as ArrayList<Pemilih>
        }
        notifyDataSetChanged()
    }

    // Fungsi untuk menyegarkan data dalam adapter dengan daftar pemilih baru
    fun refreshData(newPemilih: List<Pemilih>) {
        pemilih.clear()
        pemilih.addAll(newPemilih)
        filteredPemilihList.clear()
        filteredPemilihList.addAll(newPemilih)
        notifyDataSetChanged()
    }
}

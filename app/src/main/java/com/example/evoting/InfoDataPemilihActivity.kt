package com.example.evoting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evoting.adapter.PemilihAdapter
import com.example.evoting.database.PemilihDBHelper
import com.example.evoting.databinding.ActivityInfoDataPemilihBinding
import com.example.evoting.model.Pemilih

class InfoDataPemilihActivity : AppCompatActivity(), PemilihAdapter.OnItemClickListener {

    // Deklarasi variabel
    private lateinit var binding: ActivityInfoDataPemilihBinding
    private lateinit var db: PemilihDBHelper
    private lateinit var pemilihAdapter: PemilihAdapter
    private var filteredPemilihList: ArrayList<Pemilih> = ArrayList() // Daftar pemilih yang telah difilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoDataPemilihBinding.inflate(layoutInflater) // Inisialisasi View Binding
        setContentView(binding.root)

        db = PemilihDBHelper(this) // Inisialisasi DBHelper
        val allPemilihList = db.getAllPemilih() // Mendapatkan semua data pemilih dari database
        filteredPemilihList.addAll(allPemilihList) // Menambahkan semua data pemilih ke dalam daftar pemilih yang telah difilter

        pemilihAdapter = PemilihAdapter(filteredPemilihList, this) // Inisialisasi adapter dengan daftar pemilih yang telah difilter dan listener klik item
        binding.pemilihRecyclerview.layoutManager = LinearLayoutManager(this) // Mengatur layout manager RecyclerView
        binding.pemilihRecyclerview.adapter = pemilihAdapter // Mengatur adapter RecyclerView

        binding.toolbar.setNavigationOnClickListener { onBackPressed() } // Mengatur aksi saat tombol kembali pada toolbar ditekan

        // Mendengarkan perubahan teks pada SearchView
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pemilihAdapter.filter(newText.orEmpty()) // Memfilter daftar pemilih berdasarkan teks yang dimasukkan pada SearchView
                return true
            }
        })

        pemilihAdapter.setOnItemClickListener(this) // Mendengarkan klik item pada RecyclerView
    }

    // Mengatur aksi saat item RecyclerView diklik
    override fun onItemClick(position: Int) {
        val pemilih = filteredPemilihList[position] // Mendapatkan data pemilih dari daftar pemilih yang telah difilter
        val intent = Intent(this, DetailDataPemilihActivity::class.java) // Membuat intent untuk menampilkan detail data pemilih
        intent.putExtra("PEMILIH", pemilih) // Mengirim data pemilih melalui intent
        startActivity(intent) // Memulai aktivitas detail data pemilih
    }

    // Fungsi untuk memfilter daftar pemilih berdasarkan teks yang dimasukkan pada SearchView
    private fun filterPemilih(query: String?) {
        filteredPemilihList.clear() // Menghapus semua data dari daftar pemilih yang telah difilter
        val allPemilihList = db.getAllPemilih() // Mendapatkan semua data pemilih dari database

        // Jika query kosong atau null, tambahkan semua data pemilih ke dalam daftar pemilih yang telah difilter
        if (query.isNullOrEmpty()) {
            filteredPemilihList.addAll(allPemilihList)
        } else {
            val searchQuery = query.toLowerCase().trim() // Mengonversi query menjadi lowercase dan menghapus spasi di awal dan akhir
            // Memfilter data pemilih berdasarkan nama, NIK, nomor HP, atau lokasi rumah
            for (pemilih in allPemilihList) {
                if (pemilih.nama.toLowerCase().contains(searchQuery) ||
                    pemilih.nik.toLowerCase().contains(searchQuery) ||
                    pemilih.noHp.toLowerCase().contains(searchQuery) ||
                    pemilih.lokasiRumah.toLowerCase().contains(searchQuery)
                ) {
                    filteredPemilihList.add(pemilih) // Menambahkan pemilih yang sesuai ke dalam daftar pemilih yang telah difilter
                }
            }
        }

        pemilihAdapter.refreshData(filteredPemilihList) // Memperbarui data pada adapter dengan daftar pemilih yang telah difilter
    }
}

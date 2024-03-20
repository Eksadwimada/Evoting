package com.example.evoting

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.evoting.databinding.ActivityDetailDataPemilihBinding
import com.example.evoting.model.Pemilih

class DetailDataPemilihActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailDataPemilihBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDataPemilihBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Mendapatkan data pemilih dari intent
        val pemilih: Pemilih? = intent.getParcelableExtra("PEMILIH")

        // Menampilkan detail pemilih jika tidak null
        pemilih?.let {
            binding.tvNik.text = it.nik
            binding.tvNama.text = it.nama
            binding.tvNohp.text = it.noHp
            binding.tvJk.text = it.jenisKelamin
            binding.tvTanggal.text = it.tanggalPendataan
            binding.tvAlamat.text = it.lokasiRumah

            // Menampilkan gambar
            it.gambarProses?.let { byteArray ->
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imgView.setImageBitmap(bitmap)
            }
        }
    }
}
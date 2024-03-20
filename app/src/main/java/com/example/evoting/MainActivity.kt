package com.example.evoting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.evoting.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Handle button clicks
        binding.btnInfo.setOnClickListener {
            // Start InfoPemilihActivity
            startActivity(Intent(this, InfoPemilihActivity::class.java))
        }

        binding.btnForm.setOnClickListener {
            // Start EntriDataPemilihActivity
            startActivity(Intent(this, EntryDataPemilihActivity::class.java))
        }

        binding.btnData.setOnClickListener {
            // Start InfoPemilihActivity
            startActivity(Intent(this, InfoDataPemilihActivity::class.java))
        }

        binding.btnOut.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Konfirmasi")
        alertDialogBuilder.setMessage("Anda yakin ingin keluar ?")
        alertDialogBuilder.setPositiveButton("Ya") { dialog, which ->
            // Keluar dari aplikasi
            finish()
        }
        alertDialogBuilder.setNegativeButton("Tidak") { dialog, which ->
            // Tidak melakukan apa-apa
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
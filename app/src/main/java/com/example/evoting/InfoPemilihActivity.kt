package com.example.evoting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.evoting.databinding.ActivityInfoPemilihBinding

class InfoPemilihActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoPemilihBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoPemilihBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}
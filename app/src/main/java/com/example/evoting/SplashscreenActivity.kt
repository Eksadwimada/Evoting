
package com.example.evoting

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.example.evoting.databinding.ActivitySplashscreenBinding

class SplashscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashscreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghilangkan batas atas layar
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivitySplashscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        checkInternetConnectionAndDelay()
    }

    private fun checkInternetConnectionAndDelay() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            // Ada koneksi internet, lanjutkan dengan menampilkan splash screen
            Handler().postDelayed({
                navigateToNextScreen()
            }, SPLASH_DELAY)
        } else {
            // Jika tidak ada internet
            showNoInternetMessage()
        }
    }

    private fun showNoInternetMessage() {
        // Tampilkan pesan tidak ada koneksi internet menggunakan Toast
        Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun navigateToNextScreen() {
        // Membuat intent untuk beralih ke aktivitas berikutnya
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        // data long
        private const val SPLASH_DELAY = 3000L
    }
}
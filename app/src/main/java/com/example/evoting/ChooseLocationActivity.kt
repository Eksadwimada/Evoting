package com.example.evoting

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.Manifest
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.evoting.databinding.ActivityChooseLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class ChooseLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityChooseLocationBinding
    private lateinit var mMap: GoogleMap
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val TAG = "ChooseLocationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Mengatur onClickListener untuk tombol add location
        binding.btnAddLocation.setOnClickListener {
            if (::mMap.isInitialized) { // Memastikan mMap sudah diinisialisasi
                val selectedLocation = mMap.cameraPosition.target // Mendapatkan lokasi yang dipilih dari posisi kamera di peta
                val intent = Intent()
                intent.putExtra("selected_location", selectedLocation) // Mengirim lokasi yang dipilih ke activity sebelumnya
                setResult(RESULT_OK, intent) // Set result OK untuk mengindikasikan pemilihan lokasi berhasil
                finish() // Menutup activity saat pemilihan lokasi selesai
            } else {
                Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show() // Tampilkan pesan jika peta belum siap
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap // Inisialisasi mMap saat peta siap

        getLastKnownLocation() // Mendapatkan lokasi terakhir pengguna

        // Mengatur beberapa pengaturan peta
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true
        mMap.uiSettings.isScrollGesturesEnabled = true

        // Mendengarkan klik pada peta untuk menambahkan marker
        mMap.setOnMapClickListener { latLng ->
            mMap.clear() // Hapus semua marker yang ada
            val address = getAddressFromLatLng(latLng) // Dapatkan alamat dari koordinat yang dipilih
            mMap.addMarker(MarkerOptions().position(latLng).title(address)) // Tambahkan marker pada lokasi yang dipilih dengan judul berupa alamat
        }
    }

    private fun getLastKnownLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) // Inisialisasi FusedLocationProviderClient

        // Memeriksa izin lokasi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return // Jika izin tidak diberikan, keluar dari fungsi
        }
        fusedLocationClient.lastLocation // Mendapatkan lokasi terakhir pengguna
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    // Gunakan lokasi terakhir sebagai lokasi awal
                    val initialLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f)) // Perbarui kamera untuk menunjukkan lokasi terakhir dengan level zoom 15
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Gagal mendapatkan lokasi: ${e.message}")
            }
    }

    private fun getAddressFromLatLng(latLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault()) // Inisialisasi Geocoder dengan locale default
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) // Dapatkan alamat dari koordinat
        return if (!addresses.isNullOrEmpty()) {
            addresses[0].getAddressLine(0) // Ambil alamat baris pertama sebagai judul
        } else {
            "Alamat kosong"
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

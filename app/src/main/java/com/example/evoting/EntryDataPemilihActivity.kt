package com.example.evoting

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.evoting.database.PemilihDBHelper
import com.example.evoting.databinding.ActivityEntryDataPemilihBinding
import com.example.evoting.model.Pemilih
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EntryDataPemilihActivity : AppCompatActivity() {

    // Deklarasi variabel
    private lateinit var binding: ActivityEntryDataPemilihBinding // View Binding
    private lateinit var db: PemilihDBHelper // DBHelper untuk mengakses database
    private val calendar = Calendar.getInstance() // Objek Calendar untuk tanggal

    private var currentPhotoPath: String? = null // Path foto yang dipilih

    // Kode permintaan untuk izin, memilih gambar dari galeri, dan memilih lokasi
    private val REQUEST_PERMISSION_CODE = 123
    private val REQUEST_IMAGE_FROM_GALLERY = 1001
    private val REQUEST_LOCATION = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryDataPemilihBinding.inflate(layoutInflater) // Inisialisasi View Binding
        setContentView(binding.root)

        db = PemilihDBHelper(this) // Inisialisasi DBHelper

        // Mengatur tombol submit nonaktif
        setButtonSubmitEnabled(false)

        // Menampilkan tombol kembali pada action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Mengatur aksi saat tombol kembali ditekan
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // Mendengarkan perubahan teks di EditText dan memvalidasi input
        binding.nikEt.addTextChangedListener(textWatcher)
        binding.nameEt.addTextChangedListener(textWatcher)
        binding.noEt.addTextChangedListener(textWatcher)
        binding.locationiEt.addTextChangedListener(textWatcher)

        // Menampilkan date picker saat tombol "Pick Date" ditekan
        binding.btnDatePicker.setOnClickListener {
            showDatePicker()
        }

        // Menampilkan aktivitas memilih lokasi saat tombol "Pick Location" ditekan
        binding.btnPickLocation.setOnClickListener {
            val intent = Intent(this, ChooseLocationActivity::class.java)
            startActivityForResult(intent, REQUEST_LOCATION)
        }

        // Meminta izin akses untuk memilih gambar saat tombol "Upload Photo" ditekan
        binding.btnUploadPhoto.setOnClickListener {
            checkAndRequestPermissions()
        }

        // Menyimpan data ke database saat tombol "Submit" ditekan
        binding.btnSubmit.setOnClickListener {
            saveDataToDatabase()
        }
    }

    // Mendengarkan perubahan teks pada EditText
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validateInputs()
        }
    }

    // Memvalidasi input pengguna
    private fun validateInputs() {
        val isNIKValid = binding.nikEt.text.toString().isNotEmpty()
        val isNameValid = binding.nameEt.text.toString().isNotEmpty()
        val isNoHpValid = binding.noEt.text.toString().isNotEmpty()
        val isTanggalValid = binding.tvSelectedDate.text.toString().isNotEmpty()

        // Mengaktifkan atau menonaktifkan tombol "Submit" berdasarkan validitas input
        setButtonSubmitEnabled(isNIKValid && isNameValid && isNoHpValid && isTanggalValid)
    }

    // Mengatur status aktif/nonaktif tombol "Submit"
    private fun setButtonSubmitEnabled(isEnabled: Boolean) {
        binding.btnSubmit.isEnabled = isEnabled
        if (isEnabled) {
            binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(this,R.color.red)
        } else {
            binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grey)
        }
    }

    // Menampilkan date picker
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this, { datePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.tvSelectedDate.text = "$formattedDate"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Memeriksa dan meminta izin untuk mengakses penyimpanan
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        } else {
            pickImageFromGallery()
        }
    }

    // Menangani hasil permintaan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Memilih gambar dari galeri
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_FROM_GALLERY)
    }

    // Menampilkan gambar yang dipilih
    private fun displayImage(bitmap: Bitmap?) {
        bitmap?.let {
            binding.imgView.setImageBitmap(it)
        }
    }

    // Menangani hasil dari aktivitas yang dimulai untuk hasil tertentu
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_FROM_GALLERY -> {
                    data?.data?.let { uri ->
                        val filePath = getRealPathFromURI(uri)
                        val bitmap = BitmapFactory.decodeFile(filePath)
                        displayImage(bitmap)
                        currentPhotoPath = filePath
                    }
                }

                REQUEST_LOCATION -> {
                    if (data != null && data.hasExtra("selected_location")) {
                        val selectedLocation = data.getParcelableExtra<LatLng>("selected_location")
                        fetchAddress(selectedLocation)
                    }
                }
            }
        }
    }

    // Mengambil alamat dari lokasi yang dipilih
    private fun fetchAddress(location: LatLng?) {
        val geocoder = Geocoder(this, Locale.getDefault())
        location?.let {
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    binding.locationiEt.setText(address)
                } else {
                    Toast.makeText(this, "Alamat tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "Terjadi kesalahan saat mengambil alamat", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Mendapatkan path file gambar dari URI
    private fun getRealPathFromURI(uri: Uri): String {
        var result: String
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor == null) {
            result = uri.path ?: ""
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    // Menyimpan data ke database
    private fun saveDataToDatabase() {
        val nik = binding.nikEt.text.toString()
        val name = binding.nameEt.text.toString()
        val noHp = binding.noEt.text.toString()
        val jenisKelamin = if (binding.rbMale.isChecked) "Laki-laki" else "Perempuan"
        val tanggal = binding.tvSelectedDate.text.toString()
        val lokasiRumah = binding.locationiEt.text.toString()
        val gambarProses = currentPhotoPath ?: ""

        val gambarByteArray = readImageFileToByteArray(gambarProses)

        if (gambarByteArray != null) {
            // Cek data yang sudah ada
            if (isDataExist(nik, name, noHp)) {
                // Data sudah ada
                showDataExistConfirmation()
            } else {
                // Data belum ada
                val pemilih = Pemilih(
                    id = null,
                    nik = nik,
                    nama = name,
                    noHp = noHp,
                    jenisKelamin = jenisKelamin,
                    tanggalPendataan = tanggal,
                    lokasiRumah = lokasiRumah,
                    gambarProses = gambarByteArray
                )

                val newRowId = db.insertPemilih(pemilih)

                if (newRowId != -1L) {
                    Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    resetForm()
                } else {
                    Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Gagal membaca file gambar", Toast.LENGTH_SHORT).show()
        }
    }

    // Memeriksa apakah data sudah ada di database
    private fun isDataExist(nik: String, name: String, noHp: String): Boolean {
        val db = PemilihDBHelper(this).readableDatabase
        val selection = "${PemilihDBHelper.COLUMN_NIK} = ? AND ${PemilihDBHelper.COLUMN_NAME} = ? AND ${PemilihDBHelper.COLUMN_PHONE} = ?"
        val selectionArgs = arrayOf(nik, name, noHp)
        val cursor = db.query(
            PemilihDBHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val dataExists = cursor.count > 0
        cursor.close()
        return dataExists
    }

    // Menampilkan konfirmasi jika data sudah ada di database
    private fun showDataExistConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Data Sudah Ada")
            setMessage("Data yang Anda masukkan sudah ada. Apakah Anda ingin tetap menyimpannya?")
            setPositiveButton("Ya") { dialog, _ ->
                dialog.dismiss()
                resetForm()
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
                resetForm()
            }
            show()
        }
    }

    // Membaca file gambar menjadi array byte
    private fun readImageFileToByteArray(filePath: String): ByteArray? {
        return try {
            val file = File(filePath)
            val inputStream = FileInputStream(file)
            val buffer = ByteArrayOutputStream()
            val bufferSize = 1024
            val byteArray = ByteArray(bufferSize)
            var bytes: Int
            while (inputStream.read(byteArray).also { bytes = it } != -1) {
                buffer.write(byteArray, 0, bytes)
            }
            inputStream.close()
            buffer.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Mereset form setelah data disimpan
    private fun resetForm() {
        binding.nikEt.text?.clear()
        binding.nameEt.text?.clear()
        binding.noEt.text?.clear()
        binding.rbMale.isChecked = false
        binding.rbFemale.isChecked = false
        binding.tvSelectedDate.text = ""
        binding.locationiEt.text?.clear()
        binding.imgView.setImageDrawable(null)
        currentPhotoPath = null
        setButtonSubmitEnabled(false)
    }
}

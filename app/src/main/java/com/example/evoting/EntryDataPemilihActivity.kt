package com.example.evoting

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
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
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
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
    private val REQUEST_IMAGE_CAPTURE = 1003

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
        binding.tvSelectedDate.setOnClickListener {
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

        // Menangani klik pada tombol untuk mengambil gambar dari kamera
        binding.btnGetPhoto.setOnClickListener {
            takePictureFromCamera()
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
            val nik = binding.nikEt.text.toString()
            val name = binding.nameEt.text.toString()
            val noHp = binding.noEt.text.toString()
            val tanggal = binding.tvSelectedDate.text.toString()

            // Memvalidasi input pengguna
            validateInputs(nik, name, noHp, tanggal)
        }
    }

    // Memvalidasi input pengguna
    private fun validateInputs(nik: String, name: String, noHp: String, tanggal: String) {
        val isNIKValid = nik.isNotEmpty()
        val isNameValid = name.isNotEmpty()
        val isNoHpValid = noHp.isNotEmpty()
        val isTanggalValid = tanggal.isNotEmpty()

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
            this,
            { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.tvSelectedDate.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Memeriksa dan meminta izin untuk mengakses penyimpanan
    private fun checkAndRequestPermissions() {
        val permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded = ArrayList<String>()

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_PERMISSION_CODE)
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

    // Memilih gambar dari kamera
    private fun takePictureFromCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    // Menangani hasil dari aktivitas yang dimulai untuk hasil tertentu
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // Menangani hasil pemilihan lokasi
                REQUEST_LOCATION -> {
                    if (data != null && data.hasExtra("selected_location")) {
                        val selectedLocation = data.getParcelableExtra<LatLng>("selected_location")
                        fetchAddress(selectedLocation)
                    }
                }
                // Memilih gambar dari galeri
                REQUEST_IMAGE_FROM_GALLERY -> {
                    data?.data?.let { uri ->
                        val filePath = getRealPathFromURI(uri)
                        val bitmap = BitmapFactory.decodeFile(filePath)
                        displayImage(bitmap)
                        currentPhotoPath = filePath
                    }
                }
                // Menangani hasil pengambilan gambar dari kamera
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap?
                    displayImage(imageBitmap)
                    currentPhotoPath = saveImageToInternalStorage(imageBitmap)
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

    // Menampilkan gambar yang dipilih
    private fun displayImage(bitmap: Bitmap?) {
        bitmap?.let {
            binding.imgView.setImageBitmap(it)
        }
    }

    // Menyimpan gambar ke penyimpanan internal
    private fun saveImageToInternalStorage(bitmap: Bitmap?): String {
        bitmap?.let {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timeStamp.jpg"
            val file = File(directory, fileName)
            try {
                val stream: OutputStream = FileOutputStream(file)
                it.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return file.absolutePath
        }
        return ""
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
        binding.tvSelectedDate.text = Editable.Factory.getInstance().newEditable("")
        binding.locationiEt.text?.clear()
        binding.imgView.setImageDrawable(null)
        currentPhotoPath = null
        setButtonSubmitEnabled(false)
    }
}

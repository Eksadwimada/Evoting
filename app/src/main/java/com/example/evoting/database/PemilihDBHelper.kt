package com.example.evoting.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.evoting.model.Pemilih

// Kelas yang bertanggung jawab untuk mengelola database dan tabel Pemilih
class PemilihDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "DataPemilih.db"
        const val TABLE_NAME = "Pemilih"

        // Nama kolom dalam tabel
        const val COLUMN_ID = "id"
        const val COLUMN_NIK = "nik"
        const val COLUMN_NAME = "nama"
        const val COLUMN_PHONE = "noHp"
        const val COLUMN_GENDER = "jenisKelamin"
        const val COLUMN_DATE = "tanggalPendataan"
        const val COLUMN_LOCATION = "lokasiRumah"
        const val COLUMN_PHOTO = "gambarProses"  // Hanya nama kolom tanpa tipe data
    }

    // Membuat tabel Pemilih ketika database dibuat
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NIK TEXT, $COLUMN_NAME TEXT, $COLUMN_PHONE TEXT, $COLUMN_GENDER TEXT, $COLUMN_DATE TEXT, $COLUMN_LOCATION TEXT, $COLUMN_PHOTO BLOB)"
        db?.execSQL(createTableQuery)
    }

    // Memperbarui skema database jika versi berubah
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Memasukkan data pemilih baru ke dalam tabel Pemilih
    fun insertPemilih(pemilih: Pemilih): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NIK, pemilih.nik)
            put(COLUMN_NAME, pemilih.nama)
            put(COLUMN_PHONE, pemilih.noHp)
            put(COLUMN_GENDER, pemilih.jenisKelamin)
            put(COLUMN_DATE, pemilih.tanggalPendataan)
            put(COLUMN_LOCATION, pemilih.lokasiRumah)
            put(COLUMN_PHOTO, pemilih.gambarProses)
        }
        val newRowId = db.insert(TABLE_NAME, null, values)
        db.close()
        return newRowId
    }

    // Mengambil semua data pemilih dari tabel Pemilih dan mengembalikannya dalam bentuk ArrayList<Pemilih>
    fun getAllPemilih(): ArrayList<Pemilih> {
        val pemilihList = ArrayList<Pemilih>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val nik = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIK))
                val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val noHp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
                val jenisKelamin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER))
                val tanggalPendataan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val lokasiRumah = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                val gambarProses = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))

                val pemilih = Pemilih(
                    id = id,
                    nik = nik,
                    nama = nama,
                    noHp = noHp,
                    jenisKelamin = jenisKelamin,
                    tanggalPendataan = tanggalPendataan,
                    lokasiRumah = lokasiRumah,
                    gambarProses = gambarProses
                )
                pemilihList.add(pemilih)
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return pemilihList
    }
}

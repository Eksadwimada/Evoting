package com.example.evoting.model

import android.os.Parcel
import android.os.Parcelable

data class Pemilih(

    var id: Int? = null, // Properti ID pemilih, defaultnya null
    var nik: String, // Properti Nomor Induk Kependudukan (NIK) pemilih
    var nama: String, // Properti nama lengkap pemilih
    var noHp: String, // Properti nomor telepon pemilih
    var jenisKelamin: String, // Properti jenis kelamin pemilih
    var tanggalPendataan: String, // Properti tanggal pendataan pemilih
    var lokasiRumah: String, // Properti alamat rumah pemilih
    var gambarProses: ByteArray? = null // Properti data gambar pemilih dalam bentuk byte array, defaultnya null
) : Parcelable {

    // Implementasi Parcelable untuk mengirim objek melalui Intent
    // Konstruktor sekunder yang menerima objek Parcel
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createByteArray()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id ?: -1)
        parcel.writeString(nik)
        parcel.writeString(nama)
        parcel.writeString(noHp)
        parcel.writeString(jenisKelamin)
        parcel.writeString(tanggalPendataan)
        parcel.writeString(lokasiRumah)
        parcel.writeByteArray(gambarProses)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pemilih> {
        override fun createFromParcel(parcel: Parcel): Pemilih {
            return Pemilih(parcel)
        }

        override fun newArray(size: Int): Array<Pemilih?> {
            return arrayOfNulls(size)
        }
    }
}
package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifikasi")
data class NotifikasiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val jenisNotifikasi: String, 
    val pesan: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val referenceId: Int? = null, 
    val referenceType: String? = null, 
    val additionalData: String? = null
)

fun NotifikasiEntity.toNotifikasi(): Notifikasi {
    return Notifikasi(
        id = this.id,
        jenisNotifikasi = this.jenisNotifikasi,
        pesan = this.pesan,
        timestamp = this.timestamp,
        isRead = this.isRead,
        referenceId = this.referenceId,
        additionalData = this.additionalData
    )
}

fun Notifikasi.toEntity(): NotifikasiEntity {
    return NotifikasiEntity(
        id = this.id,
        jenisNotifikasi = this.jenisNotifikasi,
        pesan = this.pesan,
        timestamp = this.timestamp,
        isRead = this.isRead,
        referenceId = this.referenceId,
        referenceType = when (this.jenisNotifikasi) {
            "pengingat_tagihan" -> "tagihan"
            "pengingat_agenda" -> "agenda"
            else -> null
        },
        additionalData = this.additionalData
    )
}
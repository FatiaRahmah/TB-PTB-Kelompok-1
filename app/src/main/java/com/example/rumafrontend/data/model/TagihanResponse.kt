package com.example.rumafrontend.data.model

import com.example.rumafrontend.data.entity.TagihanEntity
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class TagihanResponse(
    @SerializedName("tagihan_id")
    val tagihanId: Int,
    
    @SerializedName("judul")
    val judul: String,
    
    @SerializedName("deskripsi")
    val deskripsi: String? = null,
    
    @SerializedName("jatuh_tempo")
    val jatuhTempo: String, 
    
    @SerializedName("reminder_days")
    val reminderDays: Int,
    
    @SerializedName("repeat_type")
    val repeatType: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("bukti_foto")
    val buktiFoto: String? = null,
    
    @SerializedName("tanggal_selesai")
    val tanggalSelesai: String? = null,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

fun TagihanResponse.toEntity(): TagihanEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    
    val dueDateMillis = try {
        dateFormat.parse(jatuhTempo)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    val tanggalSelesaiMillis = try {
        tanggalSelesai?.let { dateFormat.parse(it)?.time }
    } catch (e: Exception) {
        null
    }
    
    val createdAtMillis = try {
        createdAt?.let { dateFormat.parse(it)?.time } ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    
    return TagihanEntity(
        id = 0, 
        title = judul,
        description = deskripsi,
        dueDateMillis = dueDateMillis,
        status = status,
        buktiFotoPath = buktiFoto,
        tanggalSelesaiMillis = tanggalSelesaiMillis,
        reminderDays = reminderDays,
        repeatType = repeatType,
        createdAt = createdAtMillis,
        lastModified = System.currentTimeMillis(),
        serverId = tagihanId.toString(),
        isSynced = true
    )
}

fun TagihanEntity.toRequest(): TagihanRequest {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    
    return TagihanRequest(
        judul = title,
        deskripsi = description,
        jatuhTempo = dateFormat.format(Date(dueDateMillis)),
        reminderDays = reminderDays,
        repeatType = repeatType,
        status = status,
        buktiFoto = buktiFotoPath
    )
}

package com.example.rumafrontend.data.model

import com.google.gson.annotations.SerializedName

data class TagihanRequest(
    @SerializedName("judul")
    val judul: String,
    
    @SerializedName("deskripsi")
    val deskripsi: String? = null,
    
    @SerializedName("jatuh_tempo")
    val jatuhTempo: String, 
    
    @SerializedName("reminder_days")
    val reminderDays: Int = 1,
    
    @SerializedName("repeat_type")
    val repeatType: String = "Sekali saja",
    
    @SerializedName("status")
    val status: String = "belum",
    
    @SerializedName("bukti_foto")
    val buktiFoto: String? = null
)

data class TandaiLunasRequest(
    @SerializedName("bukti_foto")
    val buktiFoto: String? = null
)

data class UpdateStatusRequest(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("tanggal_selesai")
    val tanggalSelesai: String? = null,
    
    @SerializedName("clear_foto")
    val clearFoto: Boolean = false
)

package com.example.rumafrontend.data.model

import com.google.gson.annotations.SerializedName

data class NotifikasiRequest(
    @SerializedName("jenis_notifikasi") val jenisNotifikasi: String,
    @SerializedName("pesan") val pesan: String,
    @SerializedName("reference_id") val referenceId: Int? = null,
    @SerializedName("reference_type") val referenceType: String? = null,
    @SerializedName("additional_data") val additionalData: String? = null,
    @SerializedName("is_read") val isRead: Boolean = false
)

data class NotifikasiResponse(
    @SerializedName("notifikasi_id") val id: Int,
    @SerializedName("jenis_notifikasi") val jenisNotifikasi: String,
    @SerializedName("pesan") val pesan: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("reference_id") val referenceId: Int?,
    @SerializedName("reference_type") val referenceType: String?,
    @SerializedName("createdAt") val createdAt: String
)

package com.example.rumafrontend.data.entity

data class Notifikasi(
    val id: Int = 0,
    val jenisNotifikasi: String, 
    val pesan: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val referenceId: Int? = null, 
    val additionalData: String? = null
)
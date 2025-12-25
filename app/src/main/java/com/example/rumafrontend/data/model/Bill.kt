package com.example.rumafrontend.data.model

data class Bill(
    val id: Int,
    val title: String,
    val dueDateMillis: Long,
    val status: String = "belum",
    val buktiFotoPath: String? = null,
    val tanggalSelesaiMillis: Long? = null,
    val description: String? = null,
    val reminderDays: Int = 1,
    val repeatType: String = "Sekali saja"
)

enum class SortType {
    TERDEKAT, TERLAMA
}

enum class BillStatus {
    AKTIF,
    LUNAS
}


package com.example.rumafrontend.data.remote

data class BahanResponse(
    val id: Int,
    val resep_id: Int,
    val nama_bahan: String,
    val jumlah: Int,
    val satuan: String
)

data class LangkahResponse(
    val id: Int,
    val resep_id: Int,
    val urutan: Int,
    val deskripsi: String,
    val foto: String?
)

data class ResepResponse(
    val id: Int,
    val judul: String,
    val foto: String?,
    val waktu_masak: Int?,
    val porsi: Int?,
    val is_favorit: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val Bahans: List<BahanResponse> = emptyList(),
    val Langkahs: List<LangkahResponse> = emptyList()
)

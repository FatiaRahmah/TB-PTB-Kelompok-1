package com.example.rumafrontend.data.remote

data class BahanRequest(
    val nama_bahan: String,
    val jumlah: Int,
    val satuan: String
)

data class LangkahRequest(
    val deskripsi: String,
    val foto: String? = null
)

data class CreateResepRequest(
    val judul: String,
    val waktu_masak: Int,
    val porsi: Int,
    val foto: String?,
    val is_favorit: Boolean = true,
    val bahan: List<BahanRequest>,
    val langkah: List<LangkahRequest>
)

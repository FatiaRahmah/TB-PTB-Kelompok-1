package com.example.rumafrontend.data.model

import com.example.rumafrontend.data.entity.Resep.ResepEntity

data class Resep(
    val id : Int,
    val judul: String,
    val foto: String,
    val waktu_masak: Int,
    val porsi: Int,
    val is_favorit: Boolean = false,
    val bahan: List<Bahan> = emptyList(),
    val langkah: List<Langkah> = emptyList()
    )
{
 fun toResepEntity() = ResepEntity(
     id = id,
    judul = judul,
    foto = foto,
    waktu_masak = waktu_masak,
    porsi = porsi,
    is_favorit = is_favorit
 )
}

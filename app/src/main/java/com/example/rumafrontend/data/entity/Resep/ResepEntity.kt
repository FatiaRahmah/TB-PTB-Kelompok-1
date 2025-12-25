package com.example.rumafrontend.data.entity.Resep

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rumafrontend.data.model.Resep

@Entity(tableName = "resep")
data class ResepEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val judul: String,
    val foto: String,
    val waktu_masak: Int,
    val porsi:Int,
    val is_favorit: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
{
    fun toResep() = Resep(
        id = id,
        judul = judul,
        foto = foto,
        waktu_masak = waktu_masak,
        porsi = porsi,
        is_favorit = is_favorit
    )
}

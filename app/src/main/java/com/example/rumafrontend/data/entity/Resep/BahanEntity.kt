package com.example.rumafrontend.data.entity.Resep

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rumafrontend.data.model.Bahan

@Entity(tableName = "bahan",
    foreignKeys =[
        ForeignKey(
            entity = ResepEntity::class,
            parentColumns = ["id"],
            childColumns = ["resep_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("resep_id")]
)
data class BahanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val resep_id: Int,
    val bahan: String,
    val jumlah: String,
    val unit: String
){
    fun toBahan ( ) = Bahan(
        id = id,
        resep_id = resep_id,
        bahan = bahan,
        jumlah = jumlah,
        unit = unit
        )
}

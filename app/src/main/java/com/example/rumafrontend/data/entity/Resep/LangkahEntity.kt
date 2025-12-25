package com.example.rumafrontend.data.entity.Resep

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rumafrontend.data.model.Langkah

@Entity(tableName = "langkah",
    foreignKeys = [
        ForeignKey(
            entity = ResepEntity::class,
            parentColumns = ["id"],
            childColumns = ["resep_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("resep_id")]
)
data class LangkahEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val resep_id: Int,
    val urutan: Int,
    val langkah: String,
    val foto: String
){
    fun toLangkah( ) = Langkah(
        id = id,
        resep_id = resep_id,
        urutan = urutan,
        langkah = langkah,
        foto = foto
    )
}

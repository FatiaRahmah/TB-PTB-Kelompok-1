package com.example.rumafrontend.data.entity.Resep

import androidx.room.Embedded
import androidx.room.Relation

data class ResepWithDetail(
    @Embedded val resep: ResepEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "resep_id"
    ) val bahan: List<BahanEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "resep_id"
    ) val langkah: List<LangkahEntity>
)

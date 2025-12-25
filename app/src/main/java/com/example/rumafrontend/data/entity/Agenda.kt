package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agendas")
data class Agenda(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val judul: String,
    val kategori: String,
    val deskripsi: String,
    val date: String,
    val time: String,
    val location: String,
    val reminder: String,
    val isCompleted: Boolean = false
)

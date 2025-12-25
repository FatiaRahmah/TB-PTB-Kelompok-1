package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rumafrontend.data.model.Bill

@Entity(tableName = "tagihan")
data class TagihanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String? = null,
    val dueDateMillis: Long,
    val status: String = "belum", 
    val buktiFotoPath: String? = null,
    val tanggalSelesaiMillis: Long? = null,
    val reminderDays: Int = 1,
    val repeatType: String = "Sekali saja",

    
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),

    
    val serverId: String? = null,
    val isSynced: Boolean = false
)

fun TagihanEntity.toBill() = Bill(
    id = id,
    title = title,
    dueDateMillis = dueDateMillis,
    status = status,
    buktiFotoPath = buktiFotoPath,
    tanggalSelesaiMillis = tanggalSelesaiMillis,
    description = description,
    reminderDays = reminderDays,
    repeatType = repeatType
)

fun Bill.toEntity() = TagihanEntity(
    id = id,
    title = title,
    dueDateMillis = dueDateMillis,
    status = status,
    buktiFotoPath = buktiFotoPath,
    tanggalSelesaiMillis = tanggalSelesaiMillis,
    description = description,
    reminderDays = reminderDays,
    repeatType = repeatType
)


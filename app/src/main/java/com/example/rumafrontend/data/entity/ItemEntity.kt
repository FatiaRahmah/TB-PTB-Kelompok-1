package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_item")
data class ItemEntity(
    @PrimaryKey
    val id: Int,

    val categoryId: Int,

    val text: String,

    val checked: Boolean = false
)
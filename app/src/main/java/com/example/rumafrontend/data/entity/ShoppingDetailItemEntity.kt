package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_detail_items")
data class ShoppingDetailItemEntity(
    @PrimaryKey val id: Int,
    val categoryId: Int,
    val name: String,
    val checked: Boolean
)
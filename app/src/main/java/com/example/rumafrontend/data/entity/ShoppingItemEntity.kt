package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_item")
data class ShoppingItemEntity(
    @PrimaryKey
    val id: Int,          
    val title: String,
    val date: String,
    val done: Boolean
)
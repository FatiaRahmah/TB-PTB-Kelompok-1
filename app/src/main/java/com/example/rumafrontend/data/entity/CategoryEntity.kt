package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                 
    val shoppingItemId: Int,         
    val name: String,
    val iconName: String,
    val expanded: Boolean
)
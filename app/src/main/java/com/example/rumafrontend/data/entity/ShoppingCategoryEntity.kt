package com.example.rumafrontend.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_categories")
data class ShoppingCategoryEntity(
    @PrimaryKey val id: Int,
    val shoppingListId: Int,
    val name: String
)
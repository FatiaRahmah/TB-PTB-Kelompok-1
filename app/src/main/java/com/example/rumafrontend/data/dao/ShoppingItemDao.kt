package com.example.rumafrontend.data.dao

import androidx.room.*
import com.example.rumafrontend.data.entity.ShoppingItemEntity

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_item")
    suspend fun getAll(): List<ShoppingItemEntity>

    @Insert
    suspend fun insert(item: ShoppingItemEntity)

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)
}
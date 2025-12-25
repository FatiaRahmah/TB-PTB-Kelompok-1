package com.example.rumafrontend.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.rumafrontend.data.entity.ItemEntity

@Dao
interface ItemDao {

    @Query("SELECT * FROM category_item WHERE categoryId = :categoryId")
    suspend fun getByCategoryId(categoryId: Int): List<ItemEntity>
    

    @Insert
    suspend fun insert(item: ItemEntity)

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query("DELETE FROM category_item WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: Int)
}
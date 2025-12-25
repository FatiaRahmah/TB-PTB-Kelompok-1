package com.example.rumafrontend.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.rumafrontend.data.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category WHERE shoppingItemId = :shoppingItemId")
    suspend fun getByShoppingItemId(shoppingItemId: Int): List<CategoryEntity>
    

    @Insert
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM category WHERE shoppingItemId = :listId")
    suspend fun deleteByShoppingItemId(listId: Int)
}
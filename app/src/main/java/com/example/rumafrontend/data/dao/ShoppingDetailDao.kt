package com.example.rumafrontend.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rumafrontend.data.entity.ShoppingCategoryEntity
import com.example.rumafrontend.data.entity.ShoppingDetailItemEntity

@Dao
interface ShoppingDetailDao {

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ShoppingCategoryEntity>)

    @Query("SELECT * FROM shopping_categories WHERE shoppingListId = :listId")
    suspend fun getCategoriesByListId(listId: Int): List<ShoppingCategoryEntity>

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingDetailItemEntity>)

    @Query("SELECT * FROM shopping_detail_items WHERE categoryId = :catId")
    suspend fun getItemsByCategoryId(catId: Int): List<ShoppingDetailItemEntity>

    @Update
    suspend fun updateItem(item: ShoppingDetailItemEntity)

    @Query("DELETE FROM shopping_categories WHERE shoppingListId = :listId")
    suspend fun deleteCategoriesByListId(listId: Int)

}
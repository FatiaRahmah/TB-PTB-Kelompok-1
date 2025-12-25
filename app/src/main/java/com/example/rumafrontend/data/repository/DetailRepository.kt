package com.example.rumafrontend.data.repository

import com.example.rumafrontend.data.dao.CategoryDao
import com.example.rumafrontend.data.dao.ItemDao
import com.example.rumafrontend.data.entity.CategoryEntity
import com.example.rumafrontend.data.entity.ItemEntity
import com.example.rumafrontend.data.model.ShoppingListDetailResponse
import com.example.rumafrontend.data.model.CategoryResponse
import com.example.rumafrontend.data.model.ItemResponse

class DetailRepository(
    private val categoryDao: CategoryDao,
    private val itemDao: ItemDao
) {

    private val api = com.example.rumafrontend.network.ApiClient.apiService

    
    
    
    suspend fun getCategories(shoppingItemId: Int): List<CategoryEntity> {
        return categoryDao.getByShoppingItemId(shoppingItemId)
    }

    suspend fun syncDetail(listId: Int) {
        try {
            val detail = api.getDetail(listId)

            
            val oldCats = categoryDao.getByShoppingItemId(listId)
            oldCats.forEach { cat ->
                itemDao.deleteByCategoryId(cat.id)
            }
            categoryDao.deleteByShoppingItemId(listId)

            
            detail.categories.forEach { catRes ->
                categoryDao.insert(CategoryEntity(
                    id = catRes.id,
                    shoppingItemId = listId,
                    name = catRes.name,
                    iconName = "default", 
                    expanded = true
                ))

                catRes.items?.forEach { itemRes ->
                    itemDao.insert(ItemEntity(
                        id = itemRes.id,
                        categoryId = catRes.id,
                        text = itemRes.name,
                        checked = itemRes.checked
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addCategory(category: CategoryEntity) {
        
        
        
        try {
            val request = com.example.rumafrontend.data.model.CreateCategoryRequest(
                shopping_list_id = category.shoppingItemId,
                name = category.name
            )
            
            
            val response = api.createCategory(request)
            
            
            
            val map = response as? Map<*, *>
            val newId = (map?.get("id") as? Number)?.toInt() ?: 0
            

            categoryDao.insert(category.copy(id = newId))
            
            
        } catch (e: Exception) {
            
            throw e
        }
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }

    
    
    
    suspend fun getItems(categoryId: Int): List<ItemEntity> {
        return itemDao.getByCategoryId(categoryId)
    }

    suspend fun addItem(item: ItemEntity) {
        
        val request = com.example.rumafrontend.data.model.CreateItemRequest(
            category_id = item.categoryId,
            name = item.text
        )
        val response = api.createItem(request)
        val map = response as? Map<*, *>
        val newId = (map?.get("id") as? Number)?.toInt() ?: 0

        itemDao.insert(item.copy(id = newId))
    }

    
    suspend fun insertItem(item: ItemEntity) {
        itemDao.insert(item)
    }

    suspend fun updateItem(item: ItemEntity) {
        
        val request = com.example.rumafrontend.data.model.UpdateItemRequest(
            checked = item.checked
        )
        api.updateItemElement(item.id, request)
        itemDao.update(item)
    }

    
    suspend fun deleteItem(item: ItemEntity) {
        itemDao.delete(item)
    }
}
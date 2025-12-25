package com.example.rumafrontend.data.repository

import com.example.rumafrontend.data.dao.ShoppingItemDao
import com.example.rumafrontend.data.dao.CategoryDao
import com.example.rumafrontend.data.dao.ItemDao
import com.example.rumafrontend.data.entity.ShoppingItemEntity
import com.example.rumafrontend.data.entity.CategoryEntity
import com.example.rumafrontend.data.entity.ItemEntity
import com.example.rumafrontend.data.model.CreateShoppingListRequest
import com.example.rumafrontend.network.ApiClient

class ShoppingListRepository(
    private val dao: ShoppingItemDao,
    private val categoryDao: CategoryDao,
    private val itemDao: ItemDao
) {

    private val api = ApiClient.apiService

    
    
    
    suspend fun syncFromApi() {
        val response = api.getShoppingLists()
        val uiFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
        val apiFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        response.forEach {
            
            val dateForDb = try {
                val parsedApi = apiFormat.parse(it.shopping_date)
                uiFormat.format(parsedApi ?: java.util.Date())
            } catch (e: Exception) {
                it.shopping_date
            }

            dao.insert(
                ShoppingItemEntity(
                    id = it.id,
                    title = it.title,
                    date = dateForDb,
                    done = false
                )
            )
        }
    }

    
    
    
    suspend fun getAll(): List<ShoppingItemEntity> {
        return dao.getAll()
    }

    
    
    
    suspend fun addShoppingList(title: String, date: String): Int {
        
        try {
            
            
            val request = CreateShoppingListRequest(
                title = title,
                shoppingDate = date
            )
            val result = api.createShoppingList(request)
            

            
            val uiFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
            val apiFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

            val dateForDb = try {
                val parsedApi = apiFormat.parse(result.shopping_date)
                uiFormat.format(parsedApi ?: java.util.Date())
            } catch (e: Exception) {
                result.shopping_date 
            }

            
            
            dao.insert(
                ShoppingItemEntity(
                    id = result.id,
                    title = result.title,
                    date = dateForDb, 
                    done = false
                )
            )
            
            return result.id
        } catch (e: Exception) {
            
            e.printStackTrace()
            
            
            val mockId = (1..1000).random()
            val uiFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
            val apiFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val dateForDb = try {
                val parsedApi = apiFormat.parse(date)
                uiFormat.format(parsedApi ?: java.util.Date())
            } catch (e: Exception) {
                date
            }
            dao.insert(
                ShoppingItemEntity(
                    id = mockId,
                    title = title,
                    date = dateForDb,
                    done = false
                )
            )
            return mockId
        }
    }

    
    
    
    suspend fun getCategories(listId: Int): List<CategoryEntity> {
        return categoryDao.getByShoppingItemId(listId)
    }

    suspend fun getItems(catId: Int): List<ItemEntity> {
        return itemDao.getByCategoryId(catId)
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
                
                val catEntity = CategoryEntity(
                    id = catRes.id,
                    shoppingItemId = listId,
                    name = catRes.name,
                    iconName = "default",
                    expanded = true
                )
                categoryDao.insert(catEntity)

                
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

    suspend fun addCategory(shoppingItemId: Int, name: String) {
        
        try {
            
            
            val request = com.example.rumafrontend.data.model.CreateCategoryRequest(
                shopping_list_id = shoppingItemId,
                name = name
            )
            val response = api.createCategory(request)
            

            
            
            
            

            
            
            val map = response as? Map<*, *>
            val id = (map?.get("id") as? Number)?.toInt() ?: 0
            

            categoryDao.insert(CategoryEntity(
                id = id,
                shoppingItemId = shoppingItemId,
                name = name,
                iconName = "default", 
                expanded = true
            ))
            
        } catch (e: Exception) {
            
            e.printStackTrace()
            throw e
        }
    }

    suspend fun addItem(categoryId: Int, name: String) {
        val request = com.example.rumafrontend.data.model.CreateItemRequest(
            category_id = categoryId,
            name = name
        )
        val response = api.createItem(request)
        val map = response as? Map<*, *>
        val id = (map?.get("id") as? Number)?.toInt() ?: 0

        itemDao.insert(ItemEntity(
            id = id,
            categoryId = categoryId,
            text = name,
            checked = false
        ))
    }

    suspend fun toggleItem(item: ItemEntity) {
        val newChecked = !item.checked
        val request = com.example.rumafrontend.data.model.UpdateItemRequest(
            checked = newChecked
        )
        api.updateItemElement(item.id, request)
        itemDao.update(item.copy(checked = newChecked))
    }

    suspend fun deleteItem(item: ShoppingItemEntity) {
        dao.delete(item)
        
    }

    suspend fun updateItem(item: ShoppingItemEntity) {
        dao.update(item)
    }
}
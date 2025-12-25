package com.example.rumafrontend.ui.theme.screen.daftarBelanja

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.data.database.RumaDatabase
import com.example.rumafrontend.data.entity.CategoryEntity
import com.example.rumafrontend.data.entity.ItemEntity
import com.example.rumafrontend.data.model.CreateItemRequest
import com.example.rumafrontend.data.repository.DetailRepository
import com.example.rumafrontend.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

class ShoppingDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RumaDatabase.getDatabase(application)
    private val repository = DetailRepository(
        db.categoryDao(),
        db.itemDao()
    )

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private var lastShoppingItemId: Int = -1

    
    fun load(shoppingItemId: Int) {
        
        lastShoppingItemId = shoppingItemId
        viewModelScope.launch {
            try {
                
                
                repository.syncDetail(shoppingItemId)
                
                
                
                reload()
                
            } catch (e: Exception) {
                
            }
        }
    }

    private suspend fun reload() {
        val categoryEntities = repository.getCategories(lastShoppingItemId)

        val uiCategories = categoryEntities.map { categoryEntity ->

            val items = repository.getItems(categoryEntity.id).map { itemEntity ->
                Item(
                    id = itemEntity.id,              
                    text = itemEntity.text,
                    checked = itemEntity.checked
                )
            }

            Category(
                id = categoryEntity.id,  
                name = categoryEntity.name,
                icon = iconFromName(categoryEntity.iconName),
                items = items,
                expanded = categoryEntity.expanded,
                editable = categoryEntity.name == "Lainnya"
            )
        }

        _categories.value = uiCategories
        
        uiCategories.forEachIndexed { index, cat ->
            
        }
    }

    fun addCategory(
        shoppingItemId: Int,
        category: Category
    ) {
        
        
        
        viewModelScope.launch {
            try {
                
                val categoryEntity = CategoryEntity(
                    id = 0, 
                    shoppingItemId = shoppingItemId,
                    name = category.name,
                    iconName = iconToName(category.icon),
                    expanded = true
                )
                
                
                repository.addCategory(categoryEntity)
                
                
                reload()
                
            } catch (e: Exception) {
                
                
                
                val tempId = (1000..9999).random()
                val newCategory = category.copy(id = tempId, expanded = true)
                val currentList = _categories.value.toMutableList()
                currentList.add(newCategory)
                _categories.value = currentList
                
                
            }
        }
    }

    fun updateCategory(
        shoppingItemId: Int,
        index: Int,
        category: Category
    ) {
        
        
        
        
        val currentList = _categories.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = category
            _categories.value = currentList
            
        }
        
        
        viewModelScope.launch {
            try {
                val entities = repository.getCategories(shoppingItemId)
                if (index !in entities.indices) {
                    
                    return@launch
                }

                val categoryEntity = entities[index]
                

                
                repository.updateCategory(
                    categoryEntity.copy(
                        name = category.name,
                        expanded = category.expanded
                    )
                )

                
                val existingItems = repository.getItems(categoryEntity.id)
                

                
                existingItems.forEachIndexed { i, existingItem ->
                    if (i >= category.items.size) {
                        
                        repository.deleteItem(existingItem)
                    }
                }

                
                category.items.forEachIndexed { i, item ->
                    if (i < existingItems.size) {
                        
                        val updatedItem = existingItems[i].copy(
                            text = item.text,
                            checked = item.checked
                        )
                        
                        repository.updateItem(updatedItem)
                        
                        
                        try {
                            val request = com.example.rumafrontend.data.model.UpdateItemRequest(
                                checked = item.checked
                            )
                            ApiClient.apiService.updateItemElement(updatedItem.id, request)
                        } catch (e: Exception) {
                            
                        }
                    } else {
                        
                        
                        
                        
                        var backendId = 0
                        try {
                            val request = CreateItemRequest(
                                category_id = categoryEntity.id,
                                name = item.text
                            )
                            val response = ApiClient.apiService.createItem(request)
                            val map = response as? Map<*, *>
                            backendId = (map?.get("id") as? Number)?.toInt() ?: 0
                            
                        } catch (e: Exception) {
                            
                            backendId = (10000..99999).random()
                        }
                        
                        
                        val newItemEntity = ItemEntity(
                            id = backendId,
                            categoryId = categoryEntity.id,
                            text = item.text,
                            checked = item.checked
                        )
                        repository.insertItem(newItemEntity)
                        
                    }
                }

                
                
            } catch (e: Exception) {
                
            }
        }
    }

    private fun iconFromName(name: String) =
        when (name) {
            "Fastfood" -> Icons.Default.Fastfood
            "Build" -> Icons.Default.Build
            "Favorite" -> Icons.Default.Favorite
            "Checkroom" -> Icons.Default.Checkroom
            else -> Icons.Default.MoreHoriz
        }

    private fun iconToName(icon: ImageVector) =
        when (icon) {
            Icons.Default.Fastfood -> "Fastfood"
            Icons.Default.Build -> "Build"
            Icons.Default.Favorite -> "Favorite"
            Icons.Default.Checkroom -> "Checkroom"
            else -> "MoreHoriz"
        }
}
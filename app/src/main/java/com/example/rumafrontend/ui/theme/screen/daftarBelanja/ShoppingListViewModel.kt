package com.example.rumafrontend.ui.theme.screen.daftarBelanja

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.data.database.RumaDatabase
import com.example.rumafrontend.data.entity.ShoppingItemEntity
import com.example.rumafrontend.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RumaDatabase.getDatabase(application)
    private val dao = db.shoppingItemDao()
    private val categoryDao = db.categoryDao()
    private val itemDao = db.itemDao()

    private val repository = ShoppingListRepository(dao, categoryDao, itemDao)

    private val _items = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val items: StateFlow<List<ShoppingItem>> = _items

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            try {
                repository.syncFromApi()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val data = repository.getAll()
            _items.value = data.map {
                ShoppingItem(
                    id = it.id,
                    title = it.title,
                    date = it.date,
                    categories = emptyList(),
                    done = it.done
                )
            }
        }
    }

    fun addItem(title: String, date: String, onSuccess: (Int) -> Unit) {
        
        viewModelScope.launch {
            try {
                
                val newId = repository.addShoppingList(title, date)
                
                val updatedList = repository.getAll()
                _items.value = updatedList.map {
                    ShoppingItem(
                        id = it.id,
                        title = it.title,
                        date = it.date,
                        categories = emptyList(),
                        done = it.done
                    )
                }
                
                onSuccess(newId)
            } catch (e: Exception) {
                
                e.printStackTrace()

            }

        }
    }

    fun deleteItem(index: Int) {
        val item = _items.value.getOrNull(index) ?: return

        viewModelScope.launch {
            repository.deleteItem(
                ShoppingItemEntity(
                    id = item.id,
                    title = item.title,
                    date = item.date,
                    done = item.done
                )
            )
            loadItems()
        }
    }

    fun toggleDone(index: Int) {
        val item = _items.value.getOrNull(index) ?: return

        viewModelScope.launch {
            repository.updateItem(
                ShoppingItemEntity(
                    id = item.id,
                    title = item.title,
                    date = item.date,
                    done = !item.done
                )
            )
            loadItems()
        }
    }

    fun updateCategories(
        listIndex: Int,
        categories: List<Category>
    ) {
        _items.value = _items.value.toMutableList().also {
            if (listIndex in it.indices) {
                it[listIndex] = it[listIndex].copy(
                    categories = categories
                )
            }
        }
    }
}
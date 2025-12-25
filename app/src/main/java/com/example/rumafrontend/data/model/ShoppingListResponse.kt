package com.example.rumafrontend.data.model

data class ShoppingListResponse(
    val id: Int,
    val title: String,
    val shopping_date: String
)

data class ShoppingListDetailResponse(
    val id: Int,
    val title: String,
    val shopping_date: String,
    val categories: List<CategoryResponse>
)

data class CategoryResponse(
    val id: Int,
    val name: String,
    val items: List<ItemResponse>?
)

data class ItemResponse(
    val id: Int,
    val name: String,
    val checked: Boolean
)
package com.example.rumafrontend.data.model

data class CreateShoppingListRequest(
    val title: String,
    val shoppingDate: String
)

data class CreateCategoryRequest(
    val shopping_list_id: Int,
    val name: String
)

data class CreateItemRequest(
    val category_id: Int,
    val name: String
)

data class UpdateItemRequest(
    val checked: Boolean
)

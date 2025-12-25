package com.example.rumafrontend.ui.theme.screen.daftarBelanja

data class ShoppingItem(
    val id: Int,
    val title: String,
    val date: String,
    val categories: List<Category>,
    val done: Boolean
)
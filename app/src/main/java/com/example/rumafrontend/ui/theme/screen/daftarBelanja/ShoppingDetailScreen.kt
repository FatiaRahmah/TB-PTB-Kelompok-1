package com.example.rumafrontend.ui.theme.screen.daftarBelanja

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

data class Item(
    val id: Int = 0,
    val text: String,
    val checked: Boolean = false
)

data class Category(
    val id: Int = 0,
    val name: String,
    val icon: ImageVector,
    val items: List<Item> = emptyList(),
    val expanded: Boolean = true,
    val editable: Boolean = false
)

@Composable
fun ShoppingDetailScreen(
    shoppingItemId: Int,
    title: String,
    date: String,
    onBack: () -> Unit,
    listViewModel: ShoppingListViewModel = viewModel(),
    detailViewModel: ShoppingDetailViewModel = viewModel()
) {
    
    LaunchedEffect(shoppingItemId) {
        
        if (shoppingItemId == -1) {
            
            return@LaunchedEffect
        }
        detailViewModel.load(shoppingItemId)
    }

    val categories by detailViewModel.categories.collectAsState()
    
    
    var showCategoryPicker by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavigationBarShoppingDetail() }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5ECDC))
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                    Icon(Icons.Default.MoreVert, null)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF812C2A)
                )

                Text(
                    text = date,
                    fontSize = 16.sp,
                    color = Color(0xFF812C2A)
                )

                Spacer(Modifier.height(20.dp))

                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    
                    categories.forEachIndexed { index, category ->
                        
                        CategorySection(
                            category = category,
                            onUpdate = { updated ->
                                
                                detailViewModel.updateCategory(
                                    shoppingItemId = shoppingItemId,
                                    index = index,
                                    category = updated
                                )
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(160.dp))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 32.dp, bottom = 110.dp)
                    .size(56.dp)
                    .shadow(10.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .clickable { showCategoryPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF6B663D),
                    modifier = Modifier.size(30.dp)
                )
            }

            if (showCategoryPicker) {
                CategorySelectionCard(
                    onSelect = { name, icon ->
                        detailViewModel.addCategory(
                            shoppingItemId = shoppingItemId,
                            category = Category(
                                name = name,
                                icon = icon,
                                editable = name == "Lainnya"
                            )
                        )
                        showCategoryPicker = false
                    },
                    onDismiss = { showCategoryPicker = false }
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    category: Category,
    onUpdate: (Category) -> Unit
) {
    var editing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFFEADFD3), RoundedCornerShape(50.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color(0xFFD8C7B9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = Color(0xFF812C2A),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            if (category.editable && editing) {
                BasicTextField(
                    value = category.name,
                    onValueChange = {
                        onUpdate(category.copy(name = it))
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF812C2A)
                    ),
                    modifier = Modifier.weight(1f),
                    keyboardActions = KeyboardActions(onDone = { editing = false })
                )
            } else {
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF812C2A),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = category.editable) {
                            editing = true
                        }
                )
            }

            Icon(
                imageVector = if (category.expanded)
                    Icons.Default.KeyboardArrowDown
                else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF812C2A),
                modifier = Modifier.clickable {
                    onUpdate(category.copy(expanded = !category.expanded))
                }
            )
        }

        if (category.expanded) {
            
            Spacer(Modifier.height(6.dp))
            CategoryContent(category, onUpdate)
        } else {
            
        }
    }
}

@Composable
fun CategoryContent(
    category: Category,
    onUpdate: (Category) -> Unit
) {
    var newItem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {

        
        category.items.forEachIndexed { index, item ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = item.checked,
                        onClick = {
                            val items = category.items.toMutableList()
                            items[index] = item.copy(checked = !item.checked)
                            onUpdate(category.copy(items = items))
                        }
                    )

                    BasicTextField(
                        value = item.text,
                        onValueChange = { text ->
                            val items = category.items.toMutableList()
                            if (text.isBlank()) {
                                items.removeAt(index)
                            } else {
                                items[index] = item.copy(text = text)
                            }
                            onUpdate(category.copy(items = items))
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 48.dp),
                    thickness = 0.5.dp
                )
            }
        }

        
        if (category.items.isEmpty()) {
            Text(
                text = "Belum ada item. Ketik di bawah untuk menambah.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }

        
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = newItem,
            onValueChange = { newItem = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("○", color = Color(0xFF812C2A), fontSize = 16.sp)
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (newItem.isEmpty()) {
                            Text(
                                "Ketikkan item baru...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                }
            },
            textStyle = TextStyle(fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (newItem.isNotBlank()) {
                        
                        val updatedCategory = category.copy(
                            items = category.items + Item(text = newItem)
                        )
                        onUpdate(updatedCategory)
                        
                        newItem = ""
                    }
                }
            )
        )
    }
}

@Composable
fun CategorySelectionCard(
    onSelect: (String, ImageVector) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.BottomStart
    ) {

        Column(
            modifier = Modifier
                .padding(start = 28.dp, bottom = 120.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp))
                .background(Color(0xFF9B9F6E), RoundedCornerShape(28.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            CategoryPill(Icons.Default.Fastfood, "Makanan/Minuman") {
                onSelect("Makanan/Minuman", Icons.Default.Fastfood)
            }
            CategoryPill(Icons.Default.Checkroom, "Pakaian") {
                onSelect("Pakaian", Icons.Default.Checkroom)
            }
            CategoryPill(Icons.Default.Favorite, "Kesehatan") {
                onSelect("Kesehatan", Icons.Default.Favorite)
            }
            CategoryPill(Icons.Default.Build, "Peralatan") {
                onSelect("Peralatan", Icons.Default.Build)
            }
            CategoryPill(Icons.Default.MoreHoriz, "Lainnya") {
                onSelect("Lainnya", Icons.Default.MoreHoriz)
            }
        }
    }
}

@Composable
fun CategoryPill(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(260.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}

@Composable
fun BottomNavigationBarShoppingDetail() {
    NavigationBar(containerColor = Color(0xFF7A2322)) {
        NavigationBarItem(false, {}, { Icon(Icons.Default.Favorite, null) })
        NavigationBarItem(false, {}, { Icon(Icons.Default.Home, null) })
        NavigationBarItem(false, {}, { Icon(Icons.Default.Person, null) })
    }
}
package com.example.rumafrontend.ui.theme.screen.daftarBelanja

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateToDetail: (Int, String, String) -> Unit,
    onNavigateToMap: () -> Unit,            
    onBackClick: () -> Unit = {},
    viewModel: ShoppingListViewModel = viewModel()
) {
    val bg = Color(0xFFF4ECDD)
    val items by viewModel.items.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    }

    val filteredItems = items
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .sortedWith(compareBy({ it.done }, { dateFormat.parse(it.date) }))

    val selesaiCount = items.count { it.done }

    Scaffold(
        containerColor = bg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Color(0xFF6B7F2A),
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Edit, null)
            }
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.clickable { onBackClick() }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "DAFTAR BELANJA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A2322)
                    )
                }

                
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Maps",
                    modifier = Modifier.clickable {
                        onNavigateToMap()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(6.dp, RoundedCornerShape(26.dp)),
                placeholder = { Text("Cari daftar belanja") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFB8B893),
                    unfocusedContainerColor = Color(0xFFB8B893),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(26.dp),
                singleLine = true
            )

            Spacer(Modifier.height(18.dp))

            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(Icons.AutoMirrored.Filled.List, "List", items.size)
                StatCard(Icons.Default.CheckCircle, "Selesai", selesaiCount)
            }

            Spacer(Modifier.height(18.dp))

            
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC89C92), RoundedCornerShape(22.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "List Belanja Saya",
                    color = Color(0xFF7A2322),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            
            LazyColumn {
                itemsIndexed(filteredItems) { _, item ->
                    val realIndex = items.indexOf(item)
                    if (realIndex == -1) return@itemsIndexed

                    SwipeDeleteRow(
                        onDelete = { viewModel.deleteItem(realIndex) }
                    ) {
                        ShoppingRow(
                            title = item.title,
                            date = item.date,
                            done = item.done,
                            onCheckedChange = {
                                viewModel.toggleDone(realIndex)
                            },
                            onClick = {
                                
                                onNavigateToDetail(
                                    item.id,
                                    item.title,
                                    item.date
                                )
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddSheet) {
        AddListBottomSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { title, date, onSuccess ->
                
                viewModel.addItem(title, date) { newId ->
                    
                    showAddSheet = false
                    
                    onSuccess(newId)
                    
                    onNavigateToDetail(newId, title, date)
                }
            }
        )
    }
}

@Composable
private fun SwipeDeleteRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val maxOffset = -160f

    Box(Modifier.fillMaxWidth()) {

        Box(
            Modifier
                .matchParentSize()
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = {
                    onDelete()
                    scope.launch { offsetX.animateTo(0f, tween(200)) }
                },
                modifier = Modifier.background(Color.Red, RoundedCornerShape(14.dp))
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.White)
            }
        }

        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < maxOffset / 2)
                                    offsetX.animateTo(maxOffset)
                                else
                                    offsetX.animateTo(0f)
                            }
                        }
                    ) { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(
                                (offsetX.value + dragAmount)
                                    .coerceIn(maxOffset, 0f)
                            )
                        }
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, title: String, value: Int) {
    Box(
        Modifier
            .size(width = 160.dp, height = 120.dp)
            .background(Color(0xFFE2E2E2), RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null)
                Spacer(Modifier.width(10.dp))
                Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ShoppingRow(
    title: String,
    date: String,
    done: Boolean,
    onCheckedChange: () -> Unit,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (done) Color(0xFFD6D6D6) else Color(0xFFE2E2E2),
        tween(200),
        label = ""
    )

    val alpha by animateFloatAsState(
        if (done) 0.5f else 1f,
        tween(200),
        label = ""
    )

    Row(
        Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(24.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .alpha(alpha)
                .clickable { onClick() }
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(date, fontSize = 13.sp)
        }

        Box(
            modifier = Modifier
                .size(26.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
                .clickable { onCheckedChange() },
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, (Int) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2E3516),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.padding(20.dp)) {

            Row(Modifier.fillMaxWidth()) {
                Text("Kembali", color = Color.White, modifier = Modifier.clickable { onDismiss() })
                Spacer(Modifier.weight(1f))
                Text(
                    "Selesai",
                    color = if (title.isNotBlank() && date.isNotBlank()) Color.White else Color.Gray,
                    modifier = Modifier.clickable(
                        enabled = title.isNotBlank() && date.isNotBlank()
                    ) {
                        onAdd(title, date) { newId ->
                            
                        }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            BasicTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF59612C), RoundedCornerShape(14.dp))
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF59612C), RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(date.ifEmpty { "Tanggal Belanja" }, color = Color.White)
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        val locale = Locale("id", "ID")
                        date = SimpleDateFormat("dd MMM yyyy", locale).format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = pickerState) }
    }

    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
        keyboard?.show()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewShoppingList() {}
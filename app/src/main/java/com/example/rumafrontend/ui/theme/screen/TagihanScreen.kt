package com.example.rumafrontend.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import com.example.rumafrontend.utils.FileUtils

import com.example.rumafrontend.ui.theme.BackgroundColor
import com.example.rumafrontend.ui.theme.PrimaryRed
import com.example.rumafrontend.ui.theme.SecondaryBrown
import com.example.rumafrontend.ui.theme.TextLight
import com.example.rumafrontend.ui.theme.BottomBarColor
import com.example.rumafrontend.ui.theme.TextDark
import com.example.rumafrontend.ui.theme.reminderschedule.scheduleTagihanReminder
import com.example.rumafrontend.ui.theme.rumaFrontendTheme

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import com.example.rumafrontend.data.model.Bill
import com.example.rumafrontend.data.model.SortType
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.foundation.layout.Box
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagihanScreen(
    navController: NavController,
    viewModel: TagihanViewModel,
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(SortType.TERDEKAT) }
    val bills by viewModel.tagihanList.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<Bill?>(null) }

    var showCompleteDialog by remember { mutableStateOf(false) }
    var billToComplete by remember { mutableStateOf<Bill?>(null) }
    
    
    val completionDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showCompletionDatePicker by remember { mutableStateOf(false) }
    var selectedCompletionDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val activeBills = bills.filter { it.status == "belum" }

    val context = LocalContext.current

    
    var showPhotoPreview by remember { mutableStateOf(false) }
    var selectedPreviewPhoto by remember { mutableStateOf<String?>(null) }

    val filteredAndSortedBills = activeBills
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .sortedWith(
            compareBy {
                when (sortType) {
                    SortType.TERDEKAT -> it.dueDateMillis
                    SortType.TERLAMA -> -it.dueDateMillis
                }
            }
        )

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = { SimpleBottomBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundColor)
        ) {

            Button(

                
                onClick = {
                    scheduleTagihanReminder(
                        context = context,
                        tagihanId = 999,
                        tagihanTitle = "TEST NOTIFIKASI",
                        dueDateMillis = System.currentTimeMillis() + 60_000, 
                        reminderDays = 0
                    )
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("TEST NOTIFIKASI")
            }
            

            HeaderSection(
                onAddClicked = { navController.navigate("addTagihan") },
                onHistoryClicked = { navController.navigate("riwayat") },
                onOverdueClicked = { navController.navigate("overdue") },
                onCalendarClicked = { navController.navigate("calendar")  }
            )

            SearchBarSection(
                query = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                currentSortType = sortType,
                onSortTypeChange = { sortType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAndSortedBills.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SecondaryBrown,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada tagihan",
                            fontSize = 18.sp,
                            color = PrimaryRed,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tambahkan tagihan baru dengan tombol +",
                            fontSize = 14.sp,
                            color = SecondaryBrown
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), 
                    contentPadding = PaddingValues(16.dp)
                )
                {
                    items(filteredAndSortedBills) { bill ->
                        BillCardItem(
                            bill = bill,
                            onEditClicked = {
                                navController.navigate("editTagihan/${bill.id}")
                            },
                            onToggleStatus = {
                                billToComplete = bill
                                showCompleteDialog = true
                            },
                            onDeleteClicked = {
                                billToDelete = bill
                                showDeleteDialog = true
                            },
                            onPhotoClicked = { photoUrl ->
                                selectedPreviewPhoto = photoUrl
                                showPhotoPreview = true
                            }
                        )
                    }
                }
            }
        }
    }

    
    if (showPhotoPreview && selectedPreviewPhoto != null) {
        PhotoPreviewDialog(
            photoUrl = selectedPreviewPhoto!!,
            onDismiss = {
                showPhotoPreview = false
                selectedPreviewPhoto = null
            }
        )
    }

    
    if (showDeleteDialog && billToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Tagihan?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus tagihan \"${billToDelete?.title}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        billToDelete?.let { viewModel.hapusTagihan(context, it.id) }
                        showDeleteDialog = false
                        billToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    billToDelete = null
                }) {
                    Text("Batal", color = SecondaryBrown)
                }
            },
            containerColor = BackgroundColor
        )
    }

    
    if (showCompleteDialog && billToComplete != null) {
        AlertDialog(
            onDismissRequest = { 
                showCompleteDialog = false
                selectedImageUri = null
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SecondaryBrown,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Tandai Selesai?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tagihan \"${billToComplete?.title}\" akan dipindahkan ke Riwayat.")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(selectedCompletionDateMillis))
                    OutlinedTextField(
                        value = dateFormatted,
                        onValueChange = {},
                        label = { Text("Tanggal Selesai") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCompletionDatePicker = true },
                        enabled = false, 
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextDark,
                            disabledBorderColor = PrimaryRed,
                            disabledLabelColor = PrimaryRed
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showCompletionDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Bukti Pembayaran",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                                Text("Tambah Bukti Foto", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    if (selectedImageUri != null) {
                        TextButton(onClick = { selectedImageUri = null }) {
                            Text("Hapus Foto", color = PrimaryRed)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val savedPath = selectedImageUri?.let { uri ->
                            FileUtils.saveImageToInternalStorage(context, uri)
                        }
                        
                        billToComplete?.let { 
                            viewModel.tandaiSelesai(context, it.id, savedPath, selectedCompletionDateMillis) 
                        }
                        showCompleteDialog = false
                        billToComplete = null
                        selectedImageUri = null
                        selectedCompletionDateMillis = System.currentTimeMillis() 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryBrown
                    )
                ) {
                    Text("Ya, Selesai")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    billToComplete = null
                    selectedImageUri = null
                    selectedCompletionDateMillis = System.currentTimeMillis() 
                }) {
                    Text("Batal", color = PrimaryRed)
                }
            },
            containerColor = BackgroundColor
        )
    }

    
    if (showCompletionDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showCompletionDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedCompletionDateMillis = completionDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showCompletionDatePicker = false
                }) {
                    Text("OK", color = PrimaryRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDatePicker = false }) {
                    Text("Batal", color = SecondaryBrown)
                }
            }
        ) {
            DatePicker(state = completionDatePickerState)
        }
    }
}

@Composable
fun HeaderSection(
    onAddClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onOverdueClicked: () -> Unit, 
    onCalendarClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Tagihan Icon",
                tint = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tagihan Saya",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )
        }

        IconButton(onClick = onAddClicked) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Bill",
                tint = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Button(
            onClick = onOverdueClicked,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Lewat", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        
        Button(
            onClick = onHistoryClicked,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryBrown),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Riwayat", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        
        IconButton(onClick = onCalendarClicked) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Calendar",
                tint = PrimaryRed
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSection(
    query: String,
    onSearchQueryChange: (String) -> Unit,
    currentSortType: SortType,
    onSortTypeChange: (SortType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Cari judul tagihan...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = Color.LightGray,
            )
        )

        Box(modifier = Modifier.padding(start = 8.dp)) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Sort/Filter",
                    tint = PrimaryRed
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Tenggat Terdekat") },
                    onClick = {
                        onSortTypeChange(SortType.TERDEKAT)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentSortType == SortType.TERDEKAT) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Tenggat Terlama") },
                    onClick = {
                        onSortTypeChange(SortType.TERLAMA)
                        expanded = false
                    },
                    leadingIcon = {
                        if (currentSortType == SortType.TERLAMA) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BillCardItem(
    bill: Bill,
    onEditClicked: (Int) -> Unit,
    onToggleStatus: (Int) -> Unit,
    onDeleteClicked: (Int) -> Unit,
    onPhotoClicked: (String) -> Unit 
) {
    var expanded by remember { mutableStateOf(false) }
    
    
    val photoUrl = remember(bill.buktiFotoPath) {
        com.example.rumafrontend.utils.UrlUtils.getPhotoUrl(bill.buktiFotoPath)
    }

    val remainingDaysText = when (bill.status) {
        "lunas" -> "LUNAS"
        else -> {
            val diff = bill.dueDateMillis - Date().time
            val days = (diff / 86400000).toInt()
            if (days < 0) "Terlambat ${days.toLong() * -1} hari" else "${days + 1} hari lagi"
        }
    }

    val dueDateString = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(bill.dueDateMillis))
    val statusColor = if (bill.status == "lunas") SecondaryBrown else TextLight

    val isToday = remember(bill.dueDateMillis) {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = bill.dueDateMillis }
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEditClicked(bill.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            if (!photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(end = 12.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { onPhotoClicked(photoUrl) }, 
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = remainingDaysText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                
                if (isToday) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "HARI INI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dueDateString,
                    fontSize = 14.sp,
                    color = TextLight.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = TextLight
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Selesai") },
                            onClick = {
                                onToggleStatus(bill.id)
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEditClicked(bill.id)
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Hapus", color = PrimaryRed) },
                            onClick = {
                                onDeleteClicked(bill.id)
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = PrimaryRed) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleBottomBar() {
    NavigationBar(
        containerColor = BottomBarColor,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorite") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryBrown,
                unselectedIconColor = TextLight,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryBrown,
                unselectedIconColor = TextLight,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SecondaryBrown,
                unselectedIconColor = TextLight,
                indicatorColor = Color.Transparent
            )
        )
    }
}


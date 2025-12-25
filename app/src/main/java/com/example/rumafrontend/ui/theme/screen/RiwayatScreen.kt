package com.example.rumafrontend.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rumafrontend.ui.theme.BackgroundColor
import com.example.rumafrontend.ui.theme.PrimaryRed
import com.example.rumafrontend.ui.theme.SecondaryBrown
import com.example.rumafrontend.ui.theme.TextLight
import com.example.rumafrontend.ui.theme.TextDark
import com.example.rumafrontend.ui.theme.BottomBarColor
import java.text.SimpleDateFormat
import java.util.*
import com.example.rumafrontend.data.model.Bill
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.rumafrontend.utils.FileUtils

enum class RiwayatSortType {
    TERBARU, TERLAMA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    navController: NavController,
    viewModel: TagihanViewModel
) {
    val riwayatList by viewModel.riwayatList.collectAsState()

    
    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(RiwayatSortType.TERBARU) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<Bill?>(null) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var billToRestore by remember { mutableStateOf<Bill?>(null) }

    val context = LocalContext.current
    
    
    var billToUpdatePhoto by remember { mutableStateOf<Bill?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            billToUpdatePhoto?.let { bill ->
                val savedPath = FileUtils.saveImageToInternalStorage(context, selectedUri)
                viewModel.tandaiSelesai(context, bill.id, savedPath)
            }
        }
        billToUpdatePhoto = null
    }

    
    val filteredRiwayat = riwayatList
        .filter { bill ->
            
            val matchSearch = bill.title.contains(searchQuery, ignoreCase = true)

            
            val billCalendar = Calendar.getInstance().apply {
                timeInMillis = bill.dueDateMillis
            }
            val matchMonth = selectedMonth?.let { billCalendar.get(Calendar.MONTH) == it } ?: true
            val matchYear = selectedYear?.let { billCalendar.get(Calendar.YEAR) == it } ?: true

            matchSearch && matchMonth && matchYear
        }
        .sortedWith(
            when (sortType) {
                RiwayatSortType.TERBARU -> compareByDescending { it.dueDateMillis }
                RiwayatSortType.TERLAMA -> compareBy { it.dueDateMillis }
            }
        )

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Riwayat Icon",
                            tint = PrimaryRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Riwayat Tagihan",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        },
        bottomBar = { SimpleBottomBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundColor)
        ) {
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cari riwayat...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = Color.LightGray,
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                
                var showSortMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = PrimaryRed
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Terbaru") },
                            onClick = {
                                sortType = RiwayatSortType.TERBARU
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortType == RiwayatSortType.TERBARU) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Terlama") },
                            onClick = {
                                sortType = RiwayatSortType.TERLAMA
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortType == RiwayatSortType.TERLAMA) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }

                
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (selectedMonth != null || selectedYear != null) SecondaryBrown else PrimaryRed
                    )
                }
            }

            
            if (selectedMonth != null || selectedYear != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val monthNames = listOf(
                        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                    )

                    if (selectedMonth != null) {
                        FilterChip(
                            selected = true,
                            onClick = { selectedMonth = null },
                            label = { Text(monthNames[selectedMonth!!]) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryBrown,
                                selectedLabelColor = TextLight
                            )
                        )
                    }

                    if (selectedYear != null) {
                        FilterChip(
                            selected = true,
                            onClick = { selectedYear = null },
                            label = { Text(selectedYear.toString()) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryBrown,
                                selectedLabelColor = TextLight
                            )
                        )
                    }

                    TextButton(onClick = {
                        selectedMonth = null
                        selectedYear = null
                    }) {
                        Text("Reset", color = PrimaryRed)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredRiwayat.isEmpty()) {
                
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = SecondaryBrown,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedMonth != null || selectedYear != null) {
                                "Tidak ada riwayat yang cocok"
                            } else {
                                "Belum ada riwayat"
                            },
                            fontSize = 18.sp,
                            color = PrimaryRed,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedMonth != null || selectedYear != null) {
                                "Coba ubah filter pencarian"
                            } else {
                                "Tagihan yang sudah diselesaikan akan muncul di sini"
                            },
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
                    items(filteredRiwayat) { bill ->
                        RiwayatCardItem(
                            bill = bill,
                            onRestoreClicked = {
                                billToRestore = bill
                                showRestoreDialog = true
                            },
                            onDeleteClicked = {
                                billToDelete = bill
                                showDeleteDialog = true
                            },
                            onEditPhotoClicked = {
                                billToUpdatePhoto = bill
                                imagePickerLauncher.launch("image/*")
                            },
                            onEditClicked = {
                                navController.navigate("editTagihan/${bill.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    
    if (showFilterDialog) {
        FilterDialog(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthSelected = { selectedMonth = it },
            onYearSelected = { selectedYear = it },
            onDismiss = { showFilterDialog = false }
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
                    text = "Hapus Riwayat?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Riwayat \"${billToDelete?.title}\" akan dihapus permanen.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        billToDelete?.let { viewModel.hapusDariRiwayat(it.id) }
                        showDeleteDialog = false
                        billToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
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

    if (showRestoreDialog && billToRestore != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            icon = {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = null,
                    tint = SecondaryBrown,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Kembalikan Tagihan?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("\"${billToRestore?.title}\" akan dikembalikan ke tagihan aktif.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        billToRestore?.let { viewModel.kembalikanKeAktif(context, it.id) }
                        showRestoreDialog = false
                        billToRestore = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryBrown)
                ) {
                    Text("Kembalikan")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    billToRestore = null
                }) {
                    Text("Batal", color = PrimaryRed)
                }
            },
            containerColor = BackgroundColor
        )
    }
}

@Composable
fun RiwayatCardItem(
    bill: Bill,
    onRestoreClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onEditPhotoClicked: () -> Unit,
    onEditClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayDate = bill.tanggalSelesaiMillis ?: bill.dueDateMillis
    val dueDateString =
        SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(displayDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEditClicked() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryBrown),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selesai",
                        tint = TextLight,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = bill.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selesai: $dueDateString",
                            fontSize = 14.sp,
                            color = TextLight.copy(alpha = 0.8f)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
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
                            text = { Text("Ganti Foto") },
                            onClick = {
                                onEditPhotoClicked()
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Kembalikan") },
                            onClick = {
                                onRestoreClicked()
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Hapus", color = PrimaryRed) },
                            onClick = {
                                onDeleteClicked()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = PrimaryRed
                                )
                            }
                        )
                    }
                }
            }
            
            
            if (bill.buktiFotoPath != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = bill.buktiFotoPath,
                    contentDescription = "Bukti Foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, TextLight.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    selectedMonth: Int?,
    selectedYear: Int?,
    onMonthSelected: (Int?) -> Unit,
    onYearSelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear).toList().reversed()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Filter Riwayat",
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )
        },
        text = {
            Column {
                Text(
                    "Bulan",
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMonth == null,
                            onClick = { onMonthSelected(null) },
                            label = { Text("Semua Bulan") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryBrown,
                                selectedLabelColor = TextLight
                            )
                        )
                    }
                    items(monthNames.size) { index ->
                        FilterChip(
                            selected = selectedMonth == index,
                            onClick = { onMonthSelected(index) },
                            label = { Text(monthNames[index]) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryBrown,
                                selectedLabelColor = TextLight
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Tahun",
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedYear == null,
                            onClick = { onYearSelected(null) },
                            label = { Text("Semua Tahun") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryBrown,
                                selectedLabelColor = TextLight
                            )
                        )
                    }
                    items(years.size) { index ->
                        FilterChip(
                            selected = selectedYear == years[index],
                            onClick = { onYearSelected(years[index]) },
                            label = { Text(years[index].toString()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryBrown,
                                selectedLabelColor = TextLight
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRed)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = BackgroundColor
    )
}
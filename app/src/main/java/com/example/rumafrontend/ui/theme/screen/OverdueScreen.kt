package com.example.rumafrontend.ui.theme.screen

import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rumafrontend.ui.theme.BackgroundColor
import com.example.rumafrontend.ui.theme.PrimaryRed
import com.example.rumafrontend.ui.theme.SecondaryBrown
import com.example.rumafrontend.ui.theme.TextLight
import java.text.SimpleDateFormat
import java.util.*
import com.example.rumafrontend.data.model.Bill
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rumafrontend.utils.UrlUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueScreen(
    navController: NavController,
    viewModel: TagihanViewModel
) {
    val context = LocalContext.current
    val overdueBills = viewModel.getOverdueBills()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<Bill?>(null) }

    var showCompleteDialog by remember { mutableStateOf(false) }
    var billToComplete by remember { mutableStateOf<Bill?>(null) }

    
    var showPhotoPreview by remember { mutableStateOf(false) }
    var selectedPreviewPhoto by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Overdue Icon",
                            tint = PrimaryRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tagihan Lewat Tempo",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
            
            if (overdueBills.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${overdueBills.size} tagihan lewat tempo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                            Text(
                                text = "Segera selesaikan pembayaran",
                                fontSize = 14.sp,
                                color = PrimaryRed.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            if (overdueBills.isEmpty()) {
                
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
                            text = "Tidak ada tagihan lewat tempo",
                            fontSize = 18.sp,
                            color = PrimaryRed,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Semua tagihan Anda terkendali",
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
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
                {
                    items(overdueBills) { bill ->
                        OverdueBillCard(
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
                        Spacer(modifier = Modifier.height(12.dp))
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
            onDismissRequest = { showCompleteDialog = false },
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
                Text("Tagihan \"${billToComplete?.title}\" akan dipindahkan ke Riwayat.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        billToComplete?.let { viewModel.tandaiSelesai(context, it.id) }
                        showCompleteDialog = false
                        billToComplete = null
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
                }) {
                    Text("Batal", color = PrimaryRed)
                }
            },
            containerColor = BackgroundColor
        )
    }
}

@Composable
fun PhotoPreviewDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Full Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .clickable(enabled = false) { }
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Close", 
                    tint = Color.White, 
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun OverdueBillCard(
    bill: Bill,
    onEditClicked: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteClicked: () -> Unit,
    onPhotoClicked: (String) -> Unit 
) {
    var expanded by remember { mutableStateOf(false) }

    
    val photoUrl = remember(bill.buktiFotoPath) {
        UrlUtils.getPhotoUrl(bill.buktiFotoPath)
    }

    val diff = Date().time - bill.dueDateMillis
    val daysOverdue = (diff / 86400000).toInt()
    val dueDateString = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(bill.dueDateMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClicked() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bill.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TERLAMBAT $daysOverdue HARI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tenggat: $dueDateString",
                    fontSize = 12.sp,
                    color = TextLight.copy(alpha = 0.8f)
                )
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
                            text = { Text("Tandai Lunas") },
                            onClick = {
                                onToggleStatus()
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEditClicked()
                                expanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Hapus", color = PrimaryRed) },
                            onClick = {
                                onDeleteClicked()
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
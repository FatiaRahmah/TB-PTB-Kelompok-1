package com.example.rumafrontend.ui.theme.screen.resep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailResepScreen(
    id: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleted: () -> Unit,
    viewModel: DetailResepViewModel = hiltViewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    LaunchedEffect(id) {
        viewModel.loadDetail(id)
    }

    val scrollState = rememberScrollState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    
                    IconButton(
                        onClick = { viewModel.toggleFavorite() }
                    ) {
                        val fav = detail?.is_favorit == 1
                        Icon(
                            imageVector = if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (fav) Color(0xFFE57373) else Color.Gray
                        )
                    }

                    
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    menuExpanded = false
                                    detail?.id?.let(onEdit)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.requestDelete()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF3E0)
                )
            )
        }
    ) { innerPadding ->
        when {
            isLoading && detail == null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            detail == null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "Data tidak ditemukan")
                }
            }
            else -> {
                val resep = detail!!
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(Color(0xFFFAF3E0))
                        .verticalScroll(scrollState)
                ) {
                    
                    AsyncImage(
                        model = resep.foto ?: "",
                        contentDescription = resep.judul,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = resep.judul,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Bahan - Bahan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        resep.Bahans.forEach {
                            Text("• ${it.nama_bahan} ${it.jumlah} ${it.satuan}", fontSize = 14.sp)
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Langkah Memasak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        resep.Langkahs
                            .sortedBy { it.urutan }
                            .forEach { langkah ->
                                Text(
                                    text = "${langkah.urutan}. ${langkah.deskripsi}",
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                        errorMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDelete() },
                title = {
                    Text(
                        text = "Hapus Resep Ini?",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = { Text("Resep akan dihapus dari daftar.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.delete(onDeleted) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D2E2E))
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDelete() }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

package com.example.rumafrontend.ui.theme.screen.resep

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.rumafrontend.R
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahResepScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: TambahResepViewModel = hiltViewModel(),
    resepId: Int? = null,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showPhotoDialog by remember { mutableStateOf(false) }

    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.fotoUrl = it.toString() }
        showPhotoDialog = false
    }

    
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            viewModel.fotoUrl = cameraImageUri.toString()
        }
        showPhotoDialog = false
    }

    val localFoto = viewModel.fotoUrl
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(resepId) {
        if (resepId != null) {
            viewModel.startEdit(resepId)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.editingResepId == null) "Tambah Resep" else "Edit Resep",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.submit(onSuccess) }) {
                        Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6D2E2E)
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFFAF3E0))
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            
            Text("Foto Resep", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0D2C0),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(Color(0xFFFFFBF5))
                    .clickable { showPhotoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (!localFoto.isNullOrBlank()) {
                    AsyncImage(
                        model = localFoto,
                        contentDescription = "Foto Resep",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_photo),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Tap untuk tambah foto", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            
            Text("Informasi Resep", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = viewModel.judul,
                        onValueChange = { viewModel.judul = it },
                        label = { Text("Judul Resep") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Row {
                        OutlinedTextField(
                            value = viewModel.porsi,
                            onValueChange = { viewModel.porsi = it },
                            label = { Text("Porsi") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        OutlinedTextField(
                            value = viewModel.waktu,
                            onValueChange = { viewModel.waktu = it },
                            label = { Text("Waktu (menit)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bahan - bahan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { viewModel.addBahan() }) {
                    Text("+ Tambah", color = Color(0xFF6D2E2E), fontSize = 12.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    viewModel.bahanList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item.nama,
                                onValueChange = {
                                    viewModel.updateBahan(index, item.copy(nama = it))
                                },
                                label = { Text("Nama Bahan") },
                                modifier = Modifier.weight(2f)
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = item.jumlah,
                                onValueChange = {
                                    viewModel.updateBahan(index, item.copy(jumlah = it))
                                },
                                label = { Text("Jumlah") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = item.satuan,
                                onValueChange = {
                                    viewModel.updateBahan(index, item.copy(satuan = it))
                                },
                                label = { Text("Satuan") },
                                modifier = Modifier.weight(1f)
                            )
                            if (viewModel.bahanList.size > 1) {
                                IconButton(onClick = { viewModel.removeBahan(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "hapus",
                                        tint = Color(0xFFB3261E)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Langkah - langkah", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { viewModel.addLangkah() }) {
                    Text("+ Tambah", color = Color(0xFF6D2E2E), fontSize = 12.sp)
                }
            }

            viewModel.langkahList.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Langkah ${item.step}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.weight(1f))
                            if (viewModel.langkahList.size > 1) {
                                IconButton(onClick = { viewModel.removeLangkah(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "hapus",
                                        tint = Color(0xFFB3261E)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = item.deskripsi,
                            onValueChange = { viewModel.updateLangkah(index, it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp),
                            label = { Text("Jelaskan langkah memasak...") }
                        )

                        Spacer(Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    Color(0xFFE0D2C0),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.add_photo),
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                                Text(
                                    "Foto langkah (opsional)",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))
        }

        
        if (showPhotoDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoDialog = false },
                title = { Text("Pilih foto resep") },
                confirmButton = {
                    TextButton(onClick = {
                        val tmpFile = File.createTempFile(
                            "resep_", ".jpg",
                            context.cacheDir
                        )
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            tmpFile
                        )
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    }) {
                        Text("Kamera")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Galeri")
                    }
                }
            )
        }
    }
}

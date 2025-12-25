package com.example.rumafrontend.ui.theme.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rumafrontend.ui.theme.BackgroundColor
import com.example.rumafrontend.ui.theme.PrimaryRed
import com.example.rumafrontend.ui.theme.SecondaryBrown
import com.example.rumafrontend.ui.theme.TextDark
import com.example.rumafrontend.ui.theme.TextLight
import com.example.rumafrontend.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagihanScreen(
    navController: NavController,
    viewModel: TagihanViewModel,
    billId: Int? = null
) {
    
    val context = LocalContext.current

    val existingBill = billId?.let { viewModel.getTagihanById(it) }
    val isEditMode = existingBill != null

    var title by rememberSaveable { mutableStateOf(existingBill?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(existingBill?.description ?: "") }
    var reminderDays by rememberSaveable { mutableStateOf(existingBill?.reminderDays ?: 1) }
    var repeatType by rememberSaveable { mutableStateOf(existingBill?.repeatType ?: "Sekali saja") }
    var selectedImageUri by remember { 
        mutableStateOf<Uri?>(existingBill?.buktiFotoPath?.let { Uri.parse(it) }) 
    }

    var selectedDateMillis by rememberSaveable {
        mutableStateOf(existingBill?.dueDateMillis ?: System.currentTimeMillis())
    }
    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")) }
    val dueDateString = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        initialDisplayMode = DisplayMode.Picker
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Logo",
                            tint = PrimaryRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditMode) "Edit Tagihan" else "Tambah Tagihan",
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
                            tint = PrimaryRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Bayar tagihan Listrik",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Masukkan judul tagihan",
                                color = TextLight.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PrimaryRed,
                            unfocusedContainerColor = PrimaryRed,
                            focusedBorderColor = TextLight,
                            unfocusedBorderColor = TextLight.copy(alpha = 0.5f),
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            cursorColor = TextLight
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Deskripsi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text(
                                "Jangan sampai telat",
                                color = TextLight.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PrimaryRed,
                            unfocusedContainerColor = PrimaryRed,
                            focusedBorderColor = TextLight,
                            unfocusedBorderColor = TextLight.copy(alpha = 0.5f),
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight,
                            cursorColor = TextLight
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            tint = TextLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tenggat",
                                fontSize = 12.sp,
                                color = TextLight.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dueDateString,
                                fontSize = 16.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        color = TextLight.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReminderDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            tint = TextLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat",
                                fontSize = 12.sp,
                                color = TextLight.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (reminderDays == -1) "10 menit sebelumnya" else "$reminderDays hari sebelumnya",
                                fontSize = 16.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        color = TextLight.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val dayOfMonth = remember(selectedDateMillis) {
                        Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                        }.get(Calendar.DAY_OF_MONTH)
                    }

                    Text(
                        text = "Atur setiap tanggal $dayOfMonth",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLight,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )

                    RepeatTypeSelector(
                        selectedType = repeatType,
                        onTypeSelected = { repeatType = it },
                        dayOfMonth = dayOfMonth
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundColor)
                            .border(
                                width = 2.dp,
                                color = if (selectedImageUri != null) PrimaryRed else SecondaryBrown.copy(
                                    alpha = 0.3f
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Ganti Foto",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Upload",
                                    tint = SecondaryBrown,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pilih foto dari galeri",
                                    fontSize = 14.sp,
                                    color = SecondaryBrown,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "(Opsional)",
                                    fontSize = 12.sp,
                                    color = SecondaryBrown.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            
            if (isEditMode && existingBill?.status == "belum") {
                Button(
                    onClick = {
                        viewModel.tandaiSelesai(
                            context = context, 
                            id = existingBill.id,
                            buktiFotoPath = selectedImageUri?.toString()
                        )
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryBrown,
                        contentColor = TextLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tandai Selesai",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder,
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Batal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        val savedPath = selectedImageUri?.let { uri ->
                            
                            if (uri.toString().startsWith("content://") || uri.toString().startsWith("file://")) {
                                FileUtils.saveImageToInternalStorage(context, uri)
                            } else {
                                uri.toString()
                            }
                        }

                        if (isEditMode && billId != null) {
                            viewModel.updateTagihan(
                                context = context,
                                id = billId,
                                title = title,
                                dueDateMillis = selectedDateMillis,
                                description = description,
                                reminderDays = reminderDays,
                                repeatType = repeatType,
                                buktiFotoPath = savedPath
                            )
                        } else {
                            viewModel.tambahTagihan(
                                context = context,
                                title = title,
                                dueDateMillis = selectedDateMillis,
                                description = description,
                                reminderDays = reminderDays,
                                repeatType = repeatType,
                                buktiFotoPath = savedPath
                            )
                        }
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryBrown,
                        contentColor = TextLight
                    ),
                    enabled = title.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) "Update" else "Simpan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRed)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRed)
                ) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.padding(16.dp),
                colors = DatePickerDefaults.colors(
                    containerColor = BackgroundColor,
                    titleContentColor = PrimaryRed,
                    headlineContentColor = PrimaryRed,
                    weekdayContentColor = SecondaryBrown,
                    subheadContentColor = SecondaryBrown,
                    yearContentColor = TextDark,
                    currentYearContentColor = PrimaryRed,
                    selectedYearContentColor = TextLight,
                    selectedYearContainerColor = PrimaryRed,
                    dayContentColor = TextDark,
                    selectedDayContentColor = TextLight,
                    selectedDayContainerColor = PrimaryRed,
                    todayContentColor = PrimaryRed,
                    todayDateBorderColor = PrimaryRed
                )
            )
        }
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = {
                Text(
                    "Pilih Pengingat",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            },
            text = {
                Column {
                    listOf(-1, 1, 2, 3, 7).forEach { days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    reminderDays = days
                                    showReminderDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reminderDays == days,
                                onClick = {
                                    reminderDays = days
                                    showReminderDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryRed
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (days == -1) "10 menit sebelumnya" else "$days hari sebelumnya",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showReminderDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRed)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = BackgroundColor
        )
    }
}

@Composable
fun RepeatTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    dayOfMonth: Int
) {
    val monthName = SimpleDateFormat("MMMM", Locale("id", "ID")).format(Date())
    val options = listOf(
        "Atur setiap tanggal $dayOfMonth",
        "Atur setiap tanggal $dayOfMonth $monthName",
        "Sekali saja"
    )

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PrimaryRed)
            .border(1.dp, TextLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedType,
                color = TextLight,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown",
                tint = TextLight,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (option == selectedType) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Spacer(modifier = Modifier.width(28.dp))
                            }
                            Text(
                                text = option,
                                color = if (option == selectedType) PrimaryRed else TextDark,
                                fontWeight = if (option == selectedType) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    },
                    onClick = {
                        onTypeSelected(option)
                        expanded = false
                    }
                )
                if (option != options.last()) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

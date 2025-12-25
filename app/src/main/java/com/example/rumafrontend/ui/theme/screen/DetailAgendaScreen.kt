package com.example.rumafrontend.ui.theme.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.rumafrontend.ViewModel.AgendaViewModel.AgendaViewModel
import com.example.rumafrontend.ui.theme.reminderschedule.cancelReminder
import com.example.rumafrontend.ui.theme.reminderschedule.scheduleReminder
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

fun convertTo12HourFormat(time24: String): String {
    return try {
        if (time24.contains("AM", ignoreCase = true) || time24.contains("PM", ignoreCase = true)) {
            return time24
        }

        if (time24.contains("-")) {
            val times = time24.split("-")
            val startTime = convertSingleTimeTo12Hour(times[0].trim())
            val endTime = convertSingleTimeTo12Hour(times[1].trim())
            return "$startTime - $endTime"
        } else {
            return convertSingleTimeTo12Hour(time24.trim())
        }
    } catch (e: Exception) {
        time24
    }
}

fun convertSingleTimeTo12Hour(time: String): String {
    return try {
        val parts = time.split(":").map { it.replace(".", "").trim() }
        if (parts.size >= 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val period = if (hour >= 12) "PM" else "AM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }

            String.format("%02d:%02d %s", hour12, minute, period)
        } else {
            time
        }
    } catch (e: Exception) {
        time
    }
}

fun parsePengingat(pengingat: String): Pair<Int, String> {
    val parts = pengingat.split(" ")
    return if (parts.size == 2) {
        Pair(parts[0].toIntOrNull() ?: 15, parts[1])
    } else {
        Pair(15, "minutes")
    }
}

fun calculateReminderTime(date: String, time: String, reminderStr: String): Long {
    try {
        val dateTimeString = "$date $time"
        val format = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
        val dateObj = format.parse(dateTimeString) ?: return 0

        val calendar = Calendar.getInstance()
        calendar.time = dateObj

        val (value, unit) = parsePengingat(reminderStr)

        when (unit) {
            "minutes", "minute" -> calendar.add(Calendar.MINUTE, -value)
            "hours", "hour" -> calendar.add(Calendar.HOUR_OF_DAY, -value)
            "days", "day" -> calendar.add(Calendar.DAY_OF_YEAR, -value)
        }

        return calendar.timeInMillis
    } catch (e: Exception) {
        e.printStackTrace()
        return 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel,
    agendaId: Long 
) {
    
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    val agendaList by viewModel.agendas.collectAsState()
    val agenda = remember(agendaList, agendaId) {
        agendaList.find { it.id == agendaId }
    }
    LaunchedEffect(agendaId) {
        
        if (agendaList.isEmpty()) {
            viewModel.syncFromServer()
        }
    }

    if (agenda == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF868859)),
            contentAlignment = Alignment.Center
        ) {
            Text("Agenda tidak ditemukan", color = Color.White)
        }
        return
    }

    var isEditMode by remember { mutableStateOf(false) }
    var isDeleteActive by remember { mutableStateOf(false) }

    
    var editKategori by remember { mutableStateOf(agenda.kategori) }
    var editJudul by remember { mutableStateOf(agenda.judul) }
    var editDeskripsi by remember { mutableStateOf(agenda.deskripsi) }
    var editTanggal by remember { mutableStateOf(agenda.date) }
    var editWaktu by remember { mutableStateOf(agenda.time) }
    var editLokasi by remember { mutableStateOf(agenda.location) }
    var editPengingat by remember { mutableStateOf("") }

    LaunchedEffect(agenda.id) {
        editPengingat = agenda.reminder
    }
    
    val displayWaktu = remember(agenda.time, editWaktu, isEditMode) {
        if (isEditMode) {
            convertTo12HourFormat(editWaktu)
        } else {
            convertTo12HourFormat(agenda.time)
        }
    }

    var showSuccessMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "Agenda berhasil terupdate",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1000)
            navController.popBackStack()
        }
    }

    val kategoriList = listOf(
        "Momen Spesial",
        "Liburan dan Rekreasi",
        "Kumpul Keluarga",
        "Kegiatan Keagamaan",
        "Acara Rumah Tangga",
        "Hiburan dan Santai",
        "Sosial dan Kepedulian"
    )
    var expandedKategori by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var showPengingatDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    var selectedPengingatValue by remember { mutableStateOf(15) }
    var selectedPengingatUnit by remember { mutableStateOf("minutes") }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF712626))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Calendar Icon",
                                tint = Color(0xFFF2ECDC),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agenda",
                                color = Color(0xFFF2ECDC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF712626)
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF712626),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isEditMode = !isEditMode
                            isDeleteActive = false
                            if (!isEditMode) {
                                editKategori = agenda.kategori
                                editJudul = agenda.judul
                                editDeskripsi = agenda.deskripsi
                                editTanggal = agenda.date
                                editWaktu = agenda.time
                                editLokasi = agenda.location
                                editPengingat = agenda.reminder
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isEditMode) Color.White else Color.Transparent,
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (isEditMode) Color(0xFF712626) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isEditMode = false
                            isDeleteActive = true
                            showDeleteDialog = true
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isDeleteActive) Color.White else Color.Transparent,
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = if (isDeleteActive) Color(0xFF712626) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF868859))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                
                if (isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedKategori = true }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = editKategori, color = Color.White, fontSize = 16.sp)
                        }
                        Icon(
                            imageVector = if (expandedKategori) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = expandedKategori,
                        onDismissRequest = { expandedKategori = false },
                        modifier = Modifier.background(Color(0xFFF5F5DC)).width(250.dp)
                    ) {
                        kategoriList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item, color = Color.Black, fontSize = 14.sp) },
                                onClick = { editKategori = item; expandedKategori = false }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = agenda.kategori, color = Color.White, fontSize = 16.sp)
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    if (isEditMode) {
                        BasicTextField(
                            value = editJudul,
                            onValueChange = { editJudul = it },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp)
                        )
                    } else {
                        Text(
                            text = agenda.judul,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                }

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    if (isEditMode) {
                        BasicTextField(
                            value = editDeskripsi,
                            onValueChange = { editDeskripsi = it },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp)
                        )
                    } else {
                        Text(
                            text = agenda.deskripsi,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                }

                
                if (isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = editTanggal, color = Color.White, fontSize = 16.sp)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = displayWaktu, color = Color.White, fontSize = 16.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = agenda.date, color = Color.White, fontSize = 16.sp)
                            Text(
                                text = displayWaktu,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isEditMode) { showMapDialog = true }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) editLokasi else agenda.location,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isEditMode) { showPengingatDialog = true }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) editPengingat else agenda.reminder,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                Spacer(modifier = Modifier.weight(1f))

                
                if (isEditMode) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                isEditMode = false
                                editKategori = agenda.kategori
                                editJudul = agenda.judul
                                editDeskripsi = agenda.deskripsi
                                editTanggal = agenda.date
                                editWaktu = agenda.time
                                editLokasi = agenda.location
                                editPengingat = agenda.reminder
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                            modifier = Modifier.weight(1f).padding(end = 8.dp).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal", color = Color.White, fontSize = 16.sp)
                        }

                        Button(
                            onClick = {
                                if (editJudul.isNotEmpty() && editKategori.isNotEmpty() &&
                                    editDeskripsi.isNotEmpty() && editTanggal.isNotEmpty() &&
                                    editWaktu.isNotEmpty() && editLokasi.isNotEmpty()
                                ) {
                                    showSaveConfirmDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                            modifier = Modifier.weight(1f).padding(start = 8.dp).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Simpan", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }

            
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = millis
                                editTanggal =
                                    "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                                        calendar.get(Calendar.YEAR)
                                    }"
                            }
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDatePicker = false }) { Text("Batal") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                            val hour = timePickerState.hour
                            val minute = timePickerState.minute
                            val period = if (hour >= 12) "PM" else "AM"
                            val hour12 = when {
                                hour == 0 -> 12
                                hour > 12 -> hour - 12
                                else -> hour
                            }
                            editWaktu = String.format(
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                        }) { Text("Batal") }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }
            var selectedGeoPoint by remember {
                mutableStateOf(GeoPoint(-6.200000, 106.816666)) 
            }

            var hasLocationPermission by remember {
                mutableStateOf(true)
            }

            
            if (showMapDialog) {
                OpenStreetMapDialog(
                    initialLocationName = editLokasi,
                    initialGeoPoint = selectedGeoPoint,
                    hasLocationPermission = hasLocationPermission,
                    onDismiss = { showMapDialog = false },
                    onLocationSelected = { name, geoPoint ->
                        editLokasi = name
                        selectedGeoPoint = geoPoint
                        showMapDialog = false
                    }
                )
            }

                
                if (showPengingatDialog) {

                    LaunchedEffect(Unit) {
                        val (value, unit) = parsePengingat(editPengingat)
                        selectedPengingatValue = value
                        selectedPengingatUnit = unit
                    }

                    Dialog(onDismissRequest = { showPengingatDialog = false }) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Atur Pengingat",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        IconButton(onClick = { if (selectedPengingatValue < 60) selectedPengingatValue++ }) {
                                            Text("▲", fontSize = 20.sp, color = Color.Gray)
                                        }
                                        Text(
                                            "$selectedPengingatValue",
                                            color = Color.Black,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { if (selectedPengingatValue > 1) selectedPengingatValue-- }) {
                                            Text("▼", fontSize = 20.sp, color = Color.Gray)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "minutes",
                                            color = if (selectedPengingatUnit == "minutes") Color.Black else Color.LightGray,
                                            fontSize = if (selectedPengingatUnit == "minutes") 20.sp else 16.sp,
                                            fontWeight = if (selectedPengingatUnit == "minutes") FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.clickable {
                                                selectedPengingatUnit = "minutes"
                                            }.padding(vertical = 8.dp)
                                        )
                                        Text(
                                            "hours",
                                            color = if (selectedPengingatUnit == "hours") Color.Black else Color.LightGray,
                                            fontSize = if (selectedPengingatUnit == "hours") 20.sp else 16.sp,
                                            fontWeight = if (selectedPengingatUnit == "hours") FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.clickable {
                                                selectedPengingatUnit = "hours"
                                            }.padding(vertical = 8.dp)
                                        )
                                        Text(
                                            "days",
                                            color = if (selectedPengingatUnit == "days") Color.Black else Color.LightGray,
                                            fontSize = if (selectedPengingatUnit == "days") 20.sp else 16.sp,
                                            fontWeight = if (selectedPengingatUnit == "days") FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.clickable {
                                                selectedPengingatUnit = "days"
                                            }.padding(vertical = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Alert type",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(selected = true, onClick = { })
                                    Text("Notification", fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        editPengingat =
                                            "$selectedPengingatValue $selectedPengingatUnit"; showPengingatDialog =
                                        false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF3D3D2E
                                        )
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Simpan", color = Color.White) }
                            }
                        }
                    }
                }

                
                if (showSaveConfirmDialog) {
                    Dialog(onDismissRequest = { showSaveConfirmDialog = false }) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE8DCC4)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Simpan perubahan atau hapus?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5A5A4A),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    onClick = { showSaveConfirmDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF292f17
                                        )
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Batal",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        showSaveConfirmDialog = false
                                        isEditMode = false
                                        navController.popBackStack()

                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF292f17
                                        )
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Hapus",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                val context = LocalContext.current

                                        Button(
                                            onClick = {
                                                try {
                                                    
                                                    val updatedAgenda = agenda.copy(
                                                        judul = editJudul,
                                                        kategori = editKategori,
                                                        deskripsi = editDeskripsi,
                                                        date = editTanggal,
                                                        time = editWaktu,
                                                        location = editLokasi,
                                                        reminder = editPengingat
                                                    )
                                                    viewModel.updateAgenda(updatedAgenda)

                                                    
                                                    try {
                                                        val newTriggerTime = calculateReminderTime(
                                                            date = editTanggal,
                                                            time = editWaktu,
                                                            reminderStr = editPengingat
                                                        )

                                                        
                                                        cancelReminder(context, agenda.id)

                                                        
                                                        if (newTriggerTime > System.currentTimeMillis()) {
                                                            scheduleReminder(
                                                                context = context,
                                                                agendaId = agenda.id,
                                                                triggerTime = newTriggerTime,
                                                                title = editJudul,
                                                                message = "Agenda akan dimulai"
                                                            )
                                                        }
                                                    } catch (e: Exception) {
                                                        
                                                        Toast.makeText(
                                                            context,
                                                            "Pengingat tidak dapat diatur. Silakan izinkan alarm exact di pengaturan.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }

                                                    showSaveConfirmDialog = false
                                                    isEditMode = false
                                                    showSuccessMessage = true

                                                } catch (e: Exception) {
                                                    
                                                    Toast.makeText(
                                                        context,
                                                        "Gagal menyimpan: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF292f17)),
                                            modifier = Modifier.fillMaxWidth().height(56.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Simpan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                        }
                            }
                        }
                    }
                }

                
                if (showDeleteDialog) {
                    Dialog(onDismissRequest = {
                        showDeleteDialog = false
                        isDeleteActive = false
                    }) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(horizontal = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8DCC4)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Hapus acara ini?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF5A5A4A)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    horizontalArrangement = spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            showDeleteDialog = false
                                            isDeleteActive = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFF3D3D2E
                                            )
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Batal", color = Color.White, fontSize = 16.sp)
                                    }

                                    val context = LocalContext.current  

                                    Button(
                                        onClick = {
                                            showDeleteDialog = false
                                            isDeleteActive = false

                                            try {
                                                
                                                cancelReminder(context, agenda.id)

                                                
                                                viewModel.deleteAgenda(agenda)

                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Gagal menghapus agenda: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Hapus", color = Color.White, fontSize = 16.sp)
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

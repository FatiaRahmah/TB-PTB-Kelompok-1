package com.example.rumafrontend.ui.theme.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.rumafrontend.ViewModel.AgendaViewModel.AgendaViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import android.location.LocationManager
import android.provider.Settings
import android.content.Intent
import android.location.Address
import com.example.rumafrontend.ui.theme.reminderschedule.scheduleReminder
import android.os.Build
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgendaScreen(navController: NavController, viewModel: AgendaViewModel) {
    val context = LocalContext.current

    fun isLocationEnabled(): Boolean {
        val locationManager =
            context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    var kategori by remember { mutableStateOf("") }
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var pengingat by remember { mutableStateOf("") }

    
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedLocationName by remember { mutableStateOf("") }

    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
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
    var showEnableLocationDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var showPengingatDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    var showSuccessMessage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    
    var selectedPengingatValue by remember { mutableStateOf(15) }
    var selectedPengingatUnit by remember { mutableStateOf("minutes") }

    
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "Agenda berhasil ditambahkan",
                duration = SnackbarDuration.Short
            )
            delay(1000)
            navController.popBackStack()
        }
    }

    Scaffold(
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF712626)
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
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
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (kategori.isEmpty()) "Kategori" else kategori,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    Icon(
                        imageVector = if (expandedKategori) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                DropdownMenu(
                    expanded = expandedKategori,
                    onDismissRequest = { expandedKategori = false },
                    modifier = Modifier
                        .background(Color(0xFFF5F5DC))
                        .width(250.dp)
                ) {
                    kategoriList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item, color = Color.Black, fontSize = 14.sp) },
                            onClick = {
                                kategori = item
                                expandedKategori = false
                            }
                        )
                    }
                }

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    BasicTextField(
                        value = judul,
                        onValueChange = { judul = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        decorationBox = { innerTextField ->
                            if (judul.isEmpty()) {
                                Text(text = "Judul", color = Color.White, fontSize = 16.sp)
                            }
                            innerTextField()
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                }

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    BasicTextField(
                        value = deskripsi,
                        onValueChange = { deskripsi = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        decorationBox = { innerTextField ->
                            if (deskripsi.isEmpty()) {
                                Text(text = "Deskripsi", color = Color.White, fontSize = 16.sp)
                            }
                            innerTextField()
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                }

                
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
                    Text(
                        text = if (tanggal.isEmpty()) "Tanggal" else tanggal,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Time",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (waktu.isEmpty()) "Waktu" else waktu,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when {
                                    !hasLocationPermission -> {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                    !isLocationEnabled() -> {
                                        showEnableLocationDialog = true
                                    }
                                    else -> {
                                        showMapDialog = true
                                    }
                                }
                            }

                            .padding(vertical = 12.dp),
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
                            text = if (lokasi.isEmpty()) "Lokasi" else lokasi,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                }

                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPengingatDialog = true }
                            .padding(vertical = 12.dp),
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
                            text = if (pengingat.isEmpty()) "Pengingat (Opsional)" else pengingat,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                }

                Spacer(modifier = Modifier.weight(1f))

                
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", color = Color.White, fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            if (judul.isNotEmpty() && kategori.isNotEmpty() &&
                                deskripsi.isNotEmpty() && tanggal.isNotEmpty() &&
                                waktu.isNotEmpty() && lokasi.isNotEmpty()) {

                                

                                fun calculateReminderTime(
                                    tanggal: String,
                                    waktu: String,
                                    value: Int,
                                    unit: String
                                ): Long {
                                    val partsDate = tanggal.split("/")
                                    val partsTime = waktu.split(":")

                                    val calendar = Calendar.getInstance().apply {
                                        set(
                                            partsDate[2].toInt(),
                                            partsDate[1].toInt() - 1,
                                            partsDate[0].toInt(),
                                            partsTime[0].toInt(),
                                            partsTime[1].toInt()
                                        )
                                    }

                                    when (unit) {
                                        "minutes" -> calendar.add(Calendar.MINUTE, -value)
                                        "hours" -> calendar.add(Calendar.HOUR, -value)
                                        "days" -> calendar.add(Calendar.DAY_OF_YEAR, -value)
                                    }

                                    return calendar.timeInMillis
                                }

                                viewModel.addAgenda(
                                    judul = judul,
                                    kategori = kategori,
                                    deskripsi = deskripsi,
                                    tanggal = tanggal,
                                    waktu = waktu,
                                    lokasi = lokasi,
                                    pengingat = pengingat.ifEmpty { "Tidak ada pengingat" }
                                )

                                if (pengingat.isNotEmpty()) {
                                    val reminderTime = calculateReminderTime(

                                        tanggal,
                                        waktu,
                                        selectedPengingatValue,
                                        selectedPengingatUnit
                                    )
                                    Toast.makeText(
                                        context,
                                        "Reminder time: ${Date(reminderTime)}\nNow: ${Date(System.currentTimeMillis())}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    scheduleReminder(
                                       context = context,
                                       agendaId = System.currentTimeMillis(), 
                                       triggerTime = reminderTime,
                                       title = judul,
                                       message = "Agenda akan dimulai di $lokasi"
                                    )
                                    

                                }

                                showSuccessMessage = true
                            } else {
                                showErrorDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", color = Color.White, fontSize = 16.sp)
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
                                tanggal = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
                            }
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Batal")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                            waktu = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Batal")
                        }
                    },
                    text = {
                        TimePicker(state = timePickerState)
                    }
                )
            }

            
            if (showMapDialog) {
                OpenStreetMapDialog(
                    initialGeoPoint = selectedLocation,
                    initialLocationName = selectedLocationName,
                    hasLocationPermission = hasLocationPermission,
                    onDismiss = { showMapDialog = false },
                    onLocationSelected = { name, geoPoint ->
                        lokasi = name
                        selectedLocationName = name
                        selectedLocation = geoPoint   
                        showMapDialog = false
                    }
                )
            }

            
            if (showPengingatDialog) {
                PengingatDialog(
                    selectedValue = selectedPengingatValue,
                    selectedUnit = selectedPengingatUnit,
                    onValueChange = { selectedPengingatValue = it },
                    onUnitChange = { selectedPengingatUnit = it },
                    onDismiss = { showPengingatDialog = false },
                    onSave = {
                        pengingat = "$selectedPengingatValue $selectedPengingatUnit"
                        showPengingatDialog = false
                    }
                )
            }

            
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showErrorDialog = false }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Perhatian") },
                    text = { Text("Lengkapi data dahulu") }
                )
            }
            if (showEnableLocationDialog) {
                AlertDialog(
                    onDismissRequest = { showEnableLocationDialog = false },
                    title = { Text("Aktifkan Lokasi") },
                    text = { Text("GPS belum aktif. Aktifkan lokasi untuk memilih tempat.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showEnableLocationDialog = false
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        }) {
                            Text("Aktifkan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEnableLocationDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun OpenStreetMapDialog(
    initialGeoPoint: GeoPoint?,          
    initialLocationName: String?,         
    hasLocationPermission: Boolean,
    onDismiss: () -> Unit,
    onLocationSelected: (String, GeoPoint) -> Unit
)
{
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var markerPosition by remember { mutableStateOf<GeoPoint?>(initialGeoPoint) }
    var locationName by remember { mutableStateOf(initialLocationName ?: "") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var currentUserLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var distanceText by remember { mutableStateOf("") }

    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    
    val defaultLocation = GeoPoint(1.470370, 101.446280)

    
    fun calculateDistance(from: GeoPoint, to: GeoPoint): String {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        val distanceInMeters = results[0]
        return if (distanceInMeters < 1000) {
            "${distanceInMeters.toInt()} meter"
        } else {
            String.format("%.2f km", distanceInMeters / 1000)
        }
    }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }

    
    fun searchLocation(query: String) {
        if (query.length < 3) {
            searchResults = emptyList()
            return
        }

        scope.launch {
            try {
                isSearching = true
                val addresses = geocoder.getFromLocationName(query, 5)
                searchResults = addresses ?: emptyList()
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        }
    }

    
    fun getLocationName(geoPoint: GeoPoint) {
        scope.launch {
            try {
                val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    locationName = address.getAddressLine(0) ?:
                            address.featureName ?:
                            "${address.locality ?: ""} ${address.subAdminArea ?: ""}".trim()
                }
            } catch (e: Exception) {
                locationName = "Lat: ${String.format("%.4f", geoPoint.latitude)}, " +
                        "Lng: ${String.format("%.4f", geoPoint.longitude)}"
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            myLocationOverlay?.disableMyLocation()
            mapView?.onDetach()
        }
    }

    var selectedMarker by remember { mutableStateOf<Marker?>(null) }

    fun showMarker(map: MapView, point: GeoPoint) {
        
        selectedMarker?.let {
            map.overlays.remove(it)
        }

        val marker = Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Lokasi Tujuan"
        }

        map.overlays.add(marker)
        selectedMarker = marker
        map.invalidate()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(650.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column {
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3c1b0f))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            searchLocation(it) 
                        },
                        placeholder = { Text("Cari lokasi (contoh: Dumai, Riau)") },
                        leadingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    searchResults = emptyList()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3D3D2E),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }

                if (searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .heightIn(max = 220.dp) 
                            .zIndex(2f),           
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            searchResults.forEach { address ->
                                val placeName =
                                    address.getAddressLine(0)
                                        ?: "${address.locality ?: ""} ${address.subAdminArea ?: ""}".trim()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val geoPoint = GeoPoint(address.latitude, address.longitude)

                                            mapView?.let { map ->
                                                map.controller.animateTo(geoPoint)
                                                map.controller.setZoom(16.0)
                                                showMarker(map, geoPoint) 
                                            }

                                            markerPosition = geoPoint
                                            locationName = placeName
                                            searchResults = emptyList()
                                            searchQuery = placeName

                                            currentUserLocation?.let {
                                                distanceText = calculateDistance(it, geoPoint)
                                            }
                                        }
                                        .padding(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(placeName, fontSize = 14.sp)
                                }

                                HorizontalDivider()
                            }
                        }
                    }
                }

                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                mapView = this
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(15.0)
                                controller.setCenter(defaultLocation)

                                
                                if (hasLocationPermission) {
                                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                                    locationOverlay.enableMyLocation()
                                    locationOverlay.enableFollowLocation()
                                    overlays.add(locationOverlay)
                                    myLocationOverlay = locationOverlay

                                    
                                    try {
                                        if (ContextCompat.checkSelfPermission(
                                                ctx,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            )
                                            == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                                location?.let {
                                                    val userLocation =
                                                        GeoPoint(it.latitude, it.longitude)
                                                    currentUserLocation = userLocation
                                                    controller.animateTo(userLocation)
                                                }
                                            }
                                        }

                                        } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                
                                overlays.add(
                                    object : org.osmdroid.views.overlay.Overlay() {
                                        override fun onSingleTapConfirmed(
                                            e: android.view.MotionEvent?,
                                            mapView: MapView?
                                        ): Boolean {
                                            e?.let { event ->
                                                mapView?.let { map ->
                                                    val projection = map.projection
                                                    val geoPoint = projection.fromPixels(
                                                        event.x.toInt(),
                                                        event.y.toInt()
                                                    ) as GeoPoint

                                                    markerPosition = geoPoint
                                                    getLocationName(geoPoint)

                                                    mapView?.let {
                                                        showMarker(it, geoPoint) 
                                                    }

                                                    currentUserLocation?.let { userLoc ->
                                                        distanceText = calculateDistance(userLoc, geoPoint)
                                                    }

                                                }
                                            }
                                            return true
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    
                    if (markerPosition != null && locationName.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = locationName,
                                    fontSize = 14.sp,
                                    color = Color.Black,

                                )
                            }
                            if (distanceText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "± $distanceText dari lokasi Anda",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    
                    if (hasLocationPermission) {
                        FloatingActionButton(
                            onClick = {
                                myLocationOverlay?.let { overlay ->
                                    overlay.myLocation?.let { location ->
                                        mapView?.controller?.animateTo(location)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            containerColor = Color.White,
                            contentColor = Color(0xFF3D3D2E)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Lokasi Saya")
                        }
                    }
                }

                
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", color = Color.White)
                    }

                    Button(
                        onClick = {
                            markerPosition?.let {
                                onLocationSelected(locationName, it)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = markerPosition != null && locationName.isNotEmpty()
                    ) {
                        Text("Simpan", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PengingatDialog(
    selectedValue: Int,
    selectedUnit: String,
    onValueChange: (Int) -> Unit,
    onUnitChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
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
                        IconButton(onClick = {
                            if (selectedValue < 60) onValueChange(selectedValue + 1)
                        }) {
                            Text("▲", fontSize = 20.sp, color = Color.Gray)
                        }
                        Text("$selectedValue", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            if (selectedValue > 1) onValueChange(selectedValue - 1)
                        }) {
                            Text("▼", fontSize = 20.sp, color = Color.Gray)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        listOf("minutes", "hours", "days").forEach { unit ->
                            Text(
                                text = unit,
                                color = if (selectedUnit == unit) Color.Black else Color.LightGray,
                                fontSize = if (selectedUnit == unit) 20.sp else 16.sp,
                                fontWeight = if (selectedUnit == unit) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clickable { onUnitChange(unit) }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Alert type", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(selected = true, onClick = {})
                    Text("Notification", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D3D2E)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan", color = Color.White)
                }
            }
        }
    }
}

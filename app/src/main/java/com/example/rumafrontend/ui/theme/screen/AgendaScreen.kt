package com.example.rumafrontend.ui.theme.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.rumafrontend.ViewModel.AgendaViewModel.AgendaViewModel
import com.example.rumafrontend.data.entity.Agenda
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel,
) {
    val agendaList by viewModel.agendas.collectAsState()

    val categories = listOf(
        "All",
        "Momen Spesial",
        "Liburan dan Rekreasi",
        "Kumpul Keluarga",
        "Kegiatan Keagamaan",
        "Acara Rumah Tangga",
        "Hiburan dan Santai",
        "Sosial dan Kepedulian"
    )

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_agenda") },
                containerColor = Color(0xFF712626),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Agenda", tint = Color(0xFFF2ECDC))
            }
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFF712626)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {

                        navController.navigate("favorit")

                    }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorit", tint = Color(0xFF292F17))
                    }
                    IconButton(onClick = {
                        navController.navigate("beranda") {
                            
                            popUpTo("beranda") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Beranda", tint = Color(0xFF292F17))
                    }
                    IconButton(onClick = {
                        navController.navigate("profil")

                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color(0xFF292F17))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2ECDC))
        ) {
            
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

            Spacer(modifier = Modifier.height(12.dp))

            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF868859),
                        unfocusedContainerColor = Color(0xFF868859),
                        focusedBorderColor = Color(0xFFF2ECDC),
                        unfocusedBorderColor = Color(0xFFF2ECDC),
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { navController.navigate("calendar_agenda") },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF712626), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Kalender", tint = Color(0xFFF2ECDC))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            
            Text(
                "Kategori",
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2E2E1F)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    Button(
                        onClick = {
                            selectedCategory = category
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == category)
                                Color(0xFF5C1A1A)
                            else Color(0xFF7E2625)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(category, color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            
            val filteredAgenda = remember(agendaList, selectedCategory, searchQuery) {
                agendaList.filter { agenda ->
                    val agendaKategori = agenda.kategori.trim()
                    val selectedKat = selectedCategory.trim()

                    val matchesCategory = if (selectedKat == "All") {
                        true
                    } else {
                        agendaKategori.equals(selectedKat, ignoreCase = true) ||
                                agendaKategori.replace("&", "dan").trim().equals(selectedKat.replace("&", "dan").trim(), ignoreCase = true)
                    }

                    val matchesSearch = searchQuery.isEmpty() ||
                            agenda.judul.contains(searchQuery, ignoreCase = true) ||
                            agenda.deskripsi.contains(searchQuery, ignoreCase = true)

                    matchesCategory && matchesSearch
                }
            }

            
            val currentDateTime = remember { Calendar.getInstance() }

            val (upcomingAgendas, pastAgendas) = remember(filteredAgenda) {
                filteredAgenda.partition { agenda ->
                    val agendaDateTime = parseDateTimeComplete(agenda.date, agenda.time)
                    agendaDateTime == null || !agendaDateTime.before(currentDateTime)
                }
            }

            
            val sortedUpcomingAgendas = remember(upcomingAgendas) {
                upcomingAgendas.sortedWith(compareBy(
                    { parseDateTimeComplete(it.date, it.time) ?: Calendar.getInstance().apply {
                        add(Calendar.YEAR, 100)
                    } }
                ))
            }

            
            val sortedPastAgendas = remember(pastAgendas) {
                pastAgendas.sortedWith(compareByDescending {
                    parseDateTimeComplete(it.date, it.time) ?: Calendar.getInstance().apply {
                        add(Calendar.YEAR, -100)
                    }
                })
            }

            
            if (filteredAgenda.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedCategory != "All")
                                "Tidak ada agenda di kategori ini"
                            else "Agenda kosong",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedCategory != "All")
                                "Coba pilih kategori lain"
                            else "Ayo tambahkan agenda!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    
                    if (sortedUpcomingAgendas.isNotEmpty()) {
                        item {
                            Text(
                                text = "Agenda Mendatang",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF712626),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        val groupedUpcoming = sortedUpcomingAgendas.groupBy { it.date.ifEmpty { "Tanggal belum ditentukan" } }

                        groupedUpcoming.forEach { (tanggal, agendas) ->
                            item {
                                Text(
                                    text = formatTanggal(tanggal),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E2E1F),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                )
                            }

                            items(
                                items = agendas,
                                key = { it.id }
                            ) { agenda ->
                                AgendaCard(
                                    agenda = agenda,
                                    isPast = false,
                                    onClick = {
                                        navController.navigate("detail_agenda/${agenda.id}")
                                    }
                                )
                            }
                        }
                    }

                    
                    if (sortedPastAgendas.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    thickness = 1.dp,
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = "  Agenda yang Sudah Selesai  ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    thickness = 1.dp,
                                    color = Color.Gray.copy(alpha = 0.3f)
                                )
                            }
                        }

                        val groupedPast = sortedPastAgendas.groupBy { it.date.ifEmpty { "Tanggal belum ditentukan" } }

                        groupedPast.forEach { (tanggal, agendas) ->
                            item {
                                Text(
                                    text = formatTanggal(tanggal),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                )
                            }

                            items(
                                items = agendas,
                                key = { it.id }
                            ) { agenda ->
                                AgendaCard(
                                    agenda = agenda,
                                    isPast = true,
                                    onClick = {
                                        navController.navigate("detail_agenda/${agenda.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseDateTimeComplete(dateString: String, timeString: String): Calendar? {
    if (dateString.isEmpty() || timeString.isEmpty()) return null

    return try {
        
        val dateParts = dateString.split("/")
        if (dateParts.size != 3) return null

        val day = dateParts[0].toIntOrNull() ?: return null
        val month = (dateParts[1].toIntOrNull() ?: return null) - 1
        val year = dateParts[2].toIntOrNull() ?: return null

        
        val timeParts = timeString.split(":")
        if (timeParts.size != 2) return null

        val hour = timeParts[0].toIntOrNull() ?: return null
        val minute = timeParts[1].toIntOrNull() ?: return null

        
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    } catch (e: Exception) {
        Log.e("AGENDA_PARSE", "Error parsing datetime: $dateString $timeString", e)
        null
    }
}

fun parseDate(dateString: String): Calendar? {
    if (dateString.isEmpty()) return null

    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val day = parts[0].toIntOrNull() ?: return null
            val month = (parts[1].toIntOrNull() ?: return null) - 1
            val year = parts[2].toIntOrNull() ?: return null

            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } else null
    } catch (e: Exception) {
        Log.e("AGENDA_PARSE", "Error parsing date: $dateString", e)
        null
    }
}

fun formatTanggal(tanggal: String): String {
    if (tanggal == "Tanggal belum ditentukan") return tanggal

    return try {
        val parts = tanggal.split("/")
        if (parts.size == 3) {
            val day = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            val year = parts[2]

            val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agus", "Sep", "Okt", "Nov", "Des")

            "$day ${monthNames[month]} $year"
        } else {
            tanggal
        }
    } catch (e: Exception) {
        tanggal
    }
}

@Composable
fun AgendaCard(
    agenda: Agenda,
    isPast: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast)
                Color(0xFF4CAF50).copy(alpha = 0.6f)
            else
                Color(0xFF868859)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = agenda.kategori,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    if (isPast) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selesai",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agenda.judul,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                if (agenda.deskripsi.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = agenda.deskripsi,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        maxLines = 2
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = agenda.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF2ECDC),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Lihat Detail",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

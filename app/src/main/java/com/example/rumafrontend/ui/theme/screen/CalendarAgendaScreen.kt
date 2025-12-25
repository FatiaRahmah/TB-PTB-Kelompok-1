package com.example.rumafrontend.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.rumafrontend.ViewModel.AgendaViewModel.AgendaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarAgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel
) {
    
    val agendaList by viewModel.agendas.collectAsState()

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

    
    val filteredAgendas = remember(selectedDate, agendaList) {
        if (selectedDate != null) {
            agendaList.filter { it.date == selectedDate }
        } else {
            emptyList()
        }
    }

    
    val agendaDates = remember(agendaList) {
        agendaList.map { it.date }.toSet()
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
                                text = "Calendar Agenda",
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
                            tint = Color(0xFFF2ECDC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF712626)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5DC))
        ) {
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthFormat.format(currentMonth.time),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )

                    Row {
                        IconButton(onClick = {
                            currentMonth = (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        }) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = Color(0xFF5A5A4A)
                            )
                        }

                        IconButton(onClick = {
                            currentMonth = (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        }) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = Color(0xFF5A5A4A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5A5A4A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                
                CalendarGrid(
                    currentMonth = currentMonth,
                    agendaDates = agendaDates,
                    selectedDate = selectedDate,
                    onDateClick = { date ->
                        selectedDate = if (selectedDate == date) null else date
                    }
                )
            }

            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF868859))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Agenda",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (filteredAgendas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedDate != null) "Tidak ada agenda pada tanggal ini" else "Pilih tanggal untuk melihat agenda",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredAgendas,
                            key = { it.id }
                        ) { agenda ->
                            AgendaCard(
                                kategori = agenda.kategori,
                                judul = agenda.judul,
                                date = agenda.date,   
                                waktu = agenda.time,  
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

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    agendaDates: Set<String>,
    selectedDate: String?,
    onDateClick: (String) -> Unit
) {
    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    
    val firstDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column {
        var dayCounter = 1

        
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 1..7) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek) {
                            
                        } else if (dayCounter <= daysInMonth) {
                            val dateString = "$dayCounter/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
                            val hasAgenda = agendaDates.contains(dateString)
                            val isSelected = selectedDate == dateString

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF8B4545)
                                            hasAgenda -> Color(0xFFEF5350)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateClick(dateString) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = if (hasAgenda || isSelected) Color.White else Color(0xFF5A5A4A),
                                    fontSize = 16.sp,
                                    fontWeight = if (hasAgenda || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaCard(
    kategori: String,
    judul: String,
    date: String,    
    waktu: String,   
    onClick: () -> Unit
) {
    
    val agendaDateTime = remember(date, waktu) {
        val parts = date.split("/")
        val timeParts = waktu.split(":")
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.YEAR, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
        }
    }

    val isPast = remember { agendaDateTime.timeInMillis < System.currentTimeMillis() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isPast) Color(0xFF4CAF50) else Color(0xFFF5F5DC) 
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kategori,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) Color.White else Color(0xFF5A5A4A)
                )
                Text(
                    text = judul,
                    fontSize = 14.sp,
                    color = if (isPast) Color.White else Color(0xFF7A7A6A),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = waktu,
                fontSize = 14.sp,
                color = if (isPast) Color.White else Color(0xFF5A5A4A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCalendarAgendaScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF712626)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Calendar Agenda Preview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF712626)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jalankan aplikasi untuk melihat kalender",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

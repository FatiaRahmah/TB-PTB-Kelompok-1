package com.example.rumafrontend.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.rumafrontend.data.model.Bill
import com.example.rumafrontend.ui.theme.BackgroundColor
import com.example.rumafrontend.ui.theme.PrimaryRed
import com.example.rumafrontend.ui.theme.SecondaryBrown
import com.example.rumafrontend.ui.theme.TextDark
import com.example.rumafrontend.ui.theme.TextLight
import com.example.rumafrontend.ui.theme.BottomBarColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: TagihanViewModel
) {
    val bills by viewModel.tagihanList.collectAsState()

    
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    
    val selectedDateBills = selectedDate?.let { date ->
        bills.filter { bill ->
            val billCalendar = Calendar.getInstance().apply {
                timeInMillis = bill.dueDateMillis
            }
            billCalendar.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    billCalendar.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    billCalendar.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    } ?: emptyList()

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = PrimaryRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kalender Tagihan",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundColor)
        ) {
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        
                        MonthYearSelector(
                            currentMonth = currentMonth,
                            currentYear = currentYear,
                            onPreviousMonth = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear--
                                } else {
                                    currentMonth--
                                }
                                selectedDate = null
                            },
                            onNextMonth = {
                                if (currentMonth == 11) {
                                    currentMonth = 0
                                    currentYear++
                                } else {
                                    currentMonth++
                                }
                                selectedDate = null
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        
                        DaysOfWeekHeader()

                        Spacer(modifier = Modifier.height(8.dp))

                        
                        CalendarGrid(
                            month = currentMonth,
                            year = currentYear,
                            bills = bills,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                selectedDate = if (selectedDate?.timeInMillis == date.timeInMillis) {
                                    null
                                } else {
                                    date
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            
            if (selectedDate != null) {
                if (selectedDateBills.isEmpty()) {
                    
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.EventBusy,
                                        contentDescription = null,
                                        tint = SecondaryBrown,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Tidak ada tagihan",
                                        fontSize = 16.sp,
                                        color = TextDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
                                            .format(selectedDate!!.time),
                                        fontSize = 14.sp,
                                        color = SecondaryBrown
                                    )
                                }
                            }
                        }
                    }
                } else {
                    
                    item {
                        Text(
                            text = "Tagihan pada ${SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")).format(selectedDate!!.time)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(selectedDateBills) { bill ->
                        BillCardItemSimple(
                            bill = bill,
                            onClick = {
                                navController.navigate("editTagihan/${bill.id}")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                
                item {
                    CalendarLegend()
                }
            }

            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
@Composable
fun MonthYearSelector(
    currentMonth: Int,
    currentYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous Month",
                tint = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "${monthNames[currentMonth]} $currentYear",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryRed
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next Month",
                tint = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    month: Int,
    year: Int,
    bills: List<Bill>,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    
    val days = mutableListOf<Int?>()

    
    repeat(firstDayOfMonth - 1) {
        days.add(null)
    }

    
    for (day in 1..daysInMonth) {
        days.add(day)
    }

    
    val today = Calendar.getInstance()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(days.size) { index ->
            val day = days[index]

            if (day != null) {
                val dateCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                
                val hasBills = bills.any { bill ->
                    val billCalendar = Calendar.getInstance().apply {
                        timeInMillis = bill.dueDateMillis
                    }
                    billCalendar.get(Calendar.YEAR) == year &&
                            billCalendar.get(Calendar.MONTH) == month &&
                            billCalendar.get(Calendar.DAY_OF_MONTH) == day
                }

                
                val isToday = today.get(Calendar.YEAR) == year &&
                        today.get(Calendar.MONTH) == month &&
                        today.get(Calendar.DAY_OF_MONTH) == day

                
                val isSelected = selectedDate?.let {
                    it.get(Calendar.YEAR) == year &&
                            it.get(Calendar.MONTH) == month &&
                            it.get(Calendar.DAY_OF_MONTH) == day
                } ?: false

                CalendarDay(
                    day = day,
                    hasBills = hasBills,
                    isToday = isToday,
                    isSelected = isSelected,
                    onClick = {
                        if (hasBills) {
                            onDateSelected(dateCalendar)
                        }
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    hasBills: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> PrimaryRed
        hasBills -> SecondaryBrown
        isToday -> Color.LightGray
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected || hasBills -> TextLight
        isToday -> TextDark
        else -> TextDark
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !hasBills && !isSelected) {
                    Modifier.border(2.dp, SecondaryBrown, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = hasBills) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            color = textColor,
            fontWeight = if (hasBills || isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BillCardItemSimple(
    bill: Bill,
    onClick: () -> Unit
) {
    val remainingDaysText = when (bill.status) {
        "lunas" -> "LUNAS"
        else -> {
            val diff = bill.dueDateMillis - Date().time
            val days = (diff / 86400000).toInt()
            if (days < 0) "Terlambat ${days.toLong() * -1} hari" else "${days + 1} hari lagi"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    text = bill.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = remainingDaysText,
                    fontSize = 12.sp,
                    color = TextLight.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detail",
                tint = TextLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CalendarLegend() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Keterangan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LegendItem(
                color = SecondaryBrown,
                label = "Tanggal ada tagihan"
            )
            Spacer(modifier = Modifier.height(8.dp))

            LegendItem(
                color = Color.LightGray,
                label = "Hari ini",
                isBordered = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            LegendItem(
                color = PrimaryRed,
                label = "Tanggal dipilih"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ketuk tanggal berwarna untuk melihat tagihan",
                fontSize = 12.sp,
                color = SecondaryBrown,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    isBordered: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isBordered) {
                        Modifier.border(2.dp, SecondaryBrown, CircleShape)
                    } else {
                        Modifier
                    }
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextDark
        )
    }
}


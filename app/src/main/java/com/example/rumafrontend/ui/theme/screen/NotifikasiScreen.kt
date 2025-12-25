package com.example.rumafrontend.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.rumafrontend.data.entity.Notifikasi
import com.example.rumafrontend.ui.theme.notification.NotifikasiHolder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(
    navController: NavController
) {
    
    val CreamBackground = Color(0xFFEFE9D9)
    val MaroonPrimary = Color(0xFF712626)
    val MaroonCard = Color(0xFF712626)
    val MaroonText = Color(0xFF8B7355)

    
    val allNotifikasi by NotifikasiHolder.notifikasiFlow.collectAsState()

    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Semua", "Belum Dibaca", "Dibaca")

    
    val filteredNotifikasi = when (selectedTab) {
        0 -> allNotifikasi
        1 -> NotifikasiHolder.getUnreadNotifications()
        2 -> NotifikasiHolder.getReadNotifications()
        else -> allNotifikasi
    }

    
    val unreadCount = NotifikasiHolder.getUnreadCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Notifikasi",
                            color = MaroonPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text("$unreadCount", fontSize = 12.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaroonPrimary
                        )
                    }
                },
                actions = {
                    
                    if (unreadCount > 0) {
                        IconButton(onClick = {
                            NotifikasiHolder.markAllAsRead()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Tandai semua dibaca",
                                tint = MaroonPrimary
                            )
                        }
                    }

                    
                    if (allNotifikasi.isNotEmpty()) {
                        IconButton(onClick = {
                            NotifikasiHolder.deleteAll()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Hapus semua",
                                tint = MaroonPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground
                )
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, title ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedTab = index }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                color = if (selectedTab == index) MaroonPrimary else MaroonText,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            
                            if (index == 1 && unreadCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Text("$unreadCount", fontSize = 10.sp)
                                }
                            }
                        }
                        if (selectedTab == index) {
                            HorizontalDivider(
                                color = MaroonPrimary,
                                thickness = 2.dp,
                                modifier = Modifier.width(80.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            
            if (filteredNotifikasi.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaroonText.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> "Tidak ada notifikasi"
                                1 -> "Tidak ada notifikasi belum dibaca"
                                2 -> "Tidak ada notifikasi yang sudah dibaca"
                                else -> "Tidak ada notifikasi"
                            },
                            color = MaroonText,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaroonCard
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        items(filteredNotifikasi, key = { it.id }) { notif ->
                            NotifikasiItem(
                                notifikasi = notif,
                                onClick = {
                                    
                                    if (!notif.isRead) {
                                        NotifikasiHolder.markAsRead(notif.id)
                                    }

                                    
                                    when (notif.jenisNotifikasi) {
                                        "pengingat_tagihan" -> {
                                            notif.referenceId?.let { tagihanId ->
                                                navController.navigate("editTagihan/$tagihanId")
                                            }
                                        }
                                        "pengingat_agenda" -> {
                                            
                                            notif.referenceId?.let { agendaId ->
                                                
                                            }
                                        }
                                    }
                                },
                                onDelete = {
                                    NotifikasiHolder.delete(notif.id)
                                }
                            )
                            if (notif != filteredNotifikasi.last()) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.3f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotifikasiItem(
    notifikasi: Notifikasi,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (notifikasi.jenisNotifikasi) {
                    "pengingat_agenda" -> Icons.Default.EventNote
                    "pengingat_tagihan" -> Icons.Default.Payment
                    "sistem" -> Icons.Default.Info
                    else -> Icons.Default.Notifications
                },
                contentDescription = null,
                tint = Color(0xFF8B4B4B),
                modifier = Modifier.size(24.dp)
            )
        }

        
        if (!notifikasi.isRead) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .offset(x = (-8).dp, y = 4.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = notifikasi.pesan,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = if (!notifikasi.isRead) FontWeight.Bold else FontWeight.Normal
            )

            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(notifikasi.timestamp),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        
        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Hapus",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Notifikasi") },
            text = { Text("Apakah Anda yakin ingin menghapus notifikasi ini?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Baru saja"
        diff < 3600000 -> "${diff / 60000} menit yang lalu"
        diff < 86400000 -> "${diff / 3600000} jam yang lalu"
        diff < 172800000 -> "Kemarin"
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            sdf.format(Date(timestamp))
        }
    }
}
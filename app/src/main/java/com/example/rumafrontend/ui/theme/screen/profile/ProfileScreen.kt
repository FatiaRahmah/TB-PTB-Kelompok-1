package com.example.rumafrontend.ui.theme.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.rumafrontend.R

@Composable
fun ProfileScreen(
    onEditClick: () -> Unit,
    onLogoutNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile = viewModel.profileState
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val darkMode = viewModel.isDarkMode
    val showLogoutDialog = viewModel.showLogoutDialog

    
    
    val reloadTrigger = remember { System.currentTimeMillis() }
    
    LaunchedEffect(reloadTrigger) {
        viewModel.loadProfile()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3E8D6))
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            
            if (profile?.foto_profil.isNullOrBlank()) {
                
                Image(
                    painter = painterResource(id = R.drawable.foto_profil),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                
                AsyncImage(
                    model = profile!!.foto_profil,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A1C1C))
            ) {
                Text("Edit")
            }

            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(text = errorMessage, color = Color.Red)
            } else if (profile != null) {
                OutlinedTextField(
                    value = profile.username ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username") }
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = profile.email,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") }
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Mode")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = darkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.openLogoutDialog() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A1C1C))
            ) {
                Text("Log Out")
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeLogoutDialog() },
                title = { Text("Yakin Ingin Log Out??", fontWeight = FontWeight.Bold) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.logout()
                            viewModel.closeLogoutDialog()
                            onLogoutNavigateToLogin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A1C1C))
                    ) {
                        Text("Yakin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeLogoutDialog() }) {
                        Text("Ga jadi")
                    }
                }
            )
        }
    }
}

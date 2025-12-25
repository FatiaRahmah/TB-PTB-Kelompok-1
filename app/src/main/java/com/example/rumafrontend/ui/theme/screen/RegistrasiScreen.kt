package com.example.rumafrontend.ui.theme.screen

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rumafrontend.ViewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrasiScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    
    var showSuccessDialog by remember { mutableStateOf(false) }

    
    val registerResult by authViewModel.registerResult.collectAsState()
    val registerError by authViewModel.registerError.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    
    LaunchedEffect(registerResult) {
        if (registerResult) {
            showSuccessDialog = true
        }
    }

    
    LaunchedEffect(registerError) {
        if (registerError.isNotEmpty()) {
            errorMessage = registerError
            showError = true
        }
    }

    val CreamBackground = Color(0xFFF2ECDC)
    val MaroonPrimary = Color(0xFF7E2625)
    val MaroonText = Color(0xFF868859)

    fun isValidEmail(email: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Registrasi",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ayo buat akun baru",
                fontSize = 18.sp,
                color = MaroonText
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )
            Spacer(Modifier.height(24.dp))

            
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaroonPrimary,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isLoading,
                onClick = {
                    when {
                        email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "Semua field wajib diisi"
                            showError = true
                        }
                        !isValidEmail(email) -> {
                            errorMessage = "Format email tidak valid"
                            showError = true
                        }
                        password.length < 6 -> {
                            errorMessage = "Password minimal 6 karakter"
                            showError = true
                        }
                        password != confirmPassword -> {
                            errorMessage = "Password tidak cocok"
                            showError = true
                        }
                        else -> {
                            showError = false
                            authViewModel.register(email, password) 
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Registrasi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (showError) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        
        if (showSuccessDialog) {
            Dialog(
                onDismissRequest = { } 
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Registrasi Berhasil 🎉",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Akun Anda telah tersimpan. Silakan masuk untuk melanjutkan.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                            onClick = {
                                showSuccessDialog = false
                                authViewModel.resetRegisterResult()
                                onNavigateToLogin()
                            }
                        ) {
                            Text(
                                "Ke Halaman Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
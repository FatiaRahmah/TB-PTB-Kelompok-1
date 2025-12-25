package com.example.rumafrontend.ui.theme.screen.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.data.remote.profileResponse
import com.example.rumafrontend.Repository.UserRepository
import com.example.rumafrontend.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var profileState by mutableStateOf<profileResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    
    var isDarkMode by mutableStateOf(false)
        private set

    var showLogoutDialog by mutableStateOf(false)
        private set

    
    var photoPath by mutableStateOf<String?>(null)
        private set

    fun onPhotoSelected(path: String?) {
        photoPath = path
    }

    
    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.getProfile()
            if (result.isSuccess) {
                profileState = result.getOrNull()
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }

            isLoading = false
        }
    }

    
    fun updateProfile(
        userId: Int,
        username: String,
        email: String,
        password: String?
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val file: File? = photoPath?.let { File(it) }

            val result = userRepository.updateProfile(
                userId = userId,
                username = username,
                email = email,
                password = password,
                photoFile = file
            )

            if (result.isSuccess) {
                profileState = result.getOrNull()
                
                photoPath = null
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }

            isLoading = false
        }
    }

    
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            profileState = null
            photoPath = null
        }
    }

    fun toggleDarkMode() { isDarkMode = !isDarkMode }
    fun openLogoutDialog() { showLogoutDialog = true }
    fun closeLogoutDialog() { showLogoutDialog = false }
}


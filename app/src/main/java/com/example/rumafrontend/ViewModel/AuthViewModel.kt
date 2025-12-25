package com.example.rumafrontend.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.data.entity.UserEntity
import com.example.rumafrontend.data.model.RegisterRequest
import com.example.rumafrontend.data.model.loginRequest

import com.example.rumafrontend.data.repository.UserRepository
import com.example.rumafrontend.network.ApiClient
import com.example.rumafrontend.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _registerResult = MutableStateFlow(false)
    val registerResult: StateFlow<Boolean> = _registerResult

    private val _registerError = MutableStateFlow("")
    val registerError: StateFlow<String> = _registerError

    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult: StateFlow<Boolean?> = _loginResult

    private val _loginError = MutableStateFlow("")
    val loginError: StateFlow<String> = _loginError

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    
    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _registerError.value = ""
                _registerResult.value = false

                
                val registerRequest = RegisterRequest(
                    email = email,
                    password = password
                )

                val response = ApiClient.apiService.register(registerRequest)

                if (response.isSuccessful && response.body() != null) {
                    
                    val registerResponse = response.body()!!

                    
                    val user = UserEntity(email = email, password = password)
                    repository.insert(user)

                    _registerResult.value = true
                    _registerError.value = ""
                } else {
                    _registerResult.value = false
                    
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Registrasi gagal"
                    } catch (e: Exception) {
                        "Registrasi gagal: ${response.message()}"
                    }
                    _registerError.value = errorMessage
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _registerResult.value = false
                _registerError.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _loginError.value = ""
                _loginResult.value = null

                val loginReq = loginRequest(email = email, password = password)
                val response = ApiClient.apiService.login(loginReq)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    if (loginResponse.status == "Success") {
                        
                        tokenManager.saveToken(loginResponse.token)

                        
                        tokenManager.saveUserData(
                            email = email, 
                            userId = loginResponse.user?.user_id ?: 0,
                            username = loginResponse.user?.username ?: email.substringBefore("@")
                        )

                        
                        val user = UserEntity(
                            email = email, 
                            password = password
                        )
                        repository.insert(user)

                        _currentUser.value = user
                        _loginResult.value = true
                        _loginError.value = ""
                    } else {
                        _loginResult.value = false
                        _loginError.value = loginResponse.message
                    }
                } else {
                    _loginResult.value = false
                    
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Email atau password salah"
                    } catch (e: Exception) {
                        "Email atau password salah"
                    }
                    _loginError.value = errorMessage
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginResult.value = false
                _loginError.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    
    fun logout() {
        tokenManager.clearToken()
        _currentUser.value = null
        _loginResult.value = null
        _loginError.value = ""
    }

    
    fun resetRegisterResult() {
        _registerResult.value = false
        _registerError.value = ""
    }

    fun resetLoginResult() {
        _loginResult.value = null
        _loginError.value = ""
    }

    fun getToken(): String? {
        return tokenManager.getToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isTokenExists()
    }
}

package com.example.rumafrontend.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val email: String,
    val token: String,
    val message: String,
    val user: UserData? = null
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class RegisterResponse(
    val status: String? = null,
    val message: String,
    val data: UserData? = null
)

data class UserData(
    val user_id: Int,
    val username: String,
    val email: String,
    val foto_profil: String? = null
)

typealias loginRequest = LoginRequest
typealias loginRespons = LoginResponse

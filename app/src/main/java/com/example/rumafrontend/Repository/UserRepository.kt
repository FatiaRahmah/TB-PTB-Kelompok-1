package com.example.rumafrontend.Repository

import com.example.rumafrontend.data.remote.profileResponse
import com.example.rumafrontend.network.ApiService
import com.example.rumafrontend.utils.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private suspend fun authHeader(): String? {
        val token = tokenManager.getToken()
        return token?.let { "Bearer $it" }
    }

    suspend fun getProfile(): Result<profileResponse> {
        val header = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            val response = apiService.getProfile(header)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat profil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: Int,
        username: String,
        email: String,
        password: String?,
        photoFile: File?
    ): Result<profileResponse> {
        val header = authHeader() ?: return Result.failure(Exception("Token kosong"))

        val usernameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), username)
        val emailBody = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
        val passwordBody = password?.let {
            RequestBody.create("text/plain".toMediaTypeOrNull(), it)
        }

        val photoPart = photoFile?.let { file ->
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("foto_profil", file.name, requestFile)
        }

        return try {
            val response = apiService.updateProfile(
                header,
                userId,
                usernameBody,
                emailBody,
                passwordBody,
                photoPart
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal update profil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

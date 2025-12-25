package com.example.rumafrontend.Repository

import android.util.Log
import com.example.rumafrontend.utils.TokenManager
import com.example.rumafrontend.network.ApiService
import com.example.rumafrontend.data.remote.CreateResepRequest
import com.example.rumafrontend.data.remote.ResepResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResepRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private fun authHeader(): String? {
        val token = tokenManager.getToken()
        Log.d("ResepRepo", "=== TOKEN DEBUG ===")
        Log.d("ResepRepo", "Raw token: $token")
        
        
        val header = if (token?.startsWith("Bearer ") == true) {
            
            Log.d("ResepRepo", "Token already has Bearer prefix")
            token
        } else {
            
            Log.d("ResepRepo", "Adding Bearer prefix")
            token?.let { "Bearer $it" }
        }
        
        Log.d("ResepRepo", "Final auth header: ${header?.take(50)}...")
        return header
    }

    suspend fun createResep(
        request: CreateResepRequest
    ): Result<ResepResponse> {
        val h = authHeader()
        if (h == null) {
            Log.e("ResepRepo", "createResep: Token kosong - user belum login")
            return Result.failure(Exception("Anda harus login terlebih dahulu"))
        }
        
        Log.d("ResepRepo", "=== CREATE RESEP REQUEST ===")
        Log.d("ResepRepo", "Judul: ${request.judul}")
        Log.d("ResepRepo", "Bahan count: ${request.bahan.size}")
        Log.d("ResepRepo", "Langkah count: ${request.langkah.size}")
        Log.d("ResepRepo", "Foto: ${request.foto}")
        Log.d("ResepRepo", "is_favorit: ${request.is_favorit}")
        
        return try {
            val response = apiService.createResep(h, request)
            Log.d("ResepRepo", "Response code: ${response.code()}")
            Log.d("ResepRepo", "Response success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                Log.d("ResepRepo", "createResep: Success!")
                Result.success(response.body()!!)
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("ResepRepo", "createResep gagal code=$code body=$errorBody")
                Log.e("ResepRepo", "Response headers: ${response.headers()}")
                Result.failure(Exception("Gagal menambah resep: ${errorBody ?: "Error $code"}"))
            }
        } catch (e: Exception) {
            Log.e("ResepRepo", "createResep exception: ${e.message}", e)
            Result.failure(Exception("Gagal menambah resep: ${e.message}"))
        }
    }

    suspend fun searchResep(query: String?): Result<List<ResepResponse>> {
        val h = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            Result.success(apiService.getAllResep(h, query))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getResepDetail(id: Int): Result<ResepResponse> {
        val h = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            Result.success(apiService.getResepById(h, id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteResep(id: Int): Result<Unit> {
        val h = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            val resp = apiService.deleteResep(h, id)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Gagal hapus resep"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

   suspend fun getFavorit(): Result<List<ResepResponse>> {
        val h = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            Result.success(apiService.getFavoritResep(h))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorit(id: Int): Result<ResepResponse> {
        val h = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            Result.success(apiService.toggleFavorit(h, id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateResep(id: Int, request: CreateResepRequest): Result<ResepResponse> {
        val h = authHeader() ?: return Result.failure(Exception("Token kosong"))
        return try {
            val response = apiService.updateResep(h, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("ResepRepo", "updateResep gagal code=$code body=$errorBody")
                Result.failure(Exception("Gagal mengupdate resep ($code)"))
            }
        } catch (e: Exception) {
            Log.e("ResepRepo", "updateResep exception", e)
            Result.failure(e)
        }
    }

}


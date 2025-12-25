package com.example.rumafrontend.data.repository

import android.util.Log
import com.example.rumafrontend.data.dao.AgendaDao
import com.example.rumafrontend.data.entity.Agenda
import com.example.rumafrontend.data.model.AgendaRequest
import com.example.rumafrontend.data.model.AgendaResponse
import com.example.rumafrontend.data.model.toRequest
import com.example.rumafrontend.network.ApiService
import com.example.rumafrontend.utils.TokenManager
import kotlinx.coroutines.flow.Flow

class AgendaRepository(
    private val agendaDao: AgendaDao,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    
    fun getAllAgendas(): Flow<List<Agenda>> = agendaDao.getAllAgendas()
    suspend fun insertAgenda(agenda: Agenda): Long = agendaDao.insert(agenda)
    suspend fun updateAgenda(agenda: Agenda) = agendaDao.update(agenda)
    suspend fun deleteAgenda(agenda: Agenda) = agendaDao.delete(agenda)
    suspend fun getById(id: Long): Agenda? = agendaDao.getById(id)
    suspend fun deleteAllAgendas() = agendaDao.deleteAll()
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) = agendaDao.updateCompletionStatus(id, isCompleted)

    

    private fun getAuthToken(): String {
        val savedToken = tokenManager.getToken() ?: ""
        return if (savedToken.startsWith("Bearer", ignoreCase = true)) {
            savedToken
        } else {
            "Bearer $savedToken"
        }
    }

    suspend fun createAgendaOnServer(agenda: Agenda): Result<AgendaResponse> {
        return try {
            val request = agenda.toRequest()
            val response = apiService.createAgenda(getAuthToken(), request)

            if (response.isSuccessful && response.body() != null) {
                val agendaResponse = response.body()!!.data
                Log.d("AGENDA_API", "✅ CREATE SUCCESS: ${agendaResponse.judul}")
                Result.success(agendaResponse)
            } else {
                val error = "API Error: ${response.code()} - ${response.message()}"
                Log.e("AGENDA_API", "❌ CREATE FAILED: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AGENDA_API", "❌ CREATE EXCEPTION: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun fetchAgendasFromServer(
        search: String? = null,
        kategori: String? = null,
        date: String? = null
    ): Result<List<AgendaResponse>> {
        return try {
            val response = apiService.getAllAgendas(
                token = getAuthToken(),
                search = search,
                kategori = kategori,
                date = date
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAgendaOnServer(agenda: Agenda): Result<String> {
        return try {
            val request = agenda.toRequest()
            val response = apiService.updateAgenda(getAuthToken(), agenda.id, request)

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Terupdate"
                Result.success(message)
            } else {
                Result.failure(Exception("Gagal Update"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAgendaOnServer(agendaId: Long): Result<String> {
        return try {
            val response = apiService.deleteAgenda(getAuthToken(), agendaId)
            if (response.isSuccessful) {
                Result.success("Terhapus")
            } else {
                Result.failure(Exception("Gagal Hapus"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAgendasFromServer(): Result<Int> {
        return try {
            val result = fetchAgendasFromServer()
            if (result.isSuccess) {
                val remoteAgendas = result.getOrNull() ?: emptyList()
                agendaDao.deleteAll()
                remoteAgendas.forEach { response ->
                    agendaDao.insert(response.toEntity())
                }
                Result.success(remoteAgendas.size)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Sync Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

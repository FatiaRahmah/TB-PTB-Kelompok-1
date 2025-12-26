package com.example.rumafrontend.data.repository

import android.content.Context
import android.util.Log
import com.example.rumafrontend.data.dao.TagihanDao
import com.example.rumafrontend.data.entity.TagihanEntity
import com.example.rumafrontend.data.model.*
import com.example.rumafrontend.network.ApiClient
import com.example.rumafrontend.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class TagihanRepository(
    private val tagihanDao: TagihanDao,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "TagihanRepository"
    }
    
    
    
    
    fun getAllTagihan(): Flow<List<TagihanEntity>> {
        return tagihanDao.getAllTagihan()
    }
    
    
    fun getTagihanByStatus(status: String): Flow<List<TagihanEntity>> {
        return tagihanDao.getTagihanByStatus(status)
    }
    
    
    suspend fun getTagihanById(id: Int): TagihanEntity? {
        return tagihanDao.getTagihanById(id)
    }
    
    
    
    
    suspend fun createTagihan(tagihan: TagihanEntity): Result<Long> {
        return try {
            
            val localId = tagihanDao.insert(tagihan.copy(isSynced = false))
            Log.d(TAG, "Tagihan saved locally with ID: $localId")
            
            
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    val request = tagihan.toRequest()
                    val response = ApiClient.apiService.createTagihan(request)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val serverId = response.body()!!.newTagihan.tagihanId.toString()
                        
                        
                        tagihanDao.updateSyncStatus(
                            id = localId.toInt(),
                            isSynced = true,
                            serverId = serverId
                        )
                        Log.d(TAG, "Tagihan synced to backend with server ID: $serverId")
                    } else {
                        Log.w(TAG, "Failed to sync to backend: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing to backend: ${e.message}", e)
                    
                }
            } else {
                Log.d(TAG, "No internet connection, will sync later")
            }
            
            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating tagihan: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    
    
    
    suspend fun updateTagihan(tagihan: TagihanEntity): Result<Unit> {
        return try {
            
            tagihanDao.update(tagihan.copy(
                lastModified = System.currentTimeMillis(),
                isSynced = false
            ))
            Log.d(TAG, "Tagihan updated locally: ${tagihan.id}")
            
            
            if (tagihan.serverId != null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    val request = tagihan.toRequest()
                    val response = ApiClient.apiService.updateTagihan(
                        id = tagihan.serverId.toInt(),
                        request = request
                    )
                    
                    if (response.isSuccessful) {
                        tagihanDao.updateSyncStatus(
                            id = tagihan.id,
                            isSynced = true,
                            serverId = tagihan.serverId
                        )
                        Log.d(TAG, "Tagihan synced to backend")
                    } else {
                        Log.w(TAG, "Failed to sync update: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing update: ${e.message}", e)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tagihan: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    
    suspend fun updateStatus(id: Int, status: String, tanggalSelesaiMillis: Long? = null, clearFoto: Boolean = false): Result<Unit> {
        return try {
            val tagihan = tagihanDao.getTagihanById(id)
            if (tagihan == null) {
                return Result.failure(Exception("Tagihan not found"))
            }

            
            tagihanDao.updateStatus(id, status, tanggalSelesaiMillis)
            if (clearFoto) {
                tagihanDao.updateBuktiFoto(id, null)
            }
            Log.d(TAG, "Tagihan status updated locally: $id to $status")

            
            if (tagihan.serverId != null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    val tanggalSelesaiStr = if (tanggalSelesaiMillis != null) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(java.util.Date(tanggalSelesaiMillis))
                    } else null

                    val request = UpdateStatusRequest(
                        status = status,
                        tanggalSelesai = tanggalSelesaiStr,
                        clearFoto = clearFoto
                    )

                    val response = ApiClient.apiService.updateStatus(
                        id = tagihan.serverId.toInt(),
                        request = request
                    )

                    if (response.isSuccessful) {
                        tagihanDao.updateSyncStatus(id, true, tagihan.serverId)
                        Log.d(TAG, "Status synced to backend")
                    } else {
                        Log.e(TAG, "Sync status failed: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing status: ${e.message}", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status: ${e.message}", e)
            Result.failure(e)
        }
    }

    
    suspend fun updateFoto(id: Int, buktiFotoPath: String): Result<Unit> {
        return try {
            val tagihan = tagihanDao.getTagihanById(id)
            if (tagihan == null) {
                return Result.failure(Exception("Tagihan not found"))
            }

            
            tagihanDao.updateBuktiFoto(id, buktiFotoPath)
            Log.d(TAG, "Tagihan photo updated locally: $id")

            
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating photo: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun tandaiLunas(id: Int, buktiFotoPath: String? = null, tanggalSelesaiMillis: Long? = null): Result<Unit> {
        return try {
            val tagihan = tagihanDao.getTagihanById(id)
            if (tagihan == null) {
                return Result.failure(Exception("Tagihan not found"))
            }
            
            
            val now = tanggalSelesaiMillis ?: System.currentTimeMillis()
            tagihanDao.updateStatus(id, "lunas", now)
            if (buktiFotoPath != null) {
                tagihanDao.updateBuktiFoto(id, buktiFotoPath)
            }
            Log.d(TAG, "Tagihan marked as lunas locally: $id")
            
            
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error tandai lunas: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteTagihan(id: Int): Result<Unit> {
        return try {
            val tagihan = tagihanDao.getTagihanById(id)
            if (tagihan == null) {
                return Result.failure(Exception("Tagihan not found"))
            }
            
            
            if (tagihan.serverId != null && NetworkUtils.isNetworkAvailable(context)) {
                try {
                    val response = ApiClient.apiService.deleteTagihan(
                        id = tagihan.serverId.toInt()
                    )
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Tagihan deleted from backend")
                    } else {
                        Log.w(TAG, "Failed to delete from backend: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting from backend: ${e.message}", e)
                }
            }
            
            
            tagihanDao.deleteById(id)
            Log.d(TAG, "Tagihan deleted locally: $id")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting tagihan: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    
    
    
    suspend fun syncUnsyncedData(): Result<Int> {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val unsyncedTagihan = tagihanDao.getUnsyncedTagihan()
            var syncedCount = 0
            
            for (tagihan in unsyncedTagihan) {
                try {
                    if (tagihan.serverId == null) {
                        
                        val request = tagihan.toRequest()
                        val response = ApiClient.apiService.createTagihan(request)
                        
                        if (response.isSuccessful && response.body() != null) {
                            val serverId = response.body()!!.newTagihan.tagihanId.toString()
                            tagihanDao.updateSyncStatus(tagihan.id, true, serverId)
                            syncedCount++
                        }
                    } else {
                        
                        val request = tagihan.toRequest()
                        val response = ApiClient.apiService.updateTagihan(
                            id = tagihan.serverId.toInt(),
                            request = request
                        )
                        
                        if (response.isSuccessful) {
                            tagihanDao.updateSyncStatus(tagihan.id, true, tagihan.serverId)
                            syncedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing tagihan ${tagihan.id}: ${e.message}", e)
                }
            }
            
            Log.d(TAG, "Synced $syncedCount out of ${unsyncedTagihan.size} tagihan")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    
    suspend fun fetchFromBackend(): Result<Int> {
        return try {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val response = ApiClient.apiService.getAllTagihan()
            
            if (response.isSuccessful && response.body() != null) {
                val tagihanList = response.body()!!
                var insertedCount = 0
                
                for (tagihanResponse in tagihanList) {
                    
                    val existing = tagihanDao.getTagihanByServerId(
                        tagihanResponse.tagihanId.toString()
                    )
                    
                    if (existing == null) {
                        
                        val entity = tagihanResponse.toEntity()
                        tagihanDao.insert(entity)
                        insertedCount++
                    } else {
                        
                        val entity = tagihanResponse.toEntity().copy(id = existing.id)
                        tagihanDao.update(entity)
                    }
                }
                
                Log.d(TAG, "Fetched and inserted $insertedCount new tagihan from backend")
                Result.success(insertedCount)
            } else {
                Result.failure(Exception("Failed to fetch: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from backend: ${e.message}", e)
            Result.failure(e)
        }
    }
}

package com.example.rumafrontend.ui.theme.notification

import android.content.Context
import com.example.rumafrontend.data.database.RumaDatabase
import com.example.rumafrontend.data.entity.Notifikasi
import com.example.rumafrontend.data.entity.NotifikasiEntity
import com.example.rumafrontend.data.entity.toEntity
import com.example.rumafrontend.data.entity.toNotifikasi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.rumafrontend.network.ApiClient
import com.example.rumafrontend.data.model.NotifikasiRequest
import com.example.rumafrontend.utils.NetworkUtils

object NotifikasiHolder {

    private lateinit var database: RumaDatabase
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notifikasiList = MutableStateFlow<List<Notifikasi>>(emptyList())
    val notifikasiFlow: StateFlow<List<Notifikasi>> = _notifikasiList.asStateFlow()

    
    fun initialize(context: Context) {
        database = RumaDatabase.getDatabase(context)

        
        scope.launch {
            database.notifikasiDao().getAllNotifikasi().collect { entities ->
                _notifikasiList.value = entities.map { entity ->
                    entity.toNotifikasi()
                }
            }
        }
        
        
        scope.launch {
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    val response = ApiClient.apiService.getAllNotifikasi()
                    if (response.isSuccessful && response.body() != null) {
                        
                        response.body()!!.forEach { serverNotif ->
                            
                            
                            
                        }
                    }
                } catch (e: Exception) {
                    
                }
            }
        }
    }

    
    fun add(notifikasi: Notifikasi, context: Context? = null) {
        scope.launch {
            try {
                
                val entity = notifikasi.toEntity()
                val idLocal = database.notifikasiDao().insert(entity)
                
                
                
                val currentContext = context ?: return@launch
                if (NetworkUtils.isNetworkAvailable(currentContext)) {
                     val request = NotifikasiRequest(
                        jenisNotifikasi = notifikasi.jenisNotifikasi,
                        pesan = notifikasi.pesan,
                        referenceId = notifikasi.referenceId,
                        referenceType = when (notifikasi.jenisNotifikasi) {
                            "pengingat_tagihan" -> "tagihan"
                            "pengingat_agenda" -> "agenda"
                            else -> null
                        },
                        additionalData = notifikasi.additionalData,
                        isRead = notifikasi.isRead
                    )
                    
                    try {
                        ApiClient.apiService.createNotifikasi(request)
                        
                    } catch (e: Exception) {
                        
                    }
                }
            } catch (e: Exception) {
                
                e.printStackTrace()
            }
        }
    }

    
    fun markAsRead(id: Int) {
        scope.launch {
            try {
                database.notifikasiDao().markAsRead(id)
                
            } catch (e: Exception) {
                
            }
        }
    }

    
    fun markAllAsRead() {
        scope.launch {
            try {
                database.notifikasiDao().markAllAsRead()
                
            } catch (e: Exception) {
                
            }
        }
    }

    
    fun delete(id: Int) {
        scope.launch {
            try {
                database.notifikasiDao().deleteById(id)
                
            } catch (e: Exception) {
                
            }
        }
    }

    
    fun deleteAll() {
        scope.launch {
            try {
                database.notifikasiDao().deleteAll()
                
            } catch (e: Exception) {
                
            }
        }
    }

    
    fun getUnreadCount(): Int {
        return _notifikasiList.value.count { !it.isRead }
    }

    
    fun getUnreadNotifications(): List<Notifikasi> {
        return _notifikasiList.value.filter { !it.isRead }
    }

    
    fun getReadNotifications(): List<Notifikasi> {
        return _notifikasiList.value.filter { it.isRead }
    }

    
    fun cleanOldNotifications(daysOld: Int = 30) {
        scope.launch {
            try {
                val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
                database.notifikasiDao().deleteOldReadNotifikasi(cutoffTime)
                
            } catch (e: Exception) {
                
            }
        }
    }
}
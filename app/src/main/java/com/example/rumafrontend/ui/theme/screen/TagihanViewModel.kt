package com.example.rumafrontend.ui.theme.screen

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.data.database.RumaDatabase
import com.example.rumafrontend.data.entity.TagihanEntity
import com.example.rumafrontend.data.entity.toBill
import com.example.rumafrontend.data.repository.TagihanRepository
import com.example.rumafrontend.ui.theme.reminderschedule.cancelTagihanReminder
import com.example.rumafrontend.ui.theme.reminderschedule.rescheduleTagihanReminder
import com.example.rumafrontend.ui.theme.reminderschedule.scheduleTagihanReminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import java.util.*
import com.example.rumafrontend.data.model.Bill
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import android.content.Context

class TagihanViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private val database = RumaDatabase.getDatabase(context)
    private val tagihanDao = database.tagihanDao()
    
    
    private val repository = TagihanRepository(tagihanDao, context)

    private val _tagihanList = MutableStateFlow<List<Bill>>(emptyList())
    val tagihanList: StateFlow<List<Bill>> = _tagihanList.asStateFlow()

    private val _riwayatList = MutableStateFlow<List<Bill>>(emptyList())
    val riwayatList: StateFlow<List<Bill>> = _riwayatList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        loadTagihan()
        loadRiwayat()
    }

    

    private fun loadTagihan() {
        viewModelScope.launch {
            tagihanDao.getTagihanByStatus("belum")
                .map { entities -> entities.map { it.toBill() } }
                .collect { bills ->
                    _tagihanList.value = bills
                }
        }
    }

    private fun loadRiwayat() {
        viewModelScope.launch {
            tagihanDao.getTagihanByStatus("lunas")
                .map { entities -> entities.map { it.toBill() } }
                .collect { bills ->
                    _riwayatList.value = bills
                }
        }
    }

    

    fun tambahTagihan(
        context: Context,
        title: String,
        dueDateMillis: Long,
        description: String = "",
        reminderDays: Int = 1,
        repeatType: String = "Sekali saja",
        buktiFotoPath: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val entity = TagihanEntity(
                    title = title,
                    description = description.ifBlank { null },
                    dueDateMillis = dueDateMillis,
                    status = "belum",
                    reminderDays = reminderDays,
                    repeatType = repeatType,
                    buktiFotoPath = buktiFotoPath 
                )

                
                val result = repository.createTagihan(entity)
                
                result.onSuccess { newId ->
                    
                    
                    if (buktiFotoPath != null) {
                        repository.updateFoto(newId.toInt(), buktiFotoPath)
                    }

                    
                    scheduleTagihanReminder(
                        context = context,
                        tagihanId = newId.toInt(),
                        tagihanTitle = title,
                        dueDateMillis = dueDateMillis,
                        reminderDays = reminderDays
                    )

                    Log.d("TagihanViewModel", "✅ Tagihan created: $title (ID: $newId)")
                    android.widget.Toast.makeText(context, "Berhasil disimpan & sync ke server!", android.widget.Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Error creating tagihan: ${error.message}", error)
                    android.widget.Toast.makeText(context, "Gagal sync: ${error.message}. Tersimpan lokal.", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Exception in tambahTagihan: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    

    fun updateTagihan(
        context: Context,
        id: Int,
        title: String,
        dueDateMillis: Long,
        description: String = "",
        reminderDays: Int = 1,
        repeatType: String = "Sekali saja",
        buktiFotoPath: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val existing = tagihanDao.getTagihanById(id)
                if (existing == null) {
                    Log.e("TagihanViewModel", "❌ Tagihan not found: $id")
                    return@launch
                }

                val updatedEntity = existing.copy(
                    title = title,
                    description = description.ifBlank { null },
                    dueDateMillis = dueDateMillis,
                    reminderDays = reminderDays,
                    repeatType = repeatType,
                    lastModified = System.currentTimeMillis(),
                    isSynced = false
                )

                val result = repository.updateTagihan(updatedEntity)
                
                result.onSuccess {
                    if (buktiFotoPath != null) {
                        repository.updateFoto(id, buktiFotoPath)
                    }

                    scheduleTagihanReminder(
                        context = context,
                        tagihanId = id,
                        tagihanTitle = title,
                        dueDateMillis = dueDateMillis,
                        reminderDays = reminderDays
                    )

                    Log.d("TagihanViewModel", "✅ Tagihan updated: $id")
                    android.widget.Toast.makeText(context, "Berhasil diperbarui!", android.widget.Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Error updating tagihan: ${error.message}", error)
                    android.widget.Toast.makeText(context, "Gagal update: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Exception in updateTagihan: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    

    fun hapusTagihan(context: Context, id: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                
                cancelTagihanReminder(context, id)
                
                
                val result = repository.deleteTagihan(id)
                
                result.onSuccess {
                    Log.d("TagihanViewModel", "✅ Tagihan deleted: $id")
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Error deleting tagihan: ${error.message}", error)
                }
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Exception in hapusTagihan: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    

    fun tandaiSelesai(context: Context, id: Int, buktiFotoPath: String? = null, tanggalSelesaiMillis: Long? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                
                val result = repository.tandaiLunas(id, buktiFotoPath, tanggalSelesaiMillis)
                
                result.onSuccess {
                    cancelTagihanReminder(context, id)
                    Log.d("TagihanViewModel", "✅ Tagihan marked as lunas: $id")
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Error marking tagihan as lunas: ${error.message}", error)
                }
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Exception in tandaiSelesai: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    

    fun kembalikanKeAktif(context: Context, id: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                
                val result = repository.updateStatus(id, "belum", clearFoto = false)
                
                result.onSuccess {
                    val bill = tagihanDao.getTagihanById(id)
                    bill?.let {
                        scheduleTagihanReminder(
                            context = context,
                            tagihanId = it.id,
                            tagihanTitle = it.title,
                            dueDateMillis = it.dueDateMillis,
                            reminderDays = it.reminderDays
                        )
                    }
                    Log.d("TagihanViewModel", "✅ Tagihan restored to active: $id")
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Error restoring tagihan: ${error.message}", error)
                }
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Exception in kembalikanKeAktif: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hapusDariRiwayat(id: Int) {
        viewModelScope.launch {
            try {
                tagihanDao.deleteById(id)
                
            } catch (e: Exception) {
                
            }
        }
    }

    

    fun getOverdueBills(): List<Bill> {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return _tagihanList.value.filter { bill ->
            bill.status == "belum" && bill.dueDateMillis < startOfToday
        }.sortedBy { it.dueDateMillis }
    }

    fun getUpcomingBills(): List<Bill> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        return _tagihanList.value.filter { bill ->
            bill.status == "belum" && bill.dueDateMillis >= today
        }.sortedBy { it.dueDateMillis }
    }

    fun getRiwayatByMonthYear(month: Int?, year: Int?): List<Bill> {
        if (month == null && year == null) {
            return _riwayatList.value
        }

        return _riwayatList.value.filter { bill ->
            val billCalendar = Calendar.getInstance().apply {
                timeInMillis = bill.dueDateMillis
            }

            val matchMonth = month?.let { billCalendar.get(Calendar.MONTH) == it } ?: true
            val matchYear = year?.let { billCalendar.get(Calendar.YEAR) == it } ?: true

            matchMonth && matchYear
        }
    }

    fun getTagihanById(id: Int): Bill? {
        return _tagihanList.value.find { it.id == id }
            ?: _riwayatList.value.find { it.id == id }
    }

    

    fun scheduleAllReminders() {
        viewModelScope.launch {
            try {
                tagihanDao.getTagihanByStatus("belum").collect { entities ->
                    entities.forEach { entity ->
                        scheduleTagihanReminder(
                            context = context,
                            tagihanId = entity.id,
                            tagihanTitle = entity.title,
                            dueDateMillis = entity.dueDateMillis,
                            reminderDays = entity.reminderDays
                        )
                    }
                    
                }
            } catch (e: Exception) {
                
            }
        }
    }
    
    
    
    
    fun syncAfterLogin() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _syncMessage.value = "Syncing data..."
                Log.d("TagihanViewModel", "🔄 Starting sync after login...")
                
                
                val fetchResult = repository.fetchFromBackend()
                fetchResult.onSuccess { count ->
                    Log.d("TagihanViewModel", "✅ Fetched $count tagihan from backend")
                    _syncMessage.value = "Fetched $count tagihan from server"
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Fetch failed: ${error.message}")
                }
                
                
                val syncResult = repository.syncUnsyncedData()
                syncResult.onSuccess { count ->
                    Log.d("TagihanViewModel", "✅ Synced $count unsynced tagihan to backend")
                    _syncMessage.value = "Sync completed successfully"
                }.onFailure { error ->
                    Log.e("TagihanViewModel", "❌ Sync failed: ${error.message}")
                    _syncMessage.value = "Sync completed with errors"
                }
                
                
                kotlinx.coroutines.delay(3000)
                _syncMessage.value = null
                
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Sync error: ${e.message}", e)
                _syncMessage.value = "Sync failed: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _syncMessage.value = null
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    
    fun manualSync() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _syncMessage.value = "Syncing..."
                
                
                val syncResult = repository.syncUnsyncedData()
                syncResult.onSuccess { count ->
                    Log.d("TagihanViewModel", "✅ Manual sync: $count items synced")
                }
                
                
                val fetchResult = repository.fetchFromBackend()
                fetchResult.onSuccess { count ->
                    Log.d("TagihanViewModel", "✅ Manual fetch: $count items fetched")
                    _syncMessage.value = "Sync successful"
                }
                
                kotlinx.coroutines.delay(2000)
                _syncMessage.value = null
                
            } catch (e: Exception) {
                Log.e("TagihanViewModel", "❌ Manual sync error: ${e.message}", e)
                _syncMessage.value = "Sync failed"
                kotlinx.coroutines.delay(2000)
                _syncMessage.value = null
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
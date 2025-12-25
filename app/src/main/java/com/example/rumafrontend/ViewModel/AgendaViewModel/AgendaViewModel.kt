package com.example.rumafrontend.ViewModel.AgendaViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.data.database.RumaDatabase
import com.example.rumafrontend.data.entity.Agenda
import com.example.rumafrontend.data.repository.AgendaRepository
import com.example.rumafrontend.network.ApiClient
import com.example.rumafrontend.utils.TokenManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

class AgendaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AgendaRepository
    val agendas: StateFlow<List<Agenda>>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val database = RumaDatabase.getDatabase(application)
        val apiService = ApiClient.apiService
        val tokenManager = TokenManager(application)
        repository = AgendaRepository(database.agendaDao(), apiService, tokenManager)

        agendas = repository.getAllAgendas()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            
        syncFromServer()
    }

    fun addAgenda(
        judul: String,
        kategori: String,
        deskripsi: String,
        tanggal: String,
        waktu: String,
        lokasi: String,
        pengingat: String
    ) {
        viewModelScope.launch {
            val agenda = Agenda(
                judul = judul,
                kategori = kategori,
                deskripsi = deskripsi,
                date = tanggal,
                time = waktu,
                location = lokasi,
                reminder = pengingat,
                isCompleted = false
            )
            
            
            repository.insertAgenda(agenda)
            
            
            repository.createAgendaOnServer(agenda)
        }
    }

    fun syncFromServer() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.syncAgendasFromServer()
            _isLoading.value = false
        }
    }

    fun updateAgenda(agenda: Agenda) {
        viewModelScope.launch {
            repository.updateAgenda(agenda)
            repository.updateAgendaOnServer(agenda)
        }
    }

    fun deleteAgenda(agenda: Agenda) {
        viewModelScope.launch {
            repository.deleteAgenda(agenda)
            repository.deleteAgendaOnServer(agenda.id)
        }
    }
}

package com.example.rumafrontend.ui.theme.screen.resep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.Repository.ResepRepository
import com.example.rumafrontend.data.remote.ResepResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailResepViewModel @Inject constructor(
    private val resepRepository: ResepRepository
) : ViewModel() {

    private val _detail = MutableStateFlow<ResepResponse?>(null)
    val detail: StateFlow<ResepResponse?> = _detail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = resepRepository.getResepDetail(id)
            if (result.isSuccess) {
                _detail.value = result.getOrNull()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }

    fun toggleFavorite() {
        val current = _detail.value ?: return
        viewModelScope.launch {
            val result = resepRepository.toggleFavorit(current.id)
            if (result.isSuccess) {
                _detail.value = current.copy(
                    is_favorit = if (current.is_favorit == 1) 0 else 1
                )
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun requestDelete() {
        _showDeleteDialog.value = true
    }

    fun cancelDelete() {
        _showDeleteDialog.value = false
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _detail.value ?: return
        viewModelScope.launch {
            val result = resepRepository.deleteResep(current.id)
            if (result.isSuccess) {
                _showDeleteDialog.value = false
                onDeleted()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}

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
class ResepViewModel @Inject constructor(
    private val resepRepository: ResepRepository
) : ViewModel() {

    private val _resepList = MutableStateFlow<List<ResepResponse>>(emptyList())
    val resepList: StateFlow<List<ResepResponse>> = _resepList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        searchResep("")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchResep(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = resepRepository.searchResep(
                query.ifBlank { null }
            )

            if (result.isSuccess) {
                _resepList.value = result.getOrNull().orEmpty()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }

    fun toggleFavorite(resep: ResepResponse) {
        viewModelScope.launch {
            val result = resepRepository.toggleFavorit(resep.id)

            if (result.isSuccess) {
                _resepList.value = _resepList.value.map {
                    if (it.id == resep.id) {
                        it.copy(
                            is_favorit = if (it.is_favorit == 1) 0 else 1
                        )
                    } else it
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}


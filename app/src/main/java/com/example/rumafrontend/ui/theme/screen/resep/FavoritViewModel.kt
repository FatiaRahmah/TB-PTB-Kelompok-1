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
class FavoritViewModel @Inject constructor(
    private val resepRepository: ResepRepository
) : ViewModel() {

    private val _list = MutableStateFlow<List<ResepResponse>>(emptyList())
    val list: StateFlow<List<ResepResponse>> = _list.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadFavorit()
    }

    fun loadFavorit() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = resepRepository.getFavorit()
            if (result.isSuccess) {
                _list.value = result.getOrNull().orEmpty()
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
                
                loadFavorit()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}

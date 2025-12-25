package com.example.rumafrontend.ui.theme.screen.resep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rumafrontend.Repository.ResepRepository
import com.example.rumafrontend.data.remote.BahanRequest
import com.example.rumafrontend.data.remote.CreateResepRequest
import com.example.rumafrontend.data.remote.LangkahRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BahanForm(
    val nama: String = "",
    val jumlah: String = "",
    val satuan: String = ""
)

data class LangkahForm(
    val step: Int,
    val deskripsi: String = "",
    val foto: String? = null
)

@HiltViewModel
class TambahResepViewModel @Inject constructor(
    private val resepRepository: ResepRepository
) : ViewModel() {

    var editingResepId by mutableStateOf<Int?>(null)
        private set

    var judul by mutableStateOf("")
    var porsi by mutableStateOf("")
    var waktu by mutableStateOf("")
    var fotoUrl by mutableStateOf<String?>(null)

    var bahanList by mutableStateOf(mutableListOf(BahanForm()))
    var langkahList by mutableStateOf(mutableListOf(LangkahForm(step = 1)))

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)

    fun addBahan() { bahanList = (bahanList + BahanForm()).toMutableList() }

    fun updateBahan(index: Int, newValue: BahanForm) {
        if (index in bahanList.indices) {
            val copy = bahanList.toMutableList()
            copy[index] = newValue
            bahanList = copy
        }
    }

    fun removeBahan(index: Int) {
        if (bahanList.size > 1 && index in bahanList.indices) {
            val copy = bahanList.toMutableList()
            copy.removeAt(index)
            bahanList = copy
        }
    }

    fun addLangkah() {
        val nextStep = (langkahList.maxOfOrNull { it.step } ?: 0) + 1
        langkahList = (langkahList + LangkahForm(step = nextStep)).toMutableList()
    }

    fun updateLangkah(index: Int, deskripsi: String) {
        if (index in langkahList.indices) {
            val copy = langkahList.toMutableList()
            copy[index] = copy[index].copy(deskripsi = deskripsi)
            langkahList = copy
        }
    }

    fun removeLangkah(index: Int) {
        if (langkahList.size > 1 && index in langkahList.indices) {
            val copy = langkahList.toMutableList()
            copy.removeAt(index)
            langkahList = copy
                .sortedBy { it.step }
                .mapIndexed { idx, l -> l.copy(step = idx + 1) }
                .toMutableList()
        }
    }

    fun submit(onDone: () -> Unit = {}) {
        viewModelScope.launch {

            
            if (judul.isBlank()) {
                errorMessage = "Judul resep tidak boleh kosong"
                return@launch
            }
            if (fotoUrl.isNullOrBlank()) {
                errorMessage = "Foto resep wajib diisi"
                return@launch
            }

            val porsiInt = porsi.toIntOrNull() ?: 0
            val waktuInt = waktu.toIntOrNull() ?: 0

            val bahanReq: List<BahanRequest> = bahanList
                .filter { it.nama.isNotBlank() }
                .map {
                    BahanRequest(
                        nama_bahan = it.nama,
                        jumlah = it.jumlah.toIntOrNull() ?: 0,
                        satuan = it.satuan
                    )
                }

            val langkahReq: List<LangkahRequest> = langkahList
                .filter { it.deskripsi.isNotBlank() }
                .map { LangkahRequest(deskripsi = it.deskripsi) }

            if (bahanReq.isEmpty()) {
                errorMessage = "Minimal 1 bahan dengan nama terisi"
                return@launch
            }
            if (langkahReq.isEmpty()) {
                errorMessage = "Minimal 1 langkah dengan deskripsi"
                return@launch
            }

            val request = CreateResepRequest(
                judul = judul,
                waktu_masak = waktuInt,
                porsi = porsiInt,
                foto = fotoUrl,
                is_favorit = false,
                bahan = bahanReq,
                langkah = langkahReq
            )

            isLoading = true
            errorMessage = null
            success = false

            val result = if (editingResepId == null) {
                resepRepository.createResep(request)
            } else {
                resepRepository.updateResep(editingResepId!!, request)
            }

            if (result.isSuccess) {
                success = true
                onDone()
            } else {
                errorMessage = result.exceptionOrNull()?.message
                    ?: "Gagal menyimpan resep"
            }

            isLoading = false
        }
    }

    fun startEdit(id: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            editingResepId = id

            val result = resepRepository.getResepDetail(id)
            if (result.isSuccess) {
                val resep = result.getOrNull()!!
                judul = resep.judul
                porsi = resep.porsi.toString()
                waktu = resep.waktu_masak.toString()
                fotoUrl = resep.foto

                bahanList = resep.Bahans.map {
                    BahanForm(
                        nama = it.nama_bahan,
                        jumlah = it.jumlah.toString(),
                        satuan = it.satuan
                    )
                }.toMutableList()

                langkahList = resep.Langkahs
                    .sortedBy { it.urutan }
                    .mapIndexed { index, l ->
                        LangkahForm(step = index + 1, deskripsi = l.deskripsi)
                    }.toMutableList()
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }
}

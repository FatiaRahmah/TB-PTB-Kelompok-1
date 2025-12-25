# Cara Menggunakan TagihanRepository

## Inisialisasi Repository

Di ViewModel atau tempat lain yang membutuhkan:

```kotlin
import com.example.rumafrontend.data.repository.TagihanRepository
import com.example.rumafrontend.data.database.AppDatabase

class TagihanViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val repository = TagihanRepository(
        tagihanDao = database.tagihanDao(),
        context = context
    )
    
    // Sekarang bisa gunakan repository untuk operasi CRUD
}
```

## Operasi CRUD dengan Auto-Sync

### 1. Create Tagihan
```kotlin
viewModelScope.launch {
    val newTagihan = TagihanEntity(
        title = "Listrik PLN",
        description = "Tagihan listrik bulan Desember",
        dueDateMillis = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 hari dari sekarang
        reminderDays = 3,
        repeatType = "Bulanan"
    )
    
    val result = repository.createTagihan(newTagihan)
    result.onSuccess { localId ->
        Log.d("TAG", "Tagihan created with local ID: $localId")
        // Data otomatis sync ke backend jika ada internet
    }.onFailure { error ->
        Log.e("TAG", "Failed to create tagihan: ${error.message}")
    }
}
```

### 2. Update Tagihan
```kotlin
viewModelScope.launch {
    val updatedTagihan = existingTagihan.copy(
        title = "Listrik PLN - Updated",
        description = "Tagihan sudah dibayar sebagian"
    )
    
    val result = repository.updateTagihan(updatedTagihan)
    result.onSuccess {
        Log.d("TAG", "Tagihan updated successfully")
        // Otomatis sync ke backend
    }
}
```

### 3. Tandai Lunas
```kotlin
viewModelScope.launch {
    val result = repository.tandaiLunas(
        id = tagihanId,
        buktiFotoPath = "/path/to/bukti.jpg" // optional
    )
    
    result.onSuccess {
        Log.d("TAG", "Tagihan marked as lunas")
    }
}
```

### 4. Delete Tagihan
```kotlin
viewModelScope.launch {
    val result = repository.deleteTagihan(tagihanId)
    result.onSuccess {
        Log.d("TAG", "Tagihan deleted")
        // Otomatis hapus dari backend juga
    }
}
```

### 5. Read Data (dari Local DB)
```kotlin
// Observe all tagihan
repository.getAllTagihan().collect { tagihanList ->
    // Update UI dengan data terbaru
    _tagihanState.value = tagihanList
}

// Observe by status
repository.getTagihanByStatus("belum").collect { unpaidBills ->
    // Show unpaid bills
}

// Get single tagihan
viewModelScope.launch {
    val tagihan = repository.getTagihanById(id)
    if (tagihan != null) {
        // Use tagihan data
    }
}
```

## Manual Sync

### Sync Unsynced Data ke Backend
```kotlin
viewModelScope.launch {
    val result = repository.syncUnsyncedData()
    result.onSuccess { syncedCount ->
        Log.d("TAG", "Synced $syncedCount tagihan to backend")
    }.onFailure { error ->
        Log.e("TAG", "Sync failed: ${error.message}")
    }
}
```

### Fetch Data dari Backend
```kotlin
viewModelScope.launch {
    val result = repository.fetchFromBackend()
    result.onSuccess { newCount ->
        Log.d("TAG", "Fetched $newCount new tagihan from backend")
    }.onFailure { error ->
        Log.e("TAG", "Fetch failed: ${error.message}")
    }
}
```

## Authentication

Sebelum menggunakan API, user harus login terlebih dahulu:

```kotlin
import com.example.rumafrontend.network.ApiClient
import com.example.rumafrontend.utils.TokenManager
import com.example.rumafrontend.data.model.loginRequest

// Di LoginViewModel atau LoginActivity
viewModelScope.launch {
    try {
        val request = loginRequest(
            email = "user@example.com",
            password = "password123"
        )
        
        val response = ApiClient.apiService.login(request)
        
        if (response.isSuccessful && response.body() != null) {
            val loginResponse = response.body()!!
            
            // Simpan token
            val tokenManager = TokenManager(context)
            tokenManager.saveToken(
                token = loginResponse.token,
                email = loginResponse.email
            )
            
            Log.d("TAG", "Login successful")
            // Sekarang bisa gunakan repository untuk sync data
        } else {
            Log.e("TAG", "Login failed: ${response.code()}")
        }
    } catch (e: Exception) {
        Log.e("TAG", "Login error: ${e.message}", e)
    }
}
```

## Strategi Offline-First

Repository menggunakan strategi offline-first:

1. **Create/Update/Delete**: Data disimpan ke local database dulu, lalu otomatis sync ke backend jika ada internet
2. **Read**: Selalu baca dari local database (cepat, bisa offline)
3. **Sync**: Bisa manual trigger sync atau otomatis saat ada internet
4. **Conflict Resolution**: Data dari backend akan merge dengan local data berdasarkan `serverId`

## Field Mapping

Backend dan Frontend menggunakan nama field yang berbeda:

| Backend Field | Frontend Field | Keterangan |
|--------------|----------------|------------|
| `tagihan_id` | `id` (local), `serverId` | ID di backend |
| `judul` | `title` | Judul tagihan |
| `deskripsi` | `description` | Deskripsi |
| `jatuh_tempo` | `dueDateMillis` | Tanggal jatuh tempo (backend: ISO string, frontend: milliseconds) |
| `bukti_foto` | `buktiFotoPath` | Path foto bukti |
| `reminder_days` | `reminderDays` | Hari reminder |
| `repeat_type` | `repeatType` | Tipe pengulangan |

Konversi otomatis dilakukan oleh extension functions di `TagihanResponse.kt`.

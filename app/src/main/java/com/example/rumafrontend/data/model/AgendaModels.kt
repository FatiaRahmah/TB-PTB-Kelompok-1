package com.example.rumafrontend.data.model

import com.example.rumafrontend.data.entity.Agenda

data class AgendaResponse(
    val id: Long,
    val judul: String,
    val kategori: String,
    val deskripsi: String,
    val date: String,           
    val time: String,           
    val location: String?,
    val reminder: String?,
    val isCompleted: Int        
) {
    fun toEntity(): Agenda {
        return Agenda(
            id = this.id,
            judul = this.judul,
            kategori = this.kategori,
            deskripsi = this.deskripsi,
            date = convertDateToLocal(this.date), 
            time = try { this.time.substring(0, 5) } catch (e: Exception) { this.time },
            location = this.location ?: "",
            reminder = this.reminder ?: "Tidak ada pengingat",
            isCompleted = this.isCompleted == 1
        )
    }
}

data class AgendaRequest(
    val kategori: String,
    val judul: String,
    val deskripsi: String,
    val date: String,      
    val time: String,      
    val location: String,
    val reminder: String?
)

data class AgendaCreateResponse(
    val message: String,
    val data: AgendaResponse
)

fun convertDateToServer(date: String): String {
    return try {
        if (date.contains("/")) {
            val parts = date.split("/")
            if (parts.size == 3) {
                
                "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
            } else date
        } else date
    } catch (e: Exception) {
        date
    }
}

fun convertDateToLocal(date: String): String {
    return try {
        if (date.contains("-")) {
            val parts = date.split("-")
            if (parts.size == 3) {
                
                "${parts[2]}/${parts[1]}/${parts[0]}"
            } else date
        } else date
    } catch (e: Exception) {
        date
    }
}

fun Agenda.toRequest(): AgendaRequest {
    return AgendaRequest(
        kategori = this.kategori,
        judul = this.judul,
        deskripsi = this.deskripsi,
        date = convertDateToServer(this.date), 
        time = this.time,
        location = this.location,
        reminder = if (this.reminder != "Tidak ada pengingat") this.reminder else null
    )
}

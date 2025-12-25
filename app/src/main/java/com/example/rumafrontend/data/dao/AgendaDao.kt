package com.example.rumafrontend.data.dao

import androidx.room.*
import com.example.rumafrontend.data.entity.Agenda
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agendas ORDER BY date ASC, time ASC")
    fun getAllAgendas(): Flow<List<Agenda>>

    @Query("SELECT * FROM agendas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Agenda?

    @Query("SELECT * FROM agendas WHERE date = :date ORDER BY time ASC")
    fun getAgendasByDate(date: String): Flow<List<Agenda>>

    @Query("SELECT * FROM agendas WHERE isCompleted = :isCompleted ORDER BY date DESC, time DESC")
    fun getAgendasByStatus(isCompleted: Boolean): Flow<List<Agenda>>

    
    @Query("SELECT * FROM agendas WHERE kategori = :category ORDER BY date DESC")
    fun getAgendasByCategory(category: String): Flow<List<Agenda>>

    @Query("SELECT * FROM agendas WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, time ASC")
    fun getAgendasBetweenDates(startDate: String, endDate: String): Flow<List<Agenda>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(agenda: Agenda): Long

    @Update
    suspend fun update(agenda: Agenda)

    @Delete
    suspend fun delete(agenda: Agenda)

    @Query("DELETE FROM agendas")
    suspend fun deleteAll()

    @Query("UPDATE agendas SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)
}

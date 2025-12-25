package com.example.rumafrontend.data.dao

import androidx.room.*
import com.example.rumafrontend.data.entity.NotifikasiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotifikasiDao {

    

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notifikasi: NotifikasiEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifikasiList: List<NotifikasiEntity>)

    

    @Query("SELECT * FROM notifikasi ORDER BY timestamp DESC")
    fun getAllNotifikasi(): Flow<List<NotifikasiEntity>>

    @Query("SELECT * FROM notifikasi WHERE id = :id")
    suspend fun getNotifikasiById(id: Int): NotifikasiEntity?

    @Query("SELECT * FROM notifikasi WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifikasi(): Flow<List<NotifikasiEntity>>

    @Query("SELECT * FROM notifikasi WHERE isRead = 1 ORDER BY timestamp DESC")
    fun getReadNotifikasi(): Flow<List<NotifikasiEntity>>

    @Query("SELECT * FROM notifikasi WHERE jenisNotifikasi = :jenis ORDER BY timestamp DESC")
    fun getNotifikasiByJenis(jenis: String): Flow<List<NotifikasiEntity>>

    

    @Query("""
        SELECT * FROM notifikasi 
        WHERE referenceId = :refId 
        AND referenceType = :refType 
        ORDER BY timestamp DESC
    """)
    suspend fun getNotifikasiByReference(refId: Int, refType: String): List<NotifikasiEntity>

    @Query("""
        SELECT * FROM notifikasi 
        WHERE referenceId = :refId 
        ORDER BY timestamp DESC
    """)
    fun getNotifikasiByReferenceId(refId: Int): Flow<List<NotifikasiEntity>>

    

    @Update
    suspend fun update(notifikasi: NotifikasiEntity)

    @Query("UPDATE notifikasi SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifikasi SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE notifikasi SET isRead = 0 WHERE id = :id")
    suspend fun markAsUnread(id: Int)

    

    @Delete
    suspend fun delete(notifikasi: NotifikasiEntity)

    @Query("DELETE FROM notifikasi WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notifikasi")
    suspend fun deleteAll()

    @Query("DELETE FROM notifikasi WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldNotifikasi(beforeTimestamp: Long)

    @Query("""
        DELETE FROM notifikasi 
        WHERE referenceId = :refId 
        AND referenceType = :refType
    """)
    suspend fun deleteByReference(refId: Int, refType: String)

    @Query("DELETE FROM notifikasi WHERE referenceId = :refId")
    suspend fun deleteByReferenceId(refId: Int)

    

    @Query("SELECT COUNT(*) FROM notifikasi WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifikasi")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifikasi WHERE jenisNotifikasi = :jenis")
    fun getCountByJenis(jenis: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM notifikasi 
        WHERE jenisNotifikasi = :jenis 
        AND isRead = 0
    """)
    fun getUnreadCountByJenis(jenis: String): Flow<Int>

    

    @Query("""
        SELECT * FROM notifikasi 
        WHERE timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    fun getNotifikasiByDateRange(startTime: Long, endTime: Long): Flow<List<NotifikasiEntity>>

    

    @Query("""
        DELETE FROM notifikasi 
        WHERE isRead = 1 
        AND timestamp < :beforeTimestamp
    """)
    suspend fun deleteOldReadNotifikasi(beforeTimestamp: Long)
}
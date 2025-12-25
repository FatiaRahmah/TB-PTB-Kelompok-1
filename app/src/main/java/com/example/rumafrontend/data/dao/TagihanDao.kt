package com.example.rumafrontend.data.dao

import androidx.room.*
import com.example.rumafrontend.data.entity.TagihanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagihanDao {

    

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tagihan: TagihanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tagihanList: List<TagihanEntity>)

    

    @Query("SELECT * FROM tagihan ORDER BY dueDateMillis ASC")
    fun getAllTagihan(): Flow<List<TagihanEntity>>

    @Query("SELECT * FROM tagihan WHERE status = :status ORDER BY dueDateMillis ASC")
    fun getTagihanByStatus(status: String): Flow<List<TagihanEntity>>

    @Query("SELECT * FROM tagihan WHERE id = :id")
    suspend fun getTagihanById(id: Int): TagihanEntity?

    @Query("SELECT * FROM tagihan WHERE id = :id")
    fun getTagihanByIdFlow(id: Int): Flow<TagihanEntity?>

    

    @Query("""
        SELECT * FROM tagihan 
        WHERE status = 'belum' AND dueDateMillis < :currentTime 
        ORDER BY dueDateMillis ASC
    """)
    fun getOverdueBills(currentTime: Long): Flow<List<TagihanEntity>>

    @Query("""
        SELECT * FROM tagihan 
        WHERE status = 'belum' AND dueDateMillis >= :currentTime 
        ORDER BY dueDateMillis ASC
    """)
    fun getUpcomingBills(currentTime: Long): Flow<List<TagihanEntity>>

    @Query("""
        SELECT COUNT(*) FROM tagihan 
        WHERE status = 'belum' AND dueDateMillis < :currentTime
    """)
    fun getOverdueCount(currentTime: Long): Flow<Int>

    

    @Query("""
        SELECT * FROM tagihan 
        WHERE title LIKE '%' || :query || '%' 
        AND status = :status
        ORDER BY dueDateMillis ASC
    """)
    fun searchTagihan(query: String, status: String): Flow<List<TagihanEntity>>

    @Query("""
        SELECT * FROM tagihan 
        WHERE dueDateMillis BETWEEN :startMillis AND :endMillis 
        ORDER BY dueDateMillis ASC
    """)
    fun getTagihanByDateRange(startMillis: Long, endMillis: Long): Flow<List<TagihanEntity>>

    

    @Query("""
        SELECT * FROM tagihan 
        WHERE status = 'lunas'
        AND (CAST(strftime('%m', dueDateMillis/1000, 'unixepoch') AS INTEGER) - 1) = :month
        AND CAST(strftime('%Y', dueDateMillis/1000, 'unixepoch') AS INTEGER) = :year
        ORDER BY dueDateMillis DESC
    """)
    fun getRiwayatByMonthYear(month: Int, year: Int): Flow<List<TagihanEntity>>

    @Query("""
        SELECT * FROM tagihan 
        WHERE status = 'lunas'
        AND CAST(strftime('%Y', dueDateMillis/1000, 'unixepoch') AS INTEGER) = :year
        ORDER BY dueDateMillis DESC
    """)
    fun getRiwayatByYear(year: Int): Flow<List<TagihanEntity>>

    @Query("""
        SELECT * FROM tagihan 
        WHERE status = 'lunas'
        AND (CAST(strftime('%m', dueDateMillis/1000, 'unixepoch') AS INTEGER) - 1) = :month
        ORDER BY dueDateMillis DESC
    """)
    fun getRiwayatByMonth(month: Int): Flow<List<TagihanEntity>>

    

    @Query("""
        SELECT * FROM tagihan 
        WHERE CAST(strftime('%Y', dueDateMillis/1000, 'unixepoch') AS INTEGER) = :year
        AND (CAST(strftime('%m', dueDateMillis/1000, 'unixepoch') AS INTEGER) - 1) = :month
        ORDER BY dueDateMillis ASC
    """)
    fun getTagihanByMonthYear(month: Int, year: Int): Flow<List<TagihanEntity>>

    @Query("""
        SELECT * FROM tagihan 
        WHERE CAST(strftime('%Y', dueDateMillis/1000, 'unixepoch') AS INTEGER) = :year
        AND (CAST(strftime('%m', dueDateMillis/1000, 'unixepoch') AS INTEGER) - 1) = :month
        AND CAST(strftime('%d', dueDateMillis/1000, 'unixepoch') AS INTEGER) = :day
        ORDER BY dueDateMillis ASC
    """)
    fun getTagihanByDate(year: Int, month: Int, day: Int): Flow<List<TagihanEntity>>

    

    @Update
    suspend fun update(tagihan: TagihanEntity)

    @Query("""
        UPDATE tagihan 
        SET status = :status, 
            tanggalSelesaiMillis = :tanggalSelesai,
            lastModified = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateStatus(
        id: Int, 
        status: String, 
        tanggalSelesai: Long? = null,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE tagihan 
        SET buktiFotoPath = :photoPath, lastModified = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateBuktiFoto(id: Int, photoPath: String?, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE tagihan 
        SET title = :title, 
            description = :description,
            dueDateMillis = :dueDateMillis,
            reminderDays = :reminderDays,
            repeatType = :repeatType,
            lastModified = :timestamp,
            isSynced = 0
        WHERE id = :id
    """)
    suspend fun updateTagihanDetails(
        id: Int,
        title: String,
        description: String?,
        dueDateMillis: Long,
        reminderDays: Int,
        repeatType: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE tagihan SET isSynced = :isSynced, serverId = :serverId WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, isSynced: Boolean, serverId: String?)

    

    @Delete
    suspend fun delete(tagihan: TagihanEntity)

    @Query("DELETE FROM tagihan WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM tagihan WHERE status = 'lunas' AND dueDateMillis < :beforeDate")
    suspend fun deleteOldCompletedBills(beforeDate: Long)

    @Query("DELETE FROM tagihan")
    suspend fun deleteAll()

    

    @Query("SELECT COUNT(*) FROM tagihan WHERE status = 'belum'")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tagihan WHERE status = 'lunas'")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tagihan")
    fun getTotalCount(): Flow<Int>

    

    @Query("SELECT * FROM tagihan WHERE isSynced = 0")
    suspend fun getUnsyncedTagihan(): List<TagihanEntity>

    @Query("SELECT * FROM tagihan WHERE serverId = :serverId")
    suspend fun getTagihanByServerId(serverId: String): TagihanEntity?

    @Query("SELECT MAX(lastModified) FROM tagihan")
    suspend fun getLastModifiedTimestamp(): Long?
}
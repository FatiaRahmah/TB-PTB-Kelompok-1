package com.example.rumafrontend.data.dao

import androidx.room.*
import com.example.rumafrontend.data.entity.Resep.BahanEntity
import com.example.rumafrontend.data.entity.Resep.LangkahEntity
import com.example.rumafrontend.data.entity.Resep.ResepEntity
import com.example.rumafrontend.data.entity.Resep.ResepWithDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface ResepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResep(resep: ResepEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBahan(listBahan: List<BahanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLangkah(listLangkah: List<LangkahEntity>)

    @Transaction
    suspend fun insertResepLengkap(resep: ResepEntity, bahan: List<BahanEntity>, langkah: List<LangkahEntity>) {
        val resepId = insertResep(resep).toInt()
        val bahanWithId = bahan.map { it.copy(resep_id = resepId) }
        val langkahWithId = langkah.map { it.copy(resep_id = resepId) }
        insertAllBahan(bahanWithId)
        insertAllLangkah(langkahWithId)
    }

    @Query("SELECT * FROM resep ORDER BY createdAt DESC")
    fun getAllResepFlow(): Flow<List<ResepEntity>>

    @Transaction
    @Query("SELECT * FROM resep WHERE id = :resep_id")
    suspend fun getResepWithDetail(resep_id: Int): ResepWithDetail?

    @Query("SELECT * FROM resep WHERE is_favorit = 1")
    fun getFavoritResepFlow(): Flow<List<ResepEntity>>

    @Query("UPDATE resep SET is_favorit = :is_favorit WHERE id = :resep_id")
    suspend fun updateFavoriteStatus(resep_id: Int, is_favorit: Boolean)

    @Delete
    suspend fun deleteResep(resep: ResepEntity)

}

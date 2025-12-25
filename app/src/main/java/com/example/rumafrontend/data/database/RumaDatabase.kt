package com.example.rumafrontend.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rumafrontend.data.dao.NotifikasiDao
import com.example.rumafrontend.data.dao.TagihanDao
import com.example.rumafrontend.data.dao.ResepDao
import com.example.rumafrontend.data.entity.NotifikasiEntity
import com.example.rumafrontend.data.entity.TagihanEntity
import com.example.rumafrontend.data.entity.Resep.ResepEntity
import com.example.rumafrontend.data.entity.Resep.BahanEntity
import com.example.rumafrontend.data.entity.Resep.LangkahEntity
import com.example.rumafrontend.data.entity.ShoppingItemEntity
import com.example.rumafrontend.data.entity.CategoryEntity
import com.example.rumafrontend.data.entity.ItemEntity
import com.example.rumafrontend.data.dao.ShoppingItemDao
import com.example.rumafrontend.data.dao.CategoryDao
import com.example.rumafrontend.data.dao.ItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TagihanEntity::class,
        NotifikasiEntity::class,
        com.example.rumafrontend.data.entity.Agenda::class,
        com.example.rumafrontend.data.entity.UserEntity::class,
        ResepEntity::class,
        BahanEntity::class,
        LangkahEntity::class,
        ShoppingItemEntity::class,
        CategoryEntity::class,
        ItemEntity::class
    ],
    version = 6, 

    exportSchema = false
)
abstract class RumaDatabase : RoomDatabase() {

    abstract fun tagihanDao(): TagihanDao
    abstract fun notifikasiDao(): NotifikasiDao
    abstract fun agendaDao(): com.example.rumafrontend.data.dao.AgendaDao 
    abstract fun userDao(): com.example.rumafrontend.data.dao.UserDao
    abstract fun resepDao(): ResepDao

    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun categoryDao(): CategoryDao

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: RumaDatabase? = null

        fun getDatabase(context: Context): RumaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RumaDatabase::class.java,
                    "ruma_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        
        fun clearInstance() {
            INSTANCE = null
        }
    }

    
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.tagihanDao())
                }
            }
        }

        private suspend fun populateDatabase(tagihanDao: TagihanDao) {
            
            

            
            
        }
    }
}
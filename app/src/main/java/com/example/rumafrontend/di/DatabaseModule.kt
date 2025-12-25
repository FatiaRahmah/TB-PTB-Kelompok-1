package com.example.rumafrontend.di

import android.content.Context
import com.example.rumafrontend.data.database.RumaDatabase
import com.example.rumafrontend.data.dao.ResepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): RumaDatabase {
        return RumaDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideResepDao(database: RumaDatabase): ResepDao {
        return database.resepDao()
    }
}

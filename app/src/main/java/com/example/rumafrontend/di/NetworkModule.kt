package com.example.rumafrontend.di

import com.example.rumafrontend.network.ApiService
import com.example.rumafrontend.Repository.ResepRepository
import com.example.rumafrontend.Repository.UserRepository
import com.example.rumafrontend.utils.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.4:3000/") 
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideResepRepository(
        apiService: ApiService,
        tokenManager: TokenManager
    ): ResepRepository {
        return ResepRepository(apiService, tokenManager)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        tokenManager: TokenManager
    ): UserRepository {
        return UserRepository(apiService, tokenManager)
    }
}

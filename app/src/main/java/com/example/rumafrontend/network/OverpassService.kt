package com.example.rumafrontend.network

import com.example.rumafrontend.data.model.OverpassResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassService {

    @GET("api/interpreter")
    suspend fun searchStores(
        @Query("data") query: String
    ): OverpassResponse
}
package com.example.rumafrontend.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OverpassClient {

    private const val BASE_URL = "https://overpass-api.de/"

    val api: OverpassService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassService::class.java)
    }
}
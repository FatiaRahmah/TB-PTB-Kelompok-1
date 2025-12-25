package com.example.rumafrontend.network

import com.example.rumafrontend.data.model.*
import com.example.rumafrontend.data.remote.profileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    
    
    
    @GET("tagihan")
    suspend fun getAllTagihan(): Response<List<TagihanResponse>>
    
    
    @GET("tagihan/{id}")
    suspend fun getTagihanById(@Path("id") id: Int): Response<TagihanResponse>
    
    
    @POST("tagihan")
    suspend fun createTagihan(@Body request: TagihanRequest): Response<CreateTagihanResponse>
    
    
    @PUT("tagihan/{id}")
    suspend fun updateTagihan(
        @Path("id") id: Int,
        @Body request: TagihanRequest
    ): Response<MessageResponse>
    
    
    @DELETE("tagihan/{id}")
    suspend fun deleteTagihan(@Path("id") id: Int): Response<MessageResponse>

    

    @GET("agenda")
    suspend fun getAllAgendas(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("date") date: String? = null
    ): Response<List<AgendaResponse>>

    @GET("agenda/{id}")
    suspend fun getAgendaDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<AgendaResponse>

    @POST("agenda")
    suspend fun createAgenda(
        @Header("Authorization") token: String,
        @Body body: AgendaRequest
    ): Response<AgendaCreateResponse>

    @PUT("agenda/{id}")
    suspend fun updateAgenda(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: AgendaRequest
    ): Response<MessageResponse>

    @DELETE("agenda/{id}")
    suspend fun deleteAgenda(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<MessageResponse>

    
    
    
    @Multipart
    @PUT("tagihan/{id}/lunas")
    suspend fun tandaiLunas(
        @Path("id") id: Int,
        @Part("tanggal_selesai") tanggalSelesai: RequestBody,
        @Part bukti_foto: MultipartBody.Part?
    ): Response<MessageResponse>

    
    @PUT("tagihan/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest
    ): Response<MessageResponse>

    
    @Multipart
    @PUT("tagihan/{id}/foto")
    suspend fun updateFoto(
        @Path("id") id: Int,
        @Part bukti_foto: MultipartBody.Part
    ): Response<MessageResponse>

    
    
    
    @GET("notifikasi")
    suspend fun getAllNotifikasi(): Response<List<NotifikasiResponse>>
    
    @POST("notifikasi")
    suspend fun createNotifikasi(@Body request: NotifikasiRequest): Response<Any>
    
    @PUT("notifikasi/{id}/read")
    suspend fun markNotifikasiRead(@Path("id") id: Int): Response<Any>

    
    
    @GET("api/resep")
    suspend fun getAllResep(
        @Header("Authorization") authHeader: String,
        @Query("search") search: String? = null
    ): List<com.example.rumafrontend.data.remote.ResepResponse>

    @POST("api/resep")
    suspend fun createResep(
        @Header("Authorization") authHeader: String,
        @Body request: com.example.rumafrontend.data.remote.CreateResepRequest
    ): Response<com.example.rumafrontend.data.remote.ResepResponse>

    @GET("api/resep/favorit")
    suspend fun getFavoritResep(
        @Header("Authorization") authHeader: String
    ): List<com.example.rumafrontend.data.remote.ResepResponse>

    @GET("api/resep/{id}")
    suspend fun getResepById(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): com.example.rumafrontend.data.remote.ResepResponse

    @DELETE("api/resep/{id}")
    suspend fun deleteResep(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): Response<Unit>

    @PATCH("api/resep/{id}/favorit")
    suspend fun toggleFavorit(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Int
    ): com.example.rumafrontend.data.remote.ResepResponse

    @PUT("api/resep/{id}")
    suspend fun updateResep(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: com.example.rumafrontend.data.remote.CreateResepRequest
    ): Response<com.example.rumafrontend.data.remote.ResepResponse>

    @GET("api/auth/me")
    suspend fun getProfile(
        @Header("Authorization") authHeader: String
    ): Response<profileResponse>

    @Multipart
    @PUT("api/users/{id}")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Path("id") userId: Int,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody?,
        @Part foto_profil: MultipartBody.Part?
    ): Response<profileResponse>

    
    
    @GET("shopping-lists")
    suspend fun getShoppingLists(): List<ShoppingListResponse>

    @GET("shopping-lists/{id}")
    suspend fun getDetail(
        @Path("id") id: Int
    ): ShoppingListDetailResponse

    @POST("shopping-lists")
    suspend fun createShoppingList(
        @Body body: CreateShoppingListRequest
    ): ShoppingListResponse

    @POST("shopping-lists/categories")
    suspend fun createCategory(
        @Body body: CreateCategoryRequest
    ): Any

    @POST("shopping-lists/items")
    suspend fun createItem(
        @Body body: CreateItemRequest
    ): Any

    @PUT("shopping-lists/items/{id}")
    suspend fun updateItemElement(
        @Path("id") id: Int,
        @Body body: UpdateItemRequest
    ): Any
}
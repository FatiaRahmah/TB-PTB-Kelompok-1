package com.example.rumafrontend.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("error")
    val error: String? = null
)

data class CreateTagihanResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("newTagihan")
    val newTagihan: TagihanResponse
)

data class MessageResponse(
    @SerializedName("message")
    val message: String
)

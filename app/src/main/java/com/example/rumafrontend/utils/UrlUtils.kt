package com.example.rumafrontend.utils

import com.example.rumafrontend.network.ApiClient

object UrlUtils {
    
    fun getPhotoUrl(path: String?): String? {
        if (path == null) return null
        
        return if (path.startsWith("uploads/")) {
            
            val baseUrl = "http://192.168.100.5:3000/" 
            baseUrl + path
        } else {
            path
        }
    }
}

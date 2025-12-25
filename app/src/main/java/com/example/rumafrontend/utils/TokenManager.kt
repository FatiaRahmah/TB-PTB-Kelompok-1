package com.example.rumafrontend.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "ruma_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }
    
    
    fun saveToken(token: String) {
        val cleanToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else {
            token
        }
        prefs.edit().putString(KEY_TOKEN, cleanToken).apply()
    }

    
    fun saveUserData(email: String, userId: Int, username: String) {
        prefs.edit().apply {
            putString(KEY_EMAIL, email)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            apply()
        }
    }
    
    
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    
    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }
    
    
    fun clearToken() {
        prefs.edit().clear().apply()
    }
    
    
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    
    fun isTokenExists(): Boolean {
        return getToken() != null
    }
}

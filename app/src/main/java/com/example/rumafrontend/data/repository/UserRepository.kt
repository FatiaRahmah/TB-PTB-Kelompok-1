package com.example.rumafrontend.data.repository

import com.example.rumafrontend.data.dao.UserDao
import com.example.rumafrontend.data.entity.UserEntity

import kotlinx.coroutines.flow.first

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun insert(user: UserEntity): Long {
        return userDao.insert(user)
    }

    suspend fun login(email: String, password: String): UserEntity? {
        return userDao.login(email, password)
    }

    
    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers().first() 
    }
}

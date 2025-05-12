package com.marketplace.domain.repository

import com.marketplace.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, name: String): Result<User>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
} 
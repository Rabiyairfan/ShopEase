package com.marketplace.domain.repository

import com.marketplace.domain.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    fun getUserById(userId: String): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(userId: String): Result<Unit>
    
    suspend fun updateUserProfile(
        userId: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null
    ): Result<User>
    
    suspend fun updateUserPreferences(
        userId: String,
        preferences: Map<String, Any>
    ): Result<User>
    
    suspend fun addUserToFavorites(userId: String, favoriteUserId: String): Result<User>
    suspend fun removeUserFromFavorites(userId: String, favoriteUserId: String): Result<User>
    
    suspend fun blockUser(userId: String, blockedUserId: String): Result<User>
    suspend fun unblockUser(userId: String, blockedUserId: String): Result<User>
    
    suspend fun searchUsers(query: String): Flow<List<User>>
    suspend fun getUsersByRole(role: String): Flow<List<User>>
} 
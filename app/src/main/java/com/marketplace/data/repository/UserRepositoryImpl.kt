package com.marketplace.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marketplace.domain.models.User
import com.marketplace.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(null)
            return@callbackFlow
        }

        val listener = usersCollection.document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val user = snapshot?.toObject(User::class.java)?.copy(id = snapshot.id)
                trySend(user)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getUserById(userId: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val user = snapshot?.toObject(User::class.java)?.copy(id = snapshot.id)
                trySend(user)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(users)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun createUser(user: User): Result<User> = try {
        val docRef = usersCollection.document(user.id)
        val newUser = user.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        docRef.set(newUser).await()
        Result.success(newUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUser(user: User): Result<User> = try {
        val userRef = usersCollection.document(user.id)
        val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> = try {
        usersCollection.document(userId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserProfile(
        userId: String,
        name: String?,
        email: String?,
        phone: String?,
        address: String?
    ): Result<User> = try {
        val userRef = usersCollection.document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
            
        val updatedUser = user.copy(
            name = name ?: user.name,
            email = email ?: user.email,
            phone = phone ?: user.phone,
            address = address ?: user.address,
            updatedAt = System.currentTimeMillis()
        )
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserPreferences(
        userId: String,
        preferences: Map<String, Any>
    ): Result<User> = try {
        val userRef = usersCollection.document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
            
        val updatedUser = user.copy(
            preferences = preferences,
            updatedAt = System.currentTimeMillis()
        )
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addUserToFavorites(
        userId: String,
        favoriteUserId: String
    ): Result<User> = try {
        val userRef = usersCollection.document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
            
        val updatedFavorites = user.favorites.toMutableList().apply {
            if (!contains(favoriteUserId)) {
                add(favoriteUserId)
            }
        }
        
        val updatedUser = user.copy(
            favorites = updatedFavorites,
            updatedAt = System.currentTimeMillis()
        )
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeUserFromFavorites(
        userId: String,
        favoriteUserId: String
    ): Result<User> = try {
        val userRef = usersCollection.document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
            
        val updatedFavorites = user.favorites.toMutableList().apply {
            remove(favoriteUserId)
        }
        
        val updatedUser = user.copy(
            favorites = updatedFavorites,
            updatedAt = System.currentTimeMillis()
        )
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun blockUser(
        userId: String,
        blockedUserId: String
    ): Result<User> = try {
        val userRef = usersCollection.document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
            
        val updatedBlockedUsers = user.blockedUsers.toMutableList().apply {
            if (!contains(blockedUserId)) {
                add(blockedUserId)
            }
        }
        
        val updatedUser = user.copy(
            blockedUsers = updatedBlockedUsers,
            updatedAt = System.currentTimeMillis()
        )
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun unblockUser(
        userId: String,
        blockedUserId: String
    ): Result<User> = try {
        val userRef = usersCollection.document(userId)
        val user = userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
            
        val updatedBlockedUsers = user.blockedUsers.toMutableList().apply {
            remove(blockedUserId)
        }
        
        val updatedUser = user.copy(
            blockedUsers = updatedBlockedUsers,
            updatedAt = System.currentTimeMillis()
        )
        
        userRef.set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun searchUsers(query: String): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(users)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun getUsersByRole(role: String): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .whereEqualTo("role", role)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(users)
            }
            
        awaitClose { listener.remove() }
    }
} 
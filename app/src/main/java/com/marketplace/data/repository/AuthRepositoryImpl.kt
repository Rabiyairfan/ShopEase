package com.marketplace.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.marketplace.domain.models.User
import com.marketplace.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        Result.success(result.user?.toUser() ?: throw Exception("User not found"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("User creation failed")
        
        val newUser = User(
            id = user.uid,
            name = name,
            email = email
        )
        
        firestore.collection("users")
            .document(user.uid)
            .set(newUser)
            .await()
            
        Result.success(newUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProfile(user: User): Result<User> = try {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        
        firestore.collection("users")
            .document(currentUser.uid)
            .set(user)
            .await()
            
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        
        // Reauthenticate user
        val credential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential).await()
        
        // Update password
        user.updatePassword(newPassword).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        
        // Delete user data from Firestore
        firestore.collection("users")
            .document(user.uid)
            .delete()
            .await()
            
        // Delete user account
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun FirebaseUser.toUser(): User? {
        return User(
            id = uid,
            email = email ?: "",
            name = displayName ?: ""
        )
    }
} 
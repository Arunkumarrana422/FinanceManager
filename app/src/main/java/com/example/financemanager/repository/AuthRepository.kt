package com.example.financemanager.repository

import com.example.financemanager.firebase.FirebaseModule
import com.example.financemanager.firebase.FirestorePaths
import com.example.financemanager.model.User
import com.example.financemanager.utils.Resource
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseModule.auth
    private val firestore = FirebaseModule.firestore

    fun currentUser() = auth.currentUser
    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signUp(name: String, email: String, password: String): Resource<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: return Resource.Error("Sign up failed")
        val user = User(uid = uid, name = name, email = email)
        firestore.collection(FirestorePaths.USERS).document(uid).set(user).await()
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Sign up failed")
    }

    suspend fun login(email: String, password: String): Resource<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: return Resource.Error("Login failed")
        val snapshot = firestore.collection(FirestorePaths.USERS).document(uid).get().await()
        val user = snapshot.toObject(User::class.java) ?: User(uid = uid, email = email)
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Invalid email or password")
    }

    suspend fun sendPasswordReset(email: String): Resource<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not send reset email")
    }

    suspend fun fetchUserProfile(uid: String): Resource<User> = try {
        val snapshot = firestore.collection(FirestorePaths.USERS).document(uid).get().await()
        val user = snapshot.toObject(User::class.java) ?: return Resource.Error("Profile not found")
        Resource.Success(user)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not load profile")
    }

    suspend fun updateProfile(uid: String, name: String, currency: String): Resource<Unit> = try {
        firestore.collection(FirestorePaths.USERS).document(uid)
            .update(mapOf("name" to name, "currency" to currency)).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not update profile")
    }

    suspend fun changePassword(newPassword: String): Resource<Unit> = try {
        auth.currentUser?.updatePassword(newPassword)?.await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not change password")
    }

    fun logout() = auth.signOut()
}

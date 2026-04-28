package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(name: String, email: String, password: String): Result<Unit> {
        return try {
            // 1. Tạo tài khoản trong Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User creation failed")

            // 2. Thêm thông tin người dùng vào Firestore với quyền USER mặc định
            val user = User(
                id = userId,
                name = name,
                email = email,
                memberSince = "2024",
                role = UserRole.USER // Mặc định là USER
            )
            
            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Lấy thông tin user hiện tại bao gồm cả Role
    suspend fun getCurrentUserData(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val userId = user.uid

            // 1. Xóa thông tin trong Firestore
            firestore.collection("users").document(userId).delete().await()

            // 2. Xóa tài khoản trong Firebase Auth
            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

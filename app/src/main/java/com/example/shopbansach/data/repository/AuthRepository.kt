package com.example.shopbansach.data.repository

import android.net.Uri
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun registerUser(name: String, email: String, password: String): Result<Unit> {
        var userId: String? = null
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            userId = authResult.user?.uid ?: throw Exception("Lỗi tạo user")

            val user = User(
                id = userId,
                name = name,
                email = email,
                shopName = name,
                memberSince = "2024",
                role = UserRole.USER
            )
            
            firestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (userId != null) {
                try { auth.currentUser?.delete()?.await() } catch (ignore: Exception) {}
            }
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

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserData(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserById(userId: String): User? {
        if (userId.isEmpty()) return null
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, avatarUrl: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("name" to name)
            avatarUrl?.let { updates["avatarUrl"] = it }
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateShopName(userId: String, shopName: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            batch.update(firestore.collection("users").document(userId), "shopName", shopName)
            
            val books = firestore.collection("books").whereEqualTo("ownerId", userId).get().await()
            for (doc in books.documents) batch.update(doc.reference, "shopName", shopName)
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShopAvatarUrl(userId: String, shopAvatarUrl: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            batch.update(firestore.collection("users").document(userId), "shopAvatarUrl", shopAvatarUrl)
            
            val books = firestore.collection("books").whereEqualTo("ownerId", userId).get().await()
            for (doc in books.documents) batch.update(doc.reference, "shopAvatarUrl", shopAvatarUrl)
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(userId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("avatars/$userId.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Chưa đăng nhập")
            val email = user.email ?: throw Exception("Thiếu email")
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() { auth.signOut() }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Chưa đăng nhập")
            val userId = user.uid
            cleanUpUserData(userId)
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun cleanUpUserData(userId: String) {
        val batch = firestore.batch()
        val books = firestore.collection("books").whereEqualTo("ownerId", userId).get().await()
        for (doc in books.documents) batch.delete(doc.reference)
        batch.delete(firestore.collection("users").document(userId))
        batch.commit().await()
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).update("role", newRole.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserByAdmin(userId: String): Result<Unit> {
        return try {
            cleanUpUserData(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

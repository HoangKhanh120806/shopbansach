package com.example.shopbansach.data.repository

import android.net.Uri
import com.example.shopbansach.data.model.Book
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
            userId = authResult.user?.uid ?: throw Exception("User creation failed")

            val user = User(
                id = userId,
                name = name,
                email = email,
                memberSince = "2024",
                role = UserRole.USER
            )
            
            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            if (userId != null) {
                try {
                    auth.currentUser?.delete()?.await()
                } catch (deleteError: Exception) {
                }
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
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (!snapshot.exists()) return null
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserById(userId: String): User? {
        if (userId.isEmpty()) return null
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            if (!snapshot.exists()) return null
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, avatarUrl: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("name" to name)
            if (avatarUrl != null) {
                updates["avatarUrl"] = avatarUrl
            }
            
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateShopName(userId: String, shopName: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("shopName", shopName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShopAvatarUrl(userId: String, shopAvatarUrl: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("shopAvatarUrl", shopAvatarUrl)
                .await()
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

    suspend fun uploadShopAvatar(userId: String, imageUri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("shop_avatars/$userId.jpg")
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
            val email = user.email ?: throw Exception("Không tìm thấy email")
            
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    /**
     * Tự xóa tài khoản và dọn dẹp sách + ảnh của chính mình
     * @param cloudinaryRepository Cần truyền vào để xóa ảnh thực tế
     */
    suspend fun deleteAccount(cloudinaryRepository: CloudinaryRepository? = null): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val userId = user.uid
            
            // 1. Dọn dẹp sách và ảnh
            deleteAllUserBooksAndImages(userId, cloudinaryRepository)
            
            // 2. Xóa thông tin user trong Firestore
            firestore.collection("users").document(userId).delete().await()
            
            // 3. Xóa tài khoản Auth
            user.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): List<User> {
        val snapshot = firestore.collection("users").get().await()
        return snapshot.toObjects(User::class.java)
    }

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("role", newRole)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin xóa người dùng và dọn dẹp luôn các sách + ảnh của người đó
     */
    suspend fun deleteUserByAdmin(userId: String, cloudinaryRepository: CloudinaryRepository? = null): Result<Unit> {
        return try {
            // 1. Dọn dẹp sách và ảnh
            deleteAllUserBooksAndImages(userId, cloudinaryRepository)
            
            // 2. Xóa thông tin user trong Firestore
            firestore.collection("users").document(userId).delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hàm dọn dẹp: Tìm tất cả sách, xóa ảnh trên Cloudinary, sau đó xóa document trên Firestore
     */
    private suspend fun deleteAllUserBooksAndImages(userId: String, cloudinaryRepository: CloudinaryRepository?) {
        try {
            val booksSnapshot = firestore.collection("books")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
            
            if (!booksSnapshot.isEmpty) {
                val batch = firestore.batch()
                for (doc in booksSnapshot.documents) {
                    val book = doc.toObject(Book::class.java)
                    
                    // Xóa ảnh trên Cloudinary nếu có publicId và repository
                    book?.imagePublicId?.let { publicId ->
                        cloudinaryRepository?.deleteImage(publicId)
                    }
                    
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Log lỗi dọn dẹp nếu cần
        }
    }
}

package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseBookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val booksCollection = firestore.collection("books")

    suspend fun getAllBooks(limit: Long = 50): List<Book> {
        return try {
            val snapshot = booksCollection.limit(limit).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFeaturedBooks(): List<Book> {
        return try {
            val snapshot = booksCollection
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            val snapshot = booksCollection.limit(10).get().await()
            snapshot.toObjects(Book::class.java)
        }
    }

    suspend fun getNewArrivals(): List<Book> {
        return try {
            val snapshot = booksCollection.limit(20).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBookById(id: String): Book? {
        if (id.isEmpty()) return null
        return try {
            val document = booksCollection.document(id).get().await()
            document.toObject(Book::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchBooks(queryText: String): List<Book> {
        if (queryText.isEmpty()) return emptyList()
        val queryLower = queryText.lowercase(Locale.ROOT)
        return try {
            val snapshot = booksCollection
                .whereGreaterThanOrEqualTo("titleLowercase", queryLower)
                .whereLessThanOrEqualTo("titleLowercase", queryLower + "\uf8ff")
                .limit(20)
                .get().await()
            val results = snapshot.toObjects(Book::class.java)
            if (results.isEmpty()) manualSearchFallback(queryText) else results
        } catch (e: Exception) {
            manualSearchFallback(queryText)
        }
    }
    
    private suspend fun manualSearchFallback(queryText: String): List<Book> {
        return try {
            val snapshot = booksCollection.limit(100).get().await()
            val allBooks = snapshot.toObjects(Book::class.java)
            allBooks.filter { 
                it.title.contains(queryText, ignoreCase = true) || 
                it.author.contains(queryText, ignoreCase = true) 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBooksByOwner(ownerId: String): List<Book> {
        return try {
            val snapshot = booksCollection.whereEqualTo("ownerId", ownerId).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addBook(book: Book): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")
            val docRef = booksCollection.document(book.id)
            val existingDoc = docRef.get().await()

            if (existingDoc.exists()) {
                val ownerId = existingDoc.getString("ownerId")
                if (ownerId != currentUserId) {
                    // Kiểm tra xem có phải Admin không
                    val userSnapshot = firestore.collection("users").document(currentUserId).get().await()
                    val role = userSnapshot.getString("role")
                    if (role != UserRole.ADMIN.name) {
                        throw Exception("Bạn không có quyền chỉnh sửa sách này")
                    }
                }
            }
            
            docRef.set(book).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStockWithCheck(bookId: String, quantityPurchased: Int): Result<Unit> {
        return try {
            val bookRef = booksCollection.document(bookId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(bookRef)
                val currentStock = snapshot.getLong("stock") ?: 0L
                if (currentStock < quantityPurchased) {
                    throw Exception("Sản phẩm '${snapshot.getString("title")}' đã hết hàng")
                }
                transaction.update(bookRef, "stock", currentStock - quantityPurchased)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Xóa sách an toàn: Kiểm tra quyền sở hữu hoặc quyền Admin
     * @param cloudinaryRepository Nếu truyền vào sẽ dọn dẹp ảnh trên Cloudinary
     */
    suspend fun deleteBook(bookId: String, cloudinaryRepository: CloudinaryRepository? = null): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")
            val docRef = booksCollection.document(bookId)
            val existingDoc = docRef.get().await()

            if (existingDoc.exists()) {
                val book = existingDoc.toObject(Book::class.java)
                val ownerId = book?.ownerId
                
                var isAuthorized = ownerId == currentUserId
                
                if (!isAuthorized) {
                    // Kiểm tra quyền Admin
                    val userSnapshot = firestore.collection("users").document(currentUserId).get().await()
                    val role = userSnapshot.getString("role")
                    isAuthorized = role == UserRole.ADMIN.name
                }

                if (!isAuthorized) {
                    throw Exception("Bạn không có quyền xóa sách này")
                }

                // Dọn dẹp ảnh trên Cloudinary
                book?.imagePublicId?.let { publicId ->
                    cloudinaryRepository?.deleteImage(publicId)
                }

                docRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

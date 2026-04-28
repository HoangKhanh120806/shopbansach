package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseBookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val booksCollection = firestore.collection("books")

    // Lấy tất cả sách (có giới hạn để tránh tốn băng thông)
    suspend fun getAllBooks(limit: Long = 50): List<Book> {
        return try {
            val snapshot = booksCollection.limit(limit).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lấy sách nổi bật (Featured)
    suspend fun getFeaturedBooks(): List<Book> {
        return try {
            // Ưu tiên lấy theo rating cao
            val snapshot = booksCollection
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            // Fallback nếu chưa tạo index cho rating
            val snapshot = booksCollection.limit(10).get().await()
            snapshot.toObjects(Book::class.java)
        }
    }

    // Lấy sách mới cập nhật
    suspend fun getNewArrivals(): List<Book> {
        return try {
            val snapshot = booksCollection.limit(20).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Tìm sách theo ID
    suspend fun getBookById(id: String): Book? {
        if (id.isEmpty()) return null
        return try {
            val document = booksCollection.document(id).get().await()
            document.toObject(Book::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tối ưu tìm kiếm: 
     * Sử dụng prefix query trên field titleLowercase để hỗ trợ tìm kiếm không phân biệt hoa thường.
     */
    suspend fun searchBooks(queryText: String): List<Book> {
        if (queryText.isEmpty()) return emptyList()
        val queryLower = queryText.lowercase(Locale.ROOT)
        
        return try {
            // Tìm kiếm theo tiền tố của titleLowercase
            val snapshot = booksCollection
                .whereGreaterThanOrEqualTo("titleLowercase", queryLower)
                .whereLessThanOrEqualTo("titleLowercase", queryLower + "\uf8ff")
                .limit(20)
                .get().await()
            
            val results = snapshot.toObjects(Book::class.java)
            
            // Nếu không có kết quả, dùng fallback để tìm kiếm chứa chuỗi (contains) trên dữ liệu nhỏ
            if (results.isEmpty()) {
                manualSearchFallback(queryText)
            } else {
                results
            }
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

    // Lấy sách của một người dùng cụ thể (Shop của tôi)
    suspend fun getBooksByOwner(ownerId: String): List<Book> {
        return try {
            val snapshot = booksCollection.whereEqualTo("ownerId", ownerId).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lưu sách mới
    suspend fun addBook(book: Book) {
        try {
            booksCollection.document(book.id).set(book).await()
        } catch (e: Exception) {
            // Log error
        }
    }
}

package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Book
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseBookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val booksCollection = firestore.collection("books")

    // Lấy tất cả sách
    suspend fun getAllBooks(): List<Book> {
        return try {
            val snapshot = booksCollection.get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lấy sách nổi bật (Featured)
    suspend fun getFeaturedBooks(): List<Book> {
        return try {
            // Lấy 5 cuốn sách bất kỳ làm nổi bật nếu không có field isFeatured
            val snapshot = booksCollection.limit(5).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lấy sách mới cập nhật
    suspend fun getNewArrivals(): List<Book> {
        return try {
            // Sắp xếp theo ID hoặc một field timestamp nếu có
            val snapshot = booksCollection.limit(10).get().await()
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

    // Tìm kiếm sách
    suspend fun searchBooks(query: String): List<Book> {
        if (query.isEmpty()) return emptyList()
        return try {
            val snapshot = booksCollection.get().await()
            val allBooks = snapshot.toObjects(Book::class.java)
            allBooks.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.author.contains(query, ignoreCase = true) 
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

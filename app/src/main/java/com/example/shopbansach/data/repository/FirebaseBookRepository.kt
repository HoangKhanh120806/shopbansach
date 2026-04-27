package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Book
import com.google.firebase.firestore.FirebaseFirestore
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
            val snapshot = booksCollection.whereEqualTo("isFeatured", true).limit(5).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Tìm sách theo ID
    suspend fun getBookById(id: String): Book? {
        return try {
            val document = booksCollection.document(id).get().await()
            document.toObject(Book::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Lưu sách mới (Dùng để khởi tạo dữ liệu ban đầu nếu cần)
    suspend fun addBook(book: Book) {
        try {
            booksCollection.document(book.id.toString()).set(book).await()
        } catch (e: Exception) {
            // Log error
        }
    }
}

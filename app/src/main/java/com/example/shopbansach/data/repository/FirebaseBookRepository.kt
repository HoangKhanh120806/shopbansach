package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseBookRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val booksCollection = firestore.collection("books")

    data class BookPage(
        val books: List<Book>,
        val lastDocument: DocumentSnapshot?,
        val isLastPage: Boolean
    )

    suspend fun getTotalBooksCount(): Long {
        return try {
            val query = booksCollection.count()
            val snapshot = query.get(AggregateSource.SERVER).await()
            snapshot.count
        } catch (e: Exception) {
            0L
        }
    }

    suspend fun getAllBooksPaged(limit: Long, lastDocument: DocumentSnapshot? = null): BookPage {
        return try {
            var query = booksCollection.orderBy("title").limit(limit)
            if (lastDocument != null) {
                query = query.startAfter(lastDocument)
            }
            val snapshot = query.get().await()
            val books = snapshot.toObjects(Book::class.java)
            val lastVisible = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
            val isLastPage = snapshot.size() < limit
            BookPage(books, lastVisible, isLastPage)
        } catch (e: Exception) {
            BookPage(emptyList(), null, true)
        }
    }

    suspend fun getLastPage(limit: Long): BookPage {
        return try {
            val query = booksCollection.orderBy("title", Query.Direction.DESCENDING).limit(limit)
            val snapshot = query.get().await()
            val books = snapshot.toObjects(Book::class.java).reversed()
            BookPage(books, null, true)
        } catch (e: Exception) {
            BookPage(emptyList(), null, true)
        }
    }

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

    suspend fun getBooksByIds(ids: List<String>): List<Book> {
        if (ids.isEmpty()) return emptyList()
        return try {
            val validIds = ids.filter { it.isNotEmpty() }.distinct()
            if (validIds.isEmpty()) return emptyList()
            val chunks = validIds.chunked(30)
            val allBooks = mutableListOf<Book>()
            for (chunk in chunks) {
                val snapshot = booksCollection.whereIn(FieldPath.documentId(), chunk).get().await()
                allBooks.addAll(snapshot.toObjects(Book::class.java))
            }
            allBooks
        } catch (e: Exception) {
            emptyList()
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
    
    suspend fun getBooksByCategory(category: String): List<Book> {
        return try {
            val snapshot = booksCollection.whereEqualTo("category", category).limit(50).get().await()
            snapshot.toObjects(Book::class.java)
        } catch (e: Exception) {
            manualSearchFallback(category)
        }
    }
    
    private suspend fun manualSearchFallback(queryText: String): List<Book> {
        return try {
            val snapshot = booksCollection.limit(100).get().await()
            val allBooks = snapshot.toObjects(Book::class.java)
            allBooks.filter { 
                it.title.contains(queryText, ignoreCase = true) || 
                it.author.contains(queryText, ignoreCase = true) ||
                it.category.contains(queryText, ignoreCase = true)
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
            val userSnapshot = firestore.collection("users").document(currentUserId).get().await()
            val currentUser = userSnapshot.toObject(User::class.java)
            
            val shopName = currentUser?.shopName?.ifEmpty { currentUser.name } ?: currentUser?.name ?: "Người bán"
            val shopAvatar = currentUser?.shopAvatarUrl ?: currentUser?.avatarUrl

            val docRef = booksCollection.document(book.id)
            val existingDoc = docRef.get().await()

            if (existingDoc.exists()) {
                val ownerId = existingDoc.getString("ownerId")
                val role = userSnapshot.getString("role")
                if (ownerId != currentUserId && role != "ADMIN") throw Exception("Không có quyền")
                
                val updates = mapOf(
                    "title" to book.title,
                    "titleLowercase" to book.title.lowercase(Locale.ROOT),
                    "author" to book.author,
                    "price" to book.price,
                    "pages" to book.pages,
                    "synopsis" to book.synopsis,
                    "imageUrl" to book.imageUrl,
                    "category" to book.category,
                    "stock" to book.stock,
                    "shopName" to shopName,
                    "shopAvatarUrl" to shopAvatar
                ).filterValues { it != null }
                docRef.update(updates).await()
            } else {
                val bookWithShopInfo = book.copy(
                    ownerId = currentUserId,
                    shopName = shopName,
                    shopAvatarUrl = shopAvatar,
                    titleLowercase = book.title.lowercase(Locale.ROOT)
                )
                docRef.set(bookWithShopInfo).await()
            }
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
                if (currentStock < quantityPurchased.toLong()) throw Exception("Hết hàng")
                transaction.update(bookRef, "stock", currentStock - quantityPurchased.toLong())
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBook(bookId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")
            val docRef = booksCollection.document(bookId)
            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                val ownerId = existingDoc.getString("ownerId")
                val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
                val role = currentUserDoc.getString("role")
                if (ownerId != currentUserId && role != "ADMIN") throw Exception("Không có quyền")
                docRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

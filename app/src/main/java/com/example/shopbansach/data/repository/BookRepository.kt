package com.example.shopbansach.data.repository

import com.example.shopbansach.R
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User

class BookRepository {
    private val allBooks = listOf(
        Book(id = 1, title = "The Great Gatsby", author = "F. Scott Fitzgerald", price = "150.000đ", rating = 4.7, pages = 180, synopsis = "The story of the mysteriously wealthy Jay Gatsby..."),
        Book(id = 2, title = "Norwegian Wood", author = "Haruki Murakami", price = "120.000đ", rating = 4.5, pages = 296, synopsis = "A nostalgic story of loss..."),
        Book(id = 3, title = "Dune", author = "Frank Herbert", price = "200.000đ", rating = 4.8, pages = 412, synopsis = "Set on the desert planet Arrakis...")
        // ... (giữ nguyên danh sách sách cũ của bạn)
    )

    fun getFeaturedBooks(): List<Book> = allBooks.take(3)
    fun getNewArrivals(): List<Book> = allBooks.shuffled().take(5)
    fun getBookById(id: Int): Book? = allBooks.find { it.id == id }
    fun getSuggestions(): List<Book> = listOf(allBooks[5], allBooks[6], allBooks[7])
    fun getCartItems(): List<Book> = listOf(allBooks[3], allBooks[2])

    fun searchBooks(query: String): List<Book> {
        if (query.isEmpty()) return emptyList()
        return allBooks.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.author.contains(query, ignoreCase = true) 
        }
    }

    fun getCurrentUser(): User = User(
        id = "1",
        name = "Sophia Nguyen",
        memberSince = "2020",
        avatarRes = R.drawable.anh1 // Quay lại dùng ảnh resource
    )
}

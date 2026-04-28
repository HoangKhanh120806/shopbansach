package com.example.shopbansach.data.repository

import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User

class BookRepository {
    private val allBooks = listOf(
        Book(id = "1", title = "The Great Gatsby", author = "F. Scott Fitzgerald", price = "150.000đ", rating = 4.7, pages = 180, synopsis = "The story of the mysteriously wealthy Jay Gatsby..."),
        Book(id = "2", title = "Norwegian Wood", author = "Haruki Murakami", price = "120.000đ", rating = 4.5, pages = 296, synopsis = "A nostalgic story of loss..."),
        Book(id = "3", title = "Dune", author = "Frank Herbert", price = "200.000đ", rating = 4.8, pages = 412, synopsis = "Set on the desert planet Arrakis..."),
        Book(id = "4", title = "To Kill a Mockingbird", author = "Harper Lee", price = "180.000đ", rating = 4.9, pages = 324, synopsis = "A gripping story of racial injustice..."),
        Book(id = "5", title = "1984", author = "George Orwell", price = "130.000đ", rating = 4.6, pages = 328, synopsis = "A dystopian novel set in a totalitarian society..."),
        Book(id = "6", title = "The Hobbit", author = "J.R.R. Tolkien", price = "220.000đ", rating = 4.8, pages = 310, synopsis = "The adventure of Bilbo Baggins...")
    )

    fun getFeaturedBooks(): List<Book> = allBooks.take(3)
    fun getNewArrivals(): List<Book> = allBooks.shuffled().take(5)
    fun getBookById(id: String): Book? = allBooks.find { it.id == id }
    
    fun getSuggestions(): List<Book> = allBooks.shuffled().take(3)
    fun getCartItems(): List<Book> = allBooks.take(2)

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
        avatarUrl = null
    )
}

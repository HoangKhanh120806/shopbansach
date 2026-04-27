package com.example.shopbansach.data.repository

import com.example.shopbansach.R
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User

class BookRepository {
    private val allBooks = listOf(
        Book(
            id = 1,
            title = "The Great Gatsby",
            author = "F. Scott Fitzgerald",
            price = "150.000đ",
            rating = 4.7,
            pages = 180,
            synopsis = "The story of the mysteriously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan..."
        ),
        Book(
            id = 2,
            title = "Norwegian Wood",
            author = "Haruki Murakami",
            price = "120.000đ",
            rating = 4.5,
            pages = 296,
            synopsis = "A nostalgic story of loss and burgeoning sexuality..."
        ),
        Book(
            id = 3,
            title = "Dune",
            author = "Frank Herbert",
            price = "200.000đ",
            rating = 4.8,
            pages = 412,
            synopsis = "Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides..."
        ),
        Book(
            id = 4,
            title = "The Secret History",
            author = "Donna Tartt",
            price = "320.000đ",
            rating = 4.6,
            pages = 559,
            synopsis = "Under the influence of their charismatic classics professor, a group of clever, eccentric misfits at an elite New England college discover a way of thinking and living that is a world away from the humdrum existence of their contemporaries..."
        ),
        Book(
            id = 5,
            title = "The Silent Patient",
            author = "Alex Michaelides",
            price = "250.000đ",
            rating = 4.4,
            pages = 336,
            synopsis = "Alicia Berenson’s life is seemingly perfect. A famous painter married to an in-demand fashion photographer..."
        ),
        Book(
            id = 6,
            title = "Tiếng Chim Hót Trong Bụi Mận Gai",
            author = "Colleen McCullough",
            price = "180.000đ",
            rating = 4.9,
            pages = 600,
            synopsis = "Một câu chuyện tình yêu mãnh liệt và đầy bi kịch giữa Meggie Cleary và linh mục Ralph de Bricassart..."
        ),
        Book(
            id = 7,
            title = "Tết Ở Làng Địa Ngục",
            author = "Thảo Trang",
            price = "145.000đ",
            rating = 4.3,
            pages = 350,
            synopsis = "Những vụ án mạng kinh hoàng xảy ra tại một ngôi làng hẻo lánh vào dịp Tết..."
        ),
        Book(
            id = 8,
            title = "Đi Tìm Lẽ Sống",
            author = "Viktor E. Frankl",
            price = "95.000đ",
            rating = 4.8,
            pages = 220,
            synopsis = "Cuốn sách kể về trải nghiệm của tác giả trong các trại tập trung của Đức quốc xã..."
        ),
        Book(
            id = 9,
            title = "The Paper Palace",
            author = "Miranda Cowley Heller",
            price = "150.000đ",
            rating = 4.8,
            pages = 368,
            synopsis = "A story of summer, secrets, and the complexities of love and family..."
        ),
        Book(
            id = 10,
            title = "The Paper Menagerie",
            author = "Ken Liu",
            price = "120.000đ",
            rating = 4.5,
            pages = 464,
            synopsis = "A collection of speculative fiction stories..."
        ),
        Book(
            id = 11,
            title = "A Gentleman in Moscow",
            author = "Amor Towles",
            price = "180.000đ",
            rating = 4.7,
            pages = 462,
            synopsis = "In 1922, Count Alexander Rostov is deemed an unrepentant aristocrat by a Bolshevik tribunal..."
        )
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

    // User Data
    fun getCurrentUser(): User = User(
        id = "1",
        name = "Sophia Nguyen",
        memberSince = "2020",
        avatarRes = R.drawable.anh1
    )
}

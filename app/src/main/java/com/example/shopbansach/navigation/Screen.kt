package com.example.shopbansach.navigation

sealed class Screen(val route : String){

    object Home : Screen("home")

    object Login : Screen("login")

    object Register : Screen("register")

    object Forgot : Screen("forgot")

    object Search : Screen("search")

    object Cart : Screen("cart")

    object Profile : Screen("profile")

    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Int) = "book_detail/$bookId"
    }

    object Checkout : Screen("checkout")

    object ThankYou : Screen("thank_you")

    object Settings : Screen("settings")
}
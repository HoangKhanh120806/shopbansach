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
        fun createRoute(bookId: String) = "book_detail/$bookId"
    }

    object Checkout : Screen("checkout")

    object ThankYou : Screen("thank_you")

    object Settings : Screen("settings")

    object EditProfile : Screen("edit_profile")
    
    object MyShop : Screen("my_shop")

    object AddBook : Screen("add_book")

    object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: String) = "edit_book/$bookId"
    }

    object AdminHome : Screen("admin_home")
    
    object AdminUserManage : Screen("admin_user_manage")
}
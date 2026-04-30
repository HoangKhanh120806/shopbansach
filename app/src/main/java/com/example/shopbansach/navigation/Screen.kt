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

    object Checkout : Screen("checkout?bookId={bookId}&quantity={quantity}") {
        fun createRoute(bookId: String? = null, quantity: Int = 1) = 
            if (bookId != null) "checkout?bookId=$bookId&quantity=$quantity" else "checkout"
    }

    object ThankYou : Screen("thank_you/{orderId}") {
        fun createRoute(orderId: String) = "thank_you/$orderId"
    }

    object Settings : Screen("settings")

    object EditProfile : Screen("edit_profile")
    
    object MyShop : Screen("my_shop")

    object AddBook : Screen("add_book")

    object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: String) = "edit_book/$bookId"
    }

    object AdminHome : Screen("admin_home")
    
    object AdminUserManage : Screen("admin_user_manage")
    
    object AdminBookManage : Screen("admin_book_manage")
    
    object AdminOrderManage : Screen("admin_order_manage")

    object AddressList : Screen("address_list")
    
    object AddEditAddress : Screen("add_edit_address/{addressId}") {
        fun createRoute(addressId: String = "new") = "add_edit_address/$addressId"
    }

    object SellerShop : Screen("seller_shop/{sellerId}") {
        fun createRoute(sellerId: String) = "seller_shop/$sellerId"
    }

    object OrderHistory : Screen("order_history")

    object SellerOrderManage : Screen("seller_order_manage")
}

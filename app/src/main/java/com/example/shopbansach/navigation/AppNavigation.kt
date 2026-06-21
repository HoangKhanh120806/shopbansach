package com.example.shopbansach.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.view.*
import com.example.shopbansach.viewmodel.CartViewModel
import com.example.shopbansach.viewmodel.ChatViewModel
import com.example.shopbansach.viewmodel.HomeViewModel
import com.example.shopbansach.viewmodel.WishlistViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    startNotificationType: String? = null,
    startDataId: String? = null
) {
    val navController = rememberNavController()
    // ... (giữ nguyên các val khác)
    val authRepository = remember { AuthRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    val cartViewModel: CartViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val wishlistViewModel: WishlistViewModel = viewModel()
    
    var startDestination by remember { mutableStateOf<String?>(null) }
    var hasNavigatedFromNotification by remember { mutableStateOf(false) }

    // Xử lý điều hướng từ thông báo
    LaunchedEffect(startNotificationType, startDataId, startDestination) {
        if (!hasNavigatedFromNotification && startNotificationType != null && startDataId != null && startDestination != null) {
            when (startNotificationType) {
                "CHAT" -> {
                    navController.navigate(Screen.Chat.createRoute(startDataId))
                    hasNavigatedFromNotification = true
                }
                "ORDER" -> {
                    navController.navigate(Screen.OrderHistory.route)
                    hasNavigatedFromNotification = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            startDestination = Screen.Login.route
        } else {
            try {
                val userData = authRepository.getCurrentUserData()
                if (userData != null) {
                    startDestination = if (userData.role == UserRole.ADMIN) {
                        Screen.AdminHome.route
                    } else {
                        Screen.Home.route
                    }
                } else {
                    authRepository.logout()
                    startDestination = Screen.Login.route
                }
            } catch (e: Exception) {
                authRepository.logout()
                startDestination = Screen.Login.route
            }
        }
    }

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = modifier
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(navController = navController, viewModel = homeViewModel)
            }

            composable(route = Screen.FeaturedBooks.route) {
                FeaturedBooksScreen(navController = navController, viewModel = homeViewModel)
            }

            composable(route = Screen.Search.route) {
                SearchScreen(navController = navController)
            }

            composable(route = Screen.Wishlist.route) {
                WishlistScreen(navController = navController, viewModel = wishlistViewModel)
            }

            composable(route = Screen.Cart.route) {
                CartScreen(navController = navController, viewModel = cartViewModel)
            }

            composable(
                route = Screen.Checkout.route,
                arguments = listOf(
                    navArgument("bookId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("quantity") { 
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")
                val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
                CheckoutScreen(
                    navController = navController, 
                    buyNowBookId = bookId, 
                    buyNowQuantity = quantity,
                    cartViewModel = cartViewModel
                )
            }

            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                BookDetailScreen(
                    navController = navController, 
                    bookId = bookId,
                    cartViewModel = cartViewModel
                )
            }

            composable(
                route = Screen.ThankYou.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                ThankYouScreen(navController = navController, orderId = orderId)
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                )
            }

            composable(route = Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }

            composable(route = Screen.EditProfile.route) {
                EditProfileScreen(navController = navController)
            }

            composable(route = Screen.MyShop.route) {
                MyShopScreen(navController = navController)
            }

            composable(route = Screen.AddBook.route) {
                AddBookScreen(navController = navController)
            }

            composable(
                route = Screen.EditBook.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                EditBookScreen(navController = navController, bookId = bookId)
            }

            composable(route = Screen.Login.route) {
                LoginScreen(navController = navController)
            }

            composable(route = Screen.Register.route) {
                RegisterScreen(navController = navController)
            }

            composable(route = Screen.Forgot.route) {
                ForgotPasswordScreen(navController = navController)
            }

            composable(route = Screen.AdminHome.route) {
                AdminHomeScreen(navController = navController)
            }

            composable(route = Screen.AdminUserManage.route) {
                AdminUserManageScreen(navController = navController)
            }
            
            composable(route = Screen.AdminBookManage.route) {
                AdminBookManageScreen(navController = navController)
            }

            composable(route = Screen.AdminOrderManage.route) {
                AdminOrderManageScreen(navController = navController)
            }

            composable(route = Screen.AdminStatistics.route) {
                AdminStatisticsScreen(navController = navController)
            }

            composable(route = Screen.AddressList.route) {
                AddressListScreen(navController = navController)
            }

            composable(
                route = Screen.AddEditAddress.route,
                arguments = listOf(navArgument("addressId") { type = NavType.StringType })
            ) { backStackEntry ->
                val addressId = backStackEntry.arguments?.getString("addressId") ?: "new"
                AddEditAddressScreen(navController = navController, addressId = addressId)
            }
            
            composable(
                route = Screen.SellerShop.route,
                arguments = listOf(navArgument("sellerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
                SellerShopScreen(navController = navController, sellerId = sellerId)
            }

            composable(route = Screen.OrderHistory.route) {
                OrderHistoryScreen(navController = navController)
            }

            composable(route = Screen.SellerOrderManage.route) {
                SellerOrderManageScreen(navController = navController)
            }

            composable(route = Screen.RevenueDetail.route) {
                RevenueDetailScreen(navController = navController)
            }
            
            composable(route = Screen.Notifications.route) {
                NotificationScreen(navController = navController)
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("sellerId") { type = NavType.StringType },
                    navArgument("bookId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
                val bookId = backStackEntry.arguments?.getString("bookId")
                ChatScreen(
                    navController = navController, 
                    sellerId = sellerId, 
                    bookId = bookId,
                    viewModel = chatViewModel
                )
            }

            composable(route = Screen.ChatList.route) {
                ChatListScreen(navController = navController, viewModel = chatViewModel)
            }
        }
    }
}

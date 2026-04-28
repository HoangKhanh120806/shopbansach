package com.example.shopbansach.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.view.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Trạng thái xác định màn hình bắt đầu
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            startDestination = Screen.Login.route
        } else {
            // Nếu đã đăng nhập, kiểm tra role để quyết định vào Home hay AdminHome
            try {
                val userData = authRepository.getCurrentUserData()
                startDestination = if (userData?.role == UserRole.ADMIN) {
                    Screen.AdminHome.route
                } else {
                    Screen.Home.route
                }
            } catch (e: Exception) {
                // Nếu lỗi khi lấy data user, cho ra màn hình login hoặc home mặc định
                startDestination = Screen.Login.route
            }
        }
    }

    // Đợi cho đến khi xác định được startDestination
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
                HomeScreen(navController = navController)
            }

            composable(route = Screen.Search.route) {
                SearchScreen(navController = navController)
            }

            composable(route = Screen.Cart.route) {
                CartScreen(navController = navController)
            }

            composable(route = Screen.Checkout.route) {
                CheckoutScreen(navController = navController)
            }

            composable(route = Screen.ThankYou.route) {
                ThankYouScreen(navController = navController)
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

            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                BookDetailScreen(navController = navController, bookId = bookId)
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
        }
    }
}

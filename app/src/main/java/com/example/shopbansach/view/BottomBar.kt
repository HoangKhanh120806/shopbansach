package com.example.shopbansach.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors

@Composable
fun BottomBar(
    currentRoute: String = Screen.Home.route,
    onNavigate: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = AuthColors.Background,
        tonalElevation = 8.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AuthColors.Primary,
                selectedTextColor = AuthColors.Primary,
                indicatorColor = AuthColors.Accent.copy(alpha = 0.3f),
                unselectedIconColor = AuthColors.Hint,
                unselectedTextColor = AuthColors.Hint
            )
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Search.route,
            onClick = { onNavigate(Screen.Search.route) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
            label = { Text("Tìm kiếm") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AuthColors.Primary,
                selectedTextColor = AuthColors.Primary,
                indicatorColor = AuthColors.Accent.copy(alpha = 0.3f),
                unselectedIconColor = AuthColors.Hint,
                unselectedTextColor = AuthColors.Hint
            )
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Cart.route,
            onClick = { onNavigate(Screen.Cart.route) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng") },
            label = { Text("Giỏ hàng") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AuthColors.Primary,
                selectedTextColor = AuthColors.Primary,
                indicatorColor = AuthColors.Accent.copy(alpha = 0.3f),
                unselectedIconColor = AuthColors.Hint,
                unselectedTextColor = AuthColors.Hint
            )
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Hồ sơ") },
            label = { Text("Hồ sơ") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AuthColors.Primary,
                selectedTextColor = AuthColors.Primary,
                indicatorColor = AuthColors.Accent.copy(alpha = 0.3f),
                unselectedIconColor = AuthColors.Hint,
                unselectedTextColor = AuthColors.Hint
            )
        )
    }
}

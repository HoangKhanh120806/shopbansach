package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopbansach.R
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.BookRepository
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val bookRepository = remember { BookRepository() }
    val authRepository = remember { AuthRepository() }
    
    // Kiểm tra trạng thái đăng nhập thực tế từ Firebase
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val isLoggedIn = currentUser != null
    
    // Chỉ lấy dữ liệu user nếu đã đăng nhập
    val user = if (isLoggedIn) remember { bookRepository.getCurrentUser() } else null

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = Screen.Profile.route,
                onNavigate = { route ->
                    if (route != Screen.Profile.route) {
                        navController.navigate(route)
                    }
                }
            )
        },
        containerColor = AuthColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            if (isLoggedIn && user != null) {
                // GIAO DIỆN KHI ĐÃ ĐĂNG NHẬP
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AuthColors.Primary
                )
                Text(
                    text = "Cozy Reads Member Since ${user.memberSince}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuthColors.Hint,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE4E2DD))
                ) {
                    user.avatarRes?.let {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        ProfileMenuItem(
                            icon = Icons.Default.History,
                            title = "Lịch sử đơn hàng",
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Cài đặt tài khoản",
                            onClick = { navController.navigate(Screen.Settings.route) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        
                        // Theme Switch
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NightlightRound, null, modifier = Modifier.size(28.dp), tint = AuthColors.Primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Chế độ tối/sáng", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Switch(checked = isDarkTheme, onCheckedChange = { onThemeChange(it) })
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        
                        // Đăng xuất
                        ProfileMenuItem(
                            icon = Icons.Default.ExitToApp,
                            title = "Đăng xuất",
                            titleColor = Color.Red,
                            onClick = {
                                authRepository.logout()
                                // Quay về trang home và xóa sạch stack
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            } else {
                // GIAO DIỆN KHI CHƯA ĐĂNG NHẬP
                Text(
                    text = "Chào mừng bạn",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AuthColors.Primary
                )
                Text(
                    text = "Đăng nhập để xem hồ sơ và đơn hàng của bạn",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuthColors.Hint,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Theme Switch vẫn cho phép dùng khi chưa login
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NightlightRound, null, modifier = Modifier.size(28.dp), tint = AuthColors.Primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Chế độ tối/sáng", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Switch(checked = isDarkTheme, onCheckedChange = { onThemeChange(it) })
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                        // Nút Đăng nhập
                        ProfileMenuItem(
                            icon = Icons.Default.Login,
                            title = "Đăng nhập ngay",
                            titleColor = AuthColors.Accent,
                            onClick = {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = AuthColors.Primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = titleColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (titleColor != Color.Red && titleColor != AuthColors.Accent) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AuthColors.Hint
            )
        }
    }
}

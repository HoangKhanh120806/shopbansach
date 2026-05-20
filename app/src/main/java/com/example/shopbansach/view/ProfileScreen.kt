package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRegisterSellerDialog by remember { mutableStateOf(false) }
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLoggedIn = currentUser != null

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            user = authRepository.getCurrentUserData()
        }
        isLoading = false
    }

    if (showRegisterSellerDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterSellerDialog = false },
            title = { Text("Đăng ký bán hàng") },
            text = { Text("Gửi yêu cầu đăng ký bán hàng tới Admin?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            user?.let {
                                val result = authRepository.updateUserRole(it.id, UserRole.PENDING_SELLER)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Đã gửi yêu cầu!", Toast.LENGTH_SHORT).show()
                                    user = authRepository.getCurrentUserData() 
                                }
                            }
                        }
                        showRegisterSellerDialog = false
                    }
                ) { Text("Gửi") }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterSellerDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = Screen.Profile.route,
                onNavigate = { route ->
                    if (route != Screen.Profile.route) navController.navigate(route)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                if (isLoggedIn && user != null) {
                    Text(
                        text = user?.name ?: "Người dùng",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Thành viên Cozy Reads từ ${user?.memberSince}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (!user?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            when (user?.role) {
                                UserRole.SELLER, UserRole.ADMIN -> {
                                    ProfileMenuItem(
                                        icon = Icons.Default.AddBusiness,
                                        title = "Quản lý Shop",
                                        titleColor = MaterialTheme.colorScheme.tertiary,
                                        onClick = { navController.navigate(Screen.MyShop.route) }
                                    )
                                }
                                UserRole.PENDING_SELLER -> {
                                    ProfileMenuItem(
                                        icon = Icons.Default.HourglassEmpty,
                                        title = "Đang chờ duyệt",
                                        titleColor = MaterialTheme.colorScheme.secondary,
                                        onClick = { 
                                            Toast.makeText(context, "Yêu cầu đang chờ duyệt", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                else -> {
                                    ProfileMenuItem(
                                        icon = Icons.Default.AddBusiness,
                                        title = "Đăng ký bán hàng",
                                        titleColor = MaterialTheme.colorScheme.tertiary,
                                        onClick = { showRegisterSellerDialog = true }
                                    )
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                            ProfileMenuItem(
                                icon = Icons.Default.History,
                                title = "Lịch sử đơn hàng",
                                onClick = { navController.navigate(Screen.OrderHistory.route) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            
                            ProfileMenuItem(
                                icon = Icons.Default.Settings,
                                title = "Cài đặt tài khoản",
                                onClick = { navController.navigate(Screen.Settings.route) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NightlightRound, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Chế độ tối", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = isDarkTheme, 
                                    onCheckedChange = { onThemeChange(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.tertiary)
                                )
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            
                            ProfileMenuItem(
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                title = "Đăng xuất",
                                titleColor = Color.Red,
                                onClick = {
                                    authRepository.logout()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Chào mừng bạn",
                        style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Screen.Login.route) }) {
                        Text("Đăng nhập ngay")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor, modifier = Modifier.weight(1f))
        if (titleColor != Color.Red && icon != Icons.Default.HourglassEmpty) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

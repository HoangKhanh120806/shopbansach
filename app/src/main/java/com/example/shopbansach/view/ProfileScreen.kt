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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.sp
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
    
    var userState by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRegisterSellerDialog by remember { mutableStateOf(false) }
    var shopNameInput by remember { mutableStateOf("") }
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLoggedIn = currentUser != null

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val data = authRepository.getCurrentUserData()
            userState = data
            shopNameInput = data?.shopName ?: data?.name ?: ""
        }
        isLoading = false
    }

    if (showRegisterSellerDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterSellerDialog = false },
            title = { Text("Đăng ký bán hàng") },
            text = {
                Column {
                    Text("Nhập tên cửa hàng của bạn để gửi yêu cầu tới Admin:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = shopNameInput,
                        onValueChange = { shopNameInput = it },
                        label = { Text("Tên Shop") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (shopNameInput.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập tên Shop", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        scope.launch {
                            val uid = currentUser?.uid ?: return@launch
                            authRepository.updateShopName(uid, shopNameInput)
                            val result = authRepository.updateUserRole(uid, UserRole.PENDING_SELLER)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Yêu cầu đã được gửi!", Toast.LENGTH_SHORT).show()
                                userState = authRepository.getCurrentUserData() 
                            }
                        }
                        showRegisterSellerDialog = false
                    }
                ) { Text("Gửi yêu cầu") }
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
            val user = userState
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
                        text = user.name,
                        style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Thành viên Cozy Reads từ ${user.memberSince}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(model = user.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp).align(Alignment.Center), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                            // TIN NHẮN
                            ProfileMenuItem(icon = Icons.Default.Chat, title = "Tin nhắn của tôi", onClick = { navController.navigate(Screen.ChatList.route) })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                            // LỊCH SỬ ĐƠN HÀNG
                            ProfileMenuItem(icon = Icons.Default.History, title = "Lịch sử đơn hàng", onClick = { navController.navigate(Screen.OrderHistory.route) })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                            // CÀI ĐẶT TÀI KHOẢN
                            ProfileMenuItem(icon = Icons.Default.Settings, title = "Cài đặt tài khoản", onClick = { navController.navigate(Screen.Settings.route) })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                            // ĐĂNG KÝ BÁN HÀNG / QUẢN LÝ SHOP (Vị trí cạnh Cài đặt tài khoản)
                            when (user.role) {
                                UserRole.SELLER, UserRole.ADMIN -> {
                                    ProfileMenuItem(icon = Icons.Default.AddBusiness, title = "Quản lý Shop của tôi", titleColor = MaterialTheme.colorScheme.tertiary, onClick = { navController.navigate(Screen.MyShop.route) })
                                }
                                UserRole.PENDING_SELLER -> {
                                    ProfileMenuItem(icon = Icons.Default.HourglassEmpty, title = "Yêu cầu bán hàng đang chờ duyệt", titleColor = MaterialTheme.colorScheme.secondary, onClick = { Toast.makeText(context, "Vui lòng chờ Admin phê duyệt", Toast.LENGTH_SHORT).show() })
                                }
                                else -> {
                                    ProfileMenuItem(icon = Icons.Default.AddBusiness, title = "Đăng ký bán hàng ngay", titleColor = MaterialTheme.colorScheme.tertiary, onClick = { showRegisterSellerDialog = true })
                                }
                            }
                            
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            
                            // CHẾ ĐỘ TỐI
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NightlightRound, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Chế độ tối", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Switch(checked = isDarkTheme, onCheckedChange = { onThemeChange(it) }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.tertiary))
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            
                            // ĐĂNG XUẤT
                            ProfileMenuItem(icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Đăng xuất", titleColor = Color.Red, onClick = { authRepository.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } })
                        }
                    }
                } else {
                    Text(text = "Chào mừng bạn", style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Screen.Login.route) }) { Text("Đăng nhập ngay") }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, titleColor: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor, modifier = Modifier.weight(1f))
        if (titleColor != Color.Red && icon != Icons.Default.HourglassEmpty) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}

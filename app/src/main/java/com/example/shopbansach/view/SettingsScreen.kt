package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var user by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRegisterSellerDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        user = repository.getCurrentUserData()
        isLoading = false
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa tài khoản này không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = repository.deleteAccount()
                            if (result.isSuccess) {
                                Toast.makeText(context, "Đã xóa tài khoản thành công", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Lỗi: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showRegisterSellerDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterSellerDialog = false },
            title = { Text("Đăng ký bán hàng") },
            text = { Text("Bạn có muốn gửi yêu cầu đăng ký bán hàng? Yêu cầu của bạn sẽ được Admin xem xét và duyệt.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            user?.let {
                                val result = repository.updateUserRole(it.id, UserRole.PENDING_SELLER)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Yêu cầu đã được gửi! Vui lòng chờ Admin duyệt.", Toast.LENGTH_LONG).show()
                                    user = repository.getCurrentUserData()
                                } else {
                                    Toast.makeText(context, "Lỗi: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        showRegisterSellerDialog = false
                    }
                ) {
                    Text("Gửi yêu cầu", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterSellerDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cài đặt tài khoản",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                SettingsMenuItem(
                    icon = Icons.Default.Person,
                    title = "Chỉnh sửa hồ sơ",
                    onClick = { navController.navigate(Screen.EditProfile.route) }
                )
                
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                SettingsMenuItem(
                    icon = Icons.Default.LocationOn,
                    title = "Địa chỉ nhận hàng",
                    onClick = { navController.navigate(Screen.AddressList.route) }
                )

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                if (user?.role == UserRole.USER) {
                    SettingsMenuItem(
                        icon = Icons.Default.AddBusiness,
                        title = "Đăng ký bán hàng",
                        onClick = { showRegisterSellerDialog = true }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                } else if (user?.role == UserRole.PENDING_SELLER) {
                    SettingsMenuItem(
                        icon = Icons.Default.HourglassEmpty,
                        title = "Đang chờ Admin duyệt bán hàng",
                        onClick = { 
                            Toast.makeText(context, "Yêu cầu của bạn đang được xử lý", Toast.LENGTH_SHORT).show()
                        },
                        titleColor = MaterialTheme.colorScheme.secondary
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }

                SettingsMenuItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Xóa tài khoản",
                    titleColor = Color.Red,
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == Color.Red) Color.Red else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = titleColor
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

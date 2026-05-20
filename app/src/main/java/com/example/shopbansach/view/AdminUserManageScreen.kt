package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.model.UserRole
import com.example.shopbansach.viewmodel.AdminUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManageScreen(
    navController: NavController,
    viewModel: AdminUserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage, uiState.actionSuccessMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, "Lỗi: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        uiState.actionSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý người dùng", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm theo tên, email hoặc tên shop...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.filteredUsers.isEmpty()) {
                    Text(
                        text = if (uiState.searchQuery.isEmpty()) "Danh sách người dùng trống" else "Không tìm thấy người dùng phù hợp",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val sortedUsers = uiState.filteredUsers.sortedByDescending { it.role == UserRole.PENDING_SELLER }
                        
                        items(sortedUsers, key = { it.id }) { user ->
                            UserAdminItem(
                                user = user,
                                onChangeRole = { newRole -> viewModel.changeUserRole(user.id, newRole) },
                                onDelete = { viewModel.deleteUser(user.id) },
                                onUpdateInfo = { name, shopName -> viewModel.updateUserInfo(user.id, name, shopName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAdminItem(
    user: User,
    onChangeRole: (UserRole) -> Unit,
    onDelete: () -> Unit,
    onUpdateInfo: (String, String?) -> Unit
) {
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showApproveConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.role == UserRole.PENDING_SELLER) 
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = user.email, fontSize = 12.sp, color = Color.Gray)
                if (!user.shopName.isNullOrEmpty()) {
                    Text(text = "Shop: ${user.shopName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Surface(
                    color = when(user.role) {
                        UserRole.ADMIN -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                        UserRole.SELLER -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        UserRole.PENDING_SELLER -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        UserRole.USER -> Color(0xFF2196F3).copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (user.role == UserRole.PENDING_SELLER) "CHỜ DUYỆT BÁN HÀNG" else user.role.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(user.role) {
                            UserRole.ADMIN -> Color(0xFF9C27B0)
                            UserRole.SELLER -> Color(0xFF4CAF50)
                            UserRole.PENDING_SELLER -> Color(0xFFFF9800)
                            UserRole.USER -> Color(0xFF2196F3)
                        }
                    )
                }
            }

            Row {
                if (user.role == UserRole.PENDING_SELLER) {
                    IconButton(
                        onClick = { showApproveConfirm = true },
                        modifier = Modifier.background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color(0xFF4CAF50))
                    }
                }
                
                if (user.role != UserRole.ADMIN) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { showRoleDialog = true }) {
                        Icon(Icons.Default.Shield, contentDescription = "Change Role", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(user.name) }
        var editShopName by remember { mutableStateOf(user.shopName ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Sửa thông tin tài khoản") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Tên người dùng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (user.role == UserRole.SELLER || user.role == UserRole.PENDING_SELLER) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editShopName,
                            onValueChange = { editShopName = it },
                            label = { Text("Tên cửa hàng") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateInfo(editName, if (user.role == UserRole.USER) null else editShopName)
                    showEditDialog = false
                }) {
                    Text("Cập nhật")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showApproveConfirm) {
        AlertDialog(
            onDismissRequest = { showApproveConfirm = false },
            title = { Text("Duyệt người bán") },
            text = { Text("Bạn có đồng ý cấp quyền người bán cho ${user.name} không?") },
            confirmButton = {
                TextButton(onClick = {
                    onChangeRole(UserRole.SELLER)
                    showApproveConfirm = false
                }) {
                    Text("Đồng ý", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirm = false }) { Text("Hủy") }
            }
        )
    }

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Thay đổi quyền hạn") },
            text = { Text("Chọn vai trò mới cho người dùng ${user.name}") },
            confirmButton = {
                Column {
                    listOf(UserRole.USER, UserRole.SELLER, UserRole.PENDING_SELLER).forEach { role ->
                        TextButton(
                            onClick = {
                                onChangeRole(role)
                                showRoleDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = when(role) {
                                UserRole.USER -> "Người dùng thường (USER)"
                                UserRole.SELLER -> "Người bán (SELLER)"
                                else -> "Chờ duyệt (PENDING_SELLER)"
                            })
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa người dùng ${user.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

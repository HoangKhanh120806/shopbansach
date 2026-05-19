package com.example.shopbansach.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.User
import com.example.shopbansach.data.repository.AuthRepository
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.ui.auth.AuthButton
import com.example.shopbansach.ui.auth.AuthTextField
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository() }
    val cloudinaryRepository = remember { CloudinaryRepository(context) }
    val scope = rememberCoroutineScope()
    
    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Launcher để chọn ảnh từ thư viện
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        user = authRepository.getCurrentUserData()
        name = user?.name ?: ""
        avatarUrl = user?.avatarUrl
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa hồ sơ",
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Phần chỉnh sửa ảnh đại diện
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            // Hiển thị ảnh vừa chọn từ máy
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!avatarUrl.isNullOrEmpty()) {
                            // Hiển thị ảnh từ Cloudinary
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Photo",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Thông tin cá nhân",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = name,
                    hint = "Họ và tên",
                    onValueChange = { name = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Thay đổi mật khẩu",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = currentPassword,
                    hint = "Mật khẩu hiện tại",
                    isPassword = true,
                    onValueChange = { currentPassword = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = newPassword,
                    hint = "Mật khẩu mới (để trống nếu không đổi)",
                    isPassword = true,
                    onValueChange = { newPassword = it }
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (isSaving) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text("Đang lưu thay đổi...", modifier = Modifier.padding(top = 8.dp))
                    }
                } else {
                    AuthButton(
                        text = "Lưu thay đổi",
                        onClick = {
                            if (name.isEmpty()) {
                                Toast.makeText(context, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                                return@AuthButton
                            }
                            
                            isSaving = true
                            scope.launch {
                                var finalAvatarUrl = avatarUrl

                                // 1. Nếu có chọn ảnh mới, upload lên Cloudinary trước
                                selectedImageUri?.let { uri ->
                                    val uploadResult = cloudinaryRepository.uploadAvatar(uri)
                                    if (uploadResult.isSuccess) {
                                        finalAvatarUrl = uploadResult.getOrNull()
                                    } else {
                                        Toast.makeText(context, "Lỗi upload ảnh: ${uploadResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        isSaving = false
                                        return@launch
                                    }
                                }

                                // 2. Cập nhật Firestore (tên và avatarUrl mới)
                                val userUid = FirebaseAuth.getInstance().currentUser?.uid
                                if (userUid != null) {
                                    val updateResult = authRepository.updateUserProfile(userUid, name, finalAvatarUrl)
                                    
                                    // 3. Logic đổi mật khẩu nếu có nhập
                                    if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                                        val passResult = authRepository.changePassword(currentPassword, newPassword)
                                        if (passResult.isFailure) {
                                            Toast.makeText(context, "Lỗi đổi mật khẩu: ${passResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    if (updateResult.isSuccess) {
                                        Toast.makeText(context, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "Lỗi cập nhật Firestore: ${updateResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                                isSaving = false
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

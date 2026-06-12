package com.example.shopbansach

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.shopbansach.navigation.AppNavigation
import com.example.shopbansach.ui.theme.ShopbansachTheme
import com.example.shopbansach.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : ComponentActivity() {
    
    private lateinit var notificationHelper: NotificationHelper
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var notificationListener: ListenerRegistration? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Lưu thời điểm app bắt đầu chạy để tránh hiện thông báo cũ
    private val startTime = System.currentTimeMillis()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Đã bật thông báo hệ thống", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Cài đặt Splash Screen TRƯỚC super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        
        notificationHelper = NotificationHelper(this)
        checkNotificationPermission()
        setupNotificationListener()

        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            
            ShopbansachTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupNotificationListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            
            // Gỡ bỏ listener cũ nếu có (khi user logout/login lại)
            notificationListener?.remove()
            
            if (userId != null) {
                notificationListener = firestore.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isRead", false)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) return@addSnapshotListener

                        for (dc in snapshots?.documentChanges ?: emptyList()) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                val timestamp = dc.document.getLong("createdAt") ?: 0L
                                
                                // Chỉ hiện thông báo nếu nó mới được tạo (sau khi app mở)
                                // Hoặc bạn có thể bỏ check startTime nếu muốn hiện cả thông báo cũ chưa đọc
                                if (timestamp > startTime || timestamp == 0L) {
                                    val title = dc.document.getString("title") ?: "Thông báo mới"
                                    val message = dc.document.getString("message") ?: ""
                                    notificationHelper.showNotification(title, message)
                                }
                            }
                        }
                    }
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Giải phóng tài nguyên để tránh rò rỉ bộ nhớ
        authStateListener?.let { auth.removeAuthStateListener(it) }
        notificationListener?.remove()
    }
}

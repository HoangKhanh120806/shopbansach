package com.example.shopbansach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.shopbansach.navigation.AppNavigation
import com.example.shopbansach.ui.theme.ShopbansachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            val systemInDarkTheme = isSystemInDarkTheme()
            
            // Khởi tạo theme dựa trên hệ thống lúc đầu
            var useDarkTheme by remember { mutableStateOf(systemInDarkTheme) }

            ShopbansachTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        isDarkTheme = useDarkTheme,
                        onThemeChange = { useDarkTheme = it }
                    )
                }
            }
        }
    }
}

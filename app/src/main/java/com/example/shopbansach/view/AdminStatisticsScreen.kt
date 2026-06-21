package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopbansach.viewmodel.AdminStatisticsViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    navController: NavController,
    viewModel: AdminStatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê hệ thống", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Tổng quan
                item {
                    Text("Tổng quan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Doanh thu",
                            value = formatCurrency(uiState.totalRevenue),
                            icon = Icons.Default.AttachMoney,
                            color = Color(0xFF4CAF50)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Đơn hàng",
                            value = uiState.totalOrders.toString(),
                            icon = Icons.Default.ShoppingBag,
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Sách",
                            value = uiState.totalBooks.toString(),
                            icon = Icons.Default.Book,
                            color = Color(0xFF2196F3)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Người dùng",
                            value = uiState.totalUsers.toString(),
                            icon = Icons.Default.People,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }

                // Trạng thái đơn hàng
                item {
                    Text("Trạng thái đơn hàng", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            uiState.ordersByStatus.forEach { (status, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(status)
                                    Text(count.toString(), fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { if (uiState.totalOrders > 0) count.toFloat() / uiState.totalOrders else 0f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = getStatusColor(status),
                                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Top bán chạy
                item {
                    Text("Top 5 sách bán chạy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                items(uiState.topSellingBooks) { (title, quantity) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("Đã bán: $quantity", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("$quantity", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Doanh thu theo Shop
                item {
                    Text("Doanh thu theo Shop", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                if (uiState.revenueByShop.isEmpty()) {
                    item {
                        Text("Chưa có dữ liệu doanh thu shop", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    items(uiState.revenueByShop) { (shopName, revenue) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(shopName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(
                                    formatCurrency(revenue),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

fun formatCurrency(amount: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(amount)
}

fun getStatusColor(status: String): Color {
    val s = status.trim().lowercase()
    return when {
        s.contains("chờ") -> Color(0xFFFFC107)
        s.contains("xác nhận") -> Color(0xFF2196F3)
        s.contains("đang giao") -> Color(0xFF03A9F4)
        s.contains("đã giao") || s.contains("hoàn thành") || s.contains("thành công") -> Color(0xFF4CAF50)
        s.contains("hủy") -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

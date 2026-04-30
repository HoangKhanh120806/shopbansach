package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.utils.CurrencyUtils
import com.example.shopbansach.utils.CustomCard
import com.example.shopbansach.viewmodel.SellerOrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrderManageScreen(
    navController: NavController,
    viewModel: SellerOrderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Theo dõi thông báo thành công hoặc lỗi
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            // Hiện lỗi chi tiết từ Firebase
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("Chưa có đơn hàng nào", color = Color.Gray)
                    TextButton(onClick = { viewModel.loadOrders() }) {
                        Text("Tải lại trang")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    SellerOrderCard(
                        order = order,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateOrderStatus(order.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SellerOrderCard(
    order: Order,
    onUpdateStatus: (String) -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(order.createdAt))

    CustomCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã đơn: #${order.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                OrderStatusBadge(order.status)
            }
            
            Text(text = dateStr, fontSize = 12.sp, color = Color.Gray)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = "• ${item.title}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text(text = "x${item.quantity}", fontSize = 14.sp)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Thành tiền:", fontWeight = FontWeight.Medium)
                Text(
                    text = CurrencyUtils.formatPrice(order.totalPrice),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Khách hàng: ${order.shippingAddress.fullName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = "SĐT: ${order.shippingAddress.phoneNumber}", fontSize = 13.sp)
            Text(text = "Địa chỉ: ${order.shippingAddress.addressDetail}, ${order.shippingAddress.city}", fontSize = 13.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Khu vực các nút xử lý
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                when (order.status.lowercase()) {
                    "chờ xác nhận" -> {
                        OutlinedButton(
                            onClick = { onUpdateStatus("Đã hủy") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Hủy đơn")
                        }
                        Button(onClick = { onUpdateStatus("Đang giao") }) {
                            Text("Xác nhận & Giao hàng")
                        }
                    }
                    "đang giao" -> {
                        Button(
                            onClick = { onUpdateStatus("Đã giao") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Đã giao hàng")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "chờ xác nhận" -> Color(0xFFFFA000)
        "đang giao" -> MaterialTheme.colorScheme.primary
        "đã giao" -> Color(0xFF4CAF50)
        "đã hủy" -> Color.Red
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

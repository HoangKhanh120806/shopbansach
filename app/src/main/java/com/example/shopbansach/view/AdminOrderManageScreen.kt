package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopbansach.data.model.Constants
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.viewmodel.AdminOrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderManageScreen(
    navController: NavController,
    viewModel: AdminOrderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage, uiState.actionSuccess) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        uiState.actionSuccess?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Đơn hàng", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có đơn hàng nào trong hệ thống")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.orders) { order ->
                    AdminOrderItemCard(
                        order = order,
                        onStatusChange = { newStatus -> viewModel.updateStatus(order.id, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOrderItemCard(
    order: Order,
    onStatusChange: (String) -> Unit
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Đơn: #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                    Text(text = dateFormat.format(Date(order.createdAt)), fontSize = 12.sp, color = Color.Gray)
                }
                
                AssistChip(
                    onClick = { showStatusDialog = true },
                    label = { Text(order.status) },
                    trailingIcon = { Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp)) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            Text(text = "Khách hàng: ${order.shippingAddress.fullName}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = "SĐT: ${order.shippingAddress.phoneNumber}", fontSize = 13.sp, color = Color.Gray)
            Text(text = "Địa chỉ: ${order.shippingAddress.addressDetail}, ${order.shippingAddress.city}", fontSize = 13.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = "Sản phẩm:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            order.items.forEach { item ->
                Text(text = "• ${item.quantity}x ${item.title}", fontSize = 13.sp)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Tổng tiền:", fontWeight = FontWeight.Bold)
                Text(
                    text = String.format(Locale.US, "%,dđ", order.totalPrice),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Cập nhật trạng thái") },
            text = {
                Column {
                    val statuses = listOf(
                        Constants.OrderStatus.PENDING,
                        Constants.OrderStatus.SHIPPING,
                        Constants.OrderStatus.COMPLETED,
                        Constants.OrderStatus.CANCELLED
                    )
                    statuses.forEach { status ->
                        TextButton(
                            onClick = {
                                onStatusChange(status)
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(status)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

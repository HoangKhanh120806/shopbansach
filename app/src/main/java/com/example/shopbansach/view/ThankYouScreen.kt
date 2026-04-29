package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Order
import com.example.shopbansach.data.repository.OrderRepository
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
import com.example.shopbansach.utils.CurrencyUtils

@Composable
fun ThankYouScreen(navController: NavController, orderId: String) {
    val orderRepository = remember { OrderRepository() }
    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(orderId) {
        order = orderRepository.getOrderById(orderId)
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthColors.Background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (order == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Không tìm thấy thông tin đơn hàng")
                Button(onClick = { navController.navigate(Screen.Home.route) }) {
                    Text("Quay về trang chủ")
                }
            }
        } else {
            val currentOrder = order!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "Cảm ơn",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAC865C)
                    )
                )

                Text(
                    text = "Đơn hàng của bạn đã được xác nhận.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuthColors.Primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Order Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Mã đơn hàng: #${currentOrder.id.takeLast(8).uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        currentOrder.items.forEach { item ->
                            OrderItemRow(
                                title = item.title,
                                author = item.author,
                                price = CurrencyUtils.formatPrice(item.price),
                                quantity = item.quantity,
                                imageUrl = item.imageUrl
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tổng thanh toán:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = CurrencyUtils.formatPrice(currentOrder.totalPrice),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Action Buttons
                Button(
                    onClick = { 
                        navController.navigate(Screen.OrderHistory.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC865C)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Xem lịch sử đơn hàng", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tiếp tục mua sắm",
                    modifier = Modifier.clickable { 
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuthColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun OrderItemRow(title: String, author: String, price: String, quantity: Int, imageUrl: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(50.dp, 70.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE4E2DD))
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "$author | SL: $quantity", fontSize = 12.sp, color = AuthColors.Hint)
            Text(text = price, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

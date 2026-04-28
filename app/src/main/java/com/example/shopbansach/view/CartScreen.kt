package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.viewmodel.CartViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Tính toán chỉ dựa trên các sản phẩm ĐÃ CHỌN
    val selectedItems = uiState.cartItems.filter { it.isSelected }
    val subtotal = selectedItems.sumOf { it.price * it.quantity }
    val shipping = if (subtotal > 0) 30000L else 0L
    val isAllSelected = uiState.cartItems.isNotEmpty() && uiState.cartItems.all { it.isSelected }

    var itemToRemove by remember { mutableStateOf<CartItem?>(null) }

    if (itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            title = { Text("Xóa sản phẩm") },
            text = { Text("Bạn có muốn xóa \"${itemToRemove?.title}\" ra khỏi giỏ hàng không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToRemove?.let { viewModel.removeFromCart(it.bookId) }
                        itemToRemove = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Giỏ hàng (${uiState.cartItems.size})", 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (uiState.cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Xóa tất cả", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAllSelected,
                                onCheckedChange = { viewModel.toggleSelectAll(it) }
                            )
                            Text("Chọn tất cả", fontSize = 14.sp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tổng thanh toán (${selectedItems.size})", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    String.format(Locale.US, "%,dđ", subtotal + shipping),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = { navController.navigate(Screen.Checkout.route) },
                                modifier = Modifier.height(50.dp).width(160.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = selectedItems.isNotEmpty()
                            ) {
                                Text("MUA HÀNG", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                BottomBar(currentRoute = Screen.Cart.route, onNavigate = { navController.navigate(it) })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.cartItems.isEmpty() -> {
                EmptyCartView(onShopNow = { navController.navigate(Screen.Home.route) })
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onToggleSelection = { viewModel.toggleSelection(item.bookId, it) },
                            onIncrease = { viewModel.updateQuantity(item.bookId, item.quantity + 1) },
                            onDecrease = { 
                                if (item.quantity > 1) {
                                    viewModel.updateQuantity(item.bookId, item.quantity - 1)
                                } else {
                                    itemToRemove = item
                                }
                            },
                            onRemove = { itemToRemove = item }
                        )
                    }
                    
                    if (selectedItems.isNotEmpty()) {
                        item {
                            OrderSummarySection(subtotal, shipping)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onToggleSelection: (Boolean) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = onToggleSelection,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp, 85.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(text = item.author, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Text(
                    text = String.format(Locale.US, "%,dđ", item.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = "${item.quantity}", 
                            modifier = Modifier.padding(horizontal = 6.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        }
                    }
                    
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummarySection(subtotal: Long, shipping: Long) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Tóm tắt (Sản phẩm đã chọn)", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Serif)
        Spacer(modifier = Modifier.height(12.dp))
        
        SummaryDetailRow("Tiền hàng", String.format(Locale.US, "%,dđ", subtotal))
        SummaryDetailRow("Phí vận chuyển", String.format(Locale.US, "%,dđ", shipping))
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tổng cộng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                String.format(Locale.US, "%,dđ", subtotal + shipping), 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SummaryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun EmptyCartView(onShopNow: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.RemoveShoppingCart, 
                contentDescription = null, 
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Giỏ hàng của bạn đang trống", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "Hãy quay lại và chọn cho mình những cuốn sách yêu thích nhé!",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onShopNow, shape = RoundedCornerShape(12.dp)) {
                Text("KHÁM PHÁ NGAY")
            }
        }
    }
}

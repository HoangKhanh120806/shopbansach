package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.utils.CurrencyUtils
import com.example.shopbansach.utils.SharedTopAppBar
import com.example.shopbansach.utils.PrimaryButton
import com.example.shopbansach.utils.CustomCard
import com.example.shopbansach.viewmodel.CartViewModel

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val selectedItems by remember { derivedStateOf { uiState.cartItems.filter { it.isSelected } } }
    val subtotal by remember { derivedStateOf { selectedItems.sumOf { it.price * it.quantity } } }
    val shipping = if (subtotal > 0) 30000L else 0L
    val isAllSelected by remember { derivedStateOf { uiState.cartItems.isNotEmpty() && uiState.cartItems.all { it.isSelected } } }

    var itemToRemove by remember { mutableStateOf<CartItem?>(null) }

    if (itemToRemove != null) {
        AlertDialog(
            onDismissRequest = { itemToRemove = null },
            title = { Text("Xóa sản phẩm") },
            text = { Text("Bạn có muốn xóa \"${itemToRemove?.title}\" ra khỏi giỏ hàng?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToRemove?.let { viewModel.removeFromCart(it.bookId) }
                    itemToRemove = null
                }) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRemove = null }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = "Giỏ hàng (${uiState.cartItems.size})",
                onBackClick = { navController.popBackStack() },
                actions = {
                    if (uiState.cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.clickable { viewModel.toggleSelectAll(!isAllSelected) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isAllSelected, onCheckedChange = { viewModel.toggleSelectAll(it) })
                                Text("Chọn tất cả", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "Phí ship: ${CurrencyUtils.formatPrice(shipping)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tổng cộng", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    CurrencyUtils.formatPrice(subtotal + shipping),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            PrimaryButton(
                                text = "MUA HÀNG (${selectedItems.size})",
                                onClick = { navController.navigate(Screen.Checkout.createRoute()) },
                                modifier = Modifier.width(180.dp),
                                enabled = selectedItems.isNotEmpty()
                            )
                        }
                    }
                }
            } else {
                BottomBar(currentRoute = Screen.Cart.route, onNavigate = { navController.navigate(it) })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding -> 
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.cartItems.isEmpty()) {
            EmptyCartView(onShopNow = { navController.navigate(Screen.Home.route) })
        } else {
            // KHẮC PHỤC LỖI KHÔNG CUỘN ĐƯỢC:
            // 1. Loại bỏ .padding(innerPadding) khỏi Modifier để LazyColumn nhận được sự kiện chạm toàn màn hình.
            // 2. Dùng contentPadding để đẩy nội dung bên trong thoát khỏi vùng bị TopBar và BottomBar che.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
            ) {
                items(uiState.cartItems, key = { it.bookId }) { item ->
                    CartItemCard(
                        item = item,
                        onToggleSelection = { viewModel.toggleSelection(item.bookId, it) },
                        onIncrease = { viewModel.updateQuantity(item.bookId, item.quantity + 1) },
                        onDecrease = { 
                            if (item.quantity > 1) viewModel.updateQuantity(item.bookId, item.quantity - 1)
                            else itemToRemove = item
                        },
                        onRemove = { itemToRemove = item }
                    )
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
    CustomCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isSelected, onCheckedChange = onToggleSelection)
            
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp, 100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.author, 
                    fontSize = 12.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatPrice(item.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "${item.quantity}", 
                            modifier = Modifier.padding(horizontal = 10.dp), 
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    IconButton(
                        onClick = onRemove,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    ) {
                        Icon(Icons.Default.DeleteSweep, null)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(onShopNow: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.RemoveShoppingCart, 
            null, 
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Giỏ hàng của bạn đang trống", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(text = "KHÁM PHÁ NGAY", onClick = onShopNow, modifier = Modifier.width(200.dp))
    }
}

package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.BookRepository
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val repository = remember { BookRepository() }
    val cartItems = remember { repository.getCartItems() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Shopping Cart",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AuthColors.Background
                )
            )
        },
        bottomBar = {
            Column {
                // Nút Checkout
                Button(
                    onClick = { navController.navigate(Screen.Checkout.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC89C81)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Checkout", fontSize = 18.sp, color = Color.White)
                }
                
                // Bottom Navigation
                BottomBar(
                    currentRoute = Screen.Cart.route,
                    onNavigate = { route ->
                        if (route != Screen.Cart.route) {
                            navController.navigate(route)
                        }
                    }
                )
            }
        },
        containerColor = AuthColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp)
        ) {
            items(cartItems) { item ->
                CartItemRow(item)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Ở đây chúng ta tạm tính dựa trên dữ liệu mẫu từ Repository
                SummarySection(subtotal = 520.0, shipping = 30.0)
            }
        }
    }
}

@Composable
fun CartItemRow(item: Book) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Book Cover Placeholder
        Box(
            modifier = Modifier
                .size(80.dp, 110.dp)
                .shadow(4.dp, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(AuthColors.Surface)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = item.author,
                fontSize = 14.sp,
                color = AuthColors.Hint
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = AuthColors.Hint)
                    }
                    Text("1", modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AuthColors.Hint)
                    }
                }
                
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = AuthColors.Hint)
                }
            }
        }
    }
}

@Composable
fun SummarySection(subtotal: Double, shipping: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = AuthColors.Primary)
                Text(String.format(Locale.US, "₫%.0f", subtotal), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Shipping", color = AuthColors.Primary)
                Text(String.format(Locale.US, "₫%.0f", shipping), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(String.format(Locale.US, "₫%.0f", subtotal + shipping), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        }
    }
}

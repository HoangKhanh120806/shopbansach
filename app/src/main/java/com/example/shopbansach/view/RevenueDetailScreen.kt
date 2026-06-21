package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.utils.CurrencyUtils
import com.example.shopbansach.viewmodel.MyShopViewModel
import com.example.shopbansach.viewmodel.factory.MyShopViewModelFactory
import com.example.shopbansach.data.repository.CloudinaryRepository
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueDetailScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MyShopViewModel = viewModel(
        factory = MyShopViewModelFactory(CloudinaryRepository(context))
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chi tiết doanh thu", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                RevenueOverviewCard(
                    total = uiState.totalRevenue,
                    delivered = uiState.deliveredRevenue,
                    pending = uiState.pendingRevenue
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Doanh thu theo sản phẩm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            val booksWithRevenue = uiState.myBooks
                .map { book -> 
                    val revenue = uiState.bookRevenues[book.id] ?: 0L
                    val soldCount = uiState.bookSoldCounts[book.id] ?: 0
                    Triple(book, revenue, soldCount)
                }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }

            if (booksWithRevenue.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có dữ liệu doanh thu", color = Color.Gray)
                    }
                }
            } else {
                items(booksWithRevenue) { (book, revenue, soldCount) ->
                    BookRevenueItem(book, revenue, soldCount)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RevenueOverviewCard(total: Long, delivered: Long, pending: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
            )
        )) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Tổng doanh thu của shop", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                Text(
                    CurrencyUtils.formatPrice(total),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    RevenueStatSmall(
                        label = "Đã thu (Thành công)",
                        value = CurrencyUtils.formatPrice(delivered),
                        color = Color(0xFF81C784),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    RevenueStatSmall(
                        label = "Đang chờ xử lý",
                        value = CurrencyUtils.formatPrice(pending),
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun RevenueStatSmall(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BookRevenueItem(book: com.example.shopbansach.data.model.Book, revenue: Long, soldCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp, 70.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Đã bán: $soldCount cuốn", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyUtils.formatPrice(revenue),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Text("Doanh thu", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

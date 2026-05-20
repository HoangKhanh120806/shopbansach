package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
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
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.utils.CurrencyUtils
import com.example.shopbansach.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = Screen.Home.route,
                onNavigate = { route ->
                    if (route != Screen.Home.route) {
                        navController.navigate(route)
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()), 
                contentPadding = PaddingValues(top = padding.calculateTopPadding())
            ) {
                item { HomeHeader(navController, uiState.currentUser) }
                item { StorySlideSection(uiState.featuredBooks, navController) }
                item { NewArrivalsHeader() }
                items(uiState.newArrivals) { book ->
                    NewArrivalItem(book, navController)
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun HomeHeader(navController: NavController, currentUser: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cozy Reads",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = currentUser?.let { "Chào mừng, ${it.name}" } ?: "Thế giới sách trong tầm tay",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.NotificationsNone, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { navController.navigate(Screen.Profile.route) },
                contentAlignment = Alignment.Center
            ) {
                if (!currentUser?.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentUser?.avatarUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun StorySlideSection(books: List<Book>, navController: NavController) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sách nổi bật",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Xem tất cả",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                FeaturedBookCard(book) {
                    navController.navigate(Screen.BookDetail.createRoute(book.id))
                }
            }
        }
    }
}

@Composable
fun FeaturedBookCard(book: Book, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!book.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = book.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = book.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
            Icon(Icons.Default.Storefront, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = book.shopName.ifEmpty { "Người bán" },
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = CurrencyUtils.formatPrice(book.price),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun NewArrivalsHeader() {
    Text(
        text = "Mới cập nhật",
        fontFamily = FontFamily.Serif,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun NewArrivalItem(book: Book, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { navController.navigate(Screen.BookDetail.createRoute(book.id)) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp, 90.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Hiển thị tên Shop thực tế với icon như trong ảnh mẫu bạn gửi
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier
                        .padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Storefront, 
                        null, 
                        tint = Color.Black.copy(alpha = 0.7f), 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = book.shopName.ifEmpty { "Người bán" }, 
                        fontSize = 14.sp, 
                        color = Color.Black.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = book.author, 
                    fontSize = 13.sp, 
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatPrice(book.price), 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color.Black, 
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Text(
                        text = " ${book.rating}", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

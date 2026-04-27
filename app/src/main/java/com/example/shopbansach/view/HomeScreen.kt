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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
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
        containerColor = AuthColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { HomeHeader(navController) }
            item { StorySlideSection(uiState.featuredBooks, navController) }
            item { NewArrivalsHeader() }
            items(uiState.newArrivals) { book ->
                NewArrivalItem(book, navController)
            }
        }
    }
}

@Composable
fun HomeHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cozy Reads",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = (-1).sp
                ),
                color = AuthColors.Primary
            )
            Text(
                text = "Thế giới sách trong tầm tay",
                style = MaterialTheme.typography.bodySmall,
                color = AuthColors.Hint
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = AuthColors.Primary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE4E2DD))
                    .clickable {
                        navController.navigate(Screen.Profile.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    tint = AuthColors.Primary
                )
            }
        }
    }
}

@Composable
fun StorySlideSection(books: List<Book>, navController: NavController) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sách nổi bật",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = AuthColors.Primary
            )
            Text(
                text = "Xem tất cả",
                style = MaterialTheme.typography.labelLarge,
                color = AuthColors.Accent,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
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
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(AuthColors.Surface)
        ) {
            Text(
                "COVER",
                modifier = Modifier.align(Alignment.Center),
                color = AuthColors.Primary.copy(alpha = 0.1f),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = book.title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AuthColors.Primary
        )
        Text(
            text = book.author,
            fontSize = 12.sp,
            color = AuthColors.Hint,
            maxLines = 1
        )
    }
}

@Composable
fun NewArrivalsHeader() {
    Text(
        text = "Mới cập nhật",
        fontFamily = FontFamily.Serif,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = AuthColors.Primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
    )
}

@Composable
fun NewArrivalItem(book: Book, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { navController.navigate(Screen.BookDetail.createRoute(book.id)) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp, 85.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuthColors.Surface)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AuthColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    fontSize = 13.sp,
                    color = AuthColors.Hint
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " ${book.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthColors.Primary
                    )
                }
            }
            Text(
                text = book.price,
                fontWeight = FontWeight.ExtraBold,
                color = AuthColors.Accent,
                fontSize = 15.sp
            )
        }
    }
}

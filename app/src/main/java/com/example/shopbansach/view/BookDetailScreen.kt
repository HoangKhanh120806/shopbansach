package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopbansach.data.repository.BookRepository
import com.example.shopbansach.ui.auth.AuthColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(navController: NavController, bookId: Int) {
    val repository = remember { BookRepository() }
    val book = repository.getBookById(bookId)

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy sách")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Đọc thử", color = Color.White, fontSize = 16.sp)
                }
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC865C)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Mua ngay", color = Color.White, fontSize = 16.sp)
                }
            }
        },
        containerColor = AuthColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book Cover Image
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(200.dp)
                    .height(300.dp)
                    .shadow(16.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                Text(
                    "COVER",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title & Author
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = AuthColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.titleMedium,
                color = AuthColors.Hint,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats (Rating, Pages, Price)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(4) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        }
                        Icon(Icons.Default.Star, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                    Text(text = "${book.rating}/5", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }

                VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = Color.LightGray)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${book.pages}", fontWeight = FontWeight.Bold)
                    Text(text = "Pages", color = AuthColors.Hint, fontSize = 14.sp)
                }

                VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = Color.LightGray)

                Text(text = book.price, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            HorizontalDivider(
                modifier = Modifier.padding(24.dp),
                thickness = 1.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            // Synopsis
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AuthColors.Primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = book.synopsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuthColors.Primary.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

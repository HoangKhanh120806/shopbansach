package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
import com.example.shopbansach.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        bottomBar = {
            BottomBar(
                currentRoute = Screen.Search.route,
                onNavigate = { route ->
                    if (route != Screen.Search.route) {
                        navController.navigate(route)
                    }
                }
            )
        },
        containerColor = AuthColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tìm kiếm sách, tác giả...", color = AuthColors.Hint) },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AuthColors.Accent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuthColors.Accent,
                        unfocusedBorderColor = AuthColors.Accent.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                    })
                )
                
                if (uiState.searchQuery.isNotEmpty()) {
                    Text(
                        text = "Hủy",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { 
                                viewModel.onSearchQueryChange("")
                                focusManager.clearFocus()
                            },
                        color = AuthColors.Hint,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.searchQuery.isEmpty()) {
                        // Trạng thái chưa tìm kiếm: Gợi ý và Thể loại
                        item {
                            Text(
                                text = "Gợi ý cho bạn",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        items(uiState.suggestions) { book ->
                            SearchItemRow(book) {
                                navController.navigate(Screen.BookDetail.createRoute(book.id))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Thể loại phổ biến",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        item {
                            CategoriesGrid()
                        }
                    } else {
                        // Trạng thái đang tìm kiếm: Kết quả tìm kiếm
                        if (uiState.searchResults.isEmpty()) {
                            item {
                                Text("Không tìm thấy kết quả phù hợp", color = AuthColors.Hint)
                            }
                        } else {
                            items(uiState.searchResults) { book ->
                                SearchItemRow(book) {
                                    navController.navigate(Screen.BookDetail.createRoute(book.id))
                                }
                            }
                        }
                        
                        item {
                            RecentSearchesSection()
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun CategoriesGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryCard(
                title = "Văn học Kinh điển",
                subtitle = "Sage",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                color = Color(0xFFAAB396),
                modifier = Modifier.weight(1.1f)
            )
            CategoryCard(
                title = "Nghệ thuật & Thiết kế",
                subtitle = "Sand",
                icon = Icons.Default.Brush,
                color = Color(0xFFD6BFA9),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    title = "Tâm lý học",
                    subtitle = "Clay",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFFC89C81)
                )
                CategoryCard(
                    title = "Lịch sử & Văn hóa",
                    subtitle = "Sage",
                    icon = Icons.Default.Museum,
                    color = Color(0xFFAAB396)
                )
            }
            CategoryCard(
                title = "Du ký & Hồi ký",
                subtitle = "Sand",
                icon = Icons.Default.AutoStories,
                color = Color(0xFFE4E2DD),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable { /* TODO */ }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                ),
                color = AuthColors.Primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AuthColors.Primary.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuthColors.Primary.copy(alpha = 0.3f),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun SearchItemRow(book: Book, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp, 85.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AuthColors.Surface)
        ) {
            if (!book.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = book.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("COVER", modifier = Modifier.align(Alignment.Center), color = AuthColors.Hint, fontSize = 10.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = AuthColors.Primary
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = AuthColors.Hint
            )
        }
    }
}

@Composable
fun RecentSearchesSection() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Tìm kiếm gần đây",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AuthColors.Primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecentSearchTag("Kinh điển")
            RecentSearchTag("Giải thưởng")
            RecentSearchTag("Tiểu thuyết")
        }
    }
}

@Composable
fun RecentSearchTag(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            textDecoration = TextDecoration.Underline
        ),
        color = AuthColors.Hint,
        modifier = Modifier.clickable { /* TODO */ }
    )
}

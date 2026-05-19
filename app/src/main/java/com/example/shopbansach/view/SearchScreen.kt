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
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
import com.example.shopbansach.utils.CurrencyUtils
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
                            CategoriesGrid(onCategoryClick = { category ->
                                viewModel.searchByCategory(category)
                                focusManager.clearFocus()
                            })
                        }
                    } else {
                        // Trạng thái đang tìm kiếm: Kết quả tìm kiếm
                        if (uiState.searchResults.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                    Text("Không tìm thấy kết quả phù hợp", color = AuthColors.Hint)
                                }
                            }
                        } else {
                            items(uiState.searchResults) { book ->
                                SearchItemRow(book) {
                                    navController.navigate(Screen.BookDetail.createRoute(book.id))
                                }
                            }
                        }
                        
                        item {
                            RecentSearchesSection(onTagClick = { tag ->
                                viewModel.onSearchQueryChange(tag)
                            })
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun CategoriesGrid(onCategoryClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryCard(
                title = "Văn học Kinh điển",
                subtitle = "Classic",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                color = Color(0xFFAAB396),
                modifier = Modifier.weight(1.1f),
                onClick = { onCategoryClick("Văn học") }
            )
            CategoryCard(
                title = "Nghệ thuật & Thiết kế",
                subtitle = "Art",
                icon = Icons.Default.Brush,
                color = Color(0xFFD6BFA9),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Nghệ thuật") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategoryCard(
                    title = "Tâm lý học",
                    subtitle = "Psychology",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFFC89C81),
                    onClick = { onCategoryClick("Tâm lý") }
                )
                CategoryCard(
                    title = "Lịch sử & Văn hóa",
                    subtitle = "History",
                    icon = Icons.Default.Museum,
                    color = Color(0xFFAAB396),
                    onClick = { onCategoryClick("Lịch sử") }
                )
            }
            CategoryCard(
                title = "Du ký & Hồi ký",
                subtitle = "Travel",
                icon = Icons.Default.AutoStories,
                color = Color(0xFFE4E2DD),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Du ký") }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                ),
                color = Color(0xFF2D2D2D) 
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2D2D2D).copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2D2D2D).copy(alpha = 0.2f),
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun SearchItemRow(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp, 85.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .shadow(1.dp, RoundedCornerShape(8.dp))
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Hiển thị tên Shop trong kết quả tìm kiếm
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = book.shopName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatPrice(book.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RecentSearchesSection(onTagClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Tìm kiếm gần đây",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecentSearchTag("Kinh điển", onClick = { onTagClick("Văn học") })
            RecentSearchTag("Giải thưởng", onClick = { onTagClick("Best seller") })
            RecentSearchTag("Tiểu thuyết", onClick = { onTagClick("Tiểu thuyết") })
        }
    }
}

@Composable
fun RecentSearchTag(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            textDecoration = TextDecoration.Underline
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable { onClick() }
    )
}

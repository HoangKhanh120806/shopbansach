package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var showFilterSheet by remember { mutableStateOf(false) }

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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar with Filter Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tìm kiếm sách, tác giả...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                    })
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (uiState.selectedCategory != "Tất cả" || uiState.minPrice != null || uiState.minRating > 0)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Tune, 
                        contentDescription = "Filter",
                        tint = if (uiState.selectedCategory != "Tất cả" || uiState.minPrice != null || uiState.minRating > 0)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val displayList = if (uiState.searchQuery.isEmpty() && uiState.selectedCategory == "Tất cả") 
                    uiState.suggestions 
                else 
                    uiState.filteredResults

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.searchQuery.isEmpty() && uiState.selectedCategory == "Tất cả") {
                        item {
                            Text(
                                text = "Gợi ý cho bạn",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        items(displayList, key = { it.id }) { book ->
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
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Kết quả tìm kiếm",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Badge { Text("${displayList.size}") }
                            }
                        }

                        if (displayList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                    Text("Không tìm thấy kết quả phù hợp", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(displayList, key = { it.id }) { book ->
                                SearchItemRow(book) {
                                    navController.navigate(Screen.BookDetail.createRoute(book.id))
                                }
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onUpdateCategory = { viewModel.updateCategory(it) },
            onUpdatePrice = { min, max -> viewModel.updatePriceRange(min, max) },
            onUpdateRating = { viewModel.updateMinRating(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: com.example.shopbansach.viewmodel.SearchUiState,
    onDismiss: () -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdatePrice: (Long?, Long?) -> Unit,
    onUpdateRating: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .fillMaxWidth()
        ) {
            Text("Bộ lọc tìm kiếm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // Categories
            Text("Thể loại", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onUpdateCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range
            Text("Khoảng giá (VNĐ)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.minPrice?.toString() ?: "",
                    onValueChange = { onUpdatePrice(it.toLongOrNull(), uiState.maxPrice) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tối thiểu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(" - ", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(
                    value = uiState.maxPrice?.toString() ?: "",
                    onValueChange = { onUpdatePrice(uiState.minPrice, it.toLongOrNull()) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Tối đa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating
            Text("Đánh giá tối thiểu", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                (0..5).forEach { star ->
                    FilterChip(
                        selected = uiState.minRating == star,
                        onClick = { onUpdateRating(star) },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (star == 0) "Tất cả" else "$star")
                                if (star > 0) {
                                    Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Áp dụng")
            }
        }
    }
}

@Composable
fun CategoriesGrid(onCategoryClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryCard(
                title = "Văn học",
                subtitle = "Literature",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                color = Color(0xFFAAB396),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Văn học") }
            )
            CategoryCard(
                title = "Kinh tế",
                subtitle = "Economy",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFFD6BFA9),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Kinh tế") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryCard(
                title = "Tâm lý",
                subtitle = "Psychology",
                icon = Icons.Default.Psychology,
                color = Color(0xFFC89C81),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Tâm lý") }
            )
            CategoryCard(
                title = "Kỹ năng sống",
                subtitle = "Life Skills",
                icon = Icons.Default.Lightbulb,
                color = Color(0xFFAAB396),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Kỹ năng sống") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CategoryCard(
                title = "Thiếu nhi",
                subtitle = "Children",
                icon = Icons.Default.ChildCare,
                color = Color(0xFFE4E2DD),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Thiếu nhi") }
            )
            CategoryCard(
                title = "Truyện tranh",
                subtitle = "Comics",
                icon = Icons.Default.AutoStories,
                color = Color(0xFFD6BFA9),
                modifier = Modifier.weight(1f),
                onClick = { onCategoryClick("Truyện tranh") }
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
                color = Color.Black // Văn bản trên nền màu pastel nên để đen/đậm cho dễ đọc
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.2f),
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
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp, 95.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .shadow(1.dp, RoundedCornerShape(8.dp))
            ) {
                if (book.imageUrl != null) {
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
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Storefront, 
                        null, 
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = book.shopName.ifEmpty { "Người bán" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatPrice(book.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star, 
                        null, 
                        tint = Color(0xFFFFB300), 
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (book.rating > 0) " ${book.rating}" else " Chưa có", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

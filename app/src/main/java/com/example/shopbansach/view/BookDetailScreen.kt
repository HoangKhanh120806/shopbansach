package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.model.User
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.viewmodel.BookDetailViewModel
import com.example.shopbansach.viewmodel.CartActionState
import com.example.shopbansach.viewmodel.CartViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    bookId: String,
    viewModel: BookDetailViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showQuantitySheet by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(bookId) {
        viewModel.getBookDetail(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Wishlist */ },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (uiState.book != null) {
                BottomActionSection(
                    book = uiState.book!!,
                    isAdding = cartUiState.actionState is CartActionState.Loading,
                    onAddToCart = { 
                        cartViewModel.addToCart(uiState.book!!) 
                    },
                    onBuyNow = {
                        showQuantitySheet = true
                    }
                )
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
            uiState.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.book != null -> {
                val book = uiState.book!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                        .verticalScroll(scrollState)
                ) {
                    BookHeaderSection(book)

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                Text(book.category, modifier = Modifier.padding(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // THÔNG TIN NGƯỜI BÁN
                        SellerInfoSection(uiState.seller) {
                            if (uiState.seller != null) {
                                navController.navigate(Screen.SellerShop.createRoute(uiState.seller!!.id))
                            } else {
                                Toast.makeText(context, "Không thể tải thông tin Shop. Vui lòng kiểm tra kết nối!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(label = "Đánh giá", value = "${book.rating}", icon = Icons.Default.Star, iconColor = Color(0xFFFFB300))
                            StatItem(label = "Số trang", value = "${book.pages}", icon = Icons.AutoMirrored.Filled.ArrowBack) 
                            StatItem(label = "Tồn kho", value = "${book.stock}", icon = Icons.Default.ShoppingCart)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                        Text(
                            text = "Tóm tắt nội dung",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = book.synopsis,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Justify
                        )
                        
                        if (uiState.relatedBooks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Sách liên quan",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(uiState.relatedBooks) { relatedBook ->
                                    RelatedBookCard(relatedBook) {
                                        navController.navigate(Screen.BookDetail.createRoute(relatedBook.id))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuantitySheet && uiState.book != null) {
        val book = uiState.book!!
        ModalBottomSheet(
            onDismissRequest = { showQuantitySheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            QuantitySelectionContent(
                book = book,
                quantity = selectedQuantity,
                onQuantityChange = { selectedQuantity = it },
                onConfirm = {
                    showQuantitySheet = false
                    navController.navigate(Screen.Checkout.createRoute(bookId = book.id, quantity = selectedQuantity))
                }
            )
        }
    }
}

@Composable
fun SellerInfoSection(seller: User?, onVisitShop: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onVisitShop() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!seller?.shopAvatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = seller?.shopAvatarUrl,
                        contentDescription = "Shop Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = seller?.shopName ?: seller?.name ?: "Đang tải thông tin người bán...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (seller != null) "Đang hoạt động" else "Vui lòng đợi",
                    color = if (seller != null) Color(0xFF4CAF50) else Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = onVisitShop) {
                Text("Xem Shop", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuantitySelectionContent(
    book: Book,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = String.format(Locale.US, "%,dđ", book.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text("Kho: ${book.stock}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Số lượng", fontWeight = FontWeight.Medium)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                    enabled = quantity > 1
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Giảm")
                }
                
                Text(
                    text = "$quantity",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                
                IconButton(
                    onClick = { if (quantity < book.stock) onQuantityChange(quantity + 1) },
                    enabled = quantity < book.stock
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tăng")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("XÁC NHẬN", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookHeaderSection(book: Book) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
    ) {
        AsyncImage(
            model = book.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 300f
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 40.dp)
                .width(180.dp)
                .height(260.dp)
                .shadow(20.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BottomActionSection(book: Book, isAdding: Boolean, onAddToCart: () -> Unit, onBuyNow: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Giá bán", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = String.format(Locale.US, "%,dđ", book.price),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                enabled = !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Button(
                onClick = onBuyNow,
                modifier = Modifier
                    .weight(1.5f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("MUA NGAY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RelatedBookCard(book: Book, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = book.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = book.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = String.format(Locale.US, "%,dđ", book.price),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

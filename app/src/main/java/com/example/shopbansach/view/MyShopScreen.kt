package com.example.shopbansach.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.utils.CurrencyUtils
import com.example.shopbansach.viewmodel.MyShopViewModel
import com.example.shopbansach.viewmodel.factory.MyShopViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyShopScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: MyShopViewModel = viewModel(
        factory = MyShopViewModelFactory(CloudinaryRepository(context))
    )

    val uiState by viewModel.uiState.collectAsState()
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var showEditShopNameDialog by remember { mutableStateOf(false) }
    var newShopName by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateShopAvatar(it) }
    }

    LaunchedEffect(uiState.currentUser) {
        newShopName = uiState.currentUser?.shopName ?: uiState.currentUser?.name ?: "Shop của tôi"
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    if (showEditShopNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditShopNameDialog = false },
            title = { Text("Đổi tên Shop") },
            text = {
                OutlinedTextField(
                    value = newShopName,
                    onValueChange = { newShopName = it },
                    label = { Text("Tên shop mới") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateShopName(newShopName)
                    showEditShopNameDialog = false
                }) { Text("Lưu thay đổi") }
            },
            dismissButton = {
                TextButton(onClick = { showEditShopNameDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (bookToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa cuốn sách '${bookToDelete?.title}' này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookToDelete?.let { viewModel.deleteBook(it.id) }
                        bookToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quản lý Shop", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddBook.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm sách mới") }
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    ShopHeaderSection(
                        shopName = uiState.currentUser?.shopName ?: uiState.currentUser?.name ?: "Shop của tôi",
                        shopAvatarUrl = uiState.currentUser?.shopAvatarUrl,
                        onEditClick = { showEditShopNameDialog = true },
                        onChangeAvatarClick = { photoPickerLauncher.launch("image/*") },
                        isUpdating = uiState.isUpdating
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Tổng quan kinh doanh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    
                    ShopDashboard(uiState, onManageOrders = {
                        navController.navigate(Screen.SellerOrderManage.route)
                    })
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sách đang bán (${uiState.myBooks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.myBooks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Bạn chưa đăng bán cuốn sách nào", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(uiState.myBooks) { book ->
                        MyProductItem(
                            book = book, 
                            onEdit = { navController.navigate(Screen.EditBook.createRoute(book.id)) },
                            onDelete = { bookToDelete = book }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ShopDashboard(uiState: com.example.shopbansach.viewmodel.MyShopUiState, onManageOrders: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Doanh thu thực",
                value = CurrencyUtils.formatPrice(uiState.deliveredRevenue),
                icon = Icons.Default.AccountBalanceWallet,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Đang chờ",
                value = CurrencyUtils.formatPrice(uiState.pendingRevenue),
                icon = Icons.Default.PendingActions,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Bán hôm nay",
                value = "${uiState.soldToday} cuốn",
                icon = Icons.Default.Today,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            StatCard(
                modifier = Modifier.weight(1f).clickable { onManageOrders() },
                title = "Đơn mới",
                value = "${uiState.newOrdersCount} đơn",
                icon = Icons.Default.FiberNew,
                containerColor = Color(0xFFFFE0B2),
                contentColor = Color(0xFFE65100)
            )
        }

        Button(
            onClick = onManageOrders,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Quản lý tất cả đơn hàng")
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = contentColor.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun ShopHeaderSection(
    shopName: String, 
    shopAvatarUrl: String?, 
    onEditClick: () -> Unit,
    onChangeAvatarClick: () -> Unit,
    isUpdating: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable { onChangeAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!shopAvatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = shopAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Storefront, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                }
                
                if (isUpdating) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = shopName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Chủ cửa hàng", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun MyProductItem(book: Book, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp, 75.dp).clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = CurrencyUtils.formatPrice(book.price), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Text(text = "Kho: ${book.stock}", fontSize = 12.sp, color = Color.Gray)
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

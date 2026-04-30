package com.example.shopbansach.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopbansach.data.model.Address
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.utils.CurrencyUtils
import com.example.shopbansach.utils.CustomCard
import com.example.shopbansach.utils.PrimaryButton
import com.example.shopbansach.utils.SharedTopAppBar
import com.example.shopbansach.viewmodel.AddressViewModel
import com.example.shopbansach.viewmodel.BookDetailViewModel
import com.example.shopbansach.viewmodel.CartViewModel

@Composable
fun CheckoutScreen(
    navController: NavController,
    buyNowBookId: String? = null,
    buyNowQuantity: Int = 1,
    cartViewModel: CartViewModel = viewModel(),
    addressViewModel: AddressViewModel = viewModel(),
    bookViewModel: BookDetailViewModel = viewModel()
) {
    val cartUiState by cartViewModel.uiState.collectAsState()
    val addressUiState by addressViewModel.uiState.collectAsState()
    val bookUiState by bookViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("cod") }

    // Xử lý lỗi và Reset trạng thái
    LaunchedEffect(cartUiState.errorMessage) {
        cartUiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            cartViewModel.resetActionState()
        }
    }

    // Load dữ liệu ban đầu
    LaunchedEffect(Unit) {
        addressViewModel.loadAddresses()
        if (buyNowBookId != null) {
            bookViewModel.getBookDetail(buyNowBookId)
        } else {
            cartViewModel.loadCartItems()
        }
    }

    // Tự động điền địa chỉ mặc định khi danh sách tải xong
    LaunchedEffect(addressUiState.addresses) {
        val defaultAddress = addressUiState.addresses.find { it.isDefault } 
            ?: addressUiState.addresses.firstOrNull()
        
        defaultAddress?.let {
            fullName = it.fullName
            phoneNumber = it.phoneNumber
            addressDetail = it.addressDetail
            city = it.city
        }
    }

    // Xác định danh sách sản phẩm thanh toán
    val checkoutItems = remember(buyNowBookId, bookUiState.book, cartUiState.cartItems) {
        if (buyNowBookId != null && bookUiState.book != null) {
            listOf(
                CartItem(
                    bookId = bookUiState.book!!.id,
                    title = bookUiState.book!!.title,
                    price = bookUiState.book!!.price,
                    imageUrl = bookUiState.book!!.imageUrl,
                    quantity = buyNowQuantity,
                    author = bookUiState.book!!.author,
                    ownerId = bookUiState.book!!.ownerId,
                    isSelected = true
                )
            )
        } else if (buyNowBookId == null) {
            cartUiState.cartItems.filter { it.isSelected && it.stock > 0 }
        } else {
            emptyList()
        }
    }

    val subtotal = checkoutItems.sumOf { it.price * it.quantity }
    val shipping = if (subtotal > 0) 30000L else 0L
    val total = subtotal + shipping

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = "Thanh toán",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            if (checkoutItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface, 
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tổng thanh toán", style = MaterialTheme.typography.titleMedium)
                            Text(
                                CurrencyUtils.formatPrice(total), 
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold, 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        PrimaryButton(
                            text = "XÁC NHẬN ĐẶT HÀNG",
                            onClick = {
                                if (fullName.isBlank() || phoneNumber.isBlank() || addressDetail.isBlank()) {
                                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                                } else {
                                    cartViewModel.processCheckout(
                                        checkoutItems = checkoutItems,
                                        isBuyNow = buyNowBookId != null,
                                        address = Address(
                                            fullName = fullName,
                                            phoneNumber = phoneNumber,
                                            addressDetail = addressDetail,
                                            city = city
                                        ),
                                        paymentMethod = selectedPayment,
                                        totalPrice = total
                                    ) { orderId ->
                                        navController.navigate(Screen.ThankYou.createRoute(orderId)) {
                                            popUpTo(Screen.Home.route) { inclusive = false }
                                        }
                                    }
                                }
                            },
                            isLoading = cartUiState.isLoading,
                            enabled = checkoutItems.isNotEmpty()
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val isDataLoading = if (buyNowBookId != null) bookUiState.isLoading else cartUiState.isLoading

        if (isDataLoading && checkoutItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Địa chỉ giao hàng") {
                    Text(
                        "Thay đổi",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate(Screen.AddressList.route) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CheckoutTextField(fullName, { fullName = it }, "Họ và tên người nhận", imeAction = ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutTextField(phoneNumber, { phoneNumber = it }, "Số điện thoại", KeyboardType.Phone, ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutTextField(addressDetail, { addressDetail = it }, "Địa chỉ chi tiết", imeAction = ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutTextField(city, { city = it }, "Tỉnh/Thành phố", imeAction = ImeAction.Done) { focusManager.clearFocus() }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Phương thức thanh toán")
                Spacer(modifier = Modifier.height(12.dp))
                PaymentOption(selectedPayment == "cod", { selectedPayment = "cod" }, { Icon(Icons.Default.CreditCard, null) }, "Tiền mặt khi nhận hàng (COD)", null)
                PaymentOption(selectedPayment == "momo", { selectedPayment = "momo" }, { Box(Modifier.size(24.dp).background(Color(0xFFA50064), RoundedCornerShape(4.dp))) }, "Ví điện tử Momo", "Khuyên dùng")

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Chi tiết đơn hàng")
                CustomCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        checkoutItems.forEach { item ->
                            OrderItemRow(item)
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                        
                        SummaryRow("Tiền hàng", CurrencyUtils.formatPrice(subtotal))
                        SummaryRow("Phí vận chuyển", CurrencyUtils.formatPrice(shipping))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng cộng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(CurrencyUtils.formatPrice(total), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OrderItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp, 60.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, maxLines = 1, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Số lượng: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
        }
        Text(CurrencyUtils.formatPrice(item.price * item.quantity), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun SectionTitle(title: String, trailing: @Composable (RowScope.() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        trailing?.invoke(this)
    }
}

@Composable
fun CheckoutTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, imeAction: ImeAction = ImeAction.Default, onAction: () -> Unit = {}) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(placeholder, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onAny = { onAction() })
    )
}

@Composable
fun PaymentOption(selected: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit, title: String, subtitle: String?) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)),
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

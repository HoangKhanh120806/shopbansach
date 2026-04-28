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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopbansach.data.model.CartItem
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors
import com.example.shopbansach.viewmodel.AddressViewModel
import com.example.shopbansach.viewmodel.BookDetailViewModel
import com.example.shopbansach.viewmodel.CartViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    // Load dữ liệu
    LaunchedEffect(Unit) {
        addressViewModel.loadAddresses()
        if (buyNowBookId != null) {
            bookViewModel.getBookDetail(buyNowBookId)
        }
    }

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

    // XÁC ĐỊNH DANH SÁCH SẢN PHẨM THANH TOÁN
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
                    isSelected = true
                )
            )
        } else if (buyNowBookId == null) {
            cartUiState.cartItems.filter { it.isSelected }
        } else {
            emptyList()
        }
    }

    val subtotal = checkoutItems.sumOf { it.price * it.quantity }
    val shipping = if (subtotal > 0) 30000L else 0L
    val total = subtotal + shipping

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thanh toán", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AuthColors.Background)
            )
        },
        bottomBar = {
            Surface(color = AuthColors.Background, tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        if (fullName.isEmpty() || phoneNumber.isEmpty() || addressDetail.isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show()
                        } else {
                            if (buyNowBookId != null) {
                                // Nếu mua ngay, không cần xóa giỏ hàng
                                navController.navigate(Screen.ThankYou.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            } else {
                                // Xóa những món ĐÃ CHỌN trong giỏ hàng sau khi đặt hàng xong
                                cartViewModel.clearSelectedItems {
                                    navController.navigate(Screen.ThankYou.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = checkoutItems.isNotEmpty()
                ) {
                    Text("Xác nhận đặt hàng", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = AuthColors.Background
    ) { padding ->
        if (buyNowBookId != null && bookUiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Địa chỉ giao hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Chọn địa chỉ khác",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { navController.navigate(Screen.AddressList.route) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CheckoutTextField(fullName, { fullName = it }, "Họ và tên người nhận", imeAction = ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutTextField(phoneNumber, { phoneNumber = it }, "Số điện thoại", KeyboardType.Phone, ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutTextField(addressDetail, { addressDetail = it }, "Địa chỉ chi tiết (Số nhà, tên đường)", imeAction = ImeAction.Next) { focusManager.moveFocus(FocusDirection.Down) }
                Spacer(modifier = Modifier.height(8.dp))
                CheckoutTextField(city, { city = it }, "Tỉnh/Thành phố", imeAction = ImeAction.Done) { focusManager.clearFocus() }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Phương thức thanh toán", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                PaymentOption(selectedPayment == "cod", { selectedPayment = "cod" }, { Icon(Icons.Default.CreditCard, null) }, "Thanh toán khi nhận hàng (COD)", null)
                PaymentOption(selectedPayment == "momo", { selectedPayment = "momo" }, { Box(Modifier.size(24.dp).background(Color(0xFFA50064), RoundedCornerShape(4.dp))) }, "Ví điện tử Momo", "Khuyên dùng")

                Spacer(modifier = Modifier.height(24.dp))

                Text("Sản phẩm thanh toán (${checkoutItems.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                checkoutItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity}x ${item.title}", modifier = Modifier.weight(1f), maxLines = 1, fontSize = 14.sp)
                        Text(String.format(Locale.US, "%,dđ", item.price * item.quantity), fontSize = 14.sp)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                SummaryRow("Tạm tính:", String.format(Locale.US, "%,dđ", subtotal))
                SummaryRow("Phí vận chuyển:", String.format(Locale.US, "%,dđ", shipping))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng cộng:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(String.format(Locale.US, "%,dđ", total), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CheckoutTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, imeAction: ImeAction = ImeAction.Default, onAction: () -> Unit = {}) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onAny = { onAction() })
    )
}

@Composable
fun PaymentOption(selected: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit, title: String, subtitle: String?) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)),
        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

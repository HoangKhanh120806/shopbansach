package com.example.shopbansach.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.shopbansach.navigation.Screen
import com.example.shopbansach.ui.auth.AuthColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var saveAddress by remember { mutableStateOf(true) }
    var selectedPayment by remember { mutableStateOf("card") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Secure Checkout",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AuthColors.Background
                )
            )
        },
        bottomBar = {
            Surface(
                color = AuthColors.Background,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = { navController.navigate(Screen.ThankYou.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Đặt hàng ngay",
                        color = Color(0xFFD6BFA9),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = AuthColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section: Địa chỉ giao hàng
            Text(
                "Địa chỉ giao hàng",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AuthColors.Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            CheckoutTextField(value = fullName, onValueChange = { fullName = it }, placeholder = "Họ và tên")
            Spacer(modifier = Modifier.height(8.dp))
            CheckoutTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = "Số điện thoại")
            Spacer(modifier = Modifier.height(8.dp))
            CheckoutTextField(value = addressDetail, onValueChange = { addressDetail = it }, placeholder = "Địa chỉ chi tiết")
            Spacer(modifier = Modifier.height(8.dp))
            CheckoutTextField(value = city, onValueChange = { city = it }, placeholder = "Tỉnh/Thành phố")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = saveAddress,
                    onCheckedChange = { saveAddress = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFAC865C))
                )
                Text("Lưu địa chỉ này", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Phương thức thanh toán
            Text(
                "Phương thức thanh toán",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AuthColors.Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            PaymentOption(
                selected = selectedPayment == "card",
                onClick = { selectedPayment = "card" },
                icon = { Icon(Icons.Default.CreditCard, null, modifier = Modifier.size(24.dp)) },
                title = "Thẻ tín dụng/Ghi nợ (Visa, Mastercard)",
                subtitle = "•••• 4567"
            )
            
            PaymentOption(
                selected = selectedPayment == "momo",
                onClick = { selectedPayment = "momo" },
                icon = { 
                    Box(modifier = Modifier.size(24.dp).background(Color(0xFFA50064), RoundedCornerShape(4.dp)))
                },
                title = "Ví Momo",
                subtitle = null
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Tóm tắt đơn hàng
            Text(
                "Tóm tắt đơn hàng",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AuthColors.Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            SummaryRow("Sách: The Silent Patient", "- ₫250.000")
            SummaryRow("Sách: Dune", "- ₫320.000")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            SummaryRow("Tạm tính:", "₫570.000")
            SummaryRow("Phí vận chuyển:", "₫30.000")
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tổng cộng:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("₫600.000", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AuthColors.Primary)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CheckoutTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFD6BFA9).copy(alpha = 0.5f),
            focusedBorderColor = Color(0xFFD6BFA9)
        ),
        singleLine = true
    )
}

@Composable
fun PaymentOption(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFAC865C))
        )
        Spacer(modifier = Modifier.width(8.dp))
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp)
        Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

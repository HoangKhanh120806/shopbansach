package com.example.shopbansach.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthTextField(
    value: String,
    hint: String,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        // Label sẽ tự động nhảy lên đường viền khi có dữ liệu hoặc được focus
        label = { 
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        placeholder = { 
            Text(
                text = "Nhập $hint...", 
                color = AuthColors.Hint.copy(alpha = 0.5f)
            ) 
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = AuthColors.Text),
        
        // Logic ẩn/hiện mật khẩu
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = AuthColors.Hint)
                }
            }
        },
        
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AuthColors.Primary,
            unfocusedBorderColor = AuthColors.Hint.copy(alpha = 0.3f),
            focusedLabelColor = AuthColors.Primary,
            unfocusedLabelColor = AuthColors.Hint,
            // Đảm bảo nền trong suốt để thấy rõ đường viền và nhãn nổi
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = AuthColors.Primary
        ),
        singleLine = true
    )
}

@Composable
fun SearchTextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(color = AuthColors.Text),
        placeholder = { Text("Tìm sách...", color = AuthColors.Hint) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AuthColors.Primary.copy(alpha = 0.6f)
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE4E2DD),
            unfocusedContainerColor = Color(0xFFE4E2DD),
            disabledContainerColor = Color(0xFFE4E2DD),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = modifier,
        singleLine = true
    )
}

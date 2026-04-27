package com.example.shopbansach.ui.auth


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun AuthTextField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = AuthColors.Text),
            modifier = Modifier
                .fillMaxWidth()
                .background(AuthColors.Surface)
                .padding(16.dp)
        ) { inner ->
            if (value.isEmpty()) {
                Text(text = hint, color = AuthColors.Hint)
            }
            inner()
        }
    }
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

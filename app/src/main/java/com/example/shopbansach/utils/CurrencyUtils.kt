package com.example.shopbansach.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatPrice(price: Long): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        return "${formatter.format(price)}đ"
    }

    fun formatPriceWithVND(price: Long): String {
        val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
        return "${formatter.format(price)} VND"
    }
}

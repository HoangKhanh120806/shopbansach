package com.example.shopbansach.utils

import android.util.Patterns

object Validator {

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email không được để trống"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Email không đúng định dạng"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Mật khẩu không được để trống"
        if (password.length < 6) return "Mật khẩu phải có ít nhất 6 ký tự"
        return null
    }

    fun validateName(name: String): String? {
        if (name.isBlank()) return "Tên không được để trống"
        if (name.length < 2) return "Tên quá ngắn"
        return null
    }

    fun validateBookTitle(title: String): String? {
        if (title.isBlank()) return "Tên sách không được để trống"
        return null
    }

    fun validatePrice(price: Long): String? {
        if (price <= 0) return "Giá sách phải lớn hơn 0"
        return null
    }

    fun validateStock(stock: Int): String? {
        if (stock < 0) return "Số lượng tồn kho không được âm"
        return null
    }
}

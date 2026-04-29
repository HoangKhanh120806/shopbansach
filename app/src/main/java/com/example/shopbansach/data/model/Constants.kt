package com.example.shopbansach.data.model

object Constants {
    val BOOK_CATEGORIES = listOf(
        "Văn học", 
        "Kinh tế", 
        "Tâm lý", 
        "Kỹ năng sống", 
        "Thiếu nhi", 
        "Ngoại ngữ", 
        "Truyện tranh",
        "Khác"
    )

    object OrderStatus {
        const val PENDING = "Chờ xác nhận"
        const val SHIPPING = "Đang giao"
        const val COMPLETED = "Đã giao"
        const val CANCELLED = "Hủy"
    }
}

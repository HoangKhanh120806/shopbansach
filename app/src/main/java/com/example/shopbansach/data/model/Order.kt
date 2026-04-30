package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class Order(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("items")
    @set:PropertyName("items")
    var items: List<CartItem> = emptyList(),

    @get:PropertyName("totalPrice")
    @set:PropertyName("totalPrice")
    var totalPrice: Long = 0,

    @get:PropertyName("shippingAddress")
    @set:PropertyName("shippingAddress")
    var shippingAddress: Address = Address(),

    @get:PropertyName("paymentMethod")
    @set:PropertyName("paymentMethod")
    var paymentMethod: String = "cod",

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "Chờ xác nhận",

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)

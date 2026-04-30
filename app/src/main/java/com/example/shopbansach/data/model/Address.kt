package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class Address(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("fullName") @set:PropertyName("fullName")
    var fullName: String = "",

    @get:PropertyName("phoneNumber") @set:PropertyName("phoneNumber")
    var phoneNumber: String = "",

    @get:PropertyName("addressDetail") @set:PropertyName("addressDetail")
    var addressDetail: String = "",

    @get:PropertyName("city") @set:PropertyName("city")
    var city: String = "",

    @get:PropertyName("isDefault")
    @set:PropertyName("isDefault")
    var isDefault: Boolean = false
)

package com.example.shopbansach.data.model

import com.google.firebase.firestore.PropertyName

data class Review(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    
    @get:PropertyName("bookId") @set:PropertyName("bookId")
    var bookId: String = "",
    
    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",
    
    @get:PropertyName("userName") @set:PropertyName("userName")
    var userName: String = "",
    
    @get:PropertyName("userAvatarUrl") @set:PropertyName("userAvatarUrl")
    var userAvatarUrl: String? = null,
    
    @get:PropertyName("rating") @set:PropertyName("rating")
    var rating: Int = 5,
    
    @get:PropertyName("comment") @set:PropertyName("comment")
    var comment: String = "",
    
    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)

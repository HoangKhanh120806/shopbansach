package com.example.shopbansach.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.viewmodel.AdminUserViewModel

class AdminUserViewModelFactory(private val cloudinaryRepository: CloudinaryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserViewModel(cloudinaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.shopbansach.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shopbansach.data.repository.CloudinaryRepository
import com.example.shopbansach.viewmodel.MyShopViewModel

class MyShopViewModelFactory(private val cloudinaryRepository: CloudinaryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyShopViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyShopViewModel(cloudinaryRepository = cloudinaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

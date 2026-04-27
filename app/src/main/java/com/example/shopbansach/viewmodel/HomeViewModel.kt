package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val featuredBooks: List<Book> = emptyList(),
    val newArrivals: List<Book> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val repository = BookRepository()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = HomeUiState(
            featuredBooks = repository.getFeaturedBooks(),
            newArrivals = repository.getNewArrivals()
        )
    }
}

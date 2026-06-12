package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchResults: List<Book> = emptyList(),
    val filteredResults: List<Book> = emptyList(),
    val suggestions: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    // Filter states
    val selectedCategory: String = "Tất cả",
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val minRating: Int = 0,
    val categories: List<String> = listOf("Tất cả", "Văn học", "Kinh tế", "Kỹ năng sống", "Thiếu nhi", "Tiểu thuyết", "Sách giáo khoa")
)

class SearchViewModel(private val repository: FirebaseBookRepository = FirebaseBookRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null

    init {
        loadSuggestions()
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val suggestions = repository.getFeaturedBooks().take(3)
            _uiState.update { it.copy(suggestions = suggestions, isLoading = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        searchJob?.cancel()
        
        if (query.isEmpty() && _uiState.value.selectedCategory == "Tất cả") {
            _uiState.update { it.copy(searchResults = emptyList(), filteredResults = emptyList(), isLoading = false) }
        } else {
            searchJob = viewModelScope.launch {
                delay(500)
                searchBooks(query)
            }
        }
    }

    private suspend fun searchBooks(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        val results = if (query.isEmpty() && _uiState.value.selectedCategory != "Tất cả") {
             repository.getBooksByCategory(_uiState.value.selectedCategory)
        } else {
            repository.searchBooks(query)
        }
        _uiState.update { 
            it.copy(
                searchResults = results, 
                isLoading = false 
            ) 
        }
        applyFilters()
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            searchBooks(_uiState.value.searchQuery)
        }
    }

    fun updatePriceRange(min: Long?, max: Long?) {
        _uiState.update { it.copy(minPrice = min, maxPrice = max) }
        applyFilters()
    }

    fun updateMinRating(rating: Int) {
        _uiState.update { it.copy(minRating = rating) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentResults = _uiState.value.searchResults
        val minPrice = _uiState.value.minPrice ?: 0L
        val maxPrice = _uiState.value.maxPrice ?: Long.MAX_VALUE
        val minRating = _uiState.value.minRating
        val category = _uiState.value.selectedCategory

        val filtered = currentResults.filter { book ->
            val matchesCategory = if (category == "Tất cả") true else book.category == category
            val matchesPrice = book.price in minPrice..maxPrice
            val matchesRating = book.rating >= minRating
            
            matchesCategory && matchesPrice && matchesRating
        }

        _uiState.update { it.copy(filteredResults = filtered) }
    }

    fun searchByCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            searchBooks(_uiState.value.searchQuery)
        }
    }
}

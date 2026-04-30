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
    val suggestions: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
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
            val suggestions = repository.getFeaturedBooks()
            _uiState.update { it.copy(suggestions = suggestions, isLoading = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
        } else {
            searchJob = viewModelScope.launch {
                delay(500)
                searchBooks(query)
            }
        }
    }

    private suspend fun searchBooks(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        val results = repository.searchBooks(query)
        _uiState.update { it.copy(searchResults = results, isLoading = false) }
    }

    // TÍNH NĂNG MỚI: Tìm kiếm theo thể loại
    fun searchByCategory(category: String) {
        _uiState.update { it.copy(searchQuery = category, isLoading = true) }
        viewModelScope.launch {
            // Giả sử repository chưa có hàm chuyên biệt, ta dùng hàm searchBooks hiện có 
            // vì nó đã hỗ trợ tìm kiếm theo chuỗi (thể loại cũng là chuỗi)
            val results = repository.searchBooks(category)
            _uiState.update { it.copy(searchResults = results, isLoading = false) }
        }
    }
}

package com.example.shopbansach.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopbansach.data.model.Book
import com.example.shopbansach.data.repository.FirebaseBookRepository
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
        if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        } else {
            searchBooks(query)
        }
    }

    private fun searchBooks(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val results = repository.searchBooks(query)
            _uiState.update { it.copy(searchResults = results, isLoading = false) }
        }
    }
}

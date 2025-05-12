package com.marketplace.presentation.viewmodel.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.model.Product
import com.marketplace.domain.repository.ProductRepository
import com.marketplace.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val popularCategories: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortOptions: List<String> = listOf(
        "Relevance",
        "Price: Low to High",
        "Price: High to Low",
        "Newest First"
    ),
    val selectedSortOption: String = "Relevance"
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            if (query.isNotEmpty()) {
                performSearch()
            }
        }
    }

    fun clearSearch() {
        _state.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                val history = searchRepository.getSearchHistory()
                val categories = productRepository.getCategories()
                val popularCategories = productRepository.getPopularCategories()
                _state.update {
                    it.copy(
                        searchHistory = history,
                        categories = categories,
                        popularCategories = popularCategories
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to load search history")
                }
            }
        }
    }

    fun removeFromHistory(query: String) {
        viewModelScope.launch {
            try {
                searchRepository.removeFromHistory(query)
                _state.update {
                    it.copy(
                        searchHistory = it.searchHistory.filter { it != query }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to remove from history")
                }
            }
        }
    }

    fun updateMinPrice(price: Double?) {
        _state.update { it.copy(minPrice = price) }
    }

    fun updateMaxPrice(price: Double?) {
        _state.update { it.copy(maxPrice = price) }
    }

    fun toggleCategory(category: String, selected: Boolean) {
        _state.update {
            it.copy(
                selectedCategories = if (selected) {
                    it.selectedCategories + category
                } else {
                    it.selectedCategories - category
                }
            )
        }
    }

    fun updateSortOption(option: String) {
        _state.update { it.copy(selectedSortOption = option) }
    }

    fun applyFilters() {
        performSearch()
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                minPrice = null,
                maxPrice = null,
                selectedCategories = emptySet(),
                selectedSortOption = "Relevance"
            )
        }
        performSearch()
    }

    private fun performSearch() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val results = productRepository.searchProducts(
                    query = state.value.searchQuery,
                    categories = state.value.selectedCategories.toList(),
                    minPrice = state.value.minPrice,
                    maxPrice = state.value.maxPrice,
                    sortOption = state.value.selectedSortOption
                )

                if (state.value.searchQuery.isNotEmpty()) {
                    searchRepository.addToHistory(state.value.searchQuery)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        searchResults = results
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to perform search"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 
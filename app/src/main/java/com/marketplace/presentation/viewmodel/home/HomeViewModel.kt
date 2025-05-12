package com.marketplace.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.model.Category
import com.marketplace.domain.model.Product
import com.marketplace.domain.repository.ProductRepository
import com.marketplace.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val featuredProducts: List<Product> = emptyList(),
    val recentProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Load data in parallel
                val featuredProductsDeferred = kotlinx.coroutines.async {
                    productRepository.getFeaturedProducts()
                }
                val recentProductsDeferred = kotlinx.coroutines.async {
                    productRepository.getRecentProducts()
                }
                val categoriesDeferred = kotlinx.coroutines.async {
                    categoryRepository.getCategories()
                }

                // Wait for all data to load
                val featuredProducts = featuredProductsDeferred.await()
                val recentProducts = recentProductsDeferred.await()
                val categories = categoriesDeferred.await()

                _state.update {
                    it.copy(
                        isLoading = false,
                        featuredProducts = featuredProducts,
                        recentProducts = recentProducts,
                        categories = categories
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load data"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun refreshData() {
        loadHomeData()
    }
} 
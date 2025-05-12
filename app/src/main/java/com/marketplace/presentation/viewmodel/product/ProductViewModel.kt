package com.marketplace.presentation.viewmodel.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.models.Product
import com.marketplace.domain.usecase.product.AddProductUseCase
import com.marketplace.domain.usecase.product.GetProductsUseCase
import com.marketplace.domain.usecase.product.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _productState = MutableStateFlow<ProductState>(ProductState.Initial)
    val productState: StateFlow<ProductState> = _productState

    fun loadProducts(
        category: String? = null,
        brand: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null
    ) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            getProductsUseCase(category, brand, minPrice, maxPrice, sortBy)
                .catch { e ->
                    _productState.value = ProductState.Error(e.message ?: "Failed to load products")
                }
                .collect { products ->
                    _products.value = products
                    _productState.value = ProductState.Success
                }
        }
    }

    fun addProduct(
        name: String,
        description: String,
        price: Double,
        category: String,
        brand: String,
        images: List<String>,
        stock: Int,
        sellerId: String
    ) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            addProductUseCase(name, description, price, category, brand, images, stock, sellerId)
                .onSuccess {
                    _productState.value = ProductState.Success
                }
                .onFailure { e ->
                    _productState.value = ProductState.Error(e.message ?: "Failed to add product")
                }
        }
    }

    fun updateProduct(
        productId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        category: String? = null,
        brand: String? = null,
        images: List<String>? = null,
        stock: Int? = null
    ) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            updateProductUseCase(productId, name, description, price, category, brand, images, stock)
                .onSuccess {
                    _productState.value = ProductState.Success
                }
                .onFailure { e ->
                    _productState.value = ProductState.Error(e.message ?: "Failed to update product")
                }
        }
    }
}

sealed class ProductState {
    object Initial : ProductState()
    object Loading : ProductState()
    object Success : ProductState()
    data class Error(val message: String) : ProductState()
} 
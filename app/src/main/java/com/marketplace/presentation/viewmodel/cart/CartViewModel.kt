package com.marketplace.presentation.viewmodel.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.model.CartItem
import com.marketplace.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val showClearCartConfirmation: Boolean = false,
    val subtotal: Double = 0.0,
    val shippingCost: Double = 5.99,
    val tax: Double = 0.0,
    val total: Double = 0.0
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    fun loadCart() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val cartItems = cartRepository.getCartItems()
                updateCartState(cartItems)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load cart"
                    )
                }
            }
        }
    }

    fun updateItemQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val updatedItems = cartRepository.updateItemQuantity(itemId, quantity)
                updateCartState(updatedItems)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to update quantity")
                }
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                val updatedItems = cartRepository.removeItem(itemId)
                updateCartState(updatedItems)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to remove item")
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepository.clearCart()
                updateCartState(emptyList())
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to clear cart")
                }
            }
        }
    }

    fun showClearCartConfirmation() {
        _state.update { it.copy(showClearCartConfirmation = true) }
    }

    fun hideClearCartConfirmation() {
        _state.update { it.copy(showClearCartConfirmation = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun updateCartState(cartItems: List<CartItem>) {
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        val tax = subtotal * 0.1 // 10% tax
        val total = subtotal + _state.value.shippingCost + tax

        _state.update {
            it.copy(
                isLoading = false,
                cartItems = cartItems,
                subtotal = subtotal,
                tax = tax,
                total = total
            )
        }
    }
} 
} 
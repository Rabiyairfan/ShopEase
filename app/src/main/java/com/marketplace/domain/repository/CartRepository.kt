package com.marketplace.domain.repository

import com.marketplace.domain.models.Cart
import com.marketplace.domain.models.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(userId: String): Flow<Cart?>
    fun getCartItems(userId: String): Flow<List<CartItem>>
    
    suspend fun addToCart(userId: String, item: CartItem): Result<Cart>
    suspend fun updateCartItem(userId: String, item: CartItem): Result<Cart>
    suspend fun removeFromCart(userId: String, itemId: String): Result<Cart>
    suspend fun clearCart(userId: String): Result<Unit>
    
    suspend fun updateCartQuantity(userId: String, itemId: String, quantity: Int): Result<Cart>
    suspend fun calculateCartTotal(userId: String): Result<Double>
} 
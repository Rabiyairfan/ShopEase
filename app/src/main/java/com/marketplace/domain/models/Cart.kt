package com.marketplace.domain.models

data class Cart(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalItems: Int = 0,
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val subtotal: Double = 0.0
) 
package com.marketplace.domain.usecase.cart

import com.marketplace.domain.models.Cart
import com.marketplace.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartItemUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        userId: String,
        productId: String,
        quantity: Int
    ): Result<Cart> {
        return cartRepository.updateCartItem(userId, productId, quantity)
    }
} 
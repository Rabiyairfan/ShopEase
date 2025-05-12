package com.marketplace.domain.usecase.cart

import com.marketplace.domain.models.Cart
import com.marketplace.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(userId: String): Flow<Cart> {
        return cartRepository.getCart(userId)
    }
} 
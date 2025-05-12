package com.marketplace.domain.usecase.order

import com.marketplace.domain.models.Order
import com.marketplace.domain.repository.CartRepository
import com.marketplace.domain.repository.OrderRepository
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(
        userId: String,
        shippingAddress: String,
        paymentMethod: String
    ): Result<Order> {
        // Get the current cart
        val cart = cartRepository.getCart(userId).collect { cart ->
            // Validate the order
            val validationResult = orderRepository.validateOrder(
                Order(
                    userId = userId,
                    items = cart.items,
                    shippingAddress = shippingAddress,
                    paymentMethod = paymentMethod
                )
            )
            
            if (validationResult.isSuccess) {
                // Create the order
                val order = Order(
                    userId = userId,
                    items = cart.items,
                    shippingAddress = shippingAddress,
                    paymentMethod = paymentMethod
                )
                
                // Save the order
                val result = orderRepository.createOrder(order)
                
                if (result.isSuccess) {
                    // Clear the cart after successful order creation
                    cartRepository.clearCart(userId)
                }
                
                return result
            } else {
                return Result.failure(validationResult.exceptionOrNull() ?: Exception("Order validation failed"))
            }
        }
        
        return Result.failure(Exception("Failed to get cart"))
    }
} 
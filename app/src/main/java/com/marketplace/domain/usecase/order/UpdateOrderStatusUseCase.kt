package com.marketplace.domain.usecase.order

import com.marketplace.domain.models.Order
import com.marketplace.domain.models.OrderStatus
import com.marketplace.domain.repository.OrderRepository
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        status: OrderStatus
    ): Result<Order> {
        return orderRepository.updateOrderStatus(orderId, status)
    }
} 
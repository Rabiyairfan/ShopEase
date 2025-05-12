package com.marketplace.domain.usecase.order

import com.marketplace.domain.models.Order
import com.marketplace.domain.models.OrderStatus
import com.marketplace.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(
        userId: String,
        status: OrderStatus? = null
    ): Flow<List<Order>> {
        return if (status != null) {
            orderRepository.getOrdersByStatus(userId, status)
        } else {
            orderRepository.getOrders(userId)
        }
    }
} 
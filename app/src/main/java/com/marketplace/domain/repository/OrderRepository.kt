package com.marketplace.domain.repository

import com.marketplace.domain.models.Order
import com.marketplace.domain.models.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(userId: String): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
    fun getOrdersByStatus(userId: String, status: OrderStatus): Flow<List<Order>>
    
    suspend fun createOrder(order: Order): Result<Order>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order>
    suspend fun cancelOrder(orderId: String): Result<Order>
    
    suspend fun getOrderHistory(userId: String): Flow<List<Order>>
    suspend fun getRecentOrders(userId: String, limit: Int): Flow<List<Order>>
    
    suspend fun calculateOrderTotal(order: Order): Result<Double>
    suspend fun validateOrder(order: Order): Result<Boolean>
} 
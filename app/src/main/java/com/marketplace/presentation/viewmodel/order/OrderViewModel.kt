package com.marketplace.presentation.viewmodel.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.models.Order
import com.marketplace.domain.models.OrderStatus
import com.marketplace.domain.usecase.order.CreateOrderUseCase
import com.marketplace.domain.usecase.order.GetOrdersUseCase
import com.marketplace.domain.usecase.order.UpdateOrderStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val getOrdersUseCase: GetOrdersUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Initial)
    val orderState: StateFlow<OrderState> = _orderState

    fun loadOrders(userId: String, status: OrderStatus? = null) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            getOrdersUseCase(userId, status)
                .catch { e ->
                    _orderState.value = OrderState.Error(e.message ?: "Failed to load orders")
                }
                .collect { orders ->
                    _orders.value = orders
                    _orderState.value = OrderState.Success
                }
        }
    }

    fun createOrder(userId: String, shippingAddress: String, paymentMethod: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            createOrderUseCase(userId, shippingAddress, paymentMethod)
                .onSuccess { order ->
                    _orders.value = _orders.value + order
                    _orderState.value = OrderState.Success
                }
                .onFailure { e ->
                    _orderState.value = OrderState.Error(e.message ?: "Failed to create order")
                }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            updateOrderStatusUseCase(orderId, status)
                .onSuccess { updatedOrder ->
                    _orders.value = _orders.value.map { 
                        if (it.id == orderId) updatedOrder else it 
                    }
                    _orderState.value = OrderState.Success
                }
                .onFailure { e ->
                    _orderState.value = OrderState.Error(e.message ?: "Failed to update order status")
                }
        }
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return _orders.value.filter { it.status == status }
    }
}

sealed class OrderState {
    object Initial : OrderState()
    object Loading : OrderState()
    object Success : OrderState()
    data class Error(val message: String) : OrderState()
} 
package com.marketplace.domain.models

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: OrderStatus = OrderStatus.PENDING,
    val shippingAddress: Address = Address(),
    val paymentMethod: PaymentMethod = PaymentMethod(),
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OrderItem(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val subtotal: Double = 0.0
)

data class Address(
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val zipCode: String = ""
)

data class PaymentMethod(
    val type: PaymentType = PaymentType.CASH,
    val details: String = ""
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

enum class PaymentType {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    WALLET
} 
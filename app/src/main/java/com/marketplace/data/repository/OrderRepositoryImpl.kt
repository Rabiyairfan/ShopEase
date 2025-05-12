package com.marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marketplace.domain.models.Order
import com.marketplace.domain.models.OrderStatus
import com.marketplace.domain.repository.OrderRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    private val ordersCollection = firestore.collection("orders")

    override fun getOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(orders)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val order = snapshot?.toObject(Order::class.java)?.copy(id = snapshot.id)
                trySend(order)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getOrdersByStatus(userId: String, status: OrderStatus): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(orders)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun createOrder(order: Order): Result<Order> = try {
        val docRef = ordersCollection.document()
        val newOrder = order.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        docRef.set(newOrder).await()
        Result.success(newOrder)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order> = try {
        val orderRef = ordersCollection.document(orderId)
        val order = orderRef.get().await().toObject(Order::class.java)
            ?: throw Exception("Order not found")
            
        val updatedOrder = order.copy(
            status = status,
            updatedAt = System.currentTimeMillis()
        )
        
        orderRef.set(updatedOrder).await()
        Result.success(updatedOrder)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun cancelOrder(orderId: String): Result<Order> = try {
        val orderRef = ordersCollection.document(orderId)
        val order = orderRef.get().await().toObject(Order::class.java)
            ?: throw Exception("Order not found")
            
        if (order.status == OrderStatus.DELIVERED) {
            throw Exception("Cannot cancel a delivered order")
        }
        
        val updatedOrder = order.copy(
            status = OrderStatus.CANCELLED,
            updatedAt = System.currentTimeMillis()
        )
        
        orderRef.set(updatedOrder).await()
        Result.success(updatedOrder)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getOrderHistory(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(orders)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun getRecentOrders(userId: String, limit: Int): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(orders)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun calculateOrderTotal(order: Order): Result<Double> = try {
        val subtotal = order.items.sumOf { it.subtotal }
        val total = subtotal + order.shipping + order.tax
        Result.success(total)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun validateOrder(order: Order): Result<Boolean> = try {
        if (order.items.isEmpty()) {
            throw Exception("Order must contain at least one item")
        }
        
        if (order.shippingAddress.street.isBlank() || 
            order.shippingAddress.city.isBlank() || 
            order.shippingAddress.state.isBlank() || 
            order.shippingAddress.country.isBlank() || 
            order.shippingAddress.zipCode.isBlank()) {
            throw Exception("Invalid shipping address")
        }
        
        if (order.paymentMethod.type == null) {
            throw Exception("Invalid payment method")
        }
        
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 
package com.marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.marketplace.domain.models.Cart
import com.marketplace.domain.models.CartItem
import com.marketplace.domain.repository.CartRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CartRepository {

    private val cartsCollection = firestore.collection("carts")

    override fun getCart(userId: String): Flow<Cart?> = callbackFlow {
        val listener = cartsCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val cart = snapshot?.toObject(Cart::class.java)?.copy(id = snapshot.id)
                trySend(cart)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getCartItems(userId: String): Flow<List<CartItem>> = callbackFlow {
        val listener = cartsCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val cart = snapshot?.toObject(Cart::class.java)
                trySend(cart?.items ?: emptyList())
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun addToCart(userId: String, item: CartItem): Result<Cart> = try {
        val cartRef = cartsCollection.document(userId)
        val cart = cartRef.get().await().toObject(Cart::class.java)
        
        val updatedCart = if (cart == null) {
            Cart(
                id = userId,
                userId = userId,
                items = listOf(item),
                totalItems = item.quantity,
                subtotal = item.subtotal,
                total = item.subtotal
            )
        } else {
            val existingItem = cart.items.find { it.productId == item.productId }
            val updatedItems = if (existingItem != null) {
                cart.items.map { 
                    if (it.productId == item.productId) {
                        it.copy(
                            quantity = it.quantity + item.quantity,
                            subtotal = it.price * (it.quantity + item.quantity)
                        )
                    } else it
                }
            } else {
                cart.items + item
            }
            
            val subtotal = updatedItems.sumOf { it.subtotal }
            cart.copy(
                items = updatedItems,
                totalItems = updatedItems.sumOf { it.quantity },
                subtotal = subtotal,
                total = subtotal + cart.shipping + cart.tax,
                updatedAt = System.currentTimeMillis()
            )
        }
        
        cartRef.set(updatedCart).await()
        Result.success(updatedCart)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCartItem(userId: String, item: CartItem): Result<Cart> = try {
        val cartRef = cartsCollection.document(userId)
        val cart = cartRef.get().await().toObject(Cart::class.java)
            ?: throw Exception("Cart not found")
            
        val updatedItems = cart.items.map { 
            if (it.id == item.id) item else it
        }
        
        val subtotal = updatedItems.sumOf { it.subtotal }
        val updatedCart = cart.copy(
            items = updatedItems,
            totalItems = updatedItems.sumOf { it.quantity },
            subtotal = subtotal,
            total = subtotal + cart.shipping + cart.tax,
            updatedAt = System.currentTimeMillis()
        )
        
        cartRef.set(updatedCart).await()
        Result.success(updatedCart)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeFromCart(userId: String, itemId: String): Result<Cart> = try {
        val cartRef = cartsCollection.document(userId)
        val cart = cartRef.get().await().toObject(Cart::class.java)
            ?: throw Exception("Cart not found")
            
        val updatedItems = cart.items.filter { it.id != itemId }
        val subtotal = updatedItems.sumOf { it.subtotal }
        val updatedCart = cart.copy(
            items = updatedItems,
            totalItems = updatedItems.sumOf { it.quantity },
            subtotal = subtotal,
            total = subtotal + cart.shipping + cart.tax,
            updatedAt = System.currentTimeMillis()
        )
        
        cartRef.set(updatedCart).await()
        Result.success(updatedCart)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun clearCart(userId: String): Result<Unit> = try {
        cartsCollection.document(userId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCartQuantity(userId: String, itemId: String, quantity: Int): Result<Cart> = try {
        val cartRef = cartsCollection.document(userId)
        val cart = cartRef.get().await().toObject(Cart::class.java)
            ?: throw Exception("Cart not found")
            
        val updatedItems = cart.items.map { 
            if (it.id == itemId) {
                it.copy(
                    quantity = quantity,
                    subtotal = it.price * quantity
                )
            } else it
        }
        
        val subtotal = updatedItems.sumOf { it.subtotal }
        val updatedCart = cart.copy(
            items = updatedItems,
            totalItems = updatedItems.sumOf { it.quantity },
            subtotal = subtotal,
            total = subtotal + cart.shipping + cart.tax,
            updatedAt = System.currentTimeMillis()
        )
        
        cartRef.set(updatedCart).await()
        Result.success(updatedCart)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun calculateCartTotal(userId: String): Result<Double> = try {
        val cart = cartsCollection.document(userId)
            .get()
            .await()
            .toObject(Cart::class.java)
            ?: throw Exception("Cart not found")
            
        Result.success(cart.total)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 
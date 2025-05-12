package com.marketplace.presentation.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.model.*
import com.marketplace.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalOrders: Int = 0,
    val totalUsers: Int = 0,
    val totalProducts: Int = 0,
    val totalRevenue: Double = 0.0,
    val recentActivities: List<String> = emptyList()
)

data class AdminState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val dashboardStats: DashboardStats = DashboardStats(),
    val products: List<Product> = emptyList(),
    val orders: List<Order> = emptyList(),
    val users: List<User> = emptyList(),
    val categories: List<Category> = emptyList(),
    val brands: List<Brand> = emptyList()
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state = _state.asStateFlow()

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats = _dashboardStats.asStateFlow()

    fun loadDashboardStats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Load all required data in parallel
                val productsFlow = productRepository.getProducts()
                val ordersFlow = orderRepository.getOrders()
                val usersFlow = userRepository.getUsers()
                val categoriesFlow = productRepository.getCategories()
                val brandsFlow = productRepository.getBrands()

                // Combine all flows
                combine(
                    productsFlow,
                    ordersFlow,
                    usersFlow,
                    categoriesFlow,
                    brandsFlow
                ) { products, orders, users, categories, brands ->
                    // Calculate total revenue
                    val totalRevenue = orders.sumOf { it.total }

                    // Generate recent activities
                    val activities = mutableListOf<String>()
                    
                    // Add recent orders
                    orders.take(5).forEach { order ->
                        activities.add("New order #${order.id} placed by ${order.userId}")
                    }
                    
                    // Add recent user registrations
                    users.take(5).forEach { user ->
                        activities.add("New user registered: ${user.name}")
                    }
                    
                    // Add recent product additions
                    products.take(5).forEach { product ->
                        activities.add("New product added: ${product.name}")
                    }

                    // Update dashboard stats
                    _dashboardStats.update {
                        DashboardStats(
                            totalOrders = orders.size,
                            totalUsers = users.size,
                            totalProducts = products.size,
                            totalRevenue = totalRevenue,
                            recentActivities = activities
                        )
                    }

                    // Update state
                    _state.update {
                        it.copy(
                            isLoading = false,
                            products = products,
                            orders = orders,
                            users = users,
                            categories = categories,
                            brands = brands
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    // Product Management
    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.addProduct(product)
                loadDashboardStats() // Refresh stats
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.updateProduct(product)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(productId)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    // Order Management
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    // User Management
    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            try {
                userRepository.updateUserRole(userId, role)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deactivateUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.deactivateUser(userId)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    // Category Management
    fun addCategory(category: Category) {
        viewModelScope.launch {
            try {
                productRepository.addCategory(category)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                productRepository.updateCategory(category)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                productRepository.deleteCategory(categoryId)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    // Brand Management
    fun addBrand(brand: Brand) {
        viewModelScope.launch {
            try {
                productRepository.addBrand(brand)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateBrand(brand: Brand) {
        viewModelScope.launch {
            try {
                productRepository.updateBrand(brand)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteBrand(brandId: String) {
        viewModelScope.launch {
            try {
                productRepository.deleteBrand(brandId)
                loadDashboardStats()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 
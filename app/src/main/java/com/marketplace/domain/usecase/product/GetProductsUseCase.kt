package com.marketplace.domain.usecase.product

import com.marketplace.domain.models.Product
import com.marketplace.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(
        category: String? = null,
        brand: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String? = null
    ): Flow<List<Product>> {
        return productRepository.getProducts(category, brand, minPrice, maxPrice, sortBy)
    }
} 
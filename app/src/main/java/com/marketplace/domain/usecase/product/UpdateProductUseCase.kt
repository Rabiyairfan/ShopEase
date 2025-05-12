package com.marketplace.domain.usecase.product

import com.marketplace.domain.models.Product
import com.marketplace.domain.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        productId: String,
        name: String? = null,
        description: String? = null,
        price: Double? = null,
        category: String? = null,
        brand: String? = null,
        images: List<String>? = null,
        stock: Int? = null
    ): Result<Product> {
        return productRepository.updateProduct(
            productId = productId,
            name = name,
            description = description,
            price = price,
            category = category,
            brand = brand,
            images = images,
            stock = stock
        )
    }
} 
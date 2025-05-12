package com.marketplace.domain.usecase.product

import com.marketplace.domain.models.Product
import com.marketplace.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        price: Double,
        category: String,
        brand: String,
        images: List<String>,
        stock: Int,
        sellerId: String
    ): Result<Product> {
        val product = Product(
            name = name,
            description = description,
            price = price,
            category = category,
            brand = brand,
            images = images,
            stock = stock,
            sellerId = sellerId
        )
        return productRepository.addProduct(product)
    }
} 
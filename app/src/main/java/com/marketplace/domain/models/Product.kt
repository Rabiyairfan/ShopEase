package com.marketplace.domain.models

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val category: String = "",
    val brand: String = "",
    val images: List<String> = emptyList(),
    val stock: Int = 0,
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val description: String = ""
)

data class Brand(
    val id: String = "",
    val name: String = "",
    val logoUrl: String = "",
    val description: String = ""
) 
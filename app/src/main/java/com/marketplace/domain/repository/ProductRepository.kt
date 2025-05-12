package com.marketplace.domain.repository

import com.marketplace.domain.models.Product
import com.marketplace.domain.models.Category
import com.marketplace.domain.models.Brand
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun getProductsByBrand(brandId: String): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    fun searchProducts(query: String): Flow<List<Product>>
    
    fun getCategories(): Flow<List<Category>>
    fun getCategoryById(id: String): Flow<Category?>
    
    fun getBrands(): Flow<List<Brand>>
    fun getBrandById(id: String): Flow<Brand?>
    
    suspend fun addProduct(product: Product): Result<Product>
    suspend fun updateProduct(product: Product): Result<Product>
    suspend fun deleteProduct(id: String): Result<Unit>
    
    suspend fun addCategory(category: Category): Result<Category>
    suspend fun updateCategory(category: Category): Result<Category>
    suspend fun deleteCategory(id: String): Result<Unit>
    
    suspend fun addBrand(brand: Brand): Result<Brand>
    suspend fun updateBrand(brand: Brand): Result<Brand>
    suspend fun deleteBrand(id: String): Result<Unit>
} 
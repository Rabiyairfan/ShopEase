package com.marketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.marketplace.domain.models.Product
import com.marketplace.domain.models.Category
import com.marketplace.domain.models.Brand
import com.marketplace.domain.repository.ProductRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    private val productsCollection = firestore.collection("products")
    private val categoriesCollection = firestore.collection("categories")
    private val brandsCollection = firestore.collection("brands")

    override fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(products)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("category", categoryId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(products)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getProductsByBrand(brandId: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .whereEqualTo("brand", brandId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(products)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getProductById(id: String): Flow<Product?> = callbackFlow {
        val listener = productsCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val product = snapshot?.toObject(Product::class.java)?.copy(id = snapshot.id)
                trySend(product)
            }
            
        awaitClose { listener.remove() }
    }

    override fun searchProducts(query: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(products)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = categoriesCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(categories)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getCategoryById(id: String): Flow<Category?> = callbackFlow {
        val listener = categoriesCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val category = snapshot?.toObject(Category::class.java)?.copy(id = snapshot.id)
                trySend(category)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getBrands(): Flow<List<Brand>> = callbackFlow {
        val listener = brandsCollection
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val brands = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Brand::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(brands)
            }
            
        awaitClose { listener.remove() }
    }

    override fun getBrandById(id: String): Flow<Brand?> = callbackFlow {
        val listener = brandsCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val brand = snapshot?.toObject(Brand::class.java)?.copy(id = snapshot.id)
                trySend(brand)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun addProduct(product: Product): Result<Product> = try {
        val docRef = productsCollection.document()
        val newProduct = product.copy(id = docRef.id)
        docRef.set(newProduct).await()
        Result.success(newProduct)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProduct(product: Product): Result<Product> = try {
        productsCollection.document(product.id)
            .set(product)
            .await()
        Result.success(product)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteProduct(id: String): Result<Unit> = try {
        productsCollection.document(id)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addCategory(category: Category): Result<Category> = try {
        val docRef = categoriesCollection.document()
        val newCategory = category.copy(id = docRef.id)
        docRef.set(newCategory).await()
        Result.success(newCategory)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCategory(category: Category): Result<Category> = try {
        categoriesCollection.document(category.id)
            .set(category)
            .await()
        Result.success(category)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCategory(id: String): Result<Unit> = try {
        categoriesCollection.document(id)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addBrand(brand: Brand): Result<Brand> = try {
        val docRef = brandsCollection.document()
        val newBrand = brand.copy(id = docRef.id)
        docRef.set(newBrand).await()
        Result.success(newBrand)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateBrand(brand: Brand): Result<Brand> = try {
        brandsCollection.document(brand.id)
            .set(brand)
            .await()
        Result.success(brand)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteBrand(id: String): Result<Unit> = try {
        brandsCollection.document(id)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 
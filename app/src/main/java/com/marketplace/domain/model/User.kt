package com.marketplace.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val profilePictureUrl: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    CUSTOMER,
    ADMIN,
    EMPLOYEE
} 
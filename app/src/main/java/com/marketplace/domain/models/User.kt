package com.marketplace.domain.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val profileImageUrl: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    CUSTOMER,
    ADMIN,
    EMPLOYEE
} 
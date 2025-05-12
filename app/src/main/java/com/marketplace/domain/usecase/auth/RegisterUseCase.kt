package com.marketplace.domain.usecase.auth

import com.marketplace.domain.models.User
import com.marketplace.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        address: String? = null
    ): Result<User> {
        return authRepository.register(email, password, name, phone, address)
    }
} 
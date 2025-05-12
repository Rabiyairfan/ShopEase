package com.marketplace.domain.usecase.user

import com.marketplace.domain.models.User
import com.marketplace.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null
    ): Result<User> {
        return userRepository.updateUserProfile(userId, name, email, phone, address)
    }
} 
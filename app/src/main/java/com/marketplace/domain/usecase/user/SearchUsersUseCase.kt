package com.marketplace.domain.usecase.user

import com.marketplace.domain.models.User
import com.marketplace.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Flow<List<User>> {
        return userRepository.searchUsers(query)
    }
} 
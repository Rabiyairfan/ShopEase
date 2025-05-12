package com.marketplace.presentation.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.domain.models.User
import com.marketplace.domain.usecase.user.GetCurrentUserUseCase
import com.marketplace.domain.usecase.user.SearchUsersUseCase
import com.marketplace.domain.usecase.user.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val searchUsersUseCase: SearchUsersUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _userState = MutableStateFlow<UserState>(UserState.Initial)
    val userState: StateFlow<UserState> = _userState

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase()
                .catch { e ->
                    _userState.value = UserState.Error(e.message ?: "Failed to get current user")
                }
                .collect { user ->
                    _currentUser.value = user
                    _userState.value = UserState.Success
                }
        }
    }

    fun updateProfile(
        userId: String,
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null
    ) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            updateUserProfileUseCase(userId, name, email, phone, address)
                .onSuccess { updatedUser ->
                    _currentUser.value = updatedUser
                    _userState.value = UserState.Success
                }
                .onFailure { e ->
                    _userState.value = UserState.Error(e.message ?: "Failed to update profile")
                }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            searchUsersUseCase(query)
                .catch { e ->
                    _userState.value = UserState.Error(e.message ?: "Failed to search users")
                }
                .collect { users ->
                    _searchResults.value = users
                    _userState.value = UserState.Success
                }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}

sealed class UserState {
    object Initial : UserState()
    object Loading : UserState()
    object Success : UserState()
    data class Error(val message: String) : UserState()
} 
package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the UserCard screen.
 *
 * Manages loading and displaying the user's information for the digital card.
 *
 * @param userRepository Repository for user data operations
 * @param currentUserId The ID of the current user
 */
class UserCardViewModel(
    private val userRepository: UserRepository,
    private val currentUserId: String
) : ViewModel() {

  companion object {
    private const val TAG = "UserCardViewModel"
  }

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadUser()
  }

  /** Loads the current user's data from the repository. */
  private fun loadUser() {
    _isLoading.value = true
    viewModelScope.launch {
      try {
        val loadedUser = userRepository.getUserById(currentUserId)
        _user.value = loadedUser
        _error.value = null
      } catch (exception: Exception) {
        Log.e(TAG, "Failed to load user: $currentUserId", exception)
        _error.value = exception.message ?: ProfileConstants.ERROR_LOAD_USER
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Reloads the user data. */
  fun reload() {
    loadUser()
  }
}

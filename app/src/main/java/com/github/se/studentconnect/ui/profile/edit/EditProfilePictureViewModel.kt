package com.github.se.studentconnect.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditProfilePictureScreen. Manages profile picture editing state and operations. */
class EditProfilePictureViewModel(
    private val userRepository: UserRepository,
    private val userId: String
) : ViewModel() {

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error message state
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Success message state
  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  init {
    loadUserProfile()
  }

  /** Loads the current user's profile from the repository. */
  private fun loadUserProfile() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val loadedUser = userRepository.getUserById(userId)
        _user.value = loadedUser
      } catch (exception: Exception) {
        _errorMessage.value = exception.message ?: "Failed to load profile"
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Updates the user's profile picture URL. */
  fun updateProfilePicture(profilePictureUrl: String?) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val currentUser = _user.value ?: return@launch
        val updatedUser = currentUser.copy(profilePictureUrl = profilePictureUrl)

        userRepository.saveUser(updatedUser)
        _user.value = updatedUser
        _successMessage.value = "Profile picture updated successfully"
      } catch (exception: Exception) {
        _errorMessage.value = exception.message ?: "Failed to update profile picture"
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Clears the error message. */
  fun clearErrorMessage() {
    _errorMessage.value = null
  }

  /** Clears the success message. */
  fun clearSuccessMessage() {
    _successMessage.value = null
  }
}

package com.github.se.studentconnect.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for EditProfilePictureScreen. Handles image selection and user profile updates. Note:
 * Image upload functionality is simplified for demo purposes.
 */
class EditProfilePictureViewModel(
    private val userRepository: UserRepository,
    private val userId: String
) : ViewModel() {

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Loading states
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Success state
  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  init {
    loadUserProfile()
  }

  /** Loads the current user's profile from Firebase. */
  private fun loadUserProfile() {
    viewModelScope.launch {
      userRepository.getUserById(
          userId = userId,
          onSuccess = { user -> _user.value = user },
          onFailure = { exception ->
            _errorMessage.value = exception.message ?: "Failed to load profile"
          })
    }
  }

  /**
   * Handles image selection (simplified version without Firebase Storage). For demo purposes, this
   * just simulates an upload by setting a placeholder URL.
   *
   * @param imageUri The URI of the selected image
   * @param context Android context for image processing
   */
  fun uploadProfilePicture(imageUri: Uri, context: Context) {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _errorMessage.value = null

        // Simulate upload delay
        delay(2000)

        // For demo purposes, set a placeholder URL
        val placeholderUrl =
            "https://example.com/profile_pictures/${userId}_${System.currentTimeMillis()}.jpg"

        // Update user profile with new image URL
        val currentUser = _user.value ?: throw kotlin.Exception("User not found")
        val updatedUser =
            currentUser.update(profilePictureUrl = User.UpdateValue.SetValue(placeholderUrl))

        // Save to Firestore
        userRepository.saveUser(
            user = updatedUser,
            onSuccess = {
              _user.value = updatedUser
              _isLoading.value = false
              _successMessage.value = "Profile picture updated successfully! (Demo mode)"
            },
            onFailure = { exception ->
              _isLoading.value = false
              _errorMessage.value = exception.message ?: "Failed to update profile"
            })
      } catch (e: Exception) {
        _isLoading.value = false
        _errorMessage.value = e.message ?: "Failed to upload image"
      }
    }
  }

  /** Removes the current profile picture. */
  fun removeProfilePicture() {
    viewModelScope.launch {
      try {
        _isLoading.value = true
        _errorMessage.value = null
        val currentUser = _user.value ?: throw kotlin.Exception("User not found")
        val updatedUser = currentUser.update(profilePictureUrl = User.UpdateValue.SetValue(null))
        // Save to Firestore
        userRepository.saveUser(
            user = updatedUser,
            onSuccess = {
              _user.value = updatedUser
              _isLoading.value = false
              _successMessage.value = "Profile picture removed successfully!"
            },
            onFailure = { exception ->
              _isLoading.value = false
              _errorMessage.value = exception.message ?: "Failed to remove profile picture"
            })
      } catch (e: Exception) {
        _isLoading.value = false
        _errorMessage.value = e.message ?: "Failed to remove profile picture"
      }
    }
  }

  /** Clears error message. */
  fun clearErrorMessage() {
    _errorMessage.value = null
  }

  /** Clears success message. */
  fun clearSuccessMessage() {
    _successMessage.value = null
  }
}

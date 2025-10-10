package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlin.text.ifBlank
import kotlin.text.trim
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditBioScreen. Handles bio editing, validation, and Firebase updates. */
class EditBioViewModel(private val userRepository: UserRepository, private val userId: String) :
    ViewModel() {

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Bio text state
  private val _bioText = MutableStateFlow("")
  val bioText: StateFlow<String> = _bioText.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  // Success state
  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  // Validation state
  private val _isValid = MutableStateFlow(true)
  val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

  init {
    loadUserProfile()
  }

  /** Loads the current user's profile from Firebase. */
  private fun loadUserProfile() {
    viewModelScope.launch {
      userRepository.getUserById(
          userId = userId,
          onSuccess = { user ->
            _user.value = user
            _bioText.value = user?.bio ?: ""
          },
          onFailure = { exception ->
            _errorMessage.value = exception.message ?: "Failed to load profile"
          })
    }
  }

  /**
   * Updates the bio text and validates it.
   *
   * @param newBio The new bio text
   */
  fun updateBioText(newBio: String) {
    _bioText.value = newBio
    validateBio(newBio)
  }

  /**
   * Validates the bio text.
   *
   * @param bio The bio text to validate
   */
  private fun validateBio(bio: String) {
    val isValidBio = bio.length <= 500
    _isValid.value = isValidBio
  }

  /** Saves the bio to Firebase. */
  fun saveBio() {
    val bio = _bioText.value.trim()

    // Validate before saving
    if (bio.length > 500) {
      _errorMessage.value = "Bio cannot exceed 500 characters"
      return
    }

    viewModelScope.launch {
      try {
        _isLoading.value = true
        _errorMessage.value = null

        val currentUser = _user.value ?: throw kotlin.Exception("User not found")
        val updatedUser = currentUser.update(bio = User.UpdateValue.SetValue(bio.ifBlank { null }))

        // Save to Firestore
        userRepository.saveUser(
            user = updatedUser,
            onSuccess = {
              _user.value = updatedUser
              _isLoading.value = false
              _successMessage.value = "Bio updated successfully!"
            },
            onFailure = { exception ->
              _isLoading.value = false
              _errorMessage.value = exception.message ?: "Failed to update bio"
            })
      } catch (e: Exception) {
        _isLoading.value = false
        _errorMessage.value = e.message ?: "Failed to update bio"
      }
    }
  }

  /**
   * Gets the character count for the current bio.
   *
   * @return The number of characters in the bio
   */
  fun getCharacterCount(): Int {
    return _bioText.value.length
  }

  /**
   * Gets the remaining character count.
   *
   * @return The number of characters remaining (500 - current length)
   */
  fun getRemainingCharacters(): Int {
    return 500 - _bioText.value.length
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

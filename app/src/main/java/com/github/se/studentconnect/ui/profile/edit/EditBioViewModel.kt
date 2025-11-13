package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.ProfileConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for EditBioScreen.
 *
 * Handles bio editing and validation with character limit enforcement and proper error handling.
 *
 * @param userRepository Repository for user data operations
 * @param userId The ID of the user whose bio is being edited
 */
class EditBioViewModel(userRepository: UserRepository, userId: String) :
    BaseEditViewModel(userRepository, userId) {

  // Bio text state
  private val _bioText = MutableStateFlow("")
  val bioText: StateFlow<String> = _bioText.asStateFlow()

  // Character count
  private val _characterCount = MutableStateFlow(0)
  val characterCount: StateFlow<Int> = _characterCount.asStateFlow()

  // Validation error
  private val _validationError = MutableStateFlow<String?>(null)
  val validationError: StateFlow<String?> = _validationError.asStateFlow()

  init {
    loadCurrentBio()
  }

  /** Loads the current bio from the repository. */
  private fun loadCurrentBio() {
    executeWithErrorHandling(
        operation = {
          val user = userRepository.getUserById(userId)
          val currentBio = user?.bio ?: ""
          _bioText.value = currentBio
          _characterCount.value = currentBio.length
        },
        onSuccess = { resetState() })
  }

  /**
   * Updates the bio text and character count.
   *
   * If the new text exceeds the maximum length, a validation error is shown and the text is not
   * updated.
   *
   * @param newText The new bio text
   */
  fun updateBioText(newText: String) {
    if (newText.length <= ProfileConstants.MAX_BIO_LENGTH) {
      _bioText.value = newText
      _characterCount.value = newText.length
      _validationError.value = null
    } else {
      _validationError.value = ProfileConstants.ERROR_BIO_TOO_LONG
    }
  }

  /** Validates and saves the bio. */
  fun saveBio() {
    val trimmedBio = _bioText.value.trim()

    // Clear previous errors
    _validationError.value = null

    // Validate
    if (trimmedBio.isEmpty()) {
      _validationError.value = ProfileConstants.ERROR_BIO_EMPTY
      return
    }

    if (trimmedBio.length > ProfileConstants.MAX_BIO_LENGTH) {
      _validationError.value = ProfileConstants.ERROR_BIO_TOO_LONG
      return
    }

    executeWithErrorHandling(
        operation = {
          val user =
              userRepository.getUserById(userId) ?: throw IllegalStateException("User not found")

          val updatedUser = user.copy(bio = trimmedBio, updatedAt = System.currentTimeMillis())
          userRepository.saveUser(updatedUser)
        },
        onSuccess = { setSuccess(ProfileConstants.SUCCESS_BIO_UPDATED) })
  }

  /** Clears validation error. */
  fun clearValidationError() {
    _validationError.value = null
  }
}

package com.github.se.studentconnect.ui.profile.edit

import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.ProfileConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for EditNameScreen.
 *
 * Handles name editing and validation with proper error handling and state management.
 *
 * @param userRepository Repository for user data operations
 * @param userId The ID of the user whose name is being edited
 */
class EditNameViewModel(userRepository: UserRepository, userId: String) :
    BaseEditViewModel(userRepository, userId) {

  // First name state
  private val _firstName = MutableStateFlow("")
  val firstName: StateFlow<String> = _firstName.asStateFlow()

  // Last name state
  private val _lastName = MutableStateFlow("")
  val lastName: StateFlow<String> = _lastName.asStateFlow()

  // Validation errors
  private val _firstNameError = MutableStateFlow<String?>(null)
  val firstNameError: StateFlow<String?> = _firstNameError.asStateFlow()

  private val _lastNameError = MutableStateFlow<String?>(null)
  val lastNameError: StateFlow<String?> = _lastNameError.asStateFlow()

  init {
    loadCurrentName()
  }

  /** Loads the current name from the repository. */
  private fun loadCurrentName() {
    executeWithErrorHandling(
        operation = {
          val user = userRepository.getUserById(userId)
          _firstName.value = user?.firstName ?: ""
          _lastName.value = user?.lastName ?: ""
        },
        onSuccess = { resetState() })
  }

  /**
   * Updates the first name.
   *
   * @param name The new first name
   */
  fun updateFirstName(name: String) {
    _firstName.value = name
    _firstNameError.value = null
  }

  /**
   * Updates the last name.
   *
   * @param name The new last name
   */
  fun updateLastName(name: String) {
    _lastName.value = name
    _lastNameError.value = null
  }

  /** Validates and saves the name. */
  fun saveName() {
    val trimmedFirstName = _firstName.value.trim()
    val trimmedLastName = _lastName.value.trim()

    // Clear previous errors
    _firstNameError.value = null
    _lastNameError.value = null

    // Validate
    var hasError = false

    if (trimmedFirstName.isEmpty()) {
      _firstNameError.value = ProfileConstants.ERROR_FIRST_NAME_EMPTY
      hasError = true
    }

    if (trimmedLastName.isEmpty()) {
      _lastNameError.value = ProfileConstants.ERROR_LAST_NAME_EMPTY
      hasError = true
    }

    if (hasError) return

    executeWithErrorHandling(
        operation = {
          val user =
              userRepository.getUserById(userId) ?: throw IllegalStateException("User not found")

          val updatedUser =
              user.copy(
                  firstName = trimmedFirstName,
                  lastName = trimmedLastName,
                  updatedAt = System.currentTimeMillis())
          userRepository.saveUser(updatedUser)
        },
        onSuccess = { setSuccess(ProfileConstants.SUCCESS_NAME_UPDATED) })
  }

  /** Clears validation errors. */
  fun clearErrors() {
    _firstNameError.value = null
    _lastNameError.value = null
  }
}

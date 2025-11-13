package com.github.se.studentconnect.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditNationalityScreen. Manages nationality editing state and operations. */
class EditNationalityViewModel(
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
  private val _successMessage = MutableStateFlow<Int?>(null)
  val successMessage: StateFlow<Int?> = _successMessage.asStateFlow()

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
        _errorMessage.value = exception.message ?: R.string.error_failed_to_load_profile.toString()
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Updates the user's nationality. */
  fun updateNationality(countryName: String) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val currentUser = _user.value ?: return@launch
        val updatedUser = currentUser.copy(country = countryName)

        userRepository.saveUser(updatedUser)
        _user.value = updatedUser
        _successMessage.value = R.string.success_nationality_updated
      } catch (exception: Exception) {
        _errorMessage.value =
            exception.message ?: R.string.error_failed_to_update_nationality.toString()
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

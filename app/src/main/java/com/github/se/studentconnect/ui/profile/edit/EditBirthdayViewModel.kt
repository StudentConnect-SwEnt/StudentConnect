package com.github.se.studentconnect.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.resources.ResourceProvider
import com.github.se.studentconnect.ui.components.BirthdayFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditBirthdayScreen. Handles birthday date selection and validation. */
class EditBirthdayViewModel(
    private val userRepository: UserRepository,
    private val userId: String,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

  // Selected date in milliseconds (for DatePicker)
  private val _selectedDateMillis = MutableStateFlow<Long?>(null)
  val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

  // Formatted birthday string
  private val _birthdayString = MutableStateFlow<String?>(null)
  val birthdayString: StateFlow<String?> = _birthdayString.asStateFlow()

  // UI State
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  init {
    loadCurrentBirthday()
  }

  /** Loads the current birthday from Firebase. */
  private fun loadCurrentBirthday() {
    viewModelScope.launch {
      try {
        _uiState.value = UiState.Loading
        val user = userRepository.getUserById(userId)
        val currentBirthday = user?.birthdate

        if (currentBirthday != null) {
          _birthdayString.value = currentBirthday
          // Parse date to milliseconds using shared formatter
          _selectedDateMillis.value = BirthdayFormatter.parseDate(currentBirthday)
        } else {
          _birthdayString.value = null
          _selectedDateMillis.value = null
        }

        _uiState.value = UiState.Idle
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: resourceProvider.getString(R.string.error_failed_to_load_profile))
      }
    }
  }

  /**
   * Updates the selected date.
   *
   * @param dateMillis The selected date in milliseconds
   */
  fun updateSelectedDate(dateMillis: Long?) {
    _selectedDateMillis.value = dateMillis
    _birthdayString.value = dateMillis?.let { BirthdayFormatter.formatDate(it) }
  }

  /** Saves the birthday. */
  fun saveBirthday() {
    viewModelScope.launch {
      try {
        val birthdayToSave = _birthdayString.value

        _uiState.value = UiState.Loading

        // Update in Firestore
        val user = userRepository.getUserById(userId)
        if (user != null) {
          val updatedUser =
              user.copy(birthdate = birthdayToSave, updatedAt = System.currentTimeMillis())
          userRepository.saveUser(updatedUser)
          _uiState.value = UiState.Success(resourceProvider.getString(R.string.success_birthday_updated))
        } else {
          _uiState.value = UiState.Error(resourceProvider.getString(R.string.error_user_not_found))
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: resourceProvider.getString(R.string.error_failed_to_load_profile))
      }
    }
  }

  /** Removes the birthday (sets to null). */
  fun removeBirthday() {
    viewModelScope.launch {
      try {
        _uiState.value = UiState.Loading

        val user = userRepository.getUserById(userId)
        if (user != null) {
          val updatedUser = user.copy(birthdate = null, updatedAt = System.currentTimeMillis())
          userRepository.saveUser(updatedUser)
          _selectedDateMillis.value = null
          _birthdayString.value = null
          _uiState.value = UiState.Success(resourceProvider.getString(R.string.success_birthday_updated))
        } else {
          _uiState.value = UiState.Error(resourceProvider.getString(R.string.error_user_not_found))
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: resourceProvider.getString(R.string.error_failed_to_load_profile))
      }
    }
  }

  /** Resets UI state to idle. */
  fun resetState() {
    _uiState.value = UiState.Idle
  }

  /** UI state sealed class for EditBirthdayScreen. */
  sealed class UiState {
    object Idle : UiState()

    object Loading : UiState()

    data class Success(val message: String) : UiState()

    data class Error(val message: String) : UiState()
  }
}

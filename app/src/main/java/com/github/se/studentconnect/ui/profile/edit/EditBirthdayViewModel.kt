package com.github.se.studentconnect.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditBirthdayScreen. Handles birthday date selection and validation. */
class EditBirthdayViewModel(
    private val userRepository: UserRepository,
    private val userId: String
) : ViewModel() {

  private val dateFormatter =
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getDefault() }

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
        val currentBirthday = user?.birthday

        if (currentBirthday != null) {
          _birthdayString.value = currentBirthday
          // Parse date to milliseconds
          try {
            val date = dateFormatter.parse(currentBirthday)
            _selectedDateMillis.value = date?.time
          } catch (e: Exception) {
            // Invalid date format, start fresh
            _selectedDateMillis.value = null
          }
        } else {
          _birthdayString.value = null
          _selectedDateMillis.value = null
        }

        _uiState.value = UiState.Idle
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Failed to load birthday")
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
    if (dateMillis != null) {
      val date = Date(dateMillis)
      _birthdayString.value = dateFormatter.format(date)
    } else {
      _birthdayString.value = null
    }
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
              user.copy(birthday = birthdayToSave, updatedAt = System.currentTimeMillis())
          userRepository.saveUser(updatedUser)
          _uiState.value = UiState.Success("Birthday updated successfully")
        } else {
          _uiState.value = UiState.Error("User not found")
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Failed to save birthday")
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
          val updatedUser = user.copy(birthday = null, updatedAt = System.currentTimeMillis())
          userRepository.saveUser(updatedUser)
          _selectedDateMillis.value = null
          _birthdayString.value = null
          _uiState.value = UiState.Success("Birthday removed successfully")
        } else {
          _uiState.value = UiState.Error("User not found")
        }
      } catch (e: Exception) {
        _uiState.value = UiState.Error(e.message ?: "Failed to remove birthday")
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

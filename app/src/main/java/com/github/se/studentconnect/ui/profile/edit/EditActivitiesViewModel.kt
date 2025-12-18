package com.github.se.studentconnect.ui.profile.edit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.profile.saveUserWithTimeout
import com.github.se.studentconnect.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for EditActivitiesScreen. Handles activities/hobbies selection and search. */
class EditActivitiesViewModel(
    private val userRepository: UserRepository,
    private val userId: String
) : ViewModel() {

  // Available activities from central source
  private val availableActivities = Activities.allActivities

  // Search query
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Filtered activities based on search
  private val _filteredActivities = MutableStateFlow(availableActivities)
  val filteredActivities: StateFlow<List<String>> = _filteredActivities.asStateFlow()

  // Selected activities
  private val _selectedActivities = MutableStateFlow<Set<String>>(emptySet())
  val selectedActivities: StateFlow<Set<String>> = _selectedActivities.asStateFlow()

  // UI State
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  // Snackbar messages
  private val _snackbarMessage = MutableSharedFlow<String>()
  val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

  init {
    loadCurrentActivities()
  }

  /** Loads the current activities/hobbies from Firebase. */
  private fun loadCurrentActivities() {
    viewModelScope.launch {
      try {
        _uiState.value = UiState.Loading
        val user = userRepository.getUserById(userId)
        _selectedActivities.value = user?.hobbies?.toSet() ?: emptySet()
        _uiState.value = UiState.Idle
      } catch (e: Exception) {
        _uiState.value =
            UiState.Error(e.message ?: R.string.error_failed_to_load_activities.toString())
      }
    }
  }

  /**
   * Updates the search query and filters activities.
   *
   * @param query The search query
   */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    _filteredActivities.value =
        if (query.isBlank()) {
          availableActivities
        } else {
          availableActivities.filter { it.contains(query, ignoreCase = true) }
        }
  }

  /**
   * Toggles selection of an activity.
   *
   * @param activity The activity to toggle
   */
  fun toggleActivity(activity: String) {
    _selectedActivities.value =
        _selectedActivities.value.toMutableSet().apply { if (!add(activity)) remove(activity) }
  }

  /** Saves the selected activities to Firebase. */
  fun saveActivities(context: Context) {
    // Check network and notify if offline
    val isOffline = !NetworkUtils.isNetworkAvailable(context)
    if (isOffline) {
      viewModelScope.launch {
        _snackbarMessage.emit(context.getString(R.string.offline_changes_sync_message))
      }
    }

    viewModelScope.launch {
      try {
        _uiState.value = UiState.Loading

        // Verify user exists before updating
        val user = userRepository.getUserById(userId)
        if (user == null) {
          _uiState.value = UiState.Error(R.string.error_user_not_found.toString())
          return@launch
        }

        // not using userRepository.updateUser, as it doesn't work
        val updatedUser = user.copy(hobbies = _selectedActivities.value.toList())
        viewModelScope.saveUserWithTimeout(userRepository, updatedUser)

        _uiState.value = UiState.Success(R.string.success_activities_updated.toString())
      } catch (e: Exception) {
        _uiState.value =
            UiState.Error(e.message ?: R.string.error_failed_to_save_activities.toString())
      }
    }
  }

  /** Resets UI state to idle. */
  fun resetState() {
    _uiState.value = UiState.Idle
  }

  /** UI state sealed class for EditActivitiesScreen. */
  sealed class UiState {
    object Idle : UiState()

    object Loading : UiState()

    data class Success(val message: String) : UiState()

    data class Error(val message: String) : UiState()
  }
}

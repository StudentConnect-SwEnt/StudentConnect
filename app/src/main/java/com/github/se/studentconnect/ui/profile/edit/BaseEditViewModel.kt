package com.github.se.studentconnect.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for edit screens that provides common functionality.
 *
 * This abstract class handles common patterns like loading states, error handling, and navigation
 * callbacks that are shared across all edit screens.
 *
 * @param userRepository Repository for user data operations
 * @param userId The ID of the user being edited
 */
abstract class BaseEditViewModel(
    protected val userRepository: UserRepository,
    protected val userId: String
) : ViewModel() {

  // UI State
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  /** UI state sealed class for edit screens. */
  sealed class UiState {
    object Idle : UiState()

    object Loading : UiState()

    data class Success(val message: String) : UiState()

    data class Error(val message: String) : UiState()
  }

  /** Resets UI state to idle. */
  fun resetState() {
    _uiState.value = UiState.Idle
  }

  /** Sets the UI state to loading. */
  protected fun setLoading() {
    _uiState.value = UiState.Loading
  }

  /**
   * Sets the UI state to success with a message.
   *
   * @param message The success message
   */
  protected fun setSuccess(message: String) {
    _uiState.value = UiState.Success(message)
  }

  /**
   * Sets the UI state to error with a message.
   *
   * @param message The error message
   */
  protected fun setError(message: String) {
    _uiState.value = UiState.Error(message)
  }

  /**
   * Executes a suspend function with proper error handling.
   *
   * @param operation The suspend function to execute
   * @param onSuccess Callback for successful execution
   * @param onError Callback for error handling
   */
  protected fun executeWithErrorHandling(
      operation: suspend () -> Unit,
      onSuccess: () -> Unit = {},
      onError: (String) -> Unit = { setError(it) }
  ) {
    viewModelScope.launch {
      try {
        setLoading()
        operation()
        onSuccess()
      } catch (e: Exception) {
        onError(e.message ?: "An unexpected error occurred")
      }
    }
  }
}

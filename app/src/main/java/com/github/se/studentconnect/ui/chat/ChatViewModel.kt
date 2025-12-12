package com.github.se.studentconnect.ui.chat

import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state for the event chat screen.
 *
 * @property event The current event, or null if not loaded
 * @property messages List of chat messages
 * @property messageText Current text in the message input field
 * @property typingUsers Map of user IDs to their display names who are currently typing
 * @property isSending Whether a message is currently being sent
 * @property isLoading Whether data is being loaded
 * @property error Error message if something went wrong
 * @property currentUser The current logged-in user
 */
data class ChatUiState(
    val event: Event? = null,
    val messages: List<ChatMessage> = emptyList(),
    val messageText: String = "",
    val typingUsers: Map<String, String> = emptyMap(),
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null
)

/**
 * ViewModel for managing the event chat screen state and operations.
 *
 * This is a stub implementation to allow the code to compile. Full implementation should include:
 * - Chat repository integration
 * - Real-time message sync
 * - Typing indicators
 * - Message sending/receiving
 */
class ChatViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(ChatUiState())
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  /**
   * Initialize the chat for a specific event.
   *
   * @param eventId The ID of the event to load chat for
   */
  fun initializeChat(eventId: String) {
    // Stub implementation
    _uiState.value = _uiState.value.copy(isLoading = false)
  }

  /**
   * Update the message text as the user types.
   *
   * @param text The new message text
   */
  fun updateMessageText(text: String) {
    _uiState.value = _uiState.value.copy(messageText = text)
  }

  /** Send the current message. */
  fun sendMessage() {
    // Stub implementation
    val message = _uiState.value.messageText
    if (message.isNotBlank()) {
      _uiState.value = _uiState.value.copy(messageText = "", isSending = false)
    }
  }

  /** Clear any error messages. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }
}

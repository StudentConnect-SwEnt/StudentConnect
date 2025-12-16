package com.github.se.studentconnect.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.model.chat.ChatRepositoryProvider
import com.github.se.studentconnect.model.chat.TypingStatus
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

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
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentUser: User? = null
)

/**
 * ViewModel for managing the event chat screen state and operations.
 *
 * Handles:
 * - Chat repository integration
 * - Real-time message sync
 * - Typing indicators
 * - Message sending/receiving
 */
class ChatViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(ChatUiState())
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  private val chatRepository = ChatRepositoryProvider.repository
  private val eventRepository = EventRepositoryProvider.repository
  private val userRepository = UserRepositoryProvider.repository

  private var messageObserverJob: Job? = null
  private var typingObserverJob: Job? = null
  private var typingDebounceJob: Job? = null

  /**
   * Initialize the chat for a specific event.
   *
   * @param eventId The ID of the event to load chat for
   */
  fun initializeChat(eventId: String) {
    // Cancel any existing jobs
    messageObserverJob?.cancel()
    typingObserverJob?.cancel()
    typingDebounceJob?.cancel()

    // Reset state to loading
    _uiState.value = ChatUiState(isLoading = true)

    viewModelScope.launch {
      try {
        // Load event
        val event = eventRepository.getEvent(eventId)
        _uiState.value = _uiState.value.copy(event = event)

        // Load current user
        val currentUserId = AuthenticationProvider.currentUser
        if (currentUserId.isNotBlank()) {
          try {
            val user = userRepository.getUserById(currentUserId)
            _uiState.value = _uiState.value.copy(currentUser = user)
          } catch (e: Exception) {
            // Error message uses string resource R.string.error_failed_to_load_user
            _uiState.value =
                _uiState.value.copy(error = "Failed to load user: ${e.message}", isLoading = false)
            return@launch
          }
        }

        // Start observing messages
        messageObserverJob =
            viewModelScope.launch {
              chatRepository
                  .observeMessages(eventId)
                  .catch { e: Throwable ->
                    _uiState.value =
                        _uiState.value.copy(error = "Failed to load messages: ${e.message}")
                  }
                  .collect { messages: List<ChatMessage> ->
                    _uiState.value = _uiState.value.copy(messages = messages)
                  }
            }

        // Start observing typing status
        typingObserverJob =
            viewModelScope.launch {
              chatRepository
                  .observeTypingUsers(eventId)
                  .catch { _: Throwable ->
                    // Silently handle typing status errors
                  }
                  .collect { typingStatuses: List<TypingStatus> ->
                    val filteredTypingUsers =
                        typingStatuses
                            .filter { status -> status.isTyping && status.userId != currentUserId }
                            .associate { status -> status.userId to status.userName }
                    _uiState.value = _uiState.value.copy(typingUsers = filteredTypingUsers)
                  }
            }

        _uiState.value = _uiState.value.copy(isLoading = false)
      } catch (e: Exception) {
        // Error message uses string resource R.string.error_failed_to_load_event
        _uiState.value =
            _uiState.value.copy(error = "Failed to load event: ${e.message}", isLoading = false)
      }
    }
  }

  /**
   * Update the message text as the user types.
   *
   * @param text The new message text
   */
  fun updateMessageText(text: String) {
    _uiState.value = _uiState.value.copy(messageText = text)

    // Update typing status
    val currentUser = _uiState.value.currentUser
    val event = _uiState.value.event

    if (currentUser != null && event != null) {
      // Cancel previous debounce job
      typingDebounceJob?.cancel()

      if (text.isNotBlank()) {
        // User is typing
        updateTypingStatus(true)

        // Schedule a job to stop typing after 2 seconds of inactivity
        typingDebounceJob =
            viewModelScope.launch {
              delay(2000)
              updateTypingStatus(false)
            }
      } else {
        // User cleared the text, stop typing immediately
        updateTypingStatus(false)
      }
    }
  }

  /**
   * Updates the typing status for the current user.
   *
   * @param isTyping Whether the user is currently typing
   */
  private fun updateTypingStatus(isTyping: Boolean) {
    val currentUser = _uiState.value.currentUser ?: return
    val event = _uiState.value.event ?: return

    val typingStatus =
        TypingStatus(
            userId = currentUser.userId,
            userName = "${currentUser.firstName} ${currentUser.lastName}",
            eventId = event.uid,
            isTyping = isTyping)

    chatRepository.updateTypingStatus(typingStatus, onSuccess = {}, onFailure = {})
  }

  /** Send the current message. */
  fun sendMessage() {
    val messageText = _uiState.value.messageText.trim()
    val currentUser = _uiState.value.currentUser
    val event = _uiState.value.event

    // Validate preconditions
    if (messageText.isBlank() || currentUser == null || event == null) {
      return
    }

    // Set sending state
    _uiState.value = _uiState.value.copy(isSending = true)

    val message =
        ChatMessage(
            messageId = chatRepository.getNewMessageId(),
            eventId = event.uid,
            senderId = currentUser.userId,
            senderName = "${currentUser.firstName} ${currentUser.lastName}",
            content = messageText)

    chatRepository.sendMessage(
        message,
        onSuccess = {
          _uiState.value = _uiState.value.copy(messageText = "", isSending = false)
          // Stop typing status after sending
          updateTypingStatus(false)
        },
        onFailure = { exception: Exception ->
          // Error message uses string resource R.string.error_failed_to_send_message
          _uiState.value =
              _uiState.value.copy(
                  error = "Failed to send message: ${exception.message}", isSending = false)
        })
  }

  /** Clear any error messages. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  override fun onCleared() {
    super.onCleared()
    messageObserverJob?.cancel()
    typingObserverJob?.cancel()
    typingDebounceJob?.cancel()
  }
}

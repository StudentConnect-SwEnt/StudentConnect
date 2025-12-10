package com.github.se.studentconnect.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.chat.ChatMessage
import com.github.se.studentconnect.model.chat.ChatRepository
import com.github.se.studentconnect.model.chat.ChatRepositoryProvider
import com.github.se.studentconnect.model.chat.TypingStatus
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the chat screen.
 *
 * @property event The event this chat belongs to.
 * @property messages List of chat messages.
 * @property currentUser The currently logged-in user.
 * @property typingUsers List of users currently typing.
 * @property messageText The current text in the message input field.
 * @property isLoading Whether the chat is loading.
 * @property error Error message if any.
 * @property isSending Whether a message is currently being sent.
 */
data class ChatUiState(
    val event: Event? = null,
    val messages: List<ChatMessage> = emptyList(),
    val currentUser: User? = null,
    val typingUsers: List<TypingStatus> = emptyList(),
    val messageText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSending: Boolean = false
)

/**
 * ViewModel for managing event chat functionality.
 *
 * This ViewModel handles: - Real-time message streaming - Sending messages - Typing indicators -
 * User profile lookups
 */
class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ChatUiState())
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  private var messagesJob: Job? = null
  private var typingJob: Job? = null
  private var typingDebounceJob: Job? = null

  /**
   * Initializes the chat for a specific event.
   *
   * @param eventId The ID of the event to load the chat for.
   */
  fun initializeChat(eventId: String) {
    _uiState.update { it.copy(isLoading = true, error = null) }

    // Load event details
    viewModelScope.launch {
      try {
        val event = eventRepository.getEvent(eventId)
        _uiState.update { it.copy(event = event) }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to load event: ${e.message}") }
      }
    }

    // Load current user
    viewModelScope.launch {
      try {
        val currentUserId = AuthenticationProvider.currentUser
        if (currentUserId != null) {
          val user = userRepository.getUserById(currentUserId)
          _uiState.update { it.copy(currentUser = user) }
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = "Failed to load user: ${e.message}") }
      }
    }

    // Observe messages
    messagesJob?.cancel()
    messagesJob =
        viewModelScope.launch {
          chatRepository.observeMessages(eventId).collect { messages ->
            _uiState.update { it.copy(messages = messages, isLoading = false) }
          }
        }

    // Observe typing status
    typingJob?.cancel()
    typingJob =
        viewModelScope.launch {
          chatRepository.observeTypingUsers(eventId).collect { typingUsers ->
            // Filter out the current user from typing indicators
            val currentUserId = AuthenticationProvider.currentUser
            val filteredTypingUsers = typingUsers.filter { it.userId != currentUserId }
            _uiState.update { it.copy(typingUsers = filteredTypingUsers) }
          }
        }
  }

  /**
   * Updates the message text in the input field.
   *
   * @param text The new message text.
   */
  fun updateMessageText(text: String) {
    _uiState.update { it.copy(messageText = text) }

    // Update typing status
    val currentState = _uiState.value
    if (text.isNotBlank() && currentState.event != null && currentState.currentUser != null) {
      updateTypingStatus(isTyping = true)

      // Debounce: Stop typing status after user stops typing for 2 seconds
      typingDebounceJob?.cancel()
      typingDebounceJob =
          viewModelScope.launch {
            delay(2000)
            updateTypingStatus(isTyping = false)
          }
    } else if (text.isBlank()) {
      updateTypingStatus(isTyping = false)
      typingDebounceJob?.cancel()
    }
  }

  /**
   * Sends the current message.
   *
   * @param onSuccess Callback invoked when the message is successfully sent.
   */
  fun sendMessage(onSuccess: () -> Unit = {}) {
    val currentState = _uiState.value
    val messageText = currentState.messageText.trim()

    if (messageText.isBlank()) return

    val currentUser = currentState.currentUser ?: return
    val event = currentState.event ?: return

    _uiState.update { it.copy(isSending = true) }

    val message =
        ChatMessage(
            messageId = chatRepository.getNewMessageId(),
            eventId = event.uid,
            senderId = currentUser.userId,
            senderName = currentUser.getFullName(),
            content = messageText,
            timestamp = Timestamp.now())

    chatRepository.sendMessage(
        message = message,
        onSuccess = {
          _uiState.update { it.copy(messageText = "", isSending = false) }
          // Stop typing indicator after sending
          updateTypingStatus(isTyping = false)
          typingDebounceJob?.cancel()
          onSuccess()
        },
        onFailure = { exception ->
          _uiState.update {
            it.copy(error = "Failed to send message: ${exception.message}", isSending = false)
          }
        })
  }

  /**
   * Updates the typing status for the current user.
   *
   * @param isTyping Whether the user is currently typing.
   */
  private fun updateTypingStatus(isTyping: Boolean) {
    val currentState = _uiState.value
    val currentUser = currentState.currentUser ?: return
    val event = currentState.event ?: return

    val typingStatus =
        TypingStatus(
            userId = currentUser.userId,
            userName = currentUser.getFullName(),
            eventId = event.uid,
            isTyping = isTyping,
            lastUpdate = Timestamp.now())

    chatRepository.updateTypingStatus(
        typingStatus = typingStatus, onSuccess = {}, onFailure = { /* Silently fail */})
  }

  /** Clears any error messages. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }

  override fun onCleared() {
    super.onCleared()
    messagesJob?.cancel()
    typingJob?.cancel()
    typingDebounceJob?.cancel()

    // Stop typing indicator when leaving chat
    updateTypingStatus(isTyping = false)
  }
}

package com.github.se.studentconnect.model.chat

import com.github.se.studentconnect.model.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing chat messages and typing status.
 *
 * This repository provides methods for sending messages, observing real-time message updates, and
 * managing typing indicators.
 */
interface ChatRepository : Repository {
  /**
   * Observes messages for a specific event in real-time.
   *
   * @param eventId The ID of the event to observe messages for.
   * @return A Flow of list of ChatMessage objects, ordered by timestamp (oldest first).
   */
  fun observeMessages(eventId: String): Flow<List<ChatMessage>>

  /**
   * Sends a new message to an event chat.
   *
   * @param message The ChatMessage to send.
   * @param onSuccess Callback invoked when the message is successfully sent.
   * @param onFailure Callback invoked when sending the message fails.
   */
  fun sendMessage(message: ChatMessage, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Observes users who are currently typing in an event chat.
   *
   * @param eventId The ID of the event to observe typing status for.
   * @return A Flow of list of TypingStatus objects for users currently typing.
   */
  fun observeTypingUsers(eventId: String): Flow<List<TypingStatus>>

  /**
   * Updates the typing status for the current user.
   *
   * @param typingStatus The TypingStatus to update.
   * @param onSuccess Callback invoked when the status is successfully updated.
   * @param onFailure Callback invoked when updating the status fails.
   */
  fun updateTypingStatus(
      typingStatus: TypingStatus,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Generates a new unique message ID.
   *
   * @return A new unique message ID string.
   */
  fun getNewMessageId(): String
}

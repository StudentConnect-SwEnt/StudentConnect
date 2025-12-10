package com.github.se.studentconnect.model.chat

import com.google.firebase.Timestamp

/**
 * Represents a chat message in an event chat.
 *
 * @property messageId The unique identifier for the message.
 * @property eventId The ID of the event this message belongs to.
 * @property senderId The user ID of the message sender.
 * @property senderName The display name of the sender (firstName + lastName).
 * @property content The text content of the message.
 * @property timestamp When the message was sent.
 */
data class ChatMessage(
    val messageId: String,
    val eventId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Timestamp = Timestamp.now()
) {
  init {
    require(messageId.isNotBlank()) { "Message ID cannot be blank" }
    require(eventId.isNotBlank()) { "Event ID cannot be blank" }
    require(senderId.isNotBlank()) { "Sender ID cannot be blank" }
    require(senderName.isNotBlank()) { "Sender name cannot be blank" }
    require(content.isNotBlank()) { "Message content cannot be blank" }
    require(content.length <= 1000) { "Message content cannot exceed 1000 characters" }
  }

  /**
   * Converts the ChatMessage to a Map for Firestore storage.
   *
   * @return A map representation of the ChatMessage.
   */
  fun toMap(): Map<String, Any> {
    return mapOf(
        "messageId" to messageId,
        "eventId" to eventId,
        "senderId" to senderId,
        "senderName" to senderName,
        "content" to content,
        "timestamp" to timestamp)
  }

  companion object {
    /**
     * Creates a ChatMessage instance from a Map (typically from Firestore).
     *
     * @param map The map containing message data.
     * @return A ChatMessage instance, or null if the map is invalid.
     */
    fun fromMap(map: Map<String, Any?>): ChatMessage? {
      return try {
        ChatMessage(
            messageId = map["messageId"] as? String ?: return null,
            eventId = map["eventId"] as? String ?: return null,
            senderId = map["senderId"] as? String ?: return null,
            senderName = map["senderName"] as? String ?: return null,
            content = map["content"] as? String ?: return null,
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now())
      } catch (e: Exception) {
        null
      }
    }
  }
}

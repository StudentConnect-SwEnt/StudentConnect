package com.github.se.studentconnect.model.chat

import com.google.firebase.Timestamp

/**
 * Represents a user's typing status in an event chat.
 *
 * @property userId The ID of the user who is typing.
 * @property userName The display name of the user who is typing.
 * @property eventId The ID of the event.
 * @property isTyping Whether the user is currently typing.
 * @property lastUpdate The last time the typing status was updated.
 */
data class TypingStatus(
    val userId: String,
    val userName: String,
    val eventId: String,
    val isTyping: Boolean,
    val lastUpdate: Timestamp = Timestamp.now()
) {
  init {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    require(userName.isNotBlank()) { "User name cannot be blank" }
    require(eventId.isNotBlank()) { "Event ID cannot be blank" }
  }

  /**
   * Converts the TypingStatus to a Map for Firestore storage.
   *
   * @return A map representation of the TypingStatus.
   */
  fun toMap(): Map<String, Any> {
    return mapOf(
        "userId" to userId,
        "userName" to userName,
        "eventId" to eventId,
        "isTyping" to isTyping,
        "lastUpdate" to lastUpdate)
  }

  companion object {
    /**
     * Creates a TypingStatus instance from a Map (typically from Firestore).
     *
     * @param map The map containing typing status data.
     * @return A TypingStatus instance, or null if the map is invalid.
     */
    fun fromMap(map: Map<String, Any?>): TypingStatus? {
      return try {
        TypingStatus(
            userId = map["userId"] as? String ?: return null,
            userName = map["userName"] as? String ?: return null,
            eventId = map["eventId"] as? String ?: return null,
            isTyping = map["isTyping"] as? Boolean ?: false,
            lastUpdate = map["lastUpdate"] as? Timestamp ?: Timestamp.now())
      } catch (e: Exception) {
        null
      }
    }
  }
}

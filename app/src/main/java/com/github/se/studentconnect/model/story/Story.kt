package com.github.se.studentconnect.model.story

import com.google.firebase.Timestamp

/**
 * Represents a story linked to an event.
 *
 * @property storyId Unique identifier for the story
 * @property userId The user who created the story
 * @property eventId The event this story is linked to
 * @property mediaUrl The Firebase Storage URL for the story media
 * @property createdAt Timestamp when the story was created
 * @property expiresAt Timestamp when the story expires (24h after creation)
 */
data class Story(
    val storyId: String,
    val userId: String,
    val eventId: String,
    val mediaUrl: String,
    val createdAt: Timestamp,
    val expiresAt: Timestamp
) {
  fun toMap(): Map<String, Any> =
      mapOf(
          "storyId" to storyId,
          "userId" to userId,
          "eventId" to eventId,
          "mediaUrl" to mediaUrl,
          "createdAt" to createdAt,
          "expiresAt" to expiresAt)

  companion object {
    /**
     * Creates a Story instance from a Map (typically from Firestore).
     *
     * @param map The map containing story data.
     * @return A Story instance, or null if the map is invalid.
     */
    fun fromMap(map: Map<String, Any?>): Story? {
      return try {
        Story(
            storyId = map["storyId"] as? String ?: return null,
            userId = map["userId"] as? String ?: return null,
            eventId = map["eventId"] as? String ?: return null,
            mediaUrl = map["mediaUrl"] as? String ?: return null,
            createdAt = map["createdAt"] as? Timestamp ?: return null,
            expiresAt = map["expiresAt"] as? Timestamp ?: return null)
      } catch (e: Exception) {
        null
      }
    }
  }
}


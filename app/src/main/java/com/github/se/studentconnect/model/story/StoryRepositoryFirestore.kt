package com.github.se.studentconnect.model.story

import android.content.Context
import android.net.Uri
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.utils.ImageCompressor
import com.github.se.studentconnect.utils.MediaTypeDetector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of StoryRepository.
 *
 * Stories are stored in: stories/{storyId} Storage path: stories/{eventId}/{userId}/{timestamp}
 */
class StoryRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
) : StoryRepository {

  companion object {
    private const val STORIES_COLLECTION = "stories"
    private const val STORAGE_PATH_PREFIX = "stories"
    private const val SECONDS_PER_HOUR = 3600
    private const val HOURS_PER_DAY = 24
    internal const val STORY_EXPIRATION_SECONDS =
        HOURS_PER_DAY * SECONDS_PER_HOUR // 24 hours in seconds
  }

  override suspend fun getUserJoinedEvents(userId: String): List<Event> {
    return try {
      val joinedEventIds = userRepository.getJoinedEvents(userId)
      joinedEventIds.mapNotNull { eventId ->
        try {
          eventRepository.getEvent(eventId)
        } catch (e: Exception) {
          null
        }
      }
    } catch (e: Exception) {
      emptyList()
    }
  }

  override suspend fun uploadStory(
      fileUri: Uri,
      eventId: String,
      userId: String,
      context: Context
  ): Story? {
    return try {
      // Detect media type (image or video)
      val mediaType = MediaTypeDetector.detectMediaType(context, fileUri)

      // Compress only images before upload (videos are uploaded as-is)
      val uriToUpload =
          if (mediaType == MediaType.IMAGE) {
            ImageCompressor.compressImage(context, fileUri) ?: fileUri
          } else {
            fileUri
          }

      // Generate storage path: stories/{eventId}/{userId}/{timestamp}
      val timestamp = System.currentTimeMillis()
      val storagePath = "$STORAGE_PATH_PREFIX/$eventId/$userId/$timestamp"

      // Upload to Firebase Storage
      val mediaUrl = mediaRepository.upload(uriToUpload, storagePath)

      // Create Firestore document
      val storyId = db.collection(STORIES_COLLECTION).document().id

      // Calculate temporary expiresAt (will be updated with correct value based on server
      // timestamp)
      val tempCreatedAt = Timestamp.now()
      val tempExpiresAt =
          Timestamp(tempCreatedAt.seconds + STORY_EXPIRATION_SECONDS, tempCreatedAt.nanoseconds)

      // Save to Firestore with serverTimestamp for createdAt and temporary expiresAt
      // expiresAt will be updated with the correct value after we get the actual server timestamp
      val storyData =
          mapOf(
              "storyId" to storyId,
              "userId" to userId,
              "eventId" to eventId,
              "mediaUrl" to mediaUrl,
              "createdAt" to FieldValue.serverTimestamp(),
              "expiresAt" to tempExpiresAt,
              "mediaType" to mediaType.value)

      db.collection(STORIES_COLLECTION).document(storyId).set(storyData).await()

      // Fetch the document back to get the actual serverTimestamp for createdAt
      val savedDoc = db.collection(STORIES_COLLECTION).document(storyId).get().await()
      val savedData = savedDoc.data ?: emptyMap()

      // Calculate expiresAt based on actual server timestamp (24 hours later)
      val actualCreatedAt = savedData["createdAt"] as? Timestamp ?: Timestamp.now()
      val actualExpiresAt =
          Timestamp(actualCreatedAt.seconds + STORY_EXPIRATION_SECONDS, actualCreatedAt.nanoseconds)

      // Update document with correct expiresAt if it differs from temporary value
      // (usually it will, since server timestamp may differ from client timestamp)
      if (actualExpiresAt.compareTo(tempExpiresAt) != 0) {
        savedDoc.reference.update("expiresAt", actualExpiresAt).await()
      }

      // Fetch again to ensure we have the complete document with correct expiresAt
      val finalDoc = db.collection(STORIES_COLLECTION).document(storyId).get().await()
      val finalData = finalDoc.data ?: emptyMap()

      Story.fromMap(finalData)
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun getEventStories(eventId: String): List<Story> {
    return try {
      val now = Timestamp.now()
      val querySnapshot =
          db.collection(STORIES_COLLECTION).whereEqualTo("eventId", eventId).get().await()

      querySnapshot.documents
          .mapNotNull { doc -> Story.fromMap(doc.data ?: emptyMap()) }
          .filter { story ->
            // Filter out expired stories (client-side)
            story.expiresAt.compareTo(now) > 0
          }
          .sortedByDescending { it.createdAt }
    } catch (e: Exception) {
      emptyList()
    }
  }

  override suspend fun deleteStory(storyId: String, userId: String): Boolean {
    return try {
      // Get story document
      val storyDoc = db.collection(STORIES_COLLECTION).document(storyId).get().await()

      if (!storyDoc.exists()) {
        return false
      }

      val story = Story.fromMap(storyDoc.data ?: emptyMap()) ?: return false

      // Verify user is the owner
      if (story.userId != userId) {
        return false
      }

      // Delete from Storage
      try {
        mediaRepository.delete(story.mediaUrl)
      } catch (e: Exception) {
        // Continue even if storage deletion fails
      }

      // Delete from Firestore
      storyDoc.reference.delete().await()

      true
    } catch (e: Exception) {
      false
    }
  }
}

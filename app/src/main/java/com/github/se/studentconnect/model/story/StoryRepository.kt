package com.github.se.studentconnect.model.story

import android.content.Context
import android.net.Uri
import com.github.se.studentconnect.model.Repository
import com.github.se.studentconnect.model.event.Event

/**
 * Repository interface for managing stories.
 *
 * Stories are linked to events and visible to:
 * - Users who joined the event
 * - Friends of the story creator (even if they haven't joined the event)
 */
interface StoryRepository : Repository {

  /**
   * Gets all events available for story linking, including both events the user has joined and
   * events created by the user.
   *
   * @param userId The user identifier
   * @return List of events the user can link stories to (joined events and owned events)
   */
  suspend fun getUserJoinedEvents(userId: String): List<Event>

  /**
   * Uploads a story media file and creates a story document in Firestore.
   *
   * @param fileUri The local URI of the media file to upload
   * @param eventId The event this story is linked to
   * @param userId The user creating the story
   * @param context The application context
   * @return The created Story, or null if upload fails
   */
  suspend fun uploadStory(fileUri: Uri, eventId: String, userId: String, context: Context): Story?

  /**
   * Gets all stories for a specific event, filtering out expired stories.
   *
   * @param eventId The event identifier
   * @return List of non-expired stories for the event
   */
  suspend fun getEventStories(eventId: String): List<Story>

  /**
   * Deletes a story from both Firestore and Storage.
   *
   * @param storyId The story identifier
   * @param userId The user attempting to delete (must be the story owner)
   * @return true if deletion was successful, false otherwise
   */
  suspend fun deleteStory(storyId: String, userId: String): Boolean
}

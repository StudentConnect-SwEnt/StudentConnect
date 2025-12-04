package com.github.se.studentconnect.model.friends

import com.github.se.studentconnect.model.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing real-time friend location data.
 *
 * This interface defines operations for reading and writing location data to Firebase Realtime
 * Database, allowing friends to see each other's live positions on the map.
 */
interface FriendsLocationRepository : Repository {

  /**
   * Observes the real-time locations of all friends for the given user.
   *
   * @param userId The unique identifier of the current user
   * @param friendIds List of friend user IDs to track
   * @return A Flow emitting a map of userId to FriendLocation whenever any friend's location
   *   updates
   */
  fun observeFriendLocations(
      userId: String,
      friendIds: List<String>
  ): Flow<Map<String, FriendLocation>>

  /**
   * Updates the current user's location in the database for friends to see.
   *
   * @param userId The unique identifier of the current user
   * @param latitude The current latitude
   * @param longitude The current longitude
   */
  suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double)

  /**
   * Removes the current user's location from the database (e.g., when they stop sharing).
   *
   * @param userId The unique identifier of the current user
   */
  suspend fun removeUserLocation(userId: String)

  /**
   * Starts listening for friend location updates. This should be called when the map screen becomes
   * active.
   */
  fun startListening()

  /**
   * Stops listening for friend location updates. This should be called when the map screen is
   * destroyed.
   */
  fun stopListening()
}

package com.github.se.studentconnect.model.friends

/**
 * Represents a friend's real-time location on the map.
 *
 * @property userId The unique identifier of the friend
 * @property latitude The latitude coordinate of the friend's location
 * @property longitude The longitude coordinate of the friend's location
 * @property timestamp The timestamp when the location was last updated (milliseconds since epoch)
 */
data class FriendLocation(
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L
) {
  companion object {
    /**
     * Maximum age of location data in milliseconds before it's considered stale. Default: 5 minutes
     */
    const val MAX_LOCATION_AGE_MS = 5 * 60 * 1000L
  }

  /** Checks if this location data is fresh based on the current time. */
  fun isFresh(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
    return (currentTimeMs - timestamp) <= MAX_LOCATION_AGE_MS
  }
}

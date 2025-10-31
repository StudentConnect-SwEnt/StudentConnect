package com.github.se.studentconnect.model.friends

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Realtime Database implementation of FriendsLocationRepository.
 *
 * Database structure:
 * ```
 * friends_locations/
 *   ├── userId1/
 *   │   ├── latitude: Double
 *   │   ├── longitude: Double
 *   │   └── timestamp: Long
 *   ├── userId2/
 *   │   ├── latitude: Double
 *   │   ├── longitude: Double
 *   │   └── timestamp: Long
 * ```
 *
 * @property database The Firebase Realtime Database instance
 */
class FriendsLocationRepositoryFirebase(private val database: FirebaseDatabase) :
    FriendsLocationRepository {

  companion object {
    private const val TAG = "FriendsLocationRepo"
    private const val LOCATIONS_PATH = "friends_locations"
  }

  private val locationsRef: DatabaseReference = database.getReference(LOCATIONS_PATH)
  private val listeners = mutableMapOf<String, ValueEventListener>()

  override fun observeFriendLocations(
      userId: String,
      friendIds: List<String>
  ): Flow<Map<String, FriendLocation>> = callbackFlow {
    val currentLocations = mutableMapOf<String, FriendLocation>()

    Log.d(TAG, "Starting to observe ${friendIds.size} friends for user $userId")

    // Create a ValueEventListener for each friend - reads all data in one snapshot
    val valueListeners = mutableMapOf<String, ValueEventListener>()

    friendIds.forEach { friendId ->
      val friendRef = locationsRef.child(friendId)

      val listener =
          object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
              try {
                if (!snapshot.exists()) {
                  // Friend removed their location
                  currentLocations.remove(friendId)
                  trySend(currentLocations.toMap())
                  Log.d(TAG, "Friend $friendId removed their location")
                  return
                }

                // Read all fields directly from the snapshot (no extra network call)
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                val longitude = snapshot.child("longitude").getValue(Double::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java)

                if (latitude != null && longitude != null && timestamp != null) {
                  val location =
                      FriendLocation(
                          userId = friendId,
                          latitude = latitude,
                          longitude = longitude,
                          timestamp = timestamp)

                  if (location.isFresh()) {
                    currentLocations[friendId] = location
                    trySend(currentLocations.toMap())
                    Log.d(TAG, "Updated location for friend $friendId: ($latitude, $longitude)")
                  } else {
                    Log.d(TAG, "Location for friend $friendId is stale, ignoring")
                  }
                } else {
                  Log.w(
                      TAG,
                      "Incomplete location data for friend $friendId - lat: $latitude, lon: $longitude, ts: $timestamp")
                }
              } catch (e: Exception) {
                Log.e(TAG, "Error parsing location for friend $friendId", e)
              }
            }

            override fun onCancelled(error: DatabaseError) {
              Log.e(TAG, "Error observing friend $friendId: ${error.message}")
            }
          }

      friendRef.addValueEventListener(listener)
      valueListeners[friendId] = listener
      // Store in class-level map for stopListening() cleanup
      listeners[friendId] = listener
    }

    // Clean up listeners when the flow is closed
    awaitClose {
      Log.d(TAG, "Stopping observation of friend locations")
      valueListeners.forEach { (friendId, listener) ->
        locationsRef.child(friendId).removeEventListener(listener)
        // Also remove from class-level map
        listeners.remove(friendId)
      }
      valueListeners.clear()
    }
  }

  override suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double) {
    try {
      val locationData =
          mapOf(
              "latitude" to latitude,
              "longitude" to longitude,
              "timestamp" to System.currentTimeMillis())

      locationsRef.child(userId).setValue(locationData).await()
      Log.d(TAG, "Updated location for user $userId: ($latitude, $longitude)")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to update location for user $userId", e)
      throw e
    }
  }

  override suspend fun removeUserLocation(userId: String) {
    try {
      locationsRef.child(userId).removeValue().await()
      Log.d(TAG, "Removed location for user $userId")
    } catch (e: Exception) {
      Log.e(TAG, "Failed to remove location for user $userId", e)
      throw e
    }
  }

  override fun startListening() {
    // Listeners are managed per-flow in observeFriendLocations
    Log.d(TAG, "FriendsLocationRepository is ready to listen")
  }

  override fun stopListening() {
    // Clean up any remaining listeners from the class-level map
    listeners.forEach { (friendId, listener) ->
      locationsRef.child(friendId).removeEventListener(listener)
    }
    listeners.clear()
    Log.d(TAG, "Stopped all location listeners")
  }
}

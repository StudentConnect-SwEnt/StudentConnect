package com.github.se.studentconnect.model.friends

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
  private val listeners = mutableMapOf<String, ChildEventListener>()

  override fun observeFriendLocations(
      userId: String,
      friendIds: List<String>
  ): Flow<Map<String, FriendLocation>> = callbackFlow {
    val currentLocations = mutableMapOf<String, FriendLocation>()

    Log.d(TAG, "Starting to observe ${friendIds.size} friends for user $userId")

    // Create a listener for each friend
    val childListeners = mutableMapOf<String, ChildEventListener>()

    friendIds.forEach { friendId ->
      val friendRef = locationsRef.child(friendId)

      val listener =
          object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
              updateLocationFromSnapshot(friendId, snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
              updateLocationFromSnapshot(friendId, snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
              currentLocations.remove(friendId)
              trySend(currentLocations.toMap())
              Log.d(TAG, "Friend $friendId removed their location")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
              // Not relevant for location updates, but required by ChildEventListener
            }

            override fun onCancelled(error: DatabaseError) {
              Log.e(TAG, "Error observing friend $friendId: ${error.message}")
            }

            private fun updateLocationFromSnapshot(friendId: String, snapshot: DataSnapshot) {
              try {
                // Read all location data at once
                val parentSnapshot = snapshot.ref.parent
                parentSnapshot?.get()?.addOnSuccessListener { dataSnapshot ->
                  val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                  val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)
                  val timestamp = dataSnapshot.child("timestamp").getValue(Long::class.java)

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
                  }
                }
              } catch (e: Exception) {
                Log.e(TAG, "Error parsing location for friend $friendId", e)
              }
            }
          }

      friendRef.addChildEventListener(listener)
      childListeners[friendId] = listener
    }

    // Clean up listeners when the flow is closed
    awaitClose {
      Log.d(TAG, "Stopping observation of friend locations")
      childListeners.forEach { (friendId, listener) ->
        locationsRef.child(friendId).removeEventListener(listener)
      }
      childListeners.clear()
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
    // Clean up any remaining listeners
    listeners.forEach { (_, listener) -> locationsRef.removeEventListener(listener) }
    listeners.clear()
    Log.d(TAG, "Stopped all location listeners")
  }
}

package com.github.se.studentconnect.model.friends

import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsLocationRepositoryFirebaseTest {
  private lateinit var mockDatabase: FirebaseDatabase
  private lateinit var mockLocationsRef: DatabaseReference
  private lateinit var mockUserLocationRef: DatabaseReference
  private lateinit var repository: FriendsLocationRepositoryFirebase

  @Before
  fun setUp() {
    mockDatabase = mockk(relaxed = true)
    mockLocationsRef = mockk(relaxed = true)
    mockUserLocationRef = mockk(relaxed = true)
    every { mockDatabase.getReference("friends_locations") } returns mockLocationsRef
    every { mockLocationsRef.child(any()) } returns mockUserLocationRef
    repository = FriendsLocationRepositoryFirebase(mockDatabase)
  }

  @After fun tearDown() = unmockkAll()

  @Test
  fun updateUserLocation_callsSetValueWithCorrectData() = runTest {
    val slot = slot<Map<String, Any>>()
    every { mockUserLocationRef.setValue(capture(slot)) } returns Tasks.forResult(null)
    repository.updateUserLocation("user123", 46.5191, 6.5668)
    verify { mockUserLocationRef.setValue(any()) }
    assertEquals(46.5191, slot.captured["latitude"] as Double, 0.0001)
    assertEquals(6.5668, slot.captured["longitude"] as Double, 0.0001)
    assertTrue(slot.captured.containsKey("timestamp"))
  }

  @Test
  fun updateUserLocation_throwsExceptionOnFailure() = runTest {
    every { mockUserLocationRef.setValue(any()) } returns
        Tasks.forException(Exception("Firebase error"))
    try {
      repository.updateUserLocation("user123", 46.5191, 6.5668)
      fail("Expected exception")
    } catch (e: Exception) {
      assertEquals("Firebase error", e.message)
    }
  }

  @Test
  fun removeUserLocation_callsRemoveValue() = runTest {
    every { mockUserLocationRef.removeValue() } returns Tasks.forResult(null)
    repository.removeUserLocation("user123")
    verify { mockUserLocationRef.removeValue() }
  }

  @Test
  fun removeUserLocation_throwsExceptionOnFailure() = runTest {
    every { mockUserLocationRef.removeValue() } returns
        Tasks.forException(Exception("Firebase error"))
    try {
      repository.removeUserLocation("user123")
      fail("Expected exception")
    } catch (e: Exception) {
      assertEquals("Firebase error", e.message)
    }
  }

  @Test
  fun startListening_logsReadyMessage() {
    repository.startListening()
    // Should not throw exceptions, mainly logs
  }

  @Test
  fun stopListening_removesAllListeners() {
    repository.stopListening()
    // Should clear listeners without exceptions
  }

  @Test
  fun observeFriendLocations_withEmptyList_createsFlowSuccessfully() {
    val flow = repository.observeFriendLocations("user", emptyList())
    assertNotNull(flow)
  }

  @Test
  fun observeFriendLocations_addsValueEventListenersForEachFriend() = runTest {
    val friendIds = listOf("friend1", "friend2", "friend3")
    val mockFriendRefs = friendIds.associateWith { mockk<DatabaseReference>(relaxed = true) }
    mockFriendRefs.forEach { (friendId, ref) ->
      every { mockLocationsRef.child(friendId) } returns ref
    }

    val job = launch { repository.observeFriendLocations("user", friendIds).collect {} }
    advanceUntilIdle()

    mockFriendRefs.values.forEach { ref -> verify { ref.addValueEventListener(any()) } }

    job.cancel()
  }

  @Test
  fun observeFriendLocations_onDataChange_emitsFreshLocation() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.child("latitude").getValue(Double::class.java) } returns 46.5191
    every { mockSnapshot.child("longitude").getValue(Double::class.java) } returns 6.5668
    every { mockSnapshot.child("timestamp").getValue(Long::class.java) } returns
        System.currentTimeMillis()

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))

    // Use a mutable list to collect emitted values
    val emittedLocations = mutableListOf<Map<String, FriendLocation>>()
    val job = launch { flow.collect { locations -> emittedLocations.add(locations) } }

    advanceUntilIdle()
    // Trigger the listener with onDataChange
    listenerSlot.captured.onDataChange(mockSnapshot)
    advanceUntilIdle()

    // Verify that the fresh location was emitted through trySend
    assertTrue("Expected at least one emission", emittedLocations.isNotEmpty())
    val lastEmission = emittedLocations.last()
    assertTrue("Expected location for $friendId", lastEmission.containsKey(friendId))
    assertEquals(46.5191, lastEmission[friendId]?.latitude ?: 0.0, 0.0001)
    assertEquals(6.5668, lastEmission[friendId]?.longitude ?: 0.0, 0.0001)

    job.cancel()
  }

  @Test
  fun observeFriendLocations_onDataChange_updatesLocation() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.child("latitude").getValue(Double::class.java) } returns 47.0
    every { mockSnapshot.child("longitude").getValue(Double::class.java) } returns 7.0
    every { mockSnapshot.child("timestamp").getValue(Long::class.java) } returns
        System.currentTimeMillis()

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))

    // Use a mutable list to collect emitted values
    val emittedLocations = mutableListOf<Map<String, FriendLocation>>()
    val job = launch { flow.collect { locations -> emittedLocations.add(locations) } }

    advanceUntilIdle()
    // Trigger the listener with onDataChange
    listenerSlot.captured.onDataChange(mockSnapshot)
    advanceUntilIdle()

    // Verify that the fresh location was emitted through trySend
    assertTrue("Expected at least one emission", emittedLocations.isNotEmpty())
    val lastEmission = emittedLocations.last()
    assertTrue("Expected location for $friendId", lastEmission.containsKey(friendId))
    assertEquals(47.0, lastEmission[friendId]?.latitude ?: 0.0, 0.0001)
    assertEquals(7.0, lastEmission[friendId]?.longitude ?: 0.0, 0.0001)

    job.cancel()
  }

  @Test
  fun observeFriendLocations_snapshotNotExists_removesLocation() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockSnapshot.exists() } returns false

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))
    val emittedLocations = mutableListOf<Map<String, FriendLocation>>()
    val job = launch { flow.collect { emittedLocations.add(it) } }

    advanceUntilIdle()
    listenerSlot.captured.onDataChange(mockSnapshot)
    advanceUntilIdle()

    // Verify location removed (not in map)
    if (emittedLocations.isNotEmpty()) {
      val lastEmission = emittedLocations.last()
      assertFalse("Removed location should not be in map", lastEmission.containsKey(friendId))
    }

    job.cancel()
  }

  @Test
  fun observeFriendLocations_onCancelled_logsError() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockError = mockk<DatabaseError>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockError.message } returns "Database error"

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))
    val job = launch { flow.collect {} }

    advanceUntilIdle()
    listenerSlot.captured.onCancelled(mockError)
    advanceUntilIdle()

    job.cancel()
  }

  @Test
  fun observeFriendLocations_staleLocation_isIgnored() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.child("latitude").getValue(Double::class.java) } returns 46.5191
    every { mockSnapshot.child("longitude").getValue(Double::class.java) } returns 6.5668
    // Stale timestamp (more than 5 minutes old)
    every { mockSnapshot.child("timestamp").getValue(Long::class.java) } returns
        System.currentTimeMillis() - (6 * 60 * 1000L)

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))

    // Use a mutable list to collect emitted values
    val emittedLocations = mutableListOf<Map<String, FriendLocation>>()
    val job = launch { flow.collect { emittedLocations.add(it) } }

    advanceUntilIdle()
    listenerSlot.captured.onDataChange(mockSnapshot)
    advanceUntilIdle()

    // Verify that stale location was NOT added to the emitted locations
    // Either no emissions or the friend is not in the locations map
    if (emittedLocations.isNotEmpty()) {
      val lastEmission = emittedLocations.last()
      assertFalse("Stale location should not be added", lastEmission.containsKey(friendId))
    }

    job.cancel()
  }

  @Test
  fun observeFriendLocations_nullLocationData_isIgnored() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.child("latitude").getValue(Double::class.java) } returns null
    every { mockSnapshot.child("longitude").getValue(Double::class.java) } returns null
    every { mockSnapshot.child("timestamp").getValue(Long::class.java) } returns null

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))

    // Use a mutable list to collect emitted values
    val emittedLocations = mutableListOf<Map<String, FriendLocation>>()
    val job = launch { flow.collect { emittedLocations.add(it) } }

    advanceUntilIdle()
    listenerSlot.captured.onDataChange(mockSnapshot)
    advanceUntilIdle()

    // Verify that null location data was NOT added
    if (emittedLocations.isNotEmpty()) {
      val lastEmission = emittedLocations.last()
      assertFalse("Null location should not be added", lastEmission.containsKey(friendId))
    }

    job.cancel()
  }

  @Test
  fun observeFriendLocations_exceptionDuringParsing_isHandled() = runTest {
    val friendId = "friend1"
    val mockFriendRef = mockk<DatabaseReference>(relaxed = true)
    val mockSnapshot = mockk<DataSnapshot>(relaxed = true)

    every { mockLocationsRef.child(friendId) } returns mockFriendRef
    every { mockSnapshot.exists() } returns true
    every { mockSnapshot.child("latitude").getValue(Double::class.java) } throws
        Exception("Parse error")

    val listenerSlot = slot<ValueEventListener>()
    every { mockFriendRef.addValueEventListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured
        }

    val flow = repository.observeFriendLocations("user", listOf(friendId))
    val job = launch { flow.collect {} }

    advanceUntilIdle()
    listenerSlot.captured.onDataChange(mockSnapshot)
    advanceUntilIdle()

    // Test passes if no exception is thrown (exception is caught and logged)
    job.cancel()
  }

  @Test
  fun observeFriendLocations_cleanupRemovesListeners() = runTest {
    val friendIds = listOf("friend1", "friend2")
    val mockFriendRefs = friendIds.associateWith { mockk<DatabaseReference>(relaxed = true) }
    mockFriendRefs.forEach { (friendId, ref) ->
      every { mockLocationsRef.child(friendId) } returns ref
    }

    val flow = repository.observeFriendLocations("user", friendIds)
    val job = launch { flow.collect {} }
    advanceUntilIdle()

    job.cancel()
    advanceUntilIdle()

    mockFriendRefs.values.forEach { ref ->
      verify { ref.removeEventListener(any<ValueEventListener>()) }
    }
  }
}

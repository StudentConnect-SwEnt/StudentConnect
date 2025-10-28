package com.github.se.studentconnect.model.friends

import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val capturedData = slot.captured
    assertEquals(46.5191, capturedData["latitude"] as Double, 0.0001)
    assertEquals(6.5668, capturedData["longitude"] as Double, 0.0001)
    assertTrue(capturedData.containsKey("timestamp"))
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
  fun observeFriendLocations_withEmptyList_createsFlowSuccessfully() {
    val flow = repository.observeFriendLocations("user", emptyList())
    assertNotNull(flow)
  }
}

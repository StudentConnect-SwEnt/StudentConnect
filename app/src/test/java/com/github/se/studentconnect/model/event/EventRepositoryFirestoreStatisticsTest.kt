package com.github.se.studentconnect.model.event

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class EventRepositoryFirestoreStatisticsTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockEventsCollection: CollectionReference
  @Mock private lateinit var mockUsersCollection: CollectionReference
  @Mock private lateinit var mockEventDocument: DocumentReference
  @Mock private lateinit var mockParticipantsCollection: CollectionReference
  @Mock private lateinit var mockParticipantsQuerySnapshot: QuerySnapshot

  private lateinit var repository: EventRepositoryFirestore

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    repository = EventRepositoryFirestore(mockFirestore)

    whenever(mockFirestore.collection("events")).thenReturn(mockEventsCollection)
    whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
    whenever(mockEventsCollection.document(any())).thenReturn(mockEventDocument)
    whenever(mockEventDocument.collection("participants")).thenReturn(mockParticipantsCollection)
  }

  @Test
  fun `getEventStatistics with no participants returns empty statistics`() = runTest {
    // Arrange
    val eventUid = "event123"
    whenever(mockParticipantsCollection.get())
        .thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
    whenever(mockParticipantsQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getEventStatistics(eventUid, 100)

    // Assert
    assertEquals(eventUid, result.eventId)
    assertEquals(0, result.totalAttendees)
    assertTrue(result.ageDistribution.isEmpty())
    assertTrue(result.campusDistribution.isEmpty())
    assertTrue(result.joinRateOverTime.isEmpty())
    assertEquals(100, result.followerCount)
    assertEquals(0f, result.attendeesFollowersRate)

    // Verify interactions
    verify(mockFirestore).collection("events")
    verify(mockEventDocument).collection("participants")
  }

  @Test
  fun `getEventStatistics calculates distributions and percentages correctly`() = runTest {
    // Arrange
    val eventUid = "event456"
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Create timestamps on different days for join rate testing
    val day1 = Timestamp(Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000))
    val day2 = Timestamp(Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000))

    val participants =
        listOf(
            createMockParticipant("user1", day1), // EPFL, 20 years old
            createMockParticipant("user2", day1), // EPFL, 20 years old
            createMockParticipant("user3", day2), // UNIL, 24 years old
            createMockParticipant("user4", day2) // Unknown campus, unknown age
            )

    whenever(mockParticipantsCollection.get())
        .thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
    whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

    setupUserMock("user1", "01/01/${currentYear - 20}", "EPFL")
    setupUserMock("user2", "01/01/${currentYear - 20}", "EPFL")
    setupUserMock("user3", "01/01/${currentYear - 24}", "UNIL")
    setupUserMock("user4", null, null) // null birthday and university

    // Act
    val result = repository.getEventStatistics(eventUid, 200)

    // Assert - Basic counts
    assertEquals(4, result.totalAttendees)
    assertEquals(200, result.followerCount)
    assertEquals(2f, result.attendeesFollowersRate) // 4/200 * 100 = 2%

    // Assert - Age distribution (2 in 18-22, 1 in 23-25, 1 Unknown filtered or shown)
    assertTrue(result.ageDistribution.isNotEmpty())
    val ageGroup1822 = result.ageDistribution.find { it.ageRange == AgeGroups.AGE_18_22 }
    assertEquals(2, ageGroup1822?.count)
    assertEquals(50f, ageGroup1822?.percentage) // 2/4 * 100 = 50%

    // Assert - Campus distribution
    assertTrue(result.campusDistribution.isNotEmpty())
    val epflData = result.campusDistribution.find { it.campusName == "EPFL" }
    assertEquals(2, epflData?.count)
    assertEquals(50f, epflData?.percentage)

    val unknownCampus = result.campusDistribution.find { it.campusName == "Unknown" }
    assertEquals(1, unknownCampus?.count)

    // Assert - Join rate over time (should have 2 data points for 2 different days)
    assertTrue(result.joinRateOverTime.isNotEmpty())
    assertEquals(2, result.joinRateOverTime.size)
    // First day should have 2 cumulative, second day should have 4 cumulative
    assertEquals(2, result.joinRateOverTime[0].cumulativeJoins)
    assertEquals(4, result.joinRateOverTime[1].cumulativeJoins)
  }

  @Test
  fun `getEventStatistics handles zero followers and exception during user fetch`() = runTest {
    // Arrange
    val eventUid = "event789"
    val joinTimestamp = Timestamp.now()

    val participants =
        listOf(
            createMockParticipant("user1", joinTimestamp),
            createMockParticipant("user2", joinTimestamp))

    whenever(mockParticipantsCollection.get())
        .thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
    whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

    // User1 fetch succeeds
    setupUserMock("user1", "01/01/2000", "EPFL")

    // User2 fetch throws exception (covers catch block line 435-438)
    val mockUserRef2 = mock(DocumentReference::class.java)
    whenever(mockUserRef2.get()).thenReturn(Tasks.forException(RuntimeException("Network error")))
    whenever(mockUsersCollection.document("user2")).thenReturn(mockUserRef2)

    // Act - with zero followers
    val result = repository.getEventStatistics(eventUid, 0)

    // Assert
    assertEquals(2, result.totalAttendees)
    assertEquals(0, result.followerCount)
    assertEquals(0f, result.attendeesFollowersRate) // Division guard: 0 when followerCount is 0

    // Only user1 data should be in distributions (user2 failed)
    assertTrue(result.ageDistribution.isNotEmpty())
    assertTrue(result.campusDistribution.isNotEmpty())
  }

  @Test
  fun `getEventStatistics handles participants with null joinedAt`() = runTest {
    // Arrange
    val eventUid = "eventNullJoin"

    // All participants have null joinedAt - tests line 509 filter and line 512 empty check
    val participants =
        listOf(createMockParticipant("user1", null), createMockParticipant("user2", null))

    whenever(mockParticipantsCollection.get())
        .thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
    whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

    setupUserMock("user1", "01/01/2000", "EPFL")
    setupUserMock("user2", "01/01/2000", "UNIL")

    // Act
    val result = repository.getEventStatistics(eventUid, 50)

    // Assert
    assertEquals(2, result.totalAttendees)
    // Join rate should be empty since all joinedAt are null
    assertTrue(result.joinRateOverTime.isEmpty())
    // But age/campus distributions should still work
    assertTrue(result.ageDistribution.isNotEmpty())
    assertTrue(result.campusDistribution.isNotEmpty())
  }

  @Test
  fun `getEventStatistics handles user document not existing`() = runTest {
    // Arrange
    val eventUid = "eventNoUser"
    val joinTimestamp = Timestamp.now()

    val participant = createMockParticipant("user1", joinTimestamp)
    whenever(mockParticipantsCollection.get())
        .thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
    whenever(mockParticipantsQuerySnapshot.documents).thenReturn(listOf(participant))

    // User document doesn't exist (covers line 428 exists() check returning false)
    val mockUserDoc = mock(DocumentSnapshot::class.java)
    whenever(mockUserDoc.exists()).thenReturn(false)
    val mockUserRef = mock(DocumentReference::class.java)
    whenever(mockUserRef.get()).thenReturn(Tasks.forResult(mockUserDoc))
    whenever(mockUsersCollection.document("user1")).thenReturn(mockUserRef)

    // Act
    val result = repository.getEventStatistics(eventUid, 100)

    // Assert - participant counted but no user data extracted
    assertEquals(1, result.totalAttendees)
    assertTrue(result.ageDistribution.isEmpty()) // No user data = no distributions
    assertTrue(result.campusDistribution.isEmpty())
  }

  // Helper functions

  private fun createMockParticipant(uid: String, joinedAt: Timestamp?): DocumentSnapshot {
    val mock = mock(DocumentSnapshot::class.java)
    whenever(mock.getString("uid")).thenReturn(uid)
    whenever(mock.getTimestamp("joinedAt")).thenReturn(joinedAt)
    whenever(mock.getString("status")).thenReturn("joined")
    return mock
  }

  private fun setupUserMock(uid: String, birthday: String?, university: String?) {
    val mockUserDoc = mock(DocumentSnapshot::class.java)
    whenever(mockUserDoc.exists()).thenReturn(true)
    whenever(mockUserDoc.getString("birthday")).thenReturn(birthday)
    whenever(mockUserDoc.getString("university")).thenReturn(university)

    val mockUserRef = mock(DocumentReference::class.java)
    whenever(mockUserRef.get()).thenReturn(Tasks.forResult(mockUserDoc))
    whenever(mockUsersCollection.document(uid)).thenReturn(mockUserRef)
  }
}

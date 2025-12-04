package com.github.se.studentconnect.model.event

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.Date

class EventRepositoryFirestoreStatisticsTest {

    @Mock private lateinit var mockFirestore: FirebaseFirestore
    @Mock private lateinit var mockEventsCollection: CollectionReference
    @Mock private lateinit var mockUsersCollection: CollectionReference
    @Mock private lateinit var mockEventDocument: DocumentReference
    @Mock private lateinit var mockParticipantsCollection: CollectionReference
    @Mock private lateinit var mockParticipantsQuerySnapshot: QuerySnapshot
    @Mock private lateinit var mockUsersQuery: Query
    @Mock private lateinit var mockUsersQuerySnapshot: QuerySnapshot

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
        whenever(mockParticipantsCollection.get()).thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
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
        
        verify(mockFirestore).collection("events")
        verify(mockEventDocument).collection("participants")
    }

    @Test
    fun `getEventStatistics calculates distributions and percentages correctly`() = runTest {
        // Arrange
        val eventUid = "event456"
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val day1 = Timestamp(Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000))
        val day2 = Timestamp(Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000))
        
        val participants = listOf(
            createMockParticipant("user1", day1),
            createMockParticipant("user2", day1),
            createMockParticipant("user3", day2),
            createMockParticipant("user4", day2)
        )
        
        whenever(mockParticipantsCollection.get()).thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
        whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

        // Setup batched user query mock
        setupBatchedUserQuery(listOf(
            UserData("user1", "01/01/${currentYear - 20}", "EPFL"),
            UserData("user2", "01/01/${currentYear - 20}", "EPFL"),
            UserData("user3", "01/01/${currentYear - 24}", "UNIL"),
            UserData("user4", null, null)
        ))

        // Act
        val result = repository.getEventStatistics(eventUid, 200)

        // Assert
        assertEquals(4, result.totalAttendees)
        assertEquals(200, result.followerCount)
        assertEquals(2f, result.attendeesFollowersRate)

        assertTrue(result.ageDistribution.isNotEmpty())
        val ageGroup1822 = result.ageDistribution.find { it.ageRange == AgeGroups.AGE_18_22 }
        assertEquals(2, ageGroup1822?.count)
        assertEquals(50f, ageGroup1822?.percentage)

        assertTrue(result.campusDistribution.isNotEmpty())
        val epflData = result.campusDistribution.find { it.campusName == "EPFL" }
        assertEquals(2, epflData?.count)
        assertEquals(50f, epflData?.percentage)
        
        val unknownCampus = result.campusDistribution.find { it.campusName == AgeGroups.UNKNOWN }
        assertEquals(1, unknownCampus?.count)

        assertTrue(result.joinRateOverTime.isNotEmpty())
    }

    @Test
    fun `getEventStatistics handles zero followers`() = runTest {
        // Arrange
        val eventUid = "event789"
        val joinTimestamp = Timestamp.now()
        
        val participants = listOf(createMockParticipant("user1", joinTimestamp))
        
        whenever(mockParticipantsCollection.get()).thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
        whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

        setupBatchedUserQuery(listOf(UserData("user1", "01/01/2000", "EPFL")))

        // Act
        val result = repository.getEventStatistics(eventUid, 0)

        // Assert
        assertEquals(1, result.totalAttendees)
        assertEquals(0, result.followerCount)
        assertEquals(0f, result.attendeesFollowersRate)
    }

    @Test
    fun `getEventStatistics handles participants with null joinedAt`() = runTest {
        // Arrange
        val eventUid = "eventNullJoin"
        
        val participants = listOf(
            createMockParticipant("user1", null),
            createMockParticipant("user2", null)
        )
        
        whenever(mockParticipantsCollection.get()).thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
        whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

        setupBatchedUserQuery(listOf(
            UserData("user1", "01/01/2000", "EPFL"),
            UserData("user2", "01/01/2000", "UNIL")
        ))

        // Act
        val result = repository.getEventStatistics(eventUid, 50)

        // Assert
        assertEquals(2, result.totalAttendees)
        assertTrue(result.joinRateOverTime.isEmpty())
        assertTrue(result.ageDistribution.isNotEmpty())
        assertTrue(result.campusDistribution.isNotEmpty())
    }

    @Test
    fun `getEventStatistics handles query exception gracefully`() = runTest {
        // Arrange
        val eventUid = "eventError"
        val joinTimestamp = Timestamp.now()
        
        val participants = listOf(createMockParticipant("user1", joinTimestamp))
        
        whenever(mockParticipantsCollection.get()).thenReturn(Tasks.forResult(mockParticipantsQuerySnapshot))
        whenever(mockParticipantsQuerySnapshot.documents).thenReturn(participants)

        // Simulate query failure
        whenever(mockUsersCollection.whereIn(any<FieldPath>(), any())).thenReturn(mockUsersQuery)
        whenever(mockUsersQuery.get()).thenReturn(Tasks.forException(RuntimeException("Network error")))

        // Act
        val result = repository.getEventStatistics(eventUid, 100)

        // Assert - should handle gracefully with no user data
        assertEquals(1, result.totalAttendees)
        assertTrue(result.ageDistribution.isEmpty())
        assertTrue(result.campusDistribution.isEmpty())
    }

    // Helper classes and functions
    
    private data class UserData(val uid: String, val birthday: String?, val university: String?)

    private fun createMockParticipant(uid: String, joinedAt: Timestamp?): DocumentSnapshot {
        val mock = mock(DocumentSnapshot::class.java)
        whenever(mock.getString("uid")).thenReturn(uid)
        whenever(mock.getTimestamp("joinedAt")).thenReturn(joinedAt)
        whenever(mock.getString("status")).thenReturn("joined")
        return mock
    }

    private fun setupBatchedUserQuery(users: List<UserData>) {
        val userDocs = users.map { userData ->
            val doc = mock(DocumentSnapshot::class.java)
            whenever(doc.id).thenReturn(userData.uid)
            whenever(doc.getString("birthday")).thenReturn(userData.birthday)
            whenever(doc.getString("university")).thenReturn(userData.university)
            doc
        }

        whenever(mockUsersCollection.whereIn(any<FieldPath>(), any())).thenReturn(mockUsersQuery)
        whenever(mockUsersQuery.get()).thenReturn(Tasks.forResult(mockUsersQuerySnapshot))
        whenever(mockUsersQuerySnapshot.documents).thenReturn(userDocs)
    }
}

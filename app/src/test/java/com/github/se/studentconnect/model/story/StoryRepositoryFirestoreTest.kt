package com.github.se.studentconnect.model.story

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.repository.UserRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Mockito.lenient
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

// Test implementations for suspend function interfaces
private class TestMediaRepository : MediaRepository {
  private val uploads = mutableMapOf<Pair<Uri, String?>, String>()
  var shouldThrowOnUpload: Throwable? = null
  var shouldThrowOnDelete: Throwable? = null

  override suspend fun upload(uri: Uri, path: String?): String {
    shouldThrowOnUpload?.let { throw it }
    val key = uri to path
    val result = path ?: "media/${System.currentTimeMillis()}"
    uploads[key] = result
    return result
  }

  override suspend fun download(id: String): Uri = Uri.parse("file:///$id")

  override suspend fun delete(id: String) {
    shouldThrowOnDelete?.let { throw it }
  }
}

private class TestUserRepository : UserRepository {
  var joinedEvents: List<String> = emptyList()
  var shouldThrowOnGetJoinedEvents: Throwable? = null

  override suspend fun getJoinedEvents(userId: String): List<String> {
    shouldThrowOnGetJoinedEvents?.let { throw it }
    return joinedEvents
  }

  override suspend fun leaveEvent(eventId: String, userId: String) {}

  override suspend fun getUserById(userId: String) = null

  override suspend fun getUserByEmail(email: String) = null

  override suspend fun getAllUsers(): List<com.github.se.studentconnect.model.User> = emptyList()

  override suspend fun getUsersPaginated(
      limit: Int,
      lastUserId: String?
  ): Pair<List<com.github.se.studentconnect.model.User>, Boolean> =
      emptyList<com.github.se.studentconnect.model.User>() to false

  override suspend fun saveUser(user: com.github.se.studentconnect.model.User) {}

  override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

  override suspend fun deleteUser(userId: String) {}

  override suspend fun getUsersByUniversity(
      university: String
  ): List<com.github.se.studentconnect.model.User> = emptyList()

  override suspend fun getUsersByHobby(
      hobby: String
  ): List<com.github.se.studentconnect.model.User> = emptyList()

  override suspend fun getNewUid() = "new-uid"

  override suspend fun addEventToUser(eventId: String, userId: String) {}

  override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {}

  override suspend fun getInvitations(
      userId: String
  ): List<com.github.se.studentconnect.ui.screen.activities.Invitation> = emptyList()

  override suspend fun acceptInvitation(eventId: String, userId: String) {}

  override suspend fun declineInvitation(eventId: String, userId: String) {}

  override suspend fun joinEvent(eventId: String, userId: String) {}

  override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {}

  override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

  override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

  override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

  override suspend fun checkUsernameAvailability(username: String) = true
}

private class TestEventRepository : EventRepository {
  private val events = mutableMapOf<String, Event>()
  var shouldThrowOnGetEvent: ((String) -> Throwable?)? = null

  fun addTestEvent(event: Event) {
    events[event.uid] = event
  }

  override suspend fun getEvent(eventUid: String): Event {
    shouldThrowOnGetEvent?.invoke(eventUid)?.let { throw it }
    return events[eventUid] ?: throw Exception("Event not found")
  }

  override fun getNewUid() = "new-event-uid"

  override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

  override suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event> =
      emptyList()

  override suspend fun getEventParticipants(
      eventUid: String
  ): List<com.github.se.studentconnect.model.event.EventParticipant> = emptyList()

  override suspend fun addEvent(event: Event) {
    events[event.uid] = event
  }

  override suspend fun editEvent(eventUid: String, newEvent: Event) {}

  override suspend fun deleteEvent(eventUid: String) {}

  override suspend fun addParticipantToEvent(
      eventUid: String,
      participant: com.github.se.studentconnect.model.event.EventParticipant
  ) {}

  override suspend fun addInvitationToEvent(
      eventUid: String,
      invitedUser: String,
      currentUserId: String
  ) {}

  override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {}
}

class StoryRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockContentResolver: ContentResolver

  private lateinit var mockMediaRepository: TestMediaRepository
  private lateinit var mockUserRepository: TestUserRepository
  private lateinit var mockEventRepository: TestEventRepository
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockQuery: Query

  private lateinit var repository: StoryRepositoryFirestore

  private val testEvent =
      Event.Public(
          uid = "event123",
          ownerId = "owner456",
          title = "Test Event",
          subtitle = "Test Subtitle",
          description = "Test Description",
          start = Timestamp.now(),
          isFlash = false)

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Mock context for ImageCompressor FIRST (before repository creation)
    // Compression will fail, so original URI is used
    // Use lenient to avoid strict stubbing issues
    lenient().`when`(mockContext.contentResolver).thenReturn(mockContentResolver)
    lenient()
        .`when`(mockContentResolver.openInputStream(any()))
        .thenReturn(null) // Compression fails, returns null

    // Create concrete test implementations for suspend function interfaces
    mockMediaRepository = TestMediaRepository()
    mockUserRepository = TestUserRepository()
    mockEventRepository = TestEventRepository()

    repository =
        StoryRepositoryFirestore(
            mockFirestore,
            mockMediaRepository,
            mockUserRepository,
            mockEventRepository,
            mockContext)

    // Default mock behavior
    whenever(mockFirestore.collection("stories")).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    whenever(mockCollectionReference.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery)
  }

  @Test
  fun getUserJoinedEvents_withValidEvents_returnsEvents() = runTest {
    // Arrange
    val userId = "user123"
    val eventIds = listOf("event123", "event456")
    mockUserRepository.joinedEvents = eventIds
    mockEventRepository.addTestEvent(testEvent)
    mockEventRepository.shouldThrowOnGetEvent = { eventId ->
      if (eventId == "event456") Exception("Event not found") else null
    }

    // Act
    val result = repository.getUserJoinedEvents(userId)

    // Assert
    assertEquals(1, result.size)
    assertEquals("event123", result[0].uid)
  }

  @Test
  fun getUserJoinedEvents_withNoJoinedEvents_returnsEmptyList() = runTest {
    // Arrange
    val userId = "user123"
    mockUserRepository.joinedEvents = emptyList()

    // Act
    val result = repository.getUserJoinedEvents(userId)

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun getUserJoinedEvents_withException_returnsEmptyList() = runTest {
    // Arrange
    val userId = "user123"
    mockUserRepository.shouldThrowOnGetJoinedEvents = Exception("Database error")

    // Act
    val result = repository.getUserJoinedEvents(userId)

    // Assert
    assertTrue(result.isEmpty())
  }

  // Note: uploadStory tests that require ImageCompressor are moved to instrumented tests
  // See StoryRepositoryFirestoreInstrumentedTest.kt

  @Test
  fun getEventStories_withValidStories_returnsNonExpiredStories() = runTest {
    // Arrange
    val eventId = "event123"
    val now = Timestamp.now()
    val futureExpiresAt = Timestamp(now.seconds + 3600, now.nanoseconds) // 1 hour from now
    val pastExpiresAt = Timestamp(now.seconds - 3600, now.nanoseconds) // 1 hour ago

    val validStoryData =
        mapOf(
            "storyId" to "story1",
            "userId" to "user1",
            "eventId" to eventId,
            "mediaUrl" to "stories/event123/user1/123",
            "createdAt" to Timestamp(now.seconds - 3600, now.nanoseconds),
            "expiresAt" to futureExpiresAt)

    val expiredStoryData: Map<String, Any> =
        mapOf(
            "storyId" to "story2",
            "userId" to "user2",
            "eventId" to eventId,
            "mediaUrl" to "stories/event123/user2/456",
            "createdAt" to Timestamp(now.seconds - 7200, now.nanoseconds),
            "expiresAt" to pastExpiresAt)

    val mockDoc1 = mock(DocumentSnapshot::class.java)
    val mockDoc2 = mock(DocumentSnapshot::class.java)
    whenever(mockDoc1.data).thenReturn(validStoryData)
    whenever(mockDoc2.data).thenReturn(expiredStoryData)

    whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

    // Act
    val result = repository.getEventStories(eventId)

    // Assert
    assertEquals(1, result.size)
    assertEquals("story1", result[0].storyId)
  }

  @Test
  fun getEventStories_withException_returnsEmptyList() = runTest {
    // Arrange
    val eventId = "event123"
    val mockQueryTask = Tasks.forException<QuerySnapshot>(Exception("Query failed"))
    whenever(mockQuery.get()).thenReturn(mockQueryTask)

    // Act
    val result = repository.getEventStories(eventId)

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun deleteStory_withValidOwnership_deletesStory() = runTest {
    // Arrange
    val storyId = "story123"
    val userId = "user456"
    val storyData: Map<String, Any> =
        mapOf(
            "storyId" to storyId,
            "userId" to userId,
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/123",
            "createdAt" to Timestamp.now(),
            "expiresAt" to Timestamp.now())

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data).thenReturn(storyData)
    whenever(mockDocumentSnapshot.reference).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))
    // MediaRepository.delete is a suspend function, so it doesn't need explicit mocking for success
    // case
    // The actual implementation will call it, but we can verify it was called

    // Act
    val result = repository.deleteStory(storyId, userId)

    // Assert
    assertTrue("Delete should succeed", result)
    // Can't verify on concrete implementation, but we can check the result
    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteStory_withInvalidOwnership_returnsFalse() = runTest {
    // Arrange
    val storyId = "story123"
    val userId = "user456"
    val storyData: Map<String, Any> =
        mapOf(
            "storyId" to storyId,
            "userId" to "differentUser", // Different owner
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/123",
            "createdAt" to Timestamp.now(),
            "expiresAt" to Timestamp.now())

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data).thenReturn(storyData)

    // Act
    val result = repository.deleteStory(storyId, userId)

    // Assert
    assertFalse("Delete should fail for non-owner", result)
    // Can't verify on concrete implementation, but we can check the result
    verify(mockDocumentReference, never()).delete()
  }

  @Test
  fun deleteStory_withNonExistentStory_returnsFalse() = runTest {
    // Arrange
    val storyId = "story123"
    val userId = "user456"

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.exists()).thenReturn(false)

    // Act
    val result = repository.deleteStory(storyId, userId)

    // Assert
    assertFalse("Delete should fail for non-existent story", result)
  }

  @Test
  fun deleteStory_withStorageDeleteFailure_stillDeletesFromFirestore() = runTest {
    // Arrange
    val storyId = "story123"
    val userId = "user456"
    val storyData: Map<String, Any> =
        mapOf(
            "storyId" to storyId,
            "userId" to userId,
            "eventId" to "event789",
            "mediaUrl" to "stories/event789/user456/123",
            "createdAt" to Timestamp.now(),
            "expiresAt" to Timestamp.now())

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data).thenReturn(storyData)
    whenever(mockDocumentSnapshot.reference).thenReturn(mockDocumentReference)
    mockMediaRepository.shouldThrowOnDelete = Exception("Storage delete failed")
    whenever(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    // Act
    val result = repository.deleteStory(storyId, userId)

    // Assert
    assertTrue("Delete should succeed even if storage delete fails", result)
    // Storage delete will throw, but Firestore delete should still happen
    verify(mockDocumentReference).delete()
  }
}

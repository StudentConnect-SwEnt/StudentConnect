package com.github.se.studentconnect.model.story

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.user.UserRepositoryLocal
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

class StoryRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockContentResolver: ContentResolver

  private lateinit var mockMediaRepository: TestMediaRepository
  private lateinit var mockUserRepository: UserRepositoryLocal
  private lateinit var mockEventRepository: EventRepositoryLocal
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
    mockUserRepository = UserRepositoryLocal()
    mockEventRepository = EventRepositoryLocal()

    repository =
        StoryRepositoryFirestore(
            mockFirestore, mockMediaRepository, mockUserRepository, mockEventRepository)

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
    // Add joined events using the local repository
    eventIds.forEach { eventId -> mockUserRepository.addEventToUser(eventId, userId) }
    // Add test event to the local repository
    mockEventRepository.addEvent(testEvent)
    // event456 doesn't exist, so it will throw when trying to get it

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
    // No events joined, local repository starts empty

    // Act
    val result = repository.getUserJoinedEvents(userId)

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun getUserJoinedEvents_withException_returnsEmptyList() = runTest {
    // Arrange
    val userId = "user123"
    // Add a joined event that doesn't exist in EventRepository
    mockUserRepository.addEventToUser("nonexistent-event", userId)
    // This will cause getEvent to throw, which should be caught and return empty list

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
            "expiresAt" to futureExpiresAt,
            "mediaType" to "image")

    val expiredStoryData: Map<String, Any> =
        mapOf(
            "storyId" to "story2",
            "userId" to "user2",
            "eventId" to eventId,
            "mediaUrl" to "stories/event123/user2/456",
            "createdAt" to Timestamp(now.seconds - 7200, now.nanoseconds),
            "expiresAt" to pastExpiresAt,
            "mediaType" to "image")

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
            "expiresAt" to Timestamp.now(),
            "mediaType" to "image")

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
            "expiresAt" to Timestamp.now(),
            "mediaType" to "image")

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
            "expiresAt" to Timestamp.now(),
            "mediaType" to "image")

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

  @Test
  fun deleteStory_withInvalidStoryData_returnsFalse() = runTest {
    // Arrange
    val storyId = "story123"
    val userId = "user456"
    // Invalid story data (missing required fields)
    val invalidStoryData: Map<String, Any> = mapOf("storyId" to storyId)

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data).thenReturn(invalidStoryData)

    // Act
    val result = repository.deleteStory(storyId, userId)

    // Assert
    assertFalse("Delete should fail when story data is invalid", result)
    verify(mockDocumentReference, never()).delete()
  }

  @Test
  fun getEventStories_withInvalidStoryData_filtersOutInvalidStories() = runTest {
    // Arrange
    val eventId = "event123"
    val now = Timestamp.now()
    val futureExpiresAt = Timestamp(now.seconds + 3600, now.nanoseconds)

    val validStoryData =
        mapOf(
            "storyId" to "story1",
            "userId" to "user1",
            "eventId" to eventId,
            "mediaUrl" to "stories/event123/user1/123",
            "createdAt" to Timestamp(now.seconds - 3600, now.nanoseconds),
            "expiresAt" to futureExpiresAt,
            "mediaType" to "image")

    // Invalid story data (missing required fields)
    val invalidStoryData: Map<String, Any> = mapOf("storyId" to "story2", "eventId" to eventId)

    val mockDoc1 = mock(DocumentSnapshot::class.java)
    val mockDoc2 = mock(DocumentSnapshot::class.java)
    whenever(mockDoc1.data).thenReturn(validStoryData)
    whenever(mockDoc2.data).thenReturn(invalidStoryData)

    whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

    // Act
    val result = repository.getEventStories(eventId)

    // Assert
    assertEquals(1, result.size)
    assertEquals("story1", result[0].storyId)
  }
}

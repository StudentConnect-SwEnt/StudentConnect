package com.github.se.studentconnect.model.story

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class StoryRepositoryFirestoreInstrumentedTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockMediaRepository: MediaRepository
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockEventRepository: EventRepository
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuery: Query

  private lateinit var context: Context
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

  // Test implementations for suspend function interfaces
  private class TestMediaRepository : MediaRepository {
    private val uploads = mutableMapOf<Pair<Uri, String?>, String>()
    var shouldThrowOnUpload: Throwable? = null

    override suspend fun upload(uri: Uri, path: String?): String {
      shouldThrowOnUpload?.let { throw it }
      val key = uri to path
      val result = path ?: "media/${System.currentTimeMillis()}"
      uploads[key] = result
      return result
    }

    override suspend fun download(id: String): Uri = Uri.parse("file:///$id")

    override suspend fun delete(id: String) {}
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

    fun addTestEvent(event: Event) {
      events[event.uid] = event
    }

    override suspend fun getEvent(eventUid: String): Event {
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

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    context = InstrumentationRegistry.getInstrumentation().targetContext

    val mockMediaRepo = TestMediaRepository()
    val mockUserRepo = TestUserRepository()
    val mockEventRepo = TestEventRepository()

    repository =
        StoryRepositoryFirestore(mockFirestore, mockMediaRepo, mockUserRepo, mockEventRepo, context)

    // Default mock behavior
    whenever(mockFirestore.collection("stories")).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    whenever(mockCollectionReference.whereEqualTo(anyString(), anyString())).thenReturn(mockQuery)
  }

  @Test
  fun uploadStory_success_createsStory() = runTest {
    // Arrange - create a real image file for testing
    val testImageFile = File(context.cacheDir, "test_image.jpg")
    FileOutputStream(testImageFile).use { out ->
      Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
          .compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    val fileUri = Uri.fromFile(testImageFile)
    val eventId = "event123"
    val userId = "user456"
    val storyId = "story789"
    val now = Timestamp.now()
    val expiresAt = Timestamp(now.seconds + StoryRepositoryFirestore.STORY_EXPIRATION_SECONDS, now.nanoseconds)

    // Mock Firestore document creation
    val mockNewDocRef = mock(DocumentReference::class.java)
    whenever(mockCollectionReference.document()).thenReturn(mockNewDocRef)
    whenever(mockNewDocRef.id).thenReturn(storyId)
    whenever(mockNewDocRef.set(any<Map<String, Any>>())).thenReturn(Tasks.forResult(null))
    whenever(mockNewDocRef.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    val expectedMediaUrlPattern = "stories/$eventId/$userId/"

    // Mock document data (first fetch after set)
    val initialDocumentData: Map<String, Any> =
        mapOf(
            "storyId" to storyId,
            "userId" to userId,
            "eventId" to eventId,
            "mediaUrl" to "stories/$eventId/$userId/1234567890",
            "createdAt" to now,
            "mediaType" to "image")
    whenever(mockDocumentSnapshot.data).thenReturn(initialDocumentData)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.reference).thenReturn(mockNewDocRef)

    // Mock final document data (after expiresAt update) - second fetch
    val finalDocumentData = initialDocumentData.toMutableMap() as MutableMap<String, Any>
    finalDocumentData["expiresAt"] = expiresAt
    val mockFinalDoc = mock(DocumentSnapshot::class.java)
    whenever(mockFinalDoc.data).thenReturn(finalDocumentData)
    whenever(mockFinalDoc.exists()).thenReturn(true)

    // First get() call (after set) returns initial data
    // Second get() call (after update) returns final data
    whenever(mockCollectionReference.document(storyId)).thenReturn(mockNewDocRef)
    whenever(mockNewDocRef.get())
        .thenReturn(Tasks.forResult(mockDocumentSnapshot)) // First call after set()
        .thenReturn(Tasks.forResult(mockFinalDoc)) // Second call after update()

    // Act
    val result = repository.uploadStory(fileUri, eventId, userId)

    // Assert
    assertNotNull("Story should not be null", result)
    assertEquals(storyId, result?.storyId)
    assertEquals(userId, result?.userId)
    assertEquals(eventId, result?.eventId)
    assertNotNull(result?.mediaUrl)
    assertTrue(
        "MediaUrl should match expected pattern",
        result?.mediaUrl?.startsWith(expectedMediaUrlPattern) == true)
    verify(mockNewDocRef).set(any<Map<String, Any>>())
    verify(mockNewDocRef).update(anyString(), any())

    // Cleanup
    testImageFile.delete()
  }

  @Test
  fun uploadStory_withUploadFailure_returnsNull() = runTest {
    // Arrange - create a real image file for testing
    val testImageFile = File(context.cacheDir, "test_image.jpg")
    FileOutputStream(testImageFile).use { out ->
      Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
          .compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    val fileUri = Uri.fromFile(testImageFile)
    val eventId = "event123"
    val userId = "user456"

    // Create a repository with a throwing media repository
    val throwingMediaRepo =
        object : MediaRepository {
          override suspend fun upload(uri: Uri, path: String?): String {
            throw Exception("Upload failed")
          }

          override suspend fun download(id: String): Uri = Uri.parse("file:///$id")

          override suspend fun delete(id: String) {}
        }
    val testMediaRepo =
        StoryRepositoryFirestore(
            mockFirestore, throwingMediaRepo, TestUserRepository(), TestEventRepository(), context)

    // Act
    val result = testMediaRepo.uploadStory(fileUri, eventId, userId)

    // Assert
    assertNull("Story should be null on upload failure", result)

    // Cleanup
    testImageFile.delete()
  }

  @Test
  fun uploadStory_withVideoFile_uploadsWithoutCompression() = runTest {
    // Arrange - create a dummy video file
    val testVideoFile = File(context.cacheDir, "test_video.mp4")
    testVideoFile.createNewFile()
    val fileUri = Uri.fromFile(testVideoFile)
    val eventId = "event123"
    val userId = "user456"
    val storyId = "story789"
    val now = Timestamp.now()
    val expiresAt = Timestamp(now.seconds + StoryRepositoryFirestore.STORY_EXPIRATION_SECONDS, now.nanoseconds)

    val mockMediaRepo = TestMediaRepository()
    val mockUserRepo = TestUserRepository()
    val mockEventRepo = TestEventRepository()
    val testRepo =
        StoryRepositoryFirestore(mockFirestore, mockMediaRepo, mockUserRepo, mockEventRepo, context)

    val mockNewDocRef = mock(DocumentReference::class.java)
    whenever(mockCollectionReference.document()).thenReturn(mockNewDocRef)
    whenever(mockNewDocRef.id).thenReturn(storyId)
    whenever(mockNewDocRef.set(any<Map<String, Any>>())).thenReturn(Tasks.forResult(null))
    whenever(mockNewDocRef.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    val initialDocumentData: Map<String, Any> =
        mapOf(
            "storyId" to storyId,
            "userId" to userId,
            "eventId" to eventId,
            "mediaUrl" to "stories/$eventId/$userId/1234567890",
            "createdAt" to now,
            "mediaType" to "video")
    whenever(mockDocumentSnapshot.data).thenReturn(initialDocumentData)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.reference).thenReturn(mockNewDocRef)

    val finalDocumentData = initialDocumentData.toMutableMap() as MutableMap<String, Any>
    finalDocumentData["expiresAt"] = expiresAt
    val mockFinalDoc = mock(DocumentSnapshot::class.java)
    whenever(mockFinalDoc.data).thenReturn(finalDocumentData)
    whenever(mockFinalDoc.exists()).thenReturn(true)

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockNewDocRef)
    whenever(mockNewDocRef.get())
        .thenReturn(Tasks.forResult(mockDocumentSnapshot))
        .thenReturn(Tasks.forResult(mockFinalDoc))

    // Act
    val result = testRepo.uploadStory(fileUri, eventId, userId)

    // Assert
    assertNotNull("Story should not be null", result)
    assertEquals(MediaType.VIDEO, result?.mediaType)

    // Cleanup
    testVideoFile.delete()
  }

  @Test
  fun uploadStory_withValidData_createsStoryWithCorrectExpiresAt() = runTest {
    // Arrange - create a real image file
    val testImageFile = File(context.cacheDir, "test_image.jpg")
    FileOutputStream(testImageFile).use { out ->
      Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
          .compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    val fileUri = Uri.fromFile(testImageFile)
    val eventId = "event123"
    val userId = "user456"
    val storyId = "story789"
    val now = Timestamp.now()
    val expiresAt = Timestamp(now.seconds + StoryRepositoryFirestore.STORY_EXPIRATION_SECONDS, now.nanoseconds)

    val mockNewDocRef = mock(DocumentReference::class.java)
    whenever(mockCollectionReference.document()).thenReturn(mockNewDocRef)
    whenever(mockNewDocRef.id).thenReturn(storyId)
    whenever(mockNewDocRef.set(any<Map<String, Any>>())).thenReturn(Tasks.forResult(null))
    whenever(mockNewDocRef.update(anyString(), any())).thenReturn(Tasks.forResult(null))

    val documentData: Map<String, Any> =
        mapOf(
            "storyId" to storyId,
            "userId" to userId,
            "eventId" to eventId,
            "mediaUrl" to "stories/$eventId/$userId/1234567890",
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "mediaType" to "image")
    whenever(mockDocumentSnapshot.data).thenReturn(documentData)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.reference).thenReturn(mockNewDocRef)

    val mockFinalDoc = mock(DocumentSnapshot::class.java)
    whenever(mockFinalDoc.data).thenReturn(documentData)
    whenever(mockFinalDoc.exists()).thenReturn(true)

    whenever(mockCollectionReference.document(storyId)).thenReturn(mockNewDocRef)
    whenever(mockNewDocRef.get())
        .thenReturn(Tasks.forResult(mockDocumentSnapshot))
        .thenReturn(Tasks.forResult(mockFinalDoc))

    // Act
    val result = repository.uploadStory(fileUri, eventId, userId)

    // Assert
    assertNotNull("Story should not be null", result)
    assertEquals(storyId, result?.storyId)
    assertEquals(expiresAt, result?.expiresAt)

    // Cleanup
    testImageFile.delete()
  }
}

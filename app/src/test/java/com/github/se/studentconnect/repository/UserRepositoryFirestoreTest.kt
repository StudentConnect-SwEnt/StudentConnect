package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.ui.screen.activities.InvitationStatus
import com.google.android.gms.tasks.Task
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
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  @Mock private lateinit var mockQuery: Query

  @Mock private lateinit var mockJoinedEventsCollection: CollectionReference

  @Mock private lateinit var mockInvitationsCollection: CollectionReference

  private lateinit var repository: UserRepositoryFirestore

  private val testUser =
      User(
          userId = "user123",
          email = "test@epfl.ch",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          hobbies = listOf("Football", "Gaming"),
          profilePictureUrl = "https://example.com/pic.jpg",
          bio = "Computer science student",
          createdAt = 1000L,
          updatedAt = 1000L)

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    repository = UserRepositoryFirestore(mockFirestore)

    // Default mock behavior
    whenever(mockFirestore.collection("users")).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.collection("joinedEvents"))
        .thenReturn(mockJoinedEventsCollection)
    whenever(mockDocumentReference.collection("invitations")).thenReturn(mockInvitationsCollection)
  }

  @Test
  fun testGetUserByIdSuccess() = runTest {
    // Arrange
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    // Act
    val result = repository.getUserById("user123")

    // Assert
    assert(result != null)
    assert(result?.userId == "user123")
    assert(result?.email == "test@epfl.ch")
    verify(mockCollectionReference).document("user123")
    verify(mockDocumentReference).get()
  }

  @Test
  fun testGetUserByIdNotFound() = runTest {
    // Arrange
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(false)

    // Act
    val result = repository.getUserById("nonexistent")

    // Assert - Should return null for non-existent users (e.g., first-time users)
    assert(result == null)
  }

  @Test
  fun testGetUserByIdFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<DocumentSnapshot> = Tasks.forException(exception)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getUserById("user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetUserByEmailSuccess() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereEqualTo("email", "test@epfl.ch")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    // Act
    val result = repository.getUserByEmail("test@epfl.ch")

    // Assert
    assert(result != null)
    assert(result?.email == "test@epfl.ch")
  }

  @Test
  fun testGetUserByEmailNotFound() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereEqualTo("email", "notfound@epfl.ch"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(true)

    // Act
    val result = repository.getUserByEmail("notfound@epfl.ch")

    // Assert - Should return null for non-existent users
    assert(result == null)
  }

  @Test
  fun testGetUserByEmailFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)

    whenever(mockCollectionReference.whereEqualTo("email", "test@epfl.ch")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getUserByEmail("test@epfl.ch")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetAllUsersSuccess() = runTest {
    // Arrange
    val user2 =
        User(
            userId = "user456",
            email = "test2@unil.ch",
            username = "janesmith",
            firstName = "Jane",
            lastName = "Smith",
            university = "UNIL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val mockDocSnapshot2: DocumentSnapshot = mock()
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())
    whenever(mockDocSnapshot2.data).thenReturn(user2.toMap())

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockCollectionReference.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot, mockDocSnapshot2))

    // Act
    val result = repository.getAllUsers()

    // Assert
    assert(result.size == 2)
    assert(result[0].userId == "user123")
    assert(result[1].userId == "user456")
  }

  @Test
  fun testGetAllUsersEmpty() = runTest {
    // Arrange
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockCollectionReference.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getAllUsers()

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testGetAllUsersFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)
    whenever(mockCollectionReference.get()).thenReturn(mockTask)

    // Act
    val result = repository.getAllUsers()

    // Assert - Should return empty list on failure (graceful degradation)
    assert(result.isEmpty())
  }

  @Test
  fun testSaveUserSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.set(any())).thenReturn(mockTask)

    // Act
    repository.saveUser(testUser)

    // Assert
    verify(mockDocumentReference).set(testUser.toMap())
  }

  @Test
  fun testSaveUserFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<Void> = Tasks.forException(exception)
    whenever(mockDocumentReference.set(any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.saveUser(testUser)
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testUpdateUserSuccess() = runTest {
    // Arrange
    val updates = mapOf("firstName" to "Jack", "lastName" to "Brown")
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    // Act
    repository.updateUser("user123", updates)

    // Assert
    verify(mockDocumentReference).update(any<Map<String, Any?>>())
  }

  @Test
  fun testUpdateUserAddsTimestamp() = runTest {
    // Arrange
    val updates = mapOf("firstName" to "Jack")
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    // Act
    repository.updateUser("user123", updates)

    // Assert - Verify that updatedAt was added to the updates
    val captor = argumentCaptor<Map<String, Any?>>()
    verify(mockDocumentReference).update(captor.capture())
    val capturedUpdates = captor.firstValue
    assert(capturedUpdates.containsKey("updatedAt"))
    assert(capturedUpdates["firstName"] == "Jack")
  }

  @Test
  fun testUpdateUserFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val updates = mapOf("firstName" to "Jack")
    val mockTask: Task<Void> = Tasks.forException(exception)
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.updateUser("user123", updates)
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testDeleteUserSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.delete()).thenReturn(mockTask)

    // Act
    repository.deleteUser("user123")

    // Assert
    verify(mockDocumentReference).delete()
  }

  @Test
  fun testDeleteUserFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<Void> = Tasks.forException(exception)
    whenever(mockDocumentReference.delete()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.deleteUser("user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetUsersByUniversitySuccess() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereEqualTo("university", "EPFL")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    // Act
    val result = repository.getUsersByUniversity("EPFL")

    // Assert
    assert(result.size == 1)
    assert(result[0].university == "EPFL")
  }

  @Test
  fun testGetUsersByUniversityEmpty() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereEqualTo("university", "ETHZ")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getUsersByUniversity("ETHZ")

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testGetUsersByUniversityFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)

    whenever(mockCollectionReference.whereEqualTo("university", "EPFL")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getUsersByUniversity("EPFL")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetUsersByHobbySuccess() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereArrayContains("hobbies", "Football"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    // Act
    val result = repository.getUsersByHobby("Football")

    // Assert
    assert(result.size == 1)
    assert(result[0].hobbies.contains("Football"))
  }

  @Test
  fun testGetUsersByHobbyEmpty() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereArrayContains("hobbies", "Swimming"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getUsersByHobby("Swimming")

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testGetUsersByHobbyFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)

    whenever(mockCollectionReference.whereArrayContains("hobbies", "Football"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getUsersByHobby("Football")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetNewUid() = runTest {
    // Arrange
    whenever(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.id).thenReturn("generated_userId_123")

    // Act
    val newUserId = repository.getNewUid()

    // Assert
    assert(newUserId == "generated_userId_123")
    verify(mockCollectionReference).document()
  }

  @Test
  fun testGetAllUsersWithInvalidData() = runTest {
    // Arrange
    val invalidDocSnapshot: DocumentSnapshot = mock()
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())
    whenever(invalidDocSnapshot.data).thenReturn(mapOf("userId" to "")) // Invalid data

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockCollectionReference.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, invalidDocSnapshot))

    // Act
    val result = repository.getAllUsers()

    // Assert - Only valid user should be in the list
    assert(result.size == 1)
    assert(result[0].userId == "user123")
  }

  @Test
  fun testGetUserByIdWithMalformedDocument() = runTest {
    // Arrange
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data)
        .thenReturn(
            mapOf(
                "userId" to "user123",
                "email" to "invalid-email",
                "firstName" to "John",
                "lastName" to "Doe",
                "university" to "EPFL",
                "createdAt" to 1000L,
                "updatedAt" to 1000L))

    // Act
    val result = repository.getUserById("user123")

    // Assert - Should return null for malformed document
    assert(result == null)
  }

  @Test
  fun testGetUsersByHobbyWithMalformedDocuments() = runTest {
    // Arrange
    val validDocSnapshot: DocumentSnapshot = mock()
    val invalidDocSnapshot: DocumentSnapshot = mock()

    whenever(validDocSnapshot.data).thenReturn(testUser.toMap())
    whenever(invalidDocSnapshot.data)
        .thenReturn(
            mapOf(
                "userId" to "user456",
                "email" to "invalid-email",
                "firstName" to "",
                "lastName" to "Doe",
                "university" to "EPFL",
                "hobbies" to listOf("Football"),
                "createdAt" to 1000L,
                "updatedAt" to 1000L))

    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereArrayContains("hobbies", "Football"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(validDocSnapshot, invalidDocSnapshot))

    // Act
    val result = repository.getUsersByHobby("Football")

    // Assert - Only valid user should be returned
    assert(result.size == 1)
    assert(result[0].userId == "user123")
  }

  @Test
  fun testGetUsersPaginatedSuccess() = runTest {
    // Arrange
    val users =
        (1..15).map { i ->
          val mockDoc: DocumentSnapshot = mock()
          val user = testUser.copy(userId = "user$i")
          whenever(mockDoc.data).thenReturn(user.toMap())
          mockDoc
        }

    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.orderBy("userId")).thenReturn(mockQuery)
    whenever(mockQuery.limit(11L)).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(users.take(11))

    // Act
    val (resultUsers, hasMore) = repository.getUsersPaginated(10, null)

    // Assert
    assert(resultUsers.size == 10)
    assert(hasMore)
  }

  @Test
  fun testGetUsersPaginatedWithLastUserId() = runTest {
    // Arrange
    val users =
        (11..15).map { i ->
          val mockDoc: DocumentSnapshot = mock()
          val user = testUser.copy(userId = "user$i")
          whenever(mockDoc.data).thenReturn(user.toMap())
          mockDoc
        }

    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockQueryAfter: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.orderBy("userId")).thenReturn(mockQuery)
    whenever(mockQuery.limit(11L)).thenReturn(mockQuery)
    whenever(mockQuery.startAfter("user10")).thenReturn(mockQueryAfter)
    whenever(mockQueryAfter.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(users)

    // Act
    val (resultUsers, hasMore) = repository.getUsersPaginated(10, "user10")

    // Assert
    assert(resultUsers.size == 5)
    assert(!hasMore)
  }

  @Test
  fun testGetUsersPaginatedFailure() = runTest {
    // Arrange
    val exception = RuntimeException("Network timeout")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)

    whenever(mockCollectionReference.orderBy("userId")).thenReturn(mockQuery)
    whenever(mockQuery.limit(11L)).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getUsersPaginated(10, null)
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testJoinEventSuccess() = runTest {
    // Arrange
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.set(any())).thenReturn(mockTask)

    // Act
    repository.joinEvent("event123", "user123")

    // Assert
    verify(mockJoinedEventsCollection).document("event123")
    verify(mockEventDoc).set(mapOf("eventId" to "event123"))
  }

  @Test
  fun testJoinEventFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.set(any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.joinEvent("event123", "user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetJoinedEventsSuccess() = runTest {
    // Arrange
    val mockDoc1: DocumentSnapshot = mock()
    val mockDoc2: DocumentSnapshot = mock()
    val mockDoc3: DocumentSnapshot = mock()

    whenever(mockDoc1.getString("eventId")).thenReturn("event1")
    whenever(mockDoc2.getString("eventId")).thenReturn("event2")
    whenever(mockDoc3.getString("eventId")).thenReturn("event3")

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockJoinedEventsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2, mockDoc3))

    // Act
    val result = repository.getJoinedEvents("user123")

    // Assert
    assert(result.size == 3)
    assert(result == listOf("event1", "event2", "event3"))
  }

  @Test
  fun testGetJoinedEventsEmpty() = runTest {
    // Arrange
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockJoinedEventsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getJoinedEvents("user123")

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testAddEventToUserSuccess() = runTest {
    // Arrange
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.set(any())).thenReturn(mockTask)

    // Act
    repository.addEventToUser("event123", "user123")

    // Assert
    verify(mockJoinedEventsCollection).document("event123")
    verify(mockEventDoc).set(mapOf("eventId" to "event123"))
  }

  @Test
  fun testAddInvitationToUserSuccess() = runTest {
    // Arrange
    val mockInvitationDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockInvitationDoc.set(any())).thenReturn(mockTask)

    // Act
    repository.addInvitationToUser("event123", "user123", "user456")

    // Assert
    verify(mockInvitationsCollection).document("event123")
    val captor = argumentCaptor<Map<String, Any?>>()
    verify(mockInvitationDoc).set(captor.capture())
    val invitationData = captor.firstValue
    assert(invitationData["from"] == "user456")
    assert(invitationData["eventId"] == "event123")
    assert(invitationData.containsKey("timestamp"))
  }

  @Test
  fun testGetInvitationsSuccess() = runTest {
    // Arrange
    val mockDoc1: DocumentSnapshot = mock()
    val mockDoc2: DocumentSnapshot = mock()
    val timestamp = Timestamp.now()

    whenever(mockDoc1.getString("eventId")).thenReturn("event1")
    whenever(mockDoc1.getString("from")).thenReturn("user456")
    whenever(mockDoc1.getString("status")).thenReturn(InvitationStatus.Pending.name)
    whenever(mockDoc1.getTimestamp("timestamp")).thenReturn(timestamp)
    whenever(mockDoc1.id).thenReturn("event1")

    whenever(mockDoc2.getString("eventId")).thenReturn("event2")
    whenever(mockDoc2.getString("from")).thenReturn("user789")
    whenever(mockDoc2.getString("status")).thenReturn(null)
    whenever(mockDoc2.getTimestamp("timestamp")).thenReturn(timestamp)
    whenever(mockDoc2.id).thenReturn("event2")

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockInvitationsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

    // Act
    val result = repository.getInvitations("user123")

    // Assert
    assert(result.size == 2)
    assert(result[0].eventId == "event1")
    assert(result[0].from == "user456")
    assert(result[0].status == InvitationStatus.Pending)
    assert(result[1].eventId == "event2")
    assert(result[1].from == "user789")
    assert(result[1].status == InvitationStatus.Pending) // Default value
  }

  @Test
  fun testGetInvitationsEmpty() = runTest {
    // Arrange
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockInvitationsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getInvitations("user123")

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testAcceptInvitationSuccess() = runTest {
    // Arrange
    val mockInvitationDoc: DocumentReference = mock()
    val mockJoinedDoc: DocumentReference = mock()
    val mockGetTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    val mockSetTask: Task<Void> = Tasks.forResult(null)
    val mockDeleteTask: Task<Void> = Tasks.forResult(null)

    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockJoinedDoc)
    whenever(mockInvitationDoc.get()).thenReturn(mockGetTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockJoinedDoc.set(any())).thenReturn(mockSetTask)
    whenever(mockInvitationDoc.delete()).thenReturn(mockDeleteTask)

    // Act
    repository.acceptInvitation("event123", "user123")

    // Assert
    verify(mockInvitationDoc).get()
    verify(mockJoinedDoc).set(any())
    verify(mockInvitationDoc).delete()
  }
  // Add these tests to the existing UserRepositoryFirestoreTest class

  @Test
  fun testGetUserByIdReturnsNullForNonExistent() = runTest {
    // Arrange
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(false)

    // Act
    val result = repository.getUserById("nonexistent")

    // Assert - Should return null for non-existent users
    assert(result == null)
  }

  @Test
  fun testGetUserByEmailReturnsNullForNonExistent() = runTest {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockCollectionReference.whereEqualTo("email", "notfound@epfl.ch"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(true)

    // Act
    val result = repository.getUserByEmail("notfound@epfl.ch")

    // Assert - Should return null for non-existent users
    assert(result == null)
  }

  @Test
  fun testLeaveEventSuccess() = runTest {
    // Arrange
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.delete()).thenReturn(mockTask)

    // Act
    repository.leaveEvent("event123", "user123")

    // Assert
    verify(mockJoinedEventsCollection).document("event123")
    verify(mockEventDoc).delete()
  }

  @Test
  fun testLeaveEventFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.delete()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.leaveEvent("event123", "user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testSendInvitationSuccess() = runTest {
    // Arrange
    val mockEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockEventSnapshot: DocumentSnapshot = mock()
    val mockInvitedUserDoc: DocumentReference = mock()
    val mockInvitationsCollection: CollectionReference = mock()
    val mockInvitationDoc: DocumentReference = mock()

    whenever(mockFirestore.collection("events")).thenReturn(mockEventsCollection)
    whenever(mockEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.get()).thenReturn(Tasks.forResult(mockEventSnapshot))
    whenever(mockEventSnapshot.getString("ownerId")).thenReturn("user123")

    whenever(mockCollectionReference.document("user456")).thenReturn(mockInvitedUserDoc)
    whenever(mockInvitedUserDoc.collection("invitations")).thenReturn(mockInvitationsCollection)
    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockInvitationDoc.set(any())).thenReturn(Tasks.forResult(null))

    // Act
    repository.sendInvitation("event123", "user123", "user456")

    // Assert
    verify(mockInvitationDoc).set(any())
  }

  @Test
  fun testSendInvitationNotOwner() = runTest {
    // Arrange
    val mockEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockEventSnapshot: DocumentSnapshot = mock()

    whenever(mockFirestore.collection("events")).thenReturn(mockEventsCollection)
    whenever(mockEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.get()).thenReturn(Tasks.forResult(mockEventSnapshot))
    whenever(mockEventSnapshot.getString("ownerId")).thenReturn("owner123")

    // Act & Assert
    try {
      repository.sendInvitation("event123", "user456", "user789")
      assert(false) { "Should have thrown exception" }
    } catch (e: IllegalArgumentException) {
      assert(e.message!!.contains("not the owner"))
    }
  }

  @Test
  fun testSendInvitationFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<DocumentSnapshot> = Tasks.forException(exception)

    whenever(mockFirestore.collection("events")).thenReturn(mockEventsCollection)
    whenever(mockEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.sendInvitation("event123", "user123", "user456")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetJoinedEventsFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)
    whenever(mockJoinedEventsCollection.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getJoinedEvents("user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testAddEventToUserFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.set(any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.addEventToUser("event123", "user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  // Username availability tests
  @Test
  fun testCheckUsernameAvailability_returnsTrue_whenUsernameNotExists() = runTest {
    // Arrange
    whenever(mockCollectionReference.whereEqualTo("username", "newuser")).thenReturn(mockQuery)
    whenever(mockQuery.limit(1)).thenReturn(mockQuery)
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(true)

    // Act
    val result = repository.checkUsernameAvailability("newuser")

    // Assert
    assertTrue(result)
    verify(mockCollectionReference).whereEqualTo("username", "newuser")
    verify(mockQuery).limit(1)
  }

  @Test
  fun testCheckUsernameAvailability_returnsFalse_whenUsernameExists() = runTest {
    // Arrange
    whenever(mockCollectionReference.whereEqualTo("username", "johndoe")).thenReturn(mockQuery)
    whenever(mockQuery.limit(1)).thenReturn(mockQuery)
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)

    // Act
    val result = repository.checkUsernameAvailability("johndoe")

    // Assert
    assertFalse(result)
    verify(mockCollectionReference).whereEqualTo("username", "johndoe")
  }

  @Test
  fun testCheckUsernameAvailability_isCaseInsensitive() = runTest {
    // Arrange
    whenever(mockCollectionReference.whereEqualTo("username", "johndoe")).thenReturn(mockQuery)
    whenever(mockQuery.limit(1)).thenReturn(mockQuery)
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockQuery.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)

    // Act
    val result = repository.checkUsernameAvailability("JOHNDOE")

    // Assert
    assertFalse(result)
    verify(mockCollectionReference).whereEqualTo("username", "johndoe")
  }

  @Test
  fun testCheckUsernameAvailabilityFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    whenever(mockCollectionReference.whereEqualTo("username", "newuser")).thenReturn(mockQuery)
    whenever(mockQuery.limit(1)).thenReturn(mockQuery)
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)
    whenever(mockQuery.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.checkUsernameAvailability("newuser")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testFollowOrganizationSuccess() = runTest {
    // Arrange
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockOrgDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.document("org123")).thenReturn(mockOrgDoc)
    whenever(mockOrgDoc.set(any())).thenReturn(mockTask)

    // Act
    repository.followOrganization("user123", "org123")

    // Assert
    verify(mockFollowedOrgsCollection).document("org123")
    val captor = argumentCaptor<Map<String, Any?>>()
    verify(mockOrgDoc).set(captor.capture())
    val followData = captor.firstValue
    assert(followData["organizationId"] == "org123")
    assert(followData.containsKey("followedAt"))
  }

  @Test
  fun testFollowOrganizationFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockOrgDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.document("org123")).thenReturn(mockOrgDoc)
    whenever(mockOrgDoc.set(any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.followOrganization("user123", "org123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testUnfollowOrganizationSuccess() = runTest {
    // Arrange
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockOrgDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.document("org123")).thenReturn(mockOrgDoc)
    whenever(mockOrgDoc.delete()).thenReturn(mockTask)

    // Act
    repository.unfollowOrganization("user123", "org123")

    // Assert
    verify(mockFollowedOrgsCollection).document("org123")
    verify(mockOrgDoc).delete()
  }

  @Test
  fun testUnfollowOrganizationFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockOrgDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.document("org123")).thenReturn(mockOrgDoc)
    whenever(mockOrgDoc.delete()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.unfollowOrganization("user123", "org123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetFollowedOrganizationsSuccess() = runTest {
    // Arrange
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockDoc1: DocumentSnapshot = mock()
    val mockDoc2: DocumentSnapshot = mock()

    whenever(mockDoc1.getString("organizationId")).thenReturn("org1")
    whenever(mockDoc2.getString("organizationId")).thenReturn("org2")

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

    // Act
    val result = repository.getFollowedOrganizations("user123")

    // Assert
    assertEquals(2, result.size)
    assertEquals(listOf("org1", "org2"), result)
  }

  @Test
  fun testGetFollowedOrganizationsEmpty() = runTest {
    // Arrange
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getFollowedOrganizations("user123")

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun testGetFollowedOrganizationsFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)

    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getFollowedOrganizations("user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetFollowedOrganizationsHandlesNullValues() = runTest {
    // Arrange
    val mockFollowedOrgsCollection: CollectionReference = mock()
    val mockDoc1: DocumentSnapshot = mock()
    val mockDoc2: DocumentSnapshot = mock()

    whenever(mockDoc1.getString("organizationId")).thenReturn("org1")
    whenever(mockDoc2.getString("organizationId")).thenReturn(null)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockDocumentReference.collection("followedOrganizations"))
        .thenReturn(mockFollowedOrgsCollection)
    whenever(mockFollowedOrgsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

    // Act
    val result = repository.getFollowedOrganizations("user123")

    // Assert
    assertEquals(1, result.size)
    assertEquals("org1", result[0])
  }

  @Test
  fun testAddFavoriteEventSuccess() = runTest {
    // Arrange
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.set(any())).thenReturn(mockTask)

    // Act
    repository.addFavoriteEvent("user123", "event123")

    // Assert
    verify(mockFavoriteEventsCollection).document("event123")
    val captor = argumentCaptor<Map<String, Any?>>()
    verify(mockEventDoc).set(captor.capture())
    val favoriteData = captor.firstValue
    assert(favoriteData["eventId"] == "event123")
    assert(favoriteData.containsKey("addedAt"))
  }

  @Test
  fun testAddFavoriteEventFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.set(any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.addFavoriteEvent("user123", "event123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testRemoveFavoriteEventSuccess() = runTest {
    // Arrange
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.delete()).thenReturn(mockTask)

    // Act
    repository.removeFavoriteEvent("user123", "event123")

    // Assert
    verify(mockFavoriteEventsCollection).document("event123")
    verify(mockEventDoc).delete()
  }

  @Test
  fun testRemoveFavoriteEventFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockEventDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.document("event123")).thenReturn(mockEventDoc)
    whenever(mockEventDoc.delete()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.removeFavoriteEvent("user123", "event123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetFavoriteEventsSuccess() = runTest {
    // Arrange
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockDoc1: DocumentSnapshot = mock()
    val mockDoc2: DocumentSnapshot = mock()

    whenever(mockDoc1.getString("eventId")).thenReturn("event1")
    whenever(mockDoc2.getString("eventId")).thenReturn("event2")

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1, mockDoc2))

    // Act
    val result = repository.getFavoriteEvents("user123")

    // Assert
    assertEquals(2, result.size)
    assertEquals(listOf("event1", "event2"), result)
  }

  @Test
  fun testGetFavoriteEventsEmpty() = runTest {
    // Arrange
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    // Act
    val result = repository.getFavoriteEvents("user123")

    // Assert
    assertTrue(result.isEmpty())
  }

  @Test
  fun testGetFavoriteEventsFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockFavoriteEventsCollection: CollectionReference = mock()
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)

    whenever(mockDocumentReference.collection("favoriteEvents"))
        .thenReturn(mockFavoriteEventsCollection)
    whenever(mockFavoriteEventsCollection.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getFavoriteEvents("user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testDeclineInvitationSuccess() = runTest {
    // Arrange
    val mockInvitationDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forResult(null)

    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockInvitationDoc.update(anyString(), any())).thenReturn(mockTask)

    // Act
    repository.declineInvitation("event123", "user123")

    // Assert
    verify(mockInvitationDoc).update("status", InvitationStatus.Declined.name)
  }

  @Test
  fun testDeclineInvitationFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockInvitationDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockInvitationDoc.update(anyString(), any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.declineInvitation("event123", "user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testAcceptInvitationFailureWhenInvitationNotExists() = runTest {
    // Arrange
    val mockInvitationDoc: DocumentReference = mock()
    val mockJoinedDoc: DocumentReference = mock()
    val mockGetTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)

    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockJoinedEventsCollection.document("event123")).thenReturn(mockJoinedDoc)
    whenever(mockInvitationDoc.get()).thenReturn(mockGetTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(false)

    // Act & Assert
    try {
      repository.acceptInvitation("event123", "user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: IllegalArgumentException) {
      assert(e.message!!.contains("No invitation"))
    }
  }

  @Test
  fun testAddInvitationToUserFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockInvitationDoc: DocumentReference = mock()
    val mockTask: Task<Void> = Tasks.forException(exception)

    whenever(mockInvitationsCollection.document("event123")).thenReturn(mockInvitationDoc)
    whenever(mockInvitationDoc.set(any())).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.addInvitationToUser("event123", "user123", "user456")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }

  @Test
  fun testGetInvitationsFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<QuerySnapshot> = Tasks.forException(exception)
    whenever(mockInvitationsCollection.get()).thenReturn(mockTask)

    // Act & Assert
    try {
      repository.getInvitations("user123")
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
  }
}

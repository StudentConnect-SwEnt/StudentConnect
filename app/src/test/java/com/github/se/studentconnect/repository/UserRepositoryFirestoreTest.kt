package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class UserRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot

  private lateinit var repository: UserRepositoryFirestore

  private val testUser =
      User(
          userId = "user123",
          email = "test@epfl.ch",
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

    // Assert
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

    // Assert
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

    // Act & Assert
    try {
      repository.getAllUsers()
      assert(false) { "Should have thrown exception" }
    } catch (e: Exception) {
      assert(e == exception)
    }
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
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(eq("joinedEvents"), any())).thenReturn(mockTask)

    // Act
    repository.joinEvent("event123", "user123")

    // Assert
    verify(mockDocumentReference).update(eq("joinedEvents"), any())
  }

  @Test
  fun testJoinEventFailure() = runTest {
    // Arrange
    val exception = Exception("Firestore error")
    val mockTask: Task<Void> = Tasks.forException(exception)
    whenever(mockDocumentReference.update(eq("joinedEvents"), any())).thenReturn(mockTask)

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
    val eventIds = listOf("event1", "event2", "event3")
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)

    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.get("joinedEvents")).thenReturn(eventIds)

    // Act
    val result = repository.getJoinedEvents("user123")

    // Assert
    assert(result == eventIds)
  }

  @Test
  fun testGetJoinedEventsEmpty() = runTest {
    // Arrange
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)

    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.get("joinedEvents")).thenReturn(null)

    // Act
    val result = repository.getJoinedEvents("user123")

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testAddEventToUserSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(eq("joinedEvents"), any())).thenReturn(mockTask)

    // Act
    repository.addEventToUser("event123", "user123")

    // Assert
    verify(mockDocumentReference).update(eq("joinedEvents"), any())
  }

  @Test
  fun testAddInvitationToUserSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(eq("invitations"), any())).thenReturn(mockTask)

    // Act
    repository.addInvitationToUser("event123", "user123")

    // Assert
    verify(mockDocumentReference).update(eq("invitations"), any())
  }

  @Test
  fun testGetInvitationsSuccess() = runTest {
    // Arrange
    val invitations = listOf("event1", "event2")
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)

    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.get("invitations")).thenReturn(invitations)

    // Act
    val result = repository.getInvitations("user123")

    // Assert
    assert(result == invitations)
  }

  @Test
  fun testGetInvitationsEmpty() = runTest {
    // Arrange
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(mockDocumentSnapshot)

    whenever(mockDocumentReference.get()).thenReturn(mockTask)
    whenever(mockDocumentSnapshot.get("invitations")).thenReturn(null)

    // Act
    val result = repository.getInvitations("user123")

    // Assert
    assert(result.isEmpty())
  }

  @Test
  fun testAcceptInvitationSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    // Act
    repository.acceptInvitation("event123", "user123")

    // Assert
    val captor = argumentCaptor<Map<String, Any?>>()
    verify(mockDocumentReference).update(captor.capture())
    val updates = captor.firstValue
    assert(updates.containsKey("invitations"))
    assert(updates.containsKey("joinedEvents"))
  }

  @Test
  fun testLeaveEventSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(eq("joinedEvents"), any())).thenReturn(mockTask)

    // Act
    repository.leaveEvent("event123", "user123")

    // Assert
    verify(mockDocumentReference).update(eq("joinedEvents"), any())
  }

  @Test
  fun testSendInvitationSuccess() = runTest {
    // Arrange
    val mockTask: Task<Void> = Tasks.forResult(null)
    whenever(mockDocumentReference.update(eq("invitations"), any())).thenReturn(mockTask)

    // Act
    repository.sendInvitation("event123", "user456", "user789")

    // Assert
    verify(mockCollectionReference).document("user789")
    verify(mockDocumentReference).update(eq("invitations"), any())
  }
}

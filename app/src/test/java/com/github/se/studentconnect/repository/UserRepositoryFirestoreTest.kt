package com.github.se.studentconnect.repository

import com.github.se.studentconnect.model.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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

  @Mock private lateinit var mockTask: Task<Void>

  @Mock private lateinit var mockDocumentTask: Task<DocumentSnapshot>

  @Mock private lateinit var mockQueryTask: Task<QuerySnapshot>

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
  fun testGetUserByIdSuccess() {
    // Arrange
    whenever(mockDocumentReference.get()).thenReturn(mockDocumentTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(true)
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    var resultUser: User? = null
    var failureCalled = false

    // Mock the task to immediately invoke onSuccess
    whenever(mockDocumentTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
      listener.onSuccess(mockDocumentSnapshot)
      mockDocumentTask
    }
    whenever(mockDocumentTask.addOnFailureListener(any())).thenReturn(mockDocumentTask)

    // Act
    repository.getUserById(
        userId = "user123", onSuccess = { resultUser = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUser != null)
    assert(resultUser?.userId == "user123")
    assert(resultUser?.email == "test@epfl.ch")
    assert(!failureCalled)
    verify(mockCollectionReference).document("user123")
    verify(mockDocumentReference).get()
  }

  @Test
  fun testGetUserByIdNotFound() {
    // Arrange
    whenever(mockDocumentReference.get()).thenReturn(mockDocumentTask)
    whenever(mockDocumentSnapshot.exists()).thenReturn(false)

    var resultUser: User? = null
    var failureCalled = false

    whenever(mockDocumentTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<DocumentSnapshot>>(0)
      listener.onSuccess(mockDocumentSnapshot)
      mockDocumentTask
    }
    whenever(mockDocumentTask.addOnFailureListener(any())).thenReturn(mockDocumentTask)

    // Act
    repository.getUserById(
        userId = "nonexistent",
        onSuccess = { resultUser = it },
        onFailure = { failureCalled = true })

    // Assert
    assert(resultUser == null)
    assert(!failureCalled)
  }

  @Test
  fun testGetUserByIdFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    whenever(mockDocumentReference.get()).thenReturn(mockDocumentTask)

    var resultUser: User? = null
    var failureException: Exception? = null

    whenever(mockDocumentTask.addOnSuccessListener(any())).thenReturn(mockDocumentTask)
    whenever(mockDocumentTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockDocumentTask
    }

    // Act
    repository.getUserById(
        userId = "user123", onSuccess = { resultUser = it }, onFailure = { failureException = it })

    // Assert
    assert(resultUser == null)
    assert(failureException == exception)
  }

  @Test
  fun testGetUserByEmailSuccess() {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereEqualTo("email", "test@epfl.ch")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(false)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    var resultUser: User? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getUserByEmail(
        email = "test@epfl.ch",
        onSuccess = { resultUser = it },
        onFailure = { failureCalled = true })

    // Assert
    assert(resultUser != null)
    assert(resultUser?.email == "test@epfl.ch")
    assert(!failureCalled)
  }

  @Test
  fun testGetUserByEmailNotFound() {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereEqualTo("email", "notfound@epfl.ch"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.isEmpty).thenReturn(true)

    var resultUser: User? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getUserByEmail(
        email = "notfound@epfl.ch",
        onSuccess = { resultUser = it },
        onFailure = { failureCalled = true })

    // Assert
    assert(resultUser == null)
    assert(!failureCalled)
  }

  @Test
  fun testGetUserByEmailFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereEqualTo("email", "test@epfl.ch")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)

    var resultUser: User? = null
    var failureException: Exception? = null

    whenever(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    whenever(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockQueryTask
    }

    // Act
    repository.getUserByEmail(
        email = "test@epfl.ch",
        onSuccess = { resultUser = it },
        onFailure = { failureException = it })

    // Assert
    assert(resultUser == null)
    assert(failureException == exception)
  }

  @Test
  fun testGetAllUsersSuccess() {
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

    whenever(mockCollectionReference.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot, mockDocSnapshot2))

    var resultUsers: List<User>? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getAllUsers(onSuccess = { resultUsers = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUsers != null)
    assert(resultUsers?.size == 2)
    assert(resultUsers?.get(0)?.userId == "user123")
    assert(resultUsers?.get(1)?.userId == "user456")
    assert(!failureCalled)
  }

  @Test
  fun testGetAllUsersEmpty() {
    // Arrange
    whenever(mockCollectionReference.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    var resultUsers: List<User>? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getAllUsers(onSuccess = { resultUsers = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUsers != null)
    assert(resultUsers?.isEmpty() == true)
    assert(!failureCalled)
  }

  @Test
  fun testGetAllUsersFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    whenever(mockCollectionReference.get()).thenReturn(mockQueryTask)

    var resultUsers: List<User>? = null
    var failureException: Exception? = null

    whenever(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    whenever(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockQueryTask
    }

    // Act
    repository.getAllUsers(onSuccess = { resultUsers = it }, onFailure = { failureException = it })

    // Assert
    assert(resultUsers == null)
    assert(failureException == exception)
  }

  @Test
  fun testSaveUserSuccess() {
    // Arrange
    whenever(mockDocumentReference.set(any())).thenReturn(mockTask)

    var successCalled = false
    var failureCalled = false

    whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      mockTask
    }
    whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Act
    repository.saveUser(
        user = testUser, onSuccess = { successCalled = true }, onFailure = { failureCalled = true })

    // Assert
    assert(successCalled)
    assert(!failureCalled)
    verify(mockDocumentReference).set(testUser.toMap())
  }

  @Test
  fun testSaveUserFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    whenever(mockDocumentReference.set(any())).thenReturn(mockTask)

    var successCalled = false
    var failureException: Exception? = null

    whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockTask
    }

    // Act
    repository.saveUser(
        user = testUser,
        onSuccess = { successCalled = true },
        onFailure = { failureException = it })

    // Assert
    assert(!successCalled)
    assert(failureException == exception)
  }

  @Test
  fun testUpdateUserSuccess() {
    // Arrange
    val updates = mapOf("firstName" to "Jack", "lastName" to "Brown")
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    var successCalled = false
    var failureCalled = false

    whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      mockTask
    }
    whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Act
    repository.updateUser(
        userId = "user123",
        updates = updates,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Assert
    assert(successCalled)
    assert(!failureCalled)
    verify(mockDocumentReference).update(any<Map<String, Any?>>())
  }

  @Test
  fun testUpdateUserAddsTimestamp() {
    // Arrange
    val updates = mapOf("firstName" to "Jack")
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      mockTask
    }
    whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Act
    repository.updateUser(userId = "user123", updates = updates, onSuccess = {}, onFailure = {})

    // Assert - Verify that updatedAt was added to the updates
    val captor = argumentCaptor<Map<String, Any?>>()
    verify(mockDocumentReference).update(captor.capture())
    val capturedUpdates = captor.firstValue
    assert(capturedUpdates.containsKey("updatedAt"))
    assert(capturedUpdates["firstName"] == "Jack")
  }

  @Test
  fun testUpdateUserFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    val updates = mapOf("firstName" to "Jack")
    whenever(mockDocumentReference.update(any<Map<String, Any?>>())).thenReturn(mockTask)

    var successCalled = false
    var failureException: Exception? = null

    whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockTask
    }

    // Act
    repository.updateUser(
        userId = "user123",
        updates = updates,
        onSuccess = { successCalled = true },
        onFailure = { failureException = it })

    // Assert
    assert(!successCalled)
    assert(failureException == exception)
  }

  @Test
  fun testDeleteUserSuccess() {
    // Arrange
    whenever(mockDocumentReference.delete()).thenReturn(mockTask)

    var successCalled = false
    var failureCalled = false

    whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
      listener.onSuccess(null)
      mockTask
    }
    whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

    // Act
    repository.deleteUser(
        userId = "user123",
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Assert
    assert(successCalled)
    assert(!failureCalled)
    verify(mockDocumentReference).delete()
  }

  @Test
  fun testDeleteUserFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    whenever(mockDocumentReference.delete()).thenReturn(mockTask)

    var successCalled = false
    var failureException: Exception? = null

    whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
    whenever(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockTask
    }

    // Act
    repository.deleteUser(
        userId = "user123",
        onSuccess = { successCalled = true },
        onFailure = { failureException = it })

    // Assert
    assert(!successCalled)
    assert(failureException == exception)
  }

  @Test
  fun testGetUsersByUniversitySuccess() {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereEqualTo("university", "EPFL")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    var resultUsers: List<User>? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getUsersByUniversity(
        university = "EPFL", onSuccess = { resultUsers = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUsers != null)
    assert(resultUsers?.size == 1)
    assert(resultUsers?.get(0)?.university == "EPFL")
    assert(!failureCalled)
  }

  @Test
  fun testGetUsersByUniversityEmpty() {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereEqualTo("university", "ETHZ")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    var resultUsers: List<User>? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getUsersByUniversity(
        university = "ETHZ", onSuccess = { resultUsers = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUsers != null)
    assert(resultUsers?.isEmpty() == true)
    assert(!failureCalled)
  }

  @Test
  fun testGetUsersByUniversityFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereEqualTo("university", "EPFL")).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)

    var resultUsers: List<User>? = null
    var failureException: Exception? = null

    whenever(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    whenever(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockQueryTask
    }

    // Act
    repository.getUsersByUniversity(
        university = "EPFL",
        onSuccess = { resultUsers = it },
        onFailure = { failureException = it })

    // Assert
    assert(resultUsers == null)
    assert(failureException == exception)
  }

  @Test
  fun testGetUsersByHobbySuccess() {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereArrayContains("hobbies", "Football"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())

    var resultUsers: List<User>? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getUsersByHobby(
        hobby = "Football", onSuccess = { resultUsers = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUsers != null)
    assert(resultUsers?.size == 1)
    assert(resultUsers?.get(0)?.hobbies?.contains("Football") == true)
    assert(!failureCalled)
  }

  @Test
  fun testGetUsersByHobbyEmpty() {
    // Arrange
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereArrayContains("hobbies", "Swimming"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    var resultUsers: List<User>? = null
    var failureCalled = false

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getUsersByHobby(
        hobby = "Swimming", onSuccess = { resultUsers = it }, onFailure = { failureCalled = true })

    // Assert
    assert(resultUsers != null)
    assert(resultUsers?.isEmpty() == true)
    assert(!failureCalled)
  }

  @Test
  fun testGetUsersByHobbyFailure() {
    // Arrange
    val exception = Exception("Firestore error")
    val mockQuery: com.google.firebase.firestore.Query = mock()
    whenever(mockCollectionReference.whereArrayContains("hobbies", "Football"))
        .thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(mockQueryTask)

    var resultUsers: List<User>? = null
    var failureException: Exception? = null

    whenever(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask)
    whenever(mockQueryTask.addOnFailureListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnFailureListener>(0)
      listener.onFailure(exception)
      mockQueryTask
    }

    // Act
    repository.getUsersByHobby(
        hobby = "Football", onSuccess = { resultUsers = it }, onFailure = { failureException = it })

    // Assert
    assert(resultUsers == null)
    assert(failureException == exception)
  }

  @Test
  fun testGetNewuserId() {
    // Arrange
    whenever(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.id).thenReturn("generated_userId_123")

    // Act
    val newuserId = repository.getNewUid()

    // Assert
    assert(newuserId == "generated_userId_123")
    verify(mockCollectionReference).document()
  }

  @Test
  fun testGetAllUsersWithInvalidData() {
    // Arrange
    val invalidDocSnapshot: DocumentSnapshot = mock()
    whenever(mockDocumentSnapshot.data).thenReturn(testUser.toMap())
    whenever(invalidDocSnapshot.data).thenReturn(mapOf("userId" to "")) // Invalid data

    whenever(mockCollectionReference.get()).thenReturn(mockQueryTask)
    whenever(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, invalidDocSnapshot))

    var resultUsers: List<User>? = null

    whenever(mockQueryTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
      listener.onSuccess(mockQuerySnapshot)
      mockQueryTask
    }
    whenever(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask)

    // Act
    repository.getAllUsers(onSuccess = { resultUsers = it }, onFailure = {})

    // Assert - Only valid user should be in the list
    assert(resultUsers != null)
    assert(resultUsers?.size == 1)
    assert(resultUsers?.get(0)?.userId == "user123")
  }
}

package com.github.se.studentconnect.model.chat

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockEventsCollection: CollectionReference
  @Mock private lateinit var mockEventDocument: DocumentReference
  @Mock private lateinit var mockMessagesCollection: CollectionReference
  @Mock private lateinit var mockMessageDocument: DocumentReference
  @Mock private lateinit var mockTypingCollection: CollectionReference
  @Mock private lateinit var mockTypingDocument: DocumentReference
  @Mock private lateinit var mockTask: Task<Void>
  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var repository: ChatRepositoryFirestore
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Setup default mock chain for Firestore
    `when`(mockFirestore.collection("events")).thenReturn(mockEventsCollection)
    `when`(mockEventsCollection.document(any())).thenReturn(mockEventDocument)
    `when`(mockEventsCollection.document()).thenReturn(mockEventDocument)
    `when`(mockEventDocument.collection("messages")).thenReturn(mockMessagesCollection)
    `when`(mockEventDocument.collection("typing")).thenReturn(mockTypingCollection)
    `when`(mockMessagesCollection.document(any())).thenReturn(mockMessageDocument)
    `when`(mockMessagesCollection.document()).thenReturn(mockMessageDocument)
    `when`(mockMessageDocument.id).thenReturn("generated-message-id")
    `when`(mockTypingCollection.document(any())).thenReturn(mockTypingDocument)

    repository = ChatRepositoryFirestore(mockFirestore)
  }

  @Test
  fun sendMessage_callsSuccessCallback() {
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!")

    `when`(mockMessageDocument.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false
    var failureCalled = false

    repository.sendMessage(
        message = message,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    assert(successCalled) { "Success callback was not called" }
    assert(!failureCalled) { "Failure callback should not be called" }
    verify(mockMessageDocument).set(any())
  }

  @Test
  fun sendMessage_callsFailureCallback() {
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!")

    val exception = Exception("Test exception")

    `when`(mockMessageDocument.set(any())).thenReturn(mockTask)

    doAnswer { mockTask }.`when`(mockTask).addOnSuccessListener(any())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnFailureListener>(0)
          listener.onFailure(exception)
          mockTask
        }
        .`when`(mockTask)
        .addOnFailureListener(any())

    var successCalled = false
    var failureCalled = false
    var receivedException: Exception? = null

    repository.sendMessage(
        message = message,
        onSuccess = { successCalled = true },
        onFailure = {
          failureCalled = true
          receivedException = it
        })

    assert(!successCalled) { "Success callback should not be called" }
    assert(failureCalled) { "Failure callback was not called" }
    assert(receivedException == exception) { "Exception not passed correctly" }
  }

  @Test
  fun sendMessage_setsMessageDataCorrectly() {
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!")

    `when`(mockMessageDocument.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    repository.sendMessage(message = message, onSuccess = {}, onFailure = {})

    verify(mockMessageDocument).set(any())
  }

  @Test
  fun updateTypingStatus_callsSuccessCallback() {
    val typingStatus =
        TypingStatus(
            userId = "user-123", userName = "Jane Doe", eventId = "event-456", isTyping = true)

    `when`(mockTypingDocument.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false
    var failureCalled = false

    repository.updateTypingStatus(
        typingStatus = typingStatus,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    assert(successCalled) { "Success callback was not called" }
    assert(!failureCalled) { "Failure callback should not be called" }
    verify(mockTypingDocument).set(any())
  }

  @Test
  fun updateTypingStatus_callsFailureCallback() {
    val typingStatus =
        TypingStatus(
            userId = "user-123", userName = "Jane Doe", eventId = "event-456", isTyping = true)

    val exception = Exception("Test exception")

    `when`(mockTypingDocument.set(any())).thenReturn(mockTask)

    doAnswer { mockTask }.`when`(mockTask).addOnSuccessListener(any())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnFailureListener>(0)
          listener.onFailure(exception)
          mockTask
        }
        .`when`(mockTask)
        .addOnFailureListener(any())

    var successCalled = false
    var failureCalled = false
    var receivedException: Exception? = null

    repository.updateTypingStatus(
        typingStatus = typingStatus,
        onSuccess = { successCalled = true },
        onFailure = {
          failureCalled = true
          receivedException = it
        })

    assert(!successCalled) { "Success callback should not be called" }
    assert(failureCalled) { "Failure callback was not called" }
    assert(receivedException == exception) { "Exception not passed correctly" }
  }

  @Test
  fun updateTypingStatus_setsTypingDataCorrectly() {
    val typingStatus =
        TypingStatus(
            userId = "user-123", userName = "Jane Doe", eventId = "event-456", isTyping = false)

    `when`(mockTypingDocument.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    repository.updateTypingStatus(typingStatus = typingStatus, onSuccess = {}, onFailure = {})

    verify(mockTypingDocument).set(any())
  }

  @Test
  fun getNewMessageId_returnsNonEmptyId() {
    val messageId = repository.getNewMessageId()

    assert(messageId.isNotEmpty())
    assert(messageId == "generated-message-id")
  }

  @Test
  fun sendMessage_usesCorrectCollectionPath() {
    val message =
        ChatMessage(
            messageId = "msg-123",
            eventId = "event-456",
            senderId = "user-789",
            senderName = "John Doe",
            content = "Hello World!")

    `when`(mockMessageDocument.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    repository.sendMessage(message = message, onSuccess = {}, onFailure = {})

    verify(mockEventsCollection).document("event-456")
    verify(mockEventDocument).collection("messages")
    verify(mockMessagesCollection).document("msg-123")
  }

  @Test
  fun updateTypingStatus_usesCorrectCollectionPath() {
    val typingStatus =
        TypingStatus(
            userId = "user-123", userName = "Jane Doe", eventId = "event-456", isTyping = true)

    `when`(mockTypingDocument.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    repository.updateTypingStatus(typingStatus = typingStatus, onSuccess = {}, onFailure = {})

    verify(mockEventsCollection).document("event-456")
    verify(mockEventDocument).collection("typing")
    verify(mockTypingCollection).document("user-123")
  }

  @Test
  fun observeMessages_emitsEmptyListOnError() = runTest {
    val eventId = "event-123"
    val exception = Exception("Firestore error")

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockMessagesCollection)

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(
              null,
              FirebaseFirestoreException("Error", FirebaseFirestoreException.Code.UNAVAILABLE))
          mockListenerRegistration
        }
        .`when`(mockMessagesCollection)
        .addSnapshotListener(any())

    val messages = mutableListOf<List<ChatMessage>>()
    val job = launch { repository.observeMessages(eventId).collect { messages.add(it) } }

    advanceUntilIdle()

    assert(messages.isNotEmpty())
    assert(messages.first().isEmpty())

    job.cancel()
  }

  @Test
  fun observeMessages_emitsEmptyListOnNullSnapshot() = runTest {
    val eventId = "event-123"

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockMessagesCollection)

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(null, null)
          mockListenerRegistration
        }
        .`when`(mockMessagesCollection)
        .addSnapshotListener(any())

    val messages = mutableListOf<List<ChatMessage>>()
    val job = launch { repository.observeMessages(eventId).collect { messages.add(it) } }

    advanceUntilIdle()

    assert(messages.isNotEmpty())
    assert(messages.first().isEmpty())

    job.cancel()
  }

  @Test
  fun observeMessages_emitsMessagesOnSuccess() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    val messageData =
        mapOf(
            "messageId" to "msg-1",
            "eventId" to eventId,
            "senderId" to "user-1",
            "senderName" to "John Doe",
            "content" to "Hello",
            "timestamp" to com.google.firebase.Timestamp.now())

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockMessagesCollection)
    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(messageData)
    `when`(mockDocument.id).thenReturn("msg-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockMessagesCollection)
        .addSnapshotListener(any())

    val messages = mutableListOf<List<ChatMessage>>()
    val job = launch { repository.observeMessages(eventId).collect { messages.add(it) } }

    advanceUntilIdle()

    assert(messages.isNotEmpty())
    assert(messages.first().isNotEmpty())
    assert(messages.first()[0].messageId == "msg-1")

    job.cancel()
  }

  @Test
  fun observeMessages_filtersInvalidMessages() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockValidDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)
    val mockInvalidDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    val validMessageData =
        mapOf(
            "messageId" to "msg-1",
            "eventId" to eventId,
            "senderId" to "user-1",
            "senderName" to "John Doe",
            "content" to "Hello",
            "timestamp" to com.google.firebase.Timestamp.now())

    val invalidMessageData = mapOf("invalid" to "data")

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockMessagesCollection)
    `when`(mockSnapshot.documents).thenReturn(listOf(mockValidDocument, mockInvalidDocument))
    `when`(mockValidDocument.data).thenReturn(validMessageData)
    `when`(mockValidDocument.id).thenReturn("msg-1")
    `when`(mockInvalidDocument.data).thenReturn(invalidMessageData)
    `when`(mockInvalidDocument.id).thenReturn("invalid-msg")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockMessagesCollection)
        .addSnapshotListener(any())

    val messages = mutableListOf<List<ChatMessage>>()
    val job = launch { repository.observeMessages(eventId).collect { messages.add(it) } }

    advanceUntilIdle()

    assert(messages.isNotEmpty())
    assert(messages.first().size == 1) // Only valid message
    assert(messages.first()[0].messageId == "msg-1")

    job.cancel()
  }

  @Test
  fun observeMessages_handlesNullDocumentData() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockMessagesCollection)
    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(null)
    `when`(mockDocument.id).thenReturn("msg-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockMessagesCollection)
        .addSnapshotListener(any())

    val messages = mutableListOf<List<ChatMessage>>()
    val job = launch { repository.observeMessages(eventId).collect { messages.add(it) } }

    advanceUntilIdle()

    assert(messages.isNotEmpty())
    assert(messages.first().isEmpty()) // Null data means empty list

    job.cancel()
  }

  @Test
  fun observeTypingUsers_emitsEmptyListOnError() = runTest {
    val eventId = "event-123"

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(
              null,
              FirebaseFirestoreException("Error", FirebaseFirestoreException.Code.UNAVAILABLE))
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isEmpty())

    job.cancel()
  }

  @Test
  fun observeTypingUsers_emitsEmptyListOnNullSnapshot() = runTest {
    val eventId = "event-123"

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(null, null)
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isEmpty())

    job.cancel()
  }

  @Test
  fun observeTypingUsers_emitsTypingUsersOnSuccess() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    val typingData =
        mapOf(
            "userId" to "user-1",
            "userName" to "John Doe",
            "eventId" to eventId,
            "isTyping" to true,
            "lastUpdate" to com.google.firebase.Timestamp.now())

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(typingData)
    `when`(mockDocument.id).thenReturn("user-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isNotEmpty())
    assert(typingUsers.first()[0].userId == "user-1")

    job.cancel()
  }

  @Test
  fun observeTypingUsers_filtersExpiredTypingStatus() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    // Create expired typing status (more than 5 seconds ago)
    val expiredTimestamp =
        com.google.firebase.Timestamp(com.google.firebase.Timestamp.now().seconds - 10, 0)
    val typingData =
        mapOf(
            "userId" to "user-1",
            "userName" to "John Doe",
            "eventId" to eventId,
            "isTyping" to true,
            "lastUpdate" to expiredTimestamp)

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(typingData)
    `when`(mockDocument.id).thenReturn("user-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isEmpty()) // Expired typing should be filtered out

    job.cancel()
  }

  @Test
  fun observeTypingUsers_filtersNotTypingStatus() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    val typingData =
        mapOf(
            "userId" to "user-1",
            "userName" to "John Doe",
            "eventId" to eventId,
            "isTyping" to false, // Not typing
            "lastUpdate" to com.google.firebase.Timestamp.now())

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(typingData)
    `when`(mockDocument.id).thenReturn("user-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isEmpty()) // Not typing should be filtered out

    job.cancel()
  }

  @Test
  fun observeTypingUsers_handlesInvalidTypingData() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    val invalidTypingData = mapOf("invalid" to "data")

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(invalidTypingData)
    `when`(mockDocument.id).thenReturn("user-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isEmpty())

    job.cancel()
  }

  @Test
  fun observeTypingUsers_handlesNullDocumentData() = runTest {
    val eventId = "event-123"
    val mockSnapshot = mock(com.google.firebase.firestore.QuerySnapshot::class.java)
    val mockDocument = mock(com.google.firebase.firestore.DocumentSnapshot::class.java)

    `when`(mockSnapshot.documents).thenReturn(listOf(mockDocument))
    `when`(mockDocument.data).thenReturn(null)
    `when`(mockDocument.id).thenReturn("user-1")

    doAnswer { invocation ->
          val listener =
              invocation.getArgument<
                  com.google.firebase.firestore.EventListener<
                      com.google.firebase.firestore.QuerySnapshot>>(
                  0)
          listener.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }
        .`when`(mockTypingCollection)
        .addSnapshotListener(any())

    val typingUsers = mutableListOf<List<TypingStatus>>()
    val job = launch { repository.observeTypingUsers(eventId).collect { typingUsers.add(it) } }

    advanceUntilIdle()

    assert(typingUsers.isNotEmpty())
    assert(typingUsers.first().isEmpty())

    job.cancel()
  }
}

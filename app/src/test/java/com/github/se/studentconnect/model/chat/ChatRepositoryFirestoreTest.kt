package com.github.se.studentconnect.model.chat

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
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
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockTask: Task<Void>
  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var repository: ChatRepositoryFirestore

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Setup default mock chain for Firestore
    `when`(mockFirestore.collection("events")).thenReturn(mockEventsCollection)
    `when`(mockEventsCollection.document(any())).thenReturn(mockEventDocument)
    `when`(mockEventDocument.collection("messages")).thenReturn(mockMessagesCollection)
    `when`(mockEventDocument.collection("typing")).thenReturn(mockTypingCollection)
    `when`(mockMessagesCollection.document(any())).thenReturn(mockMessageDocument)
    `when`(mockMessagesCollection.document()).thenReturn(mockMessageDocument)
    `when`(mockTypingCollection.document(any())).thenReturn(mockTypingDocument)

    repository = ChatRepositoryFirestore(mockFirestore)
  }

  @Test
  fun observeMessages_emitsMessagesOnSnapshot() = runTest {
    val eventId = "event-123"
    val timestamp = Timestamp.now()

    val messageData =
        mapOf(
            "messageId" to "msg-1",
            "eventId" to eventId,
            "senderId" to "user-1",
            "senderName" to "John Doe",
            "content" to "Hello!",
            "timestamp" to timestamp)

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)
    `when`(mockDocumentSnapshot.data).thenReturn(messageData)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockQuery.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeMessages(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate snapshot event
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    listener.onEvent(mockQuerySnapshot, null)

    // Get first emission
    val messages = flow.first()

    assert(messages.size == 1)
    assert(messages[0].messageId == "msg-1")
    assert(messages[0].content == "Hello!")

    job.cancel()
  }

  @Test
  fun observeMessages_emitsEmptyListOnError() = runTest {
    val eventId = "event-123"

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockQuery.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeMessages(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate error
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    val mockException = mock(FirebaseFirestoreException::class.java)
    listener.onEvent(null, mockException)

    // Get first emission
    val messages = flow.first()

    assert(messages.isEmpty())

    job.cancel()
  }

  @Test
  fun observeMessages_emitsEmptyListOnNullSnapshot() = runTest {
    val eventId = "event-123"

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockQuery.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeMessages(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate null snapshot
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    listener.onEvent(null, null)

    // Get first emission
    val messages = flow.first()

    assert(messages.isEmpty())

    job.cancel()
  }

  @Test
  fun observeMessages_skipsMalformedMessages() = runTest {
    val eventId = "event-123"

    val validMessageData =
        mapOf(
            "messageId" to "msg-1",
            "eventId" to eventId,
            "senderId" to "user-1",
            "senderName" to "John Doe",
            "content" to "Hello!",
            "timestamp" to Timestamp.now())

    val invalidMessageData = mapOf("messageId" to "msg-2") // Missing required fields

    val mockValidSnapshot = mock(DocumentSnapshot::class.java)
    val mockInvalidSnapshot = mock(DocumentSnapshot::class.java)

    `when`(mockValidSnapshot.data).thenReturn(validMessageData)
    `when`(mockInvalidSnapshot.data).thenReturn(invalidMessageData)

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockValidSnapshot, mockInvalidSnapshot))

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockQuery.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeMessages(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate snapshot event
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    listener.onEvent(mockQuerySnapshot, null)

    // Get first emission
    val messages = flow.first()

    // Should only have valid message
    assert(messages.size == 1)
    assert(messages[0].messageId == "msg-1")

    job.cancel()
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
  fun observeTypingUsers_emitsTypingUsersOnSnapshot() = runTest {
    val eventId = "event-123"
    val recentTimestamp = Timestamp.now()

    val typingData =
        mapOf(
            "userId" to "user-1",
            "userName" to "Jane Doe",
            "eventId" to eventId,
            "isTyping" to true,
            "lastUpdate" to recentTimestamp)

    `when`(mockDocumentSnapshot.data).thenReturn(typingData)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockTypingCollection.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeTypingUsers(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate snapshot event
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    listener.onEvent(mockQuerySnapshot, null)

    // Get first emission
    val typingUsers = flow.first()

    assert(typingUsers.size == 1)
    assert(typingUsers[0].userId == "user-1")
    assert(typingUsers[0].isTyping)

    job.cancel()
  }

  @Test
  fun observeTypingUsers_filtersOutExpiredTypingStatus() = runTest {
    val eventId = "event-123"
    val oldTimestamp = Timestamp(Timestamp.now().seconds - 10, 0) // 10 seconds ago

    val typingData =
        mapOf(
            "userId" to "user-1",
            "userName" to "Jane Doe",
            "eventId" to eventId,
            "isTyping" to true,
            "lastUpdate" to oldTimestamp)

    `when`(mockDocumentSnapshot.data).thenReturn(typingData)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockTypingCollection.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeTypingUsers(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate snapshot event
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    listener.onEvent(mockQuerySnapshot, null)

    // Get first emission
    val typingUsers = flow.first()

    // Should be filtered out due to timeout
    assert(typingUsers.isEmpty())

    job.cancel()
  }

  @Test
  fun observeTypingUsers_filtersOutNotTypingUsers() = runTest {
    val eventId = "event-123"
    val recentTimestamp = Timestamp.now()

    val typingData =
        mapOf(
            "userId" to "user-1",
            "userName" to "Jane Doe",
            "eventId" to eventId,
            "isTyping" to false, // Not typing
            "lastUpdate" to recentTimestamp)

    `when`(mockDocumentSnapshot.data).thenReturn(typingData)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockTypingCollection.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeTypingUsers(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate snapshot event
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    listener.onEvent(mockQuerySnapshot, null)

    // Get first emission
    val typingUsers = flow.first()

    // Should be filtered out because isTyping is false
    assert(typingUsers.isEmpty())

    job.cancel()
  }

  @Test
  fun observeTypingUsers_emitsEmptyListOnError() = runTest {
    val eventId = "event-123"

    val listenerCaptor = ArgumentCaptor.forClass(EventListener::class.java)
    `when`(mockTypingCollection.addSnapshotListener(listenerCaptor.capture()))
        .thenReturn(mockListenerRegistration)

    val flow = repository.observeTypingUsers(eventId)

    // Collect flow in background
    val job = kotlinx.coroutines.launch { flow.collect {} }

    // Simulate error
    @Suppress("UNCHECKED_CAST") val listener = listenerCaptor.value as EventListener<QuerySnapshot>
    val mockException = mock(FirebaseFirestoreException::class.java)
    listener.onEvent(null, mockException)

    // Get first emission
    val typingUsers = flow.first()

    assert(typingUsers.isEmpty())

    job.cancel()
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
  fun getNewMessageId_returnsNonEmptyId() {
    `when`(mockMessageDocument.id).thenReturn("generated-message-id")

    val messageId = repository.getNewMessageId()

    assert(messageId.isNotEmpty())
    assert(messageId == "generated-message-id")
  }

  @Test
  fun observeMessages_removesListenerOnClose() = runTest {
    val eventId = "event-123"

    `when`(mockMessagesCollection.orderBy("timestamp", Query.Direction.ASCENDING))
        .thenReturn(mockQuery)
    `when`(mockQuery.addSnapshotListener(any())).thenReturn(mockListenerRegistration)

    val flow = repository.observeMessages(eventId)

    // Collect and immediately cancel
    val job = kotlinx.coroutines.launch { flow.collect {} }
    job.cancel()

    // Give time for cleanup
    kotlinx.coroutines.delay(100)

    // Verify listener was removed
    verify(mockListenerRegistration).remove()
  }

  @Test
  fun observeTypingUsers_removesListenerOnClose() = runTest {
    val eventId = "event-123"

    `when`(mockTypingCollection.addSnapshotListener(any())).thenReturn(mockListenerRegistration)

    val flow = repository.observeTypingUsers(eventId)

    // Collect and immediately cancel
    val job = kotlinx.coroutines.launch { flow.collect {} }
    job.cancel()

    // Give time for cleanup
    kotlinx.coroutines.delay(100)

    // Verify listener was removed
    verify(mockListenerRegistration).remove()
  }
}

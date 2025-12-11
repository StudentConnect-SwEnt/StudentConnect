package com.github.se.studentconnect.model.chat

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
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

  private lateinit var repository: ChatRepositoryFirestore

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
}

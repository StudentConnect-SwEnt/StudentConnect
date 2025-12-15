package com.github.se.studentconnect.model.notification

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class NotificationRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockWriteBatch: WriteBatch
  @Mock private lateinit var mockTask: Task<Void>
  @Mock private lateinit var mockQueryTask: Task<QuerySnapshot>

  private lateinit var repository: NotificationRepositoryFirestore

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    `when`(mockFirestore.collection("notifications")).thenReturn(mockCollectionReference)
    repository = NotificationRepositoryFirestore(mockFirestore)
  }

  @Test
  fun getNotifications_callsSuccessCallback() {
    val userId = "user-1"
    val timestamp = Timestamp.now()

    val notificationData =
        mapOf(
            "id" to "notif-1",
            "userId" to userId,
            "type" to "FRIEND_REQUEST",
            "fromUserId" to "user-2",
            "fromUserName" to "John Doe",
            "timestamp" to timestamp,
            "isRead" to false)

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.data).thenReturn(notificationData)

    // Mock Task to call success listener immediately
    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    var successCalled = false
    var resultNotifications: List<Notification>? = null

    repository.getNotifications(
        userId,
        onSuccess = {
          successCalled = true
          resultNotifications = it
        },
        onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    assert(resultNotifications != null) { "Result notifications is null" }
    assert(resultNotifications!!.size == 1) { "Expected 1 notification" }
    assert(resultNotifications!![0] is Notification.FriendRequest) {
      "Expected FriendRequest notification"
    }
  }

  @Test
  fun getUnreadNotifications_filtersCorrectly() {
    val userId = "user-1"

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.whereEqualTo("isRead", false)).thenReturn(mockQuery)
    `when`(mockQuery.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    var successCalled = false

    repository.getUnreadNotifications(userId, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
  }

  @Test
  fun getNotifications_withEventStarting_returnsCorrectType() {
    val userId = "user-1"
    val timestamp = Timestamp.now()

    val notificationData =
        mapOf(
            "id" to "notif-2",
            "userId" to userId,
            "type" to "EVENT_STARTING",
            "eventId" to "event-1",
            "eventTitle" to "Test Event",
            "eventStart" to timestamp,
            "timestamp" to timestamp,
            "isRead" to true)

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.data).thenReturn(notificationData)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    var resultNotifications: List<Notification>? = null

    repository.getNotifications(userId, onSuccess = { resultNotifications = it }, onFailure = {})

    assert(resultNotifications != null) { "Result notifications is null" }
    assert(resultNotifications!!.size == 1) { "Expected 1 notification" }
    assert(resultNotifications!![0] is Notification.EventStarting) {
      "Expected EventStarting notification"
    }
    val eventNotif = resultNotifications!![0] as Notification.EventStarting
    assert(eventNotif.eventTitle == "Test Event") { "Event title mismatch" }
  }

  @Test
  fun createNotification_setsDocumentCorrectly() {
    val notification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    `when`(mockCollectionReference.document("notif-1")).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.createNotification(
        notification, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun createNotification_generatesIdIfEmpty() {
    val notification =
        Notification.FriendRequest(
            id = "",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.id).thenReturn("generated-id")
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.createNotification(
        notification, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
  }

  @Test
  fun createNotification_eventStarting_setsCorrectly() {
    val notification =
        Notification.EventStarting(
            id = "event-notif-1",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Basketball Game",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    `when`(mockCollectionReference.document("event-notif-1")).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.createNotification(
        notification, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun markAsRead_updatesDocument() {
    val notificationId = "notif-1"

    `when`(mockCollectionReference.document(notificationId)).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.update("isRead", true)).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.markAsRead(notificationId, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    verify(mockDocumentReference).update("isRead", true)
  }

  @Test
  fun markAsRead_handlesFailure() {
    val notificationId = "notif-1"
    val exception = Exception("Test exception")

    `when`(mockCollectionReference.document(notificationId)).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.update("isRead", true)).thenReturn(mockTask)

    doAnswer { mockTask }.`when`(mockTask).addOnSuccessListener(any())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnFailureListener>(0)
          listener.onFailure(exception)
          mockTask
        }
        .`when`(mockTask)
        .addOnFailureListener(any())

    var failureCalled = false

    repository.markAsRead(notificationId, onSuccess = {}, onFailure = { failureCalled = true })

    assert(failureCalled) { "Failure callback was not called" }
  }

  @Test
  fun markAllAsRead_updatesBatch() {
    val userId = "user-1"

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.whereEqualTo("isRead", false)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.reference).thenReturn(mockDocumentReference)
    `when`(mockFirestore.batch()).thenReturn(mockWriteBatch)
    `when`(mockWriteBatch.update(any(DocumentReference::class.java), anyString(), any()))
        .thenReturn(mockWriteBatch)
    `when`(mockWriteBatch.commit()).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.markAllAsRead(userId, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
  }

  @Test
  fun deleteNotification_deletesDocument() {
    val notificationId = "notif-1"

    `when`(mockCollectionReference.document(notificationId)).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.delete()).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.deleteNotification(
        notificationId, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    verify(mockDocumentReference).delete()
  }

  @Test
  fun deleteAllNotifications_deletesBatch() {
    val userId = "user-1"

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.reference).thenReturn(mockDocumentReference)
    `when`(mockFirestore.batch()).thenReturn(mockWriteBatch)
    `when`(mockWriteBatch.delete(any(DocumentReference::class.java))).thenReturn(mockWriteBatch)
    `when`(mockWriteBatch.commit()).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.deleteAllNotifications(userId, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
  }

  @Test
  fun getNotifications_handlesFailure() {
    val userId = "user-1"
    val exception = Exception("Test exception")

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnSuccessListener(any())

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnFailureListener>(0)
          listener.onFailure(exception)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnFailureListener(any())

    var failureCalled = false

    repository.getNotifications(userId, onSuccess = {}, onFailure = { failureCalled = true })

    assert(failureCalled) { "Failure callback was not called" }
  }

  @Test
  fun getNotifications_skipsMalformedNotifications() {
    val userId = "user-1"

    val malformedData = mapOf("id" to "bad", "type" to "UNKNOWN")

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.data).thenReturn(malformedData)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    var resultNotifications: List<Notification>? = null

    repository.getNotifications(userId, onSuccess = { resultNotifications = it }, onFailure = {})

    assert(resultNotifications != null) { "Result notifications is null" }
    assert(resultNotifications!!.isEmpty()) {
      "Malformed notification should be skipped"
    } // Malformed notification should be skipped
  }

  // EventInvitation Tests
  @Test
  fun createNotification_eventInvitation_setsCorrectly() {
    val notification =
        Notification.EventInvitation(
            id = "invite-notif-1",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Private Party",
            invitedBy = "user-2",
            invitedByName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    `when`(mockCollectionReference.document("invite-notif-1")).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.createNotification(
        notification, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    verify(mockDocumentReference).set(any())
  }

  @Test
  fun createNotification_eventInvitation_generatesIdWhenEmpty() {
    val notification =
        Notification.EventInvitation(
            id = "",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Birthday Party",
            invitedBy = "user-2",
            invitedByName = "Bob Johnson",
            timestamp = null,
            isRead = false)

    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.id).thenReturn("generated-invite-id")
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
    `when`(mockDocumentReference.set(any())).thenReturn(mockTask)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<Void>>(0)
          listener.onSuccess(null)
          mockTask
        }
        .`when`(mockTask)
        .addOnSuccessListener(any())

    doAnswer { mockTask }.`when`(mockTask).addOnFailureListener(any())

    var successCalled = false

    repository.createNotification(
        notification, onSuccess = { successCalled = true }, onFailure = {})

    assert(successCalled) { "Success callback was not called" }
    verify(mockCollectionReference).document()
  }

  @Test
  fun getNotifications_returnsEventInvitationCorrectly() {
    val userId = "user-1"
    val timestamp = Timestamp.now()

    val notificationData =
        mapOf(
            "id" to "invite-1",
            "userId" to userId,
            "type" to "EVENT_INVITATION",
            "eventId" to "event-1",
            "eventTitle" to "Secret Meeting",
            "invitedBy" to "user-2",
            "invitedByName" to "Charlie Brown",
            "timestamp" to timestamp,
            "isRead" to false)

    `when`(mockCollectionReference.whereEqualTo("userId", userId)).thenReturn(mockQuery)
    `when`(mockQuery.orderBy("timestamp", Query.Direction.DESCENDING)).thenReturn(mockQuery)
    `when`(mockQuery.get()).thenReturn(mockQueryTask)
    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.data).thenReturn(notificationData)

    doAnswer { invocation ->
          val listener = invocation.getArgument<OnSuccessListener<QuerySnapshot>>(0)
          listener.onSuccess(mockQuerySnapshot)
          mockQueryTask
        }
        .`when`(mockQueryTask)
        .addOnSuccessListener(any())

    doAnswer { mockQueryTask }.`when`(mockQueryTask).addOnFailureListener(any())

    var resultNotifications: List<Notification>? = null

    repository.getNotifications(userId, onSuccess = { resultNotifications = it }, onFailure = {})

    assert(resultNotifications != null) { "Result notifications is null" }
    assert(resultNotifications!!.size == 1) { "Expected 1 notification" }
    assert(resultNotifications!![0] is Notification.EventInvitation) {
      "Expected EventInvitation notification"
    }
    val inviteNotif = resultNotifications!![0] as Notification.EventInvitation
    assertEquals("Secret Meeting", inviteNotif.eventTitle)
    assertEquals("Charlie Brown", inviteNotif.invitedByName)
    assertEquals("user-2", inviteNotif.invitedBy)
    assertEquals("event-1", inviteNotif.eventId)
  }
}

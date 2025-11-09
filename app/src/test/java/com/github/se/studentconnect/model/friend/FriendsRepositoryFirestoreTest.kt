package com.github.se.studentconnect.model.friend

import com.github.se.studentconnect.model.friends.FriendsRepositoryFirestore
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
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
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FriendsRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockUser: FirebaseUser
  @Mock private lateinit var mockUsersCollection: CollectionReference
  @Mock private lateinit var mockUserDocument: DocumentReference
  @Mock private lateinit var mockFriendsCollection: CollectionReference
  @Mock private lateinit var mockFriendRequestsCollection: CollectionReference
  @Mock private lateinit var mockSentRequestsCollection: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockTransaction: Transaction

  private lateinit var repository: FriendsRepositoryFirestore

  private val user1Id = "user1"
  private val user2Id = "user2"
  private val user3Id = "user3"

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock Firebase Auth to return user1 as current user
    mockStatic(FirebaseAuth::class.java).use { authStatic ->
      authStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)
    }

    whenever(mockAuth.currentUser).thenReturn(mockUser)
    whenever(mockUser.uid).thenReturn(user1Id)

    // Setup Firestore mocks
    whenever(mockFirestore.collection("users")).thenReturn(mockUsersCollection)
    whenever(mockUsersCollection.document(anyString())).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)

    repository = FriendsRepositoryFirestore(mockFirestore)
  }

  // ==================== getFriends Tests ====================

  @Test
  fun getFriends_whenNoFriends_returnsEmptyList() = runTest {
    // Mock the current user
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockFriendsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    val friends = repository.getFriends(user1Id)
    assertTrue(friends.isEmpty())
  }

  @Test
  fun getFriends_afterAddingFriend_returnsFriendsList() = runTest {
    // Mock the current user
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val mockDoc1: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockDoc1.id).thenReturn(user2Id)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockFriendsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc1))

    val friends = repository.getFriends(user1Id)
    assertEquals(1, friends.size)
    assertEquals(user2Id, friends[0])
  }

  @Test
  fun getFriends_whenNotCurrentUser_throwsException() = runTest {
    // Mock the current user as user1
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    try {
      repository.getFriends(user2Id)
      fail("Should have thrown IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message!!.contains("Users can only access their own friend data"))
    }
  }

  // ==================== getPendingRequests Tests ====================

  @Test
  fun getPendingRequests_whenNoRequests_returnsEmptyList() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockFriendRequestsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    val requests = repository.getPendingRequests(user1Id)
    assertTrue(requests.isEmpty())
  }

  @Test
  fun getPendingRequests_afterReceivingRequest_returnsRequest() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val mockDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockDoc.id).thenReturn(user2Id)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockFriendRequestsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc))

    val requests = repository.getPendingRequests(user1Id)
    assertEquals(1, requests.size)
    assertEquals(user2Id, requests[0])
  }

  // ==================== getSentRequests Tests ====================

  @Test
  fun getSentRequests_whenNoRequests_returnsEmptyList() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockSentRequestsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(emptyList())

    val sent = repository.getSentRequests(user1Id)
    assertTrue(sent.isEmpty())
  }

  @Test
  fun getSentRequests_afterSendingRequest_returnsRequest() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val mockDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(mockDoc.id).thenReturn(user2Id)

    val mockTask: Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)
    whenever(mockSentRequestsCollection.get()).thenReturn(mockTask)
    whenever(mockQuerySnapshot.documents).thenReturn(listOf(mockDoc))

    val sent = repository.getSentRequests(user1Id)
    assertEquals(1, sent.size)
    assertEquals(user2Id, sent[0])
  }

  // ==================== sendFriendRequest Tests ====================

  @Test
  fun sendFriendRequest_toSelf_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    try {
      repository.sendFriendRequest(user1Id, user1Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Cannot send friend request to yourself"))
    }
  }

  @Test
  fun sendFriendRequest_toNonexistentUser_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val nonExistentDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(nonExistentDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(nonExistentDoc)

    whenever(mockUsersCollection.document("nonexistent")).thenReturn(mockUserDocument)
    whenever(mockUserDocument.get()).thenReturn(mockTask)

    try {
      repository.sendFriendRequest(user1Id, "nonexistent")
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Recipient user not found"))
    }
  }

  @Test
  fun sendFriendRequest_fromNonCurrentUser_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    try {
      repository.sendFriendRequest(user2Id, user3Id)
      fail("Should have thrown IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message!!.contains("Users can only access their own friend data"))
    }
  }

  @Test
  fun sendFriendRequest_whenAlreadyFriends_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock user2 exists
    val user2Doc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(user2Doc.exists()).thenReturn(true)
    val userExistsTask: Task<DocumentSnapshot> = Tasks.forResult(user2Doc)

    whenever(mockUsersCollection.document(user2Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.get()).thenReturn(userExistsTask)

    // Mock already friends
    val friendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(friendDoc.exists()).thenReturn(true)
    val friendTask: Task<DocumentSnapshot> = Tasks.forResult(friendDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(friendTask)

    try {
      repository.sendFriendRequest(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Users are already friends"))
    }
  }

  @Test
  fun sendFriendRequest_whenRequestAlreadyExists_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock user2 exists
    val user2Doc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(user2Doc.exists()).thenReturn(true)
    val userExistsTask: Task<DocumentSnapshot> = Tasks.forResult(user2Doc)

    whenever(mockUsersCollection.document(user2Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.get()).thenReturn(userExistsTask)

    // Mock not friends
    val notFriendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(notFriendDoc.exists()).thenReturn(false)
    val notFriendTask: Task<DocumentSnapshot> = Tasks.forResult(notFriendDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(notFriendTask)

    // Mock request already exists
    val requestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(requestDoc.exists()).thenReturn(true)
    val requestTask: Task<DocumentSnapshot> = Tasks.forResult(requestDoc)

    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)
    whenever(mockSentRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(requestTask)

    try {
      repository.sendFriendRequest(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Friend request already sent"))
    }
  }

  @Test
  fun sendFriendRequest_whenReverseRequestExists_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock user2 exists
    val user2Doc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(user2Doc.exists()).thenReturn(true)
    val userExistsTask: Task<DocumentSnapshot> = Tasks.forResult(user2Doc)

    whenever(mockUsersCollection.document(user2Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.get()).thenReturn(userExistsTask)

    // Mock not friends
    val notFriendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(notFriendDoc.exists()).thenReturn(false)
    val notFriendTask: Task<DocumentSnapshot> = Tasks.forResult(notFriendDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(notFriendTask)

    // Mock no sent request
    val noSentDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(noSentDoc.exists()).thenReturn(false)
    val noSentTask: Task<DocumentSnapshot> = Tasks.forResult(noSentDoc)

    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)
    whenever(mockSentRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(noSentTask)

    // Mock reverse request exists
    val reverseDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(reverseDoc.exists()).thenReturn(true)
    val reverseTask: Task<DocumentSnapshot> = Tasks.forResult(reverseDoc)

    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockFriendRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(reverseTask)

    try {
      repository.sendFriendRequest(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("A friend request from the recipient already exists"))
    }
  }

  // ==================== acceptFriendRequest Tests ====================

  @Test
  fun acceptFriendRequest_whenNoRequest_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val noRequestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(noRequestDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(noRequestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockFriendRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    try {
      repository.acceptFriendRequest(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("No pending friend request"))
    }
  }

  @Test
  fun acceptFriendRequest_whenRequestExists_createsFriendship() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock request exists
    val requestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(requestDoc.exists()).thenReturn(true)
    val requestTask: Task<DocumentSnapshot> = Tasks.forResult(requestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockFriendRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(requestTask)

    // Mock transaction
    whenever(mockFirestore.runTransaction(any<Transaction.Function<Void>>()))
        .thenReturn(Tasks.forResult(null))

    repository.acceptFriendRequest(user1Id, user2Id)

    verify(mockFirestore).runTransaction(any<Transaction.Function<Void>>())
  }

  // ==================== rejectFriendRequest Tests ====================

  @Test
  fun rejectFriendRequest_whenNoRequest_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val noRequestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(noRequestDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(noRequestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockFriendRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    try {
      repository.rejectFriendRequest(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("No pending friend request"))
    }
  }

  @Test
  fun rejectFriendRequest_whenRequestExists_removesRequest() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock request exists
    val requestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(requestDoc.exists()).thenReturn(true)
    val requestTask: Task<DocumentSnapshot> = Tasks.forResult(requestDoc)

    val mockRecipientRequestRef: DocumentReference = mock(DocumentReference::class.java)
    val mockSenderRequestRef: DocumentReference = mock(DocumentReference::class.java)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockFriendRequestsCollection.document(user2Id)).thenReturn(mockRecipientRequestRef)
    whenever(mockRecipientRequestRef.get()).thenReturn(requestTask)
    whenever(mockRecipientRequestRef.delete()).thenReturn(Tasks.forResult(null))

    val mockUser2Document: DocumentReference = mock(DocumentReference::class.java)
    val mockUser2SentRequests: CollectionReference = mock(CollectionReference::class.java)
    whenever(mockUsersCollection.document(user2Id)).thenReturn(mockUser2Document)
    whenever(mockUser2Document.collection("sentRequests")).thenReturn(mockUser2SentRequests)
    whenever(mockUser2SentRequests.document(user1Id)).thenReturn(mockSenderRequestRef)
    whenever(mockSenderRequestRef.delete()).thenReturn(Tasks.forResult(null))

    repository.rejectFriendRequest(user1Id, user2Id)

    verify(mockRecipientRequestRef).delete()
    verify(mockSenderRequestRef).delete()
  }

  // ==================== cancelFriendRequest Tests ====================

  @Test
  fun cancelFriendRequest_whenNoRequest_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val noRequestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(noRequestDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(noRequestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)
    whenever(mockSentRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    try {
      repository.cancelFriendRequest(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("No sent friend request"))
    }
  }

  @Test
  fun cancelFriendRequest_whenRequestExists_removesRequest() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock sent request exists
    val requestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(requestDoc.exists()).thenReturn(true)
    val requestTask: Task<DocumentSnapshot> = Tasks.forResult(requestDoc)

    val mockSenderRequestRef: DocumentReference = mock(DocumentReference::class.java)
    val mockRecipientRequestRef: DocumentReference = mock(DocumentReference::class.java)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)
    whenever(mockSentRequestsCollection.document(user2Id)).thenReturn(mockSenderRequestRef)
    whenever(mockSenderRequestRef.get()).thenReturn(requestTask)
    whenever(mockSenderRequestRef.delete()).thenReturn(Tasks.forResult(null))

    val mockUser2Document: DocumentReference = mock(DocumentReference::class.java)
    val mockUser2FriendRequests: CollectionReference = mock(CollectionReference::class.java)
    whenever(mockUsersCollection.document(user2Id)).thenReturn(mockUser2Document)
    whenever(mockUser2Document.collection("friendRequests")).thenReturn(mockUser2FriendRequests)
    whenever(mockUser2FriendRequests.document(user1Id)).thenReturn(mockRecipientRequestRef)
    whenever(mockRecipientRequestRef.delete()).thenReturn(Tasks.forResult(null))

    repository.cancelFriendRequest(user1Id, user2Id)

    verify(mockSenderRequestRef).delete()
    verify(mockRecipientRequestRef).delete()
  }

  // ==================== removeFriend Tests ====================

  @Test
  fun removeFriend_whenNotFriends_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val notFriendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(notFriendDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(notFriendDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    try {
      repository.removeFriend(user1Id, user2Id)
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Users are not friends"))
    }
  }

  @Test
  fun removeFriend_whenFriends_removesBothSides() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    // Mock friendship exists
    val friendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(friendDoc.exists()).thenReturn(true)
    val friendTask: Task<DocumentSnapshot> = Tasks.forResult(friendDoc)

    val mockUser1FriendRef: DocumentReference = mock(DocumentReference::class.java)
    val mockUser2FriendRef: DocumentReference = mock(DocumentReference::class.java)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockUser1FriendRef)
    whenever(mockUser1FriendRef.get()).thenReturn(friendTask)
    whenever(mockUser1FriendRef.delete()).thenReturn(Tasks.forResult(null))

    val mockUser2Document: DocumentReference = mock(DocumentReference::class.java)
    val mockUser2Friends: CollectionReference = mock(CollectionReference::class.java)
    whenever(mockUsersCollection.document(user2Id)).thenReturn(mockUser2Document)
    whenever(mockUser2Document.collection("friends")).thenReturn(mockUser2Friends)
    whenever(mockUser2Friends.document(user1Id)).thenReturn(mockUser2FriendRef)
    whenever(mockUser2FriendRef.delete()).thenReturn(Tasks.forResult(null))

    repository.removeFriend(user1Id, user2Id)

    verify(mockUser1FriendRef).delete()
    verify(mockUser2FriendRef).delete()
  }

  // ==================== areFriends Tests ====================

  @Test
  fun areFriends_whenNotFriends_returnsFalse() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val notFriendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(notFriendDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(notFriendDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    assertFalse(repository.areFriends(user1Id, user2Id))
  }

  @Test
  fun areFriends_whenFriends_returnsTrue() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val friendDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(friendDoc.exists()).thenReturn(true)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(friendDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friends")).thenReturn(mockFriendsCollection)
    whenever(mockFriendsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    assertTrue(repository.areFriends(user1Id, user2Id))
  }

  // ==================== hasPendingRequest Tests ====================

  @Test
  fun hasPendingRequest_whenNoRequest_returnsFalse() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val noRequestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(noRequestDoc.exists()).thenReturn(false)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(noRequestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)
    whenever(mockSentRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    assertFalse(repository.hasPendingRequest(user1Id, user2Id))
  }

  @Test
  fun hasPendingRequest_whenRequestExists_returnsTrue() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val requestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(requestDoc.exists()).thenReturn(true)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(requestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("sentRequests")).thenReturn(mockSentRequestsCollection)
    whenever(mockSentRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    assertTrue(repository.hasPendingRequest(user1Id, user2Id))
  }

  @Test
  fun hasPendingRequest_asRecipient_checksIncomingRequests() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    val requestDoc: DocumentSnapshot = mock(DocumentSnapshot::class.java)
    whenever(requestDoc.exists()).thenReturn(true)
    val mockTask: Task<DocumentSnapshot> = Tasks.forResult(requestDoc)

    whenever(mockUsersCollection.document(user1Id)).thenReturn(mockUserDocument)
    whenever(mockUserDocument.collection("friendRequests")).thenReturn(mockFriendRequestsCollection)
    whenever(mockFriendRequestsCollection.document(user2Id)).thenReturn(mockDocumentReference)
    whenever(mockDocumentReference.get()).thenReturn(mockTask)

    assertTrue(repository.hasPendingRequest(user2Id, user1Id))
  }

  @Test
  fun hasPendingRequest_whenNotInvolved_throwsException() = runTest {
    whenever(mockAuth.currentUser?.uid).thenReturn(user1Id)

    try {
      repository.hasPendingRequest(user2Id, user3Id)
      fail("Should have thrown IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(
          e.message!!.contains("Users can only check pending requests where they are the sender"))
    }
  }

  // ==================== Authentication Tests ====================

  @Test
  fun operations_whenNotAuthenticated_throwsException() = runTest {
    whenever(mockAuth.currentUser).thenReturn(null)

    try {
      repository.getFriends(user1Id)
      fail("Should have thrown IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message!!.contains("User must be logged in"))
    }
  }
}

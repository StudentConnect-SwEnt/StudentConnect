package com.github.se.studentconnect.model.friend

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.friends.FriendsRepositoryFirestore
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryFirestore
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlin.collections.iterator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FriendsRepositoryFirestoreTest {
  private lateinit var repository: FriendsRepositoryFirestore
  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var auth: FirebaseAuth

  // Test User IDs - Store real Firebase UIDs
  private var user1Id = ""
  private var user2Id = ""
  private var user3Id = ""

  // Store user credentials for signing in
  private val user1Email = "user1@test.com"
  private val user2Email = "user2@test.com"
  private val user3Email = "user3@test.com"
  private val testPassword = "password123"

  @Before
  fun setUp() {
    auth = FirebaseEmulator.auth
    repository = FriendsRepositoryFirestore(FirebaseEmulator.firestore)
    userRepository = UserRepositoryFirestore(FirebaseEmulator.firestore)

    runBlocking {
      val users = mapOf("user1" to testPassword, "user2" to testPassword, "user3" to testPassword)

      for ((email, password) in users) {
        try {
          val result = auth.createUserWithEmailAndPassword("$email@test.com", password).await()
          val uid = result.user?.uid ?: ""

          // Create user profile
          val user =
              User(
                  userId = uid,
                  email = "$email@test.com",
                  username = email,
                  firstName = email,
                  lastName = "Test",
                  university = "EPFL",
                  hobbies = emptyList())
          userRepository.saveUser(user)

          when (email) {
            "user1" -> user1Id = uid
            "user2" -> user2Id = uid
            "user3" -> user3Id = uid
          }
        } catch (e: FirebaseAuthUserCollisionException) {
          // User already exists, retrieve their UID
          val result = auth.signInWithEmailAndPassword("$email@test.com", password).await()
          val uid = result.user?.uid ?: ""
          when (email) {
            "user1" -> user1Id = uid
            "user2" -> user2Id = uid
            "user3" -> user3Id = uid
          }
        }
      }
      auth.signOut()
    }
  }

  /** Helper method to sign in as user1 */
  private suspend fun signInAsUser1() {
    auth.signInWithEmailAndPassword(user1Email, testPassword).await()
  }

  /** Helper method to sign in as user2 */
  private suspend fun signInAsUser2() {
    auth.signInWithEmailAndPassword(user2Email, testPassword).await()
  }

  /** Helper method to sign in as user3 */
  private suspend fun signInAsUser3() {
    auth.signInWithEmailAndPassword(user3Email, testPassword).await()
  }

  @After
  fun tearDown() {
    if (this::auth.isInitialized) {
      runBlocking {
        // Clean up all friend-related data for all test users
        // Sign in as each user to clean their data (respects Firestore security rules)
        cleanupUserFriendData(user1Id, user1Email)
        cleanupUserFriendData(user2Id, user2Email)
        cleanupUserFriendData(user3Id, user3Email)

        auth.signOut()
      }
    }
  }

  /**
   * Helper method to clean up all friend-related subcollections for a user. This ensures tests
   * start with a clean state. Signs in as the user to respect Firestore security rules.
   */
  private suspend fun cleanupUserFriendData(userId: String, userEmail: String) {
    if (userId.isEmpty()) return

    // Sign in as the user to have permission to delete their data
    auth.signInWithEmailAndPassword(userEmail, testPassword).await()

    val firestore = FirebaseEmulator.firestore
    val userRef = firestore.collection("users").document(userId)

    // Delete all documents in friends subcollection
    val friendsSnapshot = userRef.collection("friends").get().await()
    for (doc in friendsSnapshot.documents) {
      doc.reference.delete().await()
    }

    // Delete all documents in friendRequests subcollection
    val requestsSnapshot = userRef.collection("friendRequests").get().await()
    for (doc in requestsSnapshot.documents) {
      doc.reference.delete().await()
    }

    // Delete all documents in sentRequests subcollection
    val sentSnapshot = userRef.collection("sentRequests").get().await()
    for (doc in sentSnapshot.documents) {
      doc.reference.delete().await()
    }
  }

  // ==================== getFriends Tests ====================

  @Test
  fun getFriends_whenNoFriends_returnsEmptyList() = runBlocking {
    signInAsUser1()
    val friends = repository.getFriends(user1Id)
    Assert.assertTrue(friends.isEmpty())
  }

  @Test
  fun getFriends_afterAddingFriend_returnsFriendsList() = runBlocking {
    // Send friend request as user1
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    // Accept friend request as user2
    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)

    // Check user1's friends
    signInAsUser1()
    val user1Friends = repository.getFriends(user1Id)
    Assert.assertEquals(1, user1Friends.size)
    Assert.assertEquals(user2Id, user1Friends[0])

    // Check user2's friends
    signInAsUser2()
    val user2Friends = repository.getFriends(user2Id)
    Assert.assertEquals(1, user2Friends.size)
    Assert.assertEquals(user1Id, user2Friends[0])
  }

  // ==================== getPendingRequests Tests ====================

  @Test
  fun getPendingRequests_whenNoRequests_returnsEmptyList() = runBlocking {
    signInAsUser1()
    val requests = repository.getPendingRequests(user1Id)
    Assert.assertTrue(requests.isEmpty())
  }

  @Test
  fun getPendingRequests_afterSendingRequest_returnsRequest() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    val requests = repository.getPendingRequests(user2Id)
    Assert.assertEquals(1, requests.size)
    Assert.assertEquals(user1Id, requests[0])
  }

  // ==================== getSentRequests Tests ====================

  @Test
  fun getSentRequests_whenNoRequests_returnsEmptyList() = runBlocking {
    signInAsUser1()
    val sent = repository.getSentRequests(user1Id)
    Assert.assertTrue(sent.isEmpty())
  }

  @Test
  fun getSentRequests_afterSendingRequest_returnsRequest() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    val sent = repository.getSentRequests(user1Id)
    Assert.assertEquals(1, sent.size)
    Assert.assertEquals(user2Id, sent[0])
  }

  // ==================== sendFriendRequest Tests ====================

  @Test
  fun sendFriendRequest_createsRequestOnBothSides() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    val sentRequests = repository.getSentRequests(user1Id)
    Assert.assertEquals(1, sentRequests.size)
    Assert.assertEquals(user2Id, sentRequests[0])

    signInAsUser2()
    val pendingRequests = repository.getPendingRequests(user2Id)
    Assert.assertEquals(1, pendingRequests.size)
    Assert.assertEquals(user1Id, pendingRequests[0])
  }

  @Test
  fun sendFriendRequest_toSelf_throwsException() = runBlocking {
    signInAsUser1()
    try {
      repository.sendFriendRequest(user1Id, user1Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Cannot send friend request to yourself"))
    }
  }

  @Test
  fun sendFriendRequest_toNonexistentUser_throwsException() = runBlocking {
    signInAsUser1()
    try {
      repository.sendFriendRequest(user1Id, "nonexistent-user")
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Recipient user not found"))
    }
  }

  @Test
  fun sendFriendRequest_fromNonexistentUser_throwsException() = runBlocking {
    signInAsUser1()
    try {
      repository.sendFriendRequest("nonexistent-user", user1Id)
      Assert.fail("Should have thrown IllegalAccessException")
    } catch (e: IllegalAccessException) {
      Assert.assertTrue(e.message!!.contains("Users can only access their own friend data"))
    }
  }

  @Test
  fun sendFriendRequest_whenAlreadyFriends_throwsException() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)

    signInAsUser1()
    try {
      repository.sendFriendRequest(user1Id, user2Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Users are already friends"))
    }
  }

  @Test
  fun sendFriendRequest_whenRequestAlreadyExists_throwsException() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    try {
      repository.sendFriendRequest(user1Id, user2Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Friend request already sent"))
    }
  }

  @Test
  fun sendFriendRequest_whenReverseRequestExists_throwsException() = runBlocking {
    signInAsUser2()
    repository.sendFriendRequest(user2Id, user1Id)

    signInAsUser1()
    try {
      repository.sendFriendRequest(user1Id, user2Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("A friend request from the recipient already exists"))
    }
  }

  // ==================== acceptFriendRequest Tests ====================

  @Test
  fun acceptFriendRequest_createsBidirectionalFriendship() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)

    // Check both are friends
    signInAsUser1()
    Assert.assertTrue(repository.areFriends(user1Id, user2Id))

    signInAsUser2()
    Assert.assertTrue(repository.areFriends(user2Id, user1Id))

    // Check requests are removed
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())

    signInAsUser1()
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())
  }

  @Test
  fun acceptFriendRequest_whenNoRequest_throwsException() = runBlocking {
    signInAsUser2()
    try {
      repository.acceptFriendRequest(user2Id, user1Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("No pending friend request"))
    }
  }

  // ==================== rejectFriendRequest Tests ====================

  @Test
  fun rejectFriendRequest_removesRequest() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    repository.rejectFriendRequest(user2Id, user1Id)

    // Check requests are removed
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())

    signInAsUser1()
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())

    // Check not friends
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
  }

  @Test
  fun rejectFriendRequest_whenNoRequest_throwsException() = runBlocking {
    signInAsUser2()
    try {
      repository.rejectFriendRequest(user2Id, user1Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("No pending friend request"))
    }
  }

  // ==================== cancelFriendRequest Tests ====================

  @Test
  fun cancelFriendRequest_removesRequest() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)
    repository.cancelFriendRequest(user1Id, user2Id)

    // Check requests are removed
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())

    signInAsUser2()
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())
  }

  @Test
  fun cancelFriendRequest_whenNoRequest_throwsException() = runBlocking {
    signInAsUser1()
    try {
      repository.cancelFriendRequest(user1Id, user2Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("No sent friend request"))
    }
  }

  // ==================== removeFriend Tests ====================

  @Test
  fun removeFriend_removesBidirectionalFriendship() = runBlocking {
    // Become friends
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)

    // Remove friendship
    signInAsUser1()
    repository.removeFriend(user1Id, user2Id)

    // Check both are no longer friends
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
    Assert.assertTrue(repository.getFriends(user1Id).isEmpty())

    signInAsUser2()
    Assert.assertFalse(repository.areFriends(user2Id, user1Id))
    Assert.assertTrue(repository.getFriends(user2Id).isEmpty())
  }

  @Test
  fun removeFriend_whenNotFriends_throwsException() = runBlocking {
    signInAsUser1()
    try {
      repository.removeFriend(user1Id, user2Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Users are not friends"))
    }
  }

  // ==================== areFriends Tests ====================

  @Test
  fun areFriends_whenNotFriends_returnsFalse() = runBlocking {
    signInAsUser1()
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
  }

  @Test
  fun areFriends_whenFriends_returnsTrue() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)

    signInAsUser1()
    Assert.assertTrue(repository.areFriends(user1Id, user2Id))

    signInAsUser2()
    Assert.assertTrue(repository.areFriends(user2Id, user1Id))
  }

  // ==================== hasPendingRequest Tests ====================

  @Test
  fun hasPendingRequest_whenNoRequest_returnsFalse() = runBlocking {
    signInAsUser1()
    Assert.assertFalse(repository.hasPendingRequest(user1Id, user2Id))
  }

  @Test
  fun hasPendingRequest_whenRequestExists_returnsTrue() = runBlocking {
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)
    Assert.assertTrue(repository.hasPendingRequest(user1Id, user2Id))
  }

  // ==================== Integration Tests ====================

  @Test
  fun fullFriendshipFlow_sendAcceptRemove() = runBlocking {
    // Initially not friends
    signInAsUser1()
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))

    // User1 sends request to User2
    repository.sendFriendRequest(user1Id, user2Id)
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
    Assert.assertTrue(repository.hasPendingRequest(user1Id, user2Id))

    // User2 accepts
    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)
    Assert.assertTrue(repository.areFriends(user2Id, user1Id))
    Assert.assertFalse(repository.hasPendingRequest(user2Id, user1Id))

    signInAsUser1()
    Assert.assertTrue(repository.areFriends(user1Id, user2Id))
    Assert.assertFalse(repository.hasPendingRequest(user1Id, user2Id))

    // User1 removes friendship
    repository.removeFriend(user1Id, user2Id)
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))

    signInAsUser2()
    Assert.assertFalse(repository.areFriends(user2Id, user1Id))
  }

  @Test
  fun multipleFriends_managedCorrectly() = runBlocking {
    // User1 becomes friends with User2 and User3
    signInAsUser1()
    repository.sendFriendRequest(user1Id, user2Id)

    signInAsUser2()
    repository.acceptFriendRequest(user2Id, user1Id)

    signInAsUser1()
    repository.sendFriendRequest(user1Id, user3Id)

    signInAsUser3()
    repository.acceptFriendRequest(user3Id, user1Id)

    // Check User1 has 2 friends
    signInAsUser1()
    val user1Friends = repository.getFriends(user1Id)
    Assert.assertEquals(2, user1Friends.size)
    Assert.assertTrue(user1Friends.contains(user2Id))
    Assert.assertTrue(user1Friends.contains(user3Id))

    // Check User2 has 1 friend
    signInAsUser2()
    val user2Friends = repository.getFriends(user2Id)
    Assert.assertEquals(1, user2Friends.size)
    Assert.assertEquals(user1Id, user2Friends[0])

    // Remove one friendship
    signInAsUser1()
    repository.removeFriend(user1Id, user2Id)

    // Check User1 now has 1 friend
    val user1FriendsAfter = repository.getFriends(user1Id)
    Assert.assertEquals(1, user1FriendsAfter.size)
    Assert.assertEquals(user3Id, user1FriendsAfter[0])
  }
}

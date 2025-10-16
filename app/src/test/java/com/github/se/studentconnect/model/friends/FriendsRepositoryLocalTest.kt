package com.github.se.studentconnect.model.friends

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FriendsRepositoryLocalTest {
  private lateinit var repository: FriendsRepositoryLocal

  // Test User IDs
  private val user1Id = "user1"
  private val user2Id = "user2"
  private val user3Id = "user3"

  @Before
  fun setUp() {
    repository = FriendsRepositoryLocal()
  }

  @After fun tearDown() = runBlocking { repository.clear() }

  // ==================== getFriends Tests ====================

  @Test
  fun getFriends_whenNoFriends_returnsEmptyList() = runBlocking {
    val friends = repository.getFriends(user1Id)
    Assert.assertTrue(friends.isEmpty())
  }

  @Test
  fun getFriends_afterAddingFriend_returnsFriendsList() = runBlocking {
    // Send friend request
    repository.sendFriendRequest(user1Id, user2Id)

    // Accept friend request
    repository.acceptFriendRequest(user2Id, user1Id)

    // Check user1's friends
    val user1Friends = repository.getFriends(user1Id)
    Assert.assertEquals(1, user1Friends.size)
    Assert.assertEquals(user2Id, user1Friends[0])

    // Check user2's friends
    val user2Friends = repository.getFriends(user2Id)
    Assert.assertEquals(1, user2Friends.size)
    Assert.assertEquals(user1Id, user2Friends[0])
  }

  // ==================== getPendingRequests Tests ====================

  @Test
  fun getPendingRequests_whenNoRequests_returnsEmptyList() = runBlocking {
    val requests = repository.getPendingRequests(user1Id)
    Assert.assertTrue(requests.isEmpty())
  }

  @Test
  fun getPendingRequests_afterSendingRequest_returnsRequest() = runBlocking {
    repository.sendFriendRequest(user1Id, user2Id)

    val requests = repository.getPendingRequests(user2Id)
    Assert.assertEquals(1, requests.size)
    Assert.assertEquals(user1Id, requests[0])
  }

  // ==================== getSentRequests Tests ====================

  @Test
  fun getSentRequests_whenNoRequests_returnsEmptyList() = runBlocking {
    val sent = repository.getSentRequests(user1Id)
    Assert.assertTrue(sent.isEmpty())
  }

  @Test
  fun getSentRequests_afterSendingRequest_returnsRequest() = runBlocking {
    repository.sendFriendRequest(user1Id, user2Id)

    val sent = repository.getSentRequests(user1Id)
    Assert.assertEquals(1, sent.size)
    Assert.assertEquals(user2Id, sent[0])
  }

  // ==================== sendFriendRequest Tests ====================

  @Test
  fun sendFriendRequest_createsRequestOnBothSides() = runBlocking {
    repository.sendFriendRequest(user1Id, user2Id)

    val sentRequests = repository.getSentRequests(user1Id)
    Assert.assertEquals(1, sentRequests.size)
    Assert.assertEquals(user2Id, sentRequests[0])

    val pendingRequests = repository.getPendingRequests(user2Id)
    Assert.assertEquals(1, pendingRequests.size)
    Assert.assertEquals(user1Id, pendingRequests[0])
  }

  @Test
  fun sendFriendRequest_toSelf_throwsException() = runBlocking {
    try {
      repository.sendFriendRequest(user1Id, user1Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Cannot send friend request to yourself"))
    }
  }

  @Test
  fun sendFriendRequest_whenAlreadyFriends_throwsException() = runBlocking {
    repository.sendFriendRequest(user1Id, user2Id)
    repository.acceptFriendRequest(user2Id, user1Id)

    try {
      repository.sendFriendRequest(user1Id, user2Id)
      Assert.fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      Assert.assertTrue(e.message!!.contains("Users are already friends"))
    }
  }

  @Test
  fun sendFriendRequest_whenRequestAlreadyExists_throwsException() = runBlocking {
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
    repository.sendFriendRequest(user2Id, user1Id)

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
    repository.sendFriendRequest(user1Id, user2Id)
    repository.acceptFriendRequest(user2Id, user1Id)

    // Check both are friends
    Assert.assertTrue(repository.areFriends(user1Id, user2Id))
    Assert.assertTrue(repository.areFriends(user2Id, user1Id))

    // Check requests are removed
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())
  }

  @Test
  fun acceptFriendRequest_whenNoRequest_throwsException() = runBlocking {
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
    repository.sendFriendRequest(user1Id, user2Id)
    repository.rejectFriendRequest(user2Id, user1Id)

    // Check requests are removed
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())

    // Check not friends
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
  }

  @Test
  fun rejectFriendRequest_whenNoRequest_throwsException() = runBlocking {
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
    repository.sendFriendRequest(user1Id, user2Id)
    repository.cancelFriendRequest(user1Id, user2Id)

    // Check requests are removed
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())
  }

  @Test
  fun cancelFriendRequest_whenNoRequest_throwsException() = runBlocking {
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
    repository.sendFriendRequest(user1Id, user2Id)
    repository.acceptFriendRequest(user2Id, user1Id)

    // Remove friendship
    repository.removeFriend(user1Id, user2Id)

    // Check both are no longer friends
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
    Assert.assertTrue(repository.getFriends(user1Id).isEmpty())
    Assert.assertFalse(repository.areFriends(user2Id, user1Id))
    Assert.assertTrue(repository.getFriends(user2Id).isEmpty())
  }

  @Test
  fun removeFriend_whenNotFriends_throwsException() = runBlocking {
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
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
  }

  @Test
  fun areFriends_whenFriends_returnsTrue() = runBlocking {
    repository.sendFriendRequest(user1Id, user2Id)
    repository.acceptFriendRequest(user2Id, user1Id)

    Assert.assertTrue(repository.areFriends(user1Id, user2Id))
    Assert.assertTrue(repository.areFriends(user2Id, user1Id))
  }

  // ==================== hasPendingRequest Tests ====================

  @Test
  fun hasPendingRequest_whenNoRequest_returnsFalse() = runBlocking {
    Assert.assertFalse(repository.hasPendingRequest(user1Id, user2Id))
  }

  @Test
  fun hasPendingRequest_whenRequestExists_returnsTrue() = runBlocking {
    repository.sendFriendRequest(user1Id, user2Id)
    Assert.assertTrue(repository.hasPendingRequest(user1Id, user2Id))
  }

  // ==================== Integration Tests ====================

  @Test
  fun fullFriendshipFlow_sendAcceptRemove() = runBlocking {
    // Initially not friends
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))

    // User1 sends request to User2
    repository.sendFriendRequest(user1Id, user2Id)
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
    Assert.assertTrue(repository.hasPendingRequest(user1Id, user2Id))

    // User2 accepts
    repository.acceptFriendRequest(user2Id, user1Id)
    Assert.assertTrue(repository.areFriends(user2Id, user1Id))
    Assert.assertFalse(repository.hasPendingRequest(user2Id, user1Id))
    Assert.assertTrue(repository.areFriends(user1Id, user2Id))
    Assert.assertFalse(repository.hasPendingRequest(user1Id, user2Id))

    // User1 removes friendship
    repository.removeFriend(user1Id, user2Id)
    Assert.assertFalse(repository.areFriends(user1Id, user2Id))
    Assert.assertFalse(repository.areFriends(user2Id, user1Id))
  }

  @Test
  fun multipleFriends_managedCorrectly() = runBlocking {
    // User1 becomes friends with User2 and User3
    repository.sendFriendRequest(user1Id, user2Id)
    repository.acceptFriendRequest(user2Id, user1Id)

    repository.sendFriendRequest(user1Id, user3Id)
    repository.acceptFriendRequest(user3Id, user1Id)

    // Check User1 has 2 friends
    val user1Friends = repository.getFriends(user1Id)
    Assert.assertEquals(2, user1Friends.size)
    Assert.assertTrue(user1Friends.contains(user2Id))
    Assert.assertTrue(user1Friends.contains(user3Id))

    // Check User2 has 1 friend
    val user2Friends = repository.getFriends(user2Id)
    Assert.assertEquals(1, user2Friends.size)
    Assert.assertEquals(user1Id, user2Friends[0])

    // Remove one friendship
    repository.removeFriend(user1Id, user2Id)

    // Check User1 now has 1 friend
    val user1FriendsAfter = repository.getFriends(user1Id)
    Assert.assertEquals(1, user1FriendsAfter.size)
    Assert.assertEquals(user3Id, user1FriendsAfter[0])
  }

  // ==================== Thread Safety Tests ====================

  @Test
  fun concurrentOperations_handleCorrectly() = runBlocking {
    // This test verifies that the mutex protects concurrent operations
    val jobs =
        List(10) { i ->
          launch {
            val userId = "user$i"
            val friendId = "friend$i"
            repository.sendFriendRequest(userId, friendId)
            repository.acceptFriendRequest(friendId, userId)
            Assert.assertTrue(repository.areFriends(userId, friendId))
          }
        }

    jobs.forEach { it.join() }

    // Verify all friendships were created
    for (i in 0 until 10) {
      val userId = "user$i"
      val friendId = "friend$i"
      Assert.assertTrue(repository.areFriends(userId, friendId))
    }
  }

  @Test
  fun clear_removesAllData() = runBlocking {
    // Add some data
    repository.sendFriendRequest(user1Id, user2Id)
    repository.sendFriendRequest(user1Id, user3Id)
    repository.acceptFriendRequest(user2Id, user1Id)

    // Verify data exists
    Assert.assertTrue(repository.getFriends(user1Id).isNotEmpty())
    Assert.assertTrue(repository.getSentRequests(user1Id).isNotEmpty())

    // Clear
    repository.clear()

    // Verify all data is removed
    Assert.assertTrue(repository.getFriends(user1Id).isEmpty())
    Assert.assertTrue(repository.getFriends(user2Id).isEmpty())
    Assert.assertTrue(repository.getPendingRequests(user2Id).isEmpty())
    Assert.assertTrue(repository.getSentRequests(user1Id).isEmpty())
  }
}

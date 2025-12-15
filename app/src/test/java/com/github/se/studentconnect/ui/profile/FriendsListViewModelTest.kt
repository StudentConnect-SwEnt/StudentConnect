package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class FriendsListViewModelTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var testScope: TestScope
  private lateinit var userRepository: TestUserRepository
  private lateinit var friendsRepository: TestFriendsRepository
  private lateinit var viewModel: FriendsListViewModel

  private val testUser1 =
      User(
          userId = "user1",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          university = "EPFL")

  private val testUser2 =
      User(
          userId = "user2",
          username = "janedoe",
          firstName = "Jane",
          lastName = "Doe",
          email = "jane@example.com",
          university = "ETHZ")

  private val testUser3 =
      User(
          userId = "user3",
          username = "bobsmith",
          firstName = "Bob",
          lastName = "Smith",
          email = "bob@example.com",
          university = "UNIL")

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    testScope = TestScope(testDispatcher)
    Dispatchers.setMain(testDispatcher)

    userRepository = TestUserRepository()
    friendsRepository = TestFriendsRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `viewModel initializes and loads friends`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2")
        userRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        assertEquals(2, viewModel.friends.value.size)
        assertEquals(2, viewModel.filteredFriends.value.size)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel handles empty friends list`() =
      testScope.runTest {
        friendsRepository.friendsList = emptyList()

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        assertTrue(viewModel.friends.value.isEmpty())
        assertTrue(viewModel.filteredFriends.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel shows loading state during fetch`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1")
        userRepository.users = mapOf("user1" to testUser1)
        userRepository.delay = 100L

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles friends repository error`() =
      testScope.runTest {
        friendsRepository.shouldThrowError = true

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        assertTrue(viewModel.friends.value.isEmpty())
        assertEquals("Friends fetch failed", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles user repository returning null users`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2", "user3")
        userRepository.users = mapOf("user1" to testUser1, "user3" to testUser3)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        assertEquals(2, viewModel.friends.value.size)
        assertEquals(testUser1, viewModel.friends.value[0])
        assertEquals(testUser3, viewModel.friends.value[1])
      }

  @Test
  fun `updateSearchQuery filters friends by full name`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2", "user3")
        userRepository.users =
            mapOf("user1" to testUser1, "user2" to testUser2, "user3" to testUser3)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("John")
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredFriends.value.size)
        assertEquals(testUser1, viewModel.filteredFriends.value[0])
      }

  @Test
  fun `updateSearchQuery filters friends by username`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2", "user3")
        userRepository.users =
            mapOf("user1" to testUser1, "user2" to testUser2, "user3" to testUser3)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("bobsmith")
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredFriends.value.size)
        assertEquals(testUser3, viewModel.filteredFriends.value[0])
      }

  @Test
  fun `updateSearchQuery is case insensitive`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2")
        userRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("JANE")
        advanceUntilIdle()

        assertEquals(1, viewModel.filteredFriends.value.size)
        assertEquals(testUser2, viewModel.filteredFriends.value[0])
      }

  @Test
  fun `updateSearchQuery with empty string shows all friends`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2", "user3")
        userRepository.users =
            mapOf("user1" to testUser1, "user2" to testUser2, "user3" to testUser3)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("John")
        advanceUntilIdle()
        assertEquals(1, viewModel.filteredFriends.value.size)

        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        assertEquals(3, viewModel.filteredFriends.value.size)
      }

  @Test
  fun `updateSearchQuery with blank string shows all friends`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2")
        userRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("   ")
        advanceUntilIdle()

        assertEquals(2, viewModel.filteredFriends.value.size)
      }

  @Test
  fun `updateSearchQuery with no matches returns empty list`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2")
        userRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("nonexistent")
        advanceUntilIdle()

        assertTrue(viewModel.filteredFriends.value.isEmpty())
      }

  @Test
  fun `searchQuery state updates correctly`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1")
        userRepository.users = mapOf("user1" to testUser1)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        assertEquals("", viewModel.searchQuery.value)

        viewModel.updateSearchQuery("test query")
        advanceUntilIdle()

        assertEquals("test query", viewModel.searchQuery.value)
      }

  @Test
  fun `loadFriends can be called to reload friends`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1")
        userRepository.users = mapOf("user1" to testUser1)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()
        assertEquals(1, viewModel.friends.value.size)

        friendsRepository.friendsList = listOf("user1", "user2")
        userRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

        viewModel.loadFriends()
        advanceUntilIdle()

        assertEquals(2, viewModel.friends.value.size)
      }

  @Test
  fun `loadFriends clears previous error on success`() =
      testScope.runTest {
        friendsRepository.shouldThrowError = true

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()
        assertEquals("Friends fetch failed", viewModel.error.value)

        friendsRepository.shouldThrowError = false
        friendsRepository.friendsList = listOf("user1")
        userRepository.users = mapOf("user1" to testUser1)

        viewModel.loadFriends()
        advanceUntilIdle()

        assertNull(viewModel.error.value)
        assertEquals(1, viewModel.friends.value.size)
      }

  @Test
  fun `viewModel handles large number of friends`() =
      testScope.runTest {
        val largeNumberOfFriends = (1..100).map { "user$it" }
        val largeUserMap =
            largeNumberOfFriends.associateWith {
              User(
                  userId = it,
                  username = "user$it",
                  firstName = "First$it",
                  lastName = "Last$it",
                  email = "$it@example.com",
                  university = "EPFL")
            }

        friendsRepository.friendsList = largeNumberOfFriends
        userRepository.users = largeUserMap

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        assertEquals(100, viewModel.friends.value.size)
        assertEquals(100, viewModel.filteredFriends.value.size)
      }

  @Test
  fun `search works correctly with partial matches`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("user1", "user2")
        userRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

        viewModel =
            FriendsListViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                userId = "currentUser")

        advanceUntilIdle()

        viewModel.updateSearchQuery("Doe")
        advanceUntilIdle()

        assertEquals(2, viewModel.filteredFriends.value.size)
      }

  // Test helper classes
  private class TestUserRepository(
      var users: Map<String, User> = emptyMap(),
      var delay: Long = 0L,
      var shouldThrowError: Boolean = false
  ) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
      if (delay > 0) delay(delay)
      if (shouldThrowError) throw Exception("User fetch failed")
      return users[userId]
    }

    override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

    override suspend fun saveUser(user: User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = users.values.toList()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) = emptyList<User>()

    override suspend fun getUsersByHobby(hobby: String) = emptyList<User>()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) = emptyList<Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true

    override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()

    override suspend fun pinOrganization(userId: String, organizationId: String) {}

    override suspend fun unpinOrganization(userId: String) {}

    override suspend fun getPinnedOrganization(userId: String): String? = null
  }

  private class TestFriendsRepository(
      var friendsList: List<String> = emptyList(),
      var shouldThrowError: Boolean = false
  ) : FriendsRepository {

    override suspend fun getFriends(userId: String): List<String> {
      if (shouldThrowError) throw Exception("Friends fetch failed")
      return friendsList
    }

    override suspend fun getPendingRequests(userId: String): List<String> = emptyList()

    override suspend fun getSentRequests(userId: String): List<String> = emptyList()

    override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) = Unit

    override suspend fun acceptFriendRequest(userId: String, fromUserId: String) = Unit

    override suspend fun rejectFriendRequest(userId: String, fromUserId: String) = Unit

    override suspend fun cancelFriendRequest(userId: String, toUserId: String) = Unit

    override suspend fun removeFriend(userId: String, friendId: String) = Unit

    override suspend fun areFriends(userId: String, otherUserId: String) = false

    override suspend fun hasPendingRequest(fromUserId: String, toUserId: String) = false

    override fun observeFriendship(userId: String, otherUserId: String): Flow<Boolean> {
      TODO("Not yet implemented")
    }
  }
}

package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.repository.UserRepository
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
class ProfileScreenViewModelTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var testScope: TestScope
  private lateinit var userRepository: TestUserRepository
  private lateinit var friendsRepository: TestFriendsRepository
  private lateinit var viewModel: ProfileScreenViewModel

  private val testUser =
      User(
          userId = "test_user_123",
          username = "testuser",
          firstName = "Test",
          lastName = "User",
          email = "test@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Coding"),
          bio = "Test bio")

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    testScope = TestScope(testDispatcher)
    Dispatchers.setMain(testDispatcher)

    userRepository = TestUserRepository(testUser)
    friendsRepository = TestFriendsRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `viewModel initializes and loads user profile`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel loads friends count correctly`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("friend1", "friend2", "friend3")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(3, viewModel.friendsCount.value)
      }

  @Test
  fun `viewModel loads events count correctly`() =
      testScope.runTest {
        userRepository.joinedEventIds = listOf("event1", "event2", "event3", "event4")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(4, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel handles empty friends list`() =
      testScope.runTest {
        friendsRepository.friendsList = emptyList()

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(0, viewModel.friendsCount.value)
      }

  @Test
  fun `viewModel handles empty events list`() =
      testScope.runTest {
        userRepository.joinedEventIds = emptyList()

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(0, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel shows loading state during data fetch`() =
      testScope.runTest {
        userRepository.delay = 100L

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles user fetch error`() =
      testScope.runTest {
        userRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertNull(viewModel.user.value)
        assertEquals("User fetch failed", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
      }

  @Test
  fun `viewModel handles friends fetch error gracefully`() =
      testScope.runTest {
        friendsRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertEquals(0, viewModel.friendsCount.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `viewModel handles events fetch error gracefully`() =
      testScope.runTest {
        userRepository.shouldThrowErrorOnGetJoinedEvents = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(testUser, viewModel.user.value)
        assertEquals(0, viewModel.eventsCount.value)
        assertNull(viewModel.error.value)
      }

  @Test
  fun `loadUserProfile reloads user data`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        val updatedUser = testUser.copy(firstName = "Updated")
        userRepository.user = updatedUser

        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertEquals(updatedUser, viewModel.user.value)
      }

  @Test
  fun `loadUserProfile updates friends count`() =
      testScope.runTest {
        friendsRepository.friendsList = listOf("friend1")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals(1, viewModel.friendsCount.value)

        friendsRepository.friendsList = listOf("friend1", "friend2", "friend3")

        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertEquals(3, viewModel.friendsCount.value)
      }

  @Test
  fun `loadUserProfile updates events count`() =
      testScope.runTest {
        userRepository.joinedEventIds = listOf("event1")

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals(1, viewModel.eventsCount.value)

        userRepository.joinedEventIds = listOf("event1", "event2")

        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertEquals(2, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel initializes pinned events as empty`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertTrue(viewModel.pinnedEvents.value.isEmpty())
      }

  @Test
  fun `loadPinnedEvents returns empty list`() =
      testScope.runTest {
        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        viewModel.loadPinnedEvents()
        advanceUntilIdle()

        assertTrue(viewModel.pinnedEvents.value.isEmpty())
      }

  @Test
  fun `viewModel handles large friends count`() =
      testScope.runTest {
        friendsRepository.friendsList = (1..1000).map { "friend$it" }

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(1000, viewModel.friendsCount.value)
      }

  @Test
  fun `viewModel handles large events count`() =
      testScope.runTest {
        userRepository.joinedEventIds = (1..500).map { "event$it" }

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()

        assertEquals(500, viewModel.eventsCount.value)
      }

  @Test
  fun `viewModel error state is cleared on successful reload`() =
      testScope.runTest {
        userRepository.shouldThrowError = true

        viewModel =
            ProfileScreenViewModel(
                userRepository = userRepository,
                friendsRepository = friendsRepository,
                currentUserId = testUser.userId)

        advanceUntilIdle()
        assertEquals("User fetch failed", viewModel.error.value)

        userRepository.shouldThrowError = false
        viewModel.loadUserProfile()
        advanceUntilIdle()

        assertNull(viewModel.error.value)
        assertEquals(testUser, viewModel.user.value)
      }

  // Test helper classes
  private class TestUserRepository(
      var user: User?,
      var delay: Long = 0L,
      var shouldThrowError: Boolean = false,
      var shouldThrowErrorOnGetJoinedEvents: Boolean = false,
      var joinedEventIds: List<String> = emptyList()
  ) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
      if (delay > 0) delay(delay)
      if (shouldThrowError) throw Exception("User fetch failed")
      return if (userId == user?.userId) user else null
    }

    override suspend fun getJoinedEvents(userId: String): List<String> {
      if (shouldThrowErrorOnGetJoinedEvents) throw Exception("Events fetch failed")
      return joinedEventIds
    }

    override suspend fun saveUser(user: User) = Unit

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<User>()

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

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

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

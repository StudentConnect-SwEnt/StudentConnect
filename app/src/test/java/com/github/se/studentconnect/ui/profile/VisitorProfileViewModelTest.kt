package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.resources.TestResourceProvider
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.visitorProfile.FriendRequestStatus
import com.github.se.studentconnect.ui.screen.visitorProfile.VisitorProfileViewModel
import com.github.se.studentconnect.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitorProfileViewModelTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  // Resource provider reads app/src/main/res/values/strings.xml so tests use the same messages.
  private val rp = TestResourceProvider()

  @Before
  fun setup() {
    // Set test user ID to avoid Firebase initialization in tests
    AuthenticationProvider.testUserId = "test-user-id"
  }

  @After
  fun teardown() {
    // Clean up test user ID
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun loadProfile_success_updatesUiState() = runTest {
    val user =
        User(
            userId = "user-1",
            email = "user1@studentconnect.ch",
            username = "user1",
            firstName = "Lia",
            lastName = "River",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel =
        VisitorProfileViewModel(fakeRepository { user }, fakeFriendsRepository(), rp::getString)

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(user, state.user)
    assertNull(state.errorMessage)
  }

  @Test
  fun loadProfile_notFound_setsError() = runTest {
    val viewModel =
        VisitorProfileViewModel(fakeRepository { null }, fakeFriendsRepository(), rp::getString)

    viewModel.loadProfile("missing-user")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertEquals(rp.getString(R.string.error_profile_not_found), state.errorMessage)
  }

  @Test
  fun loadProfile_failure_setsErrorMessage() = runTest {
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository { throw IllegalStateException("boom") },
            fakeFriendsRepository(),
            rp::getString)

    viewModel.loadProfile("user-2")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertEquals("boom", state.errorMessage)
  }

  private fun fakeRepository(provider: suspend (String) -> User?): UserRepository {
    return object : UserRepository {
      override suspend fun leaveEvent(eventId: String, userId: String) = Unit

      override suspend fun getUserById(userId: String): User? = provider(userId)

      override suspend fun getUserByEmail(email: String): User? = error("Not implemented")

      override suspend fun getAllUsers(): List<User> = emptyList()

      override suspend fun getUsersPaginated(
          limit: Int,
          lastUserId: String?
      ): Pair<List<User>, Boolean> = emptyList<User>() to false

      override suspend fun saveUser(user: User) = Unit

      override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

      override suspend fun deleteUser(userId: String) = Unit

      override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

      override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

      override suspend fun getNewUid(): String = "uid"

      override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

      override suspend fun addEventToUser(eventId: String, userId: String) = Unit

      override suspend fun addInvitationToUser(
          eventId: String,
          userId: String,
          fromUserId: String
      ) = Unit

      override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

      override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

      override suspend fun joinEvent(eventId: String, userId: String) = Unit

      override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
          Unit

      override suspend fun addFavoriteEvent(userId: String, eventId: String) {
        TODO("Not yet implemented")
      }

      override suspend fun removeFavoriteEvent(userId: String, eventId: String) {
        TODO("Not yet implemented")
      }

      override suspend fun getFavoriteEvents(userId: String): List<String> {
        TODO("Not yet implemented")
      }

      override suspend fun checkUsernameAvailability(username: String): Boolean {
        TODO("Not yet implemented")
      }

      override suspend fun declineInvitation(eventId: String, userId: String) = Unit
    }
  }

  private fun fakeFriendsRepository(
      friends: List<String> = emptyList(),
      sentRequests: List<String> = emptyList(),
      pendingRequests: List<String> = emptyList(),
      onSendRequest: suspend (String, String) -> Unit = { _, _ -> }
  ): FriendsRepository {
    return object : FriendsRepository {
      override suspend fun getFriends(userId: String): List<String> = friends

      override suspend fun getPendingRequests(userId: String): List<String> = pendingRequests

      override suspend fun getSentRequests(userId: String): List<String> = sentRequests

      override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) =
          onSendRequest(fromUserId, toUserId)

      override suspend fun acceptFriendRequest(userId: String, fromUserId: String) = Unit

      override suspend fun rejectFriendRequest(userId: String, fromUserId: String) = Unit

      override suspend fun removeFriend(userId: String, friendId: String) = Unit

      override suspend fun cancelFriendRequest(userId: String, toUserId: String) = Unit

      override suspend fun areFriends(userId: String, otherUserId: String): Boolean =
          friends.contains(otherUserId)

      override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean =
          sentRequests.contains(toUserId)

      override fun observeFriendship(userId: String, otherUserId: String): Flow<Boolean> {
        // Simple test implementation: emit whether the two users are friends at subscription time.
        return flow { emit(friends.contains(otherUserId)) }
      }
    }
  }

  @Test
  fun loadProfile_caches_whenSameUserAndNoForce() = runTest {
    val user =
        User(
            userId = "user-cache",
            email = "cache@studentconnect.ch",
            username = "cacheuser",
            firstName = "Cache",
            lastName = "Test",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    var calls = 0
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository {
              calls += 1
              user
            },
            fakeFriendsRepository(),
            rp::getString)

    viewModel.loadProfile("user-cache")
    advanceUntilIdle()

    // Second call with same id should be skipped (no force refresh)
    viewModel.loadProfile("user-cache")
    advanceUntilIdle()

    assertEquals(1, calls)
  }

  @Test
  fun loadProfile_forceRefresh_fetchesAgain() = runTest {
    val user =
        User(
            userId = "user-refresh",
            email = "refresh@studentconnect.ch",
            username = "refreshuser",
            firstName = "Force",
            lastName = "Refresh",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    var calls = 0
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository {
              calls += 1
              user
            },
            fakeFriendsRepository(),
            rp::getString)

    viewModel.loadProfile("user-refresh")
    advanceUntilIdle()

    viewModel.loadProfile("user-refresh", forceRefresh = true)
    advanceUntilIdle()

    assertEquals(2, calls)
  }

  @Test
  fun loadProfile_afterError_allowsRetryWithoutForce() = runTest {
    val resultSequence =
        arrayListOf<User?>(
            null,
            User(
                userId = "user-retry",
                email = "retry@studentconnect.ch",
                username = "retryuser",
                firstName = "Retry",
                lastName = "Ok",
                university = "Uni",
                updatedAt = 2,
                createdAt = 1))

    var calls = 0
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository {
              calls += 1
              resultSequence.removeAt(0)
            },
            fakeFriendsRepository(),
            rp::getString)

    // First call -> not found (error)
    viewModel.loadProfile("user-retry")
    advanceUntilIdle()

    // Second call without force should proceed because there was an error
    viewModel.loadProfile("user-retry")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, calls)
    assertEquals("user-retry", state.user?.userId)
    assertNull(state.errorMessage)
  }

  @Test
  fun loadProfile_differentUser_fetchesAgain() = runTest {
    var calls = 0
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository { id ->
              calls += 1
              User(
                  userId = id,
                  email = "$id@studentconnect.ch",
                  username = "user$id",
                  firstName = "A",
                  lastName = "B",
                  university = "Uni",
                  updatedAt = 2,
                  createdAt = 1)
            },
            fakeFriendsRepository(),
            rp::getString)

    viewModel.loadProfile("user-a")
    advanceUntilIdle()

    viewModel.loadProfile("user-b")
    advanceUntilIdle()

    assertEquals(2, calls)
  }

  @Test
  fun sendFriendRequest_success_updatesFriendRequestStatus() = runTest {
    val user =
        User(
            userId = "user-1",
            email = "user1@studentconnect.ch",
            username = "user1",
            firstName = "Test",
            lastName = "User",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    var requestSent = false
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository { user },
            fakeFriendsRepository(onSendRequest = { _, _ -> requestSent = true }),
            rp::getString)

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    viewModel.sendFriendRequest()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(requestSent)
    assertEquals(FriendRequestStatus.SENT, state.friendRequestStatus)
    assertEquals(rp.getString(R.string.friend_request_sent_success), state.friendRequestMessage)
  }

  @Test
  fun loadProfile_alreadyFriends_setsFriendRequestStatus() = runTest {
    val user =
        User(
            userId = "user-1",
            email = "user1@studentconnect.ch",
            username = "user1",
            firstName = "Test",
            lastName = "User",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel =
        VisitorProfileViewModel(
            fakeRepository { user },
            fakeFriendsRepository(friends = listOf("user-1")),
            rp::getString)

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_FRIENDS, state.friendRequestStatus)
  }

  @Test
  fun loadProfile_requestAlreadySent_setsFriendRequestStatus() = runTest {
    val user =
        User(
            userId = "user-1",
            email = "user1@studentconnect.ch",
            username = "user1",
            firstName = "Test",
            lastName = "User",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel =
        VisitorProfileViewModel(
            fakeRepository { user },
            fakeFriendsRepository(sentRequests = listOf("user-1")),
            rp::getString)

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_SENT, state.friendRequestStatus)
  }
}

package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryLocal
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
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

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, fakeFriendsRepository())

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(user, state.user)
    assertNull(state.errorMessage)
  }

  @Test
  fun loadProfile_notFound_setsError() = runTest {
    val viewModel = VisitorProfileViewModel(fakeRepository { null }, fakeFriendsRepository())

    viewModel.loadProfile("missing-user")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.user)
    assertEquals("Profile not found.", state.errorMessage)
  }

  @Test
  fun loadProfile_failure_setsErrorMessage() = runTest {
    val viewModel =
        VisitorProfileViewModel(
            fakeRepository { throw IllegalStateException("boom") }, fakeFriendsRepository())

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

      override fun observeFriendship(userId: String, otherUserId: String): Flow<Boolean> = flow {
        emit(friends.contains(otherUserId))
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
            fakeFriendsRepository())

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
            fakeFriendsRepository())

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
            fakeFriendsRepository())

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
            fakeFriendsRepository())

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
            fakeFriendsRepository(onSendRequest = { _, _ -> requestSent = true }))

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    viewModel.sendFriendRequest()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(requestSent)
    assertEquals(FriendRequestStatus.SENT, state.friendRequestStatus)
    assertEquals("Friend request sent successfully!", state.friendRequestMessage)
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
            fakeRepository { user }, fakeFriendsRepository(friends = listOf("user-1")))

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
            fakeRepository { user }, fakeFriendsRepository(sentRequests = listOf("user-1")))

    viewModel.loadProfile("user-1")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_SENT, state.friendRequestStatus)
  }

  @Test
  fun cancelFriendRequest_withLocalRepo_removesSentRequest_andUpdatesUi() = runTest {
    val user =
        User(
            userId = "user-local-1",
            email = "local1@studentconnect.ch",
            username = "local1",
            firstName = "Local",
            lastName = "User",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    // Use the local friends repository to set up a sent request from current test user
    val friendsRepo = FriendsRepositoryLocal()
    val currentUser = AuthenticationProvider.currentUser
    val targetUser = user.userId

    // currentUser sends a friend request to targetUser
    friendsRepo.sendFriendRequest(currentUser, targetUser)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, friendsRepo)

    // Load profile should detect the already-sent status
    viewModel.loadProfile(targetUser)
    advanceUntilIdle()

    var state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_SENT, state.friendRequestStatus)
    assertEquals("Friend request already sent", state.friendRequestMessage)

    // Cancel the sent request via the ViewModel
    viewModel.cancelFriendRequest()
    advanceUntilIdle()

    state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.IDLE, state.friendRequestStatus)
    assertEquals("Friend request cancelled", state.friendRequestMessage)

    // Verify repository no longer has the sent request
    val stillSent = friendsRepo.hasPendingRequest(currentUser, targetUser)
    assertFalse(stillSent)
  }

  @Test
  fun removeFriend_withLocalRepo_removesFriendship_andUpdatesUi() = runTest {
    val user =
        User(
            userId = "user-local-2",
            email = "local2@studentconnect.ch",
            username = "local2",
            firstName = "Local",
            lastName = "User2",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val friendsRepo = FriendsRepositoryLocal()
    val currentUser = AuthenticationProvider.currentUser
    val targetUser = user.userId

    // Establish friendship: other user sends request, current user accepts
    friendsRepo.sendFriendRequest(targetUser, currentUser)
    friendsRepo.acceptFriendRequest(currentUser, targetUser)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, friendsRepo)

    // Load profile should detect already friends
    viewModel.loadProfile(targetUser)
    advanceUntilIdle()

    var state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_FRIENDS, state.friendRequestStatus)

    // Remove friend via ViewModel
    viewModel.removeFriend()
    advanceUntilIdle()

    state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.IDLE, state.friendRequestStatus)
    assertEquals("Friend removed", state.friendRequestMessage)

    // Verify repository no longer considers them friends
    val areStillFriends = friendsRepo.areFriends(currentUser, targetUser)
    assertFalse(areStillFriends)
  }

  @Test
  fun subscribeToFriendshipUpdates_observerEmitsTrue_updatesUiState() = runTest {
    val user =
        User(
            userId = "user-obs-2",
            email = "obs2@studentconnect.ch",
            username = "obs2",
            firstName = "O2",
            lastName = "B2",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    // Use the local friends repository and establish a friendship with the current test user
    val friendsRepo = FriendsRepositoryLocal()
    // Make other user send a request to the current test user and accept it to establish friendship
    friendsRepo.sendFriendRequest(user.userId, AuthenticationProvider.currentUser)
    friendsRepo.acceptFriendRequest(AuthenticationProvider.currentUser, user.userId)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, friendsRepo)

    viewModel.loadProfile("user-obs-2")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_FRIENDS, state.friendRequestStatus)
    assertEquals("Already friends", state.friendRequestMessage)
  }

  @Test
  fun subscribeToFriendshipUpdates_notLoggedIn_doesNotObserve() = runTest {
    // Simulate not logged in by setting empty test user id
    AuthenticationProvider.testUserId = ""

    val user =
        User(
            userId = "user-obs",
            email = "obs@studentconnect.ch",
            username = "obs",
            firstName = "O",
            lastName = "B",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    // Use the local repository and pre-create a friendship between the real test user id and the
    // visited user.
    val localRepo = FriendsRepositoryLocal()
    // Create friendship between the usual test user and the observed user.
    // Do not rely on AuthenticationProvider here as it is set to empty string.

    // 1. "test-user-id" sends a request to the profile user
    localRepo.sendFriendRequest("test-user-id", user.userId)

    // 2. The profile user accepts the request from "test-user-id"
    // FIX: Pass the correct userId (receiver) and fromUserId (sender)
    localRepo.acceptFriendRequest(user.userId, "test-user-id")

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, localRepo)

    viewModel.loadProfile("user-obs")
    advanceUntilIdle()

    // Because currentUser is empty, subscribeToFriendshipUpdates should return early and not change
    // friend status
    val state = viewModel.uiState.value

    //
    assertEquals(FriendRequestStatus.IDLE, state.friendRequestStatus)

    // restore test user
    AuthenticationProvider.testUserId = "test-user-id"
  }

  @Test
  fun subscribeToFriendshipUpdates_observerThrows_isHandled() = runTest {
    val user =
        User(
            userId = "user-obs-3",
            email = "obs3@studentconnect.ch",
            username = "obs3",
            firstName = "O3",
            lastName = "B3",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val friendsRepo =
        object : FriendsRepository {
          override suspend fun getFriends(userId: String): List<String> = emptyList()

          override suspend fun getPendingRequests(userId: String): List<String> = emptyList()

          override suspend fun getSentRequests(userId: String): List<String> = emptyList()

          override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) = Unit

          override suspend fun acceptFriendRequest(userId: String, fromUserId: String) = Unit

          override suspend fun rejectFriendRequest(userId: String, fromUserId: String) = Unit

          override suspend fun removeFriend(userId: String, friendId: String) = Unit

          override suspend fun cancelFriendRequest(userId: String, toUserId: String) = Unit

          override suspend fun areFriends(userId: String, otherUserId: String): Boolean = false

          override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean =
              false

          override fun observeFriendship(userId: String, otherUserId: String) =
              kotlinx.coroutines.flow.flow<Boolean> { throw Exception("observer boom") }
        }

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, friendsRepo)

    viewModel.loadProfile("user-obs-3")
    // Should not throw even if observer throws
    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Observer error ignored - ensure we still have the loaded user
    assertEquals("user-obs-3", state.user?.userId)
  }

  @Test
  fun sendFriendRequest_notLoggedIn_setsError() = runTest {
    AuthenticationProvider.testUserId = ""

    val user =
        User(
            userId = "user-send-1",
            email = "send1@studentconnect.ch",
            username = "send1",
            firstName = "S",
            lastName = "One",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, fakeFriendsRepository())

    viewModel.loadProfile("user-send-1")
    advanceUntilIdle()

    viewModel.sendFriendRequest()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ERROR, state.friendRequestStatus)
    assertTrue(state.friendRequestMessage?.contains("logged in") == true)

    AuthenticationProvider.testUserId = "test-user-id"
  }

  @Test
  fun sendFriendRequest_toSelf_setsError() = runTest {
    AuthenticationProvider.testUserId = "self-user"

    val user =
        User(
            userId = "self-user",
            email = "self@studentconnect.ch",
            username = "self",
            firstName = "Me",
            lastName = "Self",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, fakeFriendsRepository())

    viewModel.loadProfile("self-user")
    advanceUntilIdle()

    viewModel.sendFriendRequest()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ERROR, state.friendRequestStatus)
    assertEquals("Cannot send friend request to yourself", state.friendRequestMessage)

    AuthenticationProvider.testUserId = "test-user-id"
  }

  @Test
  fun sendFriendRequest_repoThrows_illegalArgument_mapsToStatus() = runTest {
    val user =
        User(
            userId = "user-send-2",
            email = "send2@studentconnect.ch",
            username = "send2",
            firstName = "S2",
            lastName = "Two",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    var thrownMessage: String? = null
    val friendsRepo =
        fakeFriendsRepository(
            onSendRequest = { _, _ -> throw IllegalArgumentException("already friends") })

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, friendsRepo)

    viewModel.loadProfile("user-send-2")
    advanceUntilIdle()

    viewModel.sendFriendRequest()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_FRIENDS, state.friendRequestStatus)
    assertTrue(state.friendRequestMessage?.contains("already friends") == true)

    // Now test already sent
    val friendsRepo2 =
        fakeFriendsRepository(
            onSendRequest = { _, _ -> throw IllegalArgumentException("already sent") })
    val viewModel2 = VisitorProfileViewModel(fakeRepository { user }, friendsRepo2)
    viewModel2.loadProfile("user-send-2")
    advanceUntilIdle()
    viewModel2.sendFriendRequest()
    advanceUntilIdle()
    val state2 = viewModel2.uiState.value
    assertEquals(FriendRequestStatus.ALREADY_SENT, state2.friendRequestStatus)
    assertTrue(state2.friendRequestMessage?.contains("already sent") == true)
  }

  @Test
  fun cancelFriendRequest_notLoggedIn_setsError() = runTest {
    AuthenticationProvider.testUserId = ""

    val user =
        User(
            userId = "user-cancel-1",
            email = "cancel1@studentconnect.ch",
            username = "cancel1",
            firstName = "C",
            lastName = "One",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, fakeFriendsRepository())

    viewModel.loadProfile("user-cancel-1")
    advanceUntilIdle()

    viewModel.cancelFriendRequest()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ERROR, state.friendRequestStatus)
    assertTrue(state.friendRequestMessage?.contains("logged in") == true)

    AuthenticationProvider.testUserId = "test-user-id"
  }

  @Test
  fun removeFriend_notLoggedIn_setsError() = runTest {
    AuthenticationProvider.testUserId = ""

    val user =
        User(
            userId = "user-remove-1",
            email = "remove1@studentconnect.ch",
            username = "remove1",
            firstName = "R",
            lastName = "One",
            university = "Uni",
            updatedAt = 2,
            createdAt = 1)

    val viewModel = VisitorProfileViewModel(fakeRepository { user }, fakeFriendsRepository())

    viewModel.loadProfile("user-remove-1")
    advanceUntilIdle()

    viewModel.removeFriend()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(FriendRequestStatus.ERROR, state.friendRequestStatus)
    assertTrue(state.friendRequestMessage?.contains("logged in") == true)

    AuthenticationProvider.testUserId = "test-user-id"
  }
}

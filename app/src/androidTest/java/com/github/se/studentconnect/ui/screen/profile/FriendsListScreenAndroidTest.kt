package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.FriendsListViewModel
import com.github.se.studentconnect.ui.screen.activities.Invitation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FriendsListScreenAndroidTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockFriendsRepository: MockFriendsRepository

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
          lastName = "Smith",
          email = "jane@example.com",
          university = "ETHZ")

  private val testUser3 =
      User(
          userId = "user3",
          username = "bobwilson",
          firstName = "Bob",
          lastName = "Wilson",
          email = "bob@example.com",
          university = "UNIL")

  @Before
  fun setUp() {
    mockUserRepository = MockUserRepository()
    mockFriendsRepository = MockFriendsRepository()
  }

  @Test
  fun friendsListScreen_displaysTitle() {
    mockFriendsRepository.friendsList = listOf("user1")
    mockUserRepository.users = mapOf("user1" to testUser1)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
  }

  @Test
  fun friendsListScreen_displaysBackButton() {
    mockFriendsRepository.friendsList = emptyList()

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun friendsListScreen_backButtonTriggersNavigation() {
    var backCalled = false
    mockFriendsRepository.friendsList = emptyList()

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser",
          onNavigateBack = { backCalled = true },
          onFriendClick = {},
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(backCalled)
  }

  @Test
  fun friendsListScreen_displaysSearchBar() {
    mockFriendsRepository.friendsList = listOf("user1")
    mockUserRepository.users = mapOf("user1" to testUser1)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search friends…").assertIsDisplayed()
  }

  @Test
  fun friendsListScreen_searchBarHasSearchIcon() {
    mockFriendsRepository.friendsList = emptyList()

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
  }

  @Test
  fun friendsListScreen_displaysFriendsList() {
    mockFriendsRepository.friendsList = listOf("user1", "user2")
    mockUserRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("johndoe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    composeTestRule.onNodeWithText("janedoe").assertIsDisplayed()
  }

  @Test
  fun friendsListScreen_displaysProfilePicturePlaceholders() {
    mockFriendsRepository.friendsList = listOf("user1", "user2")
    mockUserRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onAllNodesWithContentDescription("Friend profile picture").assertCountEquals(2)
  }

  @Test
  fun friendsListScreen_friendItemClickTriggersNavigation() {
    var clickedUserId: String? = null
    mockFriendsRepository.friendsList = listOf("user1")
    mockUserRepository.users = mapOf("user1" to testUser1)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser",
          onNavigateBack = {},
          onFriendClick = { clickedUserId = it },
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("John Doe").performClick()

    assert(clickedUserId == "user1")
  }

  @Test
  fun friendsListScreen_searchFiltersResults() {
    mockFriendsRepository.friendsList = listOf("user1", "user2", "user3")
    mockUserRepository.users =
        mapOf("user1" to testUser1, "user2" to testUser2, "user3" to testUser3)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Search friends…").performTextInput("Bob")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Bob Wilson").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertDoesNotExist()
    composeTestRule.onNodeWithText("Jane Smith").assertDoesNotExist()
  }

  @Test
  fun friendsListScreen_displaysEmptyStateWhenNoFriends() {
    mockFriendsRepository.friendsList = emptyList()

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("No friends yet :(").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Add more friends and they will appear here :)")
        .assertIsDisplayed()
  }

  @Test
  fun friendsListScreen_searchByUsername() {
    mockFriendsRepository.friendsList = listOf("user1", "user2")
    mockUserRepository.users = mapOf("user1" to testUser1, "user2" to testUser2)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Search friends…").performTextInput("johndoe")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Smith").assertDoesNotExist()
  }

  @Test
  fun friendsListScreen_searchIsCaseInsensitive() {
    mockFriendsRepository.friendsList = listOf("user1")
    mockUserRepository.users = mapOf("user1" to testUser1)

    val viewModel =
        FriendsListViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            userId = "currentUser")

    composeTestRule.setContent {
      FriendsListScreen(
          userId = "currentUser", onNavigateBack = {}, onFriendClick = {}, viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Search friends…").performTextInput("JOHN")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  // Mock repositories
  private class MockUserRepository(var users: Map<String, User> = emptyMap()) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
      delay(50)
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

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true
  }

  private class MockFriendsRepository(
      var friendsList: List<String> = emptyList(),
      var shouldThrowError: Boolean = false
  ) : FriendsRepository {

    override suspend fun getFriends(userId: String): List<String> {
      delay(50)
      if (shouldThrowError) throw Exception("Friends fetch failed")
      return friendsList
    }

    override suspend fun getPendingRequests(userId: String) = emptyList<String>()

    override suspend fun getSentRequests(userId: String) = emptyList<String>()

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

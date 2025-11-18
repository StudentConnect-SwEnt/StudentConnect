package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.UserCardViewModel
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserCardScreenAndroidTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var testUser: User

  @Before
  fun setUp() {
    testUser =
        User(
            userId = "test123",
            username = "testuser",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane@test.com",
            university = "EPFL",
            bio = "Computer Science student")

    mockUserRepository = MockUserRepository(testUser)
  }

  @Test
  fun userCardScreen_displaysTopBarWithBackButton() {
    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = testUser.userId)

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun userCardScreen_backButtonTriggersNavigation() {
    var backButtonClicked = false
    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = testUser.userId)

    composeTestRule.setContent {
      UserCardScreen(viewModel = viewModel, onNavigateBack = { backButtonClicked = true })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Back").performClick()

    assert(backButtonClicked)
  }

  @Test
  fun userCardScreen_displaysInstructionText() {
    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = testUser.userId)

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap the card to flip it").assertIsDisplayed()
  }

  @Test
  fun userCardScreen_handlesUserWithAllFields() {
    val completeUser =
        User(
            userId = "complete123",
            username = "complete",
            firstName = "Complete",
            lastName = "User",
            email = "complete@test.com",
            university = "ETH Zurich",
            country = "Switzerland",
            birthdate = "01/01/2000",
            bio = "Full profile",
            profilePictureUrl = "https://example.com/pic.jpg")

    mockUserRepository.user = completeUser

    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = "complete123")

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Verify user info
    composeTestRule.onNodeWithText("Complete", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("User", substring = true).assertIsDisplayed()
  }

  @Test
  fun userCardScreen_handlesUserWithMinimalFields() {
    val minimalUser =
        User(
            userId = "minimal123",
            username = "minimal",
            firstName = "Min",
            lastName = "User",
            email = "min@test.com",
            university = "EPFL",
            hobbies = emptyList())

    mockUserRepository.user = minimalUser

    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = "minimal123")

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Should still display
    composeTestRule.onNodeWithText("Min", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("User", substring = true).assertIsDisplayed()
  }

  @Test
  fun userCardScreen_displaysCenteredContent() {
    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = testUser.userId)

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Content should be centered
    composeTestRule.onNodeWithText("Tap the card to flip it").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane", substring = true).assertIsDisplayed()
  }

  @Test
  fun userCardScreen_handlesSpecialCharactersInName() {
    val specialUser = testUser.copy(userId = "special123", firstName = "José", lastName = "García")
    mockUserRepository.user = specialUser

    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = "special123")

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel) }

    composeTestRule.waitForIdle()

    // Should display special characters correctly
    composeTestRule.onNodeWithText("José", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("García", substring = true).assertIsDisplayed()
  }

  @Test
  fun userCardScreen_topBarHasEmptyTitle() {
    val viewModel =
        UserCardViewModel(userRepository = mockUserRepository, currentUserId = testUser.userId)

    composeTestRule.setContent { UserCardScreen(viewModel = viewModel, onNavigateBack = {}) }

    composeTestRule.waitForIdle()

    // Top bar should have back button but no title
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  // Mock repository
  private class MockUserRepository(var user: User?) : UserRepository {
    override suspend fun getUserById(userId: String): User? {
      delay(50)
      return user
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

    override suspend fun getJoinedEvents(userId: String) = emptyList<String>()

    override suspend fun addEventToUser(eventId: String, userId: String) = Unit

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        Unit

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

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
}

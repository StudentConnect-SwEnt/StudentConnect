package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileSettingsScreenAndroidTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var testUser: User

  @Before
  fun setUp() {
    testUser =
        User(
            userId = "test123",
            username = "testuser",
            firstName = "John",
            lastName = "Doe",
            email = "john@test.com",
            university = "EPFL",
            country = "Switzerland",
            birthdate = "01/01/2000",
            hobbies = listOf("Reading", "Coding"),
            bio = "Test bio")

    mockUserRepository = MockUserRepository(testUser)
  }

  @Test
  fun profileSettingsScreen_displaysBackNavigationButton() {
    var backClicked = false

    composeTestRule.setContent {
      ProfileSettingsScreen(
          currentUserId = testUser.userId,
          userRepository = mockUserRepository,
          onNavigateBack = { backClicked = true })
    }

    composeTestRule.waitForIdle()

    // Back button should be visible
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

    // Click should trigger callback
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(backClicked)
  }

  @Test
  fun profileSettingsScreen_hidesBackButtonWhenCallbackIsNull() {
    composeTestRule.setContent {
      ProfileSettingsScreen(
          currentUserId = testUser.userId,
          userRepository = mockUserRepository,
          onNavigateBack = null)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
  }

  @Test
  fun profileSettingsScreen_displaysEmptyTopBarTitle() {
    composeTestRule.setContent {
      ProfileSettingsScreen(
          currentUserId = testUser.userId, userRepository = mockUserRepository, onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    // Top bar should have back button but empty title
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
  }

  @Test
  fun profileSettingsScreen_displaysProfilePicture() {
    composeTestRule.setContent {
      ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = mockUserRepository)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysEditPictureButton() {
    composeTestRule.setContent {
      ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = mockUserRepository)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Edit Profile Picture").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysEditNameButton() {
    composeTestRule.setContent {
      ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = mockUserRepository)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Edit Name").assertExists()
  }

  // Mock repository - same as in ProfileScreenAndroidTest and UserCardScreenAndroidTest
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

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

    override suspend fun joinEvent(eventId: String, userId: String) = Unit

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        Unit

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true
  }
}

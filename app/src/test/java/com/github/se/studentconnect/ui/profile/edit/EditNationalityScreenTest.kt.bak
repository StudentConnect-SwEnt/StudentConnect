package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.screen.profile.edit.EditNationalityScreen
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EditNationalityScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestUserRepository
  private val testUser =
      User(
          userId = "test_user",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthdate = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    navigatedBack = false
  }

  @Test
  fun editNationalityScreen_displaysCorrectTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Edit Nationality").assertExists()
  }

  @Test
  fun editNationalityScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun editNationalityScreen_backButtonNavigatesBack() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assertTrue("Navigation back should be called", navigatedBack)
  }

  @Test
  fun editNationalityScreen_displaysInstructions() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Where are you from ?").assertExists()
    composeTestRule
        .onNodeWithText("Helps us connect you with other students and events")
        .assertExists()
  }

  @Test
  fun editNationalityScreen_displaysSearchField() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search countries…").assertExists()
  }

  @Test
  fun editNationalityScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun editNationalityScreen_saveButtonEnabledWithPreselectedCountry() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Button should be disabled when preselected country matches current user's country
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editNationalityScreen_savesSelectedCountry() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for a different country
    composeTestRule.onNodeWithText("Search countries…").performTextReplacement("France")
    composeTestRule.waitForIdle()

    // Wait for country list to update and select France (exclude the search input)
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("France", substring = false).fetchSemanticsNodes().size >=
          2
    }
    composeTestRule.onAllNodesWithText("France", substring = false)[1].performClick()
    composeTestRule.waitForIdle()

    // Now save button should be enabled
    composeTestRule.onNodeWithText("Save").assertIsEnabled()
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify France was saved
    assertEquals("France", repository.savedUsers.last().country)
  }

  @Test
  fun editNationalityScreen_navigatesBackAfterSave() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select a different country
    composeTestRule.onNodeWithText("Search countries…").performTextReplacement("Germany")
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Germany", substring = false).fetchSemanticsNodes().size >=
          2
    }
    composeTestRule.onAllNodesWithText("Germany", substring = false)[1].performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save operation and navigation
    composeTestRule.waitForIdle()
    Thread.sleep(500) // Give time for save operation
    composeTestRule.waitForIdle()

    // Should navigate back automatically after successful save
    assert(navigatedBack)
  }

  @Test
  fun editNationalityScreen_handlesRepositoryError() {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select a different country
    composeTestRule.onNodeWithText("Search countries…").performTextReplacement("Italy")
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Italy", substring = false).fetchSemanticsNodes().size >= 2
    }
    composeTestRule.onAllNodesWithText("Italy", substring = false)[1].performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for error message
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithText("Network error").fetchSemanticsNodes().isNotEmpty()
    }

    // Should show error message in snackbar
    composeTestRule.onNodeWithText("Network error").assertExists()
  }

  @Test
  fun editNationalityScreen_withNullCountryUser() {
    val userWithoutCountry = testUser.copy(country = null)
    repository = TestUserRepository(userWithoutCountry)

    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = userWithoutCountry.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Should still display the screen properly
    composeTestRule.onNodeWithText("Edit Nationality").assertExists()
    composeTestRule.onNodeWithText("Save").assertExists()

    // Save button should be disabled when no country is selected
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editNationalityScreen_preservesOtherUserFields() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select a different country
    composeTestRule.onNodeWithText("Search countries…").performTextReplacement("Spain")
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Spain", substring = false).fetchSemanticsNodes().size >= 2
    }
    composeTestRule.onAllNodesWithText("Spain", substring = false)[1].performClick()
    composeTestRule.waitForIdle()

    // Save
    composeTestRule.onNodeWithText("Save").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    val savedUser = repository.savedUsers.last()
    // Verify other fields are preserved
    assertEquals(testUser.userId, savedUser.userId)
    assertEquals(testUser.firstName, savedUser.firstName)
    assertEquals(testUser.lastName, savedUser.lastName)
    assertEquals(testUser.email, savedUser.email)
    assertEquals(testUser.university, savedUser.university)
    assertEquals(testUser.birthdate, savedUser.birthdate)
    assertEquals(testUser.hobbies, savedUser.hobbies)
    assertEquals(testUser.bio, savedUser.bio)
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnSave: Throwable? = null,
      var saveDelay: Long = 0L
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      if (saveDelay > 0) {
        delay(saveDelay)
      }
      shouldThrowOnSave?.let { throw it }
      this.user = user
      savedUsers.add(user)
    }

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

    override suspend fun getInvitations(userId: String) = emptyList<Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

    override suspend fun removeInvitation(eventId: String, userId: String) = Unit

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
  }
}

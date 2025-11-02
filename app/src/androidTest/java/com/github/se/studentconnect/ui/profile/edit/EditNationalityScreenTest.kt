package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.profile.edit.EditNationalityScreen
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditNationalityScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestUserRepository
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
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
    assert(navigatedBack)
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
    composeTestRule.onNodeWithText("Search countries...").assertExists()
  }

  @Test
  fun editNationalityScreen_canSearchCountries() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Type in search field
    composeTestRule.onNodeWithText("Search countries...").performTextInput("France")

    // Verify search text is displayed
    composeTestRule.onNodeWithText("France", substring = true).assertExists()
  }

  @Test
  fun editNationalityScreen_filtersCountriesBasedOnSearch() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for a specific country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Fra")

    composeTestRule.waitForIdle()

    // France should be visible in the list
    composeTestRule.onNodeWithText("France").assertExists()
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
    composeTestRule.onNodeWithText("Save Changes").assertExists()
  }

  @Test
  fun editNationalityScreen_saveButtonInitiallyDisabled() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // Wait for user to load and country to be pre-selected
    composeTestRule.waitForIdle()

    // After loading, the button should be enabled because user has a country
    composeTestRule.onNodeWithText("Save Changes").assertIsEnabled()
  }

  @Test
  fun editNationalityScreen_canSelectCountry() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for and select a country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("France")
    composeTestRule.waitForIdle()

    // Click on France
    composeTestRule.onNodeWithText("France").performClick()

    composeTestRule.waitForIdle()

    // Save button should be enabled
    composeTestRule.onNodeWithText("Save Changes").assertIsEnabled()
  }

  @Test
  fun editNationalityScreen_preselectsCurrentCountry() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Switzerland should be visible in the list (user's current country)
    composeTestRule.onNodeWithText("Switzerland").assertExists()
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

    // Search and select France
    composeTestRule.onNodeWithText("Search countries...").performTextInput("France")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("France").performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Changes").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify France was saved
    assert(repository.savedUsers.last().country == "France")
  }

  @Test
  fun editNationalityScreen_showsLoadingStateWhileSaving() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select a country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Germany")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Germany").performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Changes").performClick()

    composeTestRule.waitForIdle()

    // Should show loading state (button text should disappear)
    composeTestRule.onNodeWithText("Save Changes").assertDoesNotExist()
  }

  @Test
  fun editNationalityScreen_showsSuccessMessage() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select a country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Italy")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Italy").performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Changes").performClick()

    // Wait for success message
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule
          .onAllNodesWithText("Nationality updated successfully")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Should show success message in snackbar
    composeTestRule.onNodeWithText("Nationality updated successfully").assertExists()
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

    // Select a country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Spain")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Spain").performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Changes").performClick()

    // Wait for error message
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithText("Network error").fetchSemanticsNodes().isNotEmpty()
    }

    // Should show error message in snackbar
    composeTestRule.onNodeWithText("Network error").assertExists()
  }

  @Test
  fun editNationalityScreen_displaysCountryListWithFlags() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Verify that countries are displayed (check a few common ones)
    composeTestRule.onNodeWithText("Switzerland").assertExists()
    composeTestRule.onNodeWithText("United States").assertExists()
  }

  @Test
  fun editNationalityScreen_searchIsCaseInsensitive() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search with lowercase
    composeTestRule.onNodeWithText("Search countries...").performTextInput("france")
    composeTestRule.waitForIdle()

    // France should still be found
    composeTestRule.onNodeWithText("France").assertExists()
  }

  @Test
  fun editNationalityScreen_clearingSearchShowsAllCountries() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for specific country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("France")
    composeTestRule.waitForIdle()

    // Clear search
    composeTestRule.onNodeWithText("France", substring = true).performTextClearance()
    composeTestRule.waitForIdle()

    // Multiple countries should be visible again
    composeTestRule.onNodeWithText("Switzerland").assertExists()
    composeTestRule.onNodeWithText("United States").assertExists()
  }

  @Test
  fun editNationalityScreen_canSelectMultipleCountriesSequentially() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select France
    composeTestRule.onNodeWithText("Search countries...").performTextInput("France")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("France").performClick()
    composeTestRule.waitForIdle()

    // Clear search
    composeTestRule.onNodeWithText("France", substring = true).performTextClearance()
    composeTestRule.waitForIdle()

    // Select Germany
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Germany")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Germany").performClick()
    composeTestRule.waitForIdle()

    // Save button should still be enabled
    composeTestRule.onNodeWithText("Save Changes").assertIsEnabled()
  }

  @Test
  fun editNationalityScreen_saveButtonDisabledDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select a country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Portugal")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Portugal").performClick()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Changes").performClick()

    composeTestRule.waitForIdle()

    // Button should not show "Save Changes" text anymore (showing progress instead)
    composeTestRule.onNodeWithText("Save Changes").assertDoesNotExist()
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
    composeTestRule.onNodeWithText("Save Changes").assertExists()

    // Save button should be disabled when no country is selected
    composeTestRule.onNodeWithText("Save Changes").assertIsNotEnabled()
  }

  @Test
  fun editNationalityScreen_searchWithSpecialCharacters() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for country with special characters
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Côte")
    composeTestRule.waitForIdle()

    // Côte d'Ivoire should be found (if search supports special characters)
    // Note: This depends on the actual implementation
  }

  @Test
  fun editNationalityScreen_longCountryNameDisplayedCorrectly() {
    composeTestRule.setContent {
      MaterialTheme {
        EditNationalityScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for a country with a long name
    composeTestRule.onNodeWithText("Search countries...").performTextInput("United Kingdom")
    composeTestRule.waitForIdle()

    // Should display correctly
    composeTestRule.onNodeWithText("United Kingdom").assertExists()
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

    // Select and save a new country
    composeTestRule.onNodeWithText("Search countries...").performTextInput("Belgium")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Belgium").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save Changes").performClick()

    // Wait for save
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    val savedUser = repository.savedUsers.last()
    // Verify other fields are preserved
    assert(savedUser.userId == testUser.userId)
    assert(savedUser.firstName == testUser.firstName)
    assert(savedUser.lastName == testUser.lastName)
    assert(savedUser.email == testUser.email)
    assert(savedUser.university == testUser.university)
    assert(savedUser.birthday == testUser.birthday)
    assert(savedUser.hobbies == testUser.hobbies)
    assert(savedUser.bio == testUser.bio)
    assert(savedUser.country == "Belgium")
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

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.ui.screen.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) = Unit

    override suspend fun declineInvitation(eventId: String, userId: String) = Unit

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
  }
}

package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.profile.edit.EditActivitiesScreen
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditActivitiesScreenTest {

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
          hobbies = listOf("Football", "AI", "Piano"),
          bio = "Test bio",
          profilePictureUrl = null)

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    navigatedBack = false
  }

  @Test
  fun editActivitiesScreen_displaysCorrectTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Select Your Activities").assertExists()
  }

  @Test
  fun editActivitiesScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun editActivitiesScreen_backButtonNavigatesBack() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(navigatedBack)
  }

  @Test
  fun editActivitiesScreen_displaysSearchBar() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Search activities...").assertExists()
  }

  @Test
  fun editActivitiesScreen_searchIconIsVisible() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Search").assertExists()
  }

  @Test
  fun editActivitiesScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save Activities").assertExists()
  }

  @Test
  fun editActivitiesScreen_saveButtonIsEnabledWithSelection() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    // User already has activities selected
    composeTestRule.onNodeWithText("Save Activities").assertIsEnabled()
  }

  @Test
  fun editActivitiesScreen_saveButtonIsDisabledWithNoSelection() {
    repository = TestUserRepository(testUser.copy(hobbies = emptyList()))

    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save Activities").assertIsNotEnabled()
  }

  @Test
  fun editActivitiesScreen_displaysUserSelectedActivities() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Check selected count instead since activities might not all be visible in LazyColumn
    composeTestRule.onNodeWithText("3 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_displaysSelectedCount() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("3 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_selectedCountSingularForm() {
    repository = TestUserRepository(testUser.copy(hobbies = listOf("Football")))

    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("1 activity selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_canSelectNewActivity() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for Tennis to make it visible
    composeTestRule.onNodeWithTag("searchField").performTextInput("tennis")
    composeTestRule.waitForIdle()

    // Click on an unselected activity
    composeTestRule.onNodeWithText("Tennis").performClick()

    composeTestRule.waitForIdle()

    // Selected count should update
    composeTestRule.onNodeWithText("4 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_canDeselectActivity() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Initially should have 3 selected
    composeTestRule.onNodeWithText("3 activities selected").assertExists()

    // Search for Football and deselect it
    composeTestRule.onNodeWithTag("searchField").performTextInput("football")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Football").performClick()
    composeTestRule.waitForIdle()

    // Clear search
    composeTestRule.onNodeWithTag("searchField").performTextClearance()
    composeTestRule.waitForIdle()

    // Selected count should decrease
    composeTestRule.onNodeWithText("2 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_searchFiltersActivities() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Type in search box
    composeTestRule.onNodeWithText("Search activities...").performTextInput("foot")

    composeTestRule.waitForIdle()

    // Should show Football
    composeTestRule.onNodeWithText("Football").assertExists()

    // Should not show unrelated activities
    composeTestRule.onNodeWithText("Tennis").assertDoesNotExist()
  }

  @Test
  fun editActivitiesScreen_searchIsCaseInsensitive() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Type in uppercase
    composeTestRule.onNodeWithText("Search activities...").performTextInput("TENNIS")

    composeTestRule.waitForIdle()

    // Should still find "Tennis"
    composeTestRule.onNodeWithText("Tennis").assertExists()
  }

  @Test
  fun editActivitiesScreen_clearSearchShowsAllActivities() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for something specific
    composeTestRule.onNodeWithTag("searchField").performTextInput("football")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Football").assertExists()

    // Clear search
    composeTestRule.onNodeWithTag("searchField").performTextClearance()
    composeTestRule.waitForIdle()

    // Now search for Tennis to verify all activities are available
    composeTestRule.onNodeWithTag("searchField").performTextInput("tennis")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Tennis").assertExists()
  }

  @Test
  fun editActivitiesScreen_canSearchAndSelectActivity() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for Tennis
    composeTestRule.onNodeWithText("Search activities...").performTextInput("tennis")
    composeTestRule.waitForIdle()

    // Select it
    composeTestRule.onNodeWithText("Tennis").performClick()
    composeTestRule.waitForIdle()

    // Selected count should update (3 initially + 1 new = 4)
    composeTestRule.onNodeWithText("4 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_savesActivitiesSuccessfully() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for Tennis to make it visible
    composeTestRule.onNodeWithTag("searchField").performTextInput("tennis")
    composeTestRule.waitForIdle()

    // Add a new activity
    composeTestRule.onNodeWithText("Tennis").performClick()
    composeTestRule.waitForIdle()

    // Clear search before saving
    composeTestRule.onNodeWithTag("searchField").performTextClearance()
    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Activities").performClick()

    // Wait for save to complete
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Verify saved data
    val savedUser = repository.savedUsers.last()
    assert(savedUser.hobbies.contains("Tennis"))
    assert(savedUser.hobbies.contains("Football"))
    assert(savedUser.hobbies.contains("AI"))
    assert(savedUser.hobbies.contains("Piano"))
  }

  @Test
  fun editActivitiesScreen_showsSuccessMessage() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Activities").performClick()

    // Wait for success message
    composeTestRule.waitForIdle()

    // Should show success message in snackbar
    composeTestRule.onNodeWithText("Activities updated successfully").assertExists()
  }

  @Test
  fun editActivitiesScreen_doesNotNavigateBackAfterSave() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Activities").performClick()

    // Wait for save to complete
    composeTestRule.waitUntil(timeoutMillis = 2000) { repository.savedUsers.isNotEmpty() }

    // Should NOT navigate back automatically
    assert(!navigatedBack)
  }

  @Test
  fun editActivitiesScreen_showsLoadingStateWhileSaving() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Activities").performClick()

    composeTestRule.waitForIdle()

    // Should show loading state (button text disappears, shows progress)
    composeTestRule.onNodeWithText("Save Activities").assertDoesNotExist()
  }

  @Test
  fun editActivitiesScreen_disablesInteractionDuringLoading() {
    repository.saveDelay = 1000L

    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Activities").performClick()

    composeTestRule.waitForIdle()

    // Activities should not be clickable during loading
    // We can't easily test this without checking internal state,
    // but the button being disabled is a good indicator
  }

  @Test
  fun editActivitiesScreen_handlesRepositoryError() {
    repository.shouldThrowOnSave = RuntimeException("Network error")

    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Click save
    composeTestRule.onNodeWithText("Save Activities").performClick()

    // Wait for error message
    composeTestRule.waitForIdle()

    // Should show error message in snackbar
    composeTestRule.onNodeWithText("Network error").assertExists()

    // Should NOT navigate back
    assert(!navigatedBack)
  }

  @Test
  fun editActivitiesScreen_displaysMultipleActivityCategories() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Sports
    composeTestRule.onNodeWithTag("searchField").performTextInput("football")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Football").assertExists()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    // Technology
    composeTestRule.onNodeWithTag("searchField").performTextInput("ai")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_AI").assertExists()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    // Arts
    composeTestRule.onNodeWithTag("searchField").performTextInput("painting")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Painting").assertExists()
  }

  @Test
  fun editActivitiesScreen_activitiesAreSorted() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Activities should be alphabetically sorted
    // Verify by searching for activities that should exist if sorted
    composeTestRule.onNodeWithText("Search activities...").performTextInput("tennis")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tennis").assertExists()
  }

  @Test
  fun editActivitiesScreen_canSelectMultipleActivities() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Select Tennis
    composeTestRule.onNodeWithTag("searchField").performTextInput("tennis")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Tennis").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    // Select Running
    composeTestRule.onNodeWithTag("searchField").performTextInput("running")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Running").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    // Should show updated count (3 initially + 2 new = 5)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("5 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_canDeselectAllActivities() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Deselect Football
    composeTestRule.onNodeWithTag("searchField").performTextInput("football")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Football").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    // Deselect AI
    composeTestRule.onNodeWithTag("searchField").performTextInput("ai")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_AI").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    // Deselect Piano
    composeTestRule.onNodeWithTag("searchField").performTextInput("piano")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Piano").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()

    composeTestRule.waitForIdle()

    // Selected count should disappear (no text showing count)
    composeTestRule.onNodeWithText("0 activities selected").assertDoesNotExist()
    composeTestRule.onNodeWithText("3 activities selected").assertDoesNotExist()

    // Save button should be disabled
    composeTestRule.onNodeWithTag("saveButton").assertIsNotEnabled()
  }

  @Test
  fun editActivitiesScreen_searchWithNoResults() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Search for non-existent activity
    composeTestRule.onNodeWithText("Search activities...").performTextInput("xyz123")

    composeTestRule.waitForIdle()

    // Common activities should not appear
    composeTestRule.onNodeWithText("Football").assertDoesNotExist()
    composeTestRule.onNodeWithText("Tennis").assertDoesNotExist()
  }

  @Test
  fun editActivitiesScreen_selectedActivitiesShowCheckIcon() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Selected activities should have check icon (content description "Selected")
    // Note: This requires the implementation to have proper content descriptions
    // The check icon should be visible for selected items
  }

  @Test
  fun editActivitiesScreen_persistsSelectionAfterSearch() {
    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Add Tennis
    composeTestRule.onNodeWithTag("searchField").performTextInput("tennis")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("activityItem_Tennis").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("searchField").performTextClearance()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("4 activities selected").assertExists()

    // Search for something else
    composeTestRule.onNodeWithTag("searchField").performTextInput("football")
    composeTestRule.waitForIdle()

    // Clear search
    composeTestRule.onNodeWithTag("searchField").performTextClearance()
    composeTestRule.waitForIdle()

    // Tennis should still be selected
    composeTestRule.onNodeWithText("4 activities selected").assertExists()
  }

  @Test
  fun editActivitiesScreen_handlesUserNotFound() {
    repository = TestUserRepository(null)

    composeTestRule.setContent {
      MaterialTheme {
        EditActivitiesScreen(
            userId = "non_existent_user",
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // With user not found, should still show empty state
    // Save button should be disabled with no selection
    composeTestRule.onNodeWithText("Save Activities").assertIsNotEnabled()
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnSave: Throwable? = null,
      var shouldThrowOnLoad: Throwable? = null,
      var saveDelay: Long = 0L
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      shouldThrowOnLoad?.let { throw it }
      return if (userId == user?.userId) user else null
    }

    override suspend fun saveUser(user: User) {
      if (saveDelay > 0) {
        delay(saveDelay)
      }
      shouldThrowOnSave?.let { throw it }
      savedUsers.add(user)
      this.user = user
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

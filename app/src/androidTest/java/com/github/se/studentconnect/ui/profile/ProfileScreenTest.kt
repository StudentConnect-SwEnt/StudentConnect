package com.github.se.studentconnect.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.profile.ProfileScreen
import com.github.se.studentconnect.ui.screen.profile.ProfileViewModel
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun profileScreen_showsSnackbarAfterSuccessfulUpdate() {
    val repository = UiFakeUserRepository()
    val viewModel = ProfileViewModel(repository, SAMPLE_USER.userId)

    composeTestRule.setContent {
      ProfileScreen(
          currentUserId = SAMPLE_USER.userId, userRepository = repository, viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.runOnIdle { viewModel.updateBio("Updated bio") }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithText("Bio updated successfully")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Bio updated successfully").assertIsDisplayed()
    assertTrue(repository.savedUsers.isNotEmpty())
    assertEquals("Updated bio", repository.savedUsers.last().bio)
  }

  @Test
  fun profileScreen_displaysEmptyStateForOptionalFields() {
    val userWithEmptyFields =
        SAMPLE_USER.copy(country = null, birthday = null, bio = null, hobbies = emptyList())
    val repository = UiFakeUserRepository(user = userWithEmptyFields)
    val viewModel = ProfileViewModel(repository, userWithEmptyFields.userId)

    composeTestRule.setContent {
      ProfileScreen(
          currentUserId = userWithEmptyFields.userId,
          userRepository = repository,
          viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    // Should show "Not specified" for empty fields
    composeTestRule.onAllNodesWithText("Not specified").fetchSemanticsNodes().size >= 3
  }

  @Test
  fun profileScreen_updateCountrySuccessfully() {
    val repository = UiFakeUserRepository()
    val viewModel = ProfileViewModel(repository, SAMPLE_USER.userId)

    composeTestRule.setContent {
      ProfileScreen(
          currentUserId = SAMPLE_USER.userId, userRepository = repository, viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.runOnIdle { viewModel.updateCountry("France") }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithText("Country updated successfully")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.runOnIdle { assertEquals("France", repository.savedUsers.last().country) }
  }

  @Test
  fun profileScreen_updateActivitiesSuccessfully() {
    val repository = UiFakeUserRepository()
    val viewModel = ProfileViewModel(repository, SAMPLE_USER.userId)

    composeTestRule.setContent {
      ProfileScreen(
          currentUserId = SAMPLE_USER.userId, userRepository = repository, viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.runOnIdle { viewModel.updateActivities("Swimming, Hiking, Reading") }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithText("Activities updated successfully")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.runOnIdle {
      val updatedUser = repository.savedUsers.last()
      assertEquals(listOf("Swimming", "Hiking", "Reading"), updatedUser.hobbies)
    }
  }

  @Test
  fun profileScreen_updateBirthdaySuccessfully() {
    val repository = UiFakeUserRepository()
    val viewModel = ProfileViewModel(repository, SAMPLE_USER.userId)

    composeTestRule.setContent {
      ProfileScreen(
          currentUserId = SAMPLE_USER.userId, userRepository = repository, viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.runOnIdle { viewModel.updateBirthday("15/08/1990") }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithText("Birthday updated successfully")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.runOnIdle { assertEquals("15/08/1990", repository.savedUsers.last().birthday) }
  }

  @Test
  fun profileScreen_showsLoadingInitially() {
    val repository = UiFakeUserRepository()
    val viewModel = ProfileViewModel(repository, SAMPLE_USER.userId)

    composeTestRule.setContent {
      ProfileScreen(
          currentUserId = SAMPLE_USER.userId, userRepository = repository, viewModel = viewModel)
    }

    // Should show loading indicator or user data
    // This test ensures the screen renders without crashing
    composeTestRule.waitForIdle()
  }

  private class UiFakeUserRepository(private var user: User = SAMPLE_USER) : UserRepository {

    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      delay(50)
      return user
    }

    override suspend fun saveUser(user: User) {
      delay(50)
      savedUsers += user
      this.user = user
    }

    override suspend fun leaveEvent(eventId: String, userId: String) = unsupported()

    override suspend fun getUserByEmail(email: String) = unsupported()

    override suspend fun getAllUsers(): List<User> = unsupported()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) = unsupported()

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = unsupported()

    override suspend fun deleteUser(userId: String) = unsupported()

    override suspend fun getUsersByUniversity(university: String): List<User> = unsupported()

    override suspend fun getUsersByHobby(hobby: String): List<User> = unsupported()

    override suspend fun getNewUid(): String = unsupported()

    override suspend fun getJoinedEvents(userId: String): List<String> = unsupported()

    override suspend fun addEventToUser(eventId: String, userId: String) = unsupported()

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) =
        unsupported()

    override suspend fun getInvitations(userId: String): List<Invitation> = unsupported()

    override suspend fun acceptInvitation(eventId: String, userId: String) = unsupported()

    override suspend fun declineInvitation(eventId: String, userId: String) = unsupported()

    override suspend fun joinEvent(eventId: String, userId: String) = unsupported()

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) =
        unsupported()

    override suspend fun addFavoriteEvent(userId: String, eventId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun getFavoriteEvents(userId: String): List<String> {
      TODO("Not yet implemented")
    }

    private fun unsupported(): Nothing =
        throw UnsupportedOperationException("Not required for test")
  }

  companion object {
    private val SAMPLE_USER =
        User(
            userId = "user-123",
            email = "john.doe@epfl.ch",
            firstName = "John",
            lastName = "Doe",
            university = "EPFL",
            hobbies = listOf("Running", "Coding"),
            profilePictureUrl = null,
            bio = "Love building things",
            country = "Switzerland",
            birthday = "04/05/1998",
            createdAt = 1_000L,
            updatedAt = 2_000L)
  }
}

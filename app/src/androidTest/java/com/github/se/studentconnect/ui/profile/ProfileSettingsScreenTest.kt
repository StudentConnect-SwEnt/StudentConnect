package com.github.se.studentconnect.ui.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.profile.ProfileSettingsScreen
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileSettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestUserRepository
  private val testUser =
      User(
          userId = "test_user",
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
          hobbies = listOf("Reading", "Coding"),
          bio = "Test bio")

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
  }

  @Test
  fun profileSettingsScreen_displaysUserName() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("John Doe", useUnmergedTree = true).assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysProfilePicture() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysEditPictureButton() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Edit Profile Picture").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysEditNameButton() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Edit Name").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysUniversityField() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("University").assertExists()
    composeTestRule.onNodeWithText("EPFL").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysCountryField() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Country").assertExists()
    composeTestRule.onNodeWithText("Switzerland").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysBirthdayField() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Birthday").assertExists()
    composeTestRule.onNodeWithText("01/01/2000").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysActivitiesField() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Favourite Activities").assertExists()
    composeTestRule.onNodeWithText("Reading, Coding").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysBioField() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("More About Me").assertExists()
    composeTestRule.onNodeWithText("Test bio").assertExists()
  }

  @Test
  fun profileSettingsScreen_showsLoadingState() {
    repository.getDelay = 500L

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule
        .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
        .assertExists()
  }

  @Test
  fun profileSettingsScreen_editPictureButtonCallsCallback() {
    var callbackCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(
            currentUserId = testUser.userId,
            userRepository = repository,
            onNavigateToEditPicture = { callbackCalled = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Edit Profile Picture").performClick()
    assert(callbackCalled)
  }

  @Test
  fun profileSettingsScreen_editNameButtonCallsCallback() {
    var callbackCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(
            currentUserId = testUser.userId,
            userRepository = repository,
            onNavigateToEditName = { callbackCalled = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Edit Name").performClick()
    assert(callbackCalled)
  }

  @Test
  fun profileSettingsScreen_handlesNullCountry() {
    val userWithoutCountry = testUser.copy(country = null)
    repository = TestUserRepository(userWithoutCountry)

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Country").assertExists()
  }

  @Test
  fun profileSettingsScreen_handlesNullBirthday() {
    val userWithoutBirthday = testUser.copy(birthday = null)
    repository = TestUserRepository(userWithoutBirthday)

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Birthday").assertExists()
  }

  @Test
  fun profileSettingsScreen_handlesNullBio() {
    val userWithoutBio = testUser.copy(bio = null)
    repository = TestUserRepository(userWithoutBio)

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("More About Me").assertExists()
  }

  @Test
  fun profileSettingsScreen_handlesEmptyHobbies() {
    val userWithoutHobbies = testUser.copy(hobbies = emptyList())
    repository = TestUserRepository(userWithoutHobbies)

    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Favourite Activities").assertExists()
  }

  @Test
  fun profileSettingsScreen_displaysAllFieldsSimultaneously() {
    composeTestRule.setContent {
      MaterialTheme {
        ProfileSettingsScreen(currentUserId = testUser.userId, userRepository = repository)
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("University").assertExists()
    composeTestRule.onNodeWithText("Country").assertExists()
    composeTestRule.onNodeWithText("Birthday").assertExists()
    composeTestRule.onNodeWithText("Favourite Activities").assertExists()
    composeTestRule.onNodeWithText("More About Me").assertExists()
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnGet: Throwable? = null,
      var getDelay: Long = 0L
  ) : UserRepository {

    override suspend fun getUserById(userId: String): User? {
      if (getDelay > 0) delay(getDelay)
      shouldThrowOnGet?.let { throw it }
      return if (userId == user?.userId) user else null
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

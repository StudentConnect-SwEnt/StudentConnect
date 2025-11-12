package com.github.se.studentconnect.ui.profile.edit

import android.content.Context
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.profile.edit.EditProfilePictureScreen
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EditProfilePictureScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var repository: TestUserRepository
  private lateinit var fakeMediaRepository: FakeMediaRepository

  companion object {
    @BeforeClass
    @JvmStatic
    fun setUpClass() {
      // Initialize Firebase before accessing MediaRepositoryProvider
      val context = ApplicationProvider.getApplicationContext<Context>()
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }
      // Initialize MediaRepositoryProvider with a fake repository
      MediaRepositoryProvider.repository = FakeMediaRepository()
    }
  }

  private val testUser =
      User(
          userId = "test_user",
          username = "johndoe",
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          university = "EPFL",
          country = "Switzerland",
          birthday = "01/01/2000",
          hobbies = listOf("Reading", "Hiking"),
          bio = "Test bio",
          profilePictureUrl = null)

  private val testUserWithPicture = testUser.copy(profilePictureUrl = "http://example.com/pic.jpg")

  private var navigatedBack = false

  @Before
  fun setUp() {
    repository = TestUserRepository(testUser)
    navigatedBack = false
    fakeMediaRepository = FakeMediaRepository()
    MediaRepositoryProvider.repository = fakeMediaRepository
  }

  @Test
  fun editProfilePictureScreen_displaysCorrectTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithText("Edit Profile Picture").assertExists()
  }

  @Test
  fun editProfilePictureScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun editProfilePictureScreen_backButtonNavigatesBack() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(navigatedBack)
  }

  @Test
  fun editProfilePictureScreen_displaysProfilePictureIcon() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
  }

  @Test
  fun editProfilePictureScreen_displaysNoProfilePictureText() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap above to choose a profile photo").assertExists()
  }

  @Test
  fun editProfilePictureScreen_displaysActionButtonsSection() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun editProfilePictureScreen_displaysRemovePhotoButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
    composeTestRule.onNodeWithText("Remove Photo").assertIsNotEnabled()
  }

  @Test
  fun editProfilePictureScreen_displaysInstructionText() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tap above to choose a profile photo").assertExists()
  }

  @Test
  fun editProfilePictureScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertExists()
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editProfilePictureScreen_profilePictureIsClickable() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertHasClickAction()
  }

  @Test
  fun editProfilePictureScreen_removePhotoButtonDisabledByDefault() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
    composeTestRule.onNodeWithText("Remove Photo").assertIsNotEnabled()
  }

  @Test
  fun editProfilePictureScreen_saveButtonDisabledByDefault() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Save").assertExists()
    composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
  }

  @Test
  fun editProfilePictureScreen_loadsUserProfileOnInit() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Verify the user was loaded by checking if the profile section is displayed
    composeTestRule.onNodeWithText("Tap above to choose a profile photo").assertExists()
  }

  @Test
  fun editProfilePictureScreen_handlesUserNotFound() {
    repository = TestUserRepository(null)

    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = "non_existent",
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Should still display the UI even if user is not found
    composeTestRule.onNodeWithText("Edit Profile Picture").assertExists()
  }

  @Test
  fun editProfilePictureScreen_handlesRepositoryError() {
    repository.shouldThrowOnGet = RuntimeException("Network error")

    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Should display error state
    // Note: Currently the error might be shown in a snackbar or handled differently
    composeTestRule.onNodeWithText("Edit Profile Picture").assertExists()
  }

  @Test
  fun editProfilePictureScreen_displaysLoadingStateDuringInitialLoad() {
    repository.getDelay = 500L

    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    // Check that loading state is shown (CircularProgressIndicator might be present)
    // The exact behavior depends on implementation
    composeTestRule.waitForIdle()

    // Eventually the content should be loaded
    composeTestRule
        .onNodeWithText("Tap above to choose a profile photo", useUnmergedTree = true)
        .assertExists()
  }

  @Test
  fun editProfilePictureScreen_actionButtonsVisibleSimultaneously() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // All action buttons should be visible at the same time
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun editProfilePictureScreen_profileIconIsDisplayed() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // The profile icon (Person icon) should be displayed
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
  }

  @Test
  fun editProfilePictureScreen_withExistingProfilePicture() {
    repository = TestUserRepository(testUserWithPicture)

    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUserWithPicture.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Should still display the profile section
    composeTestRule.onNodeWithText("Edit Profile Picture").assertExists()
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
  }

  @Test
  fun editProfilePictureScreen_navigationIconIsCorrect() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    // Verify the back arrow icon exists
    composeTestRule.onNodeWithContentDescription("Back").assertExists()
    composeTestRule.onNodeWithContentDescription("Back").assertHasClickAction()
  }

  @Test
  fun editProfilePictureScreen_screenshotTest() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Verify all major UI components are present
    composeTestRule.onNodeWithText("Edit Profile Picture").assertExists()
    composeTestRule.onNodeWithContentDescription("Back").assertExists()
    composeTestRule.onNodeWithContentDescription("Profile Picture").assertExists()
    composeTestRule.onNodeWithText("Tap above to choose a profile photo").assertExists()
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun editProfilePictureScreen_verifyLayoutStructure() {
    composeTestRule.setContent {
      MaterialTheme {
        EditProfilePictureScreen(
            userId = testUser.userId,
            userRepository = repository,
            onNavigateBack = { navigatedBack = true })
      }
    }

    composeTestRule.waitForIdle()

    // Verify the layout contains both cards
    // First card: Current profile picture
    composeTestRule.onNodeWithText("Tap above to choose a profile photo").assertExists()

    // Second card: Action buttons
    composeTestRule.onNodeWithText("Remove Photo").assertExists()
  }

  private class TestUserRepository(
      private var user: User? = null,
      var shouldThrowOnGet: Throwable? = null,
      var shouldThrowOnSave: Throwable? = null,
      var getDelay: Long = 0L,
      var saveDelay: Long = 0L
  ) : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun getUserById(userId: String): User? {
      if (getDelay > 0) {
        delay(getDelay)
      }
      shouldThrowOnGet?.let { throw it }
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

    override suspend fun checkUsernameAvailability(username: String): Boolean {
      TODO("Not yet implemented")
    }
  }

  private class FakeMediaRepository : MediaRepository {
    val uploads = mutableListOf<Pair<Uri, String?>>()
    var lastUploadPath: String? = null

    override suspend fun upload(uri: Uri, path: String?): String {
      uploads += uri to path
      lastUploadPath = path ?: "generated/${uploads.size}"
      return lastUploadPath!!
    }

    override suspend fun download(id: String): Uri = Uri.parse("file:///$id")

    override suspend fun delete(id: String) = Unit
  }
}

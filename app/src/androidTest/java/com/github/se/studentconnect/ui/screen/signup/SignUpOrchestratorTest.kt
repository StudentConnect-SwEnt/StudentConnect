package com.github.se.studentconnect.ui.screen.signup

import android.net.Uri
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.Invitation
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class SignUpOrchestratorTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

  private lateinit var originalMediaRepository: MediaRepository
  private lateinit var fakeMediaRepository: FakeMediaRepository

  @Before
  fun setup() {
    originalMediaRepository = MediaRepositoryProvider.repository
    fakeMediaRepository = FakeMediaRepository()
    MediaRepositoryProvider.repository = fakeMediaRepository
  }

  @After
  fun tearDown() {
    MediaRepositoryProvider.repository = originalMediaRepository
  }

  @Test
  fun orchestrator_savesUserProfile_whenStartClicked() {
    val firebaseUserId = "test-user-id"
    val email = "test@example.com"
    val profileUri = Uri.parse("file:///tmp/profile.png")

    val viewModel =
        SignUpViewModel().apply {
          setFirstName("Ada")
          setLastName("Lovelace")
          setBio("Hello world")
          setNationality("CH")
          setProfilePictureUri(profileUri)
          goTo(SignUpStep.Experiences)
        }

    val fakeRepository = FakeUserRepository()
    var completedUser: User? = null

    composeTestRule.setContent {
      SignUpOrchestrator(
          firebaseUserId = firebaseUserId,
          email = email,
          userRepository = fakeRepository,
          onSignUpComplete = { completedUser = it },
          signUpViewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Bowling").performClick()
    composeTestRule.onNodeWithText("Start Now").assertExists().performClick()

    composeTestRule.waitUntil(timeoutMillis = 5_000) { fakeRepository.savedUsers.isNotEmpty() }

    val savedUser = fakeRepository.savedUsers.single()
    assertEquals(firebaseUserId, savedUser.userId)
    assertEquals(email, savedUser.email)
    assertEquals(listOf("Bowling"), savedUser.hobbies)
    assertEquals(fakeMediaRepository.lastUploadPath, savedUser.profilePictureUrl)
    assertSame(savedUser, completedUser)
    assertTrue(fakeMediaRepository.uploads.isNotEmpty())
    assertEquals(profileUri, fakeMediaRepository.uploads.single().first)
    assertEquals("users/$firebaseUserId/profile", fakeMediaRepository.lastUploadPath)
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

  private class FakeUserRepository : UserRepository {
    val savedUsers = mutableListOf<User>()

    override suspend fun saveUser(user: User) {
      savedUsers += user
    }

    override suspend fun leaveEvent(eventId: String, userId: String) = Unit

    override suspend fun getUserById(userId: String) = null

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<User>() to false

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) = Unit

    override suspend fun deleteUser(userId: String) = Unit

    override suspend fun getUsersByUniversity(university: String) = emptyList<User>()

    override suspend fun getUsersByHobby(hobby: String) = emptyList<User>()

    override suspend fun getNewUid() = "generated-id"

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

    override suspend fun addFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) = Unit

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String): Boolean {
      TODO("Not yet implemented")
    }
  }
}

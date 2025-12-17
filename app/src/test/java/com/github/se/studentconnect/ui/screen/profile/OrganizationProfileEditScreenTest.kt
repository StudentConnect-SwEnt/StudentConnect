package com.github.se.studentconnect.ui.screen.profile

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationMemberInvitation
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.organization.SocialLinks
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationProfileEditScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var organizationRepository: TestOrganizationRepository
  private lateinit var userRepository: TestUserRepository
  private lateinit var mediaRepository: TestMediaRepository

  private val testOrgId = "test_org_123"
  private val testUserId = "test_user_123"
  private val otherUserId = "other_user_456"

  private val testUser =
      User(
          userId = testUserId,
          firstName = "Test",
          lastName = "User",
          email = "test@example.com",
          username = "testuser",
          university = "EPFL")

  private val otherUser =
      User(
          userId = otherUserId,
          firstName = "Other",
          lastName = "User",
          email = "other@example.com",
          username = "otheruser",
          university = "EPFL")

  private val testOrganization =
      Organization(
          id = testOrgId,
          name = "Test Organization",
          type = OrganizationType.StudentClub,
          description = "Test description",
          logoUrl = null,
          location = "EPFL",
          socialLinks =
              SocialLinks(
                  website = "https://test.com",
                  instagram = "@testorg",
                  x = "@testorg",
                  linkedin = "testorg"),
          memberUids = listOf(testUserId, otherUserId),
          memberRoles = mapOf(otherUserId to "President"),
          createdBy = testUserId,
          createdAt = Timestamp.now())

  private var backPressed = false

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    organizationRepository = TestOrganizationRepository(listOf(testOrganization))
    userRepository = TestUserRepository(listOf(testUser, otherUser))
    mediaRepository = TestMediaRepository()

    OrganizationRepositoryProvider.overrideForTests(organizationRepository)
    UserRepositoryProvider.overrideForTests(userRepository)
    MediaRepositoryProvider.overrideForTests(mediaRepository)

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId

    backPressed = false
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    OrganizationRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()
    MediaRepositoryProvider.cleanOverrideForTests()
    unmockkAll()
  }

  @Test
  fun organizationProfileEditScreen_displaysTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Edit Organization").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_displaysBackButton() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_backButtonWorks() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Back").performClick()

    assertTrue(backPressed)
  }

  @Test
  fun organizationProfileEditScreen_displaysSaveButton() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_displaysBasicInfoSection() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Basic Information").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_displaysOrganizationName() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Test Organization").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_nameFieldIsEditable() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Test Organization").performTextReplacement("New Name")

    composeTestRule.onNodeWithText("New Name").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_displaysLogoSection() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Change Logo").assertExists()
  }

  @Test
  fun organizationProfileEditScreen_savesChanges() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Make a change
    composeTestRule
        .onNodeWithText("Test Organization")
        .performTextReplacement("Updated Organization")

    // Save
    composeTestRule.onNodeWithText("Save").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify save was called
    assertNotNull(organizationRepository.savedOrganization)
    assertEquals("Updated Organization", organizationRepository.savedOrganization?.name)
  }

  @Test
  fun organizationProfileEditScreen_showsErrorForEmptyName() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Clear name
    composeTestRule.onNodeWithText("Test Organization").performTextClearance()

    // Try to save
    composeTestRule.onNodeWithText("Save").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should show error
    composeTestRule.onNode(hasText("cannot be empty", substring = true)).assertExists()
  }

  @Test
  fun organizationProfileEditScreen_displaysSuccessSnackbar() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Make a valid change
    composeTestRule.onNodeWithText("Test Organization").performTextReplacement("Valid Name")

    // Save
    composeTestRule.onNodeWithText("Save").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should show success message
    composeTestRule
        .onNode(hasText("successfully", substring = true, ignoreCase = true))
        .assertExists()
  }

  @Test
  fun organizationProfileEditScreen_showsErrorWhenOrganizationNotFound() {
    val emptyRepo = TestOrganizationRepository(emptyList())
    OrganizationRepositoryProvider.overrideForTests(emptyRepo)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(
            organizationId = "nonexistent", onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should show error snackbar for organization not found
    composeTestRule.onNode(hasText("Organization not found", substring = true)).assertExists()
  }

  @Test
  fun organizationProfileEditScreen_handlesNonOwnerAccess() {
    every { AuthenticationProvider.currentUser } returns otherUserId

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should show error for non-owner access
    composeTestRule
        .onNode(hasText("Only the owner can edit this organization", substring = true))
        .assertExists()
  }

  @Test
  fun organizationProfileEditScreen_handlesTrimmedName() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Add whitespace to name
    composeTestRule.onNodeWithText("Test Organization").performTextReplacement("  Valid Name  ")

    // Save
    composeTestRule.onNodeWithText("Save").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should trim and save
    assertNotNull(organizationRepository.savedOrganization)
    assertEquals("Valid Name", organizationRepository.savedOrganization?.name)
  }

  @Test
  fun organizationProfileEditScreen_handlesSaveWithoutChanges() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Save without making changes
    composeTestRule.onNodeWithText("Save").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should still save successfully
    assertNotNull(organizationRepository.savedOrganization)
  }

  @Test
  fun organizationProfileEditScreen_handlesLongOrganizationName() {
    composeTestRule.setContent {
      MaterialTheme {
        OrganizationProfileEditScreen(organizationId = testOrgId, onBack = { backPressed = true })
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Enter a name that exceeds the limit
    val longName = "a".repeat(201)
    composeTestRule.onNodeWithText("Test Organization").performTextReplacement(longName)

    // Try to save
    composeTestRule.onNodeWithText("Save").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should show error
    composeTestRule.onNode(hasText("exceeds", substring = true)).assertExists()
  }

  // Test helper repositories
  private class TestOrganizationRepository(var organizations: List<Organization>) :
      OrganizationRepository {

    var savedOrganization: Organization? = null

    override suspend fun saveOrganization(organization: Organization) {
      savedOrganization = organization
      organizations = organizations.map { if (it.id == organization.id) organization else it }
    }

    override suspend fun getOrganizationById(organizationId: String): Organization? {
      return organizations.find { it.id == organizationId }
    }

    override suspend fun getAllOrganizations(): List<Organization> = organizations

    override suspend fun getNewOrganizationId(): String = "new_org_id"

    override suspend fun sendMemberInvitation(
        organizationId: String,
        userId: String,
        role: String,
        invitedBy: String
    ) {}

    override suspend fun acceptMemberInvitation(organizationId: String, userId: String) {}

    override suspend fun rejectMemberInvitation(organizationId: String, userId: String) {}

    override suspend fun getPendingInvitations(
        organizationId: String
    ): List<OrganizationMemberInvitation> = emptyList()

    override suspend fun getUserPendingInvitations(
        userId: String
    ): List<OrganizationMemberInvitation> = emptyList()

    override suspend fun addMemberToOrganization(organizationId: String, userId: String) {}
  }

  private class TestUserRepository(var users: List<User>) : UserRepository {

    override suspend fun getUserById(userId: String): User? = users.find { it.userId == userId }

    override suspend fun getUserByEmail(email: String): User? = null

    override suspend fun getAllUsers(): List<User> = users

    override suspend fun getUsersPaginated(
        limit: Int,
        lastUserId: String?
    ): Pair<List<User>, Boolean> = Pair(users, false)

    override suspend fun saveUser(user: User) {}

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

    override suspend fun deleteUser(userId: String) {}

    override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

    override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

    override suspend fun getNewUid(): String = "new_uid"

    override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

    override suspend fun addEventToUser(eventId: String, userId: String) {}

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {}

    override suspend fun getInvitations(
        userId: String
    ): List<com.github.se.studentconnect.model.activities.Invitation> = emptyList()

    override suspend fun acceptInvitation(eventId: String, userId: String) {}

    override suspend fun declineInvitation(eventId: String, userId: String) {}

    override suspend fun removeInvitation(eventId: String, userId: String) {}

    override suspend fun joinEvent(eventId: String, userId: String) {}

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {}

    override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

    override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

    override suspend fun addPinnedEvent(userId: String, eventId: String) {}

    override suspend fun removePinnedEvent(userId: String, eventId: String) {}

    override suspend fun getPinnedEvents(userId: String): List<String> = emptyList()

    override suspend fun checkUsernameAvailability(username: String): Boolean = true

    override suspend fun pinOrganization(userId: String, organizationId: String) {}

    override suspend fun unpinOrganization(userId: String) {}

    override suspend fun getPinnedOrganization(userId: String): String? = null

    override suspend fun leaveEvent(eventId: String, userId: String) {}
  }

  private class TestMediaRepository : MediaRepository {
    var uploadCalled = false

    override suspend fun upload(uri: Uri, path: String?): String {
      uploadCalled = true
      return "new_logo_id"
    }

    override suspend fun download(id: String): Uri {
      throw Exception("Download not implemented in test")
    }

    override suspend fun delete(id: String) {}
  }
}

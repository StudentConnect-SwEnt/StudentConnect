package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationMemberInvitation
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.profile.OrganizationManagementViewModel
import com.google.firebase.Timestamp
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
class OrganizationManagementScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var repository: TestOrganizationRepository
  private lateinit var userRepository: TestUserRepository
  private val testUserId = "test_user_123"

  private val testOrganization1 =
      Organization(
          id = "org1",
          name = "AGEPoly",
          type = OrganizationType.StudentClub,
          description = "Student club",
          memberUids = listOf(testUserId, "user2"),
          createdBy = "user2",
          createdAt = Timestamp.now())

  private val testOrganization2 =
      Organization(
          id = "org2",
          name = "SwEnt Team",
          type = OrganizationType.Company,
          description = "Software engineering team",
          memberUids = listOf("user3"),
          createdBy = testUserId,
          createdAt = Timestamp.now())

  private var backPressed = false
  private var createOrganizationPressed = false
  private var joinOrganizationPressed = false
  private var clickedOrganizationId: String? = null

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)
    repository = TestOrganizationRepository(listOf(testOrganization1, testOrganization2))
    userRepository = TestUserRepository()
    backPressed = false
    createOrganizationPressed = false
    joinOrganizationPressed = false
    clickedOrganizationId = null
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun organizationManagementScreen_displaysTitle() {
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("My Organizations").assertExists()
  }

  @Test
  fun organizationManagementScreen_displaysBackButton() {
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").assertExists()
  }

  @Test
  fun organizationManagementScreen_backButtonWorks() {
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithContentDescription("Back").performClick()

    assertTrue(backPressed)
  }

  // Note: LazyColumn items tests are removed as they don't render reliably in unit tests
  // The ViewModel tests provide comprehensive coverage of the business logic

  @Test
  fun organizationManagementScreen_emptyState_displaysCorrectMessage() {
    val emptyRepository = TestOrganizationRepository(emptyList())
    val viewModel =
        OrganizationManagementViewModel(
            userId = "no_orgs_user",
            organizationRepository = emptyRepository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = "no_orgs_user",
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("No Organizations Yet").assertExists()
    composeTestRule
        .onNodeWithText(
            "You're not a member of any organizations. Create one or join an existing one!")
        .assertExists()
  }

  @Test
  fun organizationManagementScreen_emptyState_hasCreateAndJoinButtons() {
    val emptyRepository = TestOrganizationRepository(emptyList())
    val viewModel =
        OrganizationManagementViewModel(
            userId = "no_orgs_user",
            organizationRepository = emptyRepository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = "no_orgs_user",
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            onJoinOrganization = { joinOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Create Organization").assertExists()
    composeTestRule.onNode(hasText("Join Organization", substring = false)).assertExists()
  }

  @Test
  fun organizationManagementScreen_emptyState_createButtonWorks() {
    val emptyRepository = TestOrganizationRepository(emptyList())
    val viewModel =
        OrganizationManagementViewModel(
            userId = "no_orgs_user",
            organizationRepository = emptyRepository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = "no_orgs_user",
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Create Organization").performClick()

    assertTrue(createOrganizationPressed)
  }

  @Test
  fun organizationManagementScreen_errorState_displaysError() {
    val errorRepository = TestOrganizationRepository(emptyList(), shouldThrowError = true)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = errorRepository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    composeTestRule.onNode(hasText("Error:", substring = true)).assertExists()
    composeTestRule.onNodeWithText("Retry").assertExists()
  }

  @Test
  fun organizationManagementScreen_errorState_retryButtonWorks() {
    val errorRepository = TestOrganizationRepository(emptyList(), shouldThrowError = true)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = errorRepository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = { backPressed = true },
            onCreateOrganization = { createOrganizationPressed = true },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify error is displayed
    composeTestRule.onNode(hasText("Error:", substring = true)).assertExists()

    // Fix the repository and retry
    errorRepository.shouldThrowError = false
    errorRepository.organizations = listOf(testOrganization1)

    composeTestRule.onNodeWithText("Retry").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Should now show organizations
    composeTestRule.onNodeWithText("AGEPoly").assertExists()
  }

  @Test
  fun organizationCard_displaysWithNullLogoUrl() {
    val orgWithoutLogo =
        Organization(
            id = "org_no_logo",
            name = "No Logo Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now(),
            logoUrl = null)

    repository.organizations = listOf(orgWithoutLogo)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify organization card displays with Business icon fallback (logoUrl is null)
    composeTestRule.onNodeWithText("No Logo Org").assertExists()
    // Business icon should be displayed as fallback
    composeTestRule.onNodeWithContentDescription("No Logo Org").assertExists()
  }

  @Test
  fun organizationCard_displaysWithLogoUrl() {
    val orgWithLogo =
        Organization(
            id = "org_with_logo",
            name = "Logo Org",
            type = OrganizationType.Company,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now(),
            logoUrl = "test_logo_id")

    repository.organizations = listOf(orgWithLogo)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify organization card displays (logo download may fail in test, but component renders)
    composeTestRule.onNodeWithText("Logo Org").assertExists()
    composeTestRule.onNodeWithContentDescription("Logo Org").assertExists()
  }

  @Test
  fun organizationCard_handlesLogoDownloadFailure() {
    val orgWithInvalidLogo =
        Organization(
            id = "org_failed_logo",
            name = "Failed Logo Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now(),
            logoUrl = "invalid_logo_id_that_will_fail")

    repository.organizations = listOf(orgWithInvalidLogo)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify organization card falls back to Business icon when logo download fails
    // (logoBitmap will be null, so Business icon is shown)
    composeTestRule.onNodeWithText("Failed Logo Org").assertExists()
    composeTestRule.onNodeWithContentDescription("Failed Logo Org").assertExists()
  }

  @Test
  fun organizationCard_displaysOrganizationInfo() {
    val orgWithDetails =
        Organization(
            id = "org_details",
            name = "Detailed Org",
            type = OrganizationType.Company,
            description = "Test description",
            memberUids = listOf(testUserId, "user2", "user3"),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(orgWithDetails)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify organization details are displayed
    composeTestRule.onNodeWithText("Detailed Org").assertExists()
    composeTestRule.onNodeWithText("Company").assertExists()
    // Member count should be displayed
    composeTestRule.onNodeWithText("3").assertExists()
  }

  @Test
  fun organizationCard_isClickable() {
    val org =
        Organization(
            id = "org_clickable",
            name = "Clickable Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(org)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            onOrganizationClick = { clickedOrganizationId = it },
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Click on the organization card
    composeTestRule.onNodeWithText("Clickable Org").performClick()

    // Verify callback was called with correct organization ID
    assertEquals("org_clickable", clickedOrganizationId)
  }

  @Test
  fun organizationCard_displaysPinButton() {
    val org =
        Organization(
            id = "org_pin",
            name = "Pin Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(org)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify pin button is displayed
    composeTestRule.onNodeWithContentDescription("Pin organization").assertExists()
  }

  @Test
  fun organizationCard_displaysUnpinButtonWhenPinned() = runTest {
    val org =
        Organization(
            id = "org_pinned",
            name = "Pinned Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    // Pin the organization
    userRepository.pinOrganization(testUserId, "org_pinned")

    repository.organizations = listOf(org)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify unpin button is displayed (filled pin icon)
    composeTestRule.onNodeWithContentDescription("Unpin organization").assertExists()
  }

  @Test
  fun organizationCard_pinButtonIsClickable() {
    val org =
        Organization(
            id = "org_toggle_pin",
            name = "Toggle Pin Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(org)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Click pin button
    composeTestRule.onNodeWithContentDescription("Pin organization").performClick()

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify pin button changed to unpin (filled icon)
    composeTestRule.onNodeWithContentDescription("Unpin organization").assertExists()
  }

  @Test
  fun organizationCard_handlesDifferentOrganizationTypes() {
    val studentClub =
        Organization(
            id = "org_student",
            name = "Student Club",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    val company =
        Organization(
            id = "org_company",
            name = "Company",
            type = OrganizationType.Company,
            description = "Test description",
            memberUids = listOf(testUserId),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(studentClub, company)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify both organization types are displayed correctly
    composeTestRule.onNodeWithText("Student Club").assertExists()
    composeTestRule.onNodeWithText("StudentClub").assertExists()
    composeTestRule.onNodeWithText("Company").assertExists()
  }

  @Test
  fun organizationCard_handlesEmptyMemberList() {
    val orgNoMembers =
        Organization(
            id = "org_no_members",
            name = "No Members Org",
            type = OrganizationType.StudentClub,
            description = "Test description",
            memberUids = emptyList(),
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(orgNoMembers)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify organization card handles empty member list
    composeTestRule.onNodeWithText("No Members Org").assertExists()
    composeTestRule.onNodeWithText("0").assertExists()
  }

  @Test
  fun organizationCard_handlesLargeMemberCount() {
    val orgManyMembers =
        Organization(
            id = "org_many",
            name = "Large Org",
            type = OrganizationType.Company,
            description = "Test description",
            memberUids = List(100) { "user$it" },
            createdBy = testUserId,
            createdAt = Timestamp.now())

    repository.organizations = listOf(orgManyMembers)
    val viewModel =
        OrganizationManagementViewModel(
            userId = testUserId,
            organizationRepository = repository,
            userRepository = userRepository)

    composeTestRule.setContent {
      MaterialTheme {
        OrganizationManagementScreen(
            currentUserId = testUserId,
            onBack = {},
            onCreateOrganization = {},
            viewModel = viewModel)
      }
    }

    testDispatcher.scheduler.advanceUntilIdle()
    composeTestRule.waitForIdle()

    // Verify organization card handles large member counts
    composeTestRule.onNodeWithText("Large Org").assertExists()
    composeTestRule.onNodeWithText("100").assertExists()
  }

  // Test helper repository
  private class TestOrganizationRepository(
      var organizations: List<Organization>,
      var shouldThrowError: Boolean = false
  ) : OrganizationRepository {

    override suspend fun saveOrganization(organization: Organization) {
      organizations = organizations + organization
    }

    override suspend fun getOrganizationById(organizationId: String): Organization? {
      return organizations.find { it.id == organizationId }
    }

    override suspend fun getAllOrganizations(): List<Organization> {
      if (shouldThrowError) {
        throw Exception("Test error loading organizations")
      }
      return organizations
    }

    override suspend fun getNewOrganizationId(): String {
      return "new_org_id"
    }

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

  // Mock UserRepository
  private class TestUserRepository : UserRepository {
    override suspend fun leaveEvent(eventId: String, userId: String) {}

    override suspend fun getUserById(userId: String) = null

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers(): List<User> = emptyList()

    override suspend fun getUsersPaginated(
        limit: Int,
        lastUserId: String?
    ): Pair<List<User>, Boolean> = Pair(emptyList(), false)

    override suspend fun saveUser(user: User) {}

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

    override suspend fun deleteUser(userId: String) {}

    override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

    override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

    override suspend fun getNewUid() = "new_uid"

    override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

    override suspend fun addEventToUser(eventId: String, userId: String) {}

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {}

    override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

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

    override suspend fun checkUsernameAvailability(username: String) = true

    override suspend fun pinOrganization(userId: String, organizationId: String) {}

    override suspend fun unpinOrganization(userId: String) {}

    override suspend fun getPinnedOrganization(userId: String): String? = null
  }
}

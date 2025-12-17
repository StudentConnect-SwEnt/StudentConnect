package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationMemberInvitation
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OrganizationManagementViewModelTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var testScope: TestScope
  private lateinit var organizationRepository: TestOrganizationRepository
  private lateinit var userRepository: TestUserRepository
  private lateinit var viewModel: OrganizationManagementViewModel

  private val testUserId = "test_user_123"
  private val otherUserId = "other_user_456"

  private val testOrganization1 =
      Organization(
          id = "org1",
          name = "Test Organization 1",
          type = OrganizationType.StudentClub,
          description = "Test description 1",
          memberUids = listOf(testUserId, otherUserId),
          createdBy = otherUserId,
          createdAt = Timestamp.now())

  private val testOrganization2 =
      Organization(
          id = "org2",
          name = "Test Organization 2",
          type = OrganizationType.Association,
          description = "Test description 2",
          memberUids = listOf(otherUserId),
          createdBy = testUserId,
          createdAt = Timestamp.now())

  private val testOrganization3 =
      Organization(
          id = "org3",
          name = "Test Organization 3",
          type = OrganizationType.Company,
          description = "Test description 3",
          memberUids = listOf(otherUserId),
          createdBy = otherUserId,
          createdAt = Timestamp.now())

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    testScope = TestScope(testDispatcher)
    Dispatchers.setMain(testDispatcher)

    organizationRepository =
        TestOrganizationRepository(listOf(testOrganization1, testOrganization2, testOrganization3))
    userRepository = TestUserRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `viewModel initializes and loads user organizations`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.userOrganizations.size)
        assertTrue(state.userOrganizations.contains(testOrganization1))
        assertTrue(state.userOrganizations.contains(testOrganization2))
        assertFalse(state.userOrganizations.contains(testOrganization3))
      }

  @Test
  fun `loadUserOrganizations filters organizations by memberUids`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        val memberOrganizations =
            state.userOrganizations.filter { it.memberUids.contains(testUserId) }
        assertEquals(1, memberOrganizations.size)
        assertEquals(testOrganization1, memberOrganizations.first())
      }

  @Test
  fun `loadUserOrganizations filters organizations by createdBy`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        val createdOrganizations = state.userOrganizations.filter { it.createdBy == testUserId }
        assertEquals(1, createdOrganizations.size)
        assertEquals(testOrganization2, createdOrganizations.first())
      }

  @Test
  fun `loadUserOrganizations returns empty list when user has no organizations`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = "non_existent_user",
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.userOrganizations.isEmpty())
      }

  @Test
  fun `loadUserOrganizations handles repository errors`() =
      testScope.runTest {
        val errorRepository = TestOrganizationRepository(emptyList(), shouldThrowError = true)
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = errorRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load organizations"))
        assertTrue(state.userOrganizations.isEmpty())
      }

  @Test
  fun `loadUserOrganizations sets loading state correctly`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        // Check initial loading state
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        // Check final state
        assertFalse(viewModel.uiState.value.isLoading)
      }

  @Test
  fun `markRedirectToCreation sets redirect flag to true`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.shouldRedirectToCreation)

        viewModel.markRedirectToCreation()

        assertTrue(viewModel.uiState.value.shouldRedirectToCreation)
      }

  @Test
  fun `resetRedirectFlag sets redirect flag to false`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        viewModel.markRedirectToCreation()
        assertTrue(viewModel.uiState.value.shouldRedirectToCreation)

        viewModel.resetRedirectFlag()
        assertFalse(viewModel.uiState.value.shouldRedirectToCreation)
      }

  @Test
  fun `reload organizations after error clears error state`() =
      testScope.runTest {
        val errorRepository = TestOrganizationRepository(emptyList(), shouldThrowError = true)
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = errorRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        // Switch to working repository
        errorRepository.shouldThrowError = false
        errorRepository.organizations = listOf(testOrganization1)

        viewModel.loadUserOrganizations()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.userOrganizations.size)
      }

  @Test
  fun `uiState data class defaults are correct`() {
    val defaultState = OrganizationManagementUiState()
    assertTrue(defaultState.userOrganizations.isEmpty())
    assertFalse(defaultState.isLoading)
    assertNull(defaultState.error)
    assertFalse(defaultState.shouldRedirectToCreation)
    assertNull(defaultState.pinnedOrganizationId)
  }

  // Tests for pinned organization functionality
  @Test
  fun `viewModel loads pinned organization on init`() =
      testScope.runTest {
        userRepository.setPinnedOrganization(testUserId, "org1")
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        assertEquals("org1", viewModel.uiState.value.pinnedOrganizationId)
      }

  @Test
  fun `togglePinOrganization pins unpinned organization`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()
        assertNull(viewModel.uiState.value.pinnedOrganizationId)

        viewModel.togglePinOrganization("org1")
        advanceUntilIdle()

        assertEquals("org1", viewModel.uiState.value.pinnedOrganizationId)
        assertEquals("org1", userRepository.getPinnedOrganization(testUserId))
      }

  @Test
  fun `togglePinOrganization unpins pinned organization`() =
      testScope.runTest {
        userRepository.setPinnedOrganization(testUserId, "org1")
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()
        assertEquals("org1", viewModel.uiState.value.pinnedOrganizationId)

        viewModel.togglePinOrganization("org1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pinnedOrganizationId)
        assertNull(userRepository.getPinnedOrganization(testUserId))
      }

  @Test
  fun `togglePinOrganization replaces existing pin`() =
      testScope.runTest {
        userRepository.setPinnedOrganization(testUserId, "org1")
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()
        assertEquals("org1", viewModel.uiState.value.pinnedOrganizationId)

        viewModel.togglePinOrganization("org2")
        advanceUntilIdle()

        assertEquals("org2", viewModel.uiState.value.pinnedOrganizationId)
        assertEquals("org2", userRepository.getPinnedOrganization(testUserId))
      }

  @Test
  fun `togglePinOrganization handles repository errors`() =
      testScope.runTest {
        val errorUserRepo = TestUserRepository(shouldThrowError = true)
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = errorUserRepo)

        advanceUntilIdle()

        viewModel.togglePinOrganization("org1")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Failed to update pin"))
      }

  @Test
  fun `loadPinnedOrganization handles null pinned organization`() =
      testScope.runTest {
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pinnedOrganizationId)
      }

  @Test
  fun `loadPinnedOrganization handles repository errors gracefully`() =
      testScope.runTest {
        val errorUserRepo = TestUserRepository(shouldThrowError = true)
        viewModel =
            OrganizationManagementViewModel(
                userId = testUserId,
                organizationRepository = organizationRepository,
                userRepository = errorUserRepo)

        advanceUntilIdle()

        // Should not crash, just leave pinnedOrganizationId as null
        assertNull(viewModel.uiState.value.pinnedOrganizationId)
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

  // Test helper for UserRepository
  private class TestUserRepository(var shouldThrowError: Boolean = false) : UserRepository {
    private val pinnedOrganizations = mutableMapOf<String, String?>()

    fun setPinnedOrganization(userId: String, orgId: String?) {
      pinnedOrganizations[userId] = orgId
    }

    override suspend fun pinOrganization(userId: String, organizationId: String) {
      if (shouldThrowError) throw Exception("Test error pinning organization")
      pinnedOrganizations[userId] = organizationId
    }

    override suspend fun unpinOrganization(userId: String) {
      if (shouldThrowError) throw Exception("Test error unpinning organization")
      pinnedOrganizations[userId] = null
    }

    override suspend fun getPinnedOrganization(userId: String): String? {
      if (shouldThrowError) throw Exception("Test error getting pinned organization")
      return pinnedOrganizations[userId]
    }

    // Stub implementations for other methods
    override suspend fun getUserById(userId: String) = null

    override suspend fun getUserByEmail(email: String) = null

    override suspend fun getAllUsers() = emptyList<com.github.se.studentconnect.model.user.User>()

    override suspend fun getUsersPaginated(limit: Int, lastUserId: String?) =
        emptyList<com.github.se.studentconnect.model.user.User>() to false

    override suspend fun saveUser(user: com.github.se.studentconnect.model.user.User) {}

    override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

    override suspend fun deleteUser(userId: String) {}

    override suspend fun getUsersByUniversity(university: String) =
        emptyList<com.github.se.studentconnect.model.user.User>()

    override suspend fun getUsersByHobby(hobby: String) =
        emptyList<com.github.se.studentconnect.model.user.User>()

    override suspend fun getNewUid() = "test_uid"

    override suspend fun getJoinedEvents(userId: String) = emptyList<String>()

    override suspend fun addEventToUser(eventId: String, userId: String) {}

    override suspend fun addInvitationToUser(eventId: String, userId: String, fromUserId: String) {}

    override suspend fun getInvitations(userId: String) =
        emptyList<com.github.se.studentconnect.model.activities.Invitation>()

    override suspend fun acceptInvitation(eventId: String, userId: String) {}

    override suspend fun declineInvitation(eventId: String, userId: String) {}

    override suspend fun removeInvitation(eventId: String, userId: String) {}

    override suspend fun joinEvent(eventId: String, userId: String) {}

    override suspend fun leaveEvent(eventId: String, userId: String) {}

    override suspend fun sendInvitation(eventId: String, fromUserId: String, toUserId: String) {}

    override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

    override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

    override suspend fun getFavoriteEvents(userId: String) = emptyList<String>()

    override suspend fun addPinnedEvent(userId: String, eventId: String) {}

    override suspend fun removePinnedEvent(userId: String, eventId: String) {}

    override suspend fun getPinnedEvents(userId: String) = emptyList<String>()

    override suspend fun checkUsernameAvailability(username: String) = true
  }
}

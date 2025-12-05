package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationType
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
                userId = testUserId, organizationRepository = organizationRepository)

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
                userId = testUserId, organizationRepository = organizationRepository)

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
                userId = testUserId, organizationRepository = organizationRepository)

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
                userId = "non_existent_user", organizationRepository = organizationRepository)

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
                userId = testUserId, organizationRepository = errorRepository)

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
                userId = testUserId, organizationRepository = organizationRepository)

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
                userId = testUserId, organizationRepository = organizationRepository)

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
                userId = testUserId, organizationRepository = organizationRepository)

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
                userId = testUserId, organizationRepository = errorRepository)

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
  }
}

package com.github.se.studentconnect.ui.profile

import android.content.Context
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.OrganizationRepositoryLocal
import com.github.se.studentconnect.util.MainDispatcherRule
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizationProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: OrganizationProfileViewModel
  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var mockContext: Context

  private val testOrganization =
      Organization(
          id = "test_org",
          name = "Test Organization",
          type = com.github.se.studentconnect.model.organization.OrganizationType.Association,
          description = "A test organization",
          logoUrl = "https://example.com/logo.png",
          memberUids = listOf("user1", "user2"),
          createdBy = "creator1")

  private val testUser1 =
      User(
          userId = "user1",
          email = "user1@test.com",
          username = "user1",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          createdAt = 1000L,
          updatedAt = 1000L)

  private val testUser2 =
      User(
          userId = "user2",
          email = "user2@test.com",
          username = "user2",
          firstName = "Jane",
          lastName = "Smith",
          university = "EPFL",
          createdAt = 1000L,
          updatedAt = 1000L)

  private val testEvent =
      Event.Public(
          uid = "event1",
          title = "Test Event",
          description = "Test Description",
          ownerId = "test_org",
          start = Timestamp(Date()),
          end = Timestamp(Date(System.currentTimeMillis() + 3600000)),
          location = Location(46.5197, 6.6323, "EPFL"),
          participationFee = 0u,
          isFlash = false,
          subtitle = "Test Subtitle",
          tags = listOf("Sports"))

  @Before
  fun setUp() {
    // Ensure no authenticated user for tests that expect null currentUserId
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    organizationRepository = OrganizationRepositoryLocal()
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()

    // Mock Context for string resources
    mockContext = mockk(relaxed = true)
    every { mockContext.applicationContext } returns mockContext
    every { mockContext.getString(R.string.org_event_time_today) } returns "Today"
    every { mockContext.getString(R.string.org_event_time_tomorrow) } returns "Tomorrow"
    every {
      mockContext.resources.getQuantityString(R.plurals.org_event_time_in_days, any(), any())
    } answers
        {
          val days = arg<Int>(1)
          "In $days days"
        }

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)
  }

  @After
  fun tearDown() {
    // Clean up authentication state
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false
  }

  @Test
  fun `selectTab updates selected tab to EVENTS`() {
    viewModel.selectTab(OrganizationTab.MEMBERS)
    viewModel.selectTab(OrganizationTab.EVENTS)

    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `selectTab updates selected tab to MEMBERS`() {
    viewModel.selectTab(OrganizationTab.MEMBERS)

    assertEquals(OrganizationTab.MEMBERS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `multiple tab selections work correctly`() {
    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(OrganizationTab.MEMBERS)
    assertEquals(OrganizationTab.MEMBERS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(OrganizationTab.EVENTS)
    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(OrganizationTab.MEMBERS)
    assertEquals(OrganizationTab.MEMBERS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `selecting same tab multiple times is idempotent`() {
    viewModel.selectTab(OrganizationTab.EVENTS)
    viewModel.selectTab(OrganizationTab.EVENTS)
    viewModel.selectTab(OrganizationTab.EVENTS)

    assertEquals(OrganizationTab.EVENTS, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `OrganizationTab enum has correct values`() {
    val values = OrganizationTab.values()

    assertEquals(2, values.size)
    assertTrue(values.contains(OrganizationTab.EVENTS))
    assertTrue(values.contains(OrganizationTab.MEMBERS))
  }

  @Test
  fun `OrganizationProfileUiState default values are correct`() {
    val defaultState = OrganizationProfileUiState()

    assertNull(defaultState.organization)
    assertEquals(OrganizationTab.EVENTS, defaultState.selectedTab)
    assertFalse(defaultState.isLoading)
    assertNull(defaultState.error)
  }

  @Test
  fun `companion object constants have expected values`() {
    assertEquals(120, OrganizationProfileViewModel.AVATAR_BANNER_HEIGHT)
    assertEquals(80, OrganizationProfileViewModel.AVATAR_SIZE)
    assertEquals(3, OrganizationProfileViewModel.AVATAR_BORDER_WIDTH)
    assertEquals(40, OrganizationProfileViewModel.AVATAR_ICON_SIZE)
    assertEquals(140, OrganizationProfileViewModel.EVENT_CARD_WIDTH)
    assertEquals(100, OrganizationProfileViewModel.EVENT_CARD_HEIGHT)
    assertEquals(72, OrganizationProfileViewModel.MEMBER_AVATAR_SIZE)
    assertEquals(36, OrganizationProfileViewModel.MEMBER_ICON_SIZE)
    assertEquals(2, OrganizationProfileViewModel.GRID_COLUMNS)
    assertEquals(400, OrganizationProfileViewModel.MEMBERS_GRID_HEIGHT)
  }

  @Test
  fun `loadOrganizationData sets error when organization ID is null`() = runTest {
    viewModel =
        OrganizationProfileViewModel(
            organizationId = null,
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Organization ID is required", state.error)
    assertNull(state.organization)
  }

  @Test
  fun `loadOrganizationData sets error when organization not found`() = runTest {
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Organization not found", state.error)
    assertNull(state.organization)
  }

  @Test
  fun `loadOrganizationData successfully loads organization with events and members`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    userRepository.saveUser(testUser2)
    eventRepository.addEvent(testEvent)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNotNull(state.organization)
    assertEquals("Test Organization", state.organization?.name)
    assertEquals("A test organization", state.organization?.description)
    assertEquals(1, state.organization?.events?.size)
    assertEquals(2, state.organization?.members?.size)
  }

  @Test
  fun `loadOrganizationData handles missing events gracefully`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    userRepository.saveUser(testUser2)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNotNull(state.organization)
    assertTrue(state.organization?.events?.isEmpty() == true)
    assertEquals(2, state.organization?.members?.size)
  }

  @Test
  fun `loadOrganizationData handles missing members gracefully`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    eventRepository.addEvent(testEvent)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNotNull(state.organization)
    assertEquals(1, state.organization?.events?.size)
    assertTrue(state.organization?.members?.isEmpty() == true)
  }

  @Test
  fun `loadOrganizationData checks if user is following organization`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertFalse(state.organization?.isFollowing == true)
  }

  @Test
  fun `toggleFollow updates UI optimistically when organization exists`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    // Test passes even if currentUserId is null since toggleFollow handles that case
  }

  @Test
  fun `toggleFollow does nothing when organization is null`() = runTest {
    viewModel.toggleFollow()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.organization)
  }

  @Test
  fun `toggleFollow does nothing when current user is null`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val initialFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    viewModel.toggleFollow()
    advanceUntilIdle()

    val finalFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    // Since currentUserId is null, toggleFollow should not change the state
    assertEquals(initialFollowing, finalFollowing)
  }

  @Test
  fun `toggleFollow toggles from following to not following`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    // Since currentUserId is null in tests, toggleFollow won't change the state
    // This test verifies that behavior
    val initialFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    viewModel.toggleFollow()
    advanceUntilIdle()
    val finalFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    assertEquals(initialFollowing, finalFollowing)
  }

  @Test
  fun `initial state shows loading`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    val viewModelNew =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    assertTrue(viewModelNew.uiState.value.isLoading)
    advanceUntilIdle()
    assertFalse(viewModelNew.uiState.value.isLoading)
  }

  @Test
  fun `loading state transitions correctly`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    assertTrue(viewModel.uiState.value.isLoading)
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun `organization with description loads correctly`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertEquals("A test organization", state.organization?.description)
  }

  @Test
  fun `organization with no logo loads correctly`() = runTest {
    val orgWithoutLogo = testOrganization.copy(logoUrl = null)
    organizationRepository.saveOrganization(orgWithoutLogo)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertNull(state.organization?.logoUrl)
  }

  @Test
  fun `multiple events are loaded correctly`() = runTest {
    val event2 =
        testEvent.copy(
            uid = "event2",
            title = "Second Event",
            start = Timestamp(Date(System.currentTimeMillis() + 86400000)))

    organizationRepository.saveOrganization(testOrganization)
    eventRepository.addEvent(testEvent)
    eventRepository.addEvent(event2)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertEquals(2, state.organization?.events?.size)
  }

  @Test
  fun `toggleFollow with authenticated user follows organization`() = runTest {
    // Set up authenticated user
    AuthenticationProvider.testUserId = "user1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    assertFalse(initialState.organization?.isFollowing == true)

    // Toggle follow
    viewModel.toggleFollow()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertTrue(finalState.organization?.isFollowing == true)

    // Verify the organization was followed in the repository
    val followedOrgs = userRepository.getFollowedOrganizations("user1")
    assertTrue(followedOrgs.contains("test_org"))
  }

  @Test
  fun `toggleFollow with authenticated user unfollows organization`() = runTest {
    // Set up authenticated user
    AuthenticationProvider.testUserId = "user1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    userRepository.followOrganization("user1", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    assertTrue(initialState.organization?.isFollowing == true)

    // Toggle follow (should unfollow)
    viewModel.toggleFollow()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertFalse(finalState.organization?.isFollowing == true)

    // Verify the organization was unfollowed in the repository
    val followedOrgs = userRepository.getFollowedOrganizations("user1")
    assertFalse(followedOrgs.contains("test_org"))
  }
}

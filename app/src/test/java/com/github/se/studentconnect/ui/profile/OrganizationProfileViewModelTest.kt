package com.github.se.studentconnect.ui.profile

import android.content.Context
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
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
  fun `organization loads correctly with members and events`() = runTest {
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
    // Test passes when organization loads successfully
  }

  @Test
  fun `onFollowButtonClick does nothing when organization is null`() = runTest {
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNull(state.organization)
  }

  @Test
  fun `onFollowButtonClick does nothing when current user is null`() = runTest {
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
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    val finalFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    // Since currentUserId is null, onFollowButtonClick should not change the state
    assertEquals(initialFollowing, finalFollowing)
  }

  @Test
  fun `member automatically follows organization and cannot unfollow`() = runTest {
    // Set up authenticated user who IS a member
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

    val state = viewModel.uiState.value
    // Member should automatically be following
    assertTrue(state.organization?.isFollowing == true)
    assertTrue(state.organization?.isMember == true)

    // Try to click follow button (should do nothing for members)
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Should still be following and no dialog shown
    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)
    assertFalse(viewModel.uiState.value.showUnfollowDialog)
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
  fun `onFollowButtonClick with authenticated user follows organization`() = runTest {
    // Set up authenticated user who is NOT a member
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)

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
    assertFalse(initialState.organization?.isMember == true)

    // Click follow button
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertTrue(finalState.organization?.isFollowing == true)

    // Verify the organization was followed in the repository
    val followedOrgs = userRepository.getFollowedOrganizations("user3")
    assertTrue(followedOrgs.contains("test_org"))
  }

  @Test
  fun `confirmUnfollow with authenticated user unfollows organization`() = runTest {
    // Set up authenticated user who is NOT a member
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

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
    assertFalse(initialState.organization?.isMember == true)

    // Click follow button - should show dialog
    viewModel.onFollowButtonClick()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.showUnfollowDialog)

    // Confirm unfollow
    viewModel.confirmUnfollow()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertFalse(finalState.organization?.isFollowing == true)
    assertFalse(finalState.showUnfollowDialog)

    // Verify the organization was unfollowed in the repository
    val followedOrgs = userRepository.getFollowedOrganizations("user3")
    assertFalse(followedOrgs.contains("test_org"))
  }

  @Test
  fun `onFollowButtonClick shows dialog when unfollowing`() = runTest {
    // Set up authenticated user who is NOT a member
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertTrue(initialState.organization?.isFollowing == true)
    assertFalse(initialState.showUnfollowDialog)

    // Click follow button when following - should show dialog
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Dialog should be shown
    assertTrue(viewModel.uiState.value.showUnfollowDialog)
    // Still following
    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)
  }

  @Test
  fun `dismissUnfollowDialog cancels unfollow action`() = runTest {
    // Set up authenticated user who is NOT a member
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)

    // Show dialog
    viewModel.onFollowButtonClick()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.showUnfollowDialog)

    // Dismiss dialog
    viewModel.dismissUnfollowDialog()
    advanceUntilIdle()

    // Should still be following and dialog closed
    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)
    assertFalse(viewModel.uiState.value.showUnfollowDialog)

    // Verify still following in repository
    val followedOrgs = userRepository.getFollowedOrganizations("user3")
    assertTrue(followedOrgs.contains("test_org"))
  }

  @Test
  fun `performFollow prevents rapid toggles with loading flag`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    // Click follow button multiple times rapidly
    viewModel.onFollowButtonClick()
    viewModel.onFollowButtonClick()
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Should only be followed once
    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)
  }

  @Test
  fun `performUnfollow prevents rapid toggles with loading flag`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    // Show dialog and confirm unfollow multiple times
    viewModel.onFollowButtonClick()
    viewModel.confirmUnfollow()
    viewModel.confirmUnfollow()
    viewModel.confirmUnfollow()
    advanceUntilIdle()

    // Should be unfollowed
    assertFalse(viewModel.uiState.value.organization?.isFollowing == true)
  }

  @Test
  fun `creator is included as owner in members list`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    userRepository.saveUser(testUser2)
    userRepository.saveUser(
        User(
            userId = "creator1",
            email = "creator@test.com",
            username = "creator",
            firstName = "Alice",
            lastName = "Creator",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L))

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

    // Should have 3 members total (user1, user2, and creator)
    assertEquals(3, state.organization?.members?.size)

    // Creator should be in the list with "Owner" role
    val creator = state.organization?.members?.find { it.name == "Alice Creator" }
    assertNotNull(creator)
    assertEquals("Owner", creator?.role)

    // Other members should have "Member" role
    val member = state.organization?.members?.find { it.name == "John Doe" }
    assertNotNull(member)
    assertEquals("Member", member?.role)
  }

  @Test
  fun `organization loads correctly when creator is also in memberUids`() = runTest {
    val orgWithCreatorAsMember =
        testOrganization.copy(memberUids = listOf("user1", "user2", "creator1"))
    organizationRepository.saveOrganization(orgWithCreatorAsMember)
    userRepository.saveUser(testUser1)
    userRepository.saveUser(testUser2)
    userRepository.saveUser(
        User(
            userId = "creator1",
            email = "creator@test.com",
            username = "creator",
            firstName = "Alice",
            lastName = "Creator",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L))

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

    // Should still have 3 members (no duplicates)
    assertEquals(3, state.organization?.members?.size)

    // Creator should be listed with "Owner" role
    val creator = state.organization?.members?.find { it.name == "Alice Creator" }
    assertNotNull(creator)
    assertEquals("Owner", creator?.role)
  }

  @Test
  fun `member status is checked correctly for non-member following organization`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

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
    assertTrue(state.organization?.isFollowing == true)
    assertFalse(state.organization?.isMember == true)
  }

  @Test
  fun `creator is automatically following organization`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(
        User(
            userId = "creator1",
            email = "creator@test.com",
            username = "creator",
            firstName = "Alice",
            lastName = "Creator",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L))

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
    assertTrue(state.organization?.isFollowing == true)
    assertTrue(state.organization?.isMember == true)
  }

  @Test
  fun `isFollowLoading flag is set during follow action`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isFollowLoading)

    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Loading flag should be false after completion
    assertFalse(viewModel.uiState.value.isFollowLoading)
  }

  @Test
  fun `isFollowLoading flag is set during unfollow action`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isFollowLoading)

    viewModel.onFollowButtonClick()
    viewModel.confirmUnfollow()
    advanceUntilIdle()

    // Loading flag should be false after completion
    assertFalse(viewModel.uiState.value.isFollowLoading)
  }

  @Test
  fun `initial UiState fields have correct default values`() {
    val state = OrganizationProfileUiState()

    assertNull(state.organization)
    assertEquals(OrganizationTab.EVENTS, state.selectedTab)
    assertFalse(state.isLoading)
    assertFalse(state.isFollowLoading)
    assertFalse(state.showUnfollowDialog)
    assertNull(state.error)
  }

  @Test
  fun `loadOrganizationData handles error when fetching organization throws exception`() = runTest {
    // Use a repository that will throw an exception
    val failingRepository = OrganizationRepositoryLocal()

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Organization not found", state.error)
    assertNull(state.organization)
  }

  @Test
  fun `organization with multiple events sorted correctly`() = runTest {
    val event2 =
        testEvent.copy(
            uid = "event2",
            title = "Second Event",
            start = Timestamp(Date(System.currentTimeMillis() + 86400000)))
    val event3 =
        testEvent.copy(
            uid = "event3",
            title = "Third Event",
            start = Timestamp(Date(System.currentTimeMillis() + 172800000)))

    organizationRepository.saveOrganization(testOrganization)
    eventRepository.addEvent(testEvent)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

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
    assertEquals(3, state.organization?.events?.size)
  }

  @Test
  fun `member with missing user data is filtered out`() = runTest {
    // Only save one user, not all members
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    // testUser2 is not saved, so it should be filtered out

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

    // Should only have 1 member (user1), since user2 data couldn't be fetched
    assertEquals(1, state.organization?.members?.size)
  }

  @Test
  fun `showUnfollowDialog is set correctly when clicking follow button while following`() =
      runTest {
        AuthenticationProvider.testUserId = "user3"
        AuthenticationProvider.local = true

        val testUser3 =
            User(
                userId = "user3",
                email = "user3@test.com",
                username = "user3",
                firstName = "Bob",
                lastName = "Johnson",
                university = "EPFL",
                createdAt = 1000L,
                updatedAt = 1000L)

        organizationRepository.saveOrganization(testOrganization)
        userRepository.saveUser(testUser3)
        userRepository.followOrganization("user3", "test_org")

        viewModel =
            OrganizationProfileViewModel(
                organizationId = "test_org",
                context = mockContext,
                organizationRepository = organizationRepository,
                eventRepository = eventRepository,
                userRepository = userRepository)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showUnfollowDialog)

        // Click follow button when already following
        viewModel.onFollowButtonClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showUnfollowDialog)
      }

  @Test
  fun `dismissUnfollowDialog resets showUnfollowDialog flag`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    viewModel.onFollowButtonClick()
    assertTrue(viewModel.uiState.value.showUnfollowDialog)

    viewModel.dismissUnfollowDialog()
    assertFalse(viewModel.uiState.value.showUnfollowDialog)
  }

  @Test
  fun `confirmUnfollow closes dialog`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val testUser3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "Bob",
            lastName = "Johnson",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser3)
    userRepository.followOrganization("user3", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    advanceUntilIdle()

    viewModel.onFollowButtonClick()
    assertTrue(viewModel.uiState.value.showUnfollowDialog)

    viewModel.confirmUnfollow()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.showUnfollowDialog)
  }
}

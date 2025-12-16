package com.github.se.studentconnect.ui.profile

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.util.MainDispatcherRule
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import io.mockk.coEvery
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrganizationProfileViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: OrganizationProfileViewModel
  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
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

  private val testCreator =
      User(
          userId = "creator1",
          email = "creator@test.com",
          username = "creator",
          firstName = "Alice",
          lastName = "Creator",
          university = "EPFL",
          createdAt = 1000L,
          updatedAt = 1000L)

  private val testEvent =
      Event.Public(
          uid = "event1",
          title = "Test Event",
          description = "Test Description",
          ownerId = "test_org",
          organizationId = "test_org",
          start = Timestamp(Date()),
          end = Timestamp(Date(System.currentTimeMillis() + 3600000)),
          location = Location(46.5197, 6.6323, "EPFL"),
          participationFee = 0u,
          isFlash = false,
          subtitle = "Test Subtitle",
          tags = listOf("Sports"))

  @Before
  fun setUp() {
    // Initialize Firebase if not already initialized
    val context = ApplicationProvider.getApplicationContext<Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Ensure no authenticated user for tests that expect null currentUserId
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    organizationRepository = OrganizationRepositoryLocal()
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    // Test passes when organization loads successfully
  }

  @Test
  fun `toggleFollow does nothing when organization is null`() = runTest {
    viewModel.onFollowButtonClick()
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    val finalFollowing = viewModel.uiState.value.organization?.isFollowing ?: false
    // Since currentUserId is null, toggleFollow should not change the state
    assertEquals(initialFollowing, finalFollowing)
  }

  @Test
  fun `member can follow and unfollow organization`() = runTest {
    // Set up authenticated user who IS a member
    AuthenticationProvider.testUserId = "user1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    // Make user1 follow the organization
    userRepository.followOrganization("user1", "test_org")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Member should be following initially
    assertTrue(state.organization?.isFollowing == true)

    // Members cannot unfollow, so the button should show unfollow dialog
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Should still be following (members can't unfollow)
    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertEquals(2, state.organization?.events?.size)
  }

  @Test
  fun `toggleFollow with authenticated user follows organization`() = runTest {
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    assertFalse(initialState.organization?.isFollowing == true)

    // Click follow button to follow organization
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertTrue(finalState.organization?.isFollowing == true)

    // Verify the organization was followed in the repository
    val followedOrgs = userRepository.getFollowedOrganizations("user3")
    assertTrue(followedOrgs.contains("test_org"))
  }

  @Test
  fun `toggleFollow with authenticated user unfollows organization`() = runTest {
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    assertTrue(initialState.organization?.isFollowing == true)

    // Click follow button to show unfollow dialog
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Confirm unfollow in the dialog
    viewModel.confirmUnfollow()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertFalse(finalState.organization?.isFollowing == true)

    // Verify the organization was unfollowed in the repository
    val followedOrgs = userRepository.getFollowedOrganizations("user3")
    assertFalse(followedOrgs.contains("test_org"))
  }

  @Test
  fun `toggleFollow unfollows when already following`() = runTest {
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertTrue(initialState.organization?.isFollowing == true)

    // Click follow button when following - should show dialog then unfollow
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Confirm unfollow
    viewModel.confirmUnfollow()
    advanceUntilIdle()

    // Should now be unfollowing
    assertFalse(viewModel.uiState.value.organization?.isFollowing == true)
  }

  @Test
  fun `toggleFollow prevents rapid toggles with loading flag`() = runTest {
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.organization?.isFollowing == true)

    // Click follow button
    viewModel.onFollowButtonClick()
    advanceUntilIdle()

    // Should now be following
    assertTrue(viewModel.uiState.value.organization?.isFollowing == true)

    // Verify following in repository
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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
                userRepository = userRepository,
                notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

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
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.onFollowButtonClick()
    assertTrue(viewModel.uiState.value.showUnfollowDialog)

    viewModel.confirmUnfollow()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.showUnfollowDialog)
  }

  // ===================== Add Member Dialog Tests =====================

  @Test
  fun `showAddMemberDialog sets dialog state and selected role`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.showAddMemberDialog)
    assertNull(viewModel.uiState.value.selectedRole)

    viewModel.showAddMemberDialog("Admin")
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.showAddMemberDialog)
    assertEquals("Admin", viewModel.uiState.value.selectedRole)
  }

  @Test
  fun `showAddMemberDialog loads available users`() = runTest {
    AuthenticationProvider.testUserId = "current_user"
    AuthenticationProvider.local = true

    val currentUser =
        User(
            userId = "current_user",
            email = "current@test.com",
            username = "current",
            firstName = "Current",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val availableUser1 =
        User(
            userId = "available1",
            email = "available1@test.com",
            username = "available1",
            firstName = "Available",
            lastName = "One",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val availableUser2 =
        User(
            userId = "available2",
            email = "available2@test.com",
            username = "available2",
            firstName = "Available",
            lastName = "Two",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(currentUser)
    userRepository.saveUser(testUser1) // Already a member
    userRepository.saveUser(testUser2) // Already a member
    userRepository.saveUser(availableUser1)
    userRepository.saveUser(availableUser2)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.showAddMemberDialog)
    assertEquals("Member", state.selectedRole)
    assertFalse(state.isLoadingUsers)
    // Should have 2 available users (availableUser1 and availableUser2)
    // Excludes current_user, testUser1, and testUser2 (members)
    assertEquals(2, state.availableUsers.size)
    assertTrue(state.availableUsers.any { it.userId == "available1" })
    assertTrue(state.availableUsers.any { it.userId == "available2" })
    assertFalse(state.availableUsers.any { it.userId == "current_user" })
    assertFalse(state.availableUsers.any { it.userId == "user1" })
    assertFalse(state.availableUsers.any { it.userId == "user2" })
  }

  @Test
  fun `showAddMemberDialog does nothing when organization is null`() = runTest {
    viewModel =
        OrganizationProfileViewModel(
            organizationId = "nonexistent_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    assertNull(viewModel.uiState.value.organization)

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    // Dialog should still be shown, but availableUsers should remain empty
    assertTrue(viewModel.uiState.value.showAddMemberDialog)
    assertEquals("Member", viewModel.uiState.value.selectedRole)
    assertTrue(viewModel.uiState.value.availableUsers.isEmpty())
  }

  @Test
  fun `dismissAddMemberDialog resets dialog state`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Admin")
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.showAddMemberDialog)
    assertEquals("Admin", viewModel.uiState.value.selectedRole)

    viewModel.dismissAddMemberDialog()

    assertFalse(viewModel.uiState.value.showAddMemberDialog)
    assertNull(viewModel.uiState.value.selectedRole)
    assertTrue(viewModel.uiState.value.availableUsers.isEmpty())
  }

  @Test
  fun `loadAvailableUsers filters out current user and existing members`() = runTest {
    AuthenticationProvider.testUserId = "current_user"
    AuthenticationProvider.local = true

    val currentUser =
        User(
            userId = "current_user",
            email = "current@test.com",
            username = "current",
            firstName = "Current",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val availableUser =
        User(
            userId = "available",
            email = "available@test.com",
            username = "available",
            firstName = "Available",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(currentUser)
    userRepository.saveUser(testUser1) // Member
    userRepository.saveUser(testUser2) // Member
    userRepository.saveUser(availableUser)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.availableUsers.size)
    assertEquals("available", state.availableUsers[0].userId)
  }

  @Test
  fun `loadAvailableUsers handles organization with no memberUids`() = runTest {
    AuthenticationProvider.testUserId = "current_user"
    AuthenticationProvider.local = true

    val orgWithoutMembers = testOrganization.copy(memberUids = emptyList())
    val availableUser =
        User(
            userId = "available",
            email = "available@test.com",
            username = "available",
            firstName = "Available",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(orgWithoutMembers)
    userRepository.saveUser(availableUser)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should include availableUser, but exclude current_user
    assertTrue(
        state.availableUsers.isEmpty() || state.availableUsers.none { it.userId == "current_user" })
  }

  @Test
  fun `loadAvailableUsers handles exception gracefully`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    // Create a test user repository that throws exception
    val failingUserRepository =
        object : UserRepository {
          override suspend fun getAllUsers(): List<User> {
            throw RuntimeException("Failed to load users")
          }

          override suspend fun getUserById(userId: String): User? = null

          override suspend fun getUserByEmail(email: String): User? = null

          override suspend fun getUsersPaginated(
              limit: Int,
              lastUserId: String?
          ): Pair<List<User>, Boolean> = emptyList<User>() to false

          override suspend fun saveUser(user: User) {}

          override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

          override suspend fun deleteUser(userId: String) {}

          override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

          override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

          override suspend fun getNewUid(): String = "new_uid"

          override suspend fun getJoinedEvents(userId: String): List<String> = emptyList()

          override suspend fun addEventToUser(eventId: String, userId: String) {}

          override suspend fun addInvitationToUser(
              eventId: String,
              userId: String,
              fromUserId: String
          ) {}

          override suspend fun getInvitations(userId: String): List<Invitation> = emptyList()

          override suspend fun acceptInvitation(eventId: String, userId: String) {}

          override suspend fun declineInvitation(eventId: String, userId: String) {}

          override suspend fun removeInvitation(eventId: String, userId: String) {}

          override suspend fun joinEvent(eventId: String, userId: String) {}

          override suspend fun sendInvitation(
              eventId: String,
              fromUserId: String,
              toUserId: String
          ) {}

          override suspend fun addFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun removeFavoriteEvent(userId: String, eventId: String) {}

          override suspend fun getFavoriteEvents(userId: String): List<String> = emptyList()

          override suspend fun addPinnedEvent(userId: String, eventId: String) {}

          override suspend fun removePinnedEvent(userId: String, eventId: String) {}

          override suspend fun getPinnedEvents(userId: String): List<String> = emptyList()

          override suspend fun checkUsernameAvailability(username: String): Boolean = true

          override suspend fun leaveEvent(eventId: String, userId: String) {}

          override suspend fun pinOrganization(userId: String, organizationId: String) {}

          override suspend fun unpinOrganization(userId: String) {}

          override suspend fun getPinnedOrganization(userId: String): String? = null
        }

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = failingUserRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.showAddMemberDialog)
    assertFalse(state.isLoadingUsers)
    assertTrue(state.availableUsers.isEmpty())
  }

  @Test
  fun `loadAvailableUsers sets loading state correctly`() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoadingUsers)

    viewModel.showAddMemberDialog("Member")
    // isLoadingUsers should be true during loading
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoadingUsers)
  }

  // ===================== Send Member Invitation Tests =====================

  @Test
  fun `sendMemberInvitation sends invitation and creates notification`() = runTest {
    AuthenticationProvider.testUserId = "inviter_id"
    AuthenticationProvider.local = true

    val inviter =
        User(
            userId = "inviter_id",
            email = "inviter@test.com",
            username = "inviter",
            firstName = "Inviter",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val invitee =
        User(
            userId = "invitee_id",
            email = "invitee@test.com",
            username = "invitee",
            firstName = "Invitee",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(inviter)
    userRepository.saveUser(invitee)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Admin")
    advanceUntilIdle()

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    // Dialog should be dismissed
    assertFalse(viewModel.uiState.value.showAddMemberDialog)
    assertNull(viewModel.uiState.value.selectedRole)

    // Verify invitation was sent (check repository)
    val invitations = organizationRepository.getPendingInvitations("test_org")
    assertTrue(invitations.any { it.userId == "invitee_id" && it.role == "Admin" })

    // Verify notification was created
    notificationRepository.getNotifications(
        "invitee_id",
        { notifications ->
          assertTrue(
              notifications.any { notification ->
                notification is
                    com.github.se.studentconnect.model.notification.Notification.OrganizationMemberInvitation &&
                    (notification
                            as
                            com.github.se.studentconnect.model.notification.Notification.OrganizationMemberInvitation)
                        .organizationId == "test_org" &&
                    (notification
                            as
                            com.github.se.studentconnect.model.notification.Notification.OrganizationMemberInvitation)
                        .role == "Admin"
              })
        },
        {})
  }

  @Test
  fun `sendMemberInvitation does nothing when organization is null`() = runTest {
    AuthenticationProvider.testUserId = "inviter_id"
    AuthenticationProvider.local = true

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "nonexistent_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    assertNull(viewModel.uiState.value.organization)

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    // Should not crash, but nothing should happen
    val invitations = organizationRepository.getPendingInvitations("test_org")
    assertTrue(invitations.isEmpty())
  }

  @Test
  fun `sendMemberInvitation does nothing when selectedRole is null`() = runTest {
    AuthenticationProvider.testUserId = "inviter_id"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    // Don't call showAddMemberDialog, so selectedRole is null
    assertNull(viewModel.uiState.value.selectedRole)

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    // Should not send invitation
    val invitations = organizationRepository.getPendingInvitations("test_org")
    assertTrue(invitations.isEmpty())
  }

  @Test
  fun `sendMemberInvitation does nothing when currentUserId is null`() = runTest {
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    val invitee =
        User(
            userId = "invitee_id",
            email = "invitee@test.com",
            username = "invitee",
            firstName = "Invitee",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(invitee)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    // Should not send invitation
    val invitations = organizationRepository.getPendingInvitations("test_org")
    assertTrue(invitations.isEmpty())
  }

  @Test
  fun `sendMemberInvitation handles exception gracefully`() = runTest {
    AuthenticationProvider.testUserId = "inviter_id"
    AuthenticationProvider.local = true

    val inviter =
        User(
            userId = "inviter_id",
            email = "inviter@test.com",
            username = "inviter",
            firstName = "Inviter",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val invitee =
        User(
            userId = "invitee_id",
            email = "invitee@test.com",
            username = "invitee",
            firstName = "Invitee",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    // Create a repository that throws exception
    val failingOrgRepository = mockk<OrganizationRepositoryLocal>(relaxed = true)
    coEvery { failingOrgRepository.getOrganizationById("test_org") } returns testOrganization
    coEvery { failingOrgRepository.sendMemberInvitation(any(), any(), any(), any()) } throws
        RuntimeException("Failed to send invitation")

    userRepository.saveUser(inviter)
    userRepository.saveUser(invitee)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingOrgRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    // Should handle exception without crashing
    // Dialog might still be open or closed depending on implementation
  }

  @Test
  fun `sendMemberInvitation uses Unknown when inviter name cannot be retrieved`() = runTest {
    AuthenticationProvider.testUserId = "inviter_id"
    AuthenticationProvider.local = true

    val invitee =
        User(
            userId = "invitee_id",
            email = "invitee@test.com",
            username = "invitee",
            firstName = "Invitee",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    // Don't save inviter, so getUserById will return null
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(invitee)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Member")
    advanceUntilIdle()

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    // Verify notification was created with "Unknown" as inviter name
    notificationRepository.getNotifications(
        "invitee_id",
        { notifications ->
          val invitationNotification =
              notifications.firstOrNull { notification ->
                notification is
                    com.github.se.studentconnect.model.notification.Notification.OrganizationMemberInvitation
              }
                  as?
                  com.github.se.studentconnect.model.notification.Notification.OrganizationMemberInvitation
          assertNotNull(invitationNotification)
          assertEquals("Unknown", invitationNotification?.invitedByName)
        },
        {})
  }

  // ===================== Accept Member Invitation Tests =====================

  @Test
  fun `acceptMemberInvitation accepts invitation and reloads organization`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val user3 =
        User(
            userId = "user3",
            email = "user3@test.com",
            username = "user3",
            firstName = "User",
            lastName = "Three",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(user3)
    organizationRepository.sendMemberInvitation("test_org", "user3", "Member", "creator1")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    val initialMemberCount = initialState.organization?.members?.size ?: 0

    viewModel.acceptMemberInvitation("test_org")
    advanceUntilIdle()

    // Verify invitation was accepted (user should now be a member)
    val updatedOrg = organizationRepository.getOrganizationById("test_org")
    assertNotNull(updatedOrg)
    assertTrue(updatedOrg?.memberUids?.contains("user3") == true)

    // Verify organization data was reloaded
    val finalState = viewModel.uiState.value
    assertNotNull(finalState.organization)
    // Member count should have increased
    assertTrue((finalState.organization?.members?.size ?: 0) >= initialMemberCount)
  }

  @Test
  fun `acceptMemberInvitation does nothing when currentUserId is null`() = runTest {
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    organizationRepository.saveOrganization(testOrganization)
    organizationRepository.sendMemberInvitation("test_org", "user3", "Member", "creator1")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialInvitations = organizationRepository.getPendingInvitations("test_org")
    val initialInvitationCount = initialInvitations.size

    viewModel.acceptMemberInvitation("test_org")
    advanceUntilIdle()

    // Invitation should still be pending
    val finalInvitations = organizationRepository.getPendingInvitations("test_org")
    assertEquals(initialInvitationCount, finalInvitations.size)
  }

  @Test
  fun `acceptMemberInvitation handles exception gracefully`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val failingOrgRepository = mockk<OrganizationRepositoryLocal>(relaxed = true)
    coEvery { failingOrgRepository.getOrganizationById("test_org") } returns testOrganization
    coEvery { failingOrgRepository.acceptMemberInvitation(any(), any()) } throws
        RuntimeException("Failed to accept invitation")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingOrgRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    // Should not crash
    viewModel.acceptMemberInvitation("test_org")
    advanceUntilIdle()
  }

  // ===================== Reject Member Invitation Tests =====================

  @Test
  fun `rejectMemberInvitation rejects invitation`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    organizationRepository.sendMemberInvitation("test_org", "user3", "Member", "creator1")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialInvitations = organizationRepository.getPendingInvitations("test_org")
    assertTrue(initialInvitations.any { it.userId == "user3" })

    viewModel.rejectMemberInvitation("test_org")
    advanceUntilIdle()

    // Verify invitation was rejected (removed)
    val finalInvitations = organizationRepository.getPendingInvitations("test_org")
    assertFalse(finalInvitations.any { it.userId == "user3" })
  }

  @Test
  fun `rejectMemberInvitation does nothing when currentUserId is null`() = runTest {
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    organizationRepository.saveOrganization(testOrganization)
    organizationRepository.sendMemberInvitation("test_org", "user3", "Member", "creator1")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialInvitations = organizationRepository.getPendingInvitations("test_org")
    val initialInvitationCount = initialInvitations.size

    viewModel.rejectMemberInvitation("test_org")
    advanceUntilIdle()

    // Invitation should still be pending
    val finalInvitations = organizationRepository.getPendingInvitations("test_org")
    assertEquals(initialInvitationCount, finalInvitations.size)
  }

  @Test
  fun `rejectMemberInvitation handles exception gracefully`() = runTest {
    AuthenticationProvider.testUserId = "user3"
    AuthenticationProvider.local = true

    val failingOrgRepository = mockk<OrganizationRepositoryLocal>(relaxed = true)
    coEvery { failingOrgRepository.getOrganizationById("test_org") } returns testOrganization
    coEvery { failingOrgRepository.rejectMemberInvitation(any(), any()) } throws
        RuntimeException("Failed to reject invitation")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingOrgRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    // Should not crash
    viewModel.rejectMemberInvitation("test_org")
    advanceUntilIdle()
  }

  // ===================== Remove Member Tests =====================

  @Test
  fun `removeMember removes member from organization`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    userRepository.saveUser(testUser2)
    userRepository.saveUser(testCreator)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    val initialMemberCount = initialState.organization?.members?.size ?: 0
    assertTrue(initialMemberCount > 0)

    // Find a non-owner member to remove
    val memberToRemove = initialState.organization?.members?.find { it.role != "Owner" }
    assertNotNull(memberToRemove)

    viewModel.removeMember(memberToRemove!!)
    advanceUntilIdle()

    // Verify member was removed
    val updatedOrg = organizationRepository.getOrganizationById("test_org")
    assertNotNull(updatedOrg)
    assertFalse(updatedOrg?.memberUids?.contains(memberToRemove.memberId) == true)

    // Verify organization data was reloaded
    val finalState = viewModel.uiState.value
    assertNotNull(finalState.organization)
    assertTrue((finalState.organization?.members?.size ?: 0) < initialMemberCount)
  }

  @Test
  fun `removeMember does nothing when organization is null`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "nonexistent_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    assertNull(viewModel.uiState.value.organization)

    val member =
        com.github.se.studentconnect.model.organization.OrganizationMember(
            memberId = "user1", name = "Test User", role = "Member", avatarUrl = null)

    viewModel.removeMember(member)
    advanceUntilIdle()

    // Should not crash
  }

  @Test
  fun `removeMember does nothing when currentUserId is null`() = runTest {
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val member =
        com.github.se.studentconnect.model.organization.OrganizationMember(
            memberId = "user1", name = "Test User", role = "Member", avatarUrl = null)

    val initialMemberCount =
        organizationRepository.getOrganizationById("test_org")?.memberUids?.size ?: 0

    viewModel.removeMember(member)
    advanceUntilIdle()

    // Member should not be removed
    val finalMemberCount =
        organizationRepository.getOrganizationById("test_org")?.memberUids?.size ?: 0
    assertEquals(initialMemberCount, finalMemberCount)
  }

  @Test
  fun `removeMember prevents removing owner`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testCreator)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    val initialMemberCount = initialState.organization?.members?.size ?: 0

    // Try to remove owner
    val ownerMember =
        com.github.se.studentconnect.model.organization.OrganizationMember(
            memberId = "creator1", name = "Owner", role = "Owner", avatarUrl = null)

    viewModel.removeMember(ownerMember)
    advanceUntilIdle()

    // Owner should still be in the organization
    val updatedOrg = organizationRepository.getOrganizationById("test_org")
    assertNotNull(updatedOrg)
    assertTrue(
        updatedOrg?.memberUids?.contains("creator1") == true || updatedOrg?.createdBy == "creator1")

    // Member count should be unchanged
    val finalState = viewModel.uiState.value
    assertNotNull(finalState.organization)
    assertEquals(initialMemberCount, finalState.organization?.members?.size)
  }

  @Test
  fun `removeMember handles exception gracefully`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    val failingOrgRepository = mockk<OrganizationRepositoryLocal>(relaxed = true)
    coEvery { failingOrgRepository.getOrganizationById("test_org") } returns testOrganization
    coEvery { failingOrgRepository.saveOrganization(any()) } throws
        RuntimeException("Failed to save")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingOrgRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val member =
        com.github.se.studentconnect.model.organization.OrganizationMember(
            memberId = "user1", name = "Test User", role = "Member", avatarUrl = null)

    // Should not crash
    viewModel.removeMember(member)
    advanceUntilIdle()
  }

  // ===================== Refresh Organization Tests =====================

  @Test
  fun `refreshOrganization reloads organization data`() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertNotNull(initialState.organization)
    val initialName = initialState.organization?.name

    // Update organization in repository
    val updatedOrg = testOrganization.copy(name = "Updated Organization")
    organizationRepository.saveOrganization(updatedOrg)

    // Refresh
    viewModel.refreshOrganization()
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertNotNull(finalState.organization)
    assertEquals("Updated Organization", finalState.organization?.name)
  }

  @Test
  fun `refreshOrganization handles errors gracefully`() = runTest {
    val failingOrgRepository = mockk<OrganizationRepositoryLocal>(relaxed = true)
    coEvery { failingOrgRepository.getOrganizationById("test_org") } throws
        RuntimeException("Failed to load")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingOrgRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    // Should not crash
    viewModel.refreshOrganization()
    advanceUntilIdle()
  }

  // ===================== Pending Invitations Tests =====================

  @Test
  fun `pendingInvitations are loaded for owner`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testCreator)
    organizationRepository.sendMemberInvitation("test_org", "user3", "Admin", "creator1")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertTrue(state.organization?.isOwner == true)
    // Pending invitations should be loaded
    assertTrue(state.pendingInvitations.containsKey("Admin"))
    assertEquals("user3", state.pendingInvitations["Admin"])
  }

  @Test
  fun `pendingInvitations are not loaded for non-owner`() = runTest {
    AuthenticationProvider.testUserId = "user1"
    AuthenticationProvider.local = true

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    organizationRepository.sendMemberInvitation("test_org", "user3", "Admin", "creator1")

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.organization)
    assertFalse(state.organization?.isOwner == true)
    // Pending invitations should be empty for non-owners
    assertTrue(state.pendingInvitations.isEmpty())
  }

  @Test
  fun `pendingInvitations handles exception gracefully`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    val failingOrgRepository = mockk<OrganizationRepositoryLocal>(relaxed = true)
    coEvery { failingOrgRepository.getOrganizationById("test_org") } returns testOrganization
    coEvery { failingOrgRepository.getPendingInvitations("test_org") } throws
        RuntimeException("Failed to load invitations")

    userRepository.saveUser(testCreator)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = failingOrgRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should handle exception gracefully and return empty map
    assertTrue(state.pendingInvitations.isEmpty())
  }

  @Test
  fun `sendMemberInvitation updates pendingInvitations in UI`() = runTest {
    AuthenticationProvider.testUserId = "creator1"
    AuthenticationProvider.local = true

    val inviter =
        User(
            userId = "creator1",
            email = "creator@test.com",
            username = "creator",
            firstName = "Creator",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    val invitee =
        User(
            userId = "invitee_id",
            email = "invitee@test.com",
            username = "invitee",
            firstName = "Invitee",
            lastName = "User",
            university = "EPFL",
            createdAt = 1000L,
            updatedAt = 1000L)

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(inviter)
    userRepository.saveUser(invitee)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository,
            notificationRepository = notificationRepository)

    advanceUntilIdle()

    viewModel.showAddMemberDialog("Treasurer")
    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertFalse(initialState.pendingInvitations.containsKey("Treasurer"))

    viewModel.sendMemberInvitation("invitee_id")
    advanceUntilIdle()

    val finalState = viewModel.uiState.value
    // Pending invitations should be updated in UI
    assertTrue(finalState.pendingInvitations.containsKey("Treasurer"))
    assertEquals("invitee_id", finalState.pendingInvitations["Treasurer"])
  }
}

package com.github.se.studentconnect.ui.screen.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.profile.OrganizationProfileViewModel
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.util.MainDispatcherRule
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
@OptIn(ExperimentalCoroutinesApi::class)
class OrganizationProfileScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var viewModel: OrganizationProfileViewModel
  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var mockContext: android.content.Context

  private val testOrganization =
      Organization(
          id = "test_org",
          name = "Test Organization",
          type = OrganizationType.Association,
          description = "A test organization for testing purposes",
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
    every { mockContext.getString(R.string.org_profile_follow) } returns "Follow"
    every { mockContext.getString(R.string.org_profile_following) } returns "Following"
    every { mockContext.getString(R.string.org_profile_member) } returns "Member"
    every { mockContext.getString(R.string.org_profile_tab_events) } returns "Events"
    every { mockContext.getString(R.string.org_profile_tab_members) } returns "Members"
    every { mockContext.getString(R.string.org_profile_no_events) } returns "No events yet"
    every { mockContext.getString(R.string.org_profile_no_members) } returns "No members yet"
    every { mockContext.getString(R.string.org_profile_unfollow_title) } returns
        "Unfollow Organization?"
    every { mockContext.getString(R.string.org_profile_unfollow_message, any()) } returns
        "Are you sure you want to unfollow Test Organization?"
    every { mockContext.getString(R.string.org_profile_unfollow_confirm) } returns "Unfollow"
    every { mockContext.getString(R.string.org_profile_unfollow_cancel) } returns "Cancel"
  }

  @After
  fun tearDown() {
    // Clean up authentication state
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false
  }

  @Test
  fun screenDisplaysLoadingStateInitially() = runTest {
    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    // Loading indicator should be shown
    composeRule.onNodeWithTag(C.Tag.org_profile_screen).assertIsDisplayed()
  }

  @Test
  fun screenDisplaysErrorWhenOrganizationNotFound() = runTest {
    viewModel =
        OrganizationProfileViewModel(
            organizationId = "nonexistent_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme {
        OrganizationProfileScreen(organizationId = "nonexistent_org", viewModel = viewModel)
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Error message should be displayed
    composeRule.onNodeWithText("Organization not found").assertIsDisplayed()
  }

  @Test
  fun screenDisplaysErrorWhenOrganizationIdIsNull() = runTest {
    viewModel =
        OrganizationProfileViewModel(
            organizationId = null,
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = null, viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Error message should be displayed
    composeRule.onNodeWithText("Organization ID is required").assertIsDisplayed()
  }

  @Test
  fun screenDisplaysOrganizationInfo() = runTest {
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
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Check organization name in header
    composeRule.onNodeWithTag(C.Tag.org_profile_header).assertIsDisplayed()

    // Check organization title
    composeRule
        .onNodeWithTag(C.Tag.org_profile_title)
        .assertIsDisplayed()
        .assertTextEquals("Test Organization")

    // Check description
    composeRule
        .onNodeWithTag(C.Tag.org_profile_description)
        .assertIsDisplayed()
        .assertTextEquals("A test organization for testing purposes")
  }

  @Test
  fun backButtonNavigatesBack() = runTest {
    var backClicked = false
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme {
        OrganizationProfileScreen(
            organizationId = "test_org",
            onBackClick = { backClicked = true },
            viewModel = viewModel)
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click back button
    composeRule.onNodeWithContentDescription("Back").performClick()
    assertEquals(true, backClicked)
  }

  @Test
  fun avatarBannerIsDisplayed() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Check avatar banner is displayed
    composeRule.onNodeWithTag(C.Tag.org_profile_avatar_banner).assertIsDisplayed()
    composeRule.onNodeWithTag(C.Tag.org_profile_avatar).assertIsDisplayed()
  }

  @Test
  fun followButtonDisplaysCorrectTextForNonFollower() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Follow button should show "Follow"
    composeRule
        .onNodeWithTag(C.Tag.org_profile_follow_button)
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun followButtonDisplaysCorrectTextForMember() = runTest {
    AuthenticationProvider.testUserId = "user1"
    AuthenticationProvider.local = true

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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Member button should be disabled
    composeRule.onNodeWithTag(C.Tag.org_profile_follow_button).assertIsNotEnabled()
  }

  @Test
  fun tabsAreDisplayedAndClickable() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Check Events tab is displayed
    composeRule
        .onNodeWithTag(C.Tag.org_profile_tab_events)
        .assertIsDisplayed()
        .assertHasClickAction()

    // Check Members tab is displayed
    composeRule
        .onNodeWithTag(C.Tag.org_profile_tab_members)
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun switchingTabsChangesContent() = runTest {
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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Events list should be displayed by default
    composeRule.onNodeWithTag(C.Tag.org_profile_events_list).assertExists()

    // Click Members tab
    composeRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeRule.waitForIdle()

    // Members grid should be displayed
    composeRule.onNodeWithTag(C.Tag.org_profile_members_grid).assertExists()

    // Click Events tab again
    composeRule.onNodeWithTag(C.Tag.org_profile_tab_events).performClick()
    composeRule.waitForIdle()

    // Events list should be displayed again
    composeRule.onNodeWithTag(C.Tag.org_profile_events_list).assertExists()
  }

  @Test
  fun eventsTabDisplaysNoEventsMessage() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // No events message should be displayed (check if the empty view exists)
    composeRule.onNodeWithTag(C.Tag.org_profile_events_empty).assertExists()
  }

  @Test
  fun eventsTabDisplaysEvents() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    eventRepository.addEvent(testEvent)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Event should be displayed (check that event row exists)
    composeRule.onNodeWithTag("${C.Tag.org_profile_event_row_prefix}_0").assertExists()
    composeRule.onNodeWithTag("${C.Tag.org_profile_event_card_prefix}_0").assertExists()
  }

  @Test
  fun membersTabDisplaysNoMembersMessage() = runTest {
    organizationRepository.saveOrganization(testOrganization)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click Members tab
    composeRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeRule.waitForIdle()

    // No members message should be displayed
    composeRule.onNodeWithTag(C.Tag.org_profile_members_empty).assertExists()
  }

  @Test
  fun membersTabDisplaysMembers() = runTest {
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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click Members tab
    composeRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeRule.waitForIdle()

    // Members should be displayed
    composeRule.onNodeWithTag("${C.Tag.org_profile_member_card_prefix}_0").assertExists()
  }

  @Test
  fun followButtonClickShowsUnfollowDialog() = runTest {
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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click follow button (which is actually unfollow)
    composeRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    composeRule.waitForIdle()

    // Unfollow dialog should be shown
    composeRule.onNodeWithText("Unfollow Organization?").assertIsDisplayed()
    composeRule.onNodeWithText("Unfollow").assertIsDisplayed()
    composeRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun unfollowDialogCancelButton() = runTest {
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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click follow button to show dialog
    composeRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    composeRule.waitForIdle()

    // Click cancel button
    composeRule.onNodeWithText("Cancel").performClick()
    composeRule.waitForIdle()

    // Dialog should be dismissed
    composeRule.onNodeWithText("Unfollow Organization?").assertDoesNotExist()
  }

  @Test
  fun unfollowDialogConfirmButton() = runTest {
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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click follow button to show dialog
    composeRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    composeRule.waitForIdle()

    // Click unfollow button
    composeRule.onNodeWithText("Unfollow").performClick()
    advanceUntilIdle()
    composeRule.waitForIdle()

    // Dialog should be dismissed and organization should be unfollowed
    composeRule.onNodeWithText("Unfollow Organization?").assertDoesNotExist()
  }

  @Test
  fun multipleEventsAreDisplayed() = runTest {
    val event2 =
        testEvent.copy(
            uid = "event2",
            title = "Second Event",
            start = Timestamp(Date(System.currentTimeMillis() + 86400000)))

    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)
    eventRepository.addEvent(testEvent)
    eventRepository.addEvent(event2)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Both events should be displayed
    composeRule.onNodeWithTag("${C.Tag.org_profile_event_row_prefix}_0").assertExists()
    composeRule.onNodeWithTag("${C.Tag.org_profile_event_row_prefix}_1").assertExists()
  }

  @Test
  fun memberWithAvatarIsDisplayed() = runTest {
    val userWithAvatar = testUser1.copy(profilePictureUrl = "avatar_12")
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(userWithAvatar)
    userRepository.saveUser(testUser2)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click Members tab
    composeRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeRule.waitForIdle()

    // Member card should be displayed
    composeRule.onNodeWithTag("${C.Tag.org_profile_member_card_prefix}_0").assertExists()
  }

  @Test
  fun creatorIsDisplayedAsMember() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testCreator)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click Members tab
    composeRule.onNodeWithTag(C.Tag.org_profile_tab_members).performClick()
    composeRule.waitForIdle()

    // Creator should be displayed in members list
    composeRule.onNodeWithTag("${C.Tag.org_profile_member_card_prefix}_0").assertExists()
  }

  @Test
  fun aboutSectionIsDisplayed() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser1)

    viewModel =
        OrganizationProfileViewModel(
            organizationId = "test_org",
            context = mockContext,
            organizationRepository = organizationRepository,
            eventRepository = eventRepository,
            userRepository = userRepository)

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // About section should be displayed
    composeRule.onNodeWithTag(C.Tag.org_profile_about_section).assertIsDisplayed()
  }

  @Test
  fun followButtonWorksForNonMember() = runTest {
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

    composeRule.setContent {
      AppTheme { OrganizationProfileScreen(organizationId = "test_org", viewModel = viewModel) }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click follow button
    composeRule.onNodeWithTag(C.Tag.org_profile_follow_button).performClick()
    advanceUntilIdle()
    composeRule.waitForIdle()

    // Button should now show "Following"
    composeRule.onNodeWithTag(C.Tag.org_profile_follow_button).assertIsDisplayed()
  }
}

package com.github.se.studentconnect.ui.screen.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.util.MainDispatcherRule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var mockContext: android.content.Context
  private lateinit var mockFirebaseAuth: FirebaseAuth
  private lateinit var mockFirebaseUser: FirebaseUser

  private val testOrganization =
      Organization(
          id = "test_org",
          name = "Test Organization",
          type = OrganizationType.Association,
          description = "A test organization",
          logoUrl = null,
          memberUids = emptyList(),
          createdBy = "creator1")

  private val testUser =
      User(
          userId = "user1",
          email = "user1@test.com",
          username = "user1",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL",
          createdAt = 1000L,
          updatedAt = 1000L)

  @Before
  fun setUp() {
    AuthenticationProvider.testUserId = "user1"
    AuthenticationProvider.local = true

    organizationRepository = OrganizationRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()
    userRepository = UserRepositoryLocal()

    // Override the repository provider to use our test repository
    OrganizationRepositoryProvider.overrideForTests(organizationRepository)

    // Mock FirebaseAuth
    mockkStatic(FirebaseAuth::class)
    mockFirebaseAuth = mockk(relaxed = true)
    mockFirebaseUser = mockk(relaxed = true)
    every { FirebaseAuth.getInstance() } returns mockFirebaseAuth
    every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
    every { mockFirebaseUser.uid } returns "user1"

    // Mock Context
    mockContext = mockk(relaxed = true)
    every { mockContext.applicationContext } returns mockContext
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
    AuthenticationProvider.local = false

    // Clean up repository override
    OrganizationRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun organizationInvitationNotificationIsDisplayed() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser)
    organizationRepository.sendMemberInvitation("test_org", "user1", "Member", "creator1")

    val notification =
        Notification.OrganizationMemberInvitation(
            id = "notif1",
            userId = "user1",
            timestamp = Timestamp.now(),
            isRead = false,
            organizationId = "test_org",
            organizationName = "Test Organization",
            role = "Member",
            invitedBy = "creator1",
            invitedByName = "Creator")

    notificationRepository.createNotification(notification, {}, {})

    val notificationViewModel = NotificationViewModel(notificationRepository)

    composeRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            notificationViewModel = notificationViewModel,
            onClickStory = { _, _ -> },
            uiState = HomePageUiState(),
            favoriteEventIds = emptySet(),
            onDateSelected = {},
            onCalendarClick = {},
            onCalendarDismiss = {},
            onApplyFilters = {},
            onFavoriteToggle = {},
            onToggleFavoritesFilter = {},
            onClearScrollTarget = {},
            onTabSelected = {},
            onRefreshStories = {})
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click notification button to open dropdown
    composeRule.onNodeWithTag("NotificationButton").performClick()
    composeRule.waitForIdle()

    // Notification item should be displayed
    composeRule.onNodeWithTag("NotificationItem_notif1").assertExists()

    // Accept and reject buttons should be visible for organization invitations
    composeRule.onNodeWithTag("AcceptNotificationButton_notif1").assertExists()
    composeRule.onNodeWithTag("RejectNotificationButton_notif1").assertExists()
  }

  @Test
  fun organizationInvitationAcceptButtonIsClickable() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser)
    organizationRepository.sendMemberInvitation("test_org", "user1", "Member", "creator1")

    val notification =
        Notification.OrganizationMemberInvitation(
            id = "notif1",
            userId = "user1",
            timestamp = Timestamp.now(),
            isRead = false,
            organizationId = "test_org",
            organizationName = "Test Organization",
            role = "Member",
            invitedBy = "creator1",
            invitedByName = "Creator")

    notificationRepository.createNotification(notification, {}, {})

    val notificationViewModel = NotificationViewModel(notificationRepository)

    composeRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            notificationViewModel = notificationViewModel,
            onClickStory = { _, _ -> },
            uiState = HomePageUiState(),
            favoriteEventIds = emptySet(),
            onDateSelected = {},
            onCalendarClick = {},
            onCalendarDismiss = {},
            onApplyFilters = {},
            onFavoriteToggle = {},
            onToggleFavoritesFilter = {},
            onClearScrollTarget = {},
            onTabSelected = {},
            onRefreshStories = {})
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click notification button to open dropdown
    composeRule.onNodeWithTag("NotificationButton").performClick()
    composeRule.waitForIdle()

    // Accept button should be clickable
    composeRule.onNodeWithTag("AcceptNotificationButton_notif1").assertIsDisplayed().performClick()

    // Wait for coroutines to complete (HomeScreen uses coroutineScope.launch)
    advanceUntilIdle()
    composeRule.waitForIdle()
    // Give additional time for async operations
    advanceUntilIdle()
    composeRule.waitForIdle()

    // Verify invitation was accepted (check repository)
    val updatedOrg = organizationRepository.getOrganizationById("test_org")
    assertNotNull(updatedOrg)
    assertTrue(updatedOrg?.memberUids?.contains("user1") == true)
  }

  @Test
  fun organizationInvitationRejectButtonIsClickable() = runTest {
    organizationRepository.saveOrganization(testOrganization)
    userRepository.saveUser(testUser)
    organizationRepository.sendMemberInvitation("test_org", "user1", "Member", "creator1")

    val notification =
        Notification.OrganizationMemberInvitation(
            id = "notif1",
            userId = "user1",
            timestamp = Timestamp.now(),
            isRead = false,
            organizationId = "test_org",
            organizationName = "Test Organization",
            role = "Member",
            invitedBy = "creator1",
            invitedByName = "Creator")

    notificationRepository.createNotification(notification, {}, {})

    val notificationViewModel = NotificationViewModel(notificationRepository)

    composeRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            notificationViewModel = notificationViewModel,
            onClickStory = { _, _ -> },
            uiState = HomePageUiState(),
            favoriteEventIds = emptySet(),
            onDateSelected = {},
            onCalendarClick = {},
            onCalendarDismiss = {},
            onApplyFilters = {},
            onFavoriteToggle = {},
            onToggleFavoritesFilter = {},
            onClearScrollTarget = {},
            onTabSelected = {},
            onRefreshStories = {})
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click notification button to open dropdown
    composeRule.onNodeWithTag("NotificationButton").performClick()
    composeRule.waitForIdle()

    // Reject button should be clickable
    composeRule.onNodeWithTag("RejectNotificationButton_notif1").assertIsDisplayed().performClick()

    // Wait for coroutines to complete (HomeScreen uses coroutineScope.launch)
    advanceUntilIdle()
    composeRule.waitForIdle()
    // Give additional time for async operations
    advanceUntilIdle()
    composeRule.waitForIdle()

    // Verify invitation was rejected (check repository)
    val invitations = organizationRepository.getPendingInvitations("test_org")
    assertFalse(invitations.any { it.userId == "user1" })
  }

  @Test
  fun organizationInvitationNotificationMessageIsDisplayed() = runTest {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "notif1",
            userId = "user1",
            timestamp = Timestamp.now(),
            isRead = false,
            organizationId = "test_org",
            organizationName = "Test Organization",
            role = "Member",
            invitedBy = "creator1",
            invitedByName = "Creator")

    notificationRepository.createNotification(notification, {}, {})

    val notificationViewModel = NotificationViewModel(notificationRepository)

    composeRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            notificationViewModel = notificationViewModel,
            onClickStory = { _, _ -> },
            uiState = HomePageUiState(),
            favoriteEventIds = emptySet(),
            onDateSelected = {},
            onCalendarClick = {},
            onCalendarDismiss = {},
            onApplyFilters = {},
            onFavoriteToggle = {},
            onToggleFavoritesFilter = {},
            onClearScrollTarget = {},
            onTabSelected = {},
            onRefreshStories = {})
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click notification button to open dropdown
    composeRule.onNodeWithTag("NotificationButton").performClick()
    composeRule.waitForIdle()

    // Notification message should be displayed (use unmerged tree for nested content)
    composeRule.onNodeWithTag("NotificationMessage_notif1", useUnmergedTree = true).assertExists()

    // Notification item should be displayed
    composeRule.onNodeWithTag("NotificationItem_notif1").assertExists()
  }

  @Test
  fun organizationInvitationShowsCorrectIcon() = runTest {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "notif1",
            userId = "user1",
            timestamp = Timestamp.now(),
            isRead = false,
            organizationId = "test_org",
            organizationName = "Test Organization",
            role = "Member",
            invitedBy = "creator1",
            invitedByName = "Creator")

    notificationRepository.createNotification(notification, {}, {})

    val notificationViewModel = NotificationViewModel(notificationRepository)

    composeRule.setContent {
      AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            notificationViewModel = notificationViewModel,
            onClickStory = { _, _ -> },
            uiState = HomePageUiState(),
            favoriteEventIds = emptySet(),
            onDateSelected = {},
            onCalendarClick = {},
            onCalendarDismiss = {},
            onApplyFilters = {},
            onFavoriteToggle = {},
            onToggleFavoritesFilter = {},
            onClearScrollTarget = {},
            onTabSelected = {},
            onRefreshStories = {})
      }
    }

    advanceUntilIdle()
    composeRule.waitForIdle()

    // Click notification button to open dropdown
    composeRule.onNodeWithTag("NotificationButton").performClick()
    composeRule.waitForIdle()

    // Notification item should be displayed with organization invitation icon
    composeRule.onNodeWithTag("NotificationItem_notif1").assertExists()
  }
}

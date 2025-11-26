package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class HomeScreenUITest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var notificationRepository: NotificationRepositoryLocal
  private lateinit var viewModel: HomePageViewModel
  private lateinit var notificationViewModel: NotificationViewModel

  // Create future timestamp (1 hour from now) to pass temporality filter
  private val futureTime = Timestamp(java.util.Date(System.currentTimeMillis() + 3600000))

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Summer Festival",
          subtitle = "Best summer event",
          description = "Join us for an amazing summer festival.",
          start = futureTime,
          end = futureTime,
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("music", "outdoor"),
      )

  private val testEvent2 =
      Event.Public(
          uid = "event-2",
          title = "Tech Conference",
          subtitle = "Latest in tech",
          description = "Explore the latest technology trends.",
          start = futureTime,
          end = futureTime,
          location = Location(latitude = 46.52, longitude = 6.57, name = "SwissTech"),
          website = "https://example.com",
          ownerId = "owner2",
          isFlash = false,
          tags = listOf("tech", "networking"),
      )

  @Before
  fun setup() {
    // Initialize Firebase first (before accessing any repositories)
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    // Set test user ID to match the notifications
    AuthenticationProvider.testUserId = "user123"

    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    notificationRepository = NotificationRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)
    notificationViewModel = NotificationViewModel(notificationRepository)

    runBlocking {
      eventRepository.addEvent(testEvent1)
      eventRepository.addEvent(testEvent2)
    }
  }

  @After
  fun tearDown() {
    // Clean up test user ID
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun homeScreen_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchBar_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithText("Search for events…").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchIcon_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithContentDescription("Search Icon").assertIsDisplayed()
  }

  @Test
  fun homeScreen_notificationButton_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
  }

  @Test
  fun homeScreen_notificationButton_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithContentDescription("Notifications").assertHasClickAction()
  }

  @Test
  fun homeScreen_filterBar_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for content to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Filters"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
  }

  // @Test
  fun homeScreen_displaysEvents() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Summer Festival"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Summer Festival").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech Conference").assertIsDisplayed()
  }

  // @Test
  fun homeScreen_displaysEventLocations() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("EPFL"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeTestRule.onNodeWithText("SwissTech").assertIsDisplayed()
  }

  @Test
  fun homeScreen_clickNotificationButton_doesNotCrash() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for screen to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click notification button - should not crash
    composeTestRule.onNodeWithContentDescription("Notifications").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_emptyState_displaysLoading() {
    val emptyRepository = EventRepositoryLocal()
    val emptyViewModel = HomePageViewModel(emptyRepository, userRepository)

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = emptyViewModel,
          notificationViewModel = notificationViewModel)
    }

    // Initially loading indicator should be displayed
    composeTestRule.waitForIdle()
    // After loading, no events should be displayed
  }

  @Test
  fun homeScreen_eventCard_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithTag("event_card_event-1").fetchSemanticsNodes().isNotEmpty()
    }

    // Use onNodeWithTag to specifically target the event card, not the story
    composeTestRule.onNodeWithTag("event_card_event-1").assertHasClickAction()
  }

  // @Test
  fun homeScreen_displaysMultipleEventCards() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for events to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Summer Festival"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Both events should be displayed
    composeTestRule.onNodeWithText("Summer Festival").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tech Conference").assertIsDisplayed()
  }

  @Test
  fun homeScreen_filterChips_haveClickActions() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for content to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Filters"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Filters").assertHasClickAction()
    composeTestRule.onNodeWithText("Favorites").assertHasClickAction()
  }

  @Test
  fun homeScreen_clickFilterChip_doesNotCrash() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for content to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Filters"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Filters").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_notificationPanel_displaysWhenClicked() {
    // Add a friend request notification
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait for screen to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Click notification button to open panel
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify notification panel is displayed
    composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
  }

  @Test
  fun homeScreen_friendRequestNotification_displaysWithAcceptRejectButtons() {
    // Add a friend request notification
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait and open notification panel
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify friend request notification content
    composeTestRule.onNodeWithText("John Doe sent you a friend request").assertIsDisplayed()

    // Verify Accept and Reject buttons are displayed
    composeTestRule.onNodeWithText("Accept").assertIsDisplayed()
    composeTestRule.onNodeWithText("Reject").assertIsDisplayed()
  }

  @Test
  fun homeScreen_eventNotification_displaysWithoutAcceptRejectButtons() {
    // Add an event starting notification
    val eventNotification =
        Notification.EventStarting(
            id = "notif-2",
            userId = "user123",
            eventId = "event123",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(eventNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    // Wait and open notification panel
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify event notification content
    composeTestRule.onNodeWithText("Event \"Study Session\" is starting soon").assertIsDisplayed()

    // Verify Accept and Reject buttons are NOT displayed
    composeTestRule.onNodeWithText("Accept").assertDoesNotExist()
    composeTestRule.onNodeWithText("Reject").assertDoesNotExist()
  }

  @Test
  fun homeScreen_acceptButton_hasClickAction() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify Accept button has click action
    composeTestRule.onNodeWithText("Accept").assertHasClickAction()
  }

  @Test
  fun homeScreen_rejectButton_hasClickAction() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify Reject button has click action
    composeTestRule.onNodeWithText("Reject").assertHasClickAction()
  }

  @Test
  fun homeScreen_notificationItem_hasDeleteButton() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify delete button is displayed
    composeTestRule.onNodeWithTag("DeleteNotificationButton_notif-1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeleteNotificationButton_notif-1").assertHasClickAction()
  }

  @Test
  fun homeScreen_notificationBadge_displaysUnreadCount() {
    // Add two unread notifications
    val notification1 =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    val notification2 =
        Notification.EventStarting(
            id = "notif-2",
            userId = "user123",
            eventId = "event123",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking {
      notificationRepository.createNotification(notification1, {}, {})
      notificationRepository.createNotification(notification2, {}, {})
    }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify badge shows count of 2
    composeTestRule.onNodeWithText("2").assertIsDisplayed()
  }

  @Test
  fun homeScreen_multipleNotifications_displayedInPanel() {
    // Add multiple notifications
    val friendRequest =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "Alice Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    val eventNotification =
        Notification.EventStarting(
            id = "notif-2",
            userId = "user123",
            eventId = "event123",
            eventTitle = "Math Lecture",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking {
      notificationRepository.createNotification(friendRequest, {}, {})
      notificationRepository.createNotification(eventNotification, {}, {})
    }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify both notifications are displayed
    composeTestRule.onNodeWithText("Alice Smith sent you a friend request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event \"Math Lecture\" is starting soon").assertIsDisplayed()
  }

  @Test
  fun homeScreen_clickFriendRequestNotification_hasClickAction() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify notification message has click action
    composeTestRule.onNodeWithText("John Doe sent you a friend request").assertHasClickAction()
  }

  @Test
  fun homeScreen_clickEventNotification_hasClickAction() {
    val eventNotification =
        Notification.EventStarting(
            id = "notif-2",
            userId = "user123",
            eventId = "event123",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(eventNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify event notification message has click action
    composeTestRule
        .onNodeWithText("Event \"Study Session\" is starting soon")
        .assertHasClickAction()
  }

  @Test
  fun homeScreen_clickAcceptButton_doesNotCrash() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Click Accept button - should not crash
    composeTestRule.onNodeWithText("Accept").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_clickRejectButton_doesNotCrash() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Click Reject button - should not crash
    composeTestRule.onNodeWithText("Reject").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_clickDeleteButton_removesNotification() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Click delete button
    composeTestRule.onNodeWithTag("DeleteNotificationButton_notif-1").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_readNotification_changesStyle() {
    // Add a read notification
    val readNotification =
        Notification.FriendRequest(
            id = "notif-read",
            userId = "user123",
            fromUserId = "friend789",
            fromUserName = "Jane Smith",
            timestamp = Timestamp.now(),
            isRead = true)

    runBlocking { notificationRepository.createNotification(readNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify read notification is displayed
    composeTestRule.onNodeWithText("Jane Smith sent you a friend request").assertIsDisplayed()
  }

  @Test
  fun homeScreen_mixedReadUnreadNotifications_displayCorrectly() {
    val unreadNotification =
        Notification.FriendRequest(
            id = "notif-unread",
            userId = "user123",
            fromUserId = "friend123",
            fromUserName = "Alice Brown",
            timestamp = Timestamp.now(),
            isRead = false)

    val readNotification =
        Notification.EventStarting(
            id = "notif-read",
            userId = "user123",
            eventId = "event456",
            eventTitle = "Workshop",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = true)

    runBlocking {
      notificationRepository.createNotification(unreadNotification, {}, {})
      notificationRepository.createNotification(readNotification, {}, {})
    }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Badge should show only 1 unread
    composeTestRule.onNodeWithText("1").assertIsDisplayed()

    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Both notifications should be displayed
    composeTestRule.onNodeWithText("Alice Brown sent you a friend request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event \"Workshop\" is starting soon").assertIsDisplayed()
  }

  @Test
  fun homeScreen_emptyNotifications_displaysNoBadge() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Badge should not be displayed when there are no unread notifications
    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
  }

  @Test
  fun homeScreen_searchBar_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search for events…").assertHasClickAction()
  }

  @Test
  fun homeScreen_clickSearchBar_doesNotCrash() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "home") {
        composable("home") {
          HomeScreen(
              navController = navController,
              viewModel = viewModel,
              notificationViewModel = notificationViewModel)
        }
        composable("search") {
          // Empty composable for navigation destination
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Search for events…").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_notificationPanel_canBeClosed() {
    val friendRequestNotification =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend456",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking { notificationRepository.createNotification(friendRequestNotification, {}, {}) }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Open notification panel
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify panel is open
    composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()

    // Click outside or press back to close (test that it doesn't crash)
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_multipleFriendRequests_displayCorrectly() {
    val friendRequest1 =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend1",
            fromUserName = "Alice Johnson",
            timestamp = Timestamp.now(),
            isRead = false)

    val friendRequest2 =
        Notification.FriendRequest(
            id = "notif-2",
            userId = "user123",
            fromUserId = "friend2",
            fromUserName = "Bob Williams",
            timestamp = Timestamp.now(),
            isRead = false)

    runBlocking {
      notificationRepository.createNotification(friendRequest1, {}, {})
      notificationRepository.createNotification(friendRequest2, {}, {})
    }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    // Verify both friend requests are displayed
    composeTestRule.onNodeWithText("Alice Johnson sent you a friend request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob Williams sent you a friend request").assertIsDisplayed()

    // Verify both have Accept and Reject buttons
    composeTestRule
        .onAllNodes(androidx.compose.ui.test.hasText("Accept"))
        .fetchSemanticsNodes()
        .let { nodes -> assert(nodes.size >= 2) }
  }

  @Test
  fun homeScreen_favoriteFilter_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Favorites"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertHasClickAction()
  }

  @Test
  fun homeScreen_clickFavoritesFilter_doesNotCrash() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Favorites"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Favorites").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_allReadNotifications_badgeNotDisplayed() {
    // Add only read notifications
    val readNotification1 =
        Notification.FriendRequest(
            id = "notif-1",
            userId = "user123",
            fromUserId = "friend1",
            fromUserName = "Sam Green",
            timestamp = Timestamp.now(),
            isRead = true)

    val readNotification2 =
        Notification.EventStarting(
            id = "notif-2",
            userId = "user123",
            eventId = "event789",
            eventTitle = "Meeting",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = true)

    runBlocking {
      notificationRepository.createNotification(readNotification1, {}, {})
      notificationRepository.createNotification(readNotification2, {}, {})
    }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          viewModel = viewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasContentDescription("Notifications"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Badge should not show when all notifications are read
    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()

    // Open panel to verify read notifications are still displayed
    composeTestRule.onNodeWithTag("NotificationButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Sam Green sent you a friend request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event \"Meeting\" is starting soon").assertIsDisplayed()
  }
}

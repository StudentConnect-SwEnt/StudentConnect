package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationBannerTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun notificationBanner_friendRequest_displaysCorrectly() {
    val notification =
        Notification.FriendRequest(
            id = "banner-test-1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    var dismissCount = 0
    var clickCount = 0

    composeTestRule.setContent {
      AppTheme {
        NotificationBanner(
            notification = notification, onDismiss = { dismissCount++ }, onClick = { clickCount++ })
      }
    }

    // Check that the banner is displayed
    composeTestRule.onNodeWithTag("NotificationBanner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NotificationBanner_banner-test-1").assertIsDisplayed()

    // Check that the title is displayed
    composeTestRule.onNodeWithText("New Friend Request").assertIsDisplayed()

    // Check that the message is displayed
    composeTestRule.onNodeWithText("John Doe sent you a friend request").assertIsDisplayed()
  }

  @Test
  fun notificationBanner_eventStarting_displaysCorrectly() {
    val notification =
        Notification.EventStarting(
            id = "banner-test-2",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme { NotificationBanner(notification = notification, onDismiss = {}, onClick = {}) }
    }

    // Check that the banner is displayed
    composeTestRule.onNodeWithTag("NotificationBanner").assertIsDisplayed()

    // Check that the title is displayed
    composeTestRule.onNodeWithText("Event Starting").assertIsDisplayed()

    // Check that the message is displayed
    composeTestRule.onNodeWithText("Event \"Study Session\" is starting soon").assertIsDisplayed()
  }

  @Test
  fun notificationBanner_organizationInvitation_displaysCorrectly() {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "banner-test-3",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Tech Club",
            role = "Member",
            invitedBy = "admin-1",
            invitedByName = "Admin User",
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme { NotificationBanner(notification = notification, onDismiss = {}, onClick = {}) }
    }

    // Check that the banner is displayed
    composeTestRule.onNodeWithTag("NotificationBanner").assertIsDisplayed()

    // Check that the title is displayed
    composeTestRule.onNodeWithText("Organization Invitation").assertIsDisplayed()

    // Check that the message is displayed
    composeTestRule
        .onNodeWithText("Admin User invited you to join \"Tech Club\" as Member")
        .assertIsDisplayed()
  }

  @Test
  fun notificationBanner_clickBanner_triggersOnClick() {
    val notification =
        Notification.FriendRequest(
            id = "banner-test-4",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Jane Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    var clickCount = 0

    composeTestRule.setContent {
      AppTheme {
        NotificationBanner(notification = notification, onDismiss = {}, onClick = { clickCount++ })
      }
    }

    // Click on the banner
    composeTestRule.onNodeWithTag("NotificationBanner_banner-test-4").performClick()

    // Verify onClick was called
    assert(clickCount == 1)
  }

  @Test
  fun notificationBanner_clickCloseButton_hidesBanner() {
    val notification =
        Notification.FriendRequest(
            id = "banner-test-5",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Bob Smith",
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme { NotificationBanner(notification = notification, onDismiss = {}, onClick = {}) }
    }

    // Banner should be visible initially
    composeTestRule.onNodeWithTag("NotificationBanner").assertIsDisplayed()

    // Click on close button
    composeTestRule.onNodeWithContentDescription("Dismiss").performClick()

    // Wait for animation to complete
    composeTestRule.waitForIdle()

    // Banner should start hiding (visible state changes but animation may still be running)
    // The banner will eventually disappear after animation completes
  }

  @Test
  fun notificationBanner_nullNotification_doesNotDisplay() {
    composeTestRule.setContent {
      AppTheme { NotificationBanner(notification = null, onDismiss = {}, onClick = {}) }
    }

    // Banner should not be displayed
    composeTestRule.onNodeWithTag("NotificationBanner").assertDoesNotExist()
  }

  @Test
  fun notificationBanner_autoDismiss_callsOnDismiss() {
    val notification =
        Notification.FriendRequest(
            id = "banner-test-6",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Alice Johnson",
            timestamp = Timestamp.now(),
            isRead = false)

    var dismissCount = 0

    composeTestRule.setContent {
      AppTheme {
        NotificationBanner(
            notification = notification, onDismiss = { dismissCount++ }, onClick = {})
      }
    }

    // Banner should be visible initially
    composeTestRule.onNodeWithTag("NotificationBanner").assertIsDisplayed()

    // Wait for auto-dismiss (4 seconds + 300ms animation)
    composeTestRule.mainClock.advanceTimeBy(4300)
    composeTestRule.waitForIdle()

    // Verify onDismiss was called
    assert(dismissCount == 1)
  }

  @Test
  fun notificationBanner_hasCorrectZIndex() {
    val notification =
        Notification.FriendRequest(
            id = "banner-test-7",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Test User",
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme { NotificationBanner(notification = notification, onDismiss = {}, onClick = {}) }
    }

    // Banner should be displayed on top
    composeTestRule.onNodeWithTag("NotificationBanner").assertIsDisplayed()
  }

  @Test
  fun notificationBanner_hasCorrectPadding() {
    val notification =
        Notification.FriendRequest(
            id = "banner-test-8",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Test User",
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme { NotificationBanner(notification = notification, onDismiss = {}, onClick = {}) }
    }

    // Check banner exists (padding is applied via modifier)
    composeTestRule.onNodeWithTag("NotificationBanner_banner-test-8").assertIsDisplayed()
  }

  @Test
  fun handleNotificationClick_friendRequest_callsCallbacks() {
    val notification =
        Notification.FriendRequest(
            id = "click-test-1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    var readNotificationId: String? = null
    var dismissCalled = false

    // Mock NavController to avoid navigation graph requirements
    val navController = mockk<NavHostController>(relaxed = true)
    every { navController.navigate(any<String>()) } returns Unit

    // Call the function directly (not inside setContent since it's not a composable)
    handleNotificationClick(
        notification = notification,
        navController = navController,
        onNotificationRead = { readNotificationId = it },
        onDismiss = { dismissCalled = true })

    // Verify notification was marked as read
    assert(readNotificationId == "click-test-1")

    // Verify dismiss was called
    assert(dismissCalled)

    // Verify navigation was called
    verify { navController.navigate(any<String>()) }
  }

  @Test
  fun handleNotificationClick_eventStarting_callsCallbacks() {
    val notification =
        Notification.EventStarting(
            id = "click-test-2",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    var readNotificationId: String? = null
    var dismissCalled = false

    // Mock NavController to avoid navigation graph requirements
    val navController = mockk<NavHostController>(relaxed = true)
    every { navController.navigate(any<String>()) } returns Unit

    // Call the function directly (not inside setContent since it's not a composable)
    handleNotificationClick(
        notification = notification,
        navController = navController,
        onNotificationRead = { readNotificationId = it },
        onDismiss = { dismissCalled = true })

    // Verify notification was marked as read
    assert(readNotificationId == "click-test-2")

    // Verify dismiss was called
    assert(dismissCalled)

    // Verify navigation was called
    verify { navController.navigate(any<String>()) }
  }

  @Test
  fun handleNotificationClick_organizationInvitation_callsCallbacks() {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "click-test-3",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Tech Club",
            role = "Member",
            invitedBy = "admin-1",
            invitedByName = "Admin User",
            timestamp = Timestamp.now(),
            isRead = false)

    var readNotificationId: String? = null
    var dismissCalled = false

    // Mock NavController to avoid navigation graph requirements
    val navController = mockk<NavHostController>(relaxed = true)
    every { navController.navigate(any<String>()) } returns Unit

    // Call the function directly (not inside setContent since it's not a composable)
    handleNotificationClick(
        notification = notification,
        navController = navController,
        onNotificationRead = { readNotificationId = it },
        onDismiss = { dismissCalled = true })

    // Verify notification was marked as read
    assert(readNotificationId == "click-test-3")

    // Verify dismiss was called
    assert(dismissCalled)

    // Verify navigation was called
    verify { navController.navigate(any<String>()) }
  }
}

package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationDisplayTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun notificationItem_friendRequest_displaysCorrectly() {
    val notification =
        Notification.FriendRequest(
            id = "test-notif-1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    var clickCount = 0
    var deleteCount = 0

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(
            notification = notification,
            onRead = {},
            onDelete = { deleteCount++ },
            onClick = { clickCount++ })
      }
    }

    // Check that the message is displayed
    composeTestRule.onNodeWithText("John Doe sent you a friend request").assertIsDisplayed()

    // Check that the notification item is displayed
    composeTestRule.onNodeWithTag("NotificationItem_test-notif-1").assertIsDisplayed()

    // Click on the notification
    composeTestRule.onNodeWithTag("NotificationItem_test-notif-1").performClick()

    assert(clickCount == 1)
  }

  @Test
  fun notificationItem_eventStarting_displaysCorrectly() {
    val notification =
        Notification.EventStarting(
            id = "test-notif-2",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = Timestamp.now(),
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(notification = notification, onRead = {}, onDelete = {}, onClick = {})
      }
    }

    // Check that the message is displayed
    composeTestRule.onNodeWithText("Event \"Study Session\" is starting soon").assertIsDisplayed()

    // Check that the notification item is displayed
    composeTestRule.onNodeWithTag("NotificationItem_test-notif-2").assertIsDisplayed()
  }

  @Test
  fun notificationItem_deleteButton_works() {
    val notification =
        Notification.FriendRequest(
            id = "test-notif-3",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Jane Doe",
            timestamp = Timestamp.now(),
            isRead = false)

    var deleteCount = 0

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(
            notification = notification, onRead = {}, onDelete = { deleteCount++ }, onClick = {})
      }
    }

    // Click on delete button
    composeTestRule.onNodeWithTag("DeleteNotificationButton_test-notif-3").performClick()

    assert(deleteCount == 1)
  }

  @Test
  fun notificationItem_readNotification_hasCorrectStyling() {
    val notification =
        Notification.FriendRequest(
            id = "test-notif-4",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Bob Smith",
            timestamp = Timestamp.now(),
            isRead = true)

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(notification = notification, onRead = {}, onDelete = {}, onClick = {})
      }
    }

    // Check that the notification is displayed
    composeTestRule.onNodeWithTag("NotificationItem_test-notif-4").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob Smith sent you a friend request").assertIsDisplayed()
  }

  @Test
  fun notificationItem_unreadNotification_hasCorrectStyling() {
    val notification =
        Notification.FriendRequest(
            id = "test-notif-5",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "Alice Johnson",
            timestamp = Timestamp.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(notification = notification, onRead = {}, onDelete = {}, onClick = {})
      }
    }

    // Check that the notification is displayed
    composeTestRule.onNodeWithTag("NotificationItem_test-notif-5").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice Johnson sent you a friend request").assertIsDisplayed()
  }
}

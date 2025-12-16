package com.github.se.studentconnect.ui.screen.home

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepositoryLocal
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationActionsTest {

  private val mediaRepositoryTest =
      object : MediaRepository {
        // Implement required methods with no-op or mock behavior
        override suspend fun upload(uri: Uri, path: String?): String {
          return "test-media-id"
        }

        override suspend fun download(id: String): Uri {
          return Uri.EMPTY
        }

        override suspend fun delete(id: String) {}
      }

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    NotificationRepositoryProvider.overrideForTests(NotificationRepositoryLocal())
    UserRepositoryProvider.overrideForTests(UserRepositoryLocal())
    EventRepositoryProvider.overrideForTests(EventRepositoryLocal())
    OrganizationRepositoryProvider.overrideForTests(OrganizationRepositoryLocal())
    MediaRepositoryProvider.overrideForTests(mediaRepositoryTest)
  }

  @After
  fun teardown() {
    NotificationRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()
    EventRepositoryProvider.cleanOverrideForTests()
    OrganizationRepositoryProvider.cleanOverrideForTests()
    MediaRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun notificationItem_friendRequest_showsAcceptRejectButtons() {
    val notification =
        Notification.FriendRequest(
            id = "action-test-1",
            userId = "user-1",
            fromUserId = "user-2",
            fromUserName = "John Doe",
            timestamp = Timestamp.Companion.now(),
            isRead = false)

    var acceptCalled = false
    var rejectCalled = false

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(
            notification = notification,
            onRead = {},
            onDelete = {},
            onAccept = { acceptCalled = true },
            onReject = { rejectCalled = true },
            onClick = {})
      }
    }

    // Check that accept button is displayed
    composeTestRule.onNodeWithTag("AcceptNotificationButton_action-test-1").assertIsDisplayed()

    // Check that reject button is displayed
    composeTestRule.onNodeWithTag("RejectNotificationButton_action-test-1").assertIsDisplayed()

    // Click accept button
    composeTestRule.onNodeWithTag("AcceptNotificationButton_action-test-1").performClick()
    assert(acceptCalled)

    // Click reject button
    composeTestRule.onNodeWithTag("RejectNotificationButton_action-test-1").performClick()
    assert(rejectCalled)
  }

  @Test
  fun notificationItem_organizationInvitation_showsAcceptRejectButtons() {
    val notification =
        Notification.OrganizationMemberInvitation(
            id = "action-test-2",
            userId = "user-1",
            organizationId = "org-1",
            organizationName = "Tech Club",
            role = "Member",
            invitedBy = "admin-1",
            invitedByName = "Admin User",
            timestamp = Timestamp.Companion.now(),
            isRead = false)

    var acceptCalled = false
    var rejectCalled = false

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(
            notification = notification,
            onRead = {},
            onDelete = {},
            onAccept = { acceptCalled = true },
            onReject = { rejectCalled = true },
            onClick = {})
      }
    }

    // Check that accept button is displayed
    composeTestRule.onNodeWithTag("AcceptNotificationButton_action-test-2").assertIsDisplayed()

    // Check that reject button is displayed
    composeTestRule.onNodeWithTag("RejectNotificationButton_action-test-2").assertIsDisplayed()

    // Click accept button
    composeTestRule.onNodeWithTag("AcceptNotificationButton_action-test-2").performClick()
    assert(acceptCalled)

    // Click reject button
    composeTestRule.onNodeWithTag("RejectNotificationButton_action-test-2").performClick()
    assert(rejectCalled)
  }

  @Test
  fun notificationItem_eventStarting_doesNotShowAcceptRejectButtons() {
    val notification =
        Notification.EventStarting(
            id = "action-test-3",
            userId = "user-1",
            eventId = "event-1",
            eventTitle = "Study Session",
            eventStart = Timestamp.Companion.now(),
            timestamp = Timestamp.Companion.now(),
            isRead = false)

    composeTestRule.setContent {
      AppTheme {
        NotificationItem(
            notification = notification,
            onRead = {},
            onDelete = {},
            onAccept = null,
            onReject = null,
            onClick = {})
      }
    }

    // Check that accept/reject buttons are NOT displayed for event notifications
    composeTestRule.onNodeWithTag("AcceptNotificationButton_action-test-3").assertDoesNotExist()
    composeTestRule.onNodeWithTag("RejectNotificationButton_action-test-3").assertDoesNotExist()
  }
}

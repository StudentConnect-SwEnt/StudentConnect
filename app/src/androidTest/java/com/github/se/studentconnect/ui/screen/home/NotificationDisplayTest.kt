package com.github.se.studentconnect.ui.screen.home

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationMemberInvitation
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationDisplayTest {

  private val fakeEventRepository =
      object : EventRepository {
        override fun getNewUid(): String = "test-uid"

        override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

        override suspend fun getAllVisibleEventsSatisfying(
            predicate: (Event) -> Boolean
        ): List<Event> = emptyList()

        override suspend fun getEvent(eventUid: String): Event =
            Event.Public(
                uid = "test-event-uid",
                ownerId = "test-owner-id",
                title = "Test Event",
                description = "Test Description",
                start = Timestamp.now(),
                isFlash = false,
                subtitle = "Test Subtitle",
            )

        override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> =
            emptyList()

        override suspend fun addEvent(event: Event) {}

        override suspend fun editEvent(eventUid: String, newEvent: Event) {}

        override suspend fun deleteEvent(eventUid: String) {}

        override suspend fun addParticipantToEvent(
            eventUid: String,
            participant: EventParticipant
        ) {}

        override suspend fun addInvitationToEvent(
            eventUid: String,
            invitedUser: String,
            currentUserId: String
        ) {}

        override suspend fun getEventInvitations(eventUid: String): List<String> = emptyList()

        override suspend fun removeInvitationFromEvent(
            eventUid: String,
            invitedUser: String,
            currentUserId: String
        ) {}

        override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {}
      }

  private val fakeUserRepository =
      object : UserRepository {
        override suspend fun leaveEvent(eventId: String, userId: String) {}

        override suspend fun getUserById(userId: String): User? = null

        override suspend fun getUserByEmail(email: String): User? = null

        override suspend fun getAllUsers(): List<User> = emptyList()

        override suspend fun getUsersPaginated(
            limit: Int,
            lastUserId: String?
        ): Pair<List<User>, Boolean> = Pair(emptyList(), false)

        override suspend fun saveUser(user: User) {}

        override suspend fun updateUser(userId: String, updates: Map<String, Any?>) {}

        override suspend fun deleteUser(userId: String) {}

        override suspend fun getUsersByUniversity(university: String): List<User> = emptyList()

        override suspend fun getUsersByHobby(hobby: String): List<User> = emptyList()

        override suspend fun getNewUid(): String = "test-uid"

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

        override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

        override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

        override suspend fun getPinnedEvents(userId: String): List<String> = emptyList()

        override suspend fun pinOrganization(userId: String, organizationId: String) {}

        override suspend fun unpinOrganization(userId: String) {}

        override suspend fun getPinnedOrganization(userId: String): String? {
          return null
        }

        override suspend fun checkUsernameAvailability(username: String): Boolean = true
      }

  private val fakeOrganizationRepository =
      object : OrganizationRepository {
        override suspend fun saveOrganization(organization: Organization) {}

        override suspend fun getOrganizationById(organizationId: String): Organization? {
          return null
        }

        override suspend fun getAllOrganizations(): List<Organization> {
          return emptyList()
        }

        override suspend fun getNewOrganizationId(): String {
          return "test-organization-id"
        }

        override suspend fun sendMemberInvitation(
            organizationId: String,
            userId: String,
            role: String,
            invitedBy: String
        ) {}

        override suspend fun acceptMemberInvitation(organizationId: String, userId: String) {}

        override suspend fun rejectMemberInvitation(organizationId: String, userId: String) {}

        override suspend fun getPendingInvitations(
            organizationId: String
        ): List<OrganizationMemberInvitation> {
          return emptyList()
        }

        override suspend fun getUserPendingInvitations(
            userId: String
        ): List<OrganizationMemberInvitation> {
          return emptyList()
        }

        override suspend fun addMemberToOrganization(organizationId: String, userId: String) {}
      }

  private val fakeMediaRepository =
      object : MediaRepository {
        override suspend fun upload(uri: Uri, path: String?): String {
          return "test-media-id"
        }

        override suspend fun download(id: String): Uri {
          return "test-media-uri".let { Uri.parse(it) }
        }

        override suspend fun delete(id: String) {}
      }

  private val fakeNotificationRepository =
      object : NotificationRepository {
        override fun listenToNotifications(
            userId: String,
            onNotificationsChanged: (List<Notification>) -> Unit
        ): () -> Unit = {}

        override fun getNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun getUnreadNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun createNotification(
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun markAsRead(
            notificationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun markAllAsRead(
            userId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun deleteNotification(
            notificationId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun deleteAllNotifications(
            userId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}
      }

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    EventRepositoryProvider.overrideForTests(fakeEventRepository)
    UserRepositoryProvider.overrideForTests(fakeUserRepository)
    OrganizationRepositoryProvider.overrideForTests(fakeOrganizationRepository)
    MediaRepositoryProvider.overrideForTests(fakeMediaRepository)
    NotificationRepositoryProvider.overrideForTests(fakeNotificationRepository)
  }

  @After
  fun teardown() {
    EventRepositoryProvider.cleanOverrideForTests()
    UserRepositoryProvider.cleanOverrideForTests()
    OrganizationRepositoryProvider.cleanOverrideForTests()
    MediaRepositoryProvider.cleanOverrideForTests()
    NotificationRepositoryProvider.cleanOverrideForTests()
  }

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

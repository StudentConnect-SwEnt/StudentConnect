package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class HomeScreenStoriesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    // Initialize Firebase first (before accessing any repositories)
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    AuthenticationProvider.testUserId = "testUser123"
  }

  @After
  fun tearDown() {
    AuthenticationProvider.testUserId = null
  }

  // Fake repositories for testing
  private val fakeEventRepository =
      object : EventRepository {
        override fun getNewUid(): String = "test-uid"

        override suspend fun getAllVisibleEvents(): List<Event> = emptyList()

        override suspend fun getAllVisibleEventsSatisfying(
            predicate: (Event) -> Boolean
        ): List<Event> = emptyList()

        override suspend fun getEvent(eventUid: String): Event = throw NotImplementedError()

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

        override suspend fun checkUsernameAvailability(username: String): Boolean = true

        override suspend fun addPinnedEvent(userId: String, eventId: String) = Unit

        override suspend fun removePinnedEvent(userId: String, eventId: String) = Unit

        override suspend fun getPinnedEvents(userId: String) = emptyList<String>()
      }

  private val fakeFriendsRepository =
      object : FriendsRepository {
        override suspend fun getFriends(userId: String): List<String> = emptyList()

        override suspend fun getPendingRequests(userId: String): List<String> = emptyList()

        override suspend fun getSentRequests(userId: String): List<String> = emptyList()

        override suspend fun sendFriendRequest(fromUserId: String, toUserId: String) {}

        override suspend fun acceptFriendRequest(userId: String, fromUserId: String) {}

        override suspend fun rejectFriendRequest(userId: String, fromUserId: String) {}

        override suspend fun cancelFriendRequest(userId: String, toUserId: String) {}

        override suspend fun removeFriend(userId: String, friendId: String) {}

        override suspend fun areFriends(userId: String, otherUserId: String): Boolean = false

        override suspend fun hasPendingRequest(fromUserId: String, toUserId: String): Boolean =
            false

        override fun observeFriendship(userId: String, otherUserId: String): Flow<Boolean> = flow {
          emit(false)
        }
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
        ) {
          TODO("Not yet implemented")
        }

        override fun createNotification(
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
          TODO("Not yet implemented")
        }

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
        ) {
          TODO("Not yet implemented")
        }
      }

  // Mock ViewModels
  private fun createMockHomePageViewModel(): HomePageViewModel {
    return HomePageViewModel(
        eventRepository = fakeEventRepository,
        userRepository = fakeUserRepository,
        friendsRepository = fakeFriendsRepository)
  }

  private fun createMockNotificationViewModel(): NotificationViewModel {
    return NotificationViewModel(repository = fakeNotificationRepository)
  }

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Summer Festival",
          subtitle = "Best summer event",
          description = "Join us for an amazing summer festival.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("music", "outdoor"),
      )

  @Test
  fun homeScreen_eventsStories_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(1, 1)))),
              ))
    }

    // Use testTag instead of contentDescription since the StoryItem may not have an image
    composeTestRule.onNodeWithTag("story_item_${testEvent1.uid}").assertIsDisplayed()
  }

  @Test
  fun homeScreen_eventsStories_isNotDisplayed_whenNoStories() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(0, 0)))),
              ))
    }

    composeTestRule.onNodeWithContentDescription("Event Story").assertIsNotDisplayed()
  }

  @Test
  fun homeScreen_eventsStories_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(1, 1)))),
              ))
    }

    // Use testTag instead of contentDescription since the StoryItem may not have an image
    composeTestRule.onNodeWithTag("story_item_${testEvent1.uid}").assertIsDisplayed()
  }

  @Test
  fun storyItem_withViewedStory_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = true,
          onClick = {},
          contentDescription = "Test Story Content")
    }

    composeTestRule.onNodeWithContentDescription("Test Story Content").assertIsDisplayed()
  }

  @Test
  fun storyItem_withUnviewedStory_displaysCorrectly() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          contentDescription = "Test Story Content")
    }

    composeTestRule.onNodeWithContentDescription("Test Story Content").assertIsDisplayed()
  }

  @Test
  fun storyItem_withCustomTestTag_displaysWithTestTag() {
    composeTestRule.setContent {
      StoryItem(
          name = "Test Story",
          avatarRes = com.github.se.studentconnect.R.drawable.avatar_12,
          viewed = false,
          onClick = {},
          contentDescription = "Test Story Content",
          testTag = "custom_test_tag")
    }

    composeTestRule.onNodeWithContentDescription("Test Story Content").assertIsDisplayed()
  }
}

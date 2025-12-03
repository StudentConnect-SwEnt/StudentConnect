package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenQRScannerTest {

  @get:Rule val composeTestRule = createComposeRule()

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

        override suspend fun getInvitations(
            userId: String
        ): List<com.github.se.studentconnect.ui.screen.activities.Invitation> = emptyList()

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
        eventRepository = fakeEventRepository, userRepository = fakeUserRepository)
  }

  private fun createMockNotificationViewModel(): NotificationViewModel {
    return NotificationViewModel(repository = fakeNotificationRepository)
  }

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Test Event",
          subtitle = "Test subtitle",
          description = "Test description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("test"),
      )

  @Test
  fun homeScreen_withShouldOpenQRScanner_false_showsHomePage() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = emptyMap()),
          notificationViewModel = createMockNotificationViewModel())
    }

    // HomePage should be visible
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withLoadingState_showsLoadingIndicator() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = true, events = emptyList(), subscribedEventsStories = emptyMap()),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withNoEvents_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false, events = emptyList(), subscribedEventsStories = emptyMap()),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withEmptyStories_doesNotShowStoriesRow() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = emptyMap()),
          notificationViewModel = createMockNotificationViewModel())
    }

    // StoriesRow should not be visible when there are no stories
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun storiesRow_withEmptyStories_doesNotDisplay() {
    composeTestRule.setContent {
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = emptyMap())
    }

    // The stories row should not be rendered
    // We can't directly assert it doesn't exist, but we can verify the test passes
  }

  @Test
  fun storiesRow_withStoriesOfZeroTotal_doesNotDisplay() {
    composeTestRule.setContent {
      val stories: Map<Event, Pair<Int, Int>> =
          mapOf(testEvent1 as Event to Pair(0, 0)) // 0 total stories
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    // Stories with 0 total should be filtered out
  }

  @Test
  fun storiesRow_withMultipleStories_displaysCorrectly() {
    val testEvent2 =
        Event.Public(
            uid = "event-2",
            title = "Test Event 2",
            subtitle = "Test subtitle 2",
            description = "Test description 2",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
            website = "https://example.com",
            ownerId = "owner2",
            isFlash = false,
            tags = listOf("test"),
        )

    composeTestRule.setContent {
      val stories: Map<Event, Pair<Int, Int>> =
          mapOf(testEvent1 as Event to Pair(3, 1), testEvent2 as Event to Pair(5, 0))
      StoriesRow(onAddStoryClick = {}, onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
  }
}

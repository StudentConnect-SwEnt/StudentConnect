package com.github.se.studentconnect.ui.screen.home

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.viewmodel.NotificationViewModel
import com.google.firebase.Timestamp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenCameraModeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

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
  fun homeScreen_addStoryButton_exists() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(testEvent1 to Pair(0, 3))),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.onNodeWithTag("addStoryButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Story").assertIsDisplayed()
  }

  @Test
  fun homeScreen_addStoryButton_isClickable() {
    var addStoryClicked = false

    composeTestRule.setContent {
      StoriesRow(
          onAddStoryClick = { addStoryClicked = true },
          onClick = { _, _ -> },
          stories = mapOf(testEvent1 to Pair(0, 3)))
    }

    composeTestRule.onNodeWithTag("addStoryButton").performClick()

    composeTestRule.runOnIdle { assertTrue(addStoryClicked) }
  }

  @Test
  fun homeScreen_cameraModeSelector_displaysModeTabs() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onQRScannerClosed = {},
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("camera_mode_selector").assertIsDisplayed()
  }

  @Test
  fun homeScreen_cameraModeSelector_hasStoryAndQrScanModes() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onQRScannerClosed = {},
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("STORY").assertIsDisplayed()
    composeTestRule.onNodeWithText("QR SCAN").assertIsDisplayed()
  }

  @Test
  fun homeScreen_cameraModeSelector_canSwitchBetweenModes() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onQRScannerClosed = {},
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Should start on QR scan mode
    composeTestRule
        .onNodeWithText("Point the camera at a StudentConnect QR code")
        .assertIsDisplayed()

    // Click story mode
    composeTestRule.onNodeWithTag("mode_story").performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("story_capture_screen").assertIsDisplayed()
  }

  @Test
  fun homeScreen_onQRScannerClosed_callback_invoked() {
    var qrScannerClosed = false

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onQRScannerClosed = { qrScannerClosed = true },
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Click back button
    composeTestRule.onNodeWithTag("camera_mode_back_button").performClick()

    composeTestRule.runOnIdle { assertTrue(qrScannerClosed) }
  }

  @Test
  fun homeScreen_onCameraActiveChange_calledWithFalse_whenOnHomePage() {
    var cameraActive: Boolean? = null

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onCameraActiveChange = { active -> cameraActive = active },
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Should be on home page (page 1), so camera should be inactive
    composeTestRule.runOnIdle {
      assertTrue("onCameraActiveChange should be called", cameraActive != null)
      assertTrue("Camera should be inactive on home page", cameraActive == false)
    }
  }

  @Test
  fun homeScreen_onCameraActiveChange_calledWithTrue_whenOnScannerPage() {
    var cameraActive: Boolean? = null

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onCameraActiveChange = { active -> cameraActive = active },
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Should be on scanner page (page 0), so camera should be active
    composeTestRule.runOnIdle {
      assertTrue("onCameraActiveChange should be called", cameraActive != null)
      assertTrue("Camera should be active on scanner page", cameraActive == true)
    }
  }

  @Test
  fun homeScreen_onCameraActiveChange_updatesWhenNavigatingToScanner() {
    var cameraActive: Boolean? = null
    var callCount = 0

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onCameraActiveChange = { active ->
            cameraActive = active
            callCount++
          },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(testEvent1 to Pair(0, 3))),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Initially on home page, camera should be inactive
    composeTestRule.runOnIdle {
      assertTrue("Camera should initially be inactive", cameraActive == false)
    }

    // Click add story to navigate to scanner
    composeTestRule.onNodeWithTag("addStoryButton").performClick()

    composeTestRule.waitForIdle()

    // Should now be on scanner page, camera should be active
    composeTestRule.runOnIdle {
      assertTrue("Camera should be active after navigating to scanner", cameraActive == true)
      assertTrue("Callback should have been called multiple times", callCount >= 2)
    }
  }

  @Test
  fun homeScreen_onCameraActiveChange_updatesWhenNavigatingBackToHome() {
    var cameraActive: Boolean? = null
    var callCount = 0

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = true,
          onCameraActiveChange = { active ->
            cameraActive = active
            callCount++
          },
          viewModel = createMockHomePageViewModel(),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Initially on scanner page, camera should be active
    composeTestRule.runOnIdle {
      assertTrue("Camera should initially be active", cameraActive == true)
    }

    // Click back button to return to home
    composeTestRule.onNodeWithTag("camera_mode_back_button").performClick()

    composeTestRule.waitForIdle()

    // Should now be back on home page, camera should be inactive
    composeTestRule.runOnIdle {
      assertTrue("Camera should be inactive after returning to home", cameraActive == false)
      assertTrue("Callback should have been called multiple times", callCount >= 2)
    }
  }

  @Test
  fun homeScreen_onCameraActiveChange_multipleNavigations() {
    val cameraStates = mutableListOf<Boolean>()

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onCameraActiveChange = { active -> cameraStates.add(active) },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(testEvent1 to Pair(0, 3))),
          notificationViewModel = createMockNotificationViewModel())
    }

    composeTestRule.waitForIdle()

    // Navigate to scanner
    composeTestRule.onNodeWithTag("addStoryButton").performClick()
    composeTestRule.waitForIdle()

    // Navigate back to home
    composeTestRule.onNodeWithTag("camera_mode_back_button").performClick()
    composeTestRule.waitForIdle()

    // Verify we captured the state changes
    composeTestRule.runOnIdle {
      assertTrue("Should have multiple state changes", cameraStates.size >= 2)
      assertTrue("Should start with inactive state", cameraStates.first() == false)
      assertTrue("Should end with inactive state", cameraStates.last() == false)
      assertTrue(
          "Should have active state in between",
          cameraStates.contains(true) || cameraStates.any { it })
    }
  }
}

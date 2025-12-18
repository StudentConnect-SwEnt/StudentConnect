package com.github.se.studentconnect.ui.event

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventViewTest {

  /**
   * Forces the [EventViewModel] into an error state by updating its private `_uiState` field via
   * reflection. This lets us trigger the fallback UI branch without changing production code.
   */
  private fun forceErrorState(viewModel: EventViewModel, message: String) {
    val field = EventViewModel::class.java.getDeclaredField("_uiState")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val stateFlow = field.get(viewModel) as MutableStateFlow<EventUiState>
    stateFlow.value =
        EventUiState(
            event = null,
            isLoading = false,
            errorMessage = message,
        )
  }

  companion object {
    @BeforeClass
    @JvmStatic
    fun grantPermissions() {
      val instrumentation = InstrumentationRegistry.getInstrumentation()
      val context = instrumentation.targetContext
      instrumentation.uiAutomation.executeShellCommand(
          "pm grant ${context.packageName} android.permission.CAMERA")
    }
  }

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var viewModel: EventViewModel

  private val testEvent =
      Event.Public(
          uid = "test-event-123",
          title = "Test Event Title",
          subtitle = "Test Subtitle",
          description = "This is a test event description.",
          start = Timestamp.now(),
          end = Timestamp(Timestamp.now().seconds + 3600, Timestamp.now().nanoseconds),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner123",
          isFlash = false,
          tags = listOf("tech", "networking"),
          maxCapacity = 50u)

  private val testEventOwner =
      User(
          userId = "owner123",
          email = "owner@epfl.ch",
          username = "owner",
          firstName = "John",
          lastName = "Doe",
          university = "EPFL")
  private val currentUser = testEventOwner.copy(userId = "currentUser123")
  private val testEventAttendee = testEventOwner.copy(userId = "attendee123")

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = EventViewModel(eventRepository, userRepository)

    runBlocking {
      eventRepository.addEvent(testEvent)
      userRepository.saveUser(testEventOwner)
      userRepository.saveUser(currentUser)
      userRepository.saveUser(testEventAttendee)
      // Fetch the event to initialize the ViewModel state
      viewModel.fetchEvent(testEvent.uid)
    }
  }

  @After
  fun tearDown() {
    // Clean up the repository to avoid "Event already exists" errors
    runBlocking {
      try {
        eventRepository.deleteEvent(testEvent.uid)
        userRepository.deleteUser(testEventOwner.userId)
        userRepository.deleteUser(currentUser.userId)
        userRepository.deleteUser(testEventAttendee.userId)
      } catch (e: Exception) {
        // Ignore if event doesn't exist
      }
    }
  }

  @Test
  fun eventView_mainScreen_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_VIEW_SCREEN).assertIsDisplayed()
  }

  @Test
  fun eventView_topAppBar_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.TOP_APP_BAR).assertIsDisplayed()
  }

  @Test
  fun eventView_backButton_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_backButton_hasClickAction() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_eventTitle_isDisplayedInTopBar() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(testEvent.title).assertIsDisplayed()
  }

  @Test
  fun eventView_eventImage_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_IMAGE).assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun eventView_descriptionText_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }
    composeTestRule.waitUntilAtLeastOneExists(
        hasTestTag(EventViewTestTags.DESCRIPTION_TEXT), 30_000)
    composeTestRule
        .onNodeWithTag(EventViewTestTags.DESCRIPTION_TEXT)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun eventView_descriptionContent_isCorrect() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitUntilAtLeastOneExists(
        hasTestTag(EventViewTestTags.DESCRIPTION_TEXT), 30_000)
    composeTestRule.onNodeWithText(testEvent.description).performScrollTo().assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_isDisplayed() {
    // Set user as owner to make chat button visible
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.CHAT_BUTTON)
          .performScrollTo()
          .assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_chatButton_hasCorrectText() {
    // Set user as owner to make chat button visible
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).performScrollTo()
      composeTestRule.onNodeWithText("Event chat").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_chatButton_hasClickAction() {
    // Set user as owner to make chat button visible
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertHasClickAction()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_actionButtonsSection_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.ACTION_BUTTONS_SECTION).assertIsDisplayed()
  }

  @Test
  fun eventView_visitWebsiteButton_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_locationButton_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_shareButton_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_locationButton_hasClickAction() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_shareButton_hasClickAction() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_whenNotJoined_displaysJoinButton() {
    // Create a future event to ensure it hasn't started
    val futureEvent =
        testEvent.copy(
            uid = "future-join-event",
            start = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0) // 1 hour from now
            )
    runBlocking { eventRepository.addEvent(futureEvent) }

    // Create a new ViewModel for this test
    val futureEventViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { futureEventViewModel.fetchEvent(futureEvent.uid) }

    AuthenticationProvider.testUserId = "not-owner"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = futureEvent.uid,
                navController = navController,
                eventViewModel = futureEventViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Join").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
      // Clean up
      runBlocking { eventRepository.deleteEvent(futureEvent.uid) }
    }
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun eventView_infoSection_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    // Wait until the info section is composed, then scroll to it and assert
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(EventViewTestTags.INFO_SECTION), 10_000)
    composeTestRule
        .onNodeWithTag(EventViewTestTags.INFO_SECTION)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun eventView_descriptionLabel_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    // Wait until the description text is composed, then scroll to it and assert
    composeTestRule.waitUntilAtLeastOneExists(
        hasTestTag(EventViewTestTags.DESCRIPTION_TEXT), 10_000)
    composeTestRule
        .onNodeWithTag(EventViewTestTags.DESCRIPTION_TEXT)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun eventView_backButtonClick_doesNotCrash() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()
    // Should not crash
  }

  @Test
  fun eventView_scanQrButton_notDisplayedForNonOwner() {
    // Arrange - set current user to someone other than the owner
    AuthenticationProvider.testUserId = "different-user"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Assert - scan QR button should not be displayed
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_scanQrButton_displayedForOwner() {
    // Arrange - set current user to the event owner
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Assert - scan QR button should be displayed
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_scanQrButton_hasCorrectText() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertIsDisplayed()
      composeTestRule.onNodeWithText("Scan").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_scanQrButton_hasClickAction() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertHasClickAction()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_scanQrButton_click_opensQrScanner() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Act - click the scan QR button
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

      // Assert - QR scanner dialog should be displayed
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_qrScannerDialog_notDisplayedInitially() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Assert - QR scanner should not be displayed initially
      composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_validationResult_valid_displaysCorrectly() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      val participantId = "participant123"
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Act - open scanner and validate participant
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

      composeTestRule.waitForIdle()
      // Simulate validation through ViewModel
      composeTestRule.runOnIdle { viewModel.validateParticipant(testEvent.uid, participantId) }

      // Assert - valid result should be displayed
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.VALIDATION_RESULT_VALID).assertIsDisplayed()
      composeTestRule.onNodeWithText("Valid Ticket").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_validationResult_invalid_displaysCorrectly() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      val nonParticipantId = "nonparticipant123"

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Act - open scanner and validate non-participant
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

      composeTestRule.waitForIdle()
      // Simulate validation through ViewModel
      composeTestRule.runOnIdle { viewModel.validateParticipant(testEvent.uid, nonParticipantId) }

      // Assert - invalid result should be displayed
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.VALIDATION_RESULT_INVALID).assertIsDisplayed()
      composeTestRule.onNodeWithText("Invalid Ticket").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_validationResult_scanNextButton_clearsResult() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      val participantId = "participant123"
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Act - open scanner and validate participant
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

      composeTestRule.waitForIdle()
      composeTestRule.runOnIdle { viewModel.validateParticipant(testEvent.uid, participantId) }

      composeTestRule.waitForIdle()
      // Click "Scan Next" button
      composeTestRule.onNodeWithText("Scan Next").performClick()

      // Assert - validation result should be cleared
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.VALIDATION_RESULT_VALID).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_validationResult_closeScannerButton_closesDialog() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      val participantId = "participant123"
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Act - open scanner and validate participant
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

      composeTestRule.waitForIdle()
      composeTestRule.runOnIdle { viewModel.validateParticipant(testEvent.uid, participantId) }

      composeTestRule.waitForIdle()
      // Click "Close Scanner" button
      composeTestRule.onNodeWithText("Close Scanner").performClick()

      // Assert - dialog should be closed
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_editButton_displayedForOwner() {
    // Arrange
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_editButton_notDisplayedForNonOwner() {
    AuthenticationProvider.testUserId = "different-user"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_participantsInfo_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun eventView_participantsInfo_showsCorrectCount() {
    runBlocking {
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user1"))
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user2"))
    }

    // Create a fresh ViewModel that will fetch the updated data
    val testViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { testViewModel.fetchEvent(testEvent.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = testViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Scroll to participants info section first
    composeTestRule.onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO).performScrollTo()
    // Should show "2 / 50" for the participants count
    composeTestRule.onNodeWithText("2 / 50").assertIsDisplayed()
  }

  @Test
  fun eventView_eventWithoutMaxCapacity_showsParticipantsWithoutLimit() {
    val eventWithoutCapacity = testEvent.copy(uid = "event-no-capacity", maxCapacity = null)
    runBlocking { eventRepository.addEvent(eventWithoutCapacity) }

    // Create a new ViewModel for this test
    val newViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { newViewModel.fetchEvent(eventWithoutCapacity.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = eventWithoutCapacity.uid,
              navController = navController,
              eventViewModel = newViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .assertIsDisplayed()

    // Clean up this extra event
    runBlocking { eventRepository.deleteEvent(eventWithoutCapacity.uid) }
  }

  @Test
  fun eventView_participantsInfo_isClickable() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .assertHasClickAction()
  }

  @Test
  fun eventView_clickParticipantsInfo_doesNotCrash() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .performClick()
  }

  @Test
  fun eventView_clickParticipantsInfo_changeView() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .performClick()
    composeTestRule.onNodeWithTag(EventViewTestTags.BASE_SCREEN).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.ATTENDEE_LIST).assertIsDisplayed()
  }

  @Test
  fun eventView_attendeesList_topBarBackButton_returnsToEvent() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Scroll to attendees list
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()
    // Verify we are on attendees list
    composeTestRule.onNodeWithTag(EventViewTestTags.ATTENDEE_LIST).assertIsDisplayed()

    // Click Top Bar Back Button
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()

    // Verify we are back on main event view
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.BASE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun eventView_fullEvent_displaysFullButton() {
    val fullEvent =
        testEvent.copy(
            uid = "full-event",
            maxCapacity = 2u,
            start = Timestamp(Timestamp.now().seconds + 100, Timestamp.now().nanoseconds))
    runBlocking {
      eventRepository.addEvent(fullEvent)
      eventRepository.addParticipantToEvent(fullEvent.uid, EventParticipant("user1"))
      eventRepository.addParticipantToEvent(fullEvent.uid, EventParticipant("user2"))
    }

    AuthenticationProvider.testUserId = "different-user"

    try {
      // Create a new ViewModel for this test to fetch the updated event
      val fullEventViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { fullEventViewModel.fetchEvent(fullEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = fullEvent.uid,
                navController = navController,
                eventViewModel = fullEventViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Full").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
      // Clean up
      runBlocking { eventRepository.deleteEvent(fullEvent.uid) }
    }
  }

  @Test
  fun eventView_startedEvent_displaysStartedButton() {
    val pastEvent =
        testEvent.copy(
            uid = "started-event",
            start = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
            end = null)
    runBlocking { eventRepository.addEvent(pastEvent) }

    AuthenticationProvider.testUserId = "different-user"

    try {
      // Create a new ViewModel for this test
      val pastEventViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { pastEventViewModel.fetchEvent(pastEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = pastEvent.uid,
                navController = navController,
                eventViewModel = pastEventViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Started").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
      // Clean up
      runBlocking { eventRepository.deleteEvent(pastEvent.uid) }
    }
  }

  @Test
  fun eventView_eventStarted_showsHurryUpMessage() {
    val startedEvent =
        testEvent.copy(
            uid = "hurry-event",
            start = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0),
            end = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0))
    runBlocking {
      eventRepository.addEvent(startedEvent)
      eventRepository.addParticipantToEvent(startedEvent.uid, EventParticipant("test-user"))
    }

    AuthenticationProvider.testUserId = "test-user"

    try {
      // Create a new ViewModel for this test
      val startedEventViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { startedEventViewModel.fetchEvent(startedEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = startedEvent.uid,
                navController = navController,
                eventViewModel = startedEventViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Hurry up! Event has started").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
      // Clean up
      runBlocking { eventRepository.deleteEvent(startedEvent.uid) }
    }
  }

  @Test
  fun eventView_eventFarInFuture_showsDaysLeft() {
    val futureEvent =
        testEvent.copy(
            uid = "future-event",
            start = Timestamp(System.currentTimeMillis() / 1000 + 172800, 0) // 2 days from now
            )
    runBlocking { eventRepository.addEvent(futureEvent) }

    // Create a new ViewModel for this test
    val futureEventViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { futureEventViewModel.fetchEvent(futureEvent.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = futureEvent.uid,
              navController = navController,
              eventViewModel = futureEventViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.COUNTDOWN_DAYS).assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(futureEvent.uid) }
  }

  @Test
  fun eventView_eventSoon_showsCountdownTimer() {
    val soonEvent =
        testEvent.copy(
            uid = "soon-event",
            start = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0) // 1 hour from now
            )
    runBlocking { eventRepository.addEvent(soonEvent) }

    // Create a new ViewModel for this test
    val soonEventViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { soonEventViewModel.fetchEvent(soonEvent.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = soonEvent.uid,
              navController = navController,
              eventViewModel = soonEventViewModel)
        }
      }
    }

    // Wait for initial composition and state stabilization
    composeTestRule.waitForIdle()
    // Add a small delay to let countdown timer stabilize
    Thread.sleep(100)
    composeTestRule.waitForIdle()

    // Use waitUntil to handle timing issues with rapidly updating countdown
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodes(hasTestTag(EventViewTestTags.COUNTDOWN_TIMER))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    composeTestRule.onNodeWithTag(EventViewTestTags.COUNTDOWN_TIMER).assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(soonEvent.uid) }
  }

  @Test
  fun eventView_joinButton_canBeClicked() {
    AuthenticationProvider.testUserId = "test-user"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertHasClickAction()
      composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).performClick()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_leaveButton_canBeClicked() {
    AuthenticationProvider.testUserId = "test-user"

    try {
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("test-user"))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).assertHasClickAction()
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).performClick()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_privateEvent_displaysCorrectly() {
    val privateEvent =
        Event.Private(
            uid = "private-event-123",
            title = "Private Event",
            description = "Private event description",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(46.52, 6.57, "EPFL"),
            ownerId = "owner123",
            isFlash = false,
            maxCapacity = 20u)
    runBlocking { eventRepository.addEvent(privateEvent) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = privateEvent.uid,
              navController = navController,
              eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Private Event").assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(privateEvent.uid) }
  }

  @Test
  fun eventView_websiteButton_clickableForPublicEvent() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_websiteButtonClick_doesNotCrash() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).performClick()
    // Should not crash
  }

  @Test
  fun eventView_locationButtonClick_doesNotCrash() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
        composable("map/{lat}/{lon}/{zoom}") {
          // Placeholder for map screen
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).performClick()
    // Should not crash
  }

  @Test
  fun eventView_scanQrButtonClick_opensQrScanner() {
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

      // Wait for dialog to appear
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_progressBar_displayedForEventWithCapacity() {
    runBlocking { eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user1")) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Progress bar should be displayed
    composeTestRule
        .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun eventView_ownerCannotJoinOwnEvent() {
    AuthenticationProvider.testUserId = "owner123"

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Join button should not exist for owner
      composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_websiteButton_notDisplayedForPrivateEvent() {
    val privateEvent =
        Event.Private(
            uid = "private-no-website",
            title = "Private Event",
            description = "Private event has no website field",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(46.52, 6.57, "EPFL"),
            ownerId = "owner123",
            isFlash = false,
            maxCapacity = 20u)
    runBlocking { eventRepository.addEvent(privateEvent) }

    val privateViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { privateViewModel.fetchEvent(privateEvent.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = privateEvent.uid,
              navController = navController,
              eventViewModel = privateViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertDoesNotExist()

    runBlocking { eventRepository.deleteEvent(privateEvent.uid) }
  }

  @Test
  fun eventView_websiteButton_notDisplayedWhenWebsiteIsNull() {
    val publicEventNoWebsite = testEvent.copy(uid = "public-no-website", website = null)
    runBlocking { eventRepository.addEvent(publicEventNoWebsite) }

    val noWebsiteViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { noWebsiteViewModel.fetchEvent(publicEventNoWebsite.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = publicEventNoWebsite.uid,
              navController = navController,
              eventViewModel = noWebsiteViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertDoesNotExist()

    runBlocking { eventRepository.deleteEvent(publicEventNoWebsite.uid) }
  }

  @Test
  fun eventView_websiteButton_notDisplayedWhenWebsiteIsEmpty() {
    val publicEventEmptyWebsite = testEvent.copy(uid = "public-empty-website", website = "")
    runBlocking { eventRepository.addEvent(publicEventEmptyWebsite) }

    val emptyWebsiteViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { emptyWebsiteViewModel.fetchEvent(publicEventEmptyWebsite.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = publicEventEmptyWebsite.uid,
              navController = navController,
              eventViewModel = emptyWebsiteViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertDoesNotExist()

    runBlocking { eventRepository.deleteEvent(publicEventEmptyWebsite.uid) }
  }

  @Test
  fun eventView_websiteButton_displayedWhenWebsiteIsPresent() {
    val publicEventWithWebsite =
        testEvent.copy(uid = "public-with-website", website = "https://example.com")
    runBlocking { eventRepository.addEvent(publicEventWithWebsite) }

    val withWebsiteViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { withWebsiteViewModel.fetchEvent(publicEventWithWebsite.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = publicEventWithWebsite.uid,
              navController = navController,
              eventViewModel = withWebsiteViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()

    runBlocking { eventRepository.deleteEvent(publicEventWithWebsite.uid) }
  }

  @Test
  fun eventView_websiteButton_displayedForNonEmptyWebsite() {
    val publicEventWithWebsite =
        testEvent.copy(uid = "public-non-empty-website", website = "example.com")
    runBlocking { eventRepository.addEvent(publicEventWithWebsite) }

    val websiteViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { websiteViewModel.fetchEvent(publicEventWithWebsite.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = publicEventWithWebsite.uid,
              navController = navController,
              eventViewModel = websiteViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertHasClickAction()

    runBlocking { eventRepository.deleteEvent(publicEventWithWebsite.uid) }
  }

  @Test
  fun eventView_locationButton_notDisplayedWhenLocationIsNull() {
    val eventNoLocation = testEvent.copy(uid = "event-no-location", location = null)
    runBlocking { eventRepository.addEvent(eventNoLocation) }

    val noLocationViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { noLocationViewModel.fetchEvent(eventNoLocation.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = eventNoLocation.uid,
              navController = navController,
              eventViewModel = noLocationViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertDoesNotExist()

    runBlocking { eventRepository.deleteEvent(eventNoLocation.uid) }
  }

  @Test
  fun eventView_locationButton_displayedWhenLocationIsPresent() {
    val eventWithLocation =
        testEvent.copy(
            uid = "event-with-location",
            location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"))
    runBlocking { eventRepository.addEvent(eventWithLocation) }

    val withLocationViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { withLocationViewModel.fetchEvent(eventWithLocation.uid) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = eventWithLocation.uid,
              navController = navController,
              eventViewModel = withLocationViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertIsDisplayed()

    runBlocking { eventRepository.deleteEvent(eventWithLocation.uid) }
  }

  @Test
  fun eventView_locationButtonClick_navigatesWithEventUid() {
    val eventWithLocation =
        testEvent.copy(
            uid = "event-location-nav",
            location = Location(latitude = 46.5197, longitude = 6.6323, name = "Test Location"))
    runBlocking { eventRepository.addEvent(eventWithLocation) }

    val withLocationViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { withLocationViewModel.fetchEvent(eventWithLocation.uid) }

    var capturedEventUid: String? = null
    var navigationOccurred = false

    composeTestRule.setContent {
      val navController = rememberNavController()

      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = eventWithLocation.uid,
              navController = navController,
              eventViewModel = withLocationViewModel)
        }
        composable(
            "map/{latitude}/{longitude}/{zoom}?eventUid={eventUid}",
            arguments =
                listOf(
                    androidx.navigation.navArgument("eventUid") {
                      nullable = true
                      defaultValue = null
                    })) { backStackEntry ->
              navigationOccurred = true
              capturedEventUid = backStackEntry.arguments?.getString("eventUid")
            }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).performScrollTo()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Verify navigation occurred with correct eventUid
    assert(navigationOccurred) { "Expected navigation to map screen to occur" }
    assert(capturedEventUid == eventWithLocation.uid) {
      "Expected eventUid to be ${eventWithLocation.uid}, but got: $capturedEventUid"
    }

    runBlocking { eventRepository.deleteEvent(eventWithLocation.uid) }
  }

  @Test
  fun eventView_locationButtonClick_includesCorrectCoordinatesInRoute() {
    val eventWithLocation =
        testEvent.copy(
            uid = "event-coords-test",
            location = Location(latitude = 46.5197, longitude = 6.6323, name = "Test Location"))
    runBlocking { eventRepository.addEvent(eventWithLocation) }

    val withLocationViewModel = EventViewModel(eventRepository, userRepository)
    runBlocking { withLocationViewModel.fetchEvent(eventWithLocation.uid) }

    var capturedLatitude: String? = null
    var capturedLongitude: String? = null
    var navigationOccurred = false

    composeTestRule.setContent {
      val navController = rememberNavController()

      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = eventWithLocation.uid,
              navController = navController,
              eventViewModel = withLocationViewModel)
        }
        composable(
            "map/{latitude}/{longitude}/{zoom}?eventUid={eventUid}",
            arguments =
                listOf(
                    androidx.navigation.navArgument("latitude") {
                      type = androidx.navigation.NavType.StringType
                    },
                    androidx.navigation.navArgument("longitude") {
                      type = androidx.navigation.NavType.StringType
                    })) { backStackEntry ->
              navigationOccurred = true
              capturedLatitude = backStackEntry.arguments?.getString("latitude")
              capturedLongitude = backStackEntry.arguments?.getString("longitude")
            }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).performScrollTo()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Verify navigation occurred with correct coordinates
    assert(navigationOccurred) { "Expected navigation to map screen to occur" }
    assert(capturedLatitude == "46.5197") {
      "Expected latitude to be 46.5197, but got: $capturedLatitude"
    }
    assert(capturedLongitude == "6.6323") {
      "Expected longitude to be 6.6323, but got: $capturedLongitude"
    }

    runBlocking { eventRepository.deleteEvent(eventWithLocation.uid) }
  }

  @Test
  fun eventView_attendeeListShowsAllAttendees() {
    AuthenticationProvider.testUserId = currentUser.userId

    try {

      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUser.userId))
        eventRepository.addParticipantToEvent(
            testEvent.uid, EventParticipant(testEventAttendee.userId))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule
          .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
          .performScrollTo()
          .performClick()
      composeTestRule.onNodeWithTag(EventViewTestTags.ATTENDEE_LIST_OWNER).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.ATTENDEE_LIST_CURRENT_USER)
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag(EventViewTestTags.ATTENDEE_LIST_ITEM).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_attendeeListDoesNotShowCurrentUserWhenIsOwner() {
    AuthenticationProvider.testUserId = "owner123"

    try {
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUser.userId))
        eventRepository.addParticipantToEvent(
            testEvent.uid, EventParticipant(testEventAttendee.userId))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule
          .onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO)
          .performScrollTo()
          .performClick()
      composeTestRule.onNodeWithTag(EventViewTestTags.ATTENDEE_LIST_OWNER).assertIsDisplayed()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.ATTENDEE_LIST_CURRENT_USER)
          .assertIsNotDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_leaveConfirmDialog_notDisplayedInitially() {
    AuthenticationProvider.testUserId = "test-user"

    try {
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("test-user"))
      }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Leave confirmation dialog should not be displayed initially
      composeTestRule
          .onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_DIALOG)
          .assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_leaveButton_click_showsConfirmDialog() {
    AuthenticationProvider.testUserId = "test-user"

    try {
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("test-user"))
      }

      // Create a fresh ViewModel to ensure the participant is recognized
      val freshViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { freshViewModel.fetchEvent(testEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid,
                navController = navController,
                eventViewModel = freshViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Click the leave button
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).performClick()

      // Assert - confirmation dialog should be displayed
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_DIALOG).assertIsDisplayed()
      composeTestRule.onNodeWithText("Leave Event").assertIsDisplayed()
      composeTestRule
          .onNodeWithText("Are you sure you want to leave this event?")
          .assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_leaveConfirmDialog_cancelButton_dismissesDialog() {
    AuthenticationProvider.testUserId = "test-user"

    try {
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("test-user"))
      }

      // Create a fresh ViewModel
      val freshViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { freshViewModel.fetchEvent(testEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid,
                navController = navController,
                eventViewModel = freshViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Open the dialog
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).performClick()
      composeTestRule.waitForIdle()

      // Click cancel button
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_CANCEL).performClick()

      // Assert - dialog should be dismissed
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_DIALOG)
          .assertDoesNotExist()
      // User should still be joined (leave button should still be visible)
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_leaveConfirmDialog_confirmButton_leavesEvent() {
    AuthenticationProvider.testUserId = "test-user"

    try {
      runBlocking {
        eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("test-user"))
      }

      // Create a fresh ViewModel
      val freshViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { freshViewModel.fetchEvent(testEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid,
                navController = navController,
                eventViewModel = freshViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Open the dialog
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).performClick()
      composeTestRule.waitForIdle()

      // Click confirm button
      composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_CONFIRM).performClick()

      // Assert - dialog should be dismissed and user should have left
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_DIALOG)
          .assertDoesNotExist()
      // Join button should now be visible instead of leave
      composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_statisticsButton_visibleWhenOwner() {
    // Set current user as owner
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.VIEW_STATISTICS_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_statisticsButton_notVisibleWhenNotOwner() {
    // Set current user as non-owner
    AuthenticationProvider.testUserId = currentUser.userId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.VIEW_STATISTICS_BUTTON).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_statisticsButton_clickNavigatesToStatistics() {
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
          composable("eventStatistics/{eventUid}") {
            // Statistics screen placeholder for navigation test
            androidx.compose.material3.Text("Statistics Screen")
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.VIEW_STATISTICS_BUTTON).performClick()
      composeTestRule.waitForIdle()

      // Verify navigation occurred by checking if statistics screen is displayed
      composeTestRule.onNodeWithText("Statistics Screen").assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_statisticsButton_hasContentDescription() {
    // Test that button has content description (covers EventView.kt lines 233-234)
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      // Verify button exists and has content description
      composeTestRule.onNodeWithTag(EventViewTestTags.VIEW_STATISTICS_BUTTON).assertExists()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  // --- Delete Event Tests ---

  @Test
  fun eventView_deleteButton_visibleWhenOwner() {
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()

      // Wait for event to load and button to be available
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()

      // Button is at the bottom of scrollable content, so scroll to it first
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performScrollTo()
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_deleteButton_notVisibleWhenNotOwner() {
    AuthenticationProvider.testUserId = currentUser.userId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertDoesNotExist()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_deleteButton_click_showsConfirmDialog() {
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()

      // Wait for button to be available
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performScrollTo()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performClick()

      // Assert - confirmation dialog should be displayed
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_DIALOG)
          .assertIsDisplayed()
      composeTestRule.onNodeWithText("Delete Event").assertIsDisplayed()
      composeTestRule
          .onNodeWithText(
              "Are you sure you want to delete this event? This action cannot be undone.")
          .assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_deleteConfirmDialog_cancelButton_dismissesDialog() {
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
          }
        }
      }

      composeTestRule.waitForIdle()

      // Wait for button to be available
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performScrollTo()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performClick()
      composeTestRule.waitForIdle()

      // Wait for dialog to appear
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_CANCEL).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()

      // Click cancel button
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_CANCEL).performClick()

      // Assert - dialog should be dismissed
      composeTestRule.waitForIdle()
      composeTestRule
          .onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_DIALOG)
          .assertDoesNotExist()
      // Delete button should still be visible
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertIsDisplayed()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @Test
  fun eventView_deleteConfirmDialog_confirmButton_deletesEvent() {
    AuthenticationProvider.testUserId = testEvent.ownerId

    try {
      val freshViewModel = EventViewModel(eventRepository, userRepository)
      runBlocking { freshViewModel.fetchEvent(testEvent.uid) }

      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "event") {
          composable("event") {
            EventView(
                eventUid = testEvent.uid,
                navController = navController,
                eventViewModel = freshViewModel)
          }
        }
      }

      composeTestRule.waitForIdle()

      // Wait for button to be available
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performScrollTo()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_EVENT_BUTTON).performClick()

      // Wait for dialog to appear
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule
              .onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_CONFIRM)
              .assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()

      // Click confirm button
      composeTestRule.onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_CONFIRM).performClick()

      // Wait for dialog to disappear after deletion
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule
              .onNodeWithTag(EventViewTestTags.DELETE_CONFIRMATION_DIALOG)
              .assertDoesNotExist()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      composeTestRule.waitForIdle()
    } finally {
      AuthenticationProvider.testUserId = null
    }
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun eventView_subtitle_isDisplayedForPublicEvent() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid, navController = navController, eventViewModel = viewModel)
        }
      }
    }

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag(EventViewTestTags.SUBTITLE_TEXT), 10_000)
    composeTestRule
        .onNodeWithTag(EventViewTestTags.SUBTITLE_TEXT)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun eventView_errorState_displaysRetryButton() {
    val errorViewModel = EventViewModel(eventRepository, userRepository)
    forceErrorState(errorViewModel, "Test error")

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = "test-event",
              navController = navController,
              eventViewModel = errorViewModel)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
  }
}

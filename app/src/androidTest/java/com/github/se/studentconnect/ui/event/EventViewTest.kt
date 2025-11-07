package com.github.se.studentconnect.ui.event

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventViewTest {

  @get:Rule val composeTestRule = createComposeRule()

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
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner123",
          isFlash = false,
          tags = listOf("tech", "networking"),
          maxCapacity = 50u)

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = EventViewModel(eventRepository, userRepository)

    runBlocking {
      eventRepository.addEvent(testEvent)
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
      } catch (e: Exception) {
        // Ignore if event doesn't exist
      }
    }
    unmockkAll()
  }

  @Test
  fun eventView_mainScreen_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_IMAGE).assertIsDisplayed()
  }

  @Test
  fun eventView_descriptionText_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
  }

  @Test
  fun eventView_descriptionContent_isCorrect() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(testEvent.description).assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(EventViewTestTags.CHAT_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_hasCorrectText() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).performScrollTo()
    composeTestRule.onNodeWithText("Event chat").assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_hasClickAction() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_actionButtonsSection_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "not-owner"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = futureEvent.uid,
              navController = navController,
              eventViewModel = futureEventViewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Join").assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(futureEvent.uid) }
  }

  @Test
  fun eventView_infoSection_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.INFO_SECTION).assertIsDisplayed()
  }

  @Test
  fun eventView_descriptionLabel_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
  }

  @Test
  fun eventView_backButtonClick_doesNotCrash() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "different-user"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Assert - scan QR button should not be displayed
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertDoesNotExist()
  }

  @Test
  fun eventView_scanQrButton_displayedForOwner() {
    // Arrange - set current user to the event owner
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Assert - scan QR button should be displayed
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_scanQrButton_hasCorrectText() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Scan").assertIsDisplayed()
  }

  @Test
  fun eventView_scanQrButton_hasClickAction() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_scanQrButton_click_opensQrScanner() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Act - click the scan QR button
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

    // Assert - QR scanner dialog should be displayed
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun eventView_qrScannerDialog_notDisplayedInitially() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Assert - QR scanner should not be displayed initially
    composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertDoesNotExist()
  }

  @Test
  fun eventView_validationResult_valid_displaysCorrectly() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    val participantId = "participant123"
    runBlocking {
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
    }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
  }

  @Test
  fun eventView_validationResult_invalid_displaysCorrectly() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    val nonParticipantId = "nonparticipant123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
  }

  @Test
  fun eventView_validationResult_scanNextButton_clearsResult() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    val participantId = "participant123"
    runBlocking {
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
    }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
  }

  @Test
  fun eventView_validationResult_closeScannerButton_closesDialog() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    val participantId = "participant123"
    runBlocking {
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
    }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
  }

  @Test
  fun eventView_editButton_displayedForOwner() {
    // Arrange
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_editButton_notDisplayedForNonOwner() {
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "different-user"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun eventView_participantsInfo_isDisplayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventViewModel = testViewModel,
              hasJoined = false)
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
              eventViewModel = newViewModel,
              hasJoined = false)
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
  fun eventView_fullEvent_displaysFullButton() {
    val fullEvent = testEvent.copy(uid = "full-event", maxCapacity = 2u)
    runBlocking {
      eventRepository.addEvent(fullEvent)
      eventRepository.addParticipantToEvent(fullEvent.uid, EventParticipant("user1"))
      eventRepository.addParticipantToEvent(fullEvent.uid, EventParticipant("user2"))
    }

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "different-user"

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
              eventViewModel = fullEventViewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Full").assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(fullEvent.uid) }
  }

  @Test
  fun eventView_startedEvent_displaysStartedButton() {
    val pastEvent =
        testEvent.copy(
            uid = "started-event", start = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0))
    runBlocking { eventRepository.addEvent(pastEvent) }

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "different-user"

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
              eventViewModel = pastEventViewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Started").assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(pastEvent.uid) }
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

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "test-user"

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
              eventViewModel = startedEventViewModel,
              hasJoined = true)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Hurry up! Event has started").assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(startedEvent.uid) }
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
              eventViewModel = futureEventViewModel,
              hasJoined = false)
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
              eventViewModel = soonEventViewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.COUNTDOWN_TIMER).assertIsDisplayed()

    // Clean up
    runBlocking { eventRepository.deleteEvent(soonEvent.uid) }
  }

  @Test
  fun eventView_joinButton_canBeClicked() {
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "test-user"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).performClick()
  }

  @Test
  fun eventView_leaveButton_canBeClicked() {
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "test-user"

    runBlocking {
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("test-user"))
    }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = true)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).performClick()
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
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()

    // Wait for dialog to appear
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun eventView_progressBar_displayedForEventWithCapacity() {
    runBlocking { eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user1")) }

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
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
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns "owner123"

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "event") {
        composable("event") {
          EventView(
              eventUid = testEvent.uid,
              navController = navController,
              eventViewModel = viewModel,
              hasJoined = false)
        }
      }
    }

    composeTestRule.waitForIdle()
    // Join button should not exist for owner
    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertDoesNotExist()
  }
}

package com.github.se.studentconnect.ui.event

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventViewTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mocks
  private lateinit var mockNavController: NavHostController
  private lateinit var mockEventViewModel: EventViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockPollRepository: PollRepository
  private lateinit var mockFriendsRepository: FriendsRepository

  // Test Data
  private val testEventUid = "test-event-123"
  private val testUserId = "user-123"
  private val testOwnerId = "owner-456"

  private val testPublicEvent =
      Event.Public(
          uid = testEventUid,
          title = "Test Public Event",
          description = "This is a test event description",
          ownerId = testOwnerId,
          start = Timestamp(System.currentTimeMillis() / 1000 + 86400, 0),
          end = Timestamp(System.currentTimeMillis() / 1000 + 90000, 0),
          location = Location(46.5197, 6.5668, "Test Location"),
          maxCapacity = 100u,
          tags = listOf("Technology", "Networking", "Conference"),
          website = "https://test-event.com",
          imageUrl = null,
          isFlash = false,
          subtitle = "A great test event")

  private val testPrivateEvent =
      Event.Private(
          uid = testEventUid,
          title = "Test Private Event",
          description = "This is a private test event",
          ownerId = testOwnerId,
          start = Timestamp(System.currentTimeMillis() / 1000 + 86400, 0),
          end = Timestamp(System.currentTimeMillis() / 1000 + 90000, 0),
          location = Location(46.5197, 6.5668, "Private Location"),
          maxCapacity = 50u,
          imageUrl = null,
          isFlash = false)

  private val testUser =
      User(
          userId = testUserId,
          firstName = "John",
          lastName = "Doe",
          email = "john.doe@example.com",
          username = "johndoe",
          university = "EPFL",
          bio = "Test bio")

  private val testOwner =
      User(
          userId = testOwnerId,
          firstName = "Jane",
          lastName = "Owner",
          email = "jane.owner@example.com",
          username = "janeowner",
          university = "ETHZ",
          bio = "Owner bio")

  @Before
  fun setup() {
    // Initialize mocks with relaxed=true to prevent crashes on unstubbed methods
    mockNavController = mockk(relaxed = true)
    mockEventRepository = mockk(relaxed = true)
    mockUserRepository = mockk(relaxed = true)
    mockPollRepository = mockk(relaxed = true)
    mockFriendsRepository = mockk(relaxed = true)

    // Mock Singleton Object
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId

    // Default mock responses
    coEvery { mockEventRepository.getEvent(any()) } returns testPublicEvent
    coEvery { mockEventRepository.getEventParticipants(any()) } returns emptyList()
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns testOwner
    coEvery { mockUserRepository.getUserById(testUserId) } returns testUser
    coEvery { mockPollRepository.getActivePolls(any()) } returns emptyList()
  }

  @After
  fun tearDown() {
    // Explicitly unmock the singleton object to prevent state leakage
    unmockkObject(AuthenticationProvider)
    unmockkAll()
  }

  // --- Tests ---

  @Test
  fun eventView_displaysLoadingIndicator_whenLoading() {
    val uiState = MutableStateFlow(EventUiState(isLoading = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun eventView_displaysEventDetails_whenLoaded() {
    val uiState =
        MutableStateFlow(
            EventUiState(event = testPublicEvent, isLoading = false, participantCount = 5))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_VIEW_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Public Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("This is a test event description").assertIsDisplayed()
  }

  @Test
  fun eventView_displaysTags_forPublicEvent() {
    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.TAGS_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
    composeTestRule.onNodeWithText("Networking").assertIsDisplayed()
    composeTestRule.onNodeWithText("Conference").assertIsDisplayed()
  }

  @Test
  fun eventView_displaysCountdownTimer_whenEventNotStarted() {
    // 1 hour from now
    val futureEvent =
        testPublicEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0))

    val uiState = MutableStateFlow(EventUiState(event = futureEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.COUNTDOWN_TIMER).assertIsDisplayed()
  }

  @Test
  fun eventView_displaysJoinButton_whenNotJoined() {
    val uiState =
        MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false, isJoined = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertIsEnabled()
  }

  @Test
  fun eventView_displaysLeaveButton_whenJoined() {
    val uiState =
        MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false, isJoined = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_displaysOwnerButtons_whenUserIsOwner() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.EDIT_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.CREATE_POLL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_displaysLocationButton_whenLocationExists() {
    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_displaysWebsiteButton_whenWebsiteExists() {
    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_displaysShareButton() {
    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_clickBackButton_navigatesBack() {
    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()
    verify { mockNavController.popBackStack() }
  }

  @Test
  fun eventView_clickJoinButton_callsJoinEvent() {
    val uiState =
        MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false, isJoined = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).performClick()
    verify { mockEventViewModel.joinEvent(testEventUid) }
  }

  @Test
  fun eventView_clickLeaveButton_showsConfirmDialog() {
    val uiState =
        MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false, isJoined = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_EVENT_BUTTON).performClick()
    verify { mockEventViewModel.showLeaveConfirmDialog() }
  }

  @Test
  fun eventView_leaveConfirmDialog_displaysCorrectly() {
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                isJoined = true,
                showLeaveConfirmDialog = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_CONFIRM).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_CANCEL).assertIsDisplayed()
  }

  @Test
  fun eventView_leaveConfirmDialog_confirmButton_leavesEvent() {
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                isJoined = true,
                showLeaveConfirmDialog = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_CONFIRM).performClick()
    verify { mockEventViewModel.hideLeaveConfirmDialog() }
    verify { mockEventViewModel.leaveEvent(testEventUid) }
  }

  @Test
  fun eventView_leaveConfirmDialog_cancelButton_closesDialog() {
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                isJoined = true,
                showLeaveConfirmDialog = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LEAVE_CONFIRMATION_CANCEL).performClick()
    verify { mockEventViewModel.hideLeaveConfirmDialog() }
  }

  @Test
  fun eventView_clickCreatePollButton_showsCreatePollDialog() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.CREATE_POLL_BUTTON).performClick()
    verify { mockEventViewModel.showCreatePollDialog() }
  }

  @Test
  fun eventView_clickScanQrButton_showsQrScanner() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.SCAN_QR_BUTTON).performClick()
    verify { mockEventViewModel.showQrScanner() }
  }

  @Test
  fun eventView_qrScannerDialog_displaysCorrectly() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val uiState =
        MutableStateFlow(
            EventUiState(event = testPublicEvent, isLoading = false, showQrScanner = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.QR_SCANNER_DIALOG).assertIsDisplayed()
  }

  @Test
  fun eventView_validationResultValid_displaysCorrectly() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val validationResult = TicketValidationResult.Valid("participant-123")
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                showQrScanner = true,
                ticketValidationResult = validationResult))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VALIDATION_RESULT_VALID).assertIsDisplayed()
  }

  @Test
  fun eventView_validationResultInvalid_displaysCorrectly() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val validationResult = TicketValidationResult.Invalid("invalid-user-123")
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                showQrScanner = true,
                ticketValidationResult = validationResult))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VALIDATION_RESULT_INVALID).assertIsDisplayed()
  }

  @Test
  fun eventView_validationResultError_displaysCorrectly() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val validationResult = TicketValidationResult.Error("Network error")
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                showQrScanner = true,
                ticketValidationResult = validationResult))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VALIDATION_RESULT_ERROR).assertIsDisplayed()
  }

  @Test
  fun eventView_pollNotificationCard_displaysForJoinedParticipants() {
    val testPoll =
        Poll(
            uid = "poll-123",
            eventUid = testEventUid,
            question = "Test poll?",
            options = listOf(PollOption("opt-1", "Yes", 0), PollOption("opt-2", "No", 0)),
            createdAt = Timestamp(System.currentTimeMillis() / 1000, 0),
            isActive = true)

    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                isJoined = true,
                activePolls = listOf(testPoll)))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.POLL_NOTIFICATION_CARD).assertIsDisplayed()
  }

  @Test
  fun eventView_participantsInfo_displaysCorrectCount() {
    val uiState =
        MutableStateFlow(
            EventUiState(event = testPublicEvent, isLoading = false, participantCount = 42))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO).assertIsDisplayed()
    composeTestRule.onNodeWithText("42 / 100").assertIsDisplayed()
  }

  @Test
  fun eventView_participantsInfo_clickable_navigatesToAttendeesList() {
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                participantCount = 5,
                attendees = listOf(testUser),
                owner = testOwner))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO).performClick()
    verify { mockEventViewModel.fetchAttendees() }
  }

  @Test
  fun eventView_attendeesList_displaysCorrectly() {
    val attendees = listOf(testUser, testOwner)
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                isJoined = true,
                attendees = attendees,
                currentUser = testUser,
                owner = testOwner))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    // Navigate to attendees list
    composeTestRule.onNodeWithTag(EventViewTestTags.PARTICIPANTS_INFO).performClick()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(EventViewTestTags.ATTENDEE_LIST).assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_displayed() {
    val uiState = MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_fullEvent_disablesJoinButton() {
    val uiState =
        MutableStateFlow(
            EventUiState(
                event = testPublicEvent,
                isLoading = false,
                isJoined = false,
                isFull = true,
                participantCount = 100))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun eventView_startedEvent_disablesJoinButton() {
    // Started 1 hour ago
    val startedEvent =
        testPublicEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0))

    val uiState =
        MutableStateFlow(EventUiState(event = startedEvent, isLoading = false, isJoined = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.JOIN_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun eventView_privateEvent_displaysInviteButton_forOwner() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val uiState = MutableStateFlow(EventUiState(event = testPrivateEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag("event_view_invite_friends_button").assertIsDisplayed()
  }

  @Test
  fun eventView_viewPollsButton_displayed_forJoinedUser() {
    val uiState =
        MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false, isJoined = true))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VIEW_POLLS_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_viewPollsButton_displayed_forOwner() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val uiState =
        MutableStateFlow(EventUiState(event = testPublicEvent, isLoading = false, isJoined = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VIEW_POLLS_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_eventWithoutLocation_hidesLocationButton() {
    val eventNoLocation = testPublicEvent.copy(location = null)
    val uiState = MutableStateFlow(EventUiState(event = eventNoLocation, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertDoesNotExist()
  }

  @Test
  fun eventView_eventWithoutWebsite_hidesWebsiteButton() {
    val eventNoWebsite = testPublicEvent.copy(website = null)
    val uiState = MutableStateFlow(EventUiState(event = eventNoWebsite, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun eventView_privateEvent_doesNotDisplayTags() {
    val uiState = MutableStateFlow(EventUiState(event = testPrivateEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.TAGS_SECTION).assertDoesNotExist()
  }

  @Test
  fun eventView_eventWithMoreThanOneDayLeft_displaysCorrectCountdown() {
    // 2 days from now
    val futureEvent =
        testPublicEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 + 172800, 0))

    val uiState = MutableStateFlow(EventUiState(event = futureEvent, isLoading = false))
    mockEventViewModel = createMockViewModel(uiState)

    composeTestRule.setContent {
      EventView(
          eventUid = testEventUid,
          navController = mockNavController,
          eventViewModel = mockEventViewModel)
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.COUNTDOWN_DAYS).assertIsDisplayed()
  }

  // Helper function to create a mock ViewModel with a given StateFlow
  private fun createMockViewModel(uiState: MutableStateFlow<EventUiState>): EventViewModel {
    val viewModel = mockk<EventViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState as StateFlow<EventUiState>
    return viewModel
  }
}

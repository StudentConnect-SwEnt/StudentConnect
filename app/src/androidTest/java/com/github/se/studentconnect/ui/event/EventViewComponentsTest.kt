package com.github.se.studentconnect.ui.event

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.activities.EventActionButtons
import com.google.firebase.Timestamp
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventViewComponentsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavController: NavHostController
  private lateinit var mockEventViewModel: EventViewModel
  private lateinit var testContext: Context

  private val testEventUid = "test-event-123"
  private val testUserId = "user-123"
  private val testOwnerId = "owner-456"

  private val testPublicEvent =
      Event.Public(
          uid = testEventUid,
          title = "Test Event",
          description = "Test Description",
          ownerId = testOwnerId,
          start = Timestamp(System.currentTimeMillis() / 1000 + 86400, 0),
          end = Timestamp(System.currentTimeMillis() / 1000 + 90000, 0),
          location = Location(46.5197, 6.5668, "Test Location"),
          maxCapacity = 100u,
          tags = listOf("Technology", "Networking"),
          website = "https://test.com",
          imageUrl = null,
          isFlash = false,
          subtitle = "A great event")

  private val testPrivateEvent =
      Event.Private(
          uid = testEventUid,
          title = "Private Event",
          description = "Private Description",
          ownerId = testOwnerId,
          start = Timestamp(System.currentTimeMillis() / 1000 + 86400, 0),
          end = Timestamp(System.currentTimeMillis() / 1000 + 90000, 0),
          location = Location(46.5197, 6.5668, "Private Location"),
          maxCapacity = 50u,
          imageUrl = null,
          isFlash = false)

  @Before
  fun setup() {
    testContext = ApplicationProvider.getApplicationContext()
    mockNavController = mockk(relaxed = true)
    mockEventViewModel = mockk(relaxed = true)

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun eventActionButtons_ownerPublicEvent_displaysAllButtons() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_create_poll_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("event_view_scan_qr_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("event_view_edit_event_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("event_view_location_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("event_view_visit_website_button").assertIsDisplayed()
  }

  @Test
  fun eventActionButtons_ownerPrivateEvent_displaysInviteButton() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPrivateEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_invite_friends_button").assertIsDisplayed()
  }

  @Test
  fun eventActionButtons_nonOwner_displaysJoinButton() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_join_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("event_view_join_button").assertIsEnabled()
  }

  @Test
  fun eventActionButtons_nonOwnerJoined_displaysLeaveButton() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = true,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_leave_event_button").assertIsDisplayed()
  }

  @Test
  fun eventActionButtons_eventFull_disablesJoinButton() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = true,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_join_button").assertIsNotEnabled()
  }

  @Test
  fun eventActionButtons_eventStarted_disablesJoinButton() {
    every { AuthenticationProvider.currentUser } returns testUserId

    val startedEvent =
        testPublicEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0))

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = startedEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_join_button").assertIsNotEnabled()
  }

  @Test
  fun eventActionButtons_clickCreatePoll_callsViewModel() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_create_poll_button").performClick()
    verify { mockEventViewModel.showCreatePollDialog() }
  }

  @Test
  fun eventActionButtons_clickScanQr_callsViewModel() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_scan_qr_button").performClick()
    verify { mockEventViewModel.showQrScanner() }
  }

  @Test
  fun eventActionButtons_clickEdit_navigatesToEditScreen() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_edit_event_button").performClick()
    verify { mockNavController.navigate(any<String>()) }
  }

  @Test
  fun eventActionButtons_clickInviteFriends_callsViewModel() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPrivateEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_invite_friends_button").performClick()
    verify { mockEventViewModel.showInviteFriendsDialog() }
  }

  @Test
  fun eventActionButtons_clickLocation_navigatesToMap() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_location_button").performClick()
    verify { mockNavController.navigate(any<String>()) }
  }

  @Test
  fun eventActionButtons_eventWithoutLocation_hidesLocationButton() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val eventNoLocation = testPublicEvent.copy(location = null)

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = eventNoLocation,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_location_button").assertDoesNotExist()
  }

  @Test
  fun eventActionButtons_eventWithoutWebsite_hidesWebsiteButton() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val eventNoWebsite = testPublicEvent.copy(website = null)

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = eventNoWebsite,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_visit_website_button").assertDoesNotExist()
  }

  @Test
  fun eventActionButtons_eventWithEmptyWebsite_hidesWebsiteButton() {
    every { AuthenticationProvider.currentUser } returns testOwnerId

    val eventEmptyWebsite = testPublicEvent.copy(website = "")

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = eventEmptyWebsite,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_visit_website_button").assertDoesNotExist()
  }

  @Test
  fun eventActionButtons_nonOwner_hidesOwnerOnlyButtons() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_create_poll_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("event_view_scan_qr_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("event_view_edit_event_button").assertDoesNotExist()
    composeTestRule.onNodeWithTag("event_view_invite_friends_button").assertDoesNotExist()
  }

  @Test
  fun eventActionButtons_clickJoin_callsViewModel() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_join_button").performClick()
    verify { mockEventViewModel.joinEvent(testEventUid, any<Context>()) }
  }

  @Test
  fun eventActionButtons_clickLeave_showsConfirmDialog() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = true,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_leave_event_button").performClick()
    verify { mockEventViewModel.showLeaveConfirmDialog() }
  }

  @Test
  fun eventActionButtons_nonOwner_displaysCommonButtons() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_location_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("event_view_visit_website_button").assertIsDisplayed()
  }

  @Test
  fun eventActionButtons_privateEventNonOwner_hidesInviteButton() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = false,
          isFull = false,
          currentEvent = testPrivateEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_invite_friends_button").assertDoesNotExist()
  }

  @Test
  fun eventActionButtons_leaveButtonEnabled_whenJoined() {
    every { AuthenticationProvider.currentUser } returns testUserId

    composeTestRule.setContent {
      EventActionButtons(
          joined = true,
          isFull = false,
          currentEvent = testPublicEvent,
          eventViewModel = mockEventViewModel,
          navController = mockNavController)
    }

    composeTestRule.onNodeWithTag("event_view_leave_event_button").assertIsEnabled()
  }
}

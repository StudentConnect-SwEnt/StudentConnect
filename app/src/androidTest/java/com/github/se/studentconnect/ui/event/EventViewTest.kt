package com.github.se.studentconnect.ui.event

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.activities.EventView
import com.github.se.studentconnect.ui.activities.EventViewTestTags
import com.github.se.studentconnect.viewmodel.EventViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
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
          tags = listOf("tech", "networking"))

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = EventViewModel(eventRepository, userRepository)

    runBlocking { eventRepository.addEvent(testEvent) }
  }

  @Test
  fun eventView_mainScreen_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.EVENT_VIEW_SCREEN))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_VIEW_SCREEN).assertIsDisplayed()
  }

  @Test
  fun eventView_topAppBar_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.TOP_APP_BAR))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.TOP_APP_BAR).assertIsDisplayed()
  }

  @Test
  fun eventView_backButton_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.BACK_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_backButton_hasClickAction() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.BACK_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_eventTitle_isDisplayedInTopBar() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText(testEvent.title))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText(testEvent.title).assertIsDisplayed()
  }

  @Test
  fun eventView_eventImage_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.EVENT_IMAGE))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_IMAGE).assertIsDisplayed()
  }

  @Test
  fun eventView_descriptionText_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.DESCRIPTION_TEXT))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
  }

  @Test
  fun eventView_descriptionContent_isCorrect() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText(testEvent.description))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText(testEvent.description).assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.CHAT_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_hasCorrectText() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Event chat"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Event chat").assertIsDisplayed()
  }

  @Test
  fun eventView_chatButton_hasClickAction() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.CHAT_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_actionButtonsSection_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.ACTION_BUTTONS_SECTION))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.ACTION_BUTTONS_SECTION).assertIsDisplayed()
  }

  @Test
  fun eventView_visitWebsiteButton_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.VISIT_WEBSITE_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_locationButton_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.LOCATION_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_shareButton_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.SHARE_EVENT_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun eventView_locationButton_hasClickAction() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.LOCATION_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_shareButton_hasClickAction() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.SHARE_EVENT_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertHasClickAction()
  }

  @Test
  fun eventView_whenNotJoined_displaysJoinButton() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Join"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Join").assertIsDisplayed()
  }

  @Test
  fun eventView_whenJoined_displaysLeaveButton() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = true)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Leave"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Leave").assertIsDisplayed()
  }

  @Test
  fun eventView_infoSection_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.INFO_SECTION))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.INFO_SECTION).assertIsDisplayed()
  }

  @Test
  fun eventView_descriptionLabel_isDisplayed() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasText("Description"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
  }

  @Test
  fun eventView_backButtonClick_doesNotCrash() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.BACK_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()
    // Should not crash
  }
}

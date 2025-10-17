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
  fun eventView_displaysAllComponentsWhenNotJoined() {
    composeTestRule.setContent {
      EventView(
          eventUid = testEvent.uid,
          navController = rememberNavController(),
          eventViewModel = viewModel,
          hasJoined = false)
    }

    // Wait for event to load
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodes(androidx.compose.ui.test.hasTestTag(EventViewTestTags.EVENT_VIEW_SCREEN))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify main screen components
    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_VIEW_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.TOP_APP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).assertHasClickAction()

    // Verify event content
    composeTestRule.onNodeWithText(testEvent.title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.EVENT_IMAGE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithText(testEvent.description).assertIsDisplayed()
    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.INFO_SECTION).assertIsDisplayed()

    // Verify chat button
    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Event chat").assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.CHAT_BUTTON).assertHasClickAction()

    // Verify action buttons
    composeTestRule.onNodeWithTag(EventViewTestTags.ACTION_BUTTONS_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.VISIT_WEBSITE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.LOCATION_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventViewTestTags.SHARE_EVENT_BUTTON).assertHasClickAction()

    // Verify join button
    composeTestRule.onNodeWithText("Join").assertIsDisplayed()

    // Verify back button works
    composeTestRule.onNodeWithTag(EventViewTestTags.BACK_BUTTON).performClick()
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
}

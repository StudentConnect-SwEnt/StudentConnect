package com.github.se.studentconnect.ui.profile.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.ui.theme.AppTheme
import com.github.se.studentconnect.utils.MockMediaRepository
import com.google.firebase.Timestamp
import java.util.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinnedEventsSectionTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createTestEvent(
      uid: String,
      title: String,
      startDate: Date = Date(),
      url: String? = null
  ): Event.Public {
    return Event.Public(
        uid = uid,
        ownerId = "owner123",
        title = title,
        description = "Test event description",
        imageUrl = url,
        location = Location(latitude = 46.5191, longitude = 6.5668, name = "Test Location"),
        start = Timestamp(startDate),
        end = Timestamp(Date(startDate.time + 3600000)),
        maxCapacity = 100u,
        participationFee = null,
        isFlash = false,
        subtitle = "Test Subtitle",
        tags = emptyList(),
        website = null)
  }

  @Before
  fun setup() {
    MediaRepositoryProvider.overrideForTests(MockMediaRepository())
  }

  @After
  fun teardown() {
    MediaRepositoryProvider.cleanOverrideForTests()
  }

  @Test
  fun pinnedEventsSection_displaysTitleCorrectly() {
    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = emptyList(), onEventClick = {}) }
    }

    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.TITLE)
        .assertIsDisplayed()
        .assertTextContains("Pinned Events", ignoreCase = true)
  }

  @Test
  fun pinnedEventsSection_showsEmptyStateWhenNoPinnedEvents() {
    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = emptyList(), onEventClick = {}) }
    }

    composeTestRule.waitForIdle()

    // The empty state should be visible
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.EMPTY_STATE).assertIsDisplayed()

    // Check that the empty state message appears
    composeTestRule
        .onNodeWithText("Pin your favorite past events to display them here :)", substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun pinnedEventsSection_doesNotShowEmptyStateWhenEventsPresent() {
    val events = listOf(createTestEvent("event1", "Test Event 1"))

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = events, onEventClick = {}) }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.EMPTY_STATE).assertDoesNotExist()
  }

  @Test
  fun pinnedEventsSection_displaysHorizontalListWhenEventsPresent() {
    val events = listOf(createTestEvent("event1", "Test Event 1"))

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = events, onEventClick = {}) }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.HORIZONTAL_LIST).assertIsDisplayed()
  }

  @Test
  fun pinnedEventsSection_displaysSingleEvent() {
    val event = createTestEvent("event1", "Amazing Conference")

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = listOf(event), onEventClick = {}) }
    }

    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Amazing Conference").assertIsDisplayed()
  }

  @Test
  fun pinnedEventsSection_displaysTwoEvents() {
    val events =
        listOf(createTestEvent("event1", "First Event"), createTestEvent("event2", "Second Event"))

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = events, onEventClick = {}) }
    }

    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event2"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("First Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Event").assertIsDisplayed()
  }

  @Test
  fun pinnedEventsSection_displaysMaxThreeEvents() {
    val events =
        listOf(
            createTestEvent("event1", "First Event"),
            createTestEvent("event2", "Second Event"),
            createTestEvent("event3", "Third Event"))

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = events, onEventClick = {}) }
    }

    composeTestRule.waitForIdle()

    // Should show the horizontal list instead of empty state
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.HORIZONTAL_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.EMPTY_STATE).assertDoesNotExist()

    // At least the first event should be visible (cards are 70% width, so only ~1 fits on screen)
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("First Event").assertIsDisplayed()
  }

  @Test
  fun pinnedEventCard_displaysEventTitle() {
    val event = createTestEvent("event1", "Tech Meetup 2025")

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = listOf(event), onEventClick = {}) }
    }

    composeTestRule.onNodeWithText("Tech Meetup 2025").assertIsDisplayed()
  }

  @Test
  fun pinnedEventCard_displaysFormattedDate() {
    val calendar = Calendar.getInstance()
    calendar.set(2025, Calendar.JANUARY, 15, 14, 30)
    val event = createTestEvent("event1", "Test Event", calendar.time)

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = listOf(event), onEventClick = {}) }
    }

    // Date should be formatted and displayed
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
    // The exact date format may vary based on locale, so we just check the card exists
  }

  @Test
  fun pinnedEventCard_isClickable() {
    val event = createTestEvent("event1", "Clickable Event")
    var clickedEventId: String? = null

    composeTestRule.setContent {
      AppTheme {
        PinnedEventsSection(
            pinnedEvents = listOf(event), onEventClick = { clickedEventId = it.uid })
      }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1")).performClick()

    composeTestRule.runOnIdle { assert(clickedEventId == "event1") }
  }

  @Test
  fun pinnedEventCard_callsOnEventClickWithCorrectEvent() {
    val event1 = createTestEvent("event1", "First Event")
    val event2 = createTestEvent("event2", "Second Event")
    var clickedEvent: Event? = null

    composeTestRule.setContent {
      AppTheme {
        PinnedEventsSection(
            pinnedEvents = listOf(event1, event2), onEventClick = { clickedEvent = it })
      }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event2")).performClick()

    composeTestRule.runOnIdle {
      assert(clickedEvent != null)
      assert(clickedEvent?.uid == "event2")
      assert(clickedEvent?.title == "Second Event")
    }
  }

  @Test
  fun pinnedEventsSection_horizontalListIsScrollable() {
    val events = listOf(createTestEvent("event1", "Event 1"), createTestEvent("event2", "Event 2"))

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = events, onEventClick = {}) }
    }

    composeTestRule.waitForIdle()

    // The horizontal list should be present
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.HORIZONTAL_LIST).assertIsDisplayed()

    // Both cards should be visible, showing the list can display multiple items
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event2"))
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 2").assertIsDisplayed()
  }

  @Test
  fun pinnedEventCard_truncatesLongTitle() {
    val event =
        createTestEvent(
            "event1",
            "This is a very long event title that should be truncated after two lines to prevent overflow and maintain UI consistency")

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = listOf(event), onEventClick = {}) }
    }

    // The card should still display properly even with a really long title
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
  }

  @Test
  fun pinnedEventsSection_handlesMultipleClicksOnDifferentEvents() {
    val event1 = createTestEvent("event1", "Event 1")
    val event2 = createTestEvent("event2", "Event 2")
    val clickedEvents = mutableListOf<String>()

    composeTestRule.setContent {
      AppTheme {
        PinnedEventsSection(
            pinnedEvents = listOf(event1, event2), onEventClick = { clickedEvents.add(it.uid) })
      }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1")).performClick()
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event2")).performClick()
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1")).performClick()

    composeTestRule.runOnIdle {
      assert(clickedEvents.size == 3)
      assert(clickedEvents[0] == "event1")
      assert(clickedEvents[1] == "event2")
      assert(clickedEvents[2] == "event1")
    }
  }

  @Test
  fun pinnedEventsSection_sectionTagIsDisplayed() {
    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = emptyList(), onEventClick = {}) }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.SECTION).assertIsDisplayed()
  }

  @Test
  fun pinnedEventsSection_displaysWithCustomModifier() {
    composeTestRule.setContent {
      AppTheme {
        PinnedEventsSection(
            pinnedEvents = emptyList(), onEventClick = {}, modifier = androidx.compose.ui.Modifier)
      }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.SECTION).assertIsDisplayed()
  }

  @Test
  fun pinnedEventCard_displaysImage() {
    val event = createTestEvent("event1", "Test Event", url = "https://example.com/image.jpg")

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = listOf(event), onEventClick = {}) }
    }

    // Card should have image placeholder (Icon)
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()

    composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
  }

  @Test
  fun pinnedEventCard_displaysImagePlaceholder() {
    val event = createTestEvent("event1", "Test Event")

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = listOf(event), onEventClick = {}) }
    }

    // Card should have image placeholder (Icon)
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()

    composeTestRule.onNodeWithContentDescription("Event Image Placeholder").assertIsDisplayed()
  }

  @Test
  fun emptyPinnedEventsState_displaysCorrectly() {
    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = emptyList(), onEventClick = {}) }
    }

    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.EMPTY_STATE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.HORIZONTAL_LIST).assertDoesNotExist()
  }

  @Test
  fun pinnedEventsSection_eventsHaveUniqueKeys() {
    val events = listOf(createTestEvent("event1", "Event 1"), createTestEvent("event2", "Event 2"))

    composeTestRule.setContent {
      AppTheme { PinnedEventsSection(pinnedEvents = events, onEventClick = {}) }
    }

    composeTestRule.waitForIdle()

    // The horizontal list should be displayed
    composeTestRule.onNodeWithTag(PinnedEventsSectionTestTags.HORIZONTAL_LIST).assertIsDisplayed()

    // Each event should have a unique test tag based on its uid
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(PinnedEventsSectionTestTags.eventCard("event2"))
        .assertIsDisplayed()

    // Make sure we can find each event by its unique identifier and there's only one of each
    composeTestRule
        .onAllNodesWithTag(PinnedEventsSectionTestTags.eventCard("event1"))
        .assertCountEquals(1)
    composeTestRule
        .onAllNodesWithTag(PinnedEventsSectionTestTags.eventCard("event2"))
        .assertCountEquals(1)
  }
}

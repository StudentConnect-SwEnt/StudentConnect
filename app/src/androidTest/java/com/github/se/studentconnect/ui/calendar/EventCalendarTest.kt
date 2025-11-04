package com.github.se.studentconnect.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCalendarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockEvents =
      listOf(
          Event.Public(
              uid = "test-event-1",
              ownerId = "test-owner",
              title = "Test Event",
              description = "Test Description",
              location = Location(0.0, 0.0, "Test Location"),
              start = Timestamp(Date()),
              participationFee = 10u,
              isFlash = false,
              subtitle = "Test Subtitle",
              tags = listOf("test", "event"),
              imageUrl = null))

  @Test
  fun eventCalendar_isDisplayed() {
    composeTestRule.setContent {
      AppTheme { EventCalendar(events = mockEvents, selectedDate = Date(), onDateSelected = {}) }
    }

    // Verify calendar container is displayed
    composeTestRule.onNodeWithTag(EventCalendarTestTags.CALENDAR_CONTAINER).assertIsDisplayed()

    // Verify instruction text is displayed
    composeTestRule.onNodeWithText("Select a date to view events").assertIsDisplayed()

    // Verify date picker is displayed
    composeTestRule.onNodeWithTag(EventCalendarTestTags.DATE_PICKER).assertIsDisplayed()
  }

  @Test
  fun eventCalendar_withEmptyEvents_isDisplayed() {
    composeTestRule.setContent {
      AppTheme { EventCalendar(events = emptyList(), selectedDate = null, onDateSelected = {}) }
    }

    // Verify calendar still displays with empty events
    composeTestRule.onNodeWithTag(EventCalendarTestTags.CALENDAR_CONTAINER).assertIsDisplayed()

    composeTestRule.onNodeWithText("Select a date to view events").assertIsDisplayed()
  }

  @Test
  fun eventCalendar_withSelectedDate_isDisplayed() {
    val selectedDate = Date()

    composeTestRule.setContent {
      AppTheme {
        EventCalendar(events = mockEvents, selectedDate = selectedDate, onDateSelected = {})
      }
    }

    // Verify calendar displays with selected date
    composeTestRule.onNodeWithTag(EventCalendarTestTags.CALENDAR_CONTAINER).assertIsDisplayed()

    composeTestRule.onNodeWithTag(EventCalendarTestTags.DATE_PICKER).assertIsDisplayed()
  }
}

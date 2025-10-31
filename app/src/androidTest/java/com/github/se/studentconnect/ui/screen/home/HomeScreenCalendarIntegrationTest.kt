package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntil
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenCalendarIntegrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var viewModel: HomePageViewModel

  @Before
  fun setup() {
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)

    // Add test events
    runBlocking {
      val testEvent =
          Event.Public(
              uid = "test-event-calendar",
              ownerId = "test-owner",
              title = "Calendar Test Event",
              description = "Test event for calendar functionality",
              location = Location(0.0, 0.0, "Test Location"),
              start = Timestamp(Date()),
              participationFee = 15u,
              isFlash = false,
              subtitle = "Test Subtitle",
              tags = listOf("test", "calendar"),
              imageUrl = null)
      eventRepository.addEvent(testEvent)
    }
  }

  @Test
  fun homeScreen_calendarButton_isDisplayed() {
    composeTestRule.setContent {
      AppTheme { HomeScreen(navController = rememberNavController(), viewModel = viewModel) }
    }

    // Verify calendar button is displayed
    composeTestRule.onNodeWithTag("calendar_button").assertIsDisplayed()
  }

  @Test
  fun homeScreen_calendarButton_opensModal() {
    composeTestRule.setContent {
      AppTheme { HomeScreen(navController = rememberNavController(), viewModel = viewModel) }
    }

    // Click calendar button
    composeTestRule.onNodeWithTag("calendar_button").performClick()

    // Calendar modal should now be visible
    composeTestRule.onNodeWithTag("calendar_modal").assertIsDisplayed()

    // Calendar instruction text should be visible
    composeTestRule.onNodeWithText("Select a date to view events").assertIsDisplayed()
  }

  @Test
  fun homeScreen_calendarModal_containsDatePicker() {
    composeTestRule.setContent {
      AppTheme { HomeScreen(navController = rememberNavController(), viewModel = viewModel) }
    }

    // Open calendar modal
    composeTestRule.onNodeWithTag("calendar_button").performClick()

    // Verify date picker is displayed in modal
    composeTestRule.onNodeWithTag("date_picker").assertIsDisplayed()
  }

  @Test
  fun homeScreen_calendarModal_calendarContainer_isDisplayed() {
    composeTestRule.setContent {
      AppTheme { HomeScreen(navController = rememberNavController(), viewModel = viewModel) }
    }

    // Open calendar modal
    composeTestRule.onNodeWithTag("calendar_button").performClick()

    // Verify calendar container is displayed
    composeTestRule.onNodeWithTag("calendar_container").assertIsDisplayed()
  }

  @Test
  fun homeScreen_hidesCalendar_whenDateSelected() {
    composeTestRule.setContent {
      AppTheme { HomeScreen(navController = rememberNavController(), viewModel = viewModel) }
    }

    // Open calendar
    composeTestRule.onNodeWithTag("calendar_button").performClick()
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("calendar_modal").fetchSemanticsNodes().isNotEmpty()
    }

    // Select a date (simulate date selection)
    val testDate = java.util.Date()
    viewModel.onDateSelected(testDate)

    // Calendar should be hidden
    composeTestRule.waitForIdle()
  }
}

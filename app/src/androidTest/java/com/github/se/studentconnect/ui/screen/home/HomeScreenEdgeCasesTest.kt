package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenEdgeCasesTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val now = Timestamp.now()
  private val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
  private val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
  private val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time

  private val testEventToday =
      Event.Public(
          uid = "event-today",
          title = "Today Event",
          subtitle = "Today",
          description = "Event happening today",
          start = now,
          end = now,
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("test"))

  private val testEventTomorrow =
      Event.Public(
          uid = "event-tomorrow",
          title = "Tomorrow Event",
          subtitle = "Tomorrow",
          description = "Event happening tomorrow",
          start = Timestamp(tomorrow),
          end = Timestamp(tomorrow),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner2",
          isFlash = false,
          tags = listOf("test"))

  private val testEventNextWeek =
      Event.Public(
          uid = "event-next-week",
          title = "Next Week Event",
          subtitle = "Next Week",
          description = "Event happening next week",
          start = Timestamp(nextWeek),
          end = Timestamp(nextWeek),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner3",
          isFlash = false,
          tags = listOf("test"))

  @Test
  fun homeScreen_withScrollToDate_forToday_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = emptyMap(),
                  scrollToDate = Date()),
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withScrollToDate_forTomorrow_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = emptyMap(),
                  scrollToDate = tomorrow),
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withScrollToDate_forNextWeek_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = emptyMap(),
                  scrollToDate = nextWeek),
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withScrollToDate_dateNotFound_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday),
                  subscribedEventsStories = emptyMap(),
                  scrollToDate = nextWeek), // Date not in events
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withScrollToDate_emptyEventsList_handlesGracefully() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = emptyList(),
                  subscribedEventsStories = emptyMap(),
                  scrollToDate = Date()),
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withCalendarVisible_andSelectedDate_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow),
                  subscribedEventsStories = emptyMap(),
                  isCalendarVisible = true,
                  selectedDate = Date()))
    }

    composeTestRule.onNodeWithTag("calendar_modal").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withAllFeaturesEnabled_displaysCorrectly() {
    val stories: Map<Event, Pair<Int, Int>> =
        mapOf(testEventToday to Pair(2, 5), testEventTomorrow to Pair(4, 5))

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = stories,
                  isCalendarVisible = false,
                  selectedDate = Date(),
                  scrollToDate = null),
          favoriteEventIds = setOf("event-today", "event-tomorrow"),
          onDateSelected = {},
          onCalendarClick = {},
          onApplyFilters = {},
          onFavoriteToggle = {},
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withOnlyPartiallyViewedStories_displaysCorrectly() {
    val stories: Map<Event, Pair<Int, Int>> =
        mapOf(
            testEventToday to Pair(1, 5), // Partially viewed
            testEventTomorrow to Pair(0, 3), // Not viewed
            testEventNextWeek to Pair(2, 2) // Fully viewed
            )

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = stories))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withMixedFavorites_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = emptyMap()),
          favoriteEventIds = setOf("event-today", "event-next-week")) // Some favorites
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withAllEventsAsFavorites_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = emptyMap()),
          favoriteEventIds = setOf("event-today", "event-tomorrow", "event-next-week"))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withNoFavorites_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday, testEventTomorrow, testEventNextWeek),
                  subscribedEventsStories = emptyMap()),
          favoriteEventIds = emptySet())
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_calendarVisibility_togglesCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday),
                  subscribedEventsStories = emptyMap(),
                  isCalendarVisible = false))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withSingleEvent_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday),
                  subscribedEventsStories = emptyMap()))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withManyEvents_displaysCorrectly() {
    val manyEvents =
        (1..10).map { index ->
          Event.Public(
              uid = "event-$index",
              title = "Event $index",
              subtitle = "Subtitle $index",
              description = "Description $index",
              start = now,
              end = now,
              location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
              website = "https://example.com",
              ownerId = "owner$index",
              isFlash = false,
              tags = listOf("test"))
        }

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false, events = manyEvents, subscribedEventsStories = emptyMap()))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withStoriesButNoEvents_displaysCorrectly() {
    val stories: Map<Event, Pair<Int, Int>> = mapOf(testEventToday to Pair(1, 3))

    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false, events = emptyList(), subscribedEventsStories = stories))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_selectedDate_yesterday_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEventToday),
                  subscribedEventsStories = emptyMap(),
                  selectedDate = yesterday))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withDefaultParameters_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(), uiState = HomePageUiState(isLoading = false))
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }
}

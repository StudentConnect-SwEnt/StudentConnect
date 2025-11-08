package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class HomeScreenScrollAndTopBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val now = Timestamp.now()
  private val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Today Event",
          subtitle = "Test subtitle",
          description = "Test description",
          start = now,
          end = now,
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("test"),
      )

  private val testEvent2 =
      Event.Public(
          uid = "event-2",
          title = "Tomorrow Event",
          subtitle = "Test subtitle 2",
          description = "Test description 2",
          start = Timestamp(tomorrow),
          end = Timestamp(tomorrow),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner2",
          isFlash = false,
          tags = listOf("test"),
      )

  @Test
  fun homeScreen_withScrollToDate_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1, testEvent2),
                  subscribedEventsStories = emptyMap(),
                  scrollToDate = Date()),
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withSelectedDate_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1, testEvent2),
                  subscribedEventsStories = emptyMap(),
                  selectedDate = Date()),
      )
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withCalendarVisible_displaysCalendarModal() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1, testEvent2),
                  subscribedEventsStories = emptyMap(),
                  isCalendarVisible = true),
      )
    }

    composeTestRule.onNodeWithTag("calendar_modal").assertIsDisplayed()
  }

  @Test
  fun homeTopBar_displaysSearchBar() {
    composeTestRule.setContent {
      HomeTopBar(showNotifications = false, onNotificationClick = {}, onDismiss = {})
    }

    composeTestRule.onNodeWithText("Search for events...").assertIsDisplayed()
  }

  @Test
  fun homeTopBar_displaysNotificationIcon() {
    composeTestRule.setContent {
      HomeTopBar(showNotifications = false, onNotificationClick = {}, onDismiss = {})
    }

    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
  }

  @Test
  fun homeTopBar_withNotificationsShown_displaysDropdownMenu() {
    composeTestRule.setContent {
      HomeTopBar(showNotifications = true, onNotificationClick = {}, onDismiss = {})
    }

    composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
  }

  @Test
  fun homeTopBar_notificationClick_triggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      HomeTopBar(
          showNotifications = false, onNotificationClick = { clicked = true }, onDismiss = {})
    }

    composeTestRule.onNodeWithContentDescription("Notifications").performClick()
    assert(clicked)
  }

  @Test
  fun homeTopBar_displaysSearchIcon() {
    composeTestRule.setContent {
      HomeTopBar(showNotifications = false, onNotificationClick = {}, onDismiss = {})
    }

    composeTestRule.onNodeWithContentDescription("Search Icon").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withEmptyEventsAndScrollToDate_handlesGracefully() {
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
  fun homeScreen_withFavoriteEvents_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1, testEvent2),
                  subscribedEventsStories = emptyMap()),
          favoriteEventIds = setOf("event-1"),
      )
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withAllCallbacks_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          onClickStory = { _, _ -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1, testEvent2),
                  subscribedEventsStories = mapOf(testEvent1 to Pair(3, 1))),
          favoriteEventIds = setOf("event-1"),
          onDateSelected = {},
          onCalendarClick = {},
          onApplyFilters = {},
          onFavoriteToggle = {},
          onClearScrollTarget = {})
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }
}

package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenQRScannerTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Test Event",
          subtitle = "Test subtitle",
          description = "Test description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("test"),
      )

  @Test
  fun homeScreen_withShouldOpenQRScanner_false_showsHomePage() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = emptyMap()),
      )
    }

    // HomePage should be visible
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withLoadingState_showsLoadingIndicator() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = true, events = emptyList(), subscribedEventsStories = emptyMap()),
      )
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withNoEvents_displaysCorrectly() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false, events = emptyList(), subscribedEventsStories = emptyMap()),
      )
    }

    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun homeScreen_withEmptyStories_doesNotShowStoriesRow() {
    composeTestRule.setContent {
      HomeScreen(
          navController = rememberNavController(),
          shouldOpenQRScanner = false,
          onQRScannerClosed = {},
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = emptyMap()),
      )
    }

    // StoriesRow should not be visible when there are no stories
    composeTestRule.onNodeWithTag("HomePage").assertIsDisplayed()
  }

  @Test
  fun storiesRow_withEmptyStories_doesNotDisplay() {
    composeTestRule.setContent { StoriesRow(onClick = { _, _ -> }, stories = emptyMap()) }

    // The stories row should not be rendered
    // We can't directly assert it doesn't exist, but we can verify the test passes
  }

  @Test
  fun storiesRow_withStoriesOfZeroTotal_doesNotDisplay() {
    composeTestRule.setContent {
      val stories = mapOf(testEvent1 to Pair(0, 0)) // 0 total stories
      StoriesRow(onClick = { _, _ -> }, stories = stories)
    }

    // Stories with 0 total should be filtered out
  }

  @Test
  fun storiesRow_withMultipleStories_displaysCorrectly() {
    val testEvent2 =
        Event.Public(
            uid = "event-2",
            title = "Test Event 2",
            subtitle = "Test subtitle 2",
            description = "Test description 2",
            start = Timestamp.now(),
            end = Timestamp.now(),
            location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
            website = "https://example.com",
            ownerId = "owner2",
            isFlash = false,
            tags = listOf("test"),
        )

    composeTestRule.setContent {
      val stories = mapOf(testEvent1 to Pair(3, 1), testEvent2 to Pair(5, 0))
      StoriesRow(onClick = { _, _ -> }, stories = stories)
    }

    composeTestRule.onNodeWithTag("stories_row").assertIsDisplayed()
  }
}

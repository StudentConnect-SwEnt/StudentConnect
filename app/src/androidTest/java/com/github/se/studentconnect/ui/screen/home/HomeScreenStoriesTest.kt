package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.screens.HomeScreen
import com.github.se.studentconnect.viewmodel.HomePageUiState
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenStoriesTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Summer Festival",
          subtitle = "Best summer event",
          description = "Join us for an amazing summer festival.",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(latitude = 46.52, longitude = 6.57, name = "EPFL"),
          website = "https://example.com",
          ownerId = "owner1",
          isFlash = false,
          tags = listOf("music", "outdoor"),
      )

  @Test
  fun homeScreen_eventsStories_isDisplayed() {
    composeTestRule.setContent {
      HomeScreen(
          onClickStory = { e, i -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(1, 1)))),
              ),
      )
    }

    composeTestRule.onNodeWithContentDescription("Event Story").assertIsDisplayed()
  }

  @Test
  fun homeScreen_eventsStories_isNotDisplayed_whenNoStories() {
    composeTestRule.setContent {
      HomeScreen(
          onClickStory = { e, i -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(0, 0)))),
              ),
      )
    }

    composeTestRule.onNodeWithContentDescription("Event Story").assertIsNotDisplayed()
  }

  @Test
  fun homeScreen_eventsStories_hasClickAction() {
    composeTestRule.setContent {
      HomeScreen(
          onClickStory = { e, i -> },
          uiState =
              HomePageUiState(
                  isLoading = false,
                  events = listOf(testEvent1),
                  subscribedEventsStories = mapOf(pairs = arrayOf(Pair(testEvent1, Pair(1, 1)))),
              ),
      )
    }

    composeTestRule.onNodeWithContentDescription("Event Story").assertHasClickAction()
  }
}

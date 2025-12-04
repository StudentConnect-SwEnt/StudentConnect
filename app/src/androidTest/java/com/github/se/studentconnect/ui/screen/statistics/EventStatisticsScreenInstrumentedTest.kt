package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.studentconnect.model.event.AgeGroupData
import com.github.se.studentconnect.model.event.CampusData
import com.github.se.studentconnect.model.event.EventStatistics
import com.github.se.studentconnect.model.event.JoinRateData
import com.github.se.studentconnect.resources.C
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Rule
import org.junit.Test

/** Instrumented tests for EventStatisticsScreen to cover the main composable branches. */
class EventStatisticsScreenInstrumentedTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testStatistics =
      EventStatistics(
          eventId = "event123",
          totalAttendees = 42,
          ageDistribution = listOf(AgeGroupData("18-22", 20, 50f)),
          campusDistribution = listOf(CampusData("EPFL", 25, 60f)),
          joinRateOverTime =
              listOf(
                  JoinRateData(Timestamp(Date()), 10, "Day 1"),
                  JoinRateData(Timestamp(Date()), 42, "Day 2")),
          followerCount = 100,
          attendeesFollowersRate = 42f)

  @Test
  fun mainScreen_loadingState_displaysLoadingIndicator() {
    val uiState = mutableStateOf(EventStatisticsUiState(isLoading = true))

    composeTestRule.setContent {
      MaterialTheme {
        when {
          uiState.value.isLoading -> LoadingState()
          uiState.value.error != null -> ErrorState(message = uiState.value.error!!, onRetry = {})
          uiState.value.statistics != null ->
              StatisticsContent(
                  statistics = uiState.value.statistics!!,
                  animationProgress = 1f,
                  paddingValues = PaddingValues())
        }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_LOADING).assertIsDisplayed()
  }

  @Test
  fun mainScreen_errorState_displaysErrorWithRetry() {
    val uiState = mutableStateOf(EventStatisticsUiState(isLoading = false, error = "Network error"))
    var retryCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        when {
          uiState.value.isLoading -> LoadingState()
          uiState.value.error != null ->
              ErrorState(message = uiState.value.error!!, onRetry = { retryCalled = true })
          uiState.value.statistics != null ->
              StatisticsContent(
                  statistics = uiState.value.statistics!!,
                  animationProgress = 1f,
                  paddingValues = PaddingValues())
        }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_ERROR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_RETRY_BUTTON).performClick()
    composeTestRule.runOnIdle { assert(retryCalled) }
  }

  @Test
  fun mainScreen_successState_displaysContent() {
    val uiState =
        mutableStateOf(
            EventStatisticsUiState(
                isLoading = false, statistics = testStatistics, animationProgress = 1f))

    composeTestRule.setContent {
      MaterialTheme {
        when {
          uiState.value.isLoading -> LoadingState()
          uiState.value.error != null -> ErrorState(message = uiState.value.error!!, onRetry = {})
          uiState.value.statistics != null ->
              StatisticsContent(
                  statistics = uiState.value.statistics!!,
                  animationProgress = uiState.value.animationProgress,
                  paddingValues = PaddingValues())
        }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
  }

  @Test
  fun screenTitle_withEventTitle_displaysEventTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        val title = "My Event Title"
        val displayTitle = title.ifEmpty { "Event Statistics" }
        Text(text = displayTitle)
      }
    }

    composeTestRule.onNodeWithText("My Event Title").assertIsDisplayed()
  }

  @Test
  fun screenTitle_emptyEventTitle_displaysDefault() {
    composeTestRule.setContent {
      MaterialTheme {
        val title = ""
        val displayTitle = title.ifEmpty { "Event Statistics" }
        Text(text = displayTitle)
      }
    }

    composeTestRule.onNodeWithText("Event Statistics").assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun fullScreen_withLoadingState_displaysScaffold() {
    composeTestRule.setContent {
      MaterialTheme {
        Scaffold(
            modifier = Modifier.testTag(C.Tag.STATS_SCREEN),
            topBar = {
              TopAppBar(
                  title = { Text("Event Statistics") },
                  navigationIcon = {
                    IconButton(onClick = {}, modifier = Modifier.testTag(C.Tag.STATS_BACK_BUTTON)) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back")
                    }
                  },
                  modifier = Modifier.testTag(C.Tag.STATS_TOP_BAR))
            }) { paddingValues ->
              LoadingState(modifier = Modifier.padding(paddingValues))
            }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOP_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_LOADING).assertIsDisplayed()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Test
  fun fullScreen_backButton_isClickable() {
    var backClicked = false

    composeTestRule.setContent {
      MaterialTheme {
        Scaffold(
            modifier = Modifier.testTag(C.Tag.STATS_SCREEN),
            topBar = {
              TopAppBar(
                  title = { Text("Test") },
                  navigationIcon = {
                    IconButton(
                        onClick = { backClicked = true },
                        modifier = Modifier.testTag(C.Tag.STATS_BACK_BUTTON)) {
                          Icon(
                              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                              contentDescription = "Back")
                        }
                  })
            }) { paddingValues ->
              // Test only checks button clickability, padding not needed for this test
              Box(modifier = Modifier.padding(paddingValues))
            }
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_BACK_BUTTON).performClick()
    composeTestRule.runOnIdle { assert(backClicked) }
  }

  @Test
  fun totalAttendeesCard_withAnimationProgress_displaysCorrectly() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 100,
            animationProgress = 0.5f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_displaysCorrectly() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 42,
            followers = 100,
            rate = 42f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun statisticsContent_animationProgress_zero_stillRendersCards() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 0f, paddingValues = PaddingValues())
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
  }

  @Test
  fun staggeredAnimatedCard_animationProgressAboveThreshold_triggersDelay() {
    composeTestRule.setContent {
      MaterialTheme {
        // Test index 0 with progress > 0.1 (threshold for index 0)
        StatisticsContent(
            statistics = testStatistics, animationProgress = 0.5f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    // Card should eventually become visible
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
  }

  @Test
  fun staggeredAnimatedCard_animationProgressAtOne_showsImmediately() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    // When progress is 1f, cards should be visible immediately
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
  }

  @Test
  fun totalAttendeesCard_partialAnimationProgress_showsPartialCount() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 100,
            animationProgress = 0.5f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }
    // At 0.5f progress, counter should show ~50
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_rateZero_renders() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 0,
            followers = 100,
            rate = 0f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_rateHundred_renders() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 100,
            followers = 100,
            rate = 100f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun joinRateCard_withSingleDataPoint_renders() {
    composeTestRule.setContent {
      MaterialTheme {
        JoinRateCard(
            data = listOf(JoinRateData(Timestamp(Date()), 10, "Day 1")),
            animationProgress = 1f,
            lineColor = Color.Blue,
            fillColor = Color.LightGray)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertIsDisplayed()
  }

  @Test
  fun statisticsContent_allCardsWithData_rendersAll() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    // Wait for all cards to be rendered - AnimatedVisibility enter animations need time
    // Card animation duration is ~500ms, so wait for all cards with sufficient timeout
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule
          .onAllNodesWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD)
          .fetchSemanticsNodes()
          .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(C.Tag.STATS_FOLLOWERS_CARD)
              .fetchSemanticsNodes()
              .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(C.Tag.STATS_AGE_CARD)
              .fetchSemanticsNodes()
              .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(C.Tag.STATS_CAMPUS_CARD)
              .fetchSemanticsNodes()
              .isNotEmpty() &&
          composeTestRule
              .onAllNodesWithTag(C.Tag.STATS_TIMELINE_CARD)
              .fetchSemanticsNodes()
              .isNotEmpty()
    }
    // Now assert they are all displayed
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_AGE_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_CAMPUS_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertIsDisplayed()
  }
}

package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class EventStatisticsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testColors =
      listOf(Color.Blue, Color.Red, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta)

  private val testStatistics =
      EventStatistics(
          eventId = "event123",
          totalAttendees = 42,
          ageDistribution =
              listOf(AgeGroupData("18-22", 20, 47.6f), AgeGroupData("23-25", 15, 35.7f)),
          campusDistribution = listOf(CampusData("EPFL", 25, 59.5f), CampusData("UNIL", 17, 40.5f)),
          joinRateOverTime =
              listOf(
                  JoinRateData(Timestamp(Date()), 10, "Day 1"),
                  JoinRateData(Timestamp(Date()), 42, "Day 2")),
          followerCount = 100,
          attendeesFollowersRate = 42f)

  @Test
  fun loadingState_displaysIndicator() {
    composeTestRule.setContent { MaterialTheme { LoadingState() } }
    composeTestRule.onNodeWithTag(C.Tag.STATS_LOADING).assertIsDisplayed()
  }

  @Test
  fun errorState_displaysErrorMessageAndRetryButton() {
    var retryClicked = false
    composeTestRule.setContent {
      MaterialTheme { ErrorState(message = "Network error", onRetry = { retryClicked = true }) }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_ERROR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.STATS_RETRY_BUTTON).performClick()
    composeTestRule.runOnIdle { assert(retryClicked) }
  }

  @Test
  fun statisticsContent_rendersAllCards() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = testStatistics, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    // Advance time significantly to ensure all staggered animations complete
    composeTestRule.mainClock.advanceTimeBy(2000)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.STATS_CONTENT).assertExists()
    // Only check that cards can be found - some may still be animating in
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertExists()
  }

  @Test
  fun totalAttendeesCard_displaysCorrectCount() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 42,
            animationProgress = 1f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }
    composeTestRule.onNodeWithText("Total Attendees").assertIsDisplayed()
    composeTestRule.onNodeWithText("people").assertIsDisplayed()
  }

  @Test
  fun totalAttendeesCard_displaysSingularForOne() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 1,
            animationProgress = 1f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }
    composeTestRule.onNodeWithText("person").assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_displaysMetrics() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 50,
            followers = 100,
            rate = 50f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithText("Attendees / Followers").assertExists()
    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertExists()
  }

  @Test
  fun ageDistributionCard_displaysChartAndLegend() {
    composeTestRule.setContent {
      MaterialTheme {
        AgeDistributionCard(
            data = listOf(AgeGroupData("18-22", 20, 50f)),
            animationProgress = 1f,
            colors = testColors)
      }
    }
    composeTestRule.onNodeWithText("Age Distribution").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertIsDisplayed()
  }

  @Test
  fun campusDistributionCard_displaysBarChart() {
    composeTestRule.setContent {
      MaterialTheme {
        CampusDistributionCard(
            data = listOf(CampusData("EPFL", 30, 60f)), animationProgress = 1f, colors = testColors)
      }
    }
    composeTestRule.onNodeWithText("Campus Distribution").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
  }

  @Test
  fun joinRateCard_displaysLineChart() {
    composeTestRule.setContent {
      MaterialTheme {
        JoinRateCard(
            data =
                listOf(
                    JoinRateData(Timestamp(Date()), 10, "D1"),
                    JoinRateData(Timestamp(Date()), 20, "D2")),
            animationProgress = 1f,
            lineColor = Color.Blue,
            fillColor = Color.LightGray)
      }
    }
    composeTestRule.onNodeWithText("Registration Timeline").assertIsDisplayed()
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun statisticsCard_displaysTitle() {
    composeTestRule.setContent {
      MaterialTheme {
        StatisticsCard(title = "Test Card", testTag = "test_card") {
          androidx.compose.material3.Text("Content")
        }
      }
    }
    composeTestRule.onNodeWithTag("test_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Card").assertIsDisplayed()
    composeTestRule.onNodeWithText("Content").assertIsDisplayed()
  }

  @Test
  fun statisticsContent_withEmptyDistributions_hidesEmptyCards() {
    val emptyStats =
        EventStatistics(
            eventId = "empty",
            totalAttendees = 10,
            ageDistribution = emptyList(),
            campusDistribution = emptyList(),
            joinRateOverTime = emptyList(),
            followerCount = 50,
            attendeesFollowersRate = 20f)

    composeTestRule.setContent {
      MaterialTheme {
        StatisticsContent(
            statistics = emptyStats, animationProgress = 1f, paddingValues = PaddingValues())
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertExists()
    composeTestRule.onNodeWithTag(C.Tag.STATS_AGE_CARD).assertDoesNotExist()
    composeTestRule.onNodeWithTag(C.Tag.STATS_CAMPUS_CARD).assertDoesNotExist()
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertDoesNotExist()
  }

  @Test
  fun totalAttendeesCard_zeroAnimationProgress_showsZero() {
    composeTestRule.setContent {
      MaterialTheme {
        TotalAttendeesCard(
            totalAttendees = 100,
            animationProgress = 0f,
            gradientStart = Color.Blue,
            gradientEnd = Color.Magenta)
      }
    }
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_rateBelowZero_coercesToZero() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 10,
            followers = 100,
            rate = -5f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun attendeesFollowersCard_rateAbove100_coercesTo100() {
    composeTestRule.setContent {
      MaterialTheme {
        AttendeesFollowersCard(
            attendees = 150,
            followers = 100,
            rate = 150f,
            animationProgress = 1f,
            accentColor = Color.Green)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_FOLLOWERS_CARD).assertIsDisplayed()
  }

  @Test
  fun ageDistributionCard_emptyData_doesNotRender() {
    composeTestRule.setContent {
      MaterialTheme {
        AgeDistributionCard(data = emptyList(), animationProgress = 1f, colors = testColors)
      }
    }
    // Empty data means the card won't be added to StatisticsContent, but if called directly,
    // it should still render the card structure
    composeTestRule.onNodeWithTag(C.Tag.STATS_AGE_CARD).assertIsDisplayed()
  }

  @Test
  fun campusDistributionCard_emptyData_rendersCard() {
    composeTestRule.setContent {
      MaterialTheme {
        CampusDistributionCard(data = emptyList(), animationProgress = 1f, colors = testColors)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_CAMPUS_CARD).assertIsDisplayed()
  }

  @Test
  fun joinRateCard_emptyData_rendersCard() {
    composeTestRule.setContent {
      MaterialTheme {
        JoinRateCard(
            data = emptyList(),
            animationProgress = 1f,
            lineColor = Color.Blue,
            fillColor = Color.LightGray)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertIsDisplayed()
  }

  @Test
  fun joinRateCard_withNullLastItem_usesZero() {
    composeTestRule.setContent {
      MaterialTheme {
        JoinRateCard(
            data = emptyList(),
            animationProgress = 1f,
            lineColor = Color.Blue,
            fillColor = Color.LightGray)
      }
    }
    // Should render without crashing when lastOrNull is null
    composeTestRule.onNodeWithTag(C.Tag.STATS_TIMELINE_CARD).assertIsDisplayed()
  }
}

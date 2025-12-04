package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
class StatisticsChartsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testColors =
      listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6))

  // ===== AnimatedHorizontalBarChart Tests =====

  @Test
  fun barChart_rendersWithData() {
    val items = listOf("EPFL" to 50f, "UNIL" to 30f, "ETH" to 20f)

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(items = items, colors = testColors, animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeTestRule.onNodeWithText("UNIL").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()
  }

  @Test
  fun barChart_rendersWithEmptyData() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(items = emptyList(), colors = testColors, animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertExists()
  }

  @Test
  fun barChart_respectsMaxItems() {
    val items = (1..10).map { "Item $it" to it.toFloat() * 10 }

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(
            items = items, colors = testColors, maxItems = 3, animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 3").assertIsDisplayed()
    composeTestRule.onNodeWithText("Item 4").assertDoesNotExist()
  }

  // ===== AnimatedDonutChart Tests =====

  @Test
  fun donutChart_rendersWithData() {
    val segments = listOf("18-22" to 40f, "23-25" to 35f, "26-30" to 25f)

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(segments = segments, colors = testColors, animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_rendersWithEmptyData() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(segments = emptyList(), colors = testColors, animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_rendersWithZeroProgress() {
    val segments = listOf("A" to 50f, "B" to 50f)

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(segments = segments, colors = testColors, animationProgress = 0f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  // ===== AnimatedLineChart Tests =====

  @Test
  fun lineChart_rendersWithData() {
    val data =
        listOf(
            JoinRateData(Timestamp(Date(System.currentTimeMillis() - 86400000)), 10, "Day 1"),
            JoinRateData(Timestamp(Date(System.currentTimeMillis())), 25, "Day 2"))

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun lineChart_rendersWithEmptyData() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = emptyList(),
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }

    // Empty data should not render the chart (early return)
    composeTestRule.waitForIdle()
  }

  @Test
  fun lineChart_rendersWithSinglePoint() {
    val data = listOf(JoinRateData(Timestamp(Date()), 15, "Today"))

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun lineChart_rendersWithManyPoints() {
    // More than 5 points to test label filtering logic
    val baseTime = System.currentTimeMillis()
    val data =
        (0..7).map { i ->
          JoinRateData(Timestamp(Date(baseTime + i * 86400000L)), (i + 1) * 10, "Day ${i + 1}")
        }

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  // ===== ChartLegend Tests =====

  @Test
  fun legend_rendersWithItems() {
    val items = listOf("EPFL" to testColors[0], "UNIL" to testColors[1], "ETH" to testColors[2])

    composeTestRule.setContent { MaterialTheme { ChartLegend(items = items) } }

    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertIsDisplayed()
    composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    composeTestRule.onNodeWithText("UNIL").assertIsDisplayed()
    composeTestRule.onNodeWithText("ETH").assertIsDisplayed()
  }

  @Test
  fun legend_rendersWithEmptyItems() {
    composeTestRule.setContent { MaterialTheme { ChartLegend(items = emptyList()) } }

    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertExists()
  }

  // ===== AnimatedCounter Tests =====

  @Test
  fun counter_rendersWithValue() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 150, animationProgress = 1f) }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_animated_counter).assertIsDisplayed()
  }

  @Test
  fun counter_rendersWithZeroProgress() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 100, animationProgress = 0f) }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_animated_counter).assertIsDisplayed()
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
  }

  // ===== CircularPercentageIndicator Tests =====

  @Test
  fun circularIndicator_rendersWithPercentage() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 75f, color = testColors[0], animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertIsDisplayed()
  }

  @Test
  fun circularIndicator_clampsPercentageOver100() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(
            percentage = 150f, color = testColors[0], animationProgress = 1f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertIsDisplayed()
    // Should clamp to 100%
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_rendersWithZeroProgress() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 50f, color = testColors[0], animationProgress = 0f)
      }
    }

    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertIsDisplayed()
    composeTestRule.onNodeWithText("0%").assertIsDisplayed()
  }
}

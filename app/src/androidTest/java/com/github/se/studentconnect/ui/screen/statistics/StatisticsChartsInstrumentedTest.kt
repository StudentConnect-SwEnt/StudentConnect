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

/**
 * Instrumented tests for StatisticsCharts components. These tests run on a real device/emulator and
 * execute Canvas drawing code.
 */
class StatisticsChartsInstrumentedTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))

  // ===== Donut Chart Canvas Coverage =====

  @Test
  fun donutChart_executesCanvasDrawing() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(
            segments = listOf("A" to 40f, "B" to 35f, "C" to 25f),
            colors = testColors,
            animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_withSingleSegment_executesCanvas() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(
            segments = listOf("Only" to 100f), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  // ===== Line Chart Canvas Coverage =====

  @Test
  fun lineChart_executesCanvasDrawing_multiplePoints() {
    val data =
        listOf(
            JoinRateData(Timestamp(Date()), 10, "D1"),
            JoinRateData(Timestamp(Date()), 30, "D2"),
            JoinRateData(Timestamp(Date()), 25, "D3"))

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun lineChart_executesCanvasDrawing_singlePoint() {
    val data = listOf(JoinRateData(Timestamp(Date()), 50, "Only"))

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun lineChart_executesCanvasDrawing_manyPoints() {
    val data = (1..8).map { JoinRateData(Timestamp(Date()), it * 10, "D$it") }

    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  // ===== Circular Indicator Canvas Coverage =====

  @Test
  fun circularIndicator_executesCanvasDrawing() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 75f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertIsDisplayed()
  }

  @Test
  fun circularIndicator_executesCanvasDrawing_fullProgress() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(
            percentage = 100f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_executesCanvasDrawing_zeroProgress() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 0f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("0%").assertIsDisplayed()
  }

  // ===== Bar Chart Coverage =====

  @Test
  fun barChart_executesAllPaths() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(
            items = listOf("A" to 80f, "B" to 60f, "C" to 40f, "D" to 20f),
            colors = testColors,
            animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
    composeTestRule.onNodeWithText("80%").assertIsDisplayed()
  }

  // ===== Legend Coverage =====

  @Test
  fun legend_executesAllPaths() {
    composeTestRule.setContent {
      MaterialTheme {
        ChartLegend(
            items = listOf("A" to testColors[0], "B" to testColors[1], "C" to testColors[2]))
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertIsDisplayed()
  }

  // ===== Counter Coverage =====

  @Test
  fun counter_executesAllPaths() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 500, animationProgress = 1f) }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_animated_counter).assertIsDisplayed()
  }
}

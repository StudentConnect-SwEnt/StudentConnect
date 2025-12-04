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
 * Instrumented tests targeting specific condition branches for maximum coverage. Each test targets
 * specific uncovered conditions identified in SonarQube.
 */
class StatisticsChartsInstrumentedTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))

  // ===== BAR CHART: Targets if (animationProgress > 0f) branches =====

  @Test
  fun barChart_progressOne_hitsIfTrue() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(
            items = listOf("A" to 50f, "B" to 30f), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
  }

  @Test
  fun barChart_progressZero_hitsIfFalse() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(
            items = listOf("A" to 50f), colors = testColors, animationProgress = 0f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
  }

  @Test
  fun barChart_emptyItems_hitsFallback() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(items = emptyList(), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertExists()
  }

  // ===== DONUT CHART: Targets if (animationProgress > 0f) branches =====

  @Test
  fun donutChart_progressOne_hitsIfTrue() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(
            segments = listOf("A" to 60f, "B" to 40f), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_progressZero_hitsIfFalse() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(
            segments = listOf("A" to 50f), colors = testColors, animationProgress = 0f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_emptySegments_hitsZeroTotal() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(segments = emptyList(), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertExists()
  }

  // ===== LINE CHART: Targets all condition branches =====

  @Test
  fun lineChart_emptyData_hitsReturnEarly() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = emptyList(),
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun lineChart_singlePoint_hitsSizeLessThanMin() {
    val data = listOf(JoinRateData(Timestamp(Date()), 50, "D1"))
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
  fun lineChart_multiplePoints_hitsSizeGreaterThanMin() {
    val data =
        listOf(
            JoinRateData(Timestamp(Date()), 10, "D1"),
            JoinRateData(Timestamp(Date()), 30, "D2"),
            JoinRateData(Timestamp(Date()), 20, "D3"))
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
  fun lineChart_partialProgress_hitsIsPointVisibleFalse() {
    // With 0.25f progress, only first point visible, rest hidden
    val data =
        listOf(
            JoinRateData(Timestamp(Date()), 10, "D1"),
            JoinRateData(Timestamp(Date()), 20, "D2"),
            JoinRateData(Timestamp(Date()), 30, "D3"),
            JoinRateData(Timestamp(Date()), 40, "D4"))
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = data,
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 0.25f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun lineChart_manyPoints_hitsLabelSkip() {
    // With >5 points, middle labels are skipped
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

  @Test
  fun lineChart_fewPoints_showsAllLabels() {
    val data =
        listOf(
            JoinRateData(Timestamp(Date()), 10, "A"),
            JoinRateData(Timestamp(Date()), 20, "B"),
            JoinRateData(Timestamp(Date()), 30, "C"))
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
  fun lineChart_sameValues_hitsCoerceAtLeast() {
    // When all values are the same, range is 0, coerceAtLeast(1) is triggered
    val data =
        listOf(JoinRateData(Timestamp(Date()), 50, "D1"), JoinRateData(Timestamp(Date()), 50, "D2"))
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

  // ===== COUNTER: Targets if (animationProgress > 0f) branches =====

  @Test
  fun counter_progressOne_hitsIfTrue() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 100, animationProgress = 1f) }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_animated_counter).assertIsDisplayed()
  }

  @Test
  fun counter_progressZero_hitsIfFalse() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 100, animationProgress = 0f) }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
  }

  // ===== CIRCULAR INDICATOR: Targets all branches =====

  @Test
  fun circularIndicator_progressOne_hitsIfTrue() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 75f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertIsDisplayed()
  }

  @Test
  fun circularIndicator_progressZero_hitsIfFalse() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 50f, color = testColors[0], animationProgress = 0f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("0%").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_over100_clampsMax() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(
            percentage = 150f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_negative_clampsMin() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(
            percentage = -20f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("0%").assertIsDisplayed()
  }

  // ===== LEGEND =====

  @Test
  fun legend_withItems() {
    composeTestRule.setContent {
      MaterialTheme { ChartLegend(items = listOf("A" to testColors[0], "B" to testColors[1])) }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertIsDisplayed()
  }

  @Test
  fun legend_empty() {
    composeTestRule.setContent { MaterialTheme { ChartLegend(items = emptyList()) } }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertExists()
  }
}

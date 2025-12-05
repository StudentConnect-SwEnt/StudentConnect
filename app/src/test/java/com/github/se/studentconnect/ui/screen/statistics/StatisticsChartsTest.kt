package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.se.studentconnect.model.event.JoinRateData
import com.github.se.studentconnect.resources.C
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class StatisticsChartsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))

  // ===== UNIT TESTS FOR ALL HELPER FUNCTIONS =====

  @Test
  fun calculateChartPoints_conditionCoverage() {
    // Condition 1: data.isEmpty() - TRUE
    assertTrue(calculateChartPoints(emptyList(), 100f, 100f, 10f).isEmpty())

    // Condition 1: data.isEmpty() - FALSE (non-empty)
    val single = listOf(JoinRateData(Timestamp(Date()), 50, "D1"))
    assertFalse(calculateChartPoints(single, 100f, 100f, 10f).isEmpty())

    // Condition 2: data.size > 1 - FALSE (single point, else branch)
    assertEquals(50f, calculateChartPoints(single, 100f, 100f, 10f)[0].x, 0.1f)

    // Condition 2: data.size > 1 - TRUE (multiple points, if branch)
    val multi =
        listOf(JoinRateData(Timestamp(Date()), 0, "D1"), JoinRateData(Timestamp(Date()), 100, "D2"))
    val result = calculateChartPoints(multi, 100f, 100f, 10f)
    assertEquals(0f, result[0].x, 0.1f)
    assertEquals(100f, result[1].x, 0.1f)

    // Condition 3: coerceAtLeast(1) - when maxValue == minValue (division would be 0)
    val same =
        listOf(JoinRateData(Timestamp(Date()), 10, "D1"), JoinRateData(Timestamp(Date()), 10, "D2"))
    assertEquals(2, calculateChartPoints(same, 100f, 100f, 10f).size)

    // Condition 3: coerceAtLeast(1) - normal case (maxValue != minValue)
    val different =
        listOf(JoinRateData(Timestamp(Date()), 0, "D1"), JoinRateData(Timestamp(Date()), 100, "D2"))
    assertEquals(2, calculateChartPoints(different, 100f, 100f, 10f).size)
  }

  @Test
  fun createFillPath_conditionCoverage() {
    // Condition: points.isEmpty() - TRUE
    assertTrue(createFillPath(emptyList(), 100f).isEmpty)

    // Condition: points.isEmpty() - FALSE (single point)
    assertFalse(createFillPath(listOf(Offset(50f, 50f)), 100f).isEmpty)

    // Condition: points.isEmpty() - FALSE (multiple points)
    assertFalse(createFillPath(listOf(Offset(0f, 50f), Offset(100f, 75f)), 100f).isEmpty)
    assertFalse(
        createFillPath(listOf(Offset(0f, 0f), Offset(50f, 50f), Offset(100f, 100f)), 100f).isEmpty)
  }

  @Test
  fun createLinePath_conditionCoverage() {
    // Condition 1: points.isEmpty() - TRUE
    assertTrue(createLinePath(emptyList()).isEmpty)

    // Condition 1: points.isEmpty() - FALSE, loop doesn't execute (size == 1)
    assertFalse(createLinePath(listOf(Offset(50f, 50f))).isEmpty)

    // Condition 1: points.isEmpty() - FALSE, loop executes (size == 2)
    assertFalse(createLinePath(listOf(Offset(0f, 0f), Offset(100f, 100f))).isEmpty)

    // Condition 1: points.isEmpty() - FALSE, loop executes multiple times (size > 2)
    assertFalse(
        createLinePath(listOf(Offset(0f, 0f), Offset(50f, 50f), Offset(100f, 100f))).isEmpty)
  }

  @Test
  fun shouldShowLabelAtIndex_conditionCoverage() {
    // Condition: index == 0 || index == dataSize - 1 || dataSize <= MAX_LABELS(5)

    // index == 0 TRUE (short-circuits)
    assertTrue(shouldShowLabelAtIndex(0, 10))
    assertTrue(shouldShowLabelAtIndex(0, 3))

    // index == 0 FALSE, index == dataSize-1 TRUE
    assertTrue(shouldShowLabelAtIndex(9, 10))
    assertTrue(shouldShowLabelAtIndex(5, 6))

    // index == 0 FALSE, index == dataSize-1 FALSE, dataSize <= 5 TRUE
    assertTrue(shouldShowLabelAtIndex(2, 5))
    assertTrue(shouldShowLabelAtIndex(1, 4))
    assertTrue(shouldShowLabelAtIndex(2, 3))

    // ALL FALSE: index != 0, index != last, dataSize > 5
    assertFalse(shouldShowLabelAtIndex(3, 10))
    assertFalse(shouldShowLabelAtIndex(5, 10))
    assertFalse(shouldShowLabelAtIndex(1, 8))
    assertFalse(shouldShowLabelAtIndex(4, 9))
  }

  @Test
  fun calculateChartRadius_returnsCorrectValue() {
    assertEquals(40f, calculateChartRadius(100f, 20f), 0.1f)
    assertEquals(0f, calculateChartRadius(20f, 20f), 0.1f)
    assertEquals(50f, calculateChartRadius(100f, 0f), 0.1f)
  }

  @Test
  fun calculateChartCenter_returnsCorrectValue() {
    val center = calculateChartCenter(100f, 200f)
    assertEquals(50f, center.x, 0.1f)
    assertEquals(100f, center.y, 0.1f)
  }

  @Test
  fun calculateDonutSweepAngle_conditionCoverage() {
    // Condition: totalPercentage == 0f - TRUE (guard returns 0)
    assertEquals(0f, calculateDonutSweepAngle(50f, 0f, 1f), 0.1f)
    assertEquals(0f, calculateDonutSweepAngle(0f, 0f, 1f), 0.1f)
    assertEquals(0f, calculateDonutSweepAngle(100f, 0f, 0.5f), 0.1f)

    // Condition: totalPercentage == 0f - FALSE (normal calculation)
    assertEquals(180f, calculateDonutSweepAngle(50f, 100f, 1f), 0.1f)
    assertEquals(90f, calculateDonutSweepAngle(50f, 100f, 0.5f), 0.1f)
    assertEquals(360f, calculateDonutSweepAngle(100f, 100f, 1f), 0.1f)
    assertEquals(120f, calculateDonutSweepAngle(33.33f, 100f, 1f), 0.1f)
  }

  @Test
  fun calculateCircularSweepAngle_returnsCorrectValue() {
    assertEquals(0f, calculateCircularSweepAngle(0f), 0.1f)
    assertEquals(180f, calculateCircularSweepAngle(50f), 0.1f)
    assertEquals(360f, calculateCircularSweepAngle(100f), 0.1f)
  }

  @Test
  fun calculateTotalPercentage_returnsCorrectValue() {
    assertEquals(0f, calculateTotalPercentage(emptyList()), 0.1f)
    assertEquals(100f, calculateTotalPercentage(listOf("A" to 60f, "B" to 40f)), 0.1f)
    assertEquals(50f, calculateTotalPercentage(listOf("A" to 50f)), 0.1f)
  }

  @Test
  fun isPointVisible_conditionCoverage() {
    // Condition: pointX <= chartWidth * animatedProgress

    // pointX < threshold (clearly TRUE)
    assertTrue(isPointVisible(10f, 100f, 1f))
    assertTrue(isPointVisible(0f, 100f, 0.5f))

    // pointX == threshold (boundary TRUE)
    assertTrue(isPointVisible(50f, 100f, 0.5f))
    assertTrue(isPointVisible(100f, 100f, 1f))

    // pointX > threshold (FALSE)
    assertFalse(isPointVisible(51f, 100f, 0.5f))
    assertFalse(isPointVisible(75f, 100f, 0.5f))
    assertFalse(isPointVisible(100f, 100f, 0.5f))

    // Edge: zero progress
    assertTrue(isPointVisible(0f, 100f, 0f))
    assertFalse(isPointVisible(1f, 100f, 0f))
  }

  @Test
  fun getColorForIndex_cyclesCorrectly() {
    assertEquals(testColors[0], getColorForIndex(0, testColors))
    assertEquals(testColors[1], getColorForIndex(1, testColors))
    assertEquals(testColors[2], getColorForIndex(2, testColors))
    assertEquals(testColors[0], getColorForIndex(3, testColors)) // Cycles back
    assertEquals(testColors[1], getColorForIndex(4, testColors))
  }

  @Test
  fun percentageFormat_constant() {
    assertEquals("%d%%", PERCENTAGE_FORMAT)
  }

  // ===== UI COMPOSITION TESTS =====

  @Test
  fun barChart_withData() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(
            items = listOf("A" to 50f, "B" to 30f), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()
  }

  @Test
  fun barChart_empty() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(items = emptyList(), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertExists()
  }

  @Test
  fun barChart_zeroProgress() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedHorizontalBarChart(
            items = listOf("A" to 50f), colors = testColors, animationProgress = 0f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_bar_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_withData() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(
            segments = listOf("A" to 60f, "B" to 40f), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_zeroProgress() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(
            segments = listOf("A" to 50f), colors = testColors, animationProgress = 0f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertIsDisplayed()
  }

  @Test
  fun donutChart_empty() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedDonutChart(segments = emptyList(), colors = testColors, animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_donut_chart).assertExists()
  }

  @Test
  fun lineChart_empty() {
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
  fun lineChart_singlePoint() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data = listOf(JoinRateData(Timestamp(Date()), 10, "D1")),
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun lineChart_multiplePoints() {
    composeTestRule.setContent {
      MaterialTheme {
        AnimatedLineChart(
            data =
                listOf(
                    JoinRateData(Timestamp(Date()), 10, "D1"),
                    JoinRateData(Timestamp(Date()), 20, "D2")),
            lineColor = testColors[0],
            fillColor = testColors[1],
            animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_line_chart).assertIsDisplayed()
  }

  @Test
  fun legend_withItems() {
    composeTestRule.setContent {
      MaterialTheme { ChartLegend(items = listOf("A" to testColors[0], "B" to testColors[1])) }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertIsDisplayed()
    composeTestRule.onNodeWithText("A").assertIsDisplayed()
  }

  @Test
  fun legend_empty() {
    composeTestRule.setContent { MaterialTheme { ChartLegend(items = emptyList()) } }
    composeTestRule.onNodeWithTag(C.Tag.stats_chart_legend).assertExists()
  }

  @Test
  fun counter_withProgress() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 100, animationProgress = 1f) }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_animated_counter).assertIsDisplayed()
  }

  @Test
  fun counter_zeroProgress() {
    composeTestRule.setContent {
      MaterialTheme { AnimatedCounter(targetValue = 100, animationProgress = 0f) }
    }
    composeTestRule.onNodeWithText("0").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_normal() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 75f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithTag(C.Tag.stats_circular_indicator).assertIsDisplayed()
  }

  @Test
  fun circularIndicator_clampsOver100() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(
            percentage = 150f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_clampsNegative() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(
            percentage = -10f, color = testColors[0], animationProgress = 1f)
      }
    }
    composeTestRule.onNodeWithText("0%").assertIsDisplayed()
  }

  @Test
  fun circularIndicator_zeroProgress() {
    composeTestRule.setContent {
      MaterialTheme {
        CircularPercentageIndicator(percentage = 50f, color = testColors[0], animationProgress = 0f)
      }
    }
    composeTestRule.onNodeWithText("0%").assertIsDisplayed()
  }
}

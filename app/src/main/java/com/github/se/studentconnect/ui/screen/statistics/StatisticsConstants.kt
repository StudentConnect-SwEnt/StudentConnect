package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Constants for the statistics screen UI components. Centralizes all dimensions, durations, and
 * numeric values to avoid magic numbers.
 */
object StatisticsConstants {
  // Screen layout
  val SCREEN_PADDING = 20.dp
  val CARD_SPACING = 16.dp
  val CONTENT_SPACING = 16.dp
  val MEDIUM_SPACING = 8.dp
  val ERROR_PADDING = 32.dp

  // Card styling
  val CARD_PADDING = 24.dp
  val CARD_CORNER_RADIUS = 24.dp
  val CARD_ELEVATION = 8.dp
  val HERO_CARD_CORNER_RADIUS = 28.dp
  val HERO_CARD_SHADOW_ELEVATION = 16.dp

  // Hero card
  val HERO_ICON_CONTAINER_SIZE = 64.dp
  val HERO_ICON_SIZE = 32.dp

  // Loading indicator
  val LOADING_INDICATOR_SIZE = 48.dp
  val LOADING_INDICATOR_STROKE_WIDTH = 4.dp

  // Chart dimensions
  val DONUT_CHART_SIZE = 140.dp
  val DONUT_STROKE_WIDTH = 32.dp
  val BAR_CHART_HEIGHT = 200.dp
  val BAR_HEIGHT = 24.dp
  val BAR_CORNER_RADIUS = 12.dp
  val BAR_LABEL_SPACING = 4.dp
  val LINE_CHART_HEIGHT = 200.dp

  // Line chart
  val LINE_CHART_PADDING_BOTTOM = 30.dp
  val LINE_CHART_PADDING_TOP = 20.dp
  val LINE_STROKE_WIDTH = 3.dp
  val LINE_POINT_RADIUS = 6.dp
  val LINE_POINT_INNER_RADIUS = 3.dp
  val LINE_LABEL_OFFSET = 8.dp
  val LINE_LABEL_FONT_SIZE = 10.sp

  // Circular indicator
  val CIRCULAR_INDICATOR_SIZE = 100.dp
  val CIRCULAR_INDICATOR_STROKE = 12.dp

  // Legend
  val LEGEND_DOT_SIZE = 12.dp
  val LEGEND_SPACING = 8.dp
  val LEGEND_ITEM_SPACING = 16.dp

  // Animation durations (ms)
  const val CHART_ANIMATION_DURATION = 1000
  const val COUNTER_ANIMATION_DURATION = 1500
  const val CARD_ANIMATION_DURATION_MS = 400
  const val CARD_ANIMATION_DURATION_OFFSET_MS = 100
  const val BAR_ANIMATION_DELAY_PER_ITEM = 50
  const val STAGGER_DELAY_MS = 50L

  // Animation thresholds
  const val STAGGER_THRESHOLD_MULTIPLIER = 0.1f
  const val SLIDE_ANIMATION_OFFSET_DIVISOR = 4

  // Chart limits
  const val MAX_BAR_ITEMS = 6
  const val MAX_DONUT_SEGMENTS = 6
  const val MAX_LEGEND_ITEMS = 5
  const val MAX_BARS_TO_SHOW = 5
  const val LINE_CHART_MAX_LABELS = 5
  const val MIN_POINTS_FOR_LINE = 2
  const val LINE_CHART_MIN_VALUE = 0

  // Chart angles
  const val CHART_START_ANGLE = -90f
  const val FULL_CIRCLE_DEGREES = 360f
  const val PERCENTAGE_MAX = 100f

  // Alpha values
  const val BACKGROUND_ALPHA = 0.15f
  const val GRADIENT_END_ALPHA = 0.7f
  const val FILL_GRADIENT_START_ALPHA = 0.3f
  const val FILL_GRADIENT_END_ALPHA = 0.05f
  const val HERO_ICON_BACKGROUND_ALPHA = 0.2f
  const val HERO_TEXT_SECONDARY_ALPHA = 0.9f
  const val HERO_TEXT_TERTIARY_ALPHA = 0.8f
  const val SHADOW_ALPHA = 0.3f
  const val CARD_SHADOW_ALPHA = 0.1f
}

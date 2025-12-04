package com.github.se.studentconnect.ui.screen.statistics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.JoinRateData
import com.github.se.studentconnect.resources.C

internal const val PERCENTAGE_FORMAT = "%d%%"

/** Calculates chart points from data for line chart. */
internal fun calculateChartPoints(
    data: List<JoinRateData>,
    chartWidth: Float,
    chartHeight: Float,
    paddingTop: Float
): List<Offset> {
  if (data.isEmpty()) return emptyList()

  val maxValue = data.maxOfOrNull { it.cumulativeJoins } ?: 1
  val minValue = StatisticsConstants.LINE_CHART_MIN_VALUE

  return data.mapIndexed { index, point ->
    val x =
        if (data.size > 1) {
          (index.toFloat() / (data.size - 1)) * chartWidth
        } else {
          chartWidth / 2
        }
    val y =
        paddingTop +
            chartHeight *
                (1 -
                    (point.cumulativeJoins - minValue).toFloat() /
                        (maxValue - minValue).coerceAtLeast(1))
    Offset(x, y)
  }
}

/** Creates a fill path for the line chart gradient. */
internal fun createFillPath(points: List<Offset>, bottomY: Float): Path {
  if (points.isEmpty()) return Path()

  return Path().apply {
    moveTo(points.first().x, bottomY)
    points.forEach { lineTo(it.x, it.y) }
    lineTo(points.last().x, bottomY)
    close()
  }
}

/** Creates a line path connecting all points. */
internal fun createLinePath(points: List<Offset>): Path {
  if (points.isEmpty()) return Path()

  return Path().apply {
    moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
      lineTo(points[i].x, points[i].y)
    }
  }
}

/** Determines if a label should be shown at the given index. */
internal fun shouldShowLabelAtIndex(index: Int, dataSize: Int): Boolean {
  return index == 0 ||
      index == dataSize - 1 ||
      dataSize <= StatisticsConstants.LINE_CHART_MAX_LABELS
}

/** Calculates the radius for a circular chart given size and stroke width. */
internal fun calculateChartRadius(minDimension: Float, strokeWidth: Float): Float {
  return (minDimension - strokeWidth) / 2
}

/** Calculates the center point for a chart. */
internal fun calculateChartCenter(width: Float, height: Float): Offset {
  return Offset(width / 2, height / 2)
}

/** Calculates sweep angle for a donut chart segment. */
internal fun calculateDonutSweepAngle(
    percentage: Float,
    totalPercentage: Float,
    sweepMultiplier: Float
): Float {
  if (totalPercentage == 0f) return 0f
  return (percentage / totalPercentage) * StatisticsConstants.FULL_CIRCLE_DEGREES * sweepMultiplier
}

/** Calculates sweep angle for circular percentage indicator. */
internal fun calculateCircularSweepAngle(percentage: Float): Float {
  return (percentage / StatisticsConstants.PERCENTAGE_MAX) * StatisticsConstants.FULL_CIRCLE_DEGREES
}

/** Calculates total percentage from segments. */
internal fun calculateTotalPercentage(segments: List<Pair<String, Float>>): Float {
  return segments.sumOf { it.second.toDouble() }.toFloat()
}

/** Determines if a point should be visible based on animation progress. */
internal fun isPointVisible(pointX: Float, chartWidth: Float, animatedProgress: Float): Boolean {
  return pointX <= chartWidth * animatedProgress
}

/** Gets color for an index with cycling. */
internal fun getColorForIndex(index: Int, colors: List<Color>): Color {
  return colors[index % colors.size]
}

/**
 * Animated horizontal bar chart for displaying distribution data.
 *
 * @param items List of pairs (label, value) to display.
 * @param colors List of colors for each bar.
 * @param maxItems Maximum number of items to display.
 * @param animationProgress Progress of the animation (0f to 1f).
 * @param modifier Modifier for the chart container.
 */
@Composable
fun AnimatedHorizontalBarChart(
    items: List<Pair<String, Float>>,
    colors: List<Color>,
    maxItems: Int = StatisticsConstants.MAX_BAR_ITEMS,
    animationProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
  val displayItems = items.take(maxItems)
  val maxValue = displayItems.maxOfOrNull { it.second } ?: 1f
  val chartDescription = stringResource(R.string.content_description_stats_chart)

  Column(
      modifier =
          modifier.fillMaxWidth().testTag(C.Tag.stats_bar_chart).semantics {
            contentDescription = chartDescription
          },
      verticalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_SPACING)) {
        displayItems.forEachIndexed { index, (label, value) ->
          val color = colors[index % colors.size]
          val animatedWidth by
              animateFloatAsState(
                  targetValue = if (animationProgress > 0f) (value / maxValue) else 0f,
                  animationSpec =
                      tween(
                          durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
                          delayMillis = index * StatisticsConstants.BAR_ANIMATION_DELAY_PER_ITEM,
                          easing = FastOutSlowInEasing),
                  label = "bar_width_$index")

          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                      text = label,
                      style = MaterialTheme.typography.bodySmall,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f))
                  Text(
                      text = String.format(PERCENTAGE_FORMAT, value.toInt()),
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                      color = color)
                }

            Spacer(modifier = Modifier.height(StatisticsConstants.BAR_LABEL_SPACING))

            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(StatisticsConstants.BAR_HEIGHT)
                        .clip(RoundedCornerShape(StatisticsConstants.BAR_CORNER_RADIUS))
                        .background(color.copy(alpha = StatisticsConstants.BACKGROUND_ALPHA))) {
                  Box(
                      modifier =
                          Modifier.fillMaxWidth(animatedWidth)
                              .height(StatisticsConstants.BAR_HEIGHT)
                              .clip(RoundedCornerShape(StatisticsConstants.BAR_CORNER_RADIUS))
                              .background(
                                  Brush.horizontalGradient(
                                      colors =
                                          listOf(
                                              color,
                                              color.copy(
                                                  alpha =
                                                      StatisticsConstants.GRADIENT_END_ALPHA)))))
                }
          }
        }
      }
}

/**
 * Animated donut chart for displaying distribution data.
 *
 * @param segments List of pairs (label, percentage) for each segment.
 * @param colors List of colors for each segment.
 * @param animationProgress Progress of the animation (0f to 1f).
 * @param modifier Modifier for the chart container.
 */
@Composable
fun AnimatedDonutChart(
    segments: List<Pair<String, Float>>,
    colors: List<Color>,
    animationProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
  val animatedSweep = remember { Animatable(0f) }
  val chartDescription = stringResource(R.string.content_description_stats_chart)

  LaunchedEffect(animationProgress) {
    if (animationProgress > 0f) {
      animatedSweep.animateTo(
          targetValue = 1f,
          animationSpec =
              tween(
                  durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
                  easing = FastOutSlowInEasing))
    }
  }

  Box(
      modifier =
          modifier.size(StatisticsConstants.DONUT_CHART_SIZE).testTag(C.Tag.stats_donut_chart),
      contentAlignment = Alignment.Center) {
        Canvas(
            modifier =
                Modifier.size(StatisticsConstants.DONUT_CHART_SIZE).semantics {
                  contentDescription = chartDescription
                }) {
              val strokeWidth = StatisticsConstants.DONUT_STROKE_WIDTH.toPx()
              val radius = calculateChartRadius(size.minDimension, strokeWidth)
              val center = calculateChartCenter(size.width, size.height)

              var startAngle = StatisticsConstants.CHART_START_ANGLE
              val totalPercentage = calculateTotalPercentage(segments)
              val sweepMultiplier = animatedSweep.value

              segments.forEachIndexed { index, (_, percentage) ->
                val sweepAngle =
                    calculateDonutSweepAngle(percentage, totalPercentage, sweepMultiplier)
                val segmentColor = getColorForIndex(index, colors)

                drawArc(
                    color = segmentColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

                startAngle += sweepAngle
              }
            }
      }
}

/**
 * Animated line chart for displaying temporal data.
 *
 * @param data List of data points with timestamps.
 * @param lineColor Color of the line.
 * @param fillColor Color for the gradient fill under the line.
 * @param animationProgress Progress of the animation (0f to 1f).
 * @param modifier Modifier for the chart container.
 */
@Composable
fun AnimatedLineChart(
    data: List<JoinRateData>,
    lineColor: Color,
    fillColor: Color,
    animationProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()
  val chartDescription = stringResource(R.string.content_description_stats_chart)
  val pointInnerColor = MaterialTheme.colorScheme.surface
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

  val animatedProgress by
      animateFloatAsState(
          targetValue = animationProgress,
          animationSpec =
              tween(
                  durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
                  easing = FastOutSlowInEasing),
          label = "line_progress")

  if (data.isEmpty()) return

  Canvas(
      modifier =
          modifier
              .fillMaxWidth()
              .height(StatisticsConstants.LINE_CHART_HEIGHT)
              .testTag(C.Tag.stats_line_chart)
              .semantics { contentDescription = chartDescription }) {
        val paddingBottom = StatisticsConstants.LINE_CHART_PADDING_BOTTOM.toPx()
        val paddingTop = StatisticsConstants.LINE_CHART_PADDING_TOP.toPx()
        val chartHeight = size.height - paddingBottom - paddingTop
        val chartWidth = size.width

        val points = calculateChartPoints(data, chartWidth, chartHeight, paddingTop)

        // Draw gradient fill
        if (points.size >= StatisticsConstants.MIN_POINTS_FOR_LINE) {
          val fillPath = createFillPath(points, size.height - paddingBottom)
          val fillGradient =
              Brush.verticalGradient(
                  colors =
                      listOf(
                          fillColor.copy(alpha = StatisticsConstants.FILL_GRADIENT_START_ALPHA),
                          fillColor.copy(alpha = StatisticsConstants.FILL_GRADIENT_END_ALPHA)))

          clipRect(right = chartWidth * animatedProgress) {
            drawPath(path = fillPath, brush = fillGradient)
          }
        }

        // Draw line
        if (points.size >= StatisticsConstants.MIN_POINTS_FOR_LINE) {
          val linePath = createLinePath(points)
          val lineStyle =
              Stroke(width = StatisticsConstants.LINE_STROKE_WIDTH.toPx(), cap = StrokeCap.Round)

          clipRect(right = chartWidth * animatedProgress) {
            drawPath(path = linePath, color = lineColor, style = lineStyle)
          }
        }

        // Draw points
        points.forEach { point ->
          if (isPointVisible(point.x, chartWidth, animatedProgress)) {
            drawCircle(
                color = lineColor,
                radius = StatisticsConstants.LINE_POINT_RADIUS.toPx(),
                center = point)
            drawCircle(
                color = pointInnerColor,
                radius = StatisticsConstants.LINE_POINT_INNER_RADIUS.toPx(),
                center = point)
          }
        }

        // Draw labels
        val labelStyle =
            TextStyle(fontSize = StatisticsConstants.LINE_LABEL_FONT_SIZE, color = labelColor)
        data.forEachIndexed { index, dataPoint ->
          if (shouldShowLabelAtIndex(index, data.size)) {
            val textLayout = textMeasurer.measure(dataPoint.label, labelStyle)
            val x = points[index].x - textLayout.size.width / 2
            val y = size.height - paddingBottom + StatisticsConstants.LINE_LABEL_OFFSET.toPx()
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(x.coerceIn(0f, chartWidth - textLayout.size.width), y))
          }
        }
      }
}

/**
 * Legend component for charts.
 *
 * @param items List of pairs (label, color) to display in the legend.
 * @param modifier Modifier for the legend container.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartLegend(items: List<Pair<String, Color>>, modifier: Modifier = Modifier) {
  FlowRow(
      modifier = modifier.fillMaxWidth().testTag(C.Tag.stats_chart_legend),
      horizontalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_ITEM_SPACING),
      verticalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_SPACING)) {
        items.forEach { (label, color) ->
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_SPACING)) {
                Box(
                    modifier =
                        Modifier.size(StatisticsConstants.LEGEND_DOT_SIZE)
                            .clip(CircleShape)
                            .background(color))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }
        }
      }
}

/**
 * Animated counter that counts up from 0 to the target value.
 *
 * @param targetValue The final value to count to.
 * @param animationProgress Progress of the animation (0f to 1f).
 * @param style Text style for the counter.
 * @param modifier Modifier for the text.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    animationProgress: Float = 1f,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    modifier: Modifier = Modifier
) {
  val animatedValue by
      animateFloatAsState(
          targetValue = if (animationProgress > 0f) targetValue.toFloat() else 0f,
          animationSpec =
              tween(
                  durationMillis = StatisticsConstants.COUNTER_ANIMATION_DURATION,
                  easing = FastOutSlowInEasing),
          label = "counter")

  Text(
      text = animatedValue.toInt().toString(),
      style = style,
      modifier = modifier.testTag(C.Tag.stats_animated_counter))
}

/**
 * Circular percentage indicator with animated fill.
 *
 * @param percentage The percentage to display (0-100).
 * @param color The color of the progress arc.
 * @param animationProgress Progress of the animation (0f to 1f).
 * @param modifier Modifier for the indicator.
 */
@Composable
fun CircularPercentageIndicator(
    percentage: Float,
    color: Color,
    animationProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
  val chartDescription = stringResource(R.string.content_description_stats_chart)

  val animatedPercentage by
      animateFloatAsState(
          targetValue =
              if (animationProgress > 0f) {
                percentage.coerceIn(0f, StatisticsConstants.PERCENTAGE_MAX)
              } else 0f,
          animationSpec =
              tween(
                  durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
                  easing = FastOutSlowInEasing),
          label = "circular_percentage")

  Box(
      modifier =
          modifier
              .size(StatisticsConstants.CIRCULAR_INDICATOR_SIZE)
              .testTag(C.Tag.stats_circular_indicator),
      contentAlignment = Alignment.Center) {
        Canvas(
            modifier =
                Modifier.size(StatisticsConstants.CIRCULAR_INDICATOR_SIZE).semantics {
                  contentDescription = chartDescription
                }) {
              val strokeWidth = StatisticsConstants.CIRCULAR_INDICATOR_STROKE.toPx()
              val radius = calculateChartRadius(size.minDimension, strokeWidth)
              val center = calculateChartCenter(size.width, size.height)

              // Background circle
              drawCircle(
                  color = color.copy(alpha = StatisticsConstants.BACKGROUND_ALPHA),
                  radius = radius,
                  center = center,
                  style = Stroke(width = strokeWidth))

              // Progress arc
              val sweepAngle = calculateCircularSweepAngle(animatedPercentage)
              drawArc(
                  color = color,
                  startAngle = StatisticsConstants.CHART_START_ANGLE,
                  sweepAngle = sweepAngle,
                  useCenter = false,
                  topLeft = Offset(center.x - radius, center.y - radius),
                  size = Size(radius * 2, radius * 2),
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }

        // Percentage text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = String.format(PERCENTAGE_FORMAT, animatedPercentage.toInt()),
              style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
              color = color)
        }
      }
}

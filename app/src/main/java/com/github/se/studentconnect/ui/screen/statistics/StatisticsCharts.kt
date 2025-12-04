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
import androidx.compose.foundation.layout.padding
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

private const val PERCENTAGE_FORMAT = "%d%%"

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
        modifier = modifier
            .fillMaxWidth()
            .testTag(C.Tag.stats_bar_chart)
            .semantics { contentDescription = chartDescription },
        verticalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_SPACING)
    ) {
        displayItems.forEachIndexed { index, (label, value) ->
            val color = colors[index % colors.size]
            val animatedWidth by animateFloatAsState(
                targetValue = if (animationProgress > 0f) (value / maxValue) else 0f,
                animationSpec = tween(
                    durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
                    delayMillis = index * StatisticsConstants.BAR_ANIMATION_DELAY_PER_ITEM,
                    easing = FastOutSlowInEasing
                ),
                label = "bar_width_$index"
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format(PERCENTAGE_FORMAT, value.toInt()),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(StatisticsConstants.BAR_LABEL_SPACING))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StatisticsConstants.BAR_HEIGHT)
                        .clip(RoundedCornerShape(StatisticsConstants.BAR_CORNER_RADIUS))
                        .background(color.copy(alpha = StatisticsConstants.BACKGROUND_ALPHA))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedWidth)
                            .height(StatisticsConstants.BAR_HEIGHT)
                            .clip(RoundedCornerShape(StatisticsConstants.BAR_CORNER_RADIUS))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        color,
                                        color.copy(alpha = StatisticsConstants.GRADIENT_END_ALPHA)
                                    )
                                )
                            )
                    )
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
                animationSpec = tween(
                    durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Box(
        modifier = modifier
            .size(StatisticsConstants.DONUT_CHART_SIZE)
            .testTag(C.Tag.stats_donut_chart),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(StatisticsConstants.DONUT_CHART_SIZE)
                .semantics { contentDescription = chartDescription }
        ) {
            val strokeWidth = StatisticsConstants.DONUT_STROKE_WIDTH.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            var startAngle = StatisticsConstants.CHART_START_ANGLE
            val totalPercentage = segments.sumOf { it.second.toDouble() }.toFloat()
            val sweepMultiplier = animatedSweep.value

            segments.forEachIndexed { index, (_, percentage) ->
                val sweepAngle = (percentage / totalPercentage) *
                    StatisticsConstants.FULL_CIRCLE_DEGREES * sweepMultiplier
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

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

    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(
            durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "line_progress"
    )

    if (data.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(StatisticsConstants.LINE_CHART_HEIGHT)
            .testTag(C.Tag.stats_line_chart)
            .semantics { contentDescription = chartDescription }
    ) {
        val paddingBottom = StatisticsConstants.LINE_CHART_PADDING_BOTTOM.toPx()
        val paddingTop = StatisticsConstants.LINE_CHART_PADDING_TOP.toPx()
        val chartHeight = size.height - paddingBottom - paddingTop
        val chartWidth = size.width

        val maxValue = data.maxOfOrNull { it.cumulativeJoins } ?: 1
        val minValue = 0

        val points = data.mapIndexed { index, point ->
            val x = if (data.size > 1) {
                (index.toFloat() / (data.size - 1)) * chartWidth
            } else {
                chartWidth / 2
            }
            val y = paddingTop + chartHeight *
                (1 - (point.cumulativeJoins - minValue).toFloat() / (maxValue - minValue).coerceAtLeast(1))
            Offset(x, y)
        }

        // Draw gradient fill
        if (points.size >= StatisticsConstants.MIN_POINTS_FOR_LINE) {
            val fillPath = Path().apply {
                moveTo(points.first().x, size.height - paddingBottom)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, size.height - paddingBottom)
                close()
            }

            clipRect(right = chartWidth * animatedProgress) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            fillColor.copy(alpha = StatisticsConstants.FILL_GRADIENT_START_ALPHA),
                            fillColor.copy(alpha = StatisticsConstants.FILL_GRADIENT_END_ALPHA)
                        )
                    )
                )
            }
        }

        // Draw line
        if (points.size >= StatisticsConstants.MIN_POINTS_FOR_LINE) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            clipRect(right = chartWidth * animatedProgress) {
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = StatisticsConstants.LINE_STROKE_WIDTH.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Draw points
        points.forEachIndexed { index, point ->
            if (point.x <= chartWidth * animatedProgress) {
                drawCircle(
                    color = lineColor,
                    radius = StatisticsConstants.LINE_POINT_RADIUS.toPx(),
                    center = point
                )
                drawCircle(
                    color = pointInnerColor,
                    radius = StatisticsConstants.LINE_POINT_INNER_RADIUS.toPx(),
                    center = point
                )
            }
        }

        // Draw labels
        val labelStyle = TextStyle(
            fontSize = StatisticsConstants.LINE_LABEL_FONT_SIZE,
            color = labelColor
        )

        data.forEachIndexed { index, point ->
            if (index == 0 || index == data.size - 1 || data.size <= StatisticsConstants.LINE_CHART_MAX_LABELS) {
                val textLayout = textMeasurer.measure(point.label, labelStyle)
                val x = points[index].x - textLayout.size.width / 2
                val y = size.height - paddingBottom + StatisticsConstants.LINE_LABEL_OFFSET.toPx()

                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(x.coerceIn(0f, chartWidth - textLayout.size.width), y)
                )
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
fun ChartLegend(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag(C.Tag.stats_chart_legend),
        horizontalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_ITEM_SPACING),
        verticalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_SPACING)
    ) {
        items.forEach { (label, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StatisticsConstants.LEGEND_SPACING)
            ) {
                Box(
                    modifier = Modifier
                        .size(StatisticsConstants.LEGEND_DOT_SIZE)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
    val animatedValue by animateFloatAsState(
        targetValue = if (animationProgress > 0f) targetValue.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = StatisticsConstants.COUNTER_ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "counter"
    )

    Text(
        text = animatedValue.toInt().toString(),
        style = style,
        modifier = modifier.testTag(C.Tag.stats_animated_counter)
    )
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

    val animatedPercentage by animateFloatAsState(
        targetValue = if (animationProgress > 0f) {
            percentage.coerceIn(0f, StatisticsConstants.PERCENTAGE_MAX)
        } else 0f,
        animationSpec = tween(
            durationMillis = StatisticsConstants.CHART_ANIMATION_DURATION,
            easing = FastOutSlowInEasing
        ),
        label = "circular_percentage"
    )

    Box(
        modifier = modifier
            .size(StatisticsConstants.CIRCULAR_INDICATOR_SIZE)
            .testTag(C.Tag.stats_circular_indicator),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(StatisticsConstants.CIRCULAR_INDICATOR_SIZE)
                .semantics { contentDescription = chartDescription }
        ) {
            val strokeWidth = StatisticsConstants.CIRCULAR_INDICATOR_STROKE.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background circle
            drawCircle(
                color = color.copy(alpha = StatisticsConstants.BACKGROUND_ALPHA),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            val sweepAngle = (animatedPercentage / StatisticsConstants.PERCENTAGE_MAX) *
                StatisticsConstants.FULL_CIRCLE_DEGREES
            drawArc(
                color = color,
                startAngle = StatisticsConstants.CHART_START_ANGLE,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Percentage text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(PERCENTAGE_FORMAT, animatedPercentage.toInt()),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

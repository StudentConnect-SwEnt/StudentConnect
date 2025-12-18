package com.github.se.studentconnect.ui.screen.statistics

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.AgeGroupData
import com.github.se.studentconnect.model.event.CampusData
import com.github.se.studentconnect.model.event.EventStatistics
import com.github.se.studentconnect.model.event.JoinRateData
import com.github.se.studentconnect.resources.C
import kotlinx.coroutines.delay

/**
 * Event Statistics Screen displaying comprehensive metrics with modern, animated visuals.
 *
 * @param eventUid The unique identifier of the event.
 * @param navController Navigation controller for back navigation.
 * @param viewModel ViewModel for statistics data and state management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventStatisticsScreen(
    eventUid: String,
    navController: NavHostController,
    viewModel: EventStatisticsViewModel = run {
      val context = LocalContext.current
      viewModel(
          factory =
              object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                  if (modelClass.isAssignableFrom(EventStatisticsViewModel::class.java)) {
                    return EventStatisticsViewModel(getString = { id -> context.getString(id) })
                        as T
                  }
                  throw IllegalArgumentException("Unknown ViewModel class")
                }
              })
    }
) {
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(eventUid) { viewModel.loadStatistics(eventUid) }

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag(C.Tag.STATS_SCREEN),
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = stringResource(R.string.stats_screen_title),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier = Modifier.testTag(C.Tag.STATS_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back))
                  }
            },
            actions = {
              IconButton(
                  onClick = { viewModel.refresh() },
                  modifier = Modifier.testTag(C.Tag.STATS_REFRESH_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription =
                            stringResource(R.string.content_description_refresh_stats))
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.testTag(C.Tag.STATS_TOP_BAR))
      }) { paddingValues ->
        when {
          uiState.isLoading -> {
            LoadingState(modifier = Modifier.fillMaxSize().padding(paddingValues))
          }
          uiState.error != null -> {
            ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize().padding(paddingValues))
          }
          uiState.statistics != null -> {
            StatisticsContent(
                statistics = uiState.statistics!!,
                animationProgress = uiState.animationProgress,
                paddingValues = paddingValues)
          }
        }
      }
}

/** Loading state with elegant progress indicator. */
@Composable
internal fun LoadingState(modifier: Modifier = Modifier) {
  Box(modifier = modifier.testTag(C.Tag.STATS_LOADING), contentAlignment = Alignment.Center) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StatisticsConstants.CONTENT_SPACING)) {
          CircularProgressIndicator(
              modifier = Modifier.size(StatisticsConstants.LOADING_INDICATOR_SIZE),
              strokeWidth = StatisticsConstants.LOADING_INDICATOR_STROKE_WIDTH,
              color = MaterialTheme.colorScheme.primary)
          Text(
              text = stringResource(R.string.stats_loading),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
  }
}

/** Error state with retry option. */
@Composable
internal fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  Box(modifier = modifier.testTag(C.Tag.STATS_ERROR), contentAlignment = Alignment.Center) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StatisticsConstants.CONTENT_SPACING),
        modifier = Modifier.padding(StatisticsConstants.ERROR_PADDING)) {
          Text(
              text = stringResource(R.string.stats_error_loading),
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.error,
              textAlign = TextAlign.Center)
          Text(
              text = message,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center)
          Button(onClick = onRetry, modifier = Modifier.testTag(C.Tag.STATS_RETRY_BUTTON)) {
            Text(stringResource(R.string.stats_retry))
          }
        }
  }
}

/** Main statistics content with animated cards. */
@Composable
internal fun StatisticsContent(
    statistics: EventStatistics,
    animationProgress: Float,
    paddingValues: PaddingValues
) {
  // Get colors from resources
  val gradientStart = colorResource(R.color.stats_gradient_start)
  val gradientEnd = colorResource(R.color.stats_gradient_end)
  val chartColors =
      listOf(
          colorResource(R.color.stats_chart_1),
          colorResource(R.color.stats_chart_2),
          colorResource(R.color.stats_chart_3),
          colorResource(R.color.stats_chart_4),
          colorResource(R.color.stats_chart_5),
          colorResource(R.color.stats_chart_6))
  val successColor = colorResource(R.color.stats_success)

  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(paddingValues).testTag(C.Tag.STATS_CONTENT),
      contentPadding = PaddingValues(StatisticsConstants.SCREEN_PADDING),
      verticalArrangement = Arrangement.spacedBy(StatisticsConstants.CARD_SPACING)) {
        // Hero card - Total Attendees
        item {
          StaggeredAnimatedCard(index = 0, animationProgress = animationProgress) {
            TotalAttendeesCard(
                totalAttendees = statistics.totalAttendees,
                animationProgress = animationProgress,
                gradientStart = gradientStart,
                gradientEnd = gradientEnd)
          }
        }

        // Attendees/Followers Rate Card
        item {
          StaggeredAnimatedCard(index = 1, animationProgress = animationProgress) {
            AttendeesFollowersCard(
                attendees = statistics.totalAttendees,
                followers = statistics.followerCount,
                rate = statistics.attendeesFollowersRate,
                animationProgress = animationProgress,
                accentColor = successColor)
          }
        }

        // Age Distribution Card
        if (statistics.ageDistribution.isNotEmpty()) {
          item {
            StaggeredAnimatedCard(index = 2, animationProgress = animationProgress) {
              AgeDistributionCard(
                  data = statistics.ageDistribution,
                  animationProgress = animationProgress,
                  colors = chartColors)
            }
          }
        }

        // Campus Distribution Card
        if (statistics.campusDistribution.isNotEmpty()) {
          item {
            StaggeredAnimatedCard(index = 3, animationProgress = animationProgress) {
              CampusDistributionCard(
                  data = statistics.campusDistribution,
                  animationProgress = animationProgress,
                  colors = chartColors)
            }
          }
        }

        // Join Rate Over Time Card
        if (statistics.joinRateOverTime.isNotEmpty()) {
          item {
            StaggeredAnimatedCard(index = 4, animationProgress = animationProgress) {
              JoinRateCard(
                  data = statistics.joinRateOverTime,
                  animationProgress = animationProgress,
                  lineColor = gradientStart,
                  fillColor = gradientEnd)
            }
          }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(StatisticsConstants.CONTENT_SPACING)) }
      }
}

/** Wrapper for staggered card entrance animations. */
@Composable
private fun StaggeredAnimatedCard(
    index: Int,
    animationProgress: Float,
    content: @Composable () -> Unit
) {
  val staggerThreshold = index * StatisticsConstants.STAGGER_THRESHOLD_MULTIPLIER
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(animationProgress) {
    if (animationProgress > staggerThreshold && !visible) {
      delay(index * StatisticsConstants.STAGGER_DELAY_MS)
      visible = true
    }
  }

  AnimatedVisibility(
      visible = visible || animationProgress >= 1f,
      enter =
          fadeIn(
              animationSpec =
                  tween(
                      StatisticsConstants.CARD_ANIMATION_DURATION_MS,
                      easing = FastOutSlowInEasing)) +
              slideInVertically(
                  initialOffsetY = { it / StatisticsConstants.SLIDE_ANIMATION_OFFSET_DIVISOR },
                  animationSpec =
                      tween(
                          StatisticsConstants.CARD_ANIMATION_DURATION_MS +
                              StatisticsConstants.CARD_ANIMATION_DURATION_OFFSET_MS,
                          easing = FastOutSlowInEasing))) {
        content()
      }
}

/** Hero card displaying total attendees with animated counter. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun TotalAttendeesCard(
    totalAttendees: Int,
    animationProgress: Float,
    gradientStart: Color,
    gradientEnd: Color
) {
  val gradientColors = listOf(gradientStart, gradientEnd)
  val cardDescription =
      stringResource(R.string.stats_total_attendees) +
          String.format(stringResource(R.string.stats_label_separator), totalAttendees)

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .shadow(
                  elevation = StatisticsConstants.HERO_CARD_SHADOW_ELEVATION,
                  shape = RoundedCornerShape(StatisticsConstants.HERO_CARD_CORNER_RADIUS),
                  spotColor = gradientStart.copy(alpha = StatisticsConstants.SHADOW_ALPHA))
              .testTag(C.Tag.STATS_TOTAL_ATTENDEES_CARD)
              .semantics { contentDescription = cardDescription },
      shape = RoundedCornerShape(StatisticsConstants.HERO_CARD_CORNER_RADIUS),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(brush = Brush.linearGradient(colors = gradientColors))
                    .padding(StatisticsConstants.CARD_PADDING)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Column {
                      Text(
                          text = stringResource(R.string.stats_total_attendees),
                          style = MaterialTheme.typography.titleMedium,
                          color =
                              Color.White.copy(
                                  alpha = StatisticsConstants.HERO_TEXT_SECONDARY_ALPHA))
                      Spacer(modifier = Modifier.height(StatisticsConstants.MEDIUM_SPACING))
                      AnimatedCounter(
                          targetValue = (totalAttendees * animationProgress).toInt(),
                          style =
                              MaterialTheme.typography.displayLarge.copy(
                                  fontWeight = FontWeight.Bold, color = Color.White))
                      Text(
                          text =
                              if (totalAttendees == 1) stringResource(R.string.stats_person)
                              else stringResource(R.string.stats_people),
                          style = MaterialTheme.typography.bodyMedium,
                          color =
                              Color.White.copy(
                                  alpha = StatisticsConstants.HERO_TEXT_TERTIARY_ALPHA))
                    }
                    Box(
                        modifier =
                            Modifier.size(StatisticsConstants.HERO_ICON_CONTAINER_SIZE)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(
                                        alpha = StatisticsConstants.HERO_ICON_BACKGROUND_ALPHA)),
                        contentAlignment = Alignment.Center) {
                          Icon(
                              imageVector = Icons.Default.Groups,
                              contentDescription = null,
                              tint = Color.White,
                              modifier = Modifier.size(StatisticsConstants.HERO_ICON_SIZE))
                        }
                  }
            }
      }
}

/** Card showing attendees/followers rate with circular indicator. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun AttendeesFollowersCard(
    attendees: Int,
    followers: Int,
    rate: Float,
    animationProgress: Float,
    accentColor: Color
) {
  StatisticsCard(
      title = stringResource(R.string.stats_attendees_followers),
      testTag = C.Tag.STATS_FOLLOWERS_CARD) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Column(
                  verticalArrangement = Arrangement.spacedBy(StatisticsConstants.MEDIUM_SPACING)) {
                    MetricRow(
                        label = stringResource(R.string.stats_attendees),
                        value = attendees.toString())
                    MetricRow(
                        label = stringResource(R.string.stats_followers),
                        value = followers.toString())
                    Text(
                        text =
                            "${String.format(stringResource(R.string.stats_percentage_format), rate)}${stringResource(R.string.stats_percentage_symbol)} ${stringResource(R.string.stats_of_followers)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
              CircularPercentageIndicator(
                  percentage = rate.coerceIn(0f, StatisticsConstants.PERCENTAGE_MAX),
                  color = accentColor,
                  animationProgress = animationProgress)
            }
      }
}

/** Card displaying age distribution with donut chart. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun AgeDistributionCard(data: List<AgeGroupData>, animationProgress: Float, colors: List<Color>) {
  StatisticsCard(
      title = stringResource(R.string.stats_age_distribution), testTag = C.Tag.STATS_AGE_CARD) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
              AnimatedDonutChart(
                  segments = data.map { it.ageRange to it.percentage },
                  colors = colors,
                  animationProgress = animationProgress,
                  modifier = Modifier.size(StatisticsConstants.DONUT_CHART_SIZE))
              ChartLegend(
                  items =
                      data.take(StatisticsConstants.MAX_LEGEND_ITEMS).mapIndexed { index, item ->
                        String.format(
                            stringResource(R.string.stats_legend_format),
                            item.ageRange,
                            item.count) to colors[index % colors.size]
                      },
                  modifier =
                      Modifier.weight(1f).padding(start = StatisticsConstants.CONTENT_SPACING))
            }
      }
}

/** Card displaying campus distribution with horizontal bars. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun CampusDistributionCard(data: List<CampusData>, animationProgress: Float, colors: List<Color>) {
  StatisticsCard(
      title = stringResource(R.string.stats_campus_distribution),
      testTag = C.Tag.STATS_CAMPUS_CARD) {
        AnimatedHorizontalBarChart(
            items =
                data.take(StatisticsConstants.MAX_BARS_TO_SHOW).map {
                  it.campusName to it.percentage
                },
            colors = colors,
            animationProgress = animationProgress)
      }
}

/** Card displaying registration timeline with line chart. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun JoinRateCard(
    data: List<JoinRateData>,
    animationProgress: Float,
    lineColor: Color,
    fillColor: Color
) {
  StatisticsCard(
      title = stringResource(R.string.stats_join_rate), testTag = C.Tag.STATS_TIMELINE_CARD) {
        Column(verticalArrangement = Arrangement.spacedBy(StatisticsConstants.CONTENT_SPACING)) {
          val totalJoins = data.lastOrNull()?.cumulativeJoins ?: 0
          Text(
              text = "$totalJoins ${stringResource(R.string.stats_registrations)}",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.primary)
          AnimatedLineChart(
              data = data,
              lineColor = lineColor,
              fillColor = fillColor,
              animationProgress = animationProgress,
              modifier = Modifier.fillMaxWidth())
        }
      }
}

/** Reusable statistics card container with modern styling. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun StatisticsCard(title: String, testTag: String, content: @Composable () -> Unit) {
  val cardDescription = stringResource(R.string.content_description_stats_card)

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .shadow(
                  elevation = StatisticsConstants.CARD_ELEVATION,
                  shape = RoundedCornerShape(StatisticsConstants.CARD_CORNER_RADIUS),
                  spotColor =
                      MaterialTheme.colorScheme.primary.copy(
                          alpha = StatisticsConstants.CARD_SHADOW_ALPHA))
              .testTag(testTag)
              .semantics { contentDescription = cardDescription },
      shape = RoundedCornerShape(StatisticsConstants.CARD_CORNER_RADIUS),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(StatisticsConstants.CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(StatisticsConstants.CONTENT_SPACING)) {
              Text(
                  text = title,
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface)
              content()
            }
      }
}

/** Simple metric row with label and value. */
@Composable
private fun MetricRow(label: String, value: String) {
  Row(
      horizontalArrangement = Arrangement.spacedBy(StatisticsConstants.MEDIUM_SPACING),
      verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

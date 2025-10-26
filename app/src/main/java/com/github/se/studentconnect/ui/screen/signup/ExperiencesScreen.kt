package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.resources.Variables
import com.github.se.studentconnect.ui.theme.AppTheme

@Composable
fun ExperiencesScreen(
    selectedFilter: String,
    selectedTopics: Set<String>,
    onFilterSelected: (String) -> Unit,
    onTopicToggle: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    isSaving: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
  ExperiencesContent(
      selectedFilter = selectedFilter,
      selectedTopics = selectedTopics,
      onFilterSelected = onFilterSelected,
      onTopicToggle = onTopicToggle,
      onBackClick = onBackClick,
      onStartClick = onStartClick,
      isSaving = isSaving,
      errorMessage = errorMessage,
      modifier = modifier.fillMaxWidth())
}

@OptIn(ExperimentalLayoutApi::class)
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun ExperiencesContent(
    selectedFilter: String,
    selectedTopics: Set<String>,
    onFilterSelected: (String) -> Unit,
    onTopicToggle: (String) -> Unit,
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    isSaving: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val topics = experienceTopics[selectedFilter] ?: emptyList()

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Box(
        modifier =
            Modifier.fillMaxSize().semantics { testTag = C.Tag.experiences_screen_container },
        contentAlignment = Alignment.Center) {
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .verticalScroll(scrollState)
                      .padding(top = 32.dp, bottom = 120.dp)
                      .padding(horizontal = 24.dp)
                      .align(Alignment.TopCenter),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth().semantics { testTag = C.Tag.experiences_top_bar },
                    horizontalAlignment = Alignment.Start) {
                      IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface)
                      }

                      Spacer(modifier = Modifier.height(16.dp))

                      Text(
                          text = "For an experience beyond Expectations",
                          style =
                              MaterialTheme.typography.headlineMedium.copy(
                                  fontWeight = FontWeight.Medium,
                                  fontSize = 32.sp,
                                  color = MaterialTheme.colorScheme.onSurface),
                          textAlign = TextAlign.Center,
                          modifier =
                              Modifier.fillMaxWidth().semantics {
                                testTag = C.Tag.experiences_title
                              })

                      Spacer(modifier = Modifier.height(8.dp))

                      Text(
                          text = "Discover what excites you",
                          style =
                              MaterialTheme.typography.bodyMedium.copy(
                                  color = MaterialTheme.colorScheme.onSurface),
                          textAlign = TextAlign.Center,
                          modifier =
                              Modifier.fillMaxWidth().semantics {
                                testTag = C.Tag.experiences_subtitle
                              })
                    }

                Spacer(modifier = Modifier.height(24.dp))

                LazyRow(
                    modifier =
                        Modifier.fillMaxWidth().semantics {
                          testTag = C.Tag.experiences_filter_list
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)) {
                      items(filterOptions) { filter ->
                        val isSelected = filter == selectedFilter
                        ExperienceFilterChip(
                            label = filter,
                            selected = isSelected,
                            onClick = { onFilterSelected(filter) })
                      }
                    }

                Spacer(modifier = Modifier.height(70.dp))

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .semantics { testTag = C.Tag.experiences_topic_grid }
                            .align(Alignment.CenterHorizontally)) {
                      val columns = 3
                      val spacing = 16.dp
                      FlowRow(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement =
                              Arrangement.spacedBy(
                                  spacing, alignment = Alignment.CenterHorizontally),
                          verticalArrangement = Arrangement.spacedBy(spacing),
                          maxItemsInEachRow = columns) {
                            if (topics.isEmpty()) {
                              Text(
                                  text = "No topics yet",
                                  style = MaterialTheme.typography.bodyMedium,
                                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            } else {
                              topics.forEach { topic ->
                                TopicChip(
                                    label = topic,
                                    selected = topic in selectedTopics,
                                    onClick = { onTopicToggle(topic) })
                              }
                            }
                          }
                    }

                Spacer(modifier = Modifier.height(32.dp))

                if (!errorMessage.isNullOrEmpty()) {
                  Text(
                      text = errorMessage,
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodyMedium,
                      modifier = Modifier.padding(horizontal = 8.dp))
                }
              }

          PrimaryCtaButton(
              text = "Start Now",
              onClick = onStartClick,
              enabled = !isSaving,
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
        }
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun ExperienceFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
  val contentColor = if (selected) Color.White else Variables.FilterChipContent
  val backgroundColor = if (selected) Variables.ColorOnClick else Variables.FilterChipBackground

  Surface(
      onClick = onClick,
      color = backgroundColor,
      contentColor = contentColor,
      shape = RoundedCornerShape(24.dp),
      modifier =
          Modifier.semantics { testTag = "${C.Tag.experiences_filter_chip_prefix}_$label" }) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically) {
              Icon( // PLACEHOLDER FOR NOW
                  imageVector = Icons.Outlined.Star,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = contentColor)
              Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor)
            }
      }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun TopicChip(
    label: String,
    width: Dp = topicChipWidth,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val targetBackground =
      if (selected) MaterialTheme.colorScheme.primaryContainer else Variables.TopicChipBackground
  val targetContent =
      if (selected) MaterialTheme.colorScheme.onPrimaryContainer else Variables.TopicChipContent
  val targetBorder =
      if (selected) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

  val backgroundColor by animateColorAsState(targetValue = targetBackground, label = "topicChipBg")
  val contentColor by animateColorAsState(targetValue = targetContent, label = "topicChipFg")
  val borderColor by animateColorAsState(targetValue = targetBorder, label = "topicChipBorder")

  val borderStroke =
      if (selected) BorderStroke(2.dp, borderColor) else BorderStroke(1.dp, borderColor)

  Surface(
      onClick = onClick,
      color = backgroundColor,
      contentColor = contentColor,
      shape = RoundedCornerShape(22.dp),
      border = borderStroke,
      modifier =
          modifier.width(width).semantics {
            testTag = "${C.Tag.experiences_topic_chip_prefix}_$label"
          }) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            textAlign = TextAlign.Center,
            color = contentColor)
      }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun PrimaryCtaButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
  Box(modifier = modifier.padding(horizontal = 64.dp)) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors =
            ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary),
        shape = RoundedCornerShape(100.dp),
        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 16.dp),
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).semantics {
              testTag = C.Tag.experiences_cta
            }) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp))
              }
        }
  }
}

private val topicChipWidth = 100.dp

internal val filterOptions = listOf("Sports", "Science", "Music", "Language", "Art", "Tech")

internal val experienceTopics =
    mapOf(
        "Sports" to
            listOf(
                "Bowling",
                "Football",
                "Tennis",
                "Squatch",
                "Running",
                "Cycling",
                "Volleyball",
                "Baseball",
                "Climbing",
                "Rowing",
                "Rugby",
                "Hockey",
                "MMA"),
        "Science" to
            listOf(
                "Astronomy",
                "Biology",
                "Chemistry",
                "Physics",
                "Robotics",
                "Ecology",
                "Genetics",
                "Medicine",
                "Research",
                "Space",
                "Ocean",
                "Energy",
                "Climate",
                "Geology",
                "Neuro-sci"),
        "Music" to
            listOf(
                "Choir",
                "Guitar",
                "Piano",
                "Jazz",
                "Drums",
                "Violin",
                "DJing",
                "Theory",
                "Opera",
                "Bands",
                "Compose",
                "Recording",
            ),
        "Language" to
            listOf(
                "Spanish",
                "French",
                "German",
                "Japanese",
                "Mandarin",
                "Italian",
                "Arabic",
                "Russian",
                "Korean",
                "Hindi",
                "Greek",
                "Dutch",
                "Swedish",
                "Finnish"),
        "Art" to
            listOf(
                "Painting",
                "Photo",
                "Design",
                "Theatre",
                "Dance",
                "Sculpture",
                "Animation",
                "Film",
                "Crafts",
                "Fashion",
                "Architecture",
                "Ceramics"),
        "Tech" to
            listOf(
                "AI",
                "Web",
                "Mobile",
                "Cybersecurity",
                "AR/VR",
                "Cloud",
                "IoT",
                "Data",
                "Blockchain",
                "Robotics",
                "Edge",
                "DevOps",
                "GameDev",
                "Hardware",
                "ML"))

@Preview(showBackground = true)
@Composable
private fun ExperiencesScreenPreview() {
  AppTheme {
    ExperiencesScreen(
        selectedFilter = filterOptions.first(),
        selectedTopics = emptySet(),
        onFilterSelected = {},
        onTopicToggle = {},
        onBackClick = {},
        onStartClick = {},
        isSaving = false,
        errorMessage = null)
  }
}

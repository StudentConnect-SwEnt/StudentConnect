package com.github.se.studentconnect.ui.screen.signup.regularuser

import androidx.annotation.VisibleForTesting
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.components.TopicChipGrid
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
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
  val topics = Activities.experienceTopics[selectedFilter] ?: emptyList()

  Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                    vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING)
                .semantics { testTag = C.Tag.experiences_screen_container },
        horizontalAlignment = Alignment.Start) {
          Column(
              modifier = Modifier.fillMaxWidth().semantics { testTag = C.Tag.experiences_top_bar },
              horizontalAlignment = Alignment.Start) {
                SignUpBackButton(onClick = onBackClick)

                SignUpMediumSpacer()

                Text(
                    text = "For an experience beyond Expectations",
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.primary),
                    textAlign = TextAlign.Start,
                    modifier =
                        Modifier.fillMaxWidth().semantics { testTag = C.Tag.experiences_title })

                Spacer(modifier = Modifier.height(8.dp))

                SignUpSubtitle(
                    text = "Discover what excites you",
                    modifier =
                        Modifier.fillMaxWidth().semantics { testTag = C.Tag.experiences_subtitle })
              }

          Spacer(modifier = Modifier.height(24.dp))

          Column(
              modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth().semantics {
                          testTag = C.Tag.experiences_topic_grid
                        }) {
                      TopicChipGrid(
                          tags = topics,
                          selectedTags = selectedTopics,
                          onTagToggle = onTopicToggle,
                          modifier = Modifier.fillMaxWidth(),
                          filterOptions = Activities.filterOptions,
                          selectedFilter = selectedFilter,
                          onFilterSelected = onFilterSelected)
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

          Spacer(
              modifier =
                  Modifier.Companion.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

          SignUpPrimaryButton(
              text = "Start Now",
              iconRes = R.drawable.ic_arrow_forward,
              onClick = onStartClick,
              enabled = !isSaving,
              modifier =
                  Modifier.align(Alignment.CenterHorizontally).testTag(C.Tag.experiences_cta))
        }
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

@Preview(showBackground = true)
@Composable
private fun ExperiencesScreenPreview() {
  AppTheme {
    ExperiencesScreen(
        selectedFilter = Activities.filterOptions.first(),
        selectedTopics = emptySet(),
        onFilterSelected = {},
        onTopicToggle = {},
        onBackClick = {},
        onStartClick = {},
        isSaving = false,
        errorMessage = null)
  }
}

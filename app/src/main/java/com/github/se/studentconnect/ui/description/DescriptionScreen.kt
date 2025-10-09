package com.github.se.studentconnect.ui.description

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.resources.C

@Composable
fun DescriptionScreen(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  DescriptionContent(
      description = description,
      onDescriptionChange = onDescriptionChange,
      onBackClick = onBackClick,
      onSkipClick = onSkipClick,
      onContinueClick = onContinueClick,
      modifier = modifier)
}

@Composable
fun DescriptionContent(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val background = MaterialTheme.colorScheme.surface

  Surface(modifier = modifier.fillMaxWidth(), color = background) {
    Column(
        modifier =
            Modifier.fillMaxWidth().semantics { testTag = C.Tag.description_screen_container },
        horizontalAlignment = Alignment.CenterHorizontally) {
          Column(
              modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
              horizontalAlignment = Alignment.Start) {
                DescriptionTopBar(onBackClick = onBackClick, onSkipClick = onSkipClick)

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier =
                        Modifier.weight(1f).fillMaxWidth().semantics {
                          testTag = C.Tag.description_prompt_container
                        },
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      OutlinedTextField(
                          value = description,
                          onValueChange = onDescriptionChange,
                          modifier =
                              Modifier.fillMaxSize()
                                  .padding(bottom = 64.dp)
                                  .border(
                                      width = 2.dp,
                                      color = MaterialTheme.colorScheme.primary,
                                      shape = RoundedCornerShape(size = 16.dp))
                                  .fillMaxWidth()
                                  .semantics { testTag = C.Tag.description_input },
                          placeholder = {
                            Text(
                                text = "What should other students know about you?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline)
                          },
                          shape = RoundedCornerShape(16.dp),
                          colors =
                              TextFieldDefaults.colors(
                                  focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                  unfocusedIndicatorColor =
                                      MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                  cursorColor = MaterialTheme.colorScheme.primary,
                                  focusedContainerColor = Color.Transparent,
                                  unfocusedContainerColor = Color.Transparent,
                                  disabledContainerColor = Color.Transparent,
                                  focusedPlaceholderColor = MaterialTheme.colorScheme.primary,
                                  unfocusedPlaceholderColor = MaterialTheme.colorScheme.primary),
                          textStyle =
                              MaterialTheme.typography.bodyMedium.copy(
                                  color = MaterialTheme.colorScheme.outline),
                          maxLines = 8,
                          minLines = 6)
                    }
                ContinueButton(onContinueClick = onContinueClick)
              }
        }
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun DescriptionTopBar(
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.Start) {
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(top = 25.dp).semantics {
                  testTag = C.Tag.description_app_bar
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                  onClick = onBackClick,
                  modifier = Modifier.semantics { testTag = C.Tag.description_back }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface)
                  }

              Surface(
                  onClick = onSkipClick,
                  modifier =
                      Modifier.padding(end = 5.dp).semantics { testTag = C.Tag.description_skip },
                  shape = RoundedCornerShape(24.dp),
                  color = MaterialTheme.colorScheme.surfaceDim) {
                    Text(
                        text = "Skip",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                  }
            }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start) {
              Text(
                  text = "Tell us more about you",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = 28.sp,
                          color = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.semantics { testTag = C.Tag.description_title })

              Text(
                  text = "What should others know",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant),
                  modifier =
                      Modifier.padding(top = 4.dp).semantics {
                        testTag = C.Tag.description_subtitle
                      })
            }
      }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun DescriptionPrompt(
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
  Column(
      modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.description_prompt_container },
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier =
                Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(size = 16.dp))
                    .padding(1.dp)
                    .fillMaxWidth()
                    .semantics { testTag = C.Tag.description_input },
            placeholder = {
              Text(
                  text = "What should other students know about you?",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.outline)
            },
            shape = RoundedCornerShape(16.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.primary,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.primary),
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
            maxLines = 8,
            minLines = 6)
      }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun ContinueButton(onContinueClick: () -> Unit) {
  Box(modifier = Modifier.padding(horizontal = 64.dp)) {
    Button(
        onClick = onContinueClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(100.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier =
            Modifier.fillMaxWidth().padding(bottom = 16.dp).semantics {
              testTag = C.Tag.description_continue
            }) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Continue",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Continue",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp))
              }
        }
  }
}

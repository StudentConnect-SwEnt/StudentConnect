package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C

@Composable
fun DescriptionScreen(
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
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
            Modifier.fillMaxSize()
                .padding(
                    horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                    vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING)
                .semantics { testTag = C.Tag.description_screen_container },
        horizontalAlignment = Alignment.Start) {
          Row(
              modifier = Modifier.fillMaxWidth().semantics { testTag = C.Tag.description_app_bar },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                SignUpBackButton(
                    onClick = onBackClick,
                    modifier = Modifier.semantics { testTag = C.Tag.description_back })

                SignUpSkipButton(
                    onClick = onSkipClick,
                    modifier = Modifier.semantics { testTag = C.Tag.description_skip })
              }

          SignUpMediumSpacer()

          SignUpTitle(
              text = "Tell us more about you",
              modifier = Modifier.semantics { testTag = C.Tag.description_title })

          SignUpSmallSpacer()

          SignUpSubtitle(
              text = "What should others know",
              modifier = Modifier.semantics { testTag = C.Tag.description_subtitle })

          SignUpLargeSpacer()

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
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(size = 16.dp))
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

          Spacer(modifier = Modifier.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

          SignUpPrimaryButton(
              text = "Continue",
              iconRes = R.drawable.ic_arrow_forward,
              onClick = onContinueClick,
              modifier =
                  Modifier.align(Alignment.CenterHorizontally).semantics {
                    testTag = C.Tag.description_continue
                  })
        }
  }
}

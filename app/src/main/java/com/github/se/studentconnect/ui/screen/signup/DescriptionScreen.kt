package com.github.se.studentconnect.ui.screen.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.github.se.studentconnect.R
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.components.BioTextField
import com.github.se.studentconnect.ui.components.BioTextFieldConfig
import com.github.se.studentconnect.ui.components.BioTextFieldStyle

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
                // Bio TextField using reusable component
                BioTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier =
                        Modifier.fillMaxSize().semantics { testTag = C.Tag.description_input },
                    config =
                        BioTextFieldConfig(
                            placeholder = "What should other students know about you?",
                            showCharacterCount = false,
                            style = BioTextFieldStyle.Bordered))
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

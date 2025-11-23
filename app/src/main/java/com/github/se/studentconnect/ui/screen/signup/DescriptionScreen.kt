package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
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

// New reusable layout extracted from the previous DescriptionContent so other screens can reuse it.
@Composable
fun DescriptionLayout(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    @StringRes placeholderRes: Int,
    containerTag: String,
    appBarTag: String,
    backTag: String,
    skipTag: String,
    titleTag: String,
    subtitleTag: String,
    promptContainerTag: String,
    inputTag: String,
    continueTag: String,
    text: String,
    onTextChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onContinueClick: () -> Unit,
    showSkip: Boolean = true
) {
  val background = MaterialTheme.colorScheme.surface

  Surface(modifier = modifier.fillMaxWidth(), color = background) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                    vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING)
                .semantics { testTag = containerTag },
        horizontalAlignment = Alignment.Start) {
          Row(
              modifier = Modifier.fillMaxWidth().semantics { testTag = appBarTag },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                SignUpBackButton(
                    onClick = onBackClick, modifier = Modifier.semantics { testTag = backTag })

                if (showSkip) {
                  SignUpSkipButton(
                      onClick = onSkipClick, modifier = Modifier.semantics { testTag = skipTag })
                }
              }

          SignUpMediumSpacer()

          SignUpTitle(
              text = stringResource(id = titleRes),
              modifier = Modifier.semantics { testTag = titleTag })

          SignUpSmallSpacer()

          SignUpSubtitle(
              text = stringResource(id = subtitleRes),
              modifier = Modifier.semantics { testTag = subtitleTag })

          SignUpLargeSpacer()

          Column(
              modifier =
                  Modifier.weight(1f).fillMaxWidth().semantics { testTag = promptContainerTag },
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally) {
                BioTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxSize().semantics { testTag = inputTag },
                    config =
                        BioTextFieldConfig(
                            placeholder = stringResource(id = placeholderRes),
                            showCharacterCount = false,
                            style = BioTextFieldStyle.Bordered))
              }

          Spacer(modifier = Modifier.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

          SignUpPrimaryButton(
              text = stringResource(id = R.string.button_continue),
              iconRes = R.drawable.ic_arrow_forward,
              onClick = onContinueClick,
              modifier =
                  Modifier.align(Alignment.CenterHorizontally).semantics { testTag = continueTag })
        }
  }
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
  // Delegate to the reusable DescriptionLayout and provide resource ids + test tags used by the
  // original screen.
  DescriptionLayout(
      modifier = modifier,
      titleRes = R.string.about_title,
      subtitleRes = R.string.about_subtitle,
      placeholderRes = R.string.placeholder_about,
      containerTag = C.Tag.description_screen_container,
      appBarTag = C.Tag.description_app_bar,
      backTag = C.Tag.description_back,
      skipTag = C.Tag.description_skip,
      titleTag = C.Tag.description_title,
      subtitleTag = C.Tag.description_subtitle,
      promptContainerTag = C.Tag.description_prompt_container,
      inputTag = C.Tag.description_input,
      continueTag = C.Tag.description_continue,
      text = description,
      onTextChange = onDescriptionChange,
      onBackClick = onBackClick,
      onSkipClick = onSkipClick,
      onContinueClick = onContinueClick)
}

package com.github.se.studentconnect.ui.screen.signup.regularuser

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
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpLargeSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSkipButton
import com.github.se.studentconnect.ui.screen.signup.SignUpSmallSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
import com.github.se.studentconnect.ui.screen.signup.SignUpTitle

/**
 * Description screen collecting a short multi-line text input from the user. Reuses the
 * DescriptionLayout to avoid code duplication.
 *
 * @param description The current description text.
 * @param onDescriptionChange Callback when the description text changes.
 * @param onBackClick Callback when the back button is clicked.
 * @param onSkipClick Callback when the skip button is clicked.
 * @param onContinueClick Callback when the continue button is clicked.
 * @param modifier Modifier for the screen.
 */
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

// Group related parameters into small data classes to reduce the number of parameters
// and improve readability / maintainability.
data class DescriptionLayoutTags(
    val containerTag: String,
    val appBarTag: String,
    val backTag: String,
    val skipTag: String,
    val titleTag: String,
    val subtitleTag: String,
    val promptContainerTag: String,
    val inputTag: String,
    val continueTag: String
)

data class DescriptionLayoutCallbacks(
    val onBackClick: () -> Unit,
    val onSkipClick: () -> Unit,
    val onContinueClick: () -> Unit
)

data class DescriptionLayoutTextConfig(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val placeholderRes: Int,
    val text: String,
    val onTextChange: (String) -> Unit,
    val showSkip: Boolean = true
)

/**
 * Reusable layout used by multiple signup flows that collect a short multi-line text input.
 * Parameters are grouped into small data classes to avoid long parameter lists.
 */
@Composable
fun DescriptionLayout(
    modifier: Modifier = Modifier,
    tags: DescriptionLayoutTags,
    textConfig: DescriptionLayoutTextConfig,
    callbacks: DescriptionLayoutCallbacks
) {
  val background = MaterialTheme.colorScheme.surface

  Surface(modifier = modifier.fillMaxWidth(), color = background) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                    vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING)
                .semantics { testTag = tags.containerTag },
        horizontalAlignment = Alignment.Start) {
          Row(
              modifier = Modifier.fillMaxWidth().semantics { testTag = tags.appBarTag },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                SignUpBackButton(
                    onClick = callbacks.onBackClick,
                    modifier = Modifier.semantics { testTag = tags.backTag })

                if (textConfig.showSkip) {
                  SignUpSkipButton(
                      onClick = callbacks.onSkipClick,
                      modifier = Modifier.semantics { testTag = tags.skipTag })
                }
              }

          SignUpMediumSpacer()

          SignUpTitle(
              text = stringResource(id = textConfig.titleRes),
              modifier = Modifier.semantics { testTag = tags.titleTag })

          SignUpSmallSpacer()

          SignUpSubtitle(
              text = stringResource(id = textConfig.subtitleRes),
              modifier = Modifier.semantics { testTag = tags.subtitleTag })

          SignUpLargeSpacer()

          Column(
              modifier =
                  Modifier.weight(1f).fillMaxWidth().semantics {
                    testTag = tags.promptContainerTag
                  },
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally) {
                BioTextField(
                    value = textConfig.text,
                    onValueChange = textConfig.onTextChange,
                    modifier = Modifier.fillMaxSize().semantics { testTag = tags.inputTag },
                    config =
                        BioTextFieldConfig(
                            placeholder = stringResource(id = textConfig.placeholderRes),
                            showCharacterCount = false,
                            style = BioTextFieldStyle.Bordered))
              }

          Spacer(
              modifier =
                  Modifier.Companion.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

          SignUpPrimaryButton(
              text = stringResource(id = R.string.button_continue),
              iconRes = R.drawable.ic_arrow_forward,
              onClick = callbacks.onContinueClick,
              modifier =
                  Modifier.align(Alignment.CenterHorizontally).semantics {
                    testTag = tags.continueTag
                  })
        }
  }
}

/**
 * Content for the description step. Delegates to the reusable DescriptionLayout and provides the
 * proper string resources and test tags.
 *
 * @param description The current description text.
 * @param onDescriptionChange Callback when the description text changes.
 * @param onBackClick Callback when the back button is clicked.
 * @param onSkipClick Callback when the skip button is clicked.
 * @param onContinueClick Callback when the continue button is clicked.
 * @param modifier Modifier for the content.
 */
@Composable
fun DescriptionContent(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  DescriptionLayout(
      modifier = modifier,
      tags =
          DescriptionLayoutTags(
              containerTag = C.Tag.description_screen_container,
              appBarTag = C.Tag.description_app_bar,
              backTag = C.Tag.description_back,
              skipTag = C.Tag.description_skip,
              titleTag = C.Tag.description_title,
              subtitleTag = C.Tag.description_subtitle,
              promptContainerTag = C.Tag.description_prompt_container,
              inputTag = C.Tag.description_input,
              continueTag = C.Tag.description_continue),
      textConfig =
          DescriptionLayoutTextConfig(
              titleRes = R.string.about_title,
              subtitleRes = R.string.about_subtitle,
              placeholderRes = R.string.placeholder_about,
              text = description,
              onTextChange = onDescriptionChange),
      callbacks =
          DescriptionLayoutCallbacks(
              onBackClick = onBackClick,
              onSkipClick = onSkipClick,
              onContinueClick = onContinueClick))
}

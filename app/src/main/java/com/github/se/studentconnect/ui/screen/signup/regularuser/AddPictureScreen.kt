package com.github.se.studentconnect.ui.screen.signup.regularuser

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import com.github.se.studentconnect.ui.screen.signup.SignUpBackButton
import com.github.se.studentconnect.ui.screen.signup.SignUpLargeSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpMediumSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpPrimaryButton
import com.github.se.studentconnect.ui.screen.signup.SignUpScreenConstants
import com.github.se.studentconnect.ui.screen.signup.SignUpSkipButton
import com.github.se.studentconnect.ui.screen.signup.SignUpSmallSpacer
import com.github.se.studentconnect.ui.screen.signup.SignUpSubtitle
import com.github.se.studentconnect.ui.screen.signup.SignUpTitle

val DEFAULT_PLACEHOLDER = "ic_user".toUri()

/**
 * Screen for adding a profile picture during the signup flow.
 *
 * This composable allows users to upload or take a profile picture, skip the step, or continue with
 * a placeholder. It integrates with the SignUpViewModel to manage the profile picture state and
 * provides callbacks for navigation actions.
 *
 * @param viewModel The SignUpViewModel that manages the signup flow state
 * @param onSkip Callback invoked when the user chooses to skip adding a profile picture
 * @param onContinue Callback invoked when the user wants to proceed to the next step
 * @param onBack Callback invoked when the user wants to go back to the previous step
 * @param titleRes String resource id for the title text
 * @param subtitleRes String resource id for the subtitle text
 */
@Composable
fun AddPictureScreen(
    viewModel: SignUpViewModel,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    @StringRes titleRes: Int = R.string.title_add_profile_picture,
    @StringRes subtitleRes: Int = R.string.subtitle_add_profile_picture
) {
  val signUpState by viewModel.state
  var profileUri by remember { mutableStateOf(signUpState.profilePictureUri) }

  LaunchedEffect(signUpState.profilePictureUri) { profileUri = signUpState.profilePictureUri }

  val canContinue = profileUri != null
  val selectedProfileUri = profileUri?.takeUnless { it == DEFAULT_PLACEHOLDER }

  val titleText = stringResource(id = titleRes)
  val subtitleText = stringResource(id = subtitleRes)

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  horizontal = SignUpScreenConstants.SCREEN_HORIZONTAL_PADDING,
                  vertical = SignUpScreenConstants.SCREEN_VERTICAL_PADDING),
      horizontalAlignment = Alignment.Start) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          SignUpBackButton(onClick = onBack)
          Spacer(Modifier.weight(1f))
          SignUpSkipButton(
              onClick = {
                viewModel.setProfilePictureUri(DEFAULT_PLACEHOLDER)
                profileUri = DEFAULT_PLACEHOLDER
                onSkip()
              })
        }

        SignUpMediumSpacer()

        SignUpTitle(text = titleText)
        SignUpSmallSpacer()
        SignUpSubtitle(text = subtitleText)

        SignUpLargeSpacer()

        PicturePickerCard(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = PicturePickerStyle.Avatar,
            existingImagePath = null,
            selectedImageUri = selectedProfileUri,
            onImageSelected = { uri ->
              viewModel.setProfilePictureUri(uri)
              profileUri = uri
            },
            placeholderText = stringResource(R.string.placeholder_upload_profile_photo),
            overlayText = stringResource(R.string.instruction_tap_to_change_photo),
            imageDescription = stringResource(R.string.content_description_upload_photo))

        Spacer(modifier = Modifier.weight(1f))

        SignUpPrimaryButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.button_continue),
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = canContinue)
      }
}

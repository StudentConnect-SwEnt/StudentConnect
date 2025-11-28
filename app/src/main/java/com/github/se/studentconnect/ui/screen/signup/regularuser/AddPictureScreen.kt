package com.github.se.studentconnect.ui.screen.signup.regularuser

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * A stateless, reusable screen for adding a profile picture.
 *
 * This version is decoupled from any specific ViewModel, making it reusable for both Regular User
 * signup and Organization signup.
 *
 * @param selectedUri The currently selected image URI (nullable).
 * @param onUriSelected Callback when a new image is picked.
 * @param onSkip Callback when the user clicks Skip.
 * @param onContinue Callback when the user clicks Continue.
 * @param onBack Callback when the user clicks Back.
 * @param titleRes String resource id for the title text.
 * @param subtitleRes String resource id for the subtitle text.
 */
@Composable
fun AddPictureScreen(
    selectedUri: Uri?,
    onUriSelected: (Uri) -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    @StringRes titleRes: Int = R.string.title_add_profile_picture,
    @StringRes subtitleRes: Int = R.string.subtitle_add_profile_picture
) {

  val canContinue = selectedUri != null
  val displayUri = selectedUri?.takeUnless { it == DEFAULT_PLACEHOLDER }

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
          SignUpSkipButton(onClick = onSkip)
        }

        SignUpMediumSpacer()

        SignUpTitle(text = stringResource(id = titleRes))
        SignUpSmallSpacer()
        SignUpSubtitle(text = stringResource(id = subtitleRes))

        SignUpLargeSpacer()

        PicturePickerCard(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = PicturePickerStyle.Avatar,
            existingImagePath = null,
            selectedImageUri = displayUri,
            onImageSelected = onUriSelected,
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

/**
 * Screen for adding a profile picture during the REGULAR USER signup flow.
 *
 * This wrapper maintains backward compatibility with the SignUpViewModel. It extracts state from
 * the ViewModel and delegates to the stateless AddPictureScreen.
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

  AddPictureScreen(
      selectedUri = signUpState.profilePictureUri,
      onUriSelected = { uri -> viewModel.setProfilePictureUri(uri) },
      onSkip = {
        // Specific logic for Regular User skip: set placeholder
        viewModel.setProfilePictureUri(DEFAULT_PLACEHOLDER)
        onSkip()
      },
      onContinue = onContinue,
      onBack = onBack,
      titleRes = titleRes,
      subtitleRes = subtitleRes)
}

package com.github.se.studentconnect.ui.screen.signup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.github.se.studentconnect.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        UploadCard(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            imageUri = profileUri?.takeIf { it != DEFAULT_PLACEHOLDER },
            hasSelection = profileUri != null && profileUri != DEFAULT_PLACEHOLDER,
            onPickImage = { uri ->
              viewModel.setProfilePictureUri(uri)
              profileUri = uri
            })

        Spacer(modifier = Modifier.weight(1f))

        SignUpPrimaryButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.button_continue),
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = canContinue)
      }
}

@Composable
private fun UploadCard(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    hasSelection: Boolean,
    onPickImage: (Uri) -> Unit
) {
  val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
  val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f) }
  val borderPadding = 12.dp
  val frameSize = 260.dp
  val context = LocalContext.current
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  val pickMediaLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onPickImage)
      }

  LaunchedEffect(imageUri) {
    imageBitmap =
        if (imageUri != null) loadBitmapFromUri(context, imageUri, Dispatchers.IO) else null
  }

  Box(
      modifier =
          modifier
              .size(frameSize)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
              .clickable(
                  onClick = {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  })
              .drawDashedCircleBorder(borderColor, dashEffect, borderPadding),
      contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
          Image(
              bitmap = imageBitmap!!,
              contentDescription = stringResource(id = R.string.content_description_selected_photo),
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop)
          Text(
              text = stringResource(id = R.string.instruction_tap_to_change_photo),
              style =
                  MaterialTheme.typography.bodyMedium.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant),
              modifier =
                  Modifier.align(Alignment.BottomCenter)
                      .padding(bottom = 24.dp)
                      .background(
                          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                          shape = RoundedCornerShape(12.dp))
                      .padding(horizontal = 12.dp, vertical = 6.dp))
        } else {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(56.dp),
                    tonalElevation = 2.dp) {
                      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription =
                                stringResource(id = R.string.content_description_upload_photo),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }
                Spacer(Modifier.height(16.dp))
                Text(
                    text =
                        if (hasSelection) stringResource(id = R.string.text_photo_selected)
                        else stringResource(id = R.string.placeholder_upload_profile_photo),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant))
              }
        }
      }
}

private fun Modifier.drawDashedCircleBorder(
    color: Color,
    pathEffect: PathEffect,
    padding: Dp,
    strokeWidth: Dp = 3.dp
): Modifier =
    this.then(
        Modifier.drawBehind {
          val strokePx = strokeWidth.toPx()
          val paddingPx = padding.toPx()
          val radius = (size.minDimension / 2f) - paddingPx - strokePx / 2f
          if (radius > 0f) {
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = strokePx, pathEffect = pathEffect))
          }
        })

private suspend fun loadBitmapFromUri(
    context: Context,
    uri: Uri,
    dispatcher: CoroutineDispatcher
): ImageBitmap? =
    withContext(dispatcher) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
      } catch (_: Exception) {
        null
      }
    }

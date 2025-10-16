package com.github.se.studentconnect.ui.screen.signup

// import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R

// import com.github.se.studentconnect.ui.theme.AppTheme

private const val DEFAULT_PLACEHOLDER = "ic_user"

/**
 * Screen for adding a profile picture during the signup flow.
 * 
 * This composable allows users to upload or take a profile picture, skip the step,
 * or continue with a placeholder. It integrates with the SignUpViewModel to manage
 * the profile picture state and provides callbacks for navigation actions.
 * 
 * @param viewModel The SignUpViewModel that manages the signup flow state
 * @param onPickImage Callback function that handles image picking. Receives a result
 *        callback that should be called with the selected image URI (or null if cancelled)
 * @param onSkip Callback invoked when the user chooses to skip adding a profile picture
 * @param onContinue Callback invoked when the user wants to proceed to the next step
 * @param onBack Callback invoked when the user wants to go back to the previous step
 */
@Composable
fun AddPictureScreen(
    viewModel: SignUpViewModel,
    onPickImage: (onResult: (String?) -> Unit) -> Unit = {},
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
  val signUpState by viewModel.state
  var profileUri by remember { mutableStateOf(signUpState.profilePictureUri) }

  LaunchedEffect(signUpState.profilePictureUri) { profileUri = signUpState.profilePictureUri }

  val canContinue = !profileUri.isNullOrBlank()

  Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.Start) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Spacer(Modifier.weight(1f))
          SkipButton(
              onClick = {
                viewModel.setProfilePictureUri(DEFAULT_PLACEHOLDER)
                profileUri = DEFAULT_PLACEHOLDER
                onSkip()
              })
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Add a profile picture",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary))
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Let others know what you look like !",
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)

        Spacer(Modifier.height(24.dp))

        UploadCard(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            hasSelection = !profileUri.isNullOrBlank() && profileUri != DEFAULT_PLACEHOLDER,
            onClick = {
              onPickImage { uri ->
                if (!uri.isNullOrBlank()) {
                  viewModel.setProfilePictureUri(uri)
                  profileUri = uri
                }
              }
            })

        Spacer(modifier = Modifier.weight(1f))

        PrimaryActionButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Continue",
            iconRes = R.drawable.ic_arrow_forward,
            onClick = onContinue,
            enabled = canContinue)
      }
}

@Composable
private fun SkipButton(onClick: () -> Unit) {
  Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Text(
            text = "Skip",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

@Composable
private fun UploadCard(modifier: Modifier = Modifier, hasSelection: Boolean, onClick: () -> Unit) {
  val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
  val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f) }
  val borderPadding = 12.dp
  val frameSize = 260.dp

  Box(
      modifier =
          modifier
              .size(frameSize)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
              .clickable(onClick = onClick)
              .drawDashedCircleBorder(borderColor, dashEffect, borderPadding),
      contentAlignment = Alignment.Center) {
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
                          contentDescription = "Upload photo",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
              Spacer(Modifier.height(16.dp))
              Text(
                  text = if (hasSelection) "Photo selected" else "Upload/Take your profile photo",
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant))
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

// @Preview(showBackground = true, widthDp = 360, heightDp = 720)
// @Composable
// private fun AddPictureScreenPreview() {
//  AppTheme {
//    AddPictureScreen(viewModel = SignUpViewModel(), onSkip = {}, onContinue = {}, onBack = {})
//  }
// }

package com.github.se.studentconnect.ui.screen.signup

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.theme.AppTheme

private const val DEFAULT_PLACEHOLDER = "ic_user"

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
            modifier = Modifier.fillMaxWidth(),
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
private fun UploadCard(hasSelection: Boolean, onClick: () -> Unit) {
  val shape = RoundedCornerShape(28.dp)
  val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
  val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f) }
  val cornerRadius = 28.dp

  val borderPadding = 12.dp

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(320.dp)
              .clip(shape)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
              .clickable(onClick = onClick)
              .drawDashedBorder(cornerRadius, borderColor, dashEffect, borderPadding),
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
                  text = if (hasSelection) "Photo selected" else "Upload your profile photo",
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
      }
}

private fun Modifier.drawDashedBorder(
    cornerRadius: Dp,
    color: Color,
    pathEffect: PathEffect,
    padding: Dp
): Modifier =
    this.then(
        Modifier.drawBehind {
          val strokeWidth = 3.dp.toPx()
          val paddingPx = padding.toPx()
          val corner = (cornerRadius.toPx() - paddingPx).coerceAtLeast(0f)
          val inset = paddingPx + strokeWidth / 2
          val width = size.width - (paddingPx * 2) - strokeWidth
          val height = size.height - (paddingPx * 2) - strokeWidth
          drawRoundRect(
              color = color,
              topLeft = Offset(inset, inset),
              size = Size(width.coerceAtLeast(0f), height.coerceAtLeast(0f)),
              cornerRadius = CornerRadius(corner, corner),
              style = Stroke(width = strokeWidth, pathEffect = pathEffect))
        })

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun AddPictureScreenPreview() {
  AppTheme {
    AddPictureScreen(viewModel = SignUpViewModel(), onSkip = {}, onContinue = {}, onBack = {})
  }
}

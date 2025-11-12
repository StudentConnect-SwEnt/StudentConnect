package com.github.se.studentconnect.ui.screen.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R

/**
 * Common constants for all sign-up screens to ensure visual consistency.
 *
 * These values define the standard spacing, sizing, and styling used throughout the sign-up flow to
 * create a unified user experience.
 */
object SignUpScreenConstants {
  /** Horizontal padding applied to the screen edges */
  val SCREEN_HORIZONTAL_PADDING = 20.dp

  /** Vertical padding applied to the top and bottom of the screen */
  val SCREEN_VERTICAL_PADDING = 16.dp

  /** Size of the back navigation button */
  val BACK_BUTTON_SIZE = 40.dp

  /** Spacing between the title and subtitle text */
  val TITLE_TO_SUBTITLE_SPACING = 4.dp

  /** Spacing between the subtitle and the main content area */
  val SUBTITLE_TO_CONTENT_SPACING = 24.dp

  /** Spacing between the header section and the title */
  val HEADER_TO_TITLE_SPACING = 16.dp

  /** Fixed height for all primary action buttons */
  val BUTTON_HEIGHT = 56.dp

  /** Fixed width for all primary action buttons to ensure consistency */
  val BUTTON_WIDTH = 200.dp

  /** Corner radius for the rounded button shape */
  val BUTTON_CORNER_RADIUS = 40.dp

  /** Horizontal padding inside buttons */
  val BUTTON_HORIZONTAL_PADDING = 32.dp

  /** Vertical padding inside buttons */
  val BUTTON_VERTICAL_PADDING = 12.dp

  /** Size of icons displayed within buttons */
  val ICON_SIZE = 18.dp

  /** Spacing between button text and icon */
  val ICON_SPACING = 12.dp
}

/**
 * Standardized back navigation button used across all sign-up screens.
 *
 * This button provides a consistent way to navigate back in the sign-up flow, using a Material
 * Design back arrow icon with consistent sizing and styling.
 *
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Optional modifier to customize the button's appearance or behavior
 */
@Composable
fun SignUpBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  IconButton(onClick = onClick, modifier = modifier.size(SignUpScreenConstants.BACK_BUTTON_SIZE)) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = stringResource(R.string.content_description_back),
        tint = MaterialTheme.colorScheme.onSurface)
  }
}

/**
 * Standardized title text component for sign-up screens.
 *
 * Displays the main heading for each screen using a consistent typography style, font family,
 * weight, and color scheme aligned with the app's design system.
 *
 * @param text The title text to display
 * @param modifier Optional modifier to customize the text's appearance or layout
 */
@Composable
fun SignUpTitle(text: String, modifier: Modifier = Modifier) {
  Text(
      text = text,
      style =
          MaterialTheme.typography.headlineMedium.copy(
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary),
      modifier = modifier)
}

/**
 * Standardized subtitle text component for sign-up screens.
 *
 * Displays supporting text below the title to provide additional context or instructions.
 * Automatically truncates with ellipsis if the text is too long.
 *
 * @param text The subtitle text to display
 * @param modifier Optional modifier to customize the text's appearance or layout
 */
@Composable
fun SignUpSubtitle(text: String, modifier: Modifier = Modifier) {
  Text(
      text = text,
      style =
          MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Normal,
              color = MaterialTheme.colorScheme.onSurfaceVariant),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = modifier)
}

/**
 * Standardized skip button for optional sign-up steps.
 *
 * Provides users with the option to skip non-mandatory steps in the sign-up process. Uses a
 * pill-shaped design with subtle styling to indicate its optional nature.
 *
 * @param onClick Callback invoked when the skip button is clicked
 * @param modifier Optional modifier to customize the button's appearance or behavior
 */
@Composable
fun SignUpSkipButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        Text(
            text = "Skip",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

/**
 * Standardized primary action button for sign-up screens.
 *
 * This is the main call-to-action button used consistently across all sign-up screens with a fixed
 * size and centered placement to ensure visual consistency throughout the user journey.
 *
 * Key features:
 * - Fixed width and height for uniform appearance
 * - Support for optional trailing icon
 * - Disabled state handling with reduced opacity
 * - Primary color scheme from Material Theme
 *
 * @param text The button label text (e.g., "Continue", "Start Now")
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Optional modifier for positioning (e.g., centering horizontally)
 * @param enabled Whether the button is clickable. When false, the button appears dimmed
 * @param iconRes Optional drawable resource ID for a trailing icon (e.g., arrow forward)
 */
@Composable
fun SignUpPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes iconRes: Int? = null
) {
  Button(
      onClick = onClick,
      enabled = enabled,
      modifier =
          modifier
              .width(SignUpScreenConstants.BUTTON_WIDTH)
              .height(SignUpScreenConstants.BUTTON_HEIGHT),
      shape = RoundedCornerShape(SignUpScreenConstants.BUTTON_CORNER_RADIUS),
      contentPadding =
          PaddingValues(
              horizontal = SignUpScreenConstants.BUTTON_HORIZONTAL_PADDING,
              vertical = SignUpScreenConstants.BUTTON_VERTICAL_PADDING),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
              disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)),
      elevation =
          ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Text(
                  text = text,
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
              if (iconRes != null) {
                Spacer(modifier = Modifier.width(SignUpScreenConstants.ICON_SPACING))
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(SignUpScreenConstants.ICON_SIZE),
                    tint = MaterialTheme.colorScheme.onPrimary)
              }
            }
      }
}

/**
 * Small vertical spacer for minimal spacing between elements.
 *
 * Use this between the title and subtitle for tight vertical spacing.
 */
@Composable
fun SignUpSmallSpacer() =
    Spacer(modifier = Modifier.height(SignUpScreenConstants.TITLE_TO_SUBTITLE_SPACING))

/**
 * Medium vertical spacer for standard spacing between sections.
 *
 * Use this between the header/back button and the title section.
 */
@Composable
fun SignUpMediumSpacer() =
    Spacer(modifier = Modifier.height(SignUpScreenConstants.HEADER_TO_TITLE_SPACING))

/**
 * Large vertical spacer for separating major sections.
 *
 * Use this between the subtitle and the main content area of the screen.
 */
@Composable
fun SignUpLargeSpacer() =
    Spacer(modifier = Modifier.height(SignUpScreenConstants.SUBTITLE_TO_CONTENT_SPACING))

package com.github.se.studentconnect.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.ui.profile.ProfileConstants

/**
 * Configuration class for BioTextField styling and behavior.
 *
 * @param placeholder Placeholder text to display when empty
 * @param showCharacterCount Whether to show the character counter
 * @param maxCharacters Maximum number of characters allowed
 * @param minLines Minimum number of lines to display
 * @param maxLines Maximum number of lines to display
 * @param style Style variant - Outlined or Bordered
 * @param colors Custom colors for the text field (if null, uses default based on style)
 */
data class BioTextFieldConfig(
    val placeholder: String = ProfileConstants.PLACEHOLDER_BIO,
    val showCharacterCount: Boolean = true,
    val maxCharacters: Int = ProfileConstants.MAX_BIO_LENGTH,
    val minLines: Int = 6,
    val maxLines: Int = 8,
    val style: BioTextFieldStyle = BioTextFieldStyle.Outlined,
    val colors: TextFieldColors? = null
)

/**
 * Reusable bio/description text field component with character counter and validation.
 *
 * This component is used in both the signup flow (DescriptionScreen) and the profile editing flow
 * (EditBioScreen) to reduce code duplication.
 *
 * Example usage:
 * ```
 * // In profile edit screen (Outlined style)
 * var bioText by remember { mutableStateOf("") }
 * BioTextField(
 *     value = bioText,
 *     onValueChange = { bioText = it }
 * )
 *
 * // In signup flow (Bordered style)
 * BioTextField(
 *     value = description,
 *     onValueChange = { description = it },
 *     config = BioTextFieldConfig(
 *         showCharacterCount = false,
 *         style = BioTextFieldStyle.Bordered
 *     )
 * )
 * ```
 *
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the text field
 * @param enabled Whether the text field is enabled
 * @param isError Whether to show error styling
 * @param errorMessage Optional error message to display
 * @param config Configuration for styling and behavior
 */
@Composable
fun BioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    config: BioTextFieldConfig = BioTextFieldConfig()
) {
  Column {
    val textFieldColors =
        config.colors
            ?: when (config.style) {
              BioTextFieldStyle.Outlined -> getOutlinedTextFieldColors()
              BioTextFieldStyle.Bordered -> getBorderedTextFieldColors()
            }

    val textFieldModifier = getTextFieldModifier(modifier, config.style)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = textFieldModifier,
        placeholder = { PlaceholderText(config.placeholder, config.style) },
        isError = isError,
        supportingText = errorMessage?.let { { ErrorText(it) } },
        enabled = enabled,
        colors = textFieldColors,
        shape = RoundedCornerShape(16.dp),
        textStyle = getTextStyle(config.style),
        minLines = config.minLines,
        maxLines = config.maxLines)

    if (config.showCharacterCount) {
      CharacterCounter(value.length, config.maxCharacters)
    }
  }
}

/**
 * Returns the modifier for the text field based on style.
 *
 * @param modifier Base modifier
 * @param style The style variant
 */
@Composable
private fun getTextFieldModifier(modifier: Modifier, style: BioTextFieldStyle): Modifier {
  return if (style == BioTextFieldStyle.Bordered) {
    modifier.border(
        width = 2.dp,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(size = 16.dp))
  } else {
    modifier
  }
}

/**
 * Returns the text style based on style variant.
 *
 * @param style The style variant
 */
@Composable
private fun getTextStyle(style: BioTextFieldStyle) =
    MaterialTheme.typography.bodyMedium.copy(
        color =
            if (style == BioTextFieldStyle.Bordered) {
              MaterialTheme.colorScheme.outline
            } else {
              MaterialTheme.colorScheme.onSurface
            })

/** Composable for placeholder text with style-based coloring. */
@Composable
private fun PlaceholderText(text: String, style: BioTextFieldStyle) {
  Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color =
          if (style == BioTextFieldStyle.Bordered) {
            MaterialTheme.colorScheme.outline
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          })
}

/** Composable for error message text. */
@Composable
private fun ErrorText(message: String) {
  Text(text = message, color = MaterialTheme.colorScheme.error)
}

/** Composable for character counter display. */
@Composable
private fun CharacterCounter(currentLength: Int, maxLength: Int) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
    Text(
        text = "$currentLength / $maxLength",
        style = MaterialTheme.typography.bodySmall,
        color =
            if (currentLength > maxLength) {
              MaterialTheme.colorScheme.error
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            })
  }
}

/**
 * Returns the default colors for the outlined text field style.
 *
 * This style uses standard Material3 colors with a primary border when focused and outline color
 * when unfocused.
 */
@Composable
private fun getOutlinedTextFieldColors(): TextFieldColors {
  return OutlinedTextFieldDefaults.colors(
      focusedBorderColor = MaterialTheme.colorScheme.primary,
      unfocusedBorderColor = MaterialTheme.colorScheme.outline)
}

/**
 * Returns the default colors for the bordered text field style.
 *
 * This style uses a transparent container with a primary-colored border and cursor, designed for
 * use with an additional border modifier in the signup flow.
 */
@Composable
private fun getBorderedTextFieldColors(): TextFieldColors {
  return OutlinedTextFieldDefaults.colors(
      focusedBorderColor = MaterialTheme.colorScheme.primary,
      unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
      cursorColor = MaterialTheme.colorScheme.primary,
      focusedContainerColor = Color.Transparent,
      unfocusedContainerColor = Color.Transparent,
      disabledContainerColor = Color.Transparent)
}

/** Style variants for the BioTextField component. */
enum class BioTextFieldStyle {
  /** Standard Material3 outlined text field style */
  Outlined,
  /** Custom bordered style with transparent container (used in signup flow) */
  Bordered
}

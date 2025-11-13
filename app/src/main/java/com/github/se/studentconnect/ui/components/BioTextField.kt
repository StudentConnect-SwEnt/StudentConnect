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
 * Reusable bio/description text field component with character counter and validation.
 *
 * This component is used in both the signup flow (DescriptionScreen) and the profile editing flow
 * (EditBioScreen) to reduce code duplication.
 *
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the text field
 * @param placeholder Placeholder text to display when empty
 * @param enabled Whether the text field is enabled
 * @param isError Whether to show error styling
 * @param errorMessage Optional error message to display
 * @param showCharacterCount Whether to show the character counter
 * @param maxCharacters Maximum number of characters allowed
 * @param minLines Minimum number of lines to display
 * @param maxLines Maximum number of lines to display
 * @param style Style variant - "outlined" for standard outlined style, "bordered" for custom
 *   bordered style
 * @param colors Custom colors for the text field (if null, uses default Material3 colors based on
 *   style)
 */
@Composable
fun BioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = ProfileConstants.PLACEHOLDER_BIO,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    showCharacterCount: Boolean = true,
    maxCharacters: Int = ProfileConstants.MAX_BIO_LENGTH,
    minLines: Int = 6,
    maxLines: Int = 8,
    style: BioTextFieldStyle = BioTextFieldStyle.Outlined,
    colors: TextFieldColors? = null
) {
  Column {
    val textFieldColors =
        colors
            ?: when (style) {
              BioTextFieldStyle.Outlined -> getOutlinedTextFieldColors()
              BioTextFieldStyle.Bordered -> getBorderedTextFieldColors()
            }

    val textFieldModifier =
        if (style == BioTextFieldStyle.Bordered) {
          modifier.border(
              width = 2.dp,
              color = MaterialTheme.colorScheme.primary,
              shape = RoundedCornerShape(size = 16.dp))
        } else {
          modifier
        }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = textFieldModifier,
        placeholder = {
          Text(
              text = placeholder,
              style = MaterialTheme.typography.bodyMedium,
              color =
                  if (style == BioTextFieldStyle.Bordered) {
                    MaterialTheme.colorScheme.outline
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  })
        },
        isError = isError,
        supportingText =
            if (errorMessage != null) {
              { Text(text = errorMessage, color = MaterialTheme.colorScheme.error) }
            } else {
              null
            },
        enabled = enabled,
        colors = textFieldColors,
        shape = RoundedCornerShape(16.dp),
        textStyle =
            MaterialTheme.typography.bodyMedium.copy(
                color =
                    if (style == BioTextFieldStyle.Bordered) {
                      MaterialTheme.colorScheme.outline
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    }),
        minLines = minLines,
        maxLines = maxLines)

    // Character counter
    if (showCharacterCount) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(
            text = "${value.length} / $maxCharacters",
            style = MaterialTheme.typography.bodySmall,
            color =
                if (value.length > maxCharacters) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                })
      }
    }
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

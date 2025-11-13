package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Creates a composable label for form text fields with optional required indicator.
 *
 * @param label The text to display in the label, or null if no label should be shown
 * @param required Whether to append a red asterisk (*) to indicate a required field
 * @return A composable function that renders the label, or null if label is null
 */
@Composable
private fun createLabelComposable(label: String?, required: Boolean): (@Composable () -> Unit)? {
  return label?.let {
    {
      if (required) {
        Text(
            buildAnnotatedString {
              append(it)
              withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
            })
      } else {
        Text(it)
      }
    }
  }
}

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    errorText: String? = null,
    required: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasBeenInteractedWith by remember { mutableStateOf(false) }
  val shouldShowError = hasBeenInteractedWith && errorText != null
  val labelComposable = createLabelComposable(label, required)

  OutlinedTextField(
      modifier =
          modifier.onFocusChanged {
            if (it.isFocused) hasBeenFocused = true
            else if (hasBeenFocused) hasBeenInteractedWith = true
          },
      value = value,
      onValueChange = {
        hasBeenInteractedWith = true
        onValueChange(it)
      },
      label = labelComposable,
      placeholder = placeholder?.let { { Text(it) } },
      shape = RoundedCornerShape(50.dp),
      enabled = enabled,
      isError = shouldShowError,
      supportingText = { if (shouldShowError) Text(text = errorText) },
      trailingIcon = trailingIcon)
}

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    errorText: String? = null,
    required: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasBeenInteractedWith by remember { mutableStateOf(false) }
  val shouldShowError = hasBeenInteractedWith && errorText != null
  val labelComposable = createLabelComposable(label, required)

  OutlinedTextField(
      modifier =
          modifier.onFocusChanged {
            if (it.isFocused) hasBeenFocused = true
            else if (hasBeenFocused) hasBeenInteractedWith = true
          },
      value = value,
      onValueChange = {
        hasBeenInteractedWith = true
        onValueChange(it)
      },
      label = labelComposable,
      placeholder = placeholder?.let { { Text(it) } },
      shape = RoundedCornerShape(50.dp),
      enabled = enabled,
      isError = shouldShowError,
      supportingText = { if (shouldShowError) Text(text = errorText) },
      trailingIcon = trailingIcon)
}

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

private val FormTextFieldShape = RoundedCornerShape(50.dp)

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
private fun FormTextFieldState(errorText: String?): Triple<Modifier, Boolean, () -> Unit> {
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasBeenInteractedWith by remember { mutableStateOf(false) }
  val shouldShowError = hasBeenInteractedWith && errorText != null
  val modifier =
      Modifier.onFocusChanged {
        if (it.isFocused) hasBeenFocused = true
        else if (hasBeenFocused) hasBeenInteractedWith = true
      }
  val markInteracted = { hasBeenInteractedWith = true }
  return Triple(modifier, shouldShowError, markInteracted)
}

@Composable
private fun FormTextFieldCommonParams(
    modifier: Modifier,
    focusModifier: Modifier,
    label: String?,
    required: Boolean,
    placeholder: String?,
    enabled: Boolean,
    shouldShowError: Boolean,
    errorText: String?,
    trailingIcon: (@Composable () -> Unit)?,
    leadingIcon: (@Composable () -> Unit)?
): Pair<Modifier, FormTextFieldParams> {
  val labelComposable = createLabelComposable(label, required)
  val placeholderComposable: (@Composable () -> Unit)? =
      placeholder?.let { placeHolderText -> { Text(placeHolderText) } }
  val supportingText: (@Composable () -> Unit)? =
      if (shouldShowError) {
        { Text(text = errorText ?: "") }
      } else {
        null
      }
  return Pair(
      modifier.then(focusModifier),
      FormTextFieldParams(
          labelComposable,
          placeholderComposable,
          supportingText,
          enabled,
          shouldShowError,
          trailingIcon,
          leadingIcon))
}

private data class FormTextFieldParams(
    val label: (@Composable () -> Unit)?,
    val placeholder: (@Composable () -> Unit)?,
    val supportingText: (@Composable () -> Unit)?,
    val enabled: Boolean,
    val isError: Boolean,
    val trailingIcon: (@Composable () -> Unit)?,
    val leadingIcon: (@Composable () -> Unit)?
)

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
    leadingIcon: (@Composable () -> Unit)? = null,
) {
  val (focusModifier, shouldShowError, markInteracted) = FormTextFieldState(errorText)
  val (finalModifier, params) =
      FormTextFieldCommonParams(
          modifier,
          focusModifier,
          label,
          required,
          placeholder,
          enabled,
          shouldShowError,
          errorText,
          trailingIcon,
          leadingIcon)

  OutlinedTextField(
      modifier = finalModifier,
      value = value,
      onValueChange = { newValue: String ->
        markInteracted()
        onValueChange(newValue)
      },
      label = params.label,
      placeholder = params.placeholder,
      shape = FormTextFieldShape,
      enabled = params.enabled,
      isError = params.isError,
      supportingText = params.supportingText,
      trailingIcon = params.trailingIcon,
      leadingIcon = params.leadingIcon)
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
    leadingIcon: (@Composable () -> Unit)? = null,
) {
  val (focusModifier, shouldShowError, markInteracted) = FormTextFieldState(errorText)
  val (finalModifier, params) =
      FormTextFieldCommonParams(
          modifier,
          focusModifier,
          label,
          required,
          placeholder,
          enabled,
          shouldShowError,
          errorText,
          trailingIcon,
          leadingIcon)

  OutlinedTextField(
      modifier = finalModifier,
      value = value,
      onValueChange = { newValue: TextFieldValue ->
        markInteracted()
        onValueChange(newValue)
      },
      label = params.label,
      placeholder = params.placeholder,
      shape = FormTextFieldShape,
      enabled = params.enabled,
      isError = params.isError,
      supportingText = params.supportingText,
      trailingIcon = params.trailingIcon,
      leadingIcon = params.leadingIcon)
}

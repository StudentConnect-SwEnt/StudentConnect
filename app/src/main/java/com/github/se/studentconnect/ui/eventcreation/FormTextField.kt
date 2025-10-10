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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun FormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    errorText: String? = null,
) {
    var hasBeenFocused by remember { mutableStateOf(false) }

    // remember if the field has been interacted with once before
    // an interaction is: either the value has been modified, or the element was focused then
    // unfocused
    var hasBeenInteractedWith by remember { mutableStateOf(false) }

    // only show the error if the field has been focused and modified once before
    val shouldShowError = hasBeenInteractedWith && errorText != null

    OutlinedTextField(
        modifier =
            modifier.onFocusChanged {
                // if the element was focused and then unfocused, it is an interaction
                if (it.isFocused) hasBeenFocused = true
                else if (hasBeenFocused) hasBeenInteractedWith = true
            },
        value = value,
        onValueChange = {
            hasBeenInteractedWith = true
            onValueChange(it)
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        shape = RoundedCornerShape(50.dp),
        enabled = enabled,
        isError = shouldShowError,
        supportingText = {
            if (shouldShowError) Text(text = errorText!!)
        }
    )
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
) {
    var hasBeenFocused by remember { mutableStateOf(false) }

    // remember if the field has been interacted with once before
    // an interaction is: either the value has been modified, or the element was focused then
    // unfocused
    var hasBeenInteractedWith by remember { mutableStateOf(false) }

    // only show the error if the field has been focused and modified once before
    val shouldShowError = hasBeenInteractedWith && errorText != null

    OutlinedTextField(
        modifier =
            modifier.onFocusChanged {
                // if the element was focused and then unfocused, it is an interaction
                if (it.isFocused) hasBeenFocused = true
                else if (hasBeenFocused) hasBeenInteractedWith = true
            },
        value = value,
        onValueChange = {
            hasBeenInteractedWith = true
            onValueChange(it)
        },
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        shape = RoundedCornerShape(50.dp),
        enabled = enabled,
        isError = shouldShowError,
        supportingText = {
            if (shouldShowError) Text(text = errorText!!)
        }
    )
}

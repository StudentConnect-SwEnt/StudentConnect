package com.github.se.studentconnect.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * A reusable composable for profile fields with inline editing functionality.
 *
 * @param label The label for the field (e.g., "University", "Country")
 * @param value The current value of the field
 * @param isEditing Whether this field is currently being edited
 * @param isLoading Whether this field is currently loading (saving)
 * @param errorMessage Error message to display if validation fails
 * @param onEditClick Called when the edit icon is clicked
 * @param onSave Called when the save icon is clicked with the new value
 * @param onCancel Called when the cancel icon is clicked
 * @param keyboardType The keyboard type for the text field
 * @param capitalization The capitalization mode for the text field
 * @param maxLines Maximum number of lines for the text field
 * @param modifier Modifier for the composable
 * @param isEditable Whether the field can be edited
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onEditClick: () -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true
) {
  var textValue by remember(value) { mutableStateOf(value) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Update text value when the prop value changes
  LaunchedEffect(value) { textValue = value }

  // Auto-focus when editing starts
  LaunchedEffect(isEditing) {
    if (isEditing && isEditable) {
      focusRequester.requestFocus()
    }
  }

  Card(
      modifier = modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              // Left side: Label and content
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp))

                AnimatedVisibility(
                    visible = !isEditing || !isEditable,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()) {
                      Text(
                          text = value.ifBlank { "Not specified" },
                          style = MaterialTheme.typography.bodyLarge,
                          color =
                              if (value.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                              } else {
                                MaterialTheme.colorScheme.onSurface
                              })
                    }

                AnimatedVisibility(
                    visible = isEditing && isEditable,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()) {
                      OutlinedTextField(
                          value = textValue,
                          onValueChange = { textValue = it },
                          modifier = Modifier.weight(1f).focusRequester(focusRequester),
                          keyboardOptions =
                              KeyboardOptions(
                                  keyboardType = keyboardType,
                                  capitalization = capitalization,
                                  imeAction = ImeAction.Done),
                          keyboardActions =
                              KeyboardActions(
                                  onDone = {
                                    keyboardController?.hide()
                                    onSave(textValue)
                                  }),
                          maxLines = maxLines,
                          isError = errorMessage != null,
                          supportingText =
                              errorMessage?.let {
                                { Text(text = it, color = MaterialTheme.colorScheme.error) }
                              })
                    }
              }

              // Right side: Action buttons
              Row(
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    when {
                      !isEditable -> Unit
                      isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary)
                      }
                      isEditing -> {
                        // Save button
                        IconButton(onClick = { onSave(textValue) }, enabled = !isLoading) {
                          Icon(
                              imageVector = Icons.Default.Check,
                              contentDescription = "Save",
                              tint = MaterialTheme.colorScheme.primary)
                        }

                        // Cancel button
                        IconButton(onClick = onCancel, enabled = !isLoading) {
                          Icon(
                              imageVector = Icons.Default.Close,
                              contentDescription = "Cancel",
                              tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                      }
                      else -> {
                        // Edit button
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.semantics { contentDescription = "Edit $label" }) {
                              Icon(
                                  imageVector = Icons.Default.Edit,
                                  contentDescription = "Edit $label",
                                  tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                      }
                    }
                  }
            }
      }
}

/** Specialized version for multi-line text fields (like bio). */
@Composable
fun EditableProfileFieldMultiline(
    label: String,
    value: String,
    isEditing: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onEditClick: () -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true
) {
  EditableProfileField(
      label = label,
      value = value,
      isEditing = isEditing,
      isLoading = isLoading,
      errorMessage = errorMessage,
      onEditClick = onEditClick,
      onSave = onSave,
      onCancel = onCancel,
      maxLines = 3,
      modifier = modifier,
      isEditable = isEditable)
}

/** Specialized version for numeric input (like birthday). */
@Composable
fun EditableProfileFieldNumeric(
    label: String,
    value: String,
    isEditing: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onEditClick: () -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true
) {
  EditableProfileField(
      label = label,
      value = value,
      isEditing = isEditing,
      isLoading = isLoading,
      errorMessage = errorMessage,
      onEditClick = onEditClick,
      onSave = onSave,
      onCancel = onCancel,
      keyboardType = KeyboardType.Number,
      capitalization = KeyboardCapitalization.None,
      modifier = modifier,
      isEditable = isEditable)
}

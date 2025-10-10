package com.github.se.studentconnect.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.joinToString
import kotlin.let
import kotlin.text.isBlank
import kotlin.text.trim

/** Dialog for editing the user's name (first name and last name). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameDialog(
    currentFirstName: String,
    currentLastName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = true
) {
  var firstName by remember(currentFirstName) { mutableStateOf(currentFirstName) }
  var lastName by remember(currentLastName) { mutableStateOf(currentLastName) }
  var firstNameError by remember { mutableStateOf<String?>(null) }
  var lastNameError by remember { mutableStateOf<String?>(null) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Auto-focus the first name field
  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      focusRequester.requestFocus()
    }
  }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Edit Name", style = MaterialTheme.typography.headlineSmall) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          OutlinedTextField(
              value = firstName,
              onValueChange = {
                firstName = it
                firstNameError = null
              },
              label = { Text("First Name") },
              modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
              keyboardOptions =
                  KeyboardOptions(
                      capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
              isError = firstNameError != null,
              supportingText =
                  firstNameError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                  })

          OutlinedTextField(
              value = lastName,
              onValueChange = {
                lastName = it
                lastNameError = null
              },
              label = { Text("Last Name") },
              modifier = Modifier.fillMaxWidth(),
              keyboardOptions =
                  KeyboardOptions(
                      capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
              keyboardActions =
                  KeyboardActions(
                      onDone = {
                        keyboardController?.hide()
                        // Validate and save
                        if (validateAndSave(
                            firstName, lastName, firstNameError, lastNameError, onSave)) {
                          onDismiss()
                        }
                      }),
              isError = lastNameError != null,
              supportingText =
                  lastNameError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                  })
        }
      },
      confirmButton = {
        Button(
            onClick = {
              if (validateAndSave(firstName, lastName, firstNameError, lastNameError, onSave)) {
                onDismiss()
              }
            }) {
              Text("Save")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
      modifier = modifier)
}

/** Dialog for selecting a birthday using a date picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayPickerDialog(
    currentBirthday: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  // Parse current birthday if it exists
  val currentDate =
      remember(currentBirthday) {
        currentBirthday?.let { birthday ->
          try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(birthday)?.time
          } catch (e: Exception) {
            null
          }
        }
      }

  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentDate)

  var showDatePicker by remember { mutableStateOf(true) }

  if (showDatePicker) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
          Button(
              onClick = {
                datePickerState.selectedDateMillis?.let { dateMillis ->
                  val date = Date(dateMillis)
                  val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                  onSave(format.format(date))
                }
                onDismiss()
              }) {
                Text("Save")
              }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
          DatePicker(state = datePickerState)
        }
  }
}

/** Dialog for editing activities/hobbies with a multi-line text field. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivitiesDialog(
    currentActivities: List<String>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = true
) {
  val currentText = currentActivities.joinToString(", ")
  var activitiesText by remember(currentText) { mutableStateOf(currentText) }
  var error by remember { mutableStateOf<String?>(null) }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Auto-focus the text field
  LaunchedEffect(autoFocus) {
    if (autoFocus) {
      focusRequester.requestFocus()
    }
  }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = "Edit Activities", style = MaterialTheme.typography.headlineSmall) },
      text = {
        Column {
          Text(
              text = "Enter your favorite activities, separated by commas:",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 8.dp))

          OutlinedTextField(
              value = activitiesText,
              onValueChange = {
                activitiesText = it
                error = null
              },
              label = { Text("Activities") },
              modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
              maxLines = 4,
              keyboardOptions =
                  KeyboardOptions(
                      capitalization = KeyboardCapitalization.Sentences,
                      imeAction = ImeAction.Done),
              keyboardActions =
                  KeyboardActions(
                      onDone = {
                        keyboardController?.hide()
                        onSave(activitiesText)
                        onDismiss()
                      }),
              isError = error != null,
              supportingText =
                  error?.let { { Text(text = it, color = MaterialTheme.colorScheme.error) } })
        }
      },
      confirmButton = {
        Button(
            onClick = {
              onSave(activitiesText)
              onDismiss()
            }) {
              Text("Save")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
      modifier = modifier)
}

/** Helper function to validate name fields and show errors. */
private fun validateAndSave(
    firstName: String,
    lastName: String,
    firstNameError: String?,
    lastNameError: String?,
    onSave: (String, String) -> Unit
): Boolean {
  var hasError = false

  if (firstName.isBlank()) {
    // Note: We can't directly set the error state here since we're in a helper function
    // The error handling should be done in the composable
    hasError = true
  }

  if (lastName.isBlank()) {
    hasError = true
  }

  if (!hasError) {
    onSave(firstName.trim(), lastName.trim())
    return true
  }

  return false
}

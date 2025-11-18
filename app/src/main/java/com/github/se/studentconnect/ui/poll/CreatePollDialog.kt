package com.github.se.studentconnect.ui.poll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R

/** Test tags for the Create Poll dialog. */
object CreatePollTestTags {
  const val DIALOG = "create_poll_dialog"
  const val QUESTION_INPUT = "create_poll_question_input"
  const val OPTION_INPUT_PREFIX = "create_poll_option_input_"
  const val ADD_OPTION_BUTTON = "create_poll_add_option_button"
  const val REMOVE_OPTION_BUTTON_PREFIX = "create_poll_remove_option_button_"
  const val CREATE_BUTTON = "create_poll_create_button"
  const val CANCEL_BUTTON = "create_poll_cancel_button"
  const val ERROR_TEXT = "create_poll_error_text"
}

/**
 * Dialog for creating a new poll.
 *
 * @param eventUid The event identifier
 * @param onDismiss Callback when dialog is dismissed
 * @param pollViewModel The poll view model
 */
@Composable
fun CreatePollDialog(
    eventUid: String,
    onDismiss: () -> Unit,
    pollViewModel: PollViewModel = viewModel()
) {
  var question by remember { mutableStateOf("") }
  var options by remember { mutableStateOf(listOf("", "")) }
  var validationError by remember { mutableStateOf<String?>(null) }

  val uiState by pollViewModel.uiState.collectAsState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag(CreatePollTestTags.DIALOG),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp) {
          Column(
              modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.poll_create_title),
                    style = MaterialTheme.typography.headlineSmall)

                // Question input
                OutlinedTextField(
                    value = question,
                    onValueChange = {
                      question = it
                      validationError = null
                    },
                    label = { Text(stringResource(R.string.poll_create_question_label)) },
                    modifier = Modifier.fillMaxWidth().testTag(CreatePollTestTags.QUESTION_INPUT),
                    singleLine = false,
                    maxLines = 3)

                Text(
                    text = stringResource(R.string.poll_create_options_label),
                    style = MaterialTheme.typography.titleMedium)

                // Options input
                options.forEachIndexed { index, option ->
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = {
                              options = options.toMutableList().apply { this[index] = it }
                              validationError = null
                            },
                            label = {
                              Text(stringResource(R.string.poll_create_option_label, index + 1))
                            },
                            modifier =
                                Modifier.weight(1f)
                                    .testTag(CreatePollTestTags.OPTION_INPUT_PREFIX + index),
                            singleLine = true)

                        if (options.size > 2) {
                          IconButton(
                              onClick = { options = options.filterIndexed { i, _ -> i != index } },
                              modifier =
                                  Modifier.testTag(
                                      CreatePollTestTags.REMOVE_OPTION_BUTTON_PREFIX + index)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription =
                                        stringResource(R.string.poll_create_remove_option))
                              }
                        }
                      }
                }

                // Add option button
                if (options.size < 6) {
                  OutlinedButton(
                      onClick = { options = options + "" },
                      modifier =
                          Modifier.fillMaxWidth().testTag(CreatePollTestTags.ADD_OPTION_BUTTON)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.poll_create_add_option))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.poll_create_add_option))
                      }
                }

                // Error message
                (validationError ?: uiState.error)?.let { error ->
                  Text(
                      text = error,
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodySmall,
                      modifier = Modifier.testTag(CreatePollTestTags.ERROR_TEXT))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                      TextButton(
                          onClick = onDismiss,
                          modifier = Modifier.testTag(CreatePollTestTags.CANCEL_BUTTON)) {
                            Text(stringResource(R.string.button_cancel))
                          }

                      Button(
                          onClick = {
                            val trimmedQuestion = question.trim()
                            val trimmedOptions =
                                options.map { it.trim() }.filter { it.isNotEmpty() }

                            when {
                              trimmedQuestion.isEmpty() -> {
                                validationError = "Please enter a question"
                              }
                              trimmedOptions.size < 2 -> {
                                validationError = "Please provide at least 2 options"
                              }
                              else -> {
                                pollViewModel.createPoll(
                                    eventUid = eventUid,
                                    question = trimmedQuestion,
                                    optionTexts = trimmedOptions,
                                    onSuccess = onDismiss)
                              }
                            }
                          },
                          modifier = Modifier.testTag(CreatePollTestTags.CREATE_BUTTON),
                          enabled = !uiState.isLoading) {
                            if (uiState.isLoading) {
                              CircularProgressIndicator(
                                  modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                              Text(stringResource(R.string.poll_create_button))
                            }
                          }
                    }
              }
        }
  }
}

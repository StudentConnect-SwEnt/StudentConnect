package com.github.se.studentconnect.ui.poll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepository
import com.github.se.studentconnect.model.poll.PollRepositoryProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollDialog(
    eventUid: String,
    onDismiss: () -> Unit,
    onPollCreated: () -> Unit,
    pollRepository: PollRepository = PollRepositoryProvider.repository
) {
  var question by remember { mutableStateOf("") }
  var options by remember { mutableStateOf(listOf("", "")) }
  var isCreating by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface) {
          Column(
              modifier =
                  Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.poll_create_title),
                          style = MaterialTheme.typography.headlineSmall)
                      IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription =
                                stringResource(R.string.content_description_close_story))
                      }
                    }

                // Question input
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text(stringResource(R.string.poll_create_question_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3)

                // Options
                Text(
                    text = stringResource(R.string.poll_create_options_label),
                    style = MaterialTheme.typography.titleMedium)

                options.forEachIndexed { index, option ->
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                              options = options.toMutableList().apply { this[index] = newValue }
                            },
                            label = {
                              Text(stringResource(R.string.poll_create_option_label, index + 1))
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true)

                        if (options.size > 2) {
                          IconButton(
                              onClick = {
                                options = options.toMutableList().apply { removeAt(index) }
                              }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription =
                                        stringResource(R.string.poll_create_remove_option))
                              }
                        }
                      }
                }

                // Add option button
                if (options.size < 6) {
                  OutlinedButton(
                      onClick = { options = options + "" }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.poll_create_add_option))
                      }
                }

                // Create button
                Button(
                    onClick = {
                      scope.launch {
                        isCreating = true
                        try {
                          val pollUid = pollRepository.getNewUid()
                          val pollOptions =
                              options
                                  .filter { it.isNotBlank() }
                                  .mapIndexed { index, text ->
                                    PollOption(
                                        optionId = "opt${index + 1}",
                                        text = text.trim(),
                                        voteCount = 0)
                                  }

                          val poll =
                              Poll(
                                  uid = pollUid,
                                  eventUid = eventUid,
                                  question = question.trim(),
                                  options = pollOptions,
                                  isActive = true)

                          pollRepository.createPoll(poll)
                          onPollCreated()
                          onDismiss()
                        } catch (e: Exception) {
                          // Handle error (you might want to show a snackbar)
                          android.util.Log.e("CreatePollDialog", "Failed to create poll", e)
                        } finally {
                          isCreating = false
                        }
                      }
                    },
                    enabled =
                        !isCreating &&
                            question.isNotBlank() &&
                            options.count { it.isNotBlank() } >= 2,
                    modifier = Modifier.fillMaxWidth()) {
                      if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary)
                      } else {
                        Text(stringResource(R.string.poll_create_button))
                      }
                    }
              }
        }
  }
}

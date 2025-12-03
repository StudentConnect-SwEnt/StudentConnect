package com.github.se.studentconnect.ui.poll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollScreen(
    eventUid: String,
    pollUid: String,
    navController: NavHostController,
    pollViewModel: PollViewModel = viewModel()
) {
  val uiState by pollViewModel.pollUiState.collectAsState()
  val poll = uiState.poll
  val hasVoted = uiState.hasVoted
  val currentUserId = AuthenticationProvider.currentUser

  var selectedOptionId by remember { mutableStateOf<String?>(null) }
  var isOwner by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  // Fetch event to check if user is owner
  LaunchedEffect(eventUid) {
    scope.launch {
      try {
        val event = EventRepositoryProvider.repository.getEvent(eventUid)
        isOwner = currentUserId == event.ownerId
      } catch (e: Exception) {
        android.util.Log.e("PollScreen", "Failed to fetch event: $eventUid", e)
        isOwner = false
      }
    }
  }

  LaunchedEffect(eventUid, pollUid) { pollViewModel.fetchPoll(eventUid, pollUid) }

  // Set selected option if user has already voted
  LaunchedEffect(uiState.userVote) { uiState.userVote?.let { selectedOptionId = it.optionId } }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.poll_screen_title)) },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back))
              }
            })
      }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          when {
            uiState.isLoading -> {
              CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
              Column(
                  modifier = Modifier.align(Alignment.Center).padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.error ?: stringResource(R.string.poll_error_generic),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { pollViewModel.fetchPoll(eventUid, pollUid) }) {
                      Text("Retry")
                    }
                  }
            }
            poll != null -> {
              Column(
                  modifier = Modifier.fillMaxSize().padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Poll question
                    Text(
                        text = poll.question,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)

                    // Poll status
                    if (!poll.isActive) {
                      Surface(
                          modifier = Modifier.fillMaxWidth(),
                          color = MaterialTheme.colorScheme.errorContainer,
                          shape = MaterialTheme.shapes.medium) {
                            Text(
                                text = stringResource(R.string.poll_closed_message),
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium)
                          }
                    }

                    // Owner message
                    if (isOwner) {
                      Surface(
                          modifier = Modifier.fillMaxWidth(),
                          color = MaterialTheme.colorScheme.secondaryContainer,
                          shape = MaterialTheme.shapes.medium) {
                            Text(
                                text = stringResource(R.string.poll_owner_cannot_vote),
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium)
                          }
                    }

                    // Options
                    Text(
                        text =
                            if (hasVoted || isOwner) stringResource(R.string.poll_results_title)
                            else "Select an option:",
                        style = MaterialTheme.typography.titleMedium)

                    // Show results if voted or owner, otherwise show voting options
                    if (hasVoted || isOwner) {
                      // Show results
                      val totalVotes = poll.options.sumOf { it.voteCount }
                      poll.options.forEach { option ->
                        val percentage =
                            if (totalVotes > 0) (option.voteCount.toFloat() / totalVotes * 100)
                            else 0f
                        PollResultOption(
                            option = option,
                            percentage = percentage,
                            isSelected = option.optionId == selectedOptionId)
                      }

                      Spacer(modifier = Modifier.height(8.dp))
                      Text(
                          text = stringResource(R.string.poll_total_votes, totalVotes),
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                      // Show voting options
                      Column(modifier = Modifier.selectableGroup()) {
                        poll.options.forEach { option ->
                          Row(
                              Modifier.fillMaxWidth()
                                  .selectable(
                                      selected = selectedOptionId == option.optionId,
                                      onClick = { selectedOptionId = option.optionId },
                                      role = Role.RadioButton)
                                  .padding(vertical = 8.dp),
                              verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedOptionId == option.optionId, onClick = null)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = option.text, style = MaterialTheme.typography.bodyLarge)
                              }
                        }
                      }

                      Spacer(modifier = Modifier.weight(1f))

                      // Submit vote button
                      Button(
                          onClick = {
                            selectedOptionId?.let { optionId ->
                              pollViewModel.submitVote(eventUid, pollUid, optionId)
                            }
                          },
                          enabled = selectedOptionId != null && poll.isActive,
                          modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.poll_submit_vote_button))
                          }
                    }
                  }
            }
          }
        }
      }
}

@Composable
private fun PollResultOption(
    option: com.github.se.studentconnect.model.poll.PollOption,
    percentage: Float,
    isSelected: Boolean
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = option.text,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
          Text(
              text = "${option.voteCount} (${percentage.toInt()}%)",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

    Spacer(modifier = Modifier.height(4.dp))

    LinearProgressIndicator(
        progress = { percentage / 100f },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color =
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary,
    )
  }
}

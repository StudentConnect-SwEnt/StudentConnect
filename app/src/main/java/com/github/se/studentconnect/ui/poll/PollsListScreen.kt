package com.github.se.studentconnect.ui.poll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollsListScreen(
    eventUid: String,
    navController: NavHostController,
    pollViewModel: PollViewModel = viewModel()
) {
  val uiState by pollViewModel.pollsListUiState.collectAsState()

  LaunchedEffect(eventUid) { pollViewModel.fetchAllPolls(eventUid) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.polls_list_screen_title)) },
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
              Text(
                  text = uiState.error ?: stringResource(R.string.poll_error_generic),
                  modifier = Modifier.align(Alignment.Center).padding(16.dp),
                  color = MaterialTheme.colorScheme.error)
            }
            uiState.polls.isEmpty() -> {
              Text(
                  text = stringResource(R.string.polls_list_no_polls),
                  modifier = Modifier.align(Alignment.Center).padding(16.dp),
                  style = MaterialTheme.typography.bodyLarge)
            }
            else -> {
              LazyColumn(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(16.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.polls, key = { it.uid }) { poll ->
                      PollCard(
                          poll = poll,
                          hasVoted = uiState.userVotes.containsKey(poll.uid),
                          onClick = {
                            navController.navigate(Route.pollScreen(eventUid, poll.uid))
                          })
                    }
                  }
            }
          }
        }
      }
}

@Composable
private fun PollCard(poll: Poll, hasVoted: Boolean, onClick: () -> Unit) {
  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (poll.isActive) MaterialTheme.colorScheme.surfaceContainer
                  else MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = poll.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))

                // Status badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color =
                        if (poll.isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer) {
                      Text(
                          text =
                              stringResource(
                                  if (poll.isActive) R.string.poll_status_active
                                  else R.string.poll_status_closed),
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          style = MaterialTheme.typography.labelSmall,
                          color =
                              if (poll.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                              else MaterialTheme.colorScheme.onErrorContainer)
                    }
              }

          Spacer(modifier = Modifier.height(8.dp))

          // Options count and vote indicator
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.poll_options_count, poll.options.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (hasVoted) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.poll_voted_indicator),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                  }
                }
              }

          // Total votes
          val totalVotes = poll.options.sumOf { it.voteCount }
          if (totalVotes > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.poll_total_votes, totalVotes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
}

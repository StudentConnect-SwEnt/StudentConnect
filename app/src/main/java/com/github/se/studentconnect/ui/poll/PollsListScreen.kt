package com.github.se.studentconnect.ui.poll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.ui.navigation.Route

/** Test tags for the Polls List screen. */
object PollsListTestTags {
  const val SCREEN = "polls_list_screen"
  const val TOP_APP_BAR = "polls_list_top_app_bar"
  const val BACK_BUTTON = "polls_list_back_button"
  const val LOADING_INDICATOR = "polls_list_loading_indicator"
  const val NO_POLLS_TEXT = "polls_list_no_polls_text"
  const val POLL_CARD_PREFIX = "polls_list_poll_card_"
}

/**
 * Screen that displays all polls for an event (for organizers).
 *
 * @param eventUid The event identifier
 * @param navController Navigation controller
 * @param pollViewModel The poll view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollsListScreen(
    eventUid: String,
    navController: NavHostController,
    pollViewModel: PollViewModel = viewModel()
) {
  val uiState by pollViewModel.uiState.collectAsState()

  LaunchedEffect(key1 = eventUid) { pollViewModel.fetchActivePolls(eventUid) }

  Scaffold(
      modifier = Modifier.testTag(PollsListTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.polls_list_screen_title)) },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier = Modifier.testTag(PollsListTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back))
                  }
            },
            modifier = Modifier.testTag(PollsListTestTags.TOP_APP_BAR))
      }) { paddingValues ->
        when {
          uiState.isLoading -> {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(paddingValues)
                        .testTag(PollsListTestTags.LOADING_INDICATOR),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          }
          uiState.polls.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      text = stringResource(R.string.polls_list_no_polls),
                      style = MaterialTheme.typography.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag(PollsListTestTags.NO_POLLS_TEXT))
                }
          }
          else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  items(uiState.polls, key = { it.uid }) { poll ->
                    PollListItem(
                        poll = poll,
                        eventUid = eventUid,
                        onClick = { navController.navigate(Route.pollScreen(eventUid, poll.uid)) },
                        modifier = Modifier.testTag(PollsListTestTags.POLL_CARD_PREFIX + poll.uid))
                  }
                }
          }
        }
      }
}

@Composable
private fun PollListItem(
    poll: Poll,
    eventUid: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  val totalVotes = poll.options.sumOf { it.voteCount }

  Card(
      modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
      shape = RoundedCornerShape(12.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (poll.isActive) MaterialTheme.colorScheme.surfaceVariant
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = poll.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color =
                            if (poll.isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer) {
                          Text(
                              text =
                                  stringResource(
                                      if (poll.isActive) R.string.poll_status_active
                                      else R.string.poll_status_closed),
                              style = MaterialTheme.typography.labelSmall,
                              color =
                                  if (poll.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                  else MaterialTheme.colorScheme.onErrorContainer,
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                  }

              Text(
                  text = stringResource(R.string.poll_total_votes, totalVotes),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)

              // Show top option with highest votes
              poll.options
                  .maxByOrNull { it.voteCount }
                  ?.let { topOption ->
                    val percentage =
                        if (totalVotes > 0)
                            (topOption.voteCount.toFloat() / totalVotes * 100).toInt()
                        else 0
                    Text(
                        text = "Leading: ${topOption.text} ($percentage%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                  }
            }
      }
}

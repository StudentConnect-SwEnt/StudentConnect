package com.github.se.studentconnect.ui.poll

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption

/** Test tags for the Poll screen. */
object PollScreenTestTags {
  const val SCREEN = "poll_screen"
  const val TOP_APP_BAR = "poll_screen_top_app_bar"
  const val BACK_BUTTON = "poll_screen_back_button"
  const val LOADING_INDICATOR = "poll_screen_loading_indicator"
  const val ERROR_TEXT = "poll_screen_error_text"
  const val QUESTION_TEXT = "poll_screen_question_text"
  const val OPTION_CARD_PREFIX = "poll_screen_option_card_"
  const val SUBMIT_BUTTON = "poll_screen_submit_button"
  const val RESULTS_SECTION = "poll_screen_results_section"
  const val POLL_CLOSED_TEXT = "poll_screen_poll_closed_text"
}

/**
 * Screen for viewing and voting on a poll.
 *
 * @param eventUid The event identifier
 * @param pollUid The poll identifier
 * @param navController Navigation controller
 * @param pollViewModel The poll view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollScreen(
    eventUid: String,
    pollUid: String,
    navController: NavHostController,
    pollViewModel: PollViewModel = viewModel()
) {
  val uiState by pollViewModel.uiState.collectAsState()

  LaunchedEffect(key1 = pollUid) { pollViewModel.fetchPoll(eventUid, pollUid) }

  Scaffold(
      modifier = Modifier.testTag(PollScreenTestTags.SCREEN),
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.poll_screen_title)) },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier = Modifier.testTag(PollScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back))
                  }
            },
            modifier = Modifier.testTag(PollScreenTestTags.TOP_APP_BAR))
      }) { paddingValues ->
        when {
          uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().testTag(PollScreenTestTags.LOADING_INDICATOR),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          }
          uiState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      text = uiState.error ?: stringResource(R.string.poll_error_generic),
                      color = MaterialTheme.colorScheme.error,
                      modifier = Modifier.testTag(PollScreenTestTags.ERROR_TEXT))
                }
          }
          uiState.currentPoll != null -> {
            val poll = uiState.currentPoll!!
            PollContent(
                poll = poll,
                userHasVoted = uiState.userVote != null,
                isUserOwner = uiState.isUserOwner,
                selectedOptionId = uiState.selectedOptionId,
                onOptionSelected = { pollViewModel.selectOption(it) },
                onSubmitVote = {
                  pollViewModel.submitVote(eventUid, pollUid) { navController.popBackStack() }
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues))
          }
        }
      }
}

@Composable
private fun PollContent(
    poll: Poll,
    userHasVoted: Boolean,
    isUserOwner: Boolean,
    selectedOptionId: String?,
    onOptionSelected: (String) -> Unit,
    onSubmitVote: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier.verticalScroll(rememberScrollState()).padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Question
        Text(
            text = poll.question,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(PollScreenTestTags.QUESTION_TEXT))

        if (!poll.isActive) {
          Text(
              text = stringResource(R.string.poll_closed_message),
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag(PollScreenTestTags.POLL_CLOSED_TEXT))
        }

        // Show info message for organizer
        if (isUserOwner) {
          Text(
              text = stringResource(R.string.poll_owner_cannot_vote),
              color = MaterialTheme.colorScheme.primary,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.testTag("poll_owner_info_text"))
        }

        // Options
        if (userHasVoted || !poll.isActive || isUserOwner) {
          // Show results
          PollResults(poll = poll, modifier = Modifier.testTag(PollScreenTestTags.RESULTS_SECTION))
        } else {
          // Show voting options
          poll.options.forEachIndexed { index, option ->
            PollOptionCard(
                option = option,
                isSelected = selectedOptionId == option.optionId,
                onSelect = { onOptionSelected(option.optionId) },
                modifier = Modifier.testTag(PollScreenTestTags.OPTION_CARD_PREFIX + index))
          }

          // Submit button
          Button(
              onClick = onSubmitVote,
              modifier = Modifier.fillMaxWidth().testTag(PollScreenTestTags.SUBMIT_BUTTON),
              enabled = selectedOptionId != null && poll.isActive) {
                Text(stringResource(R.string.poll_submit_vote_button))
              }
        }
      }
}

@Composable
private fun PollOptionCard(
    option: PollOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(onClick = onSelect)
              .then(
                  if (isSelected)
                      Modifier.border(
                          width = 2.dp,
                          color = MaterialTheme.colorScheme.primary,
                          shape = RoundedCornerShape(12.dp))
                  else Modifier),
      shape = RoundedCornerShape(12.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
              RadioButton(selected = isSelected, onClick = onSelect)
              Spacer(modifier = Modifier.width(12.dp))
              Text(text = option.text, style = MaterialTheme.typography.bodyLarge)
            }
      }
}

@Composable
private fun PollResults(poll: Poll, modifier: Modifier = Modifier) {
  val totalVotes = poll.options.sumOf { it.voteCount }

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text(
        text = stringResource(R.string.poll_results_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold)

    poll.options.forEach { option ->
      val percentage =
          if (totalVotes > 0) (option.voteCount.toFloat() / totalVotes * 100).toInt() else 0

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = option.text,
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.weight(1f))
              Text(
                  text = "$percentage% (${option.voteCount})",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
    }

    Text(
        text = stringResource(R.string.poll_total_votes, totalVotes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

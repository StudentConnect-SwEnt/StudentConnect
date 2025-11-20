package com.github.se.studentconnect.ui.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollRepository
import com.github.se.studentconnect.model.poll.PollRepositoryProvider
import com.github.se.studentconnect.model.poll.PollVote
import com.github.se.studentconnect.repository.AuthenticationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PollUiState(
    val isLoading: Boolean = true,
    val poll: Poll? = null,
    val userVote: PollVote? = null,
    val error: String? = null,
    val hasVoted: Boolean = false
)

data class PollsListUiState(
    val isLoading: Boolean = true,
    val polls: List<Poll> = emptyList(),
    val userVotes: Map<String, PollVote> = emptyMap(),
    val error: String? = null
)

class PollViewModel(
    private val pollRepository: PollRepository = PollRepositoryProvider.repository
) : ViewModel() {

  private val _pollUiState = MutableStateFlow(PollUiState())
  val pollUiState: StateFlow<PollUiState> = _pollUiState.asStateFlow()

  private val _pollsListUiState = MutableStateFlow(PollsListUiState())
  val pollsListUiState: StateFlow<PollsListUiState> = _pollsListUiState.asStateFlow()

  fun fetchPoll(eventUid: String, pollUid: String) {
    _pollUiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        val poll = pollRepository.getPoll(eventUid, pollUid)
        val currentUserId = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }
        val userVote = currentUserId?.let { pollRepository.getUserVote(eventUid, pollUid, it) }

        _pollUiState.update {
          it.copy(
              isLoading = false,
              poll = poll,
              userVote = userVote,
              hasVoted = userVote != null,
              error = if (poll == null) "Poll not found" else null)
        }
      } catch (e: Exception) {
        _pollUiState.update {
          it.copy(isLoading = false, error = e.message ?: "Failed to load poll")
        }
      }
    }
  }

  fun fetchAllPolls(eventUid: String) {
    _pollsListUiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        val polls = pollRepository.getActivePolls(eventUid)
        val currentUserId = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }
        val userVotes = mutableMapOf<String, PollVote>()

        currentUserId?.let { userId ->
          polls.forEach { poll ->
            val vote = pollRepository.getUserVote(eventUid, poll.uid, userId)
            vote?.let { userVotes[poll.uid] = it }
          }
        }

        _pollsListUiState.update {
          it.copy(isLoading = false, polls = polls, userVotes = userVotes)
        }
      } catch (e: Exception) {
        _pollsListUiState.update {
          it.copy(isLoading = false, error = e.message ?: "Failed to load polls")
        }
      }
    }
  }

  fun submitVote(eventUid: String, pollUid: String, optionId: String) {
    viewModelScope.launch {
      try {
        val currentUserId =
            AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() } ?: return@launch
        val vote = PollVote(userId = currentUserId, pollUid = pollUid, optionId = optionId)

        pollRepository.submitVote(eventUid, vote)

        // Refresh poll after voting
        fetchPoll(eventUid, pollUid)
      } catch (e: Exception) {
        _pollUiState.update { it.copy(error = e.message ?: "Failed to submit vote") }
      }
    }
  }

  fun clearError() {
    _pollUiState.update { it.copy(error = null) }
    _pollsListUiState.update { it.copy(error = null) }
  }
}

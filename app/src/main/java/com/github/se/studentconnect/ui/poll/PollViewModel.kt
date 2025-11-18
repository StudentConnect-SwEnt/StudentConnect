package com.github.se.studentconnect.ui.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepository
import com.github.se.studentconnect.model.poll.PollRepositoryProvider
import com.github.se.studentconnect.model.poll.PollVote
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PollUiState(
    val polls: List<Poll> = emptyList(),
    val currentPoll: Poll? = null,
    val userVote: PollVote? = null,
    val selectedOptionId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasUnvotedPolls: Boolean = false,
    val isUserOwner: Boolean = false
)

class PollViewModel(
    private val pollRepository: PollRepository = PollRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(PollUiState())
  val uiState: StateFlow<PollUiState> = _uiState.asStateFlow()

  /**
   * Fetches all active polls for an event and checks if the user has any unvoted polls.
   *
   * @param eventUid The event identifier
   */
  fun fetchActivePolls(eventUid: String) {
    _uiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        val polls = pollRepository.getActivePolls(eventUid)
        val currentUserId = AuthenticationProvider.currentUser

        // Check if there are any unvoted polls
        var hasUnvoted = false
        for (poll in polls) {
          val vote = pollRepository.getUserVote(eventUid, poll.uid, currentUserId)
          if (vote == null) {
            hasUnvoted = true
            break
          }
        }

        _uiState.update { it.copy(polls = polls, isLoading = false, hasUnvotedPolls = hasUnvoted) }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message, isLoading = false) }
      }
    }
  }

  /**
   * Fetches a specific poll and the current user's vote if it exists.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   */
  fun fetchPoll(eventUid: String, pollUid: String) {
    _uiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        val poll = pollRepository.getPoll(eventUid, pollUid)
        val currentUserId = AuthenticationProvider.currentUser

        // Check if current user is the event owner
        val event = eventRepository.getEvent(eventUid)
        val isOwner = event.ownerId == currentUserId

        val userVote = pollRepository.getUserVote(eventUid, pollUid, currentUserId)

        _uiState.update {
          it.copy(
              currentPoll = poll,
              userVote = userVote,
              selectedOptionId = userVote?.optionId,
              isLoading = false,
              isUserOwner = isOwner)
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message, isLoading = false) }
      }
    }
  }

  /**
   * Creates a new poll for an event.
   *
   * @param eventUid The event identifier
   * @param question The poll question
   * @param optionTexts List of option texts
   * @param onSuccess Callback invoked on successful creation
   */
  fun createPoll(
      eventUid: String,
      question: String,
      optionTexts: List<String>,
      onSuccess: () -> Unit
  ) {
    _uiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        val pollUid = pollRepository.getNewUid()
        val options =
            optionTexts.mapIndexed { index, text ->
              PollOption(optionId = "option_$index", text = text, voteCount = 0)
            }

        val poll =
            Poll(
                uid = pollUid,
                eventUid = eventUid,
                question = question,
                options = options,
                createdAt = Timestamp.now(),
                isActive = true)

        pollRepository.createPoll(poll)
        _uiState.update { it.copy(isLoading = false) }
        onSuccess()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message, isLoading = false) }
      }
    }
  }

  /**
   * Submits a vote for the selected option.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   * @param onSuccess Callback invoked on successful vote submission
   */
  fun submitVote(eventUid: String, pollUid: String, onSuccess: () -> Unit) {
    val selectedOptionId = _uiState.value.selectedOptionId
    if (selectedOptionId == null) {
      _uiState.update { it.copy(error = "Please select an option") }
      return
    }

    _uiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        val currentUserId = AuthenticationProvider.currentUser
        val vote =
            PollVote(
                userId = currentUserId,
                pollUid = pollUid,
                optionId = selectedOptionId,
                votedAt = Timestamp.now())

        pollRepository.submitVote(vote)

        // Refresh the poll to get updated vote counts
        val updatedPoll = pollRepository.getPoll(eventUid, pollUid)
        _uiState.update { it.copy(currentPoll = updatedPoll, userVote = vote, isLoading = false) }
        onSuccess()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message, isLoading = false) }
      }
    }
  }

  /**
   * Selects an option for voting.
   *
   * @param optionId The option identifier
   */
  fun selectOption(optionId: String) {
    _uiState.update { it.copy(selectedOptionId = optionId) }
  }

  /**
   * Closes a poll, preventing further votes.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   * @param onSuccess Callback invoked on successful closure
   */
  fun closePoll(eventUid: String, pollUid: String, onSuccess: () -> Unit) {
    _uiState.update { it.copy(isLoading = true, error = null) }
    viewModelScope.launch {
      try {
        pollRepository.closePoll(eventUid, pollUid)
        _uiState.update { it.copy(isLoading = false) }
        onSuccess()
      } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message, isLoading = false) }
      }
    }
  }

  /** Clears any error messages. */
  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}

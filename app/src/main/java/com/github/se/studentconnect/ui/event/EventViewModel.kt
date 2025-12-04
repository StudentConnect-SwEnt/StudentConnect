package com.github.se.studentconnect.ui.event

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollRepository
import com.github.se.studentconnect.model.poll.PollRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val isJoined: Boolean = false,
    val showQrScanner: Boolean = false,
    val ticketValidationResult: TicketValidationResult? = null,
    val attendees: List<User> = emptyList(),
    val currentUser: User? = null,
    val owner: User? = null,
    val participantCount: Int = 0,
    val isFull: Boolean = false,
    val activePolls: List<Poll> = emptyList(),
    val showCreatePollDialog: Boolean = false,
    val showInviteFriendsDialog: Boolean = false,
    val friends: List<User> = emptyList(),
    val invitedFriendIds: Set<String> = emptySet(),
    val initialInvitedFriendIds: Set<String> = emptySet(),
    val isLoadingFriends: Boolean = false,
    @StringRes val friendsErrorRes: Int? = null,
    val isInvitingFriends: Boolean = false
)

sealed class TicketValidationResult {
  data class Valid(val participantId: String) : TicketValidationResult()

  data class Invalid(val userId: String) : TicketValidationResult()

  data class Error(val message: String) : TicketValidationResult()
}

class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val pollRepository: PollRepository = PollRepositoryProvider.repository,
    private val friendsRepository: FriendsRepository = FriendsRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(EventUiState())
  val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

  fun fetchEvent(eventUid: String) {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val fetchedEvent = eventRepository.getEvent(eventUid)
      val currentUserUid = AuthenticationProvider.currentUser

      val participants = eventRepository.getEventParticipants(eventUid)
      val ownerId = fetchedEvent.ownerId
      val filteredParticipants = participants.filter { it.uid != ownerId }
      val participantCount = filteredParticipants.size

      val actualIsJoined = filteredParticipants.any { it.uid == currentUserUid }

      val isFull = fetchedEvent.maxCapacity?.let { max -> participantCount >= max.toInt() } ?: false

      val finalIsJoined = actualIsJoined

      _uiState.update {
        it.copy(
            event = fetchedEvent,
            isLoading = false,
            isJoined = finalIsJoined,
            participantCount = participantCount,
            isFull = isFull,
            showInviteFriendsDialog = false,
            invitedFriendIds = emptySet(),
            initialInvitedFriendIds = emptySet(),
            friendsErrorRes = null,
            isInvitingFriends = false,
            friends = emptyList(),
            isLoadingFriends = false)
      }

      // Fetch active polls if user is already a participant
      if (finalIsJoined) {
        fetchActivePolls(eventUid)
      }
    }
  }

  fun leaveEvent(eventUid: String) {
    val currentUserUid = AuthenticationProvider.currentUser
    viewModelScope.launch {
      userRepository.leaveEvent(eventUid, currentUserUid)
      eventRepository.removeParticipantFromEvent(eventUid, currentUserUid)

      // Update participant count (exclude owner)
      val participants = eventRepository.getEventParticipants(eventUid)
      val event = _uiState.value.event
      val ownerId = event?.ownerId
      val participantCount = participants.count { it.uid != ownerId }
      val isFull = event?.maxCapacity?.let { max -> participantCount >= max.toInt() } ?: false

      _uiState.update {
        it.copy(isJoined = false, participantCount = participantCount, isFull = isFull)
      }
    }
  }

  fun joinEvent(eventUid: String) {
    val currentUserUid = AuthenticationProvider.currentUser
    viewModelScope.launch {
      val event = _uiState.value.event
      val ownerId = event?.ownerId
      if (event != null && currentUserUid != ownerId) {
        userRepository.joinEvent(eventUid, currentUserUid)
        val eventParticipant = EventParticipant(currentUserUid)
        eventRepository.addParticipantToEvent(eventUid, eventParticipant)
      }

      // Participant count (Exclude owner)
      val participants = eventRepository.getEventParticipants(eventUid)
      val participantCount = participants.count { it.uid != ownerId }
      val isFull = event?.maxCapacity?.let { max -> participantCount >= max.toInt() } ?: false
      val actualIsJoined = participants.any { it.uid == currentUserUid && it.uid != ownerId }

      _uiState.update {
        it.copy(isJoined = actualIsJoined, participantCount = participantCount, isFull = isFull)
      }

      // Fetch active polls after joining
      if (actualIsJoined) {
        fetchActivePolls(eventUid)
      }
    }
  }

  fun showQrScanner() {
    _uiState.update { it.copy(showQrScanner = true, ticketValidationResult = null) }
  }

  fun hideQrScanner() {
    _uiState.update { it.copy(showQrScanner = false, ticketValidationResult = null) }
  }

  fun validateParticipant(eventUid: String, scannedUserId: String) {
    viewModelScope.launch {
      try {
        val participants = eventRepository.getEventParticipants(eventUid)
        val isValid = participants.any { it.uid == scannedUserId }

        val result =
            if (isValid) {
              TicketValidationResult.Valid(scannedUserId)
            } else {
              TicketValidationResult.Invalid(scannedUserId)
            }

        _uiState.update { it.copy(ticketValidationResult = result) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              ticketValidationResult =
                  TicketValidationResult.Error(
                      e.message ?: "Unable to verify ticket. Please check your connection."))
        }
      }
    }
  }

  fun clearValidationResult() {
    _uiState.update { it.copy(ticketValidationResult = null) }
  }

  fun fetchActivePolls(eventUid: String) {
    viewModelScope.launch {
      try {
        val polls = pollRepository.getActivePolls(eventUid)
        _uiState.update { it.copy(activePolls = polls) }
      } catch (e: Exception) {
        // Log error but don't fail the UI
        android.util.Log.e("EventViewModel", "Failed to fetch active polls: ${e.message}", e)
      }
    }
  }

  fun showCreatePollDialog() {
    _uiState.update { it.copy(showCreatePollDialog = true) }
  }

  fun hideCreatePollDialog() {
    _uiState.update { it.copy(showCreatePollDialog = false) }
  }

  /** Opens the invite friends dialog and triggers loading the friend list + existing invites. */
  fun showInviteFriendsDialog() {
    _uiState.update { it.copy(showInviteFriendsDialog = true) }
    loadFriendsForInvites()
  }

  /** Closes the invite friends dialog and clears any pending selection/errors. */
  fun hideInviteFriendsDialog() {
    _uiState.update {
      it.copy(
          showInviteFriendsDialog = false,
          invitedFriendIds = emptySet(),
          initialInvitedFriendIds = emptySet(),
          friendsErrorRes = null,
          isInvitingFriends = false)
    }
  }

  /**
   * Toggles whether a friend is selected to be invited (or uninvited) in the dialog.
   *
   * @param friendId The UID of the friend being toggled.
   */
  fun toggleFriendInvitation(friendId: String) {
    _uiState.update { state ->
      val updated =
          if (state.invitedFriendIds.contains(friendId)) state.invitedFriendIds - friendId
          else state.invitedFriendIds + friendId
      state.copy(invitedFriendIds = updated)
    }
  }

  /**
   * Sends additions/removals of invitations for the current event. Only the owner is allowed. If
   * nothing changed, the dialog simply closes.
   */
  fun updateInvitationsForEvent() {
    val event = _uiState.value.event ?: return
    val currentUserId = AuthenticationProvider.currentUser
    if (currentUserId != event.ownerId) {
      _uiState.update { it.copy(friendsErrorRes = R.string.event_invite_owner_only) }
      return
    }
    val currentSelection = _uiState.value.invitedFriendIds
    val initialSelection = _uiState.value.initialInvitedFriendIds
    val toAdd = currentSelection - initialSelection
    val toRemove = initialSelection - currentSelection
    if (toAdd.isEmpty() && toRemove.isEmpty()) {
      _uiState.update { it.copy(showInviteFriendsDialog = false) }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isInvitingFriends = true, friendsErrorRes = null) }
      var hadError = false
      toAdd.forEach { friendId ->
        runCatching {
              eventRepository.addInvitationToEvent(event.uid, friendId, currentUserId)
              userRepository.addInvitationToUser(event.uid, friendId, currentUserId)
            }
            .onFailure { e ->
              android.util.Log.w("EventViewModel", "Failed to invite friend $friendId", e)
              hadError = true
              _uiState.update { state ->
                state.copy(
                    friendsErrorRes = state.friendsErrorRes ?: R.string.event_invite_send_failed)
              }
            }
      }
      toRemove.forEach { friendId ->
        runCatching {
              eventRepository.removeInvitationFromEvent(event.uid, friendId, currentUserId)
              userRepository.removeInvitation(event.uid, friendId)
            }
            .onFailure { e ->
              android.util.Log.w("EventViewModel", "Failed to remove invitation for $friendId", e)
              hadError = true
              _uiState.update { state ->
                state.copy(
                    friendsErrorRes = state.friendsErrorRes ?: R.string.event_invite_remove_failed)
              }
            }
      }
      _uiState.update {
        if (hadError) {
          it.copy(isInvitingFriends = false)
        } else {
          it.copy(
              isInvitingFriends = false,
              showInviteFriendsDialog = false,
              invitedFriendIds = currentSelection,
              initialInvitedFriendIds = currentSelection)
        }
      }
    }
  }

  /**
   * Loads event attendees, owner, and current user profile for display in the attendees tab.
   * Operates only if an event is already loaded.
   */
  fun fetchAttendees() {
    val event = uiState.value.event
    val currentUserUid = AuthenticationProvider.currentUser
    if (event != null) {
      _uiState.update { it.copy(isLoading = true) }

      viewModelScope.launch {
        val userList = mutableListOf<User>()
        val currentUser = userRepository.getUserById(currentUserUid)
        val ownerId = event.ownerId
        val owner = userRepository.getUserById(ownerId)

        eventRepository.getEventParticipants(event.uid).forEach { eventParticipant ->
          val participant = userRepository.getUserById(eventParticipant.uid)
          if (participant != null) userList.add(participant)
        }
        _uiState.update {
          it.copy(isLoading = false, attendees = userList, currentUser = currentUser, owner = owner)
        }
      }
    }
  }

  private fun loadFriendsForInvites() {
    val event = _uiState.value.event ?: return
    val currentUserUid = AuthenticationProvider.currentUser

    _uiState.update { it.copy(isLoadingFriends = true, friendsErrorRes = null) }
    viewModelScope.launch {
      try {
        val friendIds = friendsRepository.getFriends(currentUserUid)
        val invitedIds = eventRepository.getEventInvitations(event.uid).toSet()
        val friends =
            friendIds.mapNotNull { friendId ->
              runCatching { userRepository.getUserById(friendId) }
                  .onFailure { e ->
                    android.util.Log.w("EventViewModel", "Failed to fetch friend $friendId", e)
                  }
                  .getOrNull()
            }
        _uiState.update {
          it.copy(
              friends = friends,
              isLoadingFriends = false,
              friendsErrorRes = null,
              invitedFriendIds = invitedIds.intersect(friendIds.toSet()),
              initialInvitedFriendIds = invitedIds.intersect(friendIds.toSet()))
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoadingFriends = false, friendsErrorRes = R.string.event_invite_friends_error)
        }
      }
    }
  }
}

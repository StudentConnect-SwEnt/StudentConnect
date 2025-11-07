package com.github.se.studentconnect.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
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
    val participantCount: Int = 0,
    val isFull: Boolean = false
)

sealed class TicketValidationResult {
  data class Valid(val participantId: String) : TicketValidationResult()

  data class Invalid(val userId: String) : TicketValidationResult()

  data class Error(val message: String) : TicketValidationResult()
}

class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
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
            isFull = isFull)
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
}

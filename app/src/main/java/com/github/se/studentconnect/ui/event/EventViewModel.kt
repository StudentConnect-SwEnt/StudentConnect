package com.github.se.studentconnect.viewmodel

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
    val ticketValidationResult: TicketValidationResult? = null
)

sealed class TicketValidationResult {
  data class Valid(val participantId: String) : TicketValidationResult()

  data class Invalid(val userId: String) : TicketValidationResult()
}

class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EventUiState())
  val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

  fun fetchEvent(eventUid: String, isJoined: Boolean) {
    _uiState.update { it.copy(isLoading = true, isJoined = isJoined) }
    viewModelScope.launch {
      val fetchedEvent = eventRepository.getEvent(eventUid)
      _uiState.update { it.copy(event = fetchedEvent, isLoading = false) }
    }
  }

  fun leaveEvent(eventUid: String) {
    val currentUserUid = AuthenticationProvider.currentUser
    if (currentUserUid != null) {
      viewModelScope.launch {
        userRepository.leaveEvent(eventUid, currentUserUid)
        eventRepository.removeParticipantFromEvent(eventUid, currentUserUid)
        _uiState.update { it.copy(isJoined = false) }
      }
    }
  }

  fun joinEvent(eventUid: String) {
    val currentUserUid = AuthenticationProvider.currentUser
    if (currentUserUid != null) {
      viewModelScope.launch {
        userRepository.joinEvent(eventUid, currentUserUid)
        val eventParticipant = EventParticipant(currentUserUid)
        eventRepository.addParticipantToEvent(eventUid, eventParticipant)
        _uiState.update { it.copy(isJoined = true) }
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
          it.copy(ticketValidationResult = TicketValidationResult.Invalid(scannedUserId))
        }
      }
    }
  }

  fun clearValidationResult() {
    _uiState.update { it.copy(ticketValidationResult = null) }
  }
}

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
    val isJoined: Boolean = false
)

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
}

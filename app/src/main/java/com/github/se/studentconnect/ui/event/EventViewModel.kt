package com.github.se.studentconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI State for the EventView screen. */
data class EventUiState(val event: Event? = null, val isLoading: Boolean = true)

/**
 * ViewModel for the EventView screen.
 *
 * @param eventRepository The repository for event data.
 * @param userRepository The repository for user data.
 */
class EventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EventUiState())
  val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

  /**
   * Fetches the details of a specific event.
   *
   * @param eventUid The unique identifier of the event to fetch.
   */
  fun fetchEvent(eventUid: String) {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val fetchedEvent = eventRepository.getEvent(eventUid)
      _uiState.update { it.copy(event = fetchedEvent, isLoading = false) }
    }
  }

  /**
   * Allows the current user to leave an event.
   *
   * @param eventUid The unique identifier of the event to leave.
   */
  fun leaveEvent(eventUid: String) {
    val currentUserUid = Firebase.auth.currentUser?.uid
    if (currentUserUid != null) {
      viewModelScope.launch {
        userRepository.leaveEvent(eventUid, currentUserUid)
        eventRepository.removeParticipantFromEvent(eventUid, currentUserUid)
      }
    }
  }

  fun joinEvent(eventUid: String) {
    val currentUserUid = Firebase.auth.currentUser?.uid
    if (currentUserUid != null) {
      viewModelScope.launch {
        userRepository.joinEvent(eventUid, currentUserUid)
        val eventParticipant = EventParticipant(currentUserUid)
        eventRepository.addParticipantToEvent(eventUid, eventParticipant)
      }
    }
  }
}

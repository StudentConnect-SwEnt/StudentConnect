package com.github.se.studentconnect.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivitiesUiState(
    val events: List<Event> = emptyList(),
    val selectedTab: EventTab = EventTab.JoinedEvents
)

class ActivitiesViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(ActivitiesUiState())
  val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

  fun onTabSelected(tab: EventTab) {
    _uiState.update { it.copy(selectedTab = tab) }
  }

  fun refreshEvents(userUid: String) {
    viewModelScope.launch {
      val eventsList = userRepository.getJoinedEvents(userUid)
      _uiState.update { currentState ->
        currentState.copy(events = eventsList.map { eventRepository.getEvent(it) })
      }
    }
  }

  fun leaveEvent(eventUid: String) {
    viewModelScope.launch {
      userRepository.leaveEvent(eventUid, Firebase.auth.currentUser?.uid!!)
      eventRepository.removeParticipantFromEvent(eventUid, Firebase.auth.currentUser?.uid!!)
    }
  }
}

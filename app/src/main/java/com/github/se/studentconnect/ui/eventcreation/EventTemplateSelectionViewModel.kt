package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the event template selection screen.
 *
 * @property events List of events created by the current user
 * @property isLoading Whether the events are being loaded
 * @property errorMessage Optional error message if loading failed
 */
data class EventTemplateSelectionUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for the event template selection screen.
 *
 * This ViewModel loads all events created by the current user that can be used as templates for
 * creating new events.
 *
 * @property eventRepository Repository for fetching events
 */
class EventTemplateSelectionViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(EventTemplateSelectionUiState())
  val uiState: StateFlow<EventTemplateSelectionUiState> = _uiState.asStateFlow()

  /** Loads all events created by the current user. */
  fun loadUserEvents() {
    val currentUserId = Firebase.auth.currentUser?.uid ?: return

    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

    viewModelScope.launch {
      try {
        val events = eventRepository.getEventsByOwner(currentUserId)
        _uiState.value =
            _uiState.value.copy(
                events = events.sortedByDescending { it.start.toDate() }, isLoading = false)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = e.message ?: "Failed to load events")
      }
    }
  }
}

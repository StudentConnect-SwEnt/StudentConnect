package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.ui.screen.profile.EventFilter
import com.google.firebase.Timestamp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class JoinedEventsUiState(
    val allEvents: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: EventFilter = EventFilter.Past,
    val isLoading: Boolean = true
)

@OptIn(FlowPreview::class)
class JoinedEventsViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(JoinedEventsUiState())
  val uiState: StateFlow<JoinedEventsUiState> = _uiState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")

  init {
    setupDebouncedSearch()
  }

  // Fetches all events the current user has joined (both past and upcoming)
  fun loadJoinedEvents() {
    val currentUserId = AuthenticationProvider.currentUser ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      try {
        // Get all event IDs the user has joined
        val joinedEventIds = userRepository.getJoinedEvents(currentUserId)

        // Fetch all visible events to check if user is owner
        val allVisibleEvents = eventRepository.getAllVisibleEvents()
        val ownedEvents = allVisibleEvents.filter { it.ownerId == currentUserId }

        // Combine joined and owned event IDs
        val allEventIds = (joinedEventIds + ownedEvents.map { it.uid }).distinct()

        // Fetch all events
        val allEvents =
            allEventIds.mapNotNull { eventId ->
              try {
                eventRepository.getEvent(eventId)
              } catch (_: Exception) {
                null
              }
            }

        _uiState.update { it.copy(allEvents = allEvents, isLoading = false) }
        applyFilters()
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false) }
      }
    }
  }

  // Waits 300ms after typing stops before filtering to avoid laggy UI
  private fun setupDebouncedSearch() {
    viewModelScope.launch {
      _searchQuery.debounce(SEARCH_DEBOUNCE_MILLIS).collect { query ->
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
      }
    }
  }

  // Updates the search query (actual filtering happens after debounce delay)
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  // Switches between showing past or upcoming events
  fun updateFilter(filter: EventFilter) {
    _uiState.update { it.copy(selectedFilter = filter) }
    applyFilters()
  }

  // Filters events based on time (past/upcoming) and search query
  private fun applyFilters() {
    val currentState = _uiState.value
    val now = Timestamp.now()

    val filtered =
        currentState.allEvents
            .filter { event ->
              // Filter by time (Past or Upcoming)
              val endTime =
                  event.end
                      ?: run {
                        val cal = Calendar.getInstance()
                        cal.time = event.start.toDate()
                        cal.add(Calendar.HOUR_OF_DAY, 3)
                        Timestamp(cal.time)
                      }

              val isPastEvent = endTime <= now
              val matchesFilter =
                  when (currentState.selectedFilter) {
                    EventFilter.Past -> isPastEvent
                    EventFilter.Upcoming -> !isPastEvent
                  }

              matchesFilter
            }
            .filter { event ->
              // Filter by search query
              if (currentState.searchQuery.isBlank()) {
                true
              } else {
                event.title.contains(currentState.searchQuery, ignoreCase = true)
              }
            }
            .sortedByDescending { it.start } // Show newest events first

    _uiState.update { it.copy(filteredEvents = filtered) }
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MILLIS = 300L
  }
}

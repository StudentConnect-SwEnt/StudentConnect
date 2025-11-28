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
import java.util.Calendar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinedEventsUiState(
    val allEvents: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
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

  // Search query is separate from uiState so the TextField updates immediately
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  init {
    setupDebouncedSearch()
  }

  // Load all events the user has joined or created
  fun loadJoinedEvents() {
    val currentUserId = AuthenticationProvider.currentUser
    if (currentUserId.isEmpty()) return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      try {
        val joinedEventIds = userRepository.getJoinedEvents(currentUserId)

        // Also include events the user owns
        val allVisibleEvents = eventRepository.getAllVisibleEvents()
        val ownedEvents = allVisibleEvents.filter { it.ownerId == currentUserId }

        // Combine both lists and remove duplicates
        val allEventIds = (joinedEventIds + ownedEvents.map { it.uid }).distinct()

        // Fetch the actual event objects, skip any that fail to load
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

  // Wait 300ms after user stops typing before applying the search filter
  private fun setupDebouncedSearch() {
    viewModelScope.launch {
      _searchQuery.debounce(SEARCH_DEBOUNCE_MILLIS).collect { applyFilters() }
    }
  }

  // Update search query instantly (filtering happens after debounce)
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  // Switch between Past and Upcoming events
  fun updateFilter(filter: EventFilter) {
    _uiState.update { it.copy(selectedFilter = filter) }
    applyFilters()
  }

  // Apply both time-based and search-based filtering
  private fun applyFilters() {
    val currentState = _uiState.value
    val currentSearchQuery = _searchQuery.value
    val now = Timestamp.now()

    val filtered =
        currentState.allEvents
            .filter { event ->
              // If event has no end time, assume it lasts 3 hours
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
              // Show all events if search is empty, otherwise filter by title
              if (currentSearchQuery.isBlank()) {
                true
              } else {
                event.title.contains(currentSearchQuery, ignoreCase = true)
              }
            }
            .sortedByDescending { it.start }

    _uiState.update { it.copy(filteredEvents = filtered) }
  }

  companion object {
    private const val SEARCH_DEBOUNCE_MILLIS = 300L
  }
}

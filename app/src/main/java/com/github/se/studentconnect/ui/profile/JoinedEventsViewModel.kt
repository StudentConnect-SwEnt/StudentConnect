package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
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
    val isLoading: Boolean = true,
    val pinnedEventIds: List<String> = emptyList()
)

@OptIn(FlowPreview::class)
class JoinedEventsViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val targetUserId: String? = null
) : ViewModel() {

  private val _uiState = MutableStateFlow(JoinedEventsUiState())
  val uiState: StateFlow<JoinedEventsUiState> = _uiState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  // The user ID we're viewing events for (either targetUserId or current user)
  private val viewingUserId: String
    get() = targetUserId ?: AuthenticationProvider.currentUser

  // Whether we're viewing our own events (affects pinning functionality)
  private val isOwnProfile: Boolean
    get() = targetUserId == null || targetUserId == AuthenticationProvider.currentUser

  init {
    setupDebouncedSearch()
    if (isOwnProfile) {
      loadPinnedEventIds()
    }
  }

  // Clear the snackbar message after showing it to the user
  fun clearSnackbarMessage() {
    _snackbarMessage.value = null
  }

  // Load the list of pinned event IDs for the current user
  private fun loadPinnedEventIds() {
    if (viewingUserId.isEmpty()) return

    viewModelScope.launch {
      try {
        val pinnedIds = userRepository.getPinnedEvents(viewingUserId)
        android.util.Log.d(TAG, "Loaded pinned IDs: $pinnedIds")
        _uiState.update { it.copy(pinnedEventIds = pinnedIds) }
      } catch (e: Exception) {
        android.util.Log.e(TAG, "Failed to load pinned IDs", e)
      }
    }
  }

  // Load all events the user has joined or created
  fun loadJoinedEvents() {
    if (viewingUserId.isEmpty()) return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      try {
        val joinedEventIds = userRepository.getJoinedEvents(viewingUserId)

        if (isOwnProfile) {
          loadPinnedEventIds()
        }

        val allVisibleEvents = eventRepository.getAllVisibleEvents()
        val ownedEvents = allVisibleEvents.filter { it.ownerId == viewingUserId }

        // Combine joined and owned events, remove duplicates
        val allEventIds = (joinedEventIds + ownedEvents.map { it.uid }).distinct()

        // Fetch event details, skip any that fail to load
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

  // Wait 300ms after user stops typing before searching
  private fun setupDebouncedSearch() {
    viewModelScope.launch {
      _searchQuery.debounce(SEARCH_DEBOUNCE_MILLIS).collect { applyFilters() }
    }
  }

  // Update the search query
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  // Switch between Past and Upcoming event filters
  fun updateFilter(filter: EventFilter) {
    _uiState.update { it.copy(selectedFilter = filter) }
    applyFilters()
  }

  // Apply time-based and search filters to the event list
  private fun applyFilters() {
    val currentState = _uiState.value
    val currentSearchQuery = _searchQuery.value
    val now = Timestamp.now()

    val filtered =
        currentState.allEvents
            .filter { event ->
              // If no end time, assume event lasts 3 hours
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
              // Filter by title if search query is not empty
              if (currentSearchQuery.isBlank()) {
                true
              } else {
                event.title.contains(currentSearchQuery, ignoreCase = true)
              }
            }
            .sortedByDescending { it.start }

    _uiState.update { it.copy(filteredEvents = filtered) }
  }

  // Pin or unpin an event (only available for own profile)
  fun togglePinEvent(eventId: String, maxPinnedMessage: String) {
    // Only allow pinning on own profile
    if (!isOwnProfile) return
    if (viewingUserId.isEmpty()) return

    viewModelScope.launch {
      try {
        val currentPinnedIds = _uiState.value.pinnedEventIds
        val isPinned = currentPinnedIds.contains(eventId)

        if (isPinned) {
          val newPinnedIds = currentPinnedIds - eventId
          _uiState.update { it.copy(pinnedEventIds = newPinnedIds) }
          userRepository.removePinnedEvent(viewingUserId, eventId)
        } else {
          // Check if user already has 3 pinned events
          if (currentPinnedIds.size >= MAX_PINNED_EVENTS) {
            _snackbarMessage.value = maxPinnedMessage
            return@launch
          }
          val newPinnedIds = currentPinnedIds + eventId
          _uiState.update { it.copy(pinnedEventIds = newPinnedIds) }
          userRepository.addPinnedEvent(viewingUserId, eventId)
        }
      } catch (e: Exception) {
        android.util.Log.e(TAG, "Failed to toggle pin", e)
        // Reload pinned IDs to sync with server
        loadPinnedEventIds()
      }
    }
  }

  companion object {
    private const val TAG = "JoinedEventsVM"
    private const val SEARCH_DEBOUNCE_MILLIS = 300L
    private const val MAX_PINNED_EVENTS = 3
  }
}

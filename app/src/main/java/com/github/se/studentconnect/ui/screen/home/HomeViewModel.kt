package com.github.se.studentconnect.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.ui.utils.FilterData
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePageUiState(
    val subscribedEventsStories: Map<Event, Pair<Int, Int>> = emptyMap(),
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val isCalendarVisible: Boolean = false,
    val selectedDate: Date? = null,
    val scrollToDate: Date? = null,
    val showOnlyFavorites: Boolean = false,
)

class HomePageViewModel
@Inject
constructor(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    // maybe will be used after for recommendations
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUiState())
  val uiState: StateFlow<HomePageUiState> = _uiState.asStateFlow()

  private val _favoriteEventIds = MutableStateFlow<Set<String>>(emptySet())
  val favoriteEventIds: StateFlow<Set<String>> = _favoriteEventIds.asStateFlow()

  private val currentUserId: String? = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }

  private var currentFilters: FilterData =
      FilterData(
          categories = emptyList(),
          location = null,
          radiusKm = 10f,
          priceRange = 0f..100f,
          showOnlyFavorites = false)

  private var allFetchedEvents: List<Event> = emptyList()

  init {
    loadAllEvents()
    loadFavoriteEvents()
    loadAllSubscribedEventsStories()
  }

  private fun loadAllSubscribedEventsStories() {
    viewModelScope.launch {
      try {
        // Get all visible events to show stories for
        val allEvents = eventRepository.getAllVisibleEvents()

        val allEventsStory = mutableMapOf<Event, Pair<Int, Int>>()
        // Create stories with Pair(totalStories, seenStories)
        // For demo: take first 10 events, mix of seen and unseen stories
        allEvents.take(10).forEachIndexed { index, event ->
          val totalStories = if (index < 3) 3 else 5
          val seenStories = if (index == 0) 1 else 0 // First one is partially seen
          allEventsStory[event] = Pair(totalStories, seenStories)
        }

        Log.d("HomePageViewModel", "Loaded ${allEventsStory.size} stories")

        _uiState.update { it.copy(subscribedEventsStories = allEventsStory) }
      } catch (e: Exception) {
        Log.e("HomePageViewModel", "Error loading stories", e)
        _uiState.update { it.copy(subscribedEventsStories = emptyMap()) }
      }
    }
  }

  private fun loadAllEvents() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      try {
        allFetchedEvents = eventRepository.getAllVisibleEvents()
        applyFilters(currentFilters, allFetchedEvents)
      } catch (e: Exception) {
        Log.e("HomePageViewModel", "Error loading all events", e)
        allFetchedEvents = emptyList()
        _uiState.update { it.copy(isLoading = false, events = emptyList()) }
      }
    }
  }

  private fun loadFavoriteEvents() {
    currentUserId?.let { uid ->
      viewModelScope.launch {
        try {
          val favorites = userRepository.getFavoriteEvents(uid)
          val favoriteSet = favorites.toSet()
          if (_favoriteEventIds.value != favoriteSet) {
            _favoriteEventIds.value = favoriteSet
            if (currentFilters.showOnlyFavorites) {
              applyFilters(currentFilters, allFetchedEvents)
            }
          }
        } catch (e: Exception) {
          Log.e("HomePageViewModel", "Error loading favorite events", e)
        }
      }
    }
  }

  fun updateSeenStories(event: Event, seenIndex: Int) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      val stories = _uiState.value.subscribedEventsStories

      stories[event]?.first?.let { i ->
        if (i >= seenIndex) {
          stories[event]?.second?.let { j ->
            if (j < seenIndex) {
              val subscribedEventsStoryUpdate = stories.toMutableMap()
              subscribedEventsStoryUpdate.replace(event, Pair(i, j))
              _uiState.update {
                it.copy(subscribedEventsStories = subscribedEventsStoryUpdate, isLoading = false)
              }
            }
          }
        }
      }
    }
  }

  fun toggleFavorite(eventId: String) {
    currentUserId?.let { uid ->
      viewModelScope.launch {
        var didAdd = false
        _favoriteEventIds.update { current ->
          return@update if (current.contains(eventId)) {
            didAdd = false
            current - eventId
          } else {
            didAdd = true
            current + eventId
          }
        }

        try {
          if (didAdd) userRepository.addFavoriteEvent(uid, eventId)
          else userRepository.removeFavoriteEvent(uid, eventId)

          if (currentFilters.showOnlyFavorites) {
            applyFilters(currentFilters, allFetchedEvents)
          }
        } catch (e: Exception) {
          _favoriteEventIds.update { current ->
            return@update if (didAdd) current - eventId else current + eventId
          }
          Log.e("HomePageViewModel", "Error toggling favorite status for event $eventId", e)
        }
      }
    }
  }

  fun applyFilters(filters: FilterData) {
    currentFilters = filters
    applyFilters(filters, allFetchedEvents)
  }

  private fun applyFilters(filters: FilterData, eventsToFilter: List<Event>) {
    _uiState.update { it.copy(isLoading = true) }

    val currentTime = Date()

    val filtered =
        eventsToFilter.filter { event ->
          val publicEvent = event as? Event.Public

          // Temporality: only show future or LIVE events
          val eventEndTime = event.end?.toDate() ?: event.start.toDate()
          val isFutureOrLive = eventEndTime.after(currentTime) || eventEndTime == currentTime
          if (!isFutureOrLive) return@filter false

          // Favorites
          val favoriteMatch =
              if (filters.showOnlyFavorites) {
                event.uid in _favoriteEventIds.value
              } else {
                true
              }
          if (!favoriteMatch) return@filter false

          // Tags
          val tagMatch =
              if (filters.categories.isEmpty()) {
                true
              } else {
                val eventTags = publicEvent?.tags ?: emptyList()
                eventTags.any { eventTag ->
                  filters.categories.any { selectedTag ->
                    eventTag.equals(selectedTag, ignoreCase = true)
                  }
                }
              }
          if (!tagMatch) return@filter false

          // Price
          val priceMatch =
              if (publicEvent?.participationFee == null || publicEvent.participationFee == 0u) {
                filters.priceRange.start <= 0f
              } else {
                val fee = publicEvent.participationFee.toFloat()
                fee >= filters.priceRange.start && fee <= filters.priceRange.endInclusive
              }
          if (!priceMatch) return@filter false

          val locationMatch =
              if (event.location == null) {
                filters.radiusKm >= 100f
              } else {
                if (filters.location == null) return@filter true
                val distance = calculateHaversineDistance(event.location!!, filters.location)
                distance <= filters.radiusKm
              }

          locationMatch
        }
    _uiState.update {
      it.copy(events = filtered, isLoading = false, showOnlyFavorites = filters.showOnlyFavorites)
    }
  }

  fun toggleFavoritesFilter() {
    val newFilters = currentFilters.copy(showOnlyFavorites = !currentFilters.showOnlyFavorites)
    applyFilters(newFilters)
  }

  private fun calculateHaversineDistance(loc1: Location, loc2: Location): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
    val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
    val lat1Rad = Math.toRadians(loc1.latitude)
    val lat2Rad = Math.toRadians(loc2.latitude)
    val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1Rad) * cos(lat2Rad)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
  }

  fun getAvailableFilters(): List<String> = Activities.filterOptions

  fun refresh() {
    loadAllEvents()
    loadFavoriteEvents()
    loadAllSubscribedEventsStories()
  }

  /** Shows the calendar modal. */
  fun showCalendar() {
    _uiState.update { it.copy(isCalendarVisible = true) }
  }

  /** Hides the calendar modal. */
  fun hideCalendar() {
    _uiState.update { it.copy(isCalendarVisible = false) }
  }

  /**
   * Handles date selection from the calendar. Closes the calendar and sets the scroll target date.
   */
  fun onDateSelected(date: Date) {
    _uiState.update { it.copy(selectedDate = date, scrollToDate = date, isCalendarVisible = false) }
  }

  /** Clears the scroll target date after scrolling is complete. */
  fun clearScrollTarget() {
    _uiState.update { it.copy(scrollToDate = null) }
  }

  /** Gets events for a specific date. */
  fun getEventsForDate(date: Date): List<Event> {
    val calendar = Calendar.getInstance()
    calendar.time = date

    return _uiState.value.events.filter { event ->
      val eventCalendar = Calendar.getInstance()
      eventCalendar.time = event.start.toDate()

      eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
          eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
          eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
    }
  }
}

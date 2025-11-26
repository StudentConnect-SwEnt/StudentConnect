package com.github.se.studentconnect.ui.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.LocationRepository
import com.github.se.studentconnect.repository.LocationRepositoryImpl
import com.github.se.studentconnect.repository.LocationResult
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

/** Tab modes available for home screen content filtering. */
enum class HomeTabMode {
  FOR_YOU,
  EVENTS,
  DISCOVER
}

/** User preferences for event scoring. */
data class UserPreferences(
    val preferredLocation: Location? = null,
    val preferredPriceRange: ClosedFloatingPointRange<Float> = 0f..100f,
    val preferredTimeOfDay: PreferredTimeOfDay = PreferredTimeOfDay.ANY
)

/** Preferred time of day for events. */
enum class PreferredTimeOfDay {
  MORNING, // 6-12
  AFTERNOON, // 12-18
  EVENING, // 18-24
  ANY
}

/** Snapshot of everything the Home screen needs to render. */
data class HomePageUiState(
    val subscribedEventsStories: Map<Event, Pair<Int, Int>> = emptyMap(),
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val isCalendarVisible: Boolean = false,
    val selectedDate: Date? = null,
    val scrollToDate: Date? = null,
    val showOnlyFavorites: Boolean = false,
    val selectedTab: HomeTabMode = HomeTabMode.FOR_YOU,
    val userHobbies: List<String> = emptyList(),
    val attendedEvents: List<Event> = emptyList(),
    val userPreferences: UserPreferences = UserPreferences()
)

/** Coordinates event loading, filtering, favorite handling, and story progress. */
class HomePageViewModel
@Inject
constructor(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val context: Context? = null,
    private val locationRepository: LocationRepository? = null
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
    loadUserHobbies()
    loadUserAttendedEvents()
    loadUserPreferences()
    loadAllEvents()
    loadFavoriteEvents()
    loadAllSubscribedEventsStories()
  }

  private fun loadUserHobbies() {
    currentUserId?.let { uid ->
      viewModelScope.launch {
        try {
          val user = userRepository.getUserById(uid)
          val hobbies = user?.hobbies ?: emptyList()
          _uiState.update { it.copy(userHobbies = hobbies) }
          Log.d("HomePageViewModel", "Loaded user hobbies: $hobbies")
        } catch (e: Exception) {
          Log.e("HomePageViewModel", "Error loading user hobbies", e)
          _uiState.update { it.copy(userHobbies = emptyList()) }
        }
      }
    }
  }

  private fun loadUserAttendedEvents() {
    currentUserId?.let { uid ->
      viewModelScope.launch {
        try {
          val attendedEventIds = userRepository.getJoinedEvents(uid)
          val attendedEvents =
              attendedEventIds.mapNotNull { eventId ->
                try {
                  eventRepository.getEvent(eventId)
                } catch (e: Exception) {
                  Log.e("HomePageViewModel", "Error loading attended event $eventId", e)
                  null
                }
              }
          _uiState.update { it.copy(attendedEvents = attendedEvents) }
          Log.d("HomePageViewModel", "Loaded ${attendedEvents.size} attended events")
        } catch (e: Exception) {
          Log.e("HomePageViewModel", "Error loading attended events", e)
          _uiState.update { it.copy(attendedEvents = emptyList()) }
        }
      }
    }
  }

  private fun loadUserPreferences() {
    currentUserId?.let { _ ->
      viewModelScope.launch {
        try {
          // Fetch user's current location if available
          val userLocation =
              if (context != null) {
                val locRepo = locationRepository ?: LocationRepositoryImpl(context)
                when (val result = locRepo.getCurrentLocation()) {
                  is LocationResult.Success -> {
                    val androidLocation = result.location
                    Location(
                        latitude = androidLocation.latitude,
                        longitude = androidLocation.longitude,
                        name = "Current Location")
                  }
                  else -> {
                    Log.d(
                        "HomePageViewModel",
                        "Could not get location for scoring: ${result.javaClass.simpleName}")
                    null
                  }
                }
              } else {
                null
              }

          val preferences =
              UserPreferences(
                  preferredLocation = userLocation,
                  preferredPriceRange = 0f..100f,
                  preferredTimeOfDay = PreferredTimeOfDay.ANY)
          _uiState.update { it.copy(userPreferences = preferences) }
          Log.d(
              "HomePageViewModel", "Loaded user preferences with location: ${userLocation != null}")
        } catch (e: Exception) {
          Log.e("HomePageViewModel", "Error loading user preferences", e)
          _uiState.update { it.copy(userPreferences = UserPreferences()) }
        }
      }
    }
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

  /** Persists seen story progress for the provided event. */
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

  /** Optimistically toggles the favorite state of an event and syncs it with the repository. */
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

  /** Applies the given filters to the cached events and refreshes the UI state. */
  fun applyFilters(filters: FilterData) {
    currentFilters = filters
    applyFilters(filters, allFetchedEvents)
  }

  private fun applyFilters(filters: FilterData, eventsToFilter: List<Event>) {
    _uiState.update { it.copy(isLoading = true) }

    val currentTime = Date()
    val currentTab = _uiState.value.selectedTab
    val userHobbies = _uiState.value.userHobbies

    val filtered =
        eventsToFilter.filter { event ->
          // Only show public events on home page
          val publicEvent = event as? Event.Public ?: return@filter false

          // Temporality: only show future or LIVE events
          val eventEndTime = event.end?.toDate() ?: event.start.toDate()
          val isFutureOrLive = eventEndTime.after(currentTime) || eventEndTime == currentTime
          if (!isFutureOrLive) return@filter false

          // Tab-based filtering (before other filters)
          val tabMatch =
              when (currentTab) {
                HomeTabMode.FOR_YOU -> {
                  // Show events that have at least one tag matching user hobbies
                  if (userHobbies.isEmpty()) {
                    true // If user has no hobbies, show all events
                  } else {
                    val eventTags = publicEvent.tags
                    eventTags.any { eventTag ->
                      userHobbies.any { hobby -> eventTag.equals(hobby, ignoreCase = true) }
                    }
                  }
                }
                HomeTabMode.EVENTS -> {
                  // Show all events
                  true
                }
                HomeTabMode.DISCOVER -> {
                  // Show events that DON'T have any tags matching user hobbies
                  if (userHobbies.isEmpty()) {
                    true // If user has no hobbies, show all events
                  } else {
                    val eventTags = publicEvent.tags
                    eventTags.none { eventTag ->
                      userHobbies.any { hobby -> eventTag.equals(hobby, ignoreCase = true) }
                    }
                  }
                }
              }
          if (!tabMatch) return@filter false

          // Favorites
          val favoriteMatch =
              if (filters.showOnlyFavorites) {
                event.uid in _favoriteEventIds.value
              } else {
                true
              }
          if (!favoriteMatch) return@filter false

          // Tags (from filter bar)
          val tagMatch =
              if (filters.categories.isEmpty()) {
                true
              } else {
                val eventTags = publicEvent.tags
                eventTags.any { eventTag ->
                  filters.categories.any { selectedTag ->
                    eventTag.equals(selectedTag, ignoreCase = true)
                  }
                }
              }
          if (!tagMatch) return@filter false

          // Price
          val priceMatch =
              if (publicEvent.participationFee == null || publicEvent.participationFee == 0u) {
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

    // Sort events based on current tab
    val sortedEvents =
        if (currentTab == HomeTabMode.FOR_YOU) {
          // Sort by recommendation score (highest first)
          filtered.sortedByDescending { event -> calculateEventScore(event) }
        } else {
          // For other tabs, sort by start date
          filtered.sortedBy { it.start.toDate() }
        }

    _uiState.update {
      it.copy(
          events = sortedEvents, isLoading = false, showOnlyFavorites = filters.showOnlyFavorites)
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

  /**
   * Calculates a recommendation score for an event based on user preferences.
   *
   * Score formula: 0.35 * tag_similarity + 0.10 * previous_attended_event_similarity + 0.15 *
   * distance_score + 0.15 * price_preference + 0.10 * time_match + 0.15 * recency_boost
   *
   * @return A score between 0.0 and 1.0
   */
  private fun calculateEventScore(event: Event): Double {
    val publicEvent = event as? Event.Public ?: return 0.0

    val tagSimilarity = calculateTagSimilarity(publicEvent)
    val attendedSimilarity = calculateAttendedEventSimilarity(publicEvent)
    val distanceScore = calculateDistanceScore(publicEvent)
    val pricePreference = calculatePricePreference(publicEvent)
    val timeMatch = calculateTimeMatch(publicEvent)
    val recencyBoost = calculateRecencyBoost(publicEvent)

    val totalScore =
        0.35 * tagSimilarity +
            0.10 * attendedSimilarity +
            0.15 * distanceScore +
            0.15 * pricePreference +
            0.10 * timeMatch +
            0.15 * recencyBoost

    Log.d(
        "EventScore",
        "Event: ${publicEvent.title} | Score: ${"%.3f".format(totalScore)} | " +
            "Tag: ${"%.2f".format(tagSimilarity)}, " +
            "Attended: ${"%.2f".format(attendedSimilarity)}, " +
            "Distance: ${"%.2f".format(distanceScore)}, " +
            "Price: ${"%.2f".format(pricePreference)}, " +
            "Time: ${"%.2f".format(timeMatch)}, " +
            "Recency: ${"%.2f".format(recencyBoost)}")

    return totalScore
  }

  /** Calculates tag similarity score (0.0 to 1.0) based on matching user hobbies. */
  private fun calculateTagSimilarity(event: Event.Public): Double {
    val userHobbies = _uiState.value.userHobbies
    if (userHobbies.isEmpty() || event.tags.isEmpty()) return 0.5

    val matchingTags =
        event.tags.count { eventTag ->
          userHobbies.any { hobby -> eventTag.equals(hobby, ignoreCase = true) }
        }
    return (matchingTags.toDouble() / userHobbies.size.coerceAtLeast(1)).coerceIn(0.0, 1.0)
  }

  /**
   * Calculates similarity to previously attended events (0.0 to 1.0) based on shared tags and
   * categories.
   */
  private fun calculateAttendedEventSimilarity(event: Event.Public): Double {
    val attendedEvents = _uiState.value.attendedEvents
    if (attendedEvents.isEmpty()) return 0.5

    val attendedTags =
        attendedEvents.flatMap { attendedEvent ->
          (attendedEvent as? Event.Public)?.tags ?: emptyList()
        }
    if (attendedTags.isEmpty()) return 0.5

    val matchingTags =
        event.tags.count { eventTag -> attendedTags.any { it.equals(eventTag, ignoreCase = true) } }
    return (matchingTags.toDouble() / attendedTags.distinct().size.coerceAtLeast(1)).coerceIn(
        0.0, 1.0)
  }

  /** Calculates distance score (0.0 to 1.0), where 1.0 is closest. */
  private fun calculateDistanceScore(event: Event.Public): Double {
    val userLocation = _uiState.value.userPreferences.preferredLocation
    val eventLocation = event.location

    if (userLocation == null || eventLocation == null) return 0.5

    val distance = calculateHaversineDistance(userLocation, eventLocation)
    val maxDistance = 50.0 // 50km max distance for scoring
    return (1.0 - (distance / maxDistance).coerceIn(0.0, 1.0))
  }

  /** Calculates price preference score (0.0 to 1.0) based on user's price range. */
  private fun calculatePricePreference(event: Event.Public): Double {
    val priceRange = _uiState.value.userPreferences.preferredPriceRange
    val eventPrice = event.participationFee?.toFloat() ?: 0f

    return when {
      eventPrice == 0f && priceRange.start <= 0f -> 1.0
      eventPrice in priceRange -> 1.0
      eventPrice < priceRange.start -> {
        val diff = priceRange.start - eventPrice
        (1.0 - (diff / priceRange.start)).coerceIn(0.0, 1.0)
      }
      else -> {
        val diff = eventPrice - priceRange.endInclusive
        (1.0 - (diff / priceRange.endInclusive)).coerceIn(0.0, 1.0)
      }
    }
  }

  /** Calculates time match score (0.0 to 1.0) based on preferred time of day. */
  private fun calculateTimeMatch(event: Event.Public): Double {
    val preferredTime = _uiState.value.userPreferences.preferredTimeOfDay
    if (preferredTime == PreferredTimeOfDay.ANY) return 1.0

    val calendar = Calendar.getInstance()
    calendar.time = event.start.toDate()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

    return when (preferredTime) {
      PreferredTimeOfDay.MORNING -> if (hourOfDay in 6..11) 1.0 else 0.3
      PreferredTimeOfDay.AFTERNOON -> if (hourOfDay in 12..17) 1.0 else 0.3
      PreferredTimeOfDay.EVENING -> if (hourOfDay in 18..23) 1.0 else 0.3
      PreferredTimeOfDay.ANY -> 1.0
    }
  }

  /**
   * Calculates recency boost score (0.0 to 1.0), favoring events happening sooner.
   *
   * Uses event start time instead of creation time. Events happening in the next 30 days get higher
   * scores, with events happening sooner getting the highest scores.
   */
  private fun calculateRecencyBoost(event: Event.Public): Double {
    val now = System.currentTimeMillis()
    val eventStartTime = event.start.toDate().time

    // Calculate days until event starts (can be negative if event is in the past)
    val daysUntilEvent = ((eventStartTime - now) / (1000.0 * 60 * 60 * 24))

    // For events happening within 30 days, give higher scores to sooner events
    val maxDays = 30.0
    return when {
      daysUntilEvent < 0 -> 0.0 // Past events get 0
      daysUntilEvent <= maxDays -> (1.0 - (daysUntilEvent / maxDays)).coerceIn(0.0, 1.0)
      else -> 0.0 // Events more than 30 days away get 0
    }
  }

  /** Returns the static list of available filter chips for the UI. */
  fun getAvailableFilters(): List<String> = Activities.filterOptions

  /** Reloads events, favorites, and story data. */
  fun refresh() {
    loadUserHobbies()
    loadUserAttendedEvents()
    loadUserPreferences()
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

  /** Updates the selected tab mode and reapplies filters. */
  fun selectTab(tab: HomeTabMode) {
    _uiState.update { it.copy(selectedTab = tab) }
    // Reapply filters with the new tab selection
    applyFilters(currentFilters, allFetchedEvents)
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

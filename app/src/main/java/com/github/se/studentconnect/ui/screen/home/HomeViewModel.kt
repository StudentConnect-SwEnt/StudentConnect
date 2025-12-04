package com.github.se.studentconnect.ui.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.activities.Activities
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.map.LocationRepository
import com.github.se.studentconnect.model.map.LocationRepositoryImpl
import com.github.se.studentconnect.model.map.LocationResult
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.toOrganizationDataList
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
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

/** Story with user information for display. */
data class StoryWithUser(
    val story: com.github.se.studentconnect.model.story.Story,
    val username: String,
    val userId: String
)

/** Snapshot of everything the Home screen needs to render. */
data class HomePageUiState(
    val subscribedEventsStories: Map<Event, Pair<Int, Int>> = emptyMap(),
    val eventStories: Map<String, List<StoryWithUser>> =
        emptyMap(), // eventId -> list of stories with user info
    val events: List<Event> = emptyList(),
    val organizations: List<OrganizationData> = emptyList(),
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
    private val locationRepository: LocationRepository? = null,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val storyRepository: StoryRepository? = null
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

  companion object {
    // Scoring weights
    private const val WEIGHT_TAG_SIMILARITY = 0.35
    private const val WEIGHT_ATTENDED_SIMILARITY = 0.10
    private const val WEIGHT_DISTANCE = 0.15
    private const val WEIGHT_PRICE = 0.15
    private const val WEIGHT_TIME = 0.10
    private const val WEIGHT_RECENCY = 0.15

    // Score defaults
    private const val DEFAULT_SCORE = 0.5
    private const val MIN_SCORE = 0.0
    private const val MAX_SCORE = 1.0

    // Distance scoring
    private const val MAX_DISTANCE_KM = 50.0

    // Time matching
    private const val TIME_NON_MATCH_SCORE = 0.3

    // Recency scoring
    private const val MAX_RECENCY_DAYS = 30.0
  }

  init {
    loadUserHobbies()
    loadUserAttendedEvents()
    loadUserPreferences()
    loadAllEvents()
    loadFavoriteEvents()
    loadAllSubscribedEventsStories(showLoading = true)
    loadOrganizations()
  }

  /** Loads organization suggestions from the repository. */
  private fun loadOrganizations() {
    viewModelScope.launch {
      try {
        val organizations = organizationRepository.getAllOrganizations()
        val organizationDataList = organizations.toOrganizationDataList()
        _uiState.update { it.copy(organizations = organizationDataList) }
      } catch (e: Exception) {
        Log.e("HomePageViewModel", "Error loading organizations", e)
        _uiState.update { it.copy(organizations = emptyList()) }
      }
    }
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

  private fun loadAllSubscribedEventsStories(showLoading: Boolean = false) {
    viewModelScope.launch {
      try {
        // Only show loading state if explicitly requested (e.g., initial load)
        if (showLoading) {
          _uiState.update { it.copy(isLoading = true) }
        }

        if (storyRepository == null || context == null) {
          Log.w(
              "HomePageViewModel", "StoryRepository or context not available, using empty stories")
          _uiState.update {
            it.copy(
                subscribedEventsStories = emptyMap(), eventStories = emptyMap(), isLoading = false)
          }
          return@launch
        }

        // Get user's joined events to show stories for
        val currentUser = currentUserId
        if (currentUser == null) {
          Log.w("HomePageViewModel", "No current user, cannot load stories")
          _uiState.update {
            it.copy(
                subscribedEventsStories = emptyMap(), eventStories = emptyMap(), isLoading = false)
          }
          return@launch
        }

        val joinedEvents = storyRepository.getUserJoinedEvents(currentUser)
        Log.d("HomePageViewModel", "User has joined ${joinedEvents.size} events")

        val allEventsStory = mutableMapOf<Event, Pair<Int, Int>>()
        val eventStoriesMap = mutableMapOf<String, List<StoryWithUser>>()

        // For each joined event, get its stories
        for (event in joinedEvents) {
          val stories = storyRepository.getEventStories(event.uid)
          if (stories.isNotEmpty()) {
            // Pair(seenStories, totalStories)
            // For now, all stories are marked as unseen (seenStories = 0)
            // You can implement tracking of seen stories later
            allEventsStory[event] = Pair(0, stories.size)

            // Fetch user information for each story
            val storiesWithUsers =
                stories.mapNotNull { story ->
                  try {
                    val user = userRepository.getUserById(story.userId)
                    if (user != null) {
                      StoryWithUser(story = story, username = user.username, userId = user.userId)
                    } else {
                      Log.w("HomePageViewModel", "User not found for story ${story.storyId}")
                      null
                    }
                  } catch (e: Exception) {
                    Log.e("HomePageViewModel", "Error fetching user for story ${story.storyId}", e)
                    null
                  }
                }

            eventStoriesMap[event.uid] = storiesWithUsers

            Log.d(
                "HomePageViewModel",
                "Event ${event.title} has ${stories.size} stories from ${storiesWithUsers.size} users")
          }
        }

        Log.d("HomePageViewModel", "Loaded stories for ${allEventsStory.size} events")
        Log.d("HomePageViewModel", "EventStories map size: ${eventStoriesMap.size}")
        Log.d("HomePageViewModel", "EventStories keys: ${eventStoriesMap.keys.joinToString()}")

        _uiState.update {
          it.copy(
              subscribedEventsStories = allEventsStory,
              eventStories = eventStoriesMap,
              isLoading = false)
        }
      } catch (e: Exception) {
        Log.e("HomePageViewModel", "Error loading stories", e)
        _uiState.update {
          it.copy(
              subscribedEventsStories = emptyMap(), eventStories = emptyMap(), isLoading = false)
        }
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
                  // FOR_YOU shows all events, sorted by recommendation score
                  // Don't filter here - let the scoring algorithm rank everything
                  true
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
                filters.location == null || filters.radiusKm >= 100f
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
        WEIGHT_TAG_SIMILARITY * tagSimilarity +
            WEIGHT_ATTENDED_SIMILARITY * attendedSimilarity +
            WEIGHT_DISTANCE * distanceScore +
            WEIGHT_PRICE * pricePreference +
            WEIGHT_TIME * timeMatch +
            WEIGHT_RECENCY * recencyBoost

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
    if (userHobbies.isEmpty() || event.tags.isEmpty()) return DEFAULT_SCORE

    val matchingTags =
        event.tags.count { eventTag ->
          userHobbies.any { hobby -> eventTag.equals(hobby, ignoreCase = true) }
        }
    return (matchingTags.toDouble() / userHobbies.size.coerceAtLeast(1)).coerceIn(
        MIN_SCORE, MAX_SCORE)
  }

  /**
   * Calculates similarity to previously attended events (0.0 to 1.0) based on shared tags and
   * categories.
   */
  private fun calculateAttendedEventSimilarity(event: Event.Public): Double {
    val attendedEvents = _uiState.value.attendedEvents
    if (attendedEvents.isEmpty()) return DEFAULT_SCORE

    val attendedTags =
        attendedEvents.flatMap { attendedEvent ->
          (attendedEvent as? Event.Public)?.tags ?: emptyList()
        }
    if (attendedTags.isEmpty()) return DEFAULT_SCORE

    val matchingTags =
        event.tags.count { eventTag -> attendedTags.any { it.equals(eventTag, ignoreCase = true) } }
    return (matchingTags.toDouble() / attendedTags.distinct().size.coerceAtLeast(1)).coerceIn(
        MIN_SCORE, MAX_SCORE)
  }

  /** Calculates distance score (0.0 to 1.0), where 1.0 is closest. */
  private fun calculateDistanceScore(event: Event.Public): Double {
    val userLocation = _uiState.value.userPreferences.preferredLocation
    val eventLocation = event.location

    if (userLocation == null || eventLocation == null) return DEFAULT_SCORE

    val distance = calculateHaversineDistance(userLocation, eventLocation)
    return (MAX_SCORE - (distance / MAX_DISTANCE_KM).coerceIn(MIN_SCORE, MAX_SCORE))
  }

  /** Calculates price preference score (0.0 to 1.0) based on user's price range. */
  private fun calculatePricePreference(event: Event.Public): Double {
    val priceRange = _uiState.value.userPreferences.preferredPriceRange
    val eventPrice = event.participationFee?.toFloat() ?: 0f

    return when {
      eventPrice == 0f && priceRange.start <= 0f -> MAX_SCORE
      eventPrice in priceRange -> MAX_SCORE
      eventPrice < priceRange.start -> {
        val diff = priceRange.start - eventPrice
        (MAX_SCORE - (diff / priceRange.start)).coerceIn(MIN_SCORE, MAX_SCORE)
      }
      else -> {
        val diff = eventPrice - priceRange.endInclusive
        (MAX_SCORE - (diff / priceRange.endInclusive)).coerceIn(MIN_SCORE, MAX_SCORE)
      }
    }
  }

  /** Calculates time match score (0.0 to 1.0) based on preferred time of day. */
  private fun calculateTimeMatch(event: Event.Public): Double {
    val preferredTime = _uiState.value.userPreferences.preferredTimeOfDay
    if (preferredTime == PreferredTimeOfDay.ANY) return MAX_SCORE

    val calendar = Calendar.getInstance()
    calendar.time = event.start.toDate()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

    return when (preferredTime) {
      PreferredTimeOfDay.MORNING -> if (hourOfDay in 6..11) MAX_SCORE else TIME_NON_MATCH_SCORE
      PreferredTimeOfDay.AFTERNOON -> if (hourOfDay in 12..17) MAX_SCORE else TIME_NON_MATCH_SCORE
      PreferredTimeOfDay.EVENING -> if (hourOfDay in 18..23) MAX_SCORE else TIME_NON_MATCH_SCORE
      PreferredTimeOfDay.ANY -> MAX_SCORE
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

    // For events happening within MAX_RECENCY_DAYS, give higher scores to sooner events
    return when {
      daysUntilEvent < MIN_SCORE -> MIN_SCORE // Past events get 0
      daysUntilEvent <= MAX_RECENCY_DAYS ->
          (MAX_SCORE - (daysUntilEvent / MAX_RECENCY_DAYS)).coerceIn(MIN_SCORE, MAX_SCORE)
      else -> MIN_SCORE // Events more than MAX_RECENCY_DAYS away get 0
    }
  }

  /** Returns the static list of available filter chips for the UI. */
  fun getAvailableFilters(): List<String> = Activities.filterOptions

  /** Reloads events, favorites, story data, and organizations. */
  fun refresh() {
    loadUserHobbies()
    loadUserAttendedEvents()
    loadUserPreferences()
    loadAllEvents()
    loadFavoriteEvents()
    loadAllSubscribedEventsStories(showLoading = true)
    loadOrganizations()
  }

  /** Reloads only story data without triggering a full refresh or loading spinner. */
  fun refreshStories() {
    loadAllSubscribedEventsStories(showLoading = false)
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

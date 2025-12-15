package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile screen showing user information, stats, and pinned events.
 *
 * @param userRepository Repository for user data operations
 * @param friendsRepository Repository for friends data operations
 * @param eventRepository Repository for event data operations
 * @param organizationRepository Repository for organization data operations
 * @param currentUserId The ID of the current user
 */
class ProfileScreenViewModel(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository,
    private val eventRepository: EventRepository,
    private val organizationRepository: OrganizationRepository,
    private val currentUserId: String
) : ViewModel() {

  companion object {
    private const val TAG = "ProfileScreenViewModel"
  }

  // User data state
  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  // Friends count state
  private val _friendsCount = MutableStateFlow(0)
  val friendsCount: StateFlow<Int> = _friendsCount.asStateFlow()

  // Events count state (joined events)
  private val _eventsCount = MutableStateFlow(0)
  val eventsCount: StateFlow<Int> = _eventsCount.asStateFlow()

  // Pinned events state
  private val _pinnedEvents = MutableStateFlow<List<Event>>(emptyList())
  val pinnedEvents: StateFlow<List<Event>> = _pinnedEvents.asStateFlow()

  // User's organizations state
  private val _userOrganizations = MutableStateFlow<List<Organization>>(emptyList())
  val userOrganizations: StateFlow<List<Organization>> = _userOrganizations.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadUserProfile()
    loadPinnedEvents()
    loadUserOrganizations()
  }

  // Load the user's profile data and stats
  fun loadUserProfile() {
    _isLoading.value = true
    viewModelScope.launch {
      try {
        // Load user data
        val loadedUser = userRepository.getUserById(currentUserId)
        _user.value = loadedUser

        // Load friends count
        loadFriendsCount()

        // Load events count
        loadEventsCount()

        _error.value = null
      } catch (exception: Exception) {
        Log.e(TAG, "Failed to load profile for user: $currentUserId", exception)
        _error.value = exception.message ?: ProfileConstants.ERROR_LOAD_PROFILE
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Loads the count of friends for the current user. */
  private suspend fun loadFriendsCount() {
    try {
      val friends = friendsRepository.getFriends(currentUserId)
      _friendsCount.value = friends.size
    } catch (exception: Exception) {
      Log.e(TAG, "Failed to load friends count for user: $currentUserId", exception)
      // If friends loading fails, set count to 0
      _friendsCount.value = 0
    }
  }

  /** Loads the count of events for the current user (both joined and created). */
  private suspend fun loadEventsCount() {
    // Get joined events (with error handling)
    val joinedEventIds =
        try {
          userRepository.getJoinedEvents(currentUserId)
        } catch (exception: Exception) {
          Log.e(TAG, "Failed to load joined events for user: $currentUserId", exception)
          emptyList()
        }

    // Get created events (with error handling)
    val createdEventIds =
        try {
          val createdEvents = eventRepository.getEventsByOrganization(currentUserId)
          createdEvents.map { it.uid }
        } catch (exception: Exception) {
          Log.e(TAG, "Failed to load created events for user: $currentUserId", exception)
          emptyList()
        }

    // Combine and remove duplicates
    val allEventIds = combineEventIds(joinedEventIds, createdEventIds)
    _eventsCount.value = allEventIds.size
  }

  /**
   * Combines joined and created event IDs, removing duplicates.
   *
   * @param joinedIds List of event IDs the user has joined
   * @param createdIds List of event IDs the user has created
   * @return Set of unique event IDs
   */
  private fun combineEventIds(joinedIds: List<String>, createdIds: List<String>): Set<String> {
    return (joinedIds + createdIds).toSet()
  }

  // Load the pinned events for the user
  fun loadPinnedEvents() {
    viewModelScope.launch {
      try {
        val pinnedEventIds = userRepository.getPinnedEvents(currentUserId)
        Log.d(TAG, "Loading pinned events, IDs: $pinnedEventIds")
        val events =
            pinnedEventIds.mapNotNull { eventId ->
              try {
                eventRepository.getEvent(eventId)
              } catch (e: Exception) {
                Log.e(TAG, "Failed to load pinned event: $eventId", e)
                null
              }
            }
        _pinnedEvents.value = events
        Log.d(TAG, "Loaded ${events.size} pinned events: ${events.map { it.title }}")
      } catch (exception: Exception) {
        Log.e(TAG, "Failed to load pinned events for user: $currentUserId", exception)
        _pinnedEvents.value = emptyList()
      }
    }
  }

  /** Loads the pinned organization for the current user. */
  fun loadUserOrganizations() {
    viewModelScope.launch {
      try {
        val pinnedOrgId = userRepository.getPinnedOrganization(currentUserId)
        if (pinnedOrgId != null) {
          val organization = organizationRepository.getOrganizationById(pinnedOrgId)
          _userOrganizations.value = listOfNotNull(organization)
          Log.d(TAG, "Loaded pinned organization for user: $currentUserId")
        } else {
          _userOrganizations.value = emptyList()
          Log.d(TAG, "No pinned organization for user: $currentUserId")
        }
      } catch (exception: Exception) {
        Log.e(TAG, "Failed to load pinned organization for user: $currentUserId", exception)
        _userOrganizations.value = emptyList()
      }
    }
  }
}

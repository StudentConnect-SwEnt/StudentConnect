package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
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
 * @param currentUserId The ID of the current user
 */
class ProfileScreenViewModel(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository,
    private val eventRepository: EventRepository,
    private val currentUserId: String
) : ViewModel() {

  companion object {
    private const val TAG = "ProfileScreenViewModel"
  }

  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  private val _friendsCount = MutableStateFlow(0)
  val friendsCount: StateFlow<Int> = _friendsCount.asStateFlow()

  private val _eventsCount = MutableStateFlow(0)
  val eventsCount: StateFlow<Int> = _eventsCount.asStateFlow()

  private val _pinnedEvents = MutableStateFlow<List<Event>>(emptyList())
  val pinnedEvents: StateFlow<List<Event>> = _pinnedEvents.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadUserProfile()
    loadPinnedEvents()
  }

  // Load the user's profile data and stats
  fun loadUserProfile() {
    _isLoading.value = true
    viewModelScope.launch {
      try {
        val loadedUser = userRepository.getUserById(currentUserId)
        _user.value = loadedUser

        loadFriendsCount()
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

  // Load the friends count for the user
  private suspend fun loadFriendsCount() {
    try {
      val friends = friendsRepository.getFriends(currentUserId)
      _friendsCount.value = friends.size
    } catch (exception: Exception) {
      Log.e(TAG, "Failed to load friends count for user: $currentUserId", exception)
      _friendsCount.value = 0
    }
  }

  // Load the joined events count for the user
  private suspend fun loadEventsCount() {
    try {
      val joinedEventIds = userRepository.getJoinedEvents(currentUserId)
      _eventsCount.value = joinedEventIds.size
    } catch (exception: Exception) {
      Log.e(TAG, "Failed to load events count for user: $currentUserId", exception)
      _eventsCount.value = 0
    }
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
}

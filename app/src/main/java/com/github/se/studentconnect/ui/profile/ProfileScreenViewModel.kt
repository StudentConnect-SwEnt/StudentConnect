package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile screen showing user information, stats, and pinned events.
 *
 * @param userRepository Repository for user data operations
 * @param friendsRepository Repository for friends data operations
 * @param currentUserId The ID of the current user
 */
class ProfileScreenViewModel(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository,
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

  // Pinned events state (for future use)
  private val _pinnedEvents = MutableStateFlow<List<Event>>(emptyList())
  val pinnedEvents: StateFlow<List<Event>> = _pinnedEvents.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadUserProfile()
    loadPinnedEvents()
  }

  /** Loads the current user's profile and stats from the repositories. */
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

  /** Loads the count of joined events for the current user. */
  private suspend fun loadEventsCount() {
    try {
      val joinedEventIds = userRepository.getJoinedEvents(currentUserId)
      _eventsCount.value = joinedEventIds.size
    } catch (exception: Exception) {
      Log.e(TAG, "Failed to load events count for user: $currentUserId", exception)
      // If events loading fails, set count to 0
      _eventsCount.value = 0
    }
  }

  /** Loads the pinned events for the current user. (To be implemented later) */
  fun loadPinnedEvents() {
    // For now, pinned events functionality is not implemented
    // This will be implemented in the future
    _pinnedEvents.value = emptyList()
  }
}

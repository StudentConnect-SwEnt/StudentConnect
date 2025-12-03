package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/** ViewModel for the friends list screen. Loads and manages the list of friends. */
@OptIn(FlowPreview::class)
class FriendsListViewModel(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendsRepository,
    private val userId: String
) : ViewModel() {

  private val _friends = MutableStateFlow<List<User>>(emptyList())
  val friends: StateFlow<List<User>> = _friends.asStateFlow()

  private val _filteredFriends = MutableStateFlow<List<User>>(emptyList())
  val filteredFriends: StateFlow<List<User>> = _filteredFriends.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  init {
    loadFriends()
    setupDebouncedSearch()
  }

  /** Loads all friends for the given user. */
  fun loadFriends() {
    _isLoading.value = true
    _error.value = null

    viewModelScope.launch {
      try {
        val friendIds = friendsRepository.getFriends(userId)

        val friendUsers =
            friendIds
                .map { friendId ->
                  async {
                    runCatching { userRepository.getUserById(friendId) }
                        .onFailure { e ->
                          Log.w(TAG, "Failed to load friend with ID: $friendId", e)
                        }
                        .getOrNull()
                  }
                }
                .awaitAll()
                .filterNotNull()

        _friends.value = friendUsers
        _filteredFriends.value = friendUsers
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load friends for user: $userId", e)
        _error.value = e.message ?: ProfileConstants.ERROR_LOAD_FRIENDS
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Sets up debounced search to reduce filtering overhead. */
  private fun setupDebouncedSearch() {
    viewModelScope.launch {
      _searchQuery.debounce(SEARCH_DEBOUNCE_MILLIS).collect { query -> filterFriends(query) }
    }
  }

  /** Updates the search query. Filtering is debounced to avoid excessive work. */
  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  private fun filterFriends(query: String) {
    _filteredFriends.value =
        if (query.isBlank()) {
          _friends.value
        } else {
          _friends.value.filter { friend ->
            friend.getFullName().contains(query, ignoreCase = true) ||
                friend.username.contains(query, ignoreCase = true)
          }
        }
  }

  companion object {
    private const val TAG = "FriendsListViewModel"
    private const val SEARCH_DEBOUNCE_MILLIS = 300L
  }
}

package com.github.se.studentconnect.ui.screen.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import kotlinx.coroutines.launch

/** Represents the state of the search screen. */
data class SearchState(
    val query: String = "",
    val shownEvents: List<Event> = emptyList(),
    val shownUsers: List<User> = emptyList(),
    val allUsers: List<User> = emptyList(),
    val allEvents: List<Event> = emptyList(),
)

/** ViewModel for managing the state of the search screen. */
class SearchViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
) : ViewModel() {

  private val _state = mutableStateOf(SearchState())
  /** The current state of the search screen. */
  val state: State<SearchState> = _state

  init {
    viewModelScope.launch { init() }
  }

  /**
   * Initializes the ViewModel by fetching all events and users from the repositories. This function
   * should be called once when the ViewModel is created.
   */
  private suspend fun init() {
    setAllEvents(getAllEvents())
    setAllUsers(getAllUsers())
    setQuery("")
  }

  private suspend fun getAllEvents() = eventRepository.getAllVisibleEvents()

  private suspend fun getAllUsers() = userRepository.getAllUsers()

  private fun update(block: (SearchState) -> SearchState) {
    _state.value = block(_state.value)
  }

  /**
   * Updates the search query and filters the shown events and users based on the new query.
   *
   * @param query The new search query.
   */
  fun setQuery(query: String) {
    update { it.copy(query = query.trim().ifBlank { "" }) }
    setShownEvents(getEventsForQuery(query))
    setShownUsers(getUsersForQuery(query))
  }

  private fun setShownEvents(events: List<Event>) = update {
    it.copy(shownEvents = events.ifEmpty { emptyList() })
  }

  private fun setShownUsers(users: List<User>) = update {
    it.copy(shownUsers = users.ifEmpty { emptyList() })
  }

  private fun setAllEvents(events: List<Event>) = update {
    it.copy(allEvents = events.ifEmpty { emptyList() })
  }

  private fun setAllUsers(users: List<User>) = update {
    it.copy(allUsers = users.ifEmpty { emptyList() })
  }

  private fun getEventsForQuery(query: String) =
      if (query.isBlank()) _state.value.allEvents
      else _state.value.allEvents.filter { e -> e.title.contains(query, ignoreCase = true) }

  private fun getUsersForQuery(query: String) =
      if (query.isBlank()) _state.value.allUsers
      else _state.value.allUsers.filter { it.userId.startsWith(query, ignoreCase = true) }

  /** Checks if there are any events to show. */
  fun hasEvents() = state.value.shownEvents.isNotEmpty()

  /** Checks if there are any users to show. */
  fun hasUsers() = state.value.shownUsers.isNotEmpty()

  /** Resets the search query to an empty string. */
  fun reset() = setQuery("")
}

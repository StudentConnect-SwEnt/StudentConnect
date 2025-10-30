package com.github.se.studentconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePageUiState(
    val subscribedEventsStories: Map<Event, Pair<Int, Int>> = emptyMap(),
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
)

class HomePageViewModel
@Inject
constructor(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    // maybe will be used after for recommendations
    private val userRepositoryLocal: UserRepository = UserRepositoryProvider.repository,
    private val currentUserId: String,
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUiState())
  val uiState: StateFlow<HomePageUiState> = _uiState.asStateFlow()

  init {
    loadAllEvents()
    loadAllSubscribedEventsStories()
  }

  private fun loadAllSubscribedEventsStories() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      val allSubscribedEventsId = userRepositoryLocal.getJoinedEvents(currentUserId)
      val allSubscribedEvents =
          eventRepository.getAllVisibleEventsSatisfying { event ->
            allSubscribedEventsId.contains(event.uid)
          }

      val allSubscribedEventsStory = mutableMapOf<Event, Pair<Int, Int>>()
      var i = 0
      for (e in allSubscribedEvents) {
        allSubscribedEventsStory.put(e, Pair(i, 0))
        i++
      }

      _uiState.update {
        it.copy(subscribedEventsStories = allSubscribedEventsStory, isLoading = false)
      }
    }
  }

  private fun loadAllEvents() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      val allEvents = eventRepository.getAllVisibleEvents()
      _uiState.update { it.copy(events = allEvents, isLoading = false) }
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

  fun refresh() {
    loadAllEvents()
  }
}

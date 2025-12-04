package com.github.se.studentconnect.ui.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.ui.components.EventSelectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing event selection state in the camera/story capture flow.
 *
 * This ViewModel handles fetching the list of events the user has joined, which can then be linked
 * to stories they capture.
 *
 * @param storyRepository Repository for fetching user's joined events
 */
class CameraModeSelectorViewModel(private val storyRepository: StoryRepository) : ViewModel() {

  private val _eventSelectionState =
      MutableStateFlow<EventSelectionState>(EventSelectionState.Success(emptyList()))

  /** Observable state of the event selection, emitting Loading, Success, or Error states. */
  val eventSelectionState: StateFlow<EventSelectionState> = _eventSelectionState.asStateFlow()

  /**
   * Loads the user's joined events from the repository.
   *
   * If no user is authenticated, returns an empty list. Otherwise, fetches events asynchronously
   * and updates [eventSelectionState] accordingly.
   */
  // TODO: Implement event caching to avoid unnecessary backend calls.
  fun loadJoinedEvents() {
    val userId = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }
    if (userId != null) {
      _eventSelectionState.value = EventSelectionState.Loading()
      viewModelScope.launch {
        try {
          val events = storyRepository.getUserJoinedEvents(userId)
          _eventSelectionState.value = EventSelectionState.Success(events)
        } catch (e: Exception) {
          _eventSelectionState.value = EventSelectionState.Error(error = e.message)
        }
      }
    } else {
      _eventSelectionState.value = EventSelectionState.Success(emptyList())
    }
  }
}

/**
 * Factory for creating CameraModeSelectorViewModel instances with the required StoryRepository
 * dependency.
 *
 * @property storyRepository The StoryRepository to inject into the CameraModeSelectorViewModel
 */
class CameraModeSelectorViewModelFactory(private val storyRepository: StoryRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CameraModeSelectorViewModel::class.java)) {
      return CameraModeSelectorViewModel(storyRepository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}

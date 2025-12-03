package com.github.se.studentconnect.ui.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.story.StoryRepository
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.ui.components.EventSelectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for CameraModeSelectorScreen that manages event selection state. */
class CameraModeSelectorViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

  private val _eventSelectionState = MutableStateFlow<EventSelectionState>(EventSelectionState.Success(emptyList()))
  val eventSelectionState: StateFlow<EventSelectionState> = _eventSelectionState.asStateFlow()

  /** Loads the user's joined events. */
  fun loadJoinedEvents() {
    val userId = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }
    if (userId != null) {
      _eventSelectionState.value = EventSelectionState.Loading
      viewModelScope.launch {
        try {
          val events = storyRepository.getUserJoinedEvents(userId)
          _eventSelectionState.value = EventSelectionState.Success(events)
        } catch (e: Exception) {
          _eventSelectionState.value = EventSelectionState.Error(e.message)
        }
      }
    } else {
      _eventSelectionState.value = EventSelectionState.Success(emptyList())
    }
  }
}

/**
 * Factory for creating CameraModeSelectorViewModel instances with the required StoryRepository dependency.
 *
 * @property storyRepository The StoryRepository to inject into the CameraModeSelectorViewModel
 */
class CameraModeSelectorViewModelFactory(
    private val storyRepository: StoryRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CameraModeSelectorViewModel::class.java)) {
      return CameraModeSelectorViewModel(storyRepository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}


package com.github.se.studentconnect.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.event.EventStatistics
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Event Statistics screen.
 *
 * @param isLoading Whether data is currently being loaded.
 * @param statistics The loaded statistics data, or null if not yet loaded.
 * @param error Error message if loading failed, or null if no error.
 * @param animationProgress Progress of entrance animations (0f to 1f).
 */
data class EventStatisticsUiState(
    val isLoading: Boolean = true,
    val statistics: EventStatistics? = null,
    val error: String? = null,
    val animationProgress: Float = 0f
)

/**
 * ViewModel for the Event Statistics screen.
 *
 * Manages fetching event statistics, handling loading and error states, and controlling animations.
 *
 * @param eventRepository Repository for event data.
 * @param organizationRepository Repository for organization data (to get follower count).
 */
class EventStatisticsViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository = OrganizationRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(EventStatisticsUiState())
  val uiState: StateFlow<EventStatisticsUiState> = _uiState.asStateFlow()

  private var currentEventUid: String? = null

  companion object {
    // Note: This constant should match R.string.stats_error_unknown
    // ViewModels cannot access string resources directly
    const val ERROR_UNKNOWN = "An unknown error occurred"
  }

  /**
   * Loads statistics for a given event.
   *
   * @param eventUid The unique identifier of the event.
   */
  fun loadStatistics(eventUid: String) {
    currentEventUid = eventUid
    _uiState.update { it.copy(isLoading = true, error = null, animationProgress = 0f) }

    viewModelScope.launch {
      try {
        val event = eventRepository.getEvent(eventUid)

        // Get follower count from organization
        val followerCount =
            try {
              val organization = organizationRepository.getOrganizationById(event.ownerId)
              organization?.memberUids?.size ?: 0
            } catch (e: Exception) {
              // If organization fetch fails, proceed with 0 followers
              0
            }

        val statistics = eventRepository.getEventStatistics(eventUid, followerCount)
        _uiState.update {
          it.copy(isLoading = false, statistics = statistics, animationProgress = 1f)
        }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = e.message ?: ERROR_UNKNOWN) }
      }
    }
  }

  /** Refreshes statistics for the current event. */
  fun refresh() {
    currentEventUid?.let { loadStatistics(it) }
  }
}


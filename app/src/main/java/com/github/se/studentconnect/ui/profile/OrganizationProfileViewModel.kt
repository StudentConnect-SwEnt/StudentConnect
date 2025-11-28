package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.OrganizationProfile
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.fetchOrganizationMembers
import com.github.se.studentconnect.model.toOrganizationEvents
import com.github.se.studentconnect.model.toOrganizationProfile
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.OrganizationRepository
import com.github.se.studentconnect.repository.OrganizationRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Tab selection for the organization profile screen. */
enum class OrganizationTab {
  EVENTS,
  MEMBERS
}

/** UI state for the organization profile screen. */
data class OrganizationProfileUiState(
    val organization: OrganizationProfile? = null,
    val selectedTab: OrganizationTab = OrganizationTab.EVENTS,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Organization Profile screen.
 *
 * Manages organization data, tab selection, and follow/unfollow actions.
 *
 * @param organizationId The ID of the organization to display (optional for preview/testing)
 * @param organizationRepository The repository to fetch organization data from
 * @param eventRepository The repository to fetch event data from
 * @param userRepository The repository to fetch user data from
 */
class OrganizationProfileViewModel(
    private val organizationId: String? = null,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(OrganizationProfileUiState())
  val uiState: StateFlow<OrganizationProfileUiState> = _uiState.asStateFlow()

  private val currentUserId: String? = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }

  init {
    loadOrganizationData()
  }

  /** Loads organization data from the repository. */
  private fun loadOrganizationData() {
    _uiState.value = _uiState.value.copy(isLoading = true)

    viewModelScope.launch {
      try {
        if (organizationId == null) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "Organization ID is required", organization = null)
          return@launch
        }

        val organization = organizationRepository.getOrganizationById(organizationId)
        if (organization == null) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "Organization not found", organization = null)
          return@launch
        }

        // Fetch events for this organization
        val events =
            try {
              eventRepository.getEventsByOrganization(organizationId).toOrganizationEvents()
            } catch (e: Exception) {
              emptyList() // If fetching events fails, use empty list
            }

        // Fetch members for this organization
        val members =
            try {
              fetchOrganizationMembers(organization.memberUids, userRepository)
            } catch (e: Exception) {
              emptyList() // If fetching members fails, use empty list
            }

        // Check if current user is following this organization
        val isFollowing =
            currentUserId?.let { uid ->
              try {
                val followedOrgs = userRepository.getFollowedOrganizations(uid)
                followedOrgs.contains(organizationId)
              } catch (e: Exception) {
                false
              }
            } ?: false

        val organizationProfile =
            organization.toOrganizationProfile(
                isFollowing = isFollowing, events = events, members = members)

        _uiState.value =
            _uiState.value.copy(organization = organizationProfile, isLoading = false, error = null)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                error = "Failed to load organization: ${e.message}",
                organization = null)
      }
    }
  }

  /**
   * Selects a tab in the organization profile.
   *
   * @param tab The tab to select
   */
  fun selectTab(tab: OrganizationTab) {
    _uiState.value = _uiState.value.copy(selectedTab = tab)
  }

  /** Toggles the follow status for the organization. */
  fun toggleFollow() {
    val currentOrg = _uiState.value.organization ?: return
    val userId = currentUserId ?: return

    // Optimistically update UI
    val updatedOrg = currentOrg.copy(isFollowing = !currentOrg.isFollowing)
    _uiState.value = _uiState.value.copy(organization = updatedOrg)

    // Persist to backend
    viewModelScope.launch {
      try {
        if (updatedOrg.isFollowing) {
          userRepository.followOrganization(userId, currentOrg.organizationId)
        } else {
          userRepository.unfollowOrganization(userId, currentOrg.organizationId)
        }
      } catch (e: Exception) {
        // Revert on failure
        _uiState.value = _uiState.value.copy(organization = currentOrg)
      }
    }
  }

  companion object {
    // Constants for UI dimensions and styling
    const val AVATAR_BANNER_HEIGHT = 120
    const val AVATAR_SIZE = 80
    const val AVATAR_BORDER_WIDTH = 3
    const val AVATAR_ICON_SIZE = 40
    const val EVENT_CARD_WIDTH = 140
    const val EVENT_CARD_HEIGHT = 100
    const val MEMBER_AVATAR_SIZE = 72
    const val MEMBER_ICON_SIZE = 36
    const val GRID_COLUMNS = 2
    const val MEMBERS_GRID_HEIGHT = 400
  }
}

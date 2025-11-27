package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.OrganizationEvent
import com.github.se.studentconnect.model.OrganizationMember
import com.github.se.studentconnect.model.OrganizationProfile
import com.github.se.studentconnect.model.toOrganizationProfile
import com.github.se.studentconnect.repository.OrganizationRepository
import com.github.se.studentconnect.repository.OrganizationRepositoryProvider
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
 * @param repository The repository to fetch organization data from
 */
class OrganizationProfileViewModel(
    private val organizationId: String? = null,
    private val repository: OrganizationRepository = OrganizationRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(OrganizationProfileUiState())
  val uiState: StateFlow<OrganizationProfileUiState> = _uiState.asStateFlow()

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

        val organization = repository.getOrganizationById(organizationId)
        if (organization == null) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "Organization not found", organization = null)
          return@launch
        }

        // TODO: Fetch actual events and members for this organization
        // For now, using empty lists until those repositories are implemented
        val mockEvents = createMockEvents()
        val mockMembers = createMockMembers()

        val organizationProfile =
            organization.toOrganizationProfile(
                isFollowing = false, // TODO: Check if user is following this organization
                events = mockEvents,
                members = mockMembers)

        _uiState.value =
            _uiState.value.copy(
                organization = organizationProfile, isLoading = false, error = null)
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
    val updatedOrg = currentOrg.copy(isFollowing = !currentOrg.isFollowing)
    _uiState.value = _uiState.value.copy(organization = updatedOrg)
    // TODO: Persist follow/unfollow status to backend
  }

  /** Creates mock event data until event repository integration is complete. */
  private fun createMockEvents(): List<OrganizationEvent> {
    return listOf(
        OrganizationEvent(
            eventId = "event_1",
            cardTitle = "EPFL Hackathon",
            cardDate = "15 dec, 2024",
            title = "Hackathon EPFL",
            subtitle = "Tomorrow",
            location = "EPFL"),
        OrganizationEvent(
            eventId = "event_2",
            cardTitle = "EPFL Hackathon",
            cardDate = "15 dec, 2024",
            title = "Hackathon EPFL",
            subtitle = "Tomorrow",
            location = "EPFL"))
  }

  /** Creates mock member data until member repository integration is complete. */
  private fun createMockMembers(): List<OrganizationMember> {
    return listOf(
        OrganizationMember(
            memberId = "member_1", name = "Habibi", role = "Owner", avatarUrl = "avatar_12"),
        OrganizationMember(
            memberId = "member_2", name = "Habibi", role = "Owner", avatarUrl = "avatar_13"),
        OrganizationMember(
            memberId = "member_3", name = "Habibi", role = "Owner", avatarUrl = "avatar_23"),
        OrganizationMember(
            memberId = "member_4", name = "Habibi", role = "Owner", avatarUrl = "avatar_12"),
        OrganizationMember(
            memberId = "member_5", name = "Habibi", role = "Owner", avatarUrl = "avatar_13"),
        OrganizationMember(
            memberId = "member_6", name = "Habibi", role = "Owner", avatarUrl = "avatar_23"))
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

package com.github.se.studentconnect.ui.profile

import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.OrganizationEvent
import com.github.se.studentconnect.model.OrganizationMember
import com.github.se.studentconnect.model.OrganizationProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 */
// TODO: Inject a repository interface (e.g., OrganizationRepository) to fetch real data
// instead of using mock data. This will avoid a large refactor later when implementing
// the backend integration.
class OrganizationProfileViewModel(private val organizationId: String? = null) : ViewModel() {

  private val _uiState = MutableStateFlow(OrganizationProfileUiState())
  val uiState: StateFlow<OrganizationProfileUiState> = _uiState.asStateFlow()

  init {
    // TODO: Replace mock data loading with repository call when backend is implemented
    // Load mock data for now
    loadOrganizationData()
  }

  /** Loads organization data. Currently uses mock data. */
  private fun loadOrganizationData() {
    _uiState.value = _uiState.value.copy(isLoading = true)

    // Mock data - in production, this would fetch from a repository
    val mockOrganization = createMockOrganization()

    _uiState.value =
        _uiState.value.copy(organization = mockOrganization, isLoading = false, error = null)
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
  }

  /** Creates mock organization data for testing and preview. */
  private fun createMockOrganization(): OrganizationProfile {
    val mockEvents =
        listOf(
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

    val mockMembers =
        listOf(
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

    return OrganizationProfile(
        organizationId = organizationId ?: "org_evolve",
        name = "Evolve",
        description =
            "Evolve est une organisation dédiée au développement du potentiel humain et professionnel.",
        logoUrl = null,
        isFollowing = false,
        events = mockEvents,
        members = mockMembers)
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

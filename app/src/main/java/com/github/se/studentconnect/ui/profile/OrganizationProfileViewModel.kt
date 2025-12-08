package com.github.se.studentconnect.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationProfile
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.toOrganizationEvents
import com.github.se.studentconnect.model.organization.toOrganizationMember
import com.github.se.studentconnect.model.organization.toOrganizationProfile
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
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
    val isFollowLoading: Boolean = false,
    val showUnfollowDialog: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Organization Profile screen.
 *
 * Manages organization data, tab selection, and follow/unfollow actions.
 *
 * @param organizationId The ID of the organization to display (optional for preview/testing)
 * @param context Android context for accessing string resources
 * @param organizationRepository The repository to fetch organization data from
 * @param eventRepository The repository to fetch event data from
 * @param userRepository The repository to fetch user data from
 */
class OrganizationProfileViewModel(
    private val organizationId: String? = null,
    context: Context,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  // Use application context to avoid memory leaks
  private val appContext: Context = context.applicationContext

  companion object {
    private const val TAG = "OrganizationProfileVM"
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

        val events = fetchOrganizationEvents(organizationId)
        val members = fetchOrganizationMembers(organization)
        val isMember = checkIfUserIsMember(organization)
        val isFollowing = checkIfUserIsFollowing(isMember, organizationId)

        val organizationProfile =
            organization.toOrganizationProfile(
                isFollowing = isFollowing, isMember = isMember, events = events, members = members)

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

  /** Fetches events for the organization. Returns empty list on failure. */
  private suspend fun fetchOrganizationEvents(orgId: String) =
      try {
        eventRepository.getEventsByOrganization(orgId).toOrganizationEvents(appContext)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch events for organization $orgId", e)
        emptyList()
      }

  /** Fetches members for the organization. Returns empty list on failure. */
  private suspend fun fetchOrganizationMembers(
      organization: com.github.se.studentconnect.model.organization.Organization
  ) =
      try {
        val allMemberUids = buildMemberUidsList(organization)
        allMemberUids.mapNotNull { uid -> fetchMemberWithRole(uid, organization.createdBy) }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch members for organization ${organization.id}", e)
        emptyList()
      }

  /** Builds the list of member UIDs, ensuring creator is included. */
  private fun buildMemberUidsList(
      organization: com.github.se.studentconnect.model.organization.Organization
  ): List<String> {
    val allMemberUids = organization.memberUids.toMutableList()
    if (!allMemberUids.contains(organization.createdBy)) {
      allMemberUids.add(0, organization.createdBy)
    }
    return allMemberUids
  }

  /** Fetches a single member and assigns their role. Returns null on failure. */
  private suspend fun fetchMemberWithRole(uid: String, creatorId: String) =
      try {
        val user = userRepository.getUserById(uid)
        user?.let {
          val role = if (uid == creatorId) "Owner" else "Member"
          it.toOrganizationMember(role)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch user with ID $uid for organization member list", e)
        null
      }

  /** Checks if the current user is a member or creator of the organization. */
  private fun checkIfUserIsMember(
      organization: com.github.se.studentconnect.model.organization.Organization
  ) =
      currentUserId?.let { uid ->
        organization.memberUids.contains(uid) || organization.createdBy == uid
      } ?: false

  /** Checks if the current user is following the organization. */
  private suspend fun checkIfUserIsFollowing(isMember: Boolean, orgId: String) =
      if (isMember) {
        true
      } else {
        currentUserId?.let { uid -> isUserFollowingOrganization(uid, orgId) } ?: false
      }

  /** Checks if a specific user is following the organization. Returns false on error. */
  private suspend fun isUserFollowingOrganization(uid: String, orgId: String) =
      try {
        val followedOrgs = userRepository.getFollowedOrganizations(uid)
        followedOrgs.contains(orgId)
      } catch (e: Exception) {
        false
      }

  /**
   * Selects a tab in the organization profile.
   *
   * @param tab The tab to select
   */
  fun selectTab(tab: OrganizationTab) {
    _uiState.value = _uiState.value.copy(selectedTab = tab)
  }

  /**
   * Handles follow button click. Shows confirmation dialog for unfollowing. Members cannot
   * unfollow - they must leave the organization first.
   */
  fun onFollowButtonClick() {
    val currentOrg = _uiState.value.organization ?: return

    // If already following, show confirmation dialog before unfollowing
    if (currentOrg.isFollowing && !currentOrg.isMember) {
      _uiState.value = _uiState.value.copy(showUnfollowDialog = true)
    } else {
      // If not following, follow immediately
      performFollow()
    }
  }

  /** Dismisses the unfollow confirmation dialog. */
  fun dismissUnfollowDialog() {
    _uiState.value = _uiState.value.copy(showUnfollowDialog = false)
  }

  /** Confirms unfollowing the organization. */
  fun confirmUnfollow() {
    _uiState.value = _uiState.value.copy(showUnfollowDialog = false)
    performUnfollow()
  }

  /** Performs the follow action. */
  private fun performFollow() {
    val currentOrg = _uiState.value.organization ?: return
    val userId = currentUserId ?: return

    // Prevent rapid toggles - guard with loading flag
    if (_uiState.value.isFollowLoading) {
      Log.d(TAG, "Follow action already in progress, ignoring")
      return
    }

    viewModelScope.launch {
      try {
        // Set loading flag
        _uiState.value = _uiState.value.copy(isFollowLoading = true)

        // Follow the organization
        userRepository.followOrganization(userId, currentOrg.organizationId)

        // Update UI after successful backend operation
        val updatedOrg = currentOrg.copy(isFollowing = true)
        _uiState.value = _uiState.value.copy(organization = updatedOrg, isFollowLoading = false)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to follow organization", e)
        // Keep current state on failure
        _uiState.value = _uiState.value.copy(isFollowLoading = false)
      }
    }
  }

  /** Performs the unfollow action. */
  private fun performUnfollow() {
    val currentOrg = _uiState.value.organization ?: return
    val userId = currentUserId ?: return

    // Prevent rapid toggles - guard with loading flag
    if (_uiState.value.isFollowLoading) {
      Log.d(TAG, "Unfollow action already in progress, ignoring")
      return
    }

    // Check if user is a member - they cannot unfollow
    viewModelScope.launch {
      try {
        val organization = organizationRepository.getOrganizationById(currentOrg.organizationId)
        if (organization != null) {
          val isMember =
              organization.memberUids.contains(userId) || organization.createdBy == userId

          // Members cannot unfollow
          if (isMember) {
            Log.d(TAG, "Members cannot unfollow organization")
            return@launch
          }
        }

        // Set loading flag
        _uiState.value = _uiState.value.copy(isFollowLoading = true)

        // Unfollow the organization
        userRepository.unfollowOrganization(userId, currentOrg.organizationId)

        // Update UI after successful backend operation
        val updatedOrg = currentOrg.copy(isFollowing = false)
        _uiState.value = _uiState.value.copy(organization = updatedOrg, isFollowLoading = false)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to unfollow organization", e)
        // Keep current state on failure
        _uiState.value = _uiState.value.copy(isFollowLoading = false)
      }
    }
  }
}

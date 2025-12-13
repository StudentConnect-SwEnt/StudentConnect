package com.github.se.studentconnect.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationProfile
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.toOrganizationEvents
import com.github.se.studentconnect.model.organization.toOrganizationMember
import com.github.se.studentconnect.model.organization.toOrganizationProfile
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Timestamp
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
    val showAddMemberDialog: Boolean = false,
    val selectedRole: String? = null,
    val availableUsers: List<User> = emptyList(),
    val isLoadingUsers: Boolean = false,
    val pendingInvitations: Map<String, String> = emptyMap(), // role -> userId
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
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val notificationRepository: NotificationRepository =
        NotificationRepositoryProvider.repository
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

    // Standard organization roles
    val STANDARD_ROLES =
        listOf("Owner", "President", "Vice President", "Treasurer", "Secretary", "Member")
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
        val isOwner = currentUserId == organization.createdBy
        val isFollowing = checkIfUserIsFollowing(isMember, organizationId)

        val organizationProfile =
            organization.toOrganizationProfile(
                isFollowing = isFollowing,
                isMember = isMember,
                isOwner = isOwner,
                events = events,
                members = members)

        // Fetch pending invitations if user is owner
        val pendingInvitations =
            if (isOwner) {
              fetchPendingInvitations(organizationId)
            } else {
              emptyMap()
            }

        _uiState.value =
            _uiState.value.copy(
                organization = organizationProfile,
                pendingInvitations = pendingInvitations,
                isLoading = false,
                error = null)
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
  ): List<com.github.se.studentconnect.model.organization.OrganizationMember> =
      try {
        val allMemberUids = buildMemberUidsList(organization)
        allMemberUids.mapNotNull { uid ->
          fetchMemberWithRole(uid, organization.createdBy, organization)
        }
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
  private suspend fun fetchMemberWithRole(
      uid: String,
      creatorId: String,
      organization: com.github.se.studentconnect.model.organization.Organization
  ): com.github.se.studentconnect.model.organization.OrganizationMember? =
      try {
        val user = userRepository.getUserById(uid)

        user?.let {
          // Get role from memberRoles map, fallback to Owner/Member logic
          val role = organization.memberRoles[uid] ?: if (uid == creatorId) "Owner" else "Member"
          Log.d(
              TAG,
              "Fetched member $uid with role: $role (from memberRoles: ${organization.memberRoles[uid]})")
          it.toOrganizationMember(role)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch user with ID $uid for organization member list", e)
        null
      }

  /** Fetches pending invitations for the organization. Returns map of role -> userId. */
  private suspend fun fetchPendingInvitations(orgId: String): Map<String, String> =
      try {
        val invitations = organizationRepository.getPendingInvitations(orgId)
        // Create map of role -> userId for quick lookup
        invitations.associate { it.role to it.userId }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch pending invitations", e)
        emptyMap()
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

  /**
   * Shows the add member dialog for a specific role.
   *
   * @param role The role to assign to the new member
   */
  fun showAddMemberDialog(role: String) {
    _uiState.value = _uiState.value.copy(showAddMemberDialog = true, selectedRole = role)
    loadAvailableUsers()
  }

  /** Dismisses the add member dialog. */
  fun dismissAddMemberDialog() {
    _uiState.value =
        _uiState.value.copy(
            showAddMemberDialog = false, selectedRole = null, availableUsers = emptyList())
  }

  /** Loads users that can be invited (not already members). */
  private fun loadAvailableUsers() {
    val currentOrg = _uiState.value.organization ?: return

    viewModelScope.launch {
      try {
        _uiState.value = _uiState.value.copy(isLoadingUsers = true)

        // Get all users
        val allUsers = userRepository.getAllUsers()

        // Get current member UIDs
        val organization = organizationRepository.getOrganizationById(currentOrg.organizationId)
        if (organization == null) {
          Log.e(TAG, "Organization not found while loading available users")
          _uiState.value = _uiState.value.copy(isLoadingUsers = false)
          return@launch
        }

        val memberUids =
            organization.memberUids.toMutableSet().apply {
              // Always include the creator
              add(organization.createdBy)
            }

        // Filter out existing members and current user
        val availableUsers =
            allUsers.filter { user ->
              !memberUids.contains(user.userId) && user.userId != currentUserId
            }

        _uiState.value =
            _uiState.value.copy(availableUsers = availableUsers, isLoadingUsers = false)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load available users", e)
        _uiState.value = _uiState.value.copy(isLoadingUsers = false)
      }
    }
  }

  /**
   * Sends a member invitation to a user.
   *
   * @param userId The ID of the user to invite
   */
  fun sendMemberInvitation(userId: String) {
    val currentOrg = _uiState.value.organization ?: return
    val role = _uiState.value.selectedRole ?: return
    val inviterId = currentUserId ?: return

    viewModelScope.launch {
      try {
        // Get inviter name
        val inviter = userRepository.getUserById(inviterId)
        val inviterName = inviter?.getFullName() ?: "Unknown"

        // Send invitation
        organizationRepository.sendMemberInvitation(
            organizationId = currentOrg.organizationId,
            userId = userId,
            role = role,
            invitedBy = inviterId)

        // Create notification
        val notification =
            Notification.OrganizationMemberInvitation(
                id = "",
                userId = userId,
                timestamp = Timestamp.now(),
                isRead = false,
                organizationId = currentOrg.organizationId,
                organizationName = currentOrg.name,
                role = role,
                invitedBy = inviterId,
                invitedByName = inviterName)

        notificationRepository.createNotification(
            notification,
            onSuccess = { Log.d(TAG, "Notification sent successfully to user $userId") },
            onFailure = { error ->
              Log.e(TAG, "Failed to send notification to user $userId: $error")
            })

        // Update pending invitations in UI
        val updatedInvitations = _uiState.value.pendingInvitations.toMutableMap()
        updatedInvitations[role] = userId
        _uiState.value = _uiState.value.copy(pendingInvitations = updatedInvitations)

        // Close dialog
        dismissAddMemberDialog()
      } catch (e: Exception) {
        Log.e(TAG, "Failed to send member invitation", e)
      }
    }
  }

  /**
   * Accepts a member invitation (called from notification action).
   *
   * @param organizationId The ID of the organization
   */
  fun acceptMemberInvitation(organizationId: String) {
    val userId = currentUserId ?: return

    viewModelScope.launch {
      try {
        organizationRepository.acceptMemberInvitation(organizationId, userId)
        loadOrganizationData()
      } catch (e: Exception) {
        Log.e(TAG, "Failed to accept member invitation", e)
      }
    }
  }

  /**
   * Rejects a member invitation (called from notification action).
   *
   * @param organizationId The ID of the organization
   */
  fun rejectMemberInvitation(organizationId: String) {
    val userId = currentUserId ?: return

    viewModelScope.launch {
      try {
        organizationRepository.rejectMemberInvitation(organizationId, userId)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to reject member invitation", e)
      }
    }
  }

  /**
   * Removes a member from the organization.
   *
   * @param member The member to remove
   */
  fun removeMember(member: com.github.se.studentconnect.model.organization.OrganizationMember) {
    val currentOrg = _uiState.value.organization ?: return
    currentUserId ?: return

    // Can't remove the owner
    if (member.role == "Owner") {
      Log.d(TAG, "Cannot remove the owner")
      return
    }

    viewModelScope.launch {
      try {
        val organization = organizationRepository.getOrganizationById(currentOrg.organizationId)
        if (organization != null) {
          // Remove from memberUids
          val updatedMemberUids = organization.memberUids.toMutableList()
          updatedMemberUids.remove(member.memberId)

          // Remove from memberRoles
          val updatedMemberRoles = organization.memberRoles.toMutableMap()
          updatedMemberRoles.remove(member.memberId)

          // Update organization
          val updatedOrg =
              organization.copy(memberUids = updatedMemberUids, memberRoles = updatedMemberRoles)
          organizationRepository.saveOrganization(updatedOrg)

          // Reload organization data
          loadOrganizationData()
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to remove member", e)
      }
    }
  }

  /**
   * Refreshes the organization data (called when invitation is accepted/rejected). This is a public
   * method that can be called from outside the ViewModel.
   */
  fun refreshOrganization() {
    loadOrganizationData()
  }
}

package com.github.se.studentconnect.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.organization.SocialLinks
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the organization profile edit screen. */
data class OrganizationProfileEditUiState(
    val organization: Organization? = null,
    val name: String = "",
    val type: OrganizationType = OrganizationType.Other,
    val description: String = "",
    val location: String? = null,
    val mainDomains: List<String> = emptyList(),
    val ageRanges: List<String> = emptyList(),
    val typicalEventSize: String? = null,
    val roles: List<OrganizationRole> = emptyList(),
    val socialWebsite: String? = null,
    val socialInstagram: String? = null,
    val socialX: String? = null,
    val socialLinkedIn: String? = null,
    val logoUrl: String? = null,
    val logoUri: Uri? = null,
    val members: List<OrganizationMemberEdit> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val showRemoveMemberDialog: Boolean = false,
    val memberToRemove: OrganizationMemberEdit? = null,
    val showChangeRoleDialog: Boolean = false,
    val memberToChangeRole: OrganizationMemberEdit? = null,
    val successMessage: String? = null
)

/** Represents a member in the edit screen with their information. */
data class OrganizationMemberEdit(
    val userId: String,
    val name: String,
    val role: String,
    val avatarUrl: String?,
    val isOwner: Boolean = false
)

/**
 * ViewModel for the Organization Profile Edit screen.
 *
 * Manages organization data editing, validation, and member management.
 *
 * @param organizationId The ID of the organization to edit
 * @param context Android context for accessing string resources
 * @param organizationRepository The repository for organization operations
 * @param userRepository The repository for user operations
 * @param mediaRepository The repository for media operations
 */
class OrganizationProfileEditViewModel(
    private val organizationId: String,
    context: Context,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val mediaRepository: MediaRepository = MediaRepositoryProvider.repository
) : ViewModel() {

  private val appContext: Context = context.applicationContext

  companion object {
    private const val TAG = "OrgProfileEditVM"
    const val MAX_NAME_LENGTH = 200
    const val MAX_DESCRIPTION_LENGTH = 1000
  }

  private val _uiState = MutableStateFlow(OrganizationProfileEditUiState())
  val uiState: StateFlow<OrganizationProfileEditUiState> = _uiState.asStateFlow()

  private val currentUserId: String? = AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }

  init {
    loadOrganizationData()
  }

  /** Loads the organization data from the repository. */
  private fun loadOrganizationData() {
    _uiState.value = _uiState.value.copy(isLoading = true)

    viewModelScope.launch {
      try {
        val organization = organizationRepository.getOrganizationById(organizationId)
        if (organization == null) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "Organization not found", organization = null)
          return@launch
        }

        // Verify user is the owner
        if (currentUserId != organization.createdBy) {
          _uiState.value =
              _uiState.value.copy(
                  isLoading = false, error = "Only the owner can edit this organization")
          return@launch
        }

        // Load members
        val members = loadMembers(organization)

        _uiState.value =
            _uiState.value.copy(
                organization = organization,
                name = organization.name,
                type = organization.type,
                description = organization.description ?: "",
                location = organization.location,
                mainDomains = organization.mainDomains,
                ageRanges = organization.ageRanges,
                typicalEventSize = organization.typicalEventSize,
                roles = organization.roles,
                socialWebsite = organization.socialLinks.website,
                socialInstagram = organization.socialLinks.instagram,
                socialX = organization.socialLinks.x,
                socialLinkedIn = organization.socialLinks.linkedin,
                logoUrl = organization.logoUrl,
                members = members,
                isLoading = false,
                error = null)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load organization", e)
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                error = "Failed to load organization: ${e.message}",
                organization = null)
      }
    }
  }

  /** Loads members for the organization. */
  private suspend fun loadMembers(organization: Organization): List<OrganizationMemberEdit> {
    return try {
      val allMemberUids = organization.memberUids.toMutableList()
      if (!allMemberUids.contains(organization.createdBy)) {
        allMemberUids.add(0, organization.createdBy)
      }

      allMemberUids.mapNotNull { uid ->
        val user = userRepository.getUserById(uid)
        user?.let {
          val role =
              organization.memberRoles[uid]
                  ?: if (uid == organization.createdBy)
                      appContext.getString(com.github.se.studentconnect.R.string.text_role_owner)
                  else appContext.getString(com.github.se.studentconnect.R.string.text_role_member)
          OrganizationMemberEdit(
              userId = uid,
              name = it.getFullName(),
              role = role,
              avatarUrl = it.profilePictureUrl,
              isOwner = uid == organization.createdBy)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to load members", e)
      emptyList()
    }
  }

  /** Updates the organization name. */
  fun updateName(name: String) {
    _uiState.value = _uiState.value.copy(name = name, nameError = null)
  }

  /** Updates the organization type. */
  fun updateType(type: OrganizationType) {
    _uiState.value = _uiState.value.copy(type = type)
  }

  /** Updates the organization description. */
  fun updateDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description, descriptionError = null)
  }

  /** Updates the organization location. */
  fun updateLocation(location: String?) {
    _uiState.value = _uiState.value.copy(location = location)
  }

  /** Updates the main domains. */
  fun updateMainDomains(domains: List<String>) {
    _uiState.value = _uiState.value.copy(mainDomains = domains)
  }

  /** Updates the age ranges. */
  fun updateAgeRanges(ranges: List<String>) {
    _uiState.value = _uiState.value.copy(ageRanges = ranges)
  }

  /** Updates the typical event size. */
  fun updateTypicalEventSize(size: String?) {
    _uiState.value = _uiState.value.copy(typicalEventSize = size)
  }

  /** Updates the roles. */
  fun updateRoles(roles: List<OrganizationRole>) {
    _uiState.value = _uiState.value.copy(roles = roles)
  }

  /**
   * Updates the organization website URL.
   *
   * @param url The website URL, or null to clear it
   */
  fun updateSocialWebsite(url: String?) {
    _uiState.value = _uiState.value.copy(socialWebsite = url)
  }

  /**
   * Updates the organization Instagram URL.
   *
   * @param url The Instagram URL, or null to clear it
   */
  fun updateSocialInstagram(url: String?) {
    _uiState.value = _uiState.value.copy(socialInstagram = url)
  }

  /**
   * Updates the organization X (formerly Twitter) URL.
   *
   * @param url The X URL, or null to clear it
   */
  fun updateSocialX(url: String?) {
    _uiState.value = _uiState.value.copy(socialX = url)
  }

  /**
   * Updates the organization LinkedIn URL.
   *
   * @param url The LinkedIn URL, or null to clear it
   */
  fun updateSocialLinkedIn(url: String?) {
    _uiState.value = _uiState.value.copy(socialLinkedIn = url)
  }

  /** Updates the logo URI. */
  fun updateLogoUri(uri: Uri?) {
    _uiState.value = _uiState.value.copy(logoUri = uri)
  }

  /** Removes the logo. */
  fun removeLogo() {
    _uiState.value = _uiState.value.copy(logoUri = null, logoUrl = null)
  }

  /** Validates and saves the organization. */
  fun saveOrganization() {
    val currentState = _uiState.value

    // Clear previous errors
    _uiState.value =
        _uiState.value.copy(
            nameError = null, descriptionError = null, error = null, successMessage = null)

    // Validate
    var hasError = false

    val trimmedName = currentState.name.trim()
    if (trimmedName.isEmpty()) {
      _uiState.value = _uiState.value.copy(nameError = "Organization name cannot be empty")
      hasError = true
    } else if (trimmedName.length > MAX_NAME_LENGTH) {
      _uiState.value =
          _uiState.value.copy(nameError = "Organization name exceeds $MAX_NAME_LENGTH characters")
      hasError = true
    }

    if (currentState.description.length > MAX_DESCRIPTION_LENGTH) {
      _uiState.value =
          _uiState.value.copy(
              descriptionError = "Description exceeds $MAX_DESCRIPTION_LENGTH characters")
      hasError = true
    }

    if (hasError) return

    _uiState.value = _uiState.value.copy(isSaving = true)

    viewModelScope.launch {
      try {
        val organization = currentState.organization ?: return@launch

        // Upload logo if changed
        var newLogoUrl = currentState.logoUrl
        if (currentState.logoUri != null) {
          try {
            newLogoUrl = mediaRepository.upload(currentState.logoUri)
            Log.d(TAG, "Uploaded new logo: $newLogoUrl")
          } catch (e: Exception) {
            Log.e(TAG, "Failed to upload logo", e)
            _uiState.value =
                _uiState.value.copy(isSaving = false, error = "Failed to upload logo: ${e.message}")
            return@launch
          }
        }

        // Create updated organization
        val updatedOrganization =
            organization.copy(
                name = trimmedName,
                type = currentState.type,
                description = currentState.description.ifBlank { null },
                logoUrl = newLogoUrl,
                location = currentState.location,
                mainDomains = currentState.mainDomains,
                ageRanges = currentState.ageRanges,
                typicalEventSize = currentState.typicalEventSize,
                roles = currentState.roles,
                socialLinks =
                    SocialLinks(
                        website = currentState.socialWebsite?.ifBlank { null },
                        instagram = currentState.socialInstagram?.ifBlank { null },
                        x = currentState.socialX?.ifBlank { null },
                        linkedin = currentState.socialLinkedIn?.ifBlank { null }))

        // Save to repository
        organizationRepository.saveOrganization(updatedOrganization)

        _uiState.value =
            _uiState.value.copy(
                isSaving = false,
                successMessage = "Organization updated successfully",
                logoUrl = newLogoUrl,
                logoUri = null)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to save organization", e)
        _uiState.value =
            _uiState.value.copy(
                isSaving = false, error = "Failed to update organization: ${e.message}")
      }
    }
  }

  /** Shows the remove member dialog. */
  fun showRemoveMemberDialog(member: OrganizationMemberEdit) {
    if (member.isOwner) {
      _uiState.value = _uiState.value.copy(error = "Cannot remove the organization owner")
      return
    }
    _uiState.value = _uiState.value.copy(showRemoveMemberDialog = true, memberToRemove = member)
  }

  /** Dismisses the remove member dialog. */
  fun dismissRemoveMemberDialog() {
    _uiState.value = _uiState.value.copy(showRemoveMemberDialog = false, memberToRemove = null)
  }

  /** Confirms removing a member. */
  fun confirmRemoveMember() {
    val member = _uiState.value.memberToRemove ?: return
    val organization = _uiState.value.organization ?: return

    viewModelScope.launch {
      try {
        // Remove from memberUids
        val updatedMemberUids = organization.memberUids.toMutableList()
        updatedMemberUids.remove(member.userId)

        // Remove from memberRoles
        val updatedMemberRoles = organization.memberRoles.toMutableMap()
        updatedMemberRoles.remove(member.userId)

        // Update organization
        val updatedOrg =
            organization.copy(memberUids = updatedMemberUids, memberRoles = updatedMemberRoles)
        organizationRepository.saveOrganization(updatedOrg)

        // Reload data
        loadOrganizationData()

        _uiState.value =
            _uiState.value.copy(
                showRemoveMemberDialog = false,
                memberToRemove = null,
                successMessage = "Member removed successfully")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to remove member", e)
        _uiState.value =
            _uiState.value.copy(
                showRemoveMemberDialog = false,
                memberToRemove = null,
                error = "Failed to remove member: ${e.message}")
      }
    }
  }

  /** Shows the change role dialog. */
  fun showChangeRoleDialog(member: OrganizationMemberEdit) {
    if (member.isOwner) {
      _uiState.value = _uiState.value.copy(error = "Cannot change the owner's role")
      return
    }
    _uiState.value = _uiState.value.copy(showChangeRoleDialog = true, memberToChangeRole = member)
  }

  /** Dismisses the change role dialog. */
  fun dismissChangeRoleDialog() {
    _uiState.value = _uiState.value.copy(showChangeRoleDialog = false, memberToChangeRole = null)
  }

  /** Confirms changing a member's role. */
  fun confirmChangeRole(newRole: String) {
    val member = _uiState.value.memberToChangeRole ?: return
    val organization = _uiState.value.organization ?: return

    viewModelScope.launch {
      try {
        // Update memberRoles
        val updatedMemberRoles = organization.memberRoles.toMutableMap()
        updatedMemberRoles[member.userId] = newRole

        // Update organization
        val updatedOrg = organization.copy(memberRoles = updatedMemberRoles)
        organizationRepository.saveOrganization(updatedOrg)

        // Reload data
        loadOrganizationData()

        _uiState.value =
            _uiState.value.copy(
                showChangeRoleDialog = false,
                memberToChangeRole = null,
                successMessage = "Role updated successfully")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to change role", e)
        _uiState.value =
            _uiState.value.copy(
                showChangeRoleDialog = false,
                memberToChangeRole = null,
                error = "Failed to change role: ${e.message}")
      }
    }
  }

  /** Clears the success message. */
  fun clearSuccessMessage() {
    _uiState.value = _uiState.value.copy(successMessage = null)
  }

  /** Clears the error message. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }
}

package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the organization management screen. */
data class OrganizationManagementUiState(
    val userOrganizations: List<Organization> = emptyList(),
    val pinnedOrganizationId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldRedirectToCreation: Boolean = false
)

/**
 * ViewModel for the Organization Management screen.
 *
 * Manages user's organizations and navigation logic.
 *
 * @param userId The ID of the current user
 * @param organizationRepository Repository for organization data
 * @param userRepository Repository for user data
 */
class OrganizationManagementViewModel(
    private val userId: String,
    private val organizationRepository: OrganizationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

  companion object {
    private const val TAG = "OrgManagementVM"
  }

  private val _uiState = MutableStateFlow(OrganizationManagementUiState())
  val uiState: StateFlow<OrganizationManagementUiState> = _uiState.asStateFlow()

  init {
    loadUserOrganizations()
    loadPinnedOrganization()
  }

  /** Loads organizations where the user is a member. */
  fun loadUserOrganizations() {
    _uiState.value = _uiState.value.copy(isLoading = true, error = null)

    viewModelScope.launch {
      try {
        // Fetch all organizations
        val allOrganizations = organizationRepository.getAllOrganizations()

        // Filter organizations where the user is a member or creator
        val userOrganizations =
            allOrganizations.filter { org ->
              org.memberUids.contains(userId) || org.createdBy == userId
            }

        _uiState.value =
            _uiState.value.copy(
                userOrganizations = userOrganizations, isLoading = false, error = null)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load user organizations", e)
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                error = "Failed to load organizations: ${e.message}",
                userOrganizations = emptyList())
      }
    }
  }

  /** Marks that the user should be redirected to organization creation. */
  fun markRedirectToCreation() {
    _uiState.value = _uiState.value.copy(shouldRedirectToCreation = true)
  }

  /** Resets the redirect flag. */
  fun resetRedirectFlag() {
    _uiState.value = _uiState.value.copy(shouldRedirectToCreation = false)
  }

  /** Loads the user's pinned organization ID. */
  private fun loadPinnedOrganization() {
    viewModelScope.launch {
      try {
        val pinnedOrgId = userRepository.getPinnedOrganization(userId)
        _uiState.value = _uiState.value.copy(pinnedOrganizationId = pinnedOrgId)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to load pinned organization", e)
      }
    }
  }

  /**
   * Pins or unpins an organization.
   *
   * @param organizationId The ID of the organization to pin/unpin
   */
  fun togglePinOrganization(organizationId: String) {
    viewModelScope.launch {
      try {
        val currentPinned = _uiState.value.pinnedOrganizationId
        if (currentPinned == organizationId) {
          // Unpin
          userRepository.unpinOrganization(userId)
          _uiState.value = _uiState.value.copy(pinnedOrganizationId = null)
          Log.d(TAG, "Unpinned organization: $organizationId")
        } else {
          // Pin
          userRepository.pinOrganization(userId, organizationId)
          _uiState.value = _uiState.value.copy(pinnedOrganizationId = organizationId)
          Log.d(TAG, "Pinned organization: $organizationId")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to toggle pin organization", e)
        _uiState.value = _uiState.value.copy(error = "Failed to update pin: ${e.message}")
      }
    }
  }
}

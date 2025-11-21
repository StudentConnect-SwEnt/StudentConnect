package com.github.se.studentconnect.ui.screen.signup

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.organization.SocialLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the various steps of the organization signup flow, each corresponding to a specific
 * screen in the onboarding process.
 */
enum class OrganizationSignupStep {
  BasicInfo,
  UploadLogo,
  Description,
  Brand,
  OrganizationProfile,
  TeamRoles
}

/**
 * Immutable data class holding all information entered by the user throughout the organization
 * signup flow.
 */
data class OrganizationSignupState(
    val createdBy: String? = null,
    // Basic Info Screen
    val name: String = "",
    val type: OrganizationType? = null,
    // Upload Logo Screen
    val logoUri: Uri? = null,
    // Description Screen
    val description: String? = null,
    // Brand Screen
    val website: String? = null,
    val instagram: String? = null,
    val x: String? = null,
    val linkedin: String? = null,
    // Organization Profile Screen
    val location: String? = null,
    val mainDomains: List<String> = emptyList(),
    val ageRanges: List<String> = emptyList(),
    val typicalEventSize: String? = null,
    // Team Roles Screen
    val roles: List<OrganizationRole> = emptyList(),
    // Navigation
    val currentStep: OrganizationSignupStep = OrganizationSignupStep.BasicInfo
)

/**
 * ViewModel managing the organization signup flow state.
 *
 * Handles user input updates, navigation between steps, and validation of individual form fields.
 */
class OrganizationSignupViewModel : ViewModel() {

  private val _state = mutableStateOf(OrganizationSignupState())
  val state: State<OrganizationSignupState> = _state

  private val _isUploadingLogo = MutableStateFlow(false)
  val isUploadingLogo: StateFlow<Boolean> = _isUploadingLogo.asStateFlow()

  private val _uploadError = MutableStateFlow<String?>(null)
  val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

  private fun update(block: (OrganizationSignupState) -> OrganizationSignupState) {
    _state.value = block(_state.value)
  }

  // Basic Info Screen updates
  fun setCreatedBy(userId: String) = update {
    it.copy(createdBy = userId.trim().ifBlank { null })
  }

  fun setName(name: String) = update { it.copy(name = name.trim()) }

  fun setType(type: OrganizationType?) = update { it.copy(type = type) }

  // Upload Logo Screen updates
  fun setLogoUri(uri: Uri?) = update {
    it.copy(logoUri = uri?.takeIf { uri -> uri.toString().isNotBlank() })
  }

  /**
   * Uploads the logo image to Firebase Storage.
   *
   * @param mediaRepository The media repository to use for uploading.
   * @param organizationId Optional organization ID. If null, uses createdBy user ID as temporary
   *   path.
   * @return The uploaded logo URL (storage path), or null if upload failed or no logo URI is set.
   */
  suspend fun uploadLogo(
      mediaRepository: MediaRepository,
      organizationId: String? = null
  ): String? {
    val logoUri = state.value.logoUri ?: return null
    val scheme = logoUri.scheme?.lowercase()

    if (scheme.isNullOrBlank()) {
      return null
    }

    // Skip upload for remote URIs (already uploaded)
    if (scheme == "http" || scheme == "https") {
      android.util.Log.w(
          "OrganizationSignupViewModel", "Skipping upload for remote logo URI: $logoUri")
      return null
    }

    _isUploadingLogo.value = true
    _uploadError.value = null

    return try {
      val userId = state.value.createdBy
          ?: throw IllegalStateException("Cannot upload logo: createdBy user ID is not set")

      // Use organization ID if available, otherwise use user ID as temporary path
      val path =
          if (organizationId != null) {
            "organizations/$organizationId/logo"
          } else {
            "organizations/$userId/logo"
          }

      val uploadId = mediaRepository.upload(logoUri, path)
      android.util.Log.d("OrganizationSignupViewModel", "Logo uploaded successfully: $uploadId")
      uploadId
    } catch (e: Exception) {
      android.util.Log.e("OrganizationSignupViewModel", "Failed to upload logo", e)
      _uploadError.value = "Failed to upload logo: ${e.message}"
      null
    } finally {
      _isUploadingLogo.value = false
    }
  }

  // Description Screen updates
  fun setDescription(description: String?) = update {
    it.copy(description = description?.ifBlank { null })
  }

  // Brand Screen updates
  fun setWebsite(website: String?) = update {
    it.copy(website = website?.trim()?.ifBlank { null })
  }

  fun setInstagram(instagram: String?) = update {
    it.copy(instagram = instagram?.trim()?.ifBlank { null })
  }

  fun setX(x: String?) = update { it.copy(x = x?.trim()?.ifBlank { null }) }

  fun setLinkedIn(linkedin: String?) = update {
    it.copy(linkedin = linkedin?.trim()?.ifBlank { null })
  }

  // Organization Profile Screen updates
  fun setLocation(location: String?) = update {
    it.copy(location = location?.trim()?.ifBlank { null })
  }

  fun setMainDomains(domains: List<String>) = update {
    it.copy(mainDomains = domains.map { it.trim() }.filter { it.isNotBlank() })
  }

  fun addMainDomain(domain: String) = update {
    val trimmed = domain.trim()
    if (trimmed.isBlank() || it.mainDomains.size >= 3) {
      it
    } else {
      it.copy(mainDomains = it.mainDomains + trimmed)
    }
  }

  fun removeMainDomain(domain: String) = update {
    it.copy(mainDomains = it.mainDomains - domain)
  }

  fun setAgeRanges(ranges: List<String>) = update {
    it.copy(ageRanges = ranges.map { it.trim() }.filter { it.isNotBlank() })
  }

  fun addAgeRange(range: String) = update {
    val trimmed = range.trim()
    if (trimmed.isBlank()) {
      it
    } else {
      it.copy(ageRanges = it.ageRanges + trimmed)
    }
  }

  fun removeAgeRange(range: String) = update {
    it.copy(ageRanges = it.ageRanges - range)
  }

  fun setTypicalEventSize(size: String?) = update {
    it.copy(typicalEventSize = size?.trim()?.ifBlank { null })
  }

  // Team Roles Screen updates
  fun setRoles(roles: List<OrganizationRole>) = update { it.copy(roles = roles) }

  fun addRole(role: OrganizationRole) = update {
    it.copy(roles = it.roles + role)
  }

  fun removeRole(role: OrganizationRole) = update {
    it.copy(roles = it.roles - role)
  }

  fun updateRole(index: Int, role: OrganizationRole) = update {
    if (index in it.roles.indices) {
      it.copy(roles = it.roles.toMutableList().apply { this[index] = role })
    } else {
      it
    }
  }

  // Section-level update methods
  /**
   * Updates all basic info fields at once.
   *
   * @param name The organization name.
   * @param type The organization type.
   */
  fun updateBasicInfo(
      name: String? = null,
      type: OrganizationType? = null
  ) = update {
    it.copy(
        name = name?.trim() ?: it.name,
        type = type ?: it.type)
  }

  /**
   * Updates all organization profile fields at once.
   *
   * @param location Optional location.
   * @param mainDomains List of main domains (max 3).
   * @param ageRanges List of age ranges.
   * @param typicalEventSize Optional typical event size.
   */
  fun updateOrganizationProfile(
      location: String? = null,
      mainDomains: List<String>? = null,
      ageRanges: List<String>? = null,
      typicalEventSize: String? = null
  ) = update {
    it.copy(
        location = location?.trim()?.ifBlank { null } ?: it.location,
        mainDomains =
            mainDomains?.map { domain -> domain.trim() }?.filter { it.isNotBlank() }
                ?: it.mainDomains,
        ageRanges =
            ageRanges?.map { range -> range.trim() }?.filter { it.isNotBlank() }
                ?: it.ageRanges,
        typicalEventSize = typicalEventSize?.trim()?.ifBlank { null } ?: it.typicalEventSize)
  }

  /**
   * Updates all branding/social links at once.
   *
   * @param website Optional website URL.
   * @param instagram Optional Instagram URL.
   * @param x Optional X (Twitter) URL.
   * @param linkedin Optional LinkedIn URL.
   */
  fun updateBranding(
      website: String? = null,
      instagram: String? = null,
      x: String? = null,
      linkedin: String? = null
  ) = update {
    it.copy(
        website = website?.trim()?.ifBlank { null } ?: it.website,
        instagram = instagram?.trim()?.ifBlank { null } ?: it.instagram,
        x = x?.trim()?.ifBlank { null } ?: it.x,
        linkedin = linkedin?.trim()?.ifBlank { null } ?: it.linkedin)
  }

  /**
   * Updates branding using a SocialLinks object.
   *
   * @param socialLinks The SocialLinks object containing all social media links.
   */
  fun updateBranding(socialLinks: SocialLinks) = update {
    it.copy(
        website = socialLinks.website,
        instagram = socialLinks.instagram,
        x = socialLinks.x,
        linkedin = socialLinks.linkedin)
  }

  // Navigation step helpers
  fun goTo(step: OrganizationSignupStep) = update { it.copy(currentStep = step) }

  fun nextStep() = update { it.copy(currentStep = it.currentStep.next()) }

  fun prevStep() = update { it.copy(currentStep = it.currentStep.prev()) }

  // Validation checks
  /**
   * Validates basic info section (name and type are required).
   */
  val isBasicInfoValid: Boolean
    get() =
        state.value.name.isNotBlank() &&
            state.value.name.length <= OrganizationModel.MAX_NAME_LENGTH &&
            state.value.type != null

  /**
   * Validates description section (optional, but must be within length limit if provided).
   */
  val isDescriptionValid: Boolean
    get() = (state.value.description?.length ?: 0) <= OrganizationModel.MAX_DESCRIPTION_LENGTH

  /**
   * Validates organization profile section.
   */
  val isOrganizationProfileValid: Boolean
    get() {
      val s = state.value
      // Main domains validation
      if (s.mainDomains.size > OrganizationModel.MAX_MAIN_DOMAINS) return false
      if (s.mainDomains.any { it.isBlank() || it.length > OrganizationModel.MAX_DOMAIN_LENGTH }) {
        return false
      }
      // Age ranges validation
      if (s.ageRanges.any { it.isBlank() || it.length > OrganizationModel.MAX_AGE_RANGE_LENGTH }) {
        return false
      }
      // Location validation
      if (s.location != null && s.location.length > OrganizationModel.MAX_LOCATION_LENGTH) {
        return false
      }
      // Typical event size validation
      if (s.typicalEventSize != null &&
          s.typicalEventSize.length > OrganizationModel.MAX_EVENT_SIZE_LENGTH) {
        return false
      }
      return true
    }

  /**
   * Validates main domains (max 3, each non-blank and within length limit).
   */
  val isMainDomainsValid: Boolean
    get() =
        state.value.mainDomains.size <= OrganizationModel.MAX_MAIN_DOMAINS &&
            state.value.mainDomains.all { it.isNotBlank() && it.length <= OrganizationModel.MAX_DOMAIN_LENGTH }

  /**
   * Validates age ranges (each non-blank and within length limit).
   */
  val isAgeRangesValid: Boolean
    get() =
        state.value.ageRanges.all { it.isNotBlank() && it.length <= OrganizationModel.MAX_AGE_RANGE_LENGTH }

  /**
   * Validates location (optional, but must be within length limit if provided).
   */
  val isLocationValid: Boolean
    get() = (state.value.location?.length ?: 0) <= OrganizationModel.MAX_LOCATION_LENGTH

  /**
   * Validates typical event size (optional, but must be within length limit if provided).
   */
  val isTypicalEventSizeValid: Boolean
    get() = (state.value.typicalEventSize?.length ?: 0) <= OrganizationModel.MAX_EVENT_SIZE_LENGTH

  /**
   * Validates branding/social links section (all URLs must be valid if provided).
   */
  val isBrandingValid: Boolean
    get() {
      val s = state.value
      return isValidUrl(s.website) &&
          isValidUrl(s.instagram) &&
          isValidUrl(s.x) &&
          isValidUrl(s.linkedin)
    }

  /**
   * Validates roles section (all roles must be valid).
   */
  val isRolesValid: Boolean
    get() = state.value.roles.all { role ->
      role.name.isNotBlank() &&
          role.name.length <= OrganizationRole.MAX_NAME_LENGTH &&
          (role.description == null || role.description.length <= OrganizationRole.MAX_DESCRIPTION_LENGTH)
    }

  /**
   * Validates if a URL is valid (null is considered valid as URLs are optional).
   */
  private fun isValidUrl(url: String?): Boolean {
    if (url == null || url.isBlank()) return true // Optional field
    return try {
      val uri = java.net.URI(url)
      uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
    } catch (e: Exception) {
      false
    }
  }

  /**
   * Comprehensive validation for final submission.
   * Checks all required fields and validates optional fields if provided.
   *
   * @return true if all required fields are valid and optional fields are valid (if provided).
   */
  fun isValidForSubmission(): Boolean {
    val s = state.value

    // Required fields
    if (s.createdBy.isNullOrBlank()) return false
    if (!isBasicInfoValid) return false

    // Optional fields validation (only if provided)
    if (!isDescriptionValid) return false
    if (!isOrganizationProfileValid) return false
    if (!isBrandingValid) return false
    if (!isRolesValid) return false

    return true
  }

  /**
   * Gets a list of validation errors for debugging purposes.
   *
   * @return List of validation error messages.
   */
  fun getValidationErrors(): List<String> {
    val errors = mutableListOf<String>()
    val s = state.value

    // Required fields
    if (s.createdBy.isNullOrBlank()) {
      errors.add("Created by user ID is required")
    }
    if (s.name.isBlank()) {
      errors.add("Organization name is required")
    } else if (s.name.length > OrganizationModel.MAX_NAME_LENGTH) {
      errors.add("Organization name cannot exceed ${OrganizationModel.MAX_NAME_LENGTH} characters")
    }
    if (s.type == null) {
      errors.add("Organization type is required")
    }

    // Optional fields
    if (s.description != null && s.description.length > OrganizationModel.MAX_DESCRIPTION_LENGTH) {
      errors.add("Description cannot exceed ${OrganizationModel.MAX_DESCRIPTION_LENGTH} characters")
    }
    if (s.mainDomains.size > OrganizationModel.MAX_MAIN_DOMAINS) {
      errors.add("Main domains cannot exceed ${OrganizationModel.MAX_MAIN_DOMAINS} items")
    }
    s.mainDomains.forEachIndexed { index, domain ->
      if (domain.isBlank()) {
        errors.add("Main domain at index $index cannot be blank")
      } else if (domain.length > OrganizationModel.MAX_DOMAIN_LENGTH) {
        errors.add("Main domain at index $index cannot exceed ${OrganizationModel.MAX_DOMAIN_LENGTH} characters")
      }
    }
    s.ageRanges.forEachIndexed { index, range ->
      if (range.isBlank()) {
        errors.add("Age range at index $index cannot be blank")
      } else if (range.length > OrganizationModel.MAX_AGE_RANGE_LENGTH) {
        errors.add("Age range at index $index cannot exceed ${OrganizationModel.MAX_AGE_RANGE_LENGTH} characters")
      }
    }
    if (s.location != null && s.location.length > OrganizationModel.MAX_LOCATION_LENGTH) {
      errors.add("Location cannot exceed ${OrganizationModel.MAX_LOCATION_LENGTH} characters")
    }
    if (s.typicalEventSize != null && s.typicalEventSize.length > OrganizationModel.MAX_EVENT_SIZE_LENGTH) {
      errors.add("Typical event size cannot exceed ${OrganizationModel.MAX_EVENT_SIZE_LENGTH} characters")
    }
    if (!isValidUrl(s.website)) {
      errors.add("Website URL is invalid")
    }
    if (!isValidUrl(s.instagram)) {
      errors.add("Instagram URL is invalid")
    }
    if (!isValidUrl(s.x)) {
      errors.add("X (Twitter) URL is invalid")
    }
    if (!isValidUrl(s.linkedin)) {
      errors.add("LinkedIn URL is invalid")
    }
    s.roles.forEachIndexed { index, role ->
      if (role.name.isBlank()) {
        errors.add("Role at index $index: name cannot be blank")
      } else if (role.name.length > OrganizationRole.MAX_NAME_LENGTH) {
        errors.add("Role at index $index: name cannot exceed ${OrganizationRole.MAX_NAME_LENGTH} characters")
      }
      if (role.description != null && role.description.length > OrganizationRole.MAX_DESCRIPTION_LENGTH) {
        errors.add("Role at index $index: description cannot exceed ${OrganizationRole.MAX_DESCRIPTION_LENGTH} characters")
      }
    }

    return errors
  }

  fun reset() = update { OrganizationSignupState() }
}

/** Returns the next logical step in the organization signup sequence. */
private fun OrganizationSignupStep.next(): OrganizationSignupStep =
    when (this) {
      OrganizationSignupStep.BasicInfo -> OrganizationSignupStep.UploadLogo
      OrganizationSignupStep.UploadLogo -> OrganizationSignupStep.Description
      OrganizationSignupStep.Description -> OrganizationSignupStep.Brand
      OrganizationSignupStep.Brand -> OrganizationSignupStep.OrganizationProfile
      OrganizationSignupStep.OrganizationProfile -> OrganizationSignupStep.TeamRoles
      OrganizationSignupStep.TeamRoles -> OrganizationSignupStep.TeamRoles
    }

/** Returns the previous step in the organization signup sequence. */
private fun OrganizationSignupStep.prev(): OrganizationSignupStep =
    when (this) {
      OrganizationSignupStep.BasicInfo -> OrganizationSignupStep.BasicInfo
      OrganizationSignupStep.UploadLogo -> OrganizationSignupStep.BasicInfo
      OrganizationSignupStep.Description -> OrganizationSignupStep.UploadLogo
      OrganizationSignupStep.Brand -> OrganizationSignupStep.Description
      OrganizationSignupStep.OrganizationProfile -> OrganizationSignupStep.Brand
      OrganizationSignupStep.TeamRoles -> OrganizationSignupStep.OrganizationProfile
    }


package com.github.se.studentconnect.ui.screen.signup

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.organization.SocialLinks

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
    val bannerUri: Uri? = null,
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

  private fun update(block: (OrganizationSignupState) -> OrganizationSignupState) {
    _state.value = block(_state.value)
  }

  // Basic Info Screen updates
  fun setCreatedBy(userId: String) = update {
    it.copy(createdBy = userId.trim().ifBlank { null })
  }

  fun setName(name: String) = update { it.copy(name = name.trim()) }

  fun setType(type: OrganizationType?) = update { it.copy(type = type) }

  fun setBannerUri(uri: Uri?) = update {
    it.copy(bannerUri = uri?.takeIf { uri -> uri.toString().isNotBlank() })
  }

  // Upload Logo Screen updates
  fun setLogoUri(uri: Uri?) = update {
    it.copy(logoUri = uri?.takeIf { uri -> uri.toString().isNotBlank() })
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
   * @param bannerUri Optional banner image URI.
   */
  fun updateBasicInfo(
      name: String? = null,
      type: OrganizationType? = null,
      bannerUri: Uri? = null
  ) = update {
    it.copy(
        name = name?.trim() ?: it.name,
        type = type ?: it.type,
        bannerUri = bannerUri?.takeIf { uri -> uri.toString().isNotBlank() } ?: it.bannerUri)
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
  val isBasicInfoValid: Boolean
    get() =
        state.value.name.isNotBlank() &&
            state.value.name.length <= 200 &&
            state.value.type != null

  val isDescriptionValid: Boolean
    get() = (state.value.description?.length ?: 0) <= 2000

  val isMainDomainsValid: Boolean
    get() = state.value.mainDomains.size <= 3

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


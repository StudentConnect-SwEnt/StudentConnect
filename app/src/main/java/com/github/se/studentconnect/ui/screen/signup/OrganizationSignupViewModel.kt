package com.github.se.studentconnect.ui.screen.signup.organization

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRole
import com.github.se.studentconnect.model.organization.OrganizationType
import com.github.se.studentconnect.model.organization.SocialLinks
import com.google.firebase.Timestamp

/** Represents the sequential steps of the organization signup flow. */
enum class OrganizationSignUpStep {
  Info,
  Logo,
  Description,
  Socials,
  Team,
  ProfileSetup
}

/**
 * Immutable state holding all information entered by the organization during signup. Uses types
 * from OrganizationModel where possible for consistency.
 */
data class OrganizationSignUpState(
    val organizationName: String = "",
    val organizationType: OrganizationType? = null,
    val logoUri: Uri? = null,
    val description: String = "",
    val websiteUrl: String = "",
    val instagramHandle: String = "",
    val xHandle: String = "",
    val linkedinUrl: String = "",
    val teamRoles: List<OrganizationRole> = emptyList(),
    val location: String = "",
    val domains: Set<String> = emptySet(),
    val targetAgeRanges: Set<String> = emptySet(),
    val eventSize: String? = null,
    val currentStep: OrganizationSignUpStep = OrganizationSignUpStep.Info
)

/** ViewModel managing the organization signup flow state. */
class OrganizationSignUpViewModel : ViewModel() {

  private val _state = mutableStateOf(OrganizationSignUpState())
  val state: State<OrganizationSignUpState> = _state

  private fun update(block: (OrganizationSignUpState) -> OrganizationSignUpState) {
    _state.value = block(_state.value)
  }

  /**
   * Sets the organization name, trimming leading/trailing whitespace.
   *
   * @param name The organization name entered by the user.
   */
  fun setOrganizationName(name: String) = update { it.copy(organizationName = name.trim()) }

  /**
   * Sets the organization type.
   *
   * @param type The organization type selected by the user.
   */
  fun setOrganizationType(type: OrganizationType) = update { it.copy(organizationType = type) }

  /**
   * Sets the logo URI.
   *
   * @param uri The URI of the logo image selected by the user.
   */
  fun setLogoUri(uri: Uri?) = update {
    it.copy(logoUri = uri?.takeIf { uri -> uri.toString().isNotBlank() })
  }

  /**
   * Sets the organization description.
   *
   * @param desc The description text entered by the user.
   */
  fun setDescription(desc: String) = update { it.copy(description = desc) }

  /**
   * Sets the organization website URL, trimming leading/trailing whitespace.
   *
   * @param url The website URL entered by the user.
   */
  fun setWebsite(url: String) = update { it.copy(websiteUrl = url.trim()) }

  /**
   * Sets the Instagram handle, trimming leading/trailing whitespace.
   *
   * @param handle The Instagram handle entered by the user.
   */
  fun setInstagram(handle: String) = update { it.copy(instagramHandle = handle.trim()) }

  /**
   * Sets the X (Twitter) handle, trimming leading/trailing whitespace.
   *
   * @param handle The X handle entered by the user.
   */
  fun setX(handle: String) = update { it.copy(xHandle = handle.trim()) }

  /**
   * Sets the LinkedIn URL, trimming leading/trailing whitespace.
   *
   * @param url The LinkedIn URL entered by the user.
   */
  fun setLinkedin(url: String) = update { it.copy(linkedinUrl = url.trim()) }

  /**
   * Adds a new role to the team roles list.
   *
   * @param role The [OrganizationRole] to add.
   */
  fun addRole(role: OrganizationRole) = update { it.copy(teamRoles = listOf(role) + it.teamRoles) }

  /**
   * Removes a role from the team roles list.
   *
   * @param role The [OrganizationRole] to remove.
   */
  fun removeRole(role: OrganizationRole) = update {
    it.copy(teamRoles = it.teamRoles.filterNot { r -> r == role })
  }

  /**
   * Sets the entire list of team roles.
   *
   * @param roles The list of [OrganizationRole]s to set.
   */
  fun setRoles(roles: List<OrganizationRole>) = update { it.copy(teamRoles = roles) }

  /**
   * Sets the location string.
   *
   * @param location The location entered by the user.
   */
  fun setLocation(location: String) = update { it.copy(location = location) }

  /**
   * Toggles a domain in the selected domains set.
   *
   * @param domainKey The domain key to toggle.
   */
  fun toggleDomain(domainKey: String) = update { it.copy(domains = it.domains.toggle(domainKey)) }

  /**
   * Toggles an age range in the selected target age ranges set.
   *
   * @param ageKey The age range key to toggle.
   */
  fun toggleAgeRange(ageKey: String) = update {
    it.copy(targetAgeRanges = it.targetAgeRanges.toggle(ageKey))
  }

  /**
   * Sets the typical event size.
   *
   * @param size The event size selected by the user.
   */
  fun setEventSize(size: String?) = update { it.copy(eventSize = size) }

  /**
   * Navigates to a specific step in the signup flow.
   *
   * @param step The [OrganizationSignUpStep] to navigate to.
   */
  fun goTo(step: OrganizationSignUpStep) = update { it.copy(currentStep = step) }

  fun nextStep() = update { it.copy(currentStep = it.currentStep.next()) }

  /** Navigates to the previous step in the signup flow. */
  fun prevStep() = update { it.copy(currentStep = it.currentStep.prev()) }

  val isInfoValid: Boolean
    get() = state.value.organizationName.isNotBlank() && state.value.organizationType != null

  val isProfileSetupValid: Boolean
    get() =
        state.value.location.isNotBlank() &&
            state.value.domains.isNotEmpty() &&
            state.value.eventSize != null

  /**
   * Creates the final [Organization] from the current state. This should be called when the user
   * clicks "Finish" or "Submit".
   *
   * @param orgId The unique ID generated for this organization (e.g. from Auth or Firestore).
   * @param currentUserId The ID of the currently logged-in user (the creator).
   * @param uploadedLogoUrl The download URL of the logo after it has been uploaded to Storage
   *   (nullable).
   */
  fun createOrganizationModel(
      orgId: String,
      currentUserId: String,
      uploadedLogoUrl: String?
  ): Organization {
    val s = state.value

    return Organization(
        id = orgId,
        name = s.organizationName,
        type = s.organizationType ?: OrganizationType.Other,
        description = s.description.ifBlank { null },
        logoUrl = uploadedLogoUrl,
        location = s.location.ifBlank { null },
        mainDomains = s.domains.toList(),
        ageRanges = s.targetAgeRanges.toList(),
        typicalEventSize = s.eventSize,
        roles = s.teamRoles,
        socialLinks =
            SocialLinks(
                website = s.websiteUrl.ifBlank { null },
                instagram = s.instagramHandle.ifBlank { null },
                x = s.xHandle.ifBlank { null },
                linkedin = s.linkedinUrl.ifBlank { null }),
        createdAt = Timestamp.now(),
        createdBy = currentUserId)
  }

  private fun <T> Set<T>.toggle(item: T): Set<T> {
    return if (contains(item)) minus(item) else plus(item)
  }

  /** Resets the signup state to the initial default values. */
  fun reset() = update { OrganizationSignUpState() }

  /**
   * Toggle the selected organization type. If the provided type is already selected, clears it;
   * otherwise sets it.
   */
  fun toggleOrganizationType(type: OrganizationType) = update {
    it.copy(organizationType = if (it.organizationType == type) null else type)
  }
}

private fun OrganizationSignUpStep.next(): OrganizationSignUpStep =
    when (this) {
      OrganizationSignUpStep.Info -> OrganizationSignUpStep.Logo
      OrganizationSignUpStep.Logo -> OrganizationSignUpStep.Description
      OrganizationSignUpStep.Description -> OrganizationSignUpStep.Socials
      OrganizationSignUpStep.Socials -> OrganizationSignUpStep.Team
      OrganizationSignUpStep.Team -> OrganizationSignUpStep.ProfileSetup
      OrganizationSignUpStep.ProfileSetup -> OrganizationSignUpStep.ProfileSetup
    }

private fun OrganizationSignUpStep.prev(): OrganizationSignUpStep =
    when (this) {
      OrganizationSignUpStep.Info -> OrganizationSignUpStep.Info
      OrganizationSignUpStep.Logo -> OrganizationSignUpStep.Info
      OrganizationSignUpStep.Description -> OrganizationSignUpStep.Logo
      OrganizationSignUpStep.Socials -> OrganizationSignUpStep.Description
      OrganizationSignUpStep.Team -> OrganizationSignUpStep.Socials
      OrganizationSignUpStep.ProfileSetup -> OrganizationSignUpStep.Team
    }

package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import com.github.se.studentconnect.model.location.Location
import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents the UI state for the Event Creation screens. This is a sealed class to handle
 * different event types (Public vs Private) while sharing common properties.
 */
sealed class CreateEventUiState {
  abstract val title: String
  abstract val description: String
  abstract val location: Location?

  abstract val startDate: LocalDate?
  abstract val startTime: LocalTime

  abstract val endDate: LocalDate?
  abstract val endTime: LocalTime

  abstract val numberOfParticipantsString: String
  abstract val hasParticipationFee: Boolean
  abstract val participationFeeString: String
  abstract val isFlash: Boolean
  abstract val flashDurationHours: Int
  abstract val flashDurationMinutes: Int
  abstract val finishedSaving: Boolean
  abstract val isSaving: Boolean
  abstract val bannerImageUri: Uri?
  abstract val bannerImagePath: String?
  abstract val shouldRemoveBanner: Boolean
  abstract val createAsOrganization: Boolean
  abstract val selectedOrganizationId: String?
  abstract val userOrganizations: List<Pair<String, String>> // List of (id, name) pairs

  /** UI State for creating a Public Event. */
  data class Public(
      override val title: String = "",
      override val description: String = "",
      override val location: Location? = null,
      override val startDate: LocalDate? = null,
      override val startTime: LocalTime = LocalTime.of(0, 0),
      override val endDate: LocalDate? = null,
      override val endTime: LocalTime = LocalTime.of(0, 0),
      override val numberOfParticipantsString: String = "",
      override val hasParticipationFee: Boolean = false,
      override val participationFeeString: String = "",
      override val isFlash: Boolean = false,
      override val flashDurationHours: Int = 1,
      override val flashDurationMinutes: Int = 0,
      override val finishedSaving: Boolean = false,
      override val isSaving: Boolean = false,
      override val bannerImageUri: Uri? = null,
      override val bannerImagePath: String? = null,
      override val shouldRemoveBanner: Boolean = false,
      override val createAsOrganization: Boolean = false,
      override val selectedOrganizationId: String? = null,
      override val userOrganizations: List<Pair<String, String>> = emptyList(),
      val subtitle: String = "",
      val website: String = "",
      val tags: List<String> = emptyList(),
  ) : CreateEventUiState()

  /** UI State for creating a Private Event. */
  data class Private(
      override val title: String = "",
      override val description: String = "",
      override val location: Location? = null,
      override val startDate: LocalDate? = null,
      override val startTime: LocalTime = LocalTime.of(0, 0),
      override val endDate: LocalDate? = null,
      override val endTime: LocalTime = LocalTime.of(0, 0),
      override val numberOfParticipantsString: String = "",
      override val hasParticipationFee: Boolean = false,
      override val participationFeeString: String = "",
      override val isFlash: Boolean = false,
      override val flashDurationHours: Int = 1,
      override val flashDurationMinutes: Int = 0,
      override val finishedSaving: Boolean = false,
      override val isSaving: Boolean = false,
      override val bannerImageUri: Uri? = null,
      override val bannerImagePath: String? = null,
      override val shouldRemoveBanner: Boolean = false,
      override val createAsOrganization: Boolean = false,
      override val selectedOrganizationId: String? = null,
      override val userOrganizations: List<Pair<String, String>> = emptyList(),
  ) : CreateEventUiState()
}

/**
 * Helper extension to copy shared properties across the sealed hierarchy. This enables the
 * BaseViewModel to update common fields without casting to a specific subclass.
 *
 * Suppressed LongParameterList: This function acts as a unified copy constructor for the sealed
 * hierarchy. It requires all common fields to be present.
 */
@Suppress("LongParameterList")
fun CreateEventUiState.copyCommon(
    title: String = this.title,
    description: String = this.description,
    location: Location? = this.location,
    startDate: LocalDate? = this.startDate,
    startTime: LocalTime = this.startTime,
    endDate: LocalDate? = this.endDate,
    endTime: LocalTime = this.endTime,
    numberOfParticipantsString: String = this.numberOfParticipantsString,
    hasParticipationFee: Boolean = this.hasParticipationFee,
    participationFeeString: String = this.participationFeeString,
    isFlash: Boolean = this.isFlash,
    flashDurationHours: Int = this.flashDurationHours,
    flashDurationMinutes: Int = this.flashDurationMinutes,
    finishedSaving: Boolean = this.finishedSaving,
    isSaving: Boolean = this.isSaving,
    bannerImageUri: Uri? = this.bannerImageUri,
    bannerImagePath: String? = this.bannerImagePath,
    shouldRemoveBanner: Boolean = this.shouldRemoveBanner,
    createAsOrganization: Boolean = this.createAsOrganization,
    selectedOrganizationId: String? = this.selectedOrganizationId,
    userOrganizations: List<Pair<String, String>> = this.userOrganizations
): CreateEventUiState {
  return when (this) {
    is CreateEventUiState.Public ->
        copy(
            title = title,
            description = description,
            location = location,
            startDate = startDate,
            startTime = startTime,
            endDate = endDate,
            endTime = endTime,
            numberOfParticipantsString = numberOfParticipantsString,
            hasParticipationFee = hasParticipationFee,
            participationFeeString = participationFeeString,
            isFlash = isFlash,
            flashDurationHours = flashDurationHours,
            flashDurationMinutes = flashDurationMinutes,
            finishedSaving = finishedSaving,
            isSaving = isSaving,
            bannerImageUri = bannerImageUri,
            bannerImagePath = bannerImagePath,
            shouldRemoveBanner = shouldRemoveBanner,
            createAsOrganization = createAsOrganization,
            selectedOrganizationId = selectedOrganizationId,
            userOrganizations = userOrganizations)
    is CreateEventUiState.Private ->
        copy(
            title = title,
            description = description,
            location = location,
            startDate = startDate,
            startTime = startTime,
            endDate = endDate,
            endTime = endTime,
            numberOfParticipantsString = numberOfParticipantsString,
            hasParticipationFee = hasParticipationFee,
            participationFeeString = participationFeeString,
            isFlash = isFlash,
            flashDurationHours = flashDurationHours,
            flashDurationMinutes = flashDurationMinutes,
            finishedSaving = finishedSaving,
            isSaving = isSaving,
            bannerImageUri = bannerImageUri,
            bannerImagePath = bannerImagePath,
            shouldRemoveBanner = shouldRemoveBanner,
            createAsOrganization = createAsOrganization,
            selectedOrganizationId = selectedOrganizationId,
            userOrganizations = userOrganizations)
  }
}

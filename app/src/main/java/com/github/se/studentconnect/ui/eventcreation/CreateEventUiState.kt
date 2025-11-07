package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import com.github.se.studentconnect.model.location.Location
import java.time.LocalDate
import java.time.LocalTime

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
  abstract val finishedSaving: Boolean
  abstract val isSaving: Boolean
  abstract val bannerImageUri: Uri?
  abstract val bannerImagePath: String?
  abstract val shouldRemoveBanner: Boolean

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
      override val finishedSaving: Boolean = false,
      override val isSaving: Boolean = false,
      override val bannerImageUri: Uri? = null,
      override val bannerImagePath: String? = null,
      override val shouldRemoveBanner: Boolean = false,
      val subtitle: String = "",
      val website: String = "",
      val tags: List<String> = emptyList(),
  ) : CreateEventUiState()

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
      override val finishedSaving: Boolean = false,
      override val isSaving: Boolean = false,
      override val bannerImageUri: Uri? = null,
      override val bannerImagePath: String? = null,
      override val shouldRemoveBanner: Boolean = false,
  ) : CreateEventUiState()
}

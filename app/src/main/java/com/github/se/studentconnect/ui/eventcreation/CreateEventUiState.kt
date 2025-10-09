package com.github.se.studentconnect.ui.eventcreation

import java.time.LocalTime

sealed class CreateEventUiState {
  abstract val title: String
  abstract val description: String
  abstract val locationString: String

  abstract val startDateString: String
  abstract val startTime: LocalTime

  abstract val endDateString: String
  abstract val endTime: LocalTime

  abstract val numberOfParticipantsString: String
  abstract val hasParticipationFee: Boolean
  abstract val participationFeeString: String
  abstract val isFlash: Boolean

  data class Public(
      override val title: String = "",
      override val description: String = "",
      override val locationString: String = "",
      override val startDateString: String = "",
      override val startTime: LocalTime = LocalTime.of(0, 0),
      override val endDateString: String = "",
      override val endTime: LocalTime = LocalTime.of(0, 0),
      override val numberOfParticipantsString: String = "",
      override val hasParticipationFee: Boolean = false,
      override val participationFeeString: String = "",
      override val isFlash: Boolean = false,
      val subtitle: String = "",
      val website: String = "",
      val tags: List<String> = emptyList(),
  ) : CreateEventUiState()

  data class Private(
      override val title: String = "",
      override val description: String = "",
      override val locationString: String = "",
      override val startDateString: String = "",
      override val startTime: LocalTime = LocalTime.of(0, 0),
      override val endDateString: String = "",
      override val endTime: LocalTime = LocalTime.of(0, 0),
      override val numberOfParticipantsString: String = "",
      override val hasParticipationFee: Boolean = false,
      override val participationFeeString: String = "",
      override val isFlash: Boolean = false,
  ) : CreateEventUiState()
}

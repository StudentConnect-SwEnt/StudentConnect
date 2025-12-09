package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.launch

/** ViewModel for creating and editing Private events. Manages [CreateEventUiState.Private]. */
class CreatePrivateEventViewModel :
    BaseCreateEventViewModel<CreateEventUiState.Private>(CreateEventUiState.Private()) {

  override fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event {
    val s = uiState.value
    val start = timestampFrom(s.startDate!!, s.startTime)
    val end = timestampFrom(s.endDate!!, s.endTime)
    val maxCapacity =
        try {
          s.numberOfParticipantsString.toUInt()
        } catch (_: Exception) {
          null
        }
    val fee =
        try {
          s.participationFeeString.toUInt()
        } catch (_: Exception) {
          null
        }
    return Event.Private(
        uid = uid,
        ownerId = ownerId,
        title = s.title,
        description = s.description,
        imageUrl = bannerPath,
        location = s.location,
        start = start,
        end = end,
        maxCapacity = maxCapacity,
        participationFee = fee,
        isFlash = s.isFlash)
  }

  /**
   * Pre-fills the form with data from an existing event as a template. Unlike prefill(), this does
   * NOT set start/end dates and does NOT set editingEventUid, so saving will create a NEW event.
   */
  override fun prefillFromTemplate(event: Event) {
    _uiState.value =
        CreateEventUiState.Private(
            title = event.title,
            description = event.description,
            location = event.location,
            startDate = null, // Clear dates for template
            startTime = java.time.LocalTime.of(0, 0),
            endDate = null,
            endTime = java.time.LocalTime.of(0, 0),
            numberOfParticipantsString = event.maxCapacity?.toString() ?: "",
            hasParticipationFee = event.participationFee != null,
            participationFeeString = event.participationFee?.toString() ?: "",
            isFlash = event.isFlash,
            bannerImagePath = event.imageUrl,
        )
  }

  override fun loadEvent(eventUid: String) {
    super.loadEvent(eventUid)
    viewModelScope.launch {
      val event = eventRepository.getEvent(eventUid)
      if (event is Event.Private) prefill(event)
    }
  }

  private fun prefill(event: Event.Private) {
    val startDT = event.start.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val endDT =
        (event.end ?: event.start)
            .toDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

    _uiState.value =
        CreateEventUiState.Private(
            title = event.title,
            description = event.description,
            location = event.location,
            startDate = startDT.toLocalDate(),
            startTime = startDT.toLocalTime(),
            endDate = endDT.toLocalDate(),
            endTime = endDT.toLocalTime(),
            numberOfParticipantsString = event.maxCapacity?.toString() ?: "",
            hasParticipationFee = event.participationFee != null,
            participationFeeString = event.participationFee?.toString() ?: "",
            isFlash = event.isFlash,
            bannerImagePath = event.imageUrl)
  }

  private fun timestampFrom(date: LocalDate, time: LocalTime): Timestamp {
    val instant = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()
    return Timestamp(instant)
  }
}

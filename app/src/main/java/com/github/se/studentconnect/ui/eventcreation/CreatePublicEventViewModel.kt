package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.launch

/** ViewModel for creating and editing Public events. Manages [CreateEventUiState.Public]. */
class CreatePublicEventViewModel :
    BaseCreateEventViewModel<CreateEventUiState.Public>(CreateEventUiState.Public()) {

  // Public specific updates
  fun updateSubtitle(newSubtitle: String) {
    _uiState.value = uiState.value.copy(subtitle = newSubtitle)
  }

  fun updateWebsite(newWebsite: String) {
    _uiState.value = uiState.value.copy(website = newWebsite)
  }

  fun updateTags(newTags: List<String>) {
    _uiState.value = uiState.value.copy(tags = newTags)
  }

  /**
   * Pre-fills the form with data from an existing event as a template. Unlike prefill(), this does
   * NOT set start/end dates and does NOT set editingEventUid, so saving will create a NEW event.
   */
  override fun prefillFromTemplate(event: Event) {
    val publicEvent = event as? Event.Public ?: return
    _uiState.value =
        CreateEventUiState.Public(
            title = publicEvent.title,
            description = publicEvent.description,
            location = publicEvent.location,
            startDate = null,
            startTime = LocalTime.of(0, 0),
            endDate = null,
            endTime = LocalTime.of(0, 0),
            numberOfParticipantsString = publicEvent.maxCapacity?.toString() ?: "",
            hasParticipationFee = publicEvent.participationFee != null,
            participationFeeString = publicEvent.participationFee?.toString() ?: "",
            isFlash = publicEvent.isFlash,
            flashDurationHours = 1,
            flashDurationMinutes = 0,
            subtitle = publicEvent.subtitle,
            website = publicEvent.website.orEmpty(),
            tags = publicEvent.tags,
            bannerImagePath = publicEvent.imageUrl,
        )
  }

  override fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event {
    val s = uiState.value
    val start =
        if (s.isFlash && editingEventUid == null) {
          // Flash events start immediately
          Timestamp.now()
        } else {
          timestampFrom(s.startDate!!, s.startTime)
        }
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

    return Event.Public(
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
        isFlash = s.isFlash,
        subtitle = s.subtitle,
        tags = s.tags,
        website = s.website)
  }

  override fun loadEvent(eventUid: String) {
    super.loadEvent(eventUid)
    viewModelScope.launch {
      val event = eventRepository.getEvent(eventUid)
      if (event is Event.Public) prefill(event)
    }
  }

  private fun prefill(event: Event.Public) {
    val startDT = event.start.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val endDT =
        (event.end ?: event.start)
            .toDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

    _uiState.value =
        CreateEventUiState.Public(
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
            flashDurationHours =
                if (event.isFlash) {
                  // Calculate duration from event start/end
                  val durationMs =
                      (event.end?.toDate()?.time ?: event.start.toDate().time) -
                          event.start.toDate().time
                  val totalMinutes = (durationMs / (1000 * 60)).toInt()
                  totalMinutes / 60
                } else {
                  1
                },
            flashDurationMinutes =
                if (event.isFlash) {
                  val durationMs =
                      (event.end?.toDate()?.time ?: event.start.toDate().time) -
                          event.start.toDate().time
                  val totalMinutes = (durationMs / (1000 * 60)).toInt()
                  totalMinutes % 60
                } else {
                  0
                },
            bannerImagePath = event.imageUrl,
            subtitle = event.subtitle,
            website = event.website.orEmpty(),
            tags = event.tags)
  }
}

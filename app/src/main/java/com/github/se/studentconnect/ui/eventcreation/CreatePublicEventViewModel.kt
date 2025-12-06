package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.launch

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

  override suspend fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event {
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
            bannerImagePath = event.imageUrl,
            subtitle = event.subtitle,
            website = event.website.orEmpty(),
            tags = event.tags)
  }

  private fun timestampFrom(date: LocalDate, time: LocalTime): Timestamp {
    val instant = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()
    return Timestamp(instant)
  }
}

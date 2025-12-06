package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePublicEventViewModel : BaseCreateEventViewModel<CreateEventUiState.Public>() {

  private val _uiState = MutableStateFlow(CreateEventUiState.Public())
  val uiState: StateFlow<CreateEventUiState.Public> = _uiState.asStateFlow()

  fun updateTitle(newTitle: String) {
    _uiState.value = uiState.value.copy(title = newTitle)
  }

  fun updateDescription(newDescription: String) {
    _uiState.value = uiState.value.copy(description = newDescription)
  }

  fun updateLocation(newLocation: Location?) {
    _uiState.value = uiState.value.copy(location = newLocation)
  }

  fun updateStartDate(newStartDate: LocalDate?) {
    _uiState.value = uiState.value.copy(startDate = newStartDate)
  }

  fun updateStartTime(newStartTime: LocalTime) {
    _uiState.value = uiState.value.copy(startTime = newStartTime)
  }

  fun updateEndDate(newEndDate: LocalDate?) {
    _uiState.value = uiState.value.copy(endDate = newEndDate)
  }

  fun updateEndTime(newEndTime: LocalTime) {
    _uiState.value = uiState.value.copy(endTime = newEndTime)
  }

  fun updateNumberOfParticipantsString(newNumberOfParticipantsString: String) {
    _uiState.value = uiState.value.copy(numberOfParticipantsString = newNumberOfParticipantsString)
  }

  fun updateHasParticipationFee(newHasParticipationFee: Boolean) {
    _uiState.value = uiState.value.copy(hasParticipationFee = newHasParticipationFee)
  }

  fun updateParticipationFeeString(newParticipationFeeString: String) {
    _uiState.value = uiState.value.copy(participationFeeString = newParticipationFeeString)
  }

  fun updateIsFlash(newIsFlash: Boolean) {
    _uiState.value = uiState.value.copy(isFlash = newIsFlash)
  }

  fun updateBannerImageUri(newUri: Uri) {
    _uiState.value = uiState.value.copy(bannerImageUri = newUri, shouldRemoveBanner = false)
  }

  fun removeBannerImage() {
    _uiState.value =
        uiState.value.copy(bannerImageUri = null, bannerImagePath = null, shouldRemoveBanner = true)
  }

  // Public specific
  fun updateSubtitle(newSubtitle: String) {
    _uiState.value = uiState.value.copy(subtitle = newSubtitle)
  }

  fun updateWebsite(newWebsite: String) {
    _uiState.value = uiState.value.copy(website = newWebsite)
  }

  fun updateTags(newTags: List<String>) {
    _uiState.value = uiState.value.copy(tags = newTags)
  }

  // --- Base Implementation ---
  override fun getCurrentState() = uiState.value

  override fun updateIsSaving(isSaving: Boolean) {
    _uiState.value = uiState.value.copy(isSaving = isSaving)
  }

  override fun updateFinishedSaving(finished: Boolean) {
    _uiState.value = uiState.value.copy(finishedSaving = finished)
  }

  override fun updateBannerAfterSave(path: String?) {
    _uiState.value =
        uiState.value.copy(
            bannerImageUri = null, bannerImagePath = path, shouldRemoveBanner = false)
  }

  override fun validateState(): Boolean {
    val s = uiState.value
    return s.title.isNotBlank() && s.startDate != null && s.endDate != null
  }

  override suspend fun resolveBannerImagePath(eventUid: String): String? {
    val s = uiState.value
    return when {
      s.bannerImageUri != null ->
          mediaRepository.upload(s.bannerImageUri, "events/$eventUid/banner")
      s.shouldRemoveBanner -> null
      else -> s.bannerImagePath
    }
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

  fun prefill(event: Event.Public) {
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

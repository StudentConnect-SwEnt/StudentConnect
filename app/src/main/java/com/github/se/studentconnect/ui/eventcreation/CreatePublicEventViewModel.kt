package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePublicEventViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val mediaRepository: MediaRepository = MediaRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(CreateEventUiState.Public())
  val uiState: StateFlow<CreateEventUiState.Public> = _uiState.asStateFlow()

  private val _navigateToEvent = MutableSharedFlow<String>()
  val navigateToEvent: SharedFlow<String> = _navigateToEvent.asSharedFlow()

  private var editingEventUid: String? = null

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

  fun updateSubtitle(newSubtitle: String) {
    _uiState.value = uiState.value.copy(subtitle = newSubtitle)
  }

  fun updateWebsite(newWebsite: String) {
    _uiState.value = uiState.value.copy(website = newWebsite)
  }

  fun updateTags(newTags: List<String>) {
    _uiState.value = uiState.value.copy(tags = newTags)
  }

  fun resetFinishedSaving() {
    _uiState.value = uiState.value.copy(finishedSaving = false, isSaving = false)
  }

  fun prefill(event: Event.Public) {
    editingEventUid = event.uid
    val startDateTime =
        event.start.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val endTimestamp = event.end ?: event.start
    val endDateTime =
        endTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    _uiState.value =
        CreateEventUiState.Public(
            title = event.title,
            description = event.description,
            location = event.location,
            startDate = startDateTime.toLocalDate(),
            startTime = startDateTime.toLocalTime(),
            endDate = endDateTime.toLocalDate(),
            endTime = endDateTime.toLocalTime(),
            numberOfParticipantsString = event.maxCapacity?.toString() ?: "",
            hasParticipationFee = event.participationFee != null,
            participationFeeString = event.participationFee?.toString() ?: "",
            isFlash = event.isFlash,
            subtitle = event.subtitle,
            website = event.website.orEmpty(),
            tags = event.tags,
            bannerImagePath = event.imageUrl,
        )
  }

  fun loadEvent(eventUid: String) {
    editingEventUid = eventUid
    viewModelScope.launch {
      val event = eventRepository.getEvent(eventUid)
      if (event is Event.Public) prefill(event)
    }
  }

  fun saveEvent() {
    val state = uiState.value
    val canSave = state.title.isNotBlank() && state.startDate != null && state.endDate != null
    check(canSave)

    val currentUserId = Firebase.auth.currentUser?.uid
    checkNotNull(currentUserId)

    _uiState.value = state.copy(isSaving = true)
    val eventUid = editingEventUid ?: eventRepository.getNewUid()

    viewModelScope.launch {
      try {
        val latestState = uiState.value
        val start =
            LocalDateTime.of(latestState.startDate!!, latestState.startTime).let {
              val instant = it.atZone(ZoneId.systemDefault()).toInstant()
              Timestamp(instant)
            }

        val end =
            LocalDateTime.of(latestState.endDate!!, latestState.endTime).let {
              val instant = it.atZone(ZoneId.systemDefault()).toInstant()
              Timestamp(instant)
            }

        val maxCapacity =
            try {
              latestState.numberOfParticipantsString.toUInt()
            } catch (_: Exception) {
              null
            }

        val participationFee =
            try {
              latestState.participationFeeString.toUInt()
            } catch (_: Exception) {
              null
            }

        val bannerPath = resolveBannerImagePath(eventUid, latestState)

        val event =
            Event.Public(
                uid = eventUid,
                ownerId = currentUserId,
                title = latestState.title,
                description = latestState.description,
                imageUrl = bannerPath,
                location = latestState.location,
                start = start,
                end = end,
                maxCapacity = maxCapacity,
                participationFee = participationFee,
                isFlash = latestState.isFlash,
                subtitle = latestState.subtitle,
                tags = latestState.tags,
                website = latestState.website)

        if (editingEventUid != null) {
          eventRepository.editEvent(eventUid, event)
        } else {
          eventRepository.addEvent(event)
        }

        _uiState.value =
            uiState.value.copy(
                isSaving = false,
                finishedSaving = true,
                bannerImageUri = null,
                bannerImagePath = bannerPath,
                shouldRemoveBanner = false)

        _navigateToEvent.emit(eventUid)
      } catch (_: Exception) {
        _uiState.value = uiState.value.copy(isSaving = false, finishedSaving = false)
      }
    }
  }

  private suspend fun resolveBannerImagePath(
      eventUid: String,
      state: CreateEventUiState.Public
  ): String? {
    return when {
      state.bannerImageUri != null ->
          mediaRepository.upload(state.bannerImageUri, "events/$eventUid/banner")
      state.shouldRemoveBanner -> null
      else -> state.bannerImagePath
    }
  }
}

package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.authentication.AuthRepository
import com.github.se.studentconnect.model.authentication.AuthRepositoryFirebase
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreatePrivateEventViewModel(
    private val authRepository: AuthRepository = AuthRepositoryFirebase(),
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(CreateEventUiState.Private())
  val uiState: StateFlow<CreateEventUiState.Private> = _uiState.asStateFlow()

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

  fun prefill(event: Event.Private) {
    val startDateTime =
        event.start.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val endTimestamp = event.end ?: event.start
    val endDateTime =
        endTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    _uiState.value =
        CreateEventUiState.Private(
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
        )
  }

  fun loadEvent(eventUid: String) {
    viewModelScope.launch {
      val event = eventRepository.getEvent(eventUid)
      if (event is Event.Private) prefill(event)
    }
  }

  fun saveEvent() {
    val canSave =
        uiState.value.title.isNotBlank() &&
            uiState.value.startDate != null &&
            uiState.value.endDate != null
    check(canSave)

    val start =
        LocalDateTime.of(uiState.value.startDate, uiState.value.startTime).let {
          val instant = it.atZone(ZoneId.systemDefault()).toInstant()

          Timestamp(instant)
        }

    val end =
        LocalDateTime.of(uiState.value.endDate, uiState.value.endTime).let {
          val instant = it.atZone(ZoneId.systemDefault()).toInstant()

          Timestamp(instant)
        }

    val maxCapacity =
        try {
          uiState.value.numberOfParticipantsString.toUInt()
        } catch (_: Exception) {
          null
        }

    val participationFee =
        try {
          uiState.value.participationFeeString.toUInt()
        } catch (_: Exception) {
          null
        }

    val event =
        Event.Private(
            uid = eventRepository.getNewUid(),
            ownerId = Firebase.auth.currentUser?.uid!!,
            title = uiState.value.title,
            description = uiState.value.description,
            imageUrl = null,
            location = uiState.value.location,
            start = start,
            end = end,
            maxCapacity = maxCapacity,
            participationFee = participationFee,
            isFlash = uiState.value.isFlash)

    viewModelScope.launch {
      try {
        eventRepository.addEvent(event)
        _uiState.value = uiState.value.copy(finishedSaving = true)
      } catch (_: Exception) {
        _uiState.value = uiState.value.copy(finishedSaving = false)
      }
    }
  }
}

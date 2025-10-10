package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.location.Location
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class CreatePrivateEventViewModel : ViewModel() {
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
}

package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.ViewModel
import com.github.se.studentconnect.model.location.Location
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreatePublicEventViewModel : ViewModel() {
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

  fun updateSubtitle(newSubtitle: String) {
    _uiState.value = uiState.value.copy(subtitle = newSubtitle)
  }

  fun updateWebsite(newWebsite: String) {
    _uiState.value = uiState.value.copy(website = newWebsite)
  }

  fun updateTags(newTags: List<String>) {
    _uiState.value = uiState.value.copy(tags = newTags)
  }
}

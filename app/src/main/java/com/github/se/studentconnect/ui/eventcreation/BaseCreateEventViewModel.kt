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
import com.google.firebase.auth.auth
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseCreateEventViewModel<S : CreateEventUiState>(
    initialState: S,
    protected val eventRepository: EventRepository = EventRepositoryProvider.repository,
    protected val mediaRepository: MediaRepository = MediaRepositoryProvider.repository
) : ViewModel() {

  protected val _navigateToEvent = MutableSharedFlow<String>()
  val navigateToEvent: SharedFlow<String> = _navigateToEvent.asSharedFlow()

  // State is now managed in the base class
  protected val _uiState = MutableStateFlow(initialState)
  val uiState: StateFlow<S> = _uiState.asStateFlow()

  protected var editingEventUid: String? = null

  // --- Shared Update Functions ---

  fun updateTitle(newTitle: String) {
    updateState { copyCommon(title = newTitle) }
  }

  fun updateDescription(newDescription: String) {
    updateState { copyCommon(description = newDescription) }
  }

  fun updateLocation(newLocation: Location?) {
    updateState { copyCommon(location = newLocation) }
  }

  fun updateStartDate(newStartDate: LocalDate?) {
    updateState { copyCommon(startDate = newStartDate) }
  }

  fun updateStartTime(newStartTime: LocalTime) {
    updateState { copyCommon(startTime = newStartTime) }
  }

  fun updateEndDate(newEndDate: LocalDate?) {
    updateState { copyCommon(endDate = newEndDate) }
  }

  fun updateEndTime(newEndTime: LocalTime) {
    updateState { copyCommon(endTime = newEndTime) }
  }

  fun updateNumberOfParticipantsString(newNumberOfParticipantsString: String) {
    updateState { copyCommon(numberOfParticipantsString = newNumberOfParticipantsString) }
  }

  fun updateHasParticipationFee(newHasParticipationFee: Boolean) {
    updateState { copyCommon(hasParticipationFee = newHasParticipationFee) }
  }

  fun updateParticipationFeeString(newParticipationFeeString: String) {
    updateState { copyCommon(participationFeeString = newParticipationFeeString) }
  }

  fun updateIsFlash(newIsFlash: Boolean) {
    updateState { copyCommon(isFlash = newIsFlash) }
  }

  fun updateBannerImageUri(newUri: Uri) {
    updateState { copyCommon(bannerImageUri = newUri, shouldRemoveBanner = false) }
  }

  fun removeBannerImage() {
    updateState {
      copyCommon(bannerImageUri = null, bannerImagePath = null, shouldRemoveBanner = true)
    }
  }

  fun resetFinishedSaving() {
    updateState { copyCommon(finishedSaving = false, isSaving = false) }
  }

  /**
   * Helper to safely update state using the copyCommon mechanism. We use an unchecked cast here
   * because copyCommon guarantees to return the same runtime type as the object it is called on.
   */
  @Suppress("UNCHECKED_CAST")
  protected fun updateState(transform: S.() -> CreateEventUiState) {
    _uiState.value = _uiState.value.transform() as S
  }

  // --- Shared Logic ---

  protected fun validateState(): Boolean {
    val s = uiState.value
    return s.title.isNotBlank() && s.startDate != null && s.endDate != null
  }

  protected suspend fun resolveBannerImagePath(eventUid: String): String? {
    val s = uiState.value
    return when {
      s.bannerImageUri != null ->
          mediaRepository.upload(s.bannerImageUri!!, "events/$eventUid/banner")
      s.shouldRemoveBanner -> null
      else -> s.bannerImagePath
    }
  }

  open fun loadEvent(eventUid: String) {
    editingEventUid = eventUid
  }

  // --- Abstract Building Logic ---

  protected abstract suspend fun buildEvent(
      uid: String,
      ownerId: String,
      bannerPath: String?
  ): Event

  fun saveEvent() {
    if (!validateState()) return

    val currentUserId = Firebase.auth.currentUser?.uid
    checkNotNull(currentUserId)

    updateState { copyCommon(isSaving = true) }
    val eventUid = editingEventUid ?: eventRepository.getNewUid()

    viewModelScope.launch {
      try {
        val bannerPath = resolveBannerImagePath(eventUid)
        val event = buildEvent(eventUid, currentUserId, bannerPath)

        if (editingEventUid != null) {
          eventRepository.editEvent(eventUid, event)
        } else {
          eventRepository.addEvent(event)
        }

        updateState {
          copyCommon(
              bannerImageUri = null,
              bannerImagePath = bannerPath,
              shouldRemoveBanner = false,
              finishedSaving = true,
              isSaving = false)
        }
        _navigateToEvent.emit(eventUid)
      } catch (e: Exception) {
        updateState { copyCommon(isSaving = false, finishedSaving = false) }
      }
    }
  }
}

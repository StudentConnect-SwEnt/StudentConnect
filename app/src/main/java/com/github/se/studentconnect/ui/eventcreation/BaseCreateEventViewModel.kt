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

/**
 * Abstract Base ViewModel for creating and editing events. Encapsulates common state management and
 * save logic for both Public and Private events.
 *
 * @param S The specific type of [CreateEventUiState] managed by this ViewModel.
 * @param initialState The starting state.
 * @param eventRepository Repository for event operations.
 * @param mediaRepository Repository for media uploads.
 */
abstract class BaseCreateEventViewModel<S : CreateEventUiState>(
    initialState: S,
    protected val eventRepository: EventRepository = EventRepositoryProvider.repository,
    protected val mediaRepository: MediaRepository = MediaRepositoryProvider.repository
) : ViewModel() {

  protected val _navigateToEvent = MutableSharedFlow<String>()
  /** Flow to signal navigation to the created/edited event. */
  val navigateToEvent: SharedFlow<String> = _navigateToEvent.asSharedFlow()

  protected val _uiState = MutableStateFlow(initialState)
  /** The current UI state. */
  val uiState: StateFlow<S> = _uiState.asStateFlow()

  protected var editingEventUid: String? = null

  // --- Shared Update Functions ---

  /**
   * Updates the event title.
   *
   * @param newTitle The new title.
   */
  fun updateTitle(newTitle: String) {
    updateState { copyCommon(title = newTitle) }
  }

  /**
   * Updates the event description.
   *
   * @param newDescription The new description.
   */
  fun updateDescription(newDescription: String) {
    updateState { copyCommon(description = newDescription) }
  }

  /**
   * Updates the event location.
   *
   * @param newLocation The new location.
   */
  fun updateLocation(newLocation: Location?) {
    updateState { copyCommon(location = newLocation) }
  }

  /**
   * Updates the event start date.
   *
   * @param newStartDate The new start date.
   */
  fun updateStartDate(newStartDate: LocalDate?) {
    updateState { copyCommon(startDate = newStartDate) }
  }

  /**
   * Updates the event start time.
   *
   * @param newStartTime The new start time.
   */
  fun updateStartTime(newStartTime: LocalTime) {
    updateState { copyCommon(startTime = newStartTime) }
  }

  /**
   * Updates the event end date.
   *
   * @param newEndDate The new end date.
   */
  fun updateEndDate(newEndDate: LocalDate?) {
    updateState { copyCommon(endDate = newEndDate) }
  }

  /**
   * Updates the event end time.
   *
   * @param newEndTime The new end time.
   */
  fun updateEndTime(newEndTime: LocalTime) {
    updateState { copyCommon(endTime = newEndTime) }
  }

  /**
   * Updates the number of participants string.
   *
   * @param newNumberOfParticipantsString The new number of participants string.
   */
  fun updateNumberOfParticipantsString(newNumberOfParticipantsString: String) {
    updateState { copyCommon(numberOfParticipantsString = newNumberOfParticipantsString) }
  }

  /**
   * Updates whether the event has a participation fee.
   *
   * @param newHasParticipationFee The new has participation fee value.
   */
  fun updateHasParticipationFee(newHasParticipationFee: Boolean) {
    updateState { copyCommon(hasParticipationFee = newHasParticipationFee) }
  }

  /**
   * Updates the participation fee string.
   *
   * @param newParticipationFeeString The new participation fee string.
   */
  fun updateParticipationFeeString(newParticipationFeeString: String) {
    updateState { copyCommon(participationFeeString = newParticipationFeeString) }
  }

  /**
   * Updates whether the event is a flash event.
   *
   * @param newIsFlash The new is flash value.
   */
  fun updateIsFlash(newIsFlash: Boolean) {
    updateState { copyCommon(isFlash = newIsFlash) }
  }

  /**
   * Updates the banner image URI.
   *
   * @param newUri The new banner image URI.
   */
  fun updateBannerImageUri(newUri: Uri) {
    updateState { copyCommon(bannerImageUri = newUri, shouldRemoveBanner = false) }
  }

  /** Removes the banner image. */
  fun removeBannerImage() {
    updateState {
      copyCommon(bannerImageUri = null, bannerImagePath = null, shouldRemoveBanner = true)
    }
  }

  /** Resets the finished saving flag after handling. */
  fun resetFinishedSaving() {
    updateState { copyCommon(finishedSaving = false, isSaving = false) }
  }

  /**
   * Helper to safely update state using the copyCommon mechanism. Performs an unchecked cast which
   * is safe as copyCommon preserves the runtime type.
   */
  protected fun updateState(transform: S.() -> CreateEventUiState) {
    _uiState.value = _uiState.value.transform() as S
  }

  /** Validates the current state before saving. */
  protected fun validateState(): Boolean {
    val s = uiState.value
    return s.title.isNotBlank() && s.startDate != null && s.endDate != null
  }

  /**
   * Resolves the final path for the banner image. Uploads the image if a new URI is selected, or
   * handles removal/retention of existing image.
   *
   * Note: This is private to avoid exposing suspend functions in the ViewModel hierarchy.
   */
  private suspend fun resolveBannerImagePath(eventUid: String): String? {
    val s = uiState.value
    return when {
      s.bannerImageUri != null ->
          mediaRepository.upload(s.bannerImageUri!!, "events/$eventUid/banner")
      s.shouldRemoveBanner -> null
      else -> s.bannerImagePath
    }
  }

  /**
   * Sets the ViewModel to "Edit Mode" for a specific event UID. Subclasses must implement the logic
   * to fetch and prefill data.
   */
  open fun loadEvent(eventUid: String) {
    editingEventUid = eventUid
  }

  /** Constructs the Event object from the current UI state. */
  protected abstract fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event

  /**
   * Saves the event to the repository. Handles image uploading and database operations within the
   * ViewModel scope.
   */
  fun saveEvent() {
    if (!validateState()) return

    val currentUserId = Firebase.auth.currentUser?.uid
    checkNotNull(currentUserId)

    updateState { copyCommon(isSaving = true) }
    val eventUid = editingEventUid ?: eventRepository.getNewUid()

    viewModelScope.launch {
      try {
        val bannerPath = resolveBannerImagePath(eventUid)

        // Build event (computation only)
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
        e.printStackTrace()
        updateState { copyCommon(isSaving = false, finishedSaving = false) }
      }
    }
  }
}

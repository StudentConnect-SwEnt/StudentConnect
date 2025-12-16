package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.model.organization.OrganizationRepository
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
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
    protected val mediaRepository: MediaRepository = MediaRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val organizationRepository: OrganizationRepository =
        OrganizationRepositoryProvider.repository,
    private val friendsRepository: FriendsRepository = FriendsRepositoryProvider.repository,
    private val notificationRepository: NotificationRepository =
        NotificationRepositoryProvider.repository
) : ViewModel() {

  protected val _navigateToEvent = MutableSharedFlow<String>()
  /** Flow to signal navigation to the created/edited event. */
  val navigateToEvent: SharedFlow<String> = _navigateToEvent.asSharedFlow()

  protected val _uiState = MutableStateFlow(initialState)
  /** The current UI state. */
  val uiState: StateFlow<S> = _uiState.asStateFlow()

  protected var editingEventUid: String? = null

  init {
    // Load user's organizations on initialization
    loadUserOrganizations()
  }

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
   * Updates the flash event duration hours.
   *
   * @param newHours The new duration hours value (0-5).
   */
  fun updateFlashDurationHours(newHours: Int) {
    updateState {
      copyCommon(flashDurationHours = newHours.coerceIn(0, C.FlashEvent.MAX_DURATION_HOURS.toInt()))
    }
  }

  /**
   * Updates the flash event duration minutes.
   *
   * @param newMinutes The new duration minutes value (0-59).
   */
  fun updateFlashDurationMinutes(newMinutes: Int) {
    updateState { copyCommon(flashDurationMinutes = newMinutes.coerceIn(0, 59)) }
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
   * Updates whether to create the event as an organization.
   *
   * @param createAsOrg The new create as organization value.
   */
  fun updateCreateAsOrganization(createAsOrg: Boolean) {
    updateState {
      copyCommon(
          createAsOrganization = createAsOrg,
          selectedOrganizationId =
              if (!createAsOrg) null
              else
                  this.userOrganizations.firstOrNull()?.first) // Auto-select first org when enabled
    }
  }

  /**
   * Updates the selected organization ID.
   *
   * @param organizationId The new selected organization ID.
   */
  fun updateSelectedOrganizationId(organizationId: String?) {
    updateState { copyCommon(selectedOrganizationId = organizationId) }
  }

  /** Loads the organizations that the current user owns (is the creator of). */
  private fun loadUserOrganizations() {
    val currentUserId = Firebase.auth.currentUser?.uid ?: return

    viewModelScope.launch {
      try {
        val allOrganizations = organizationRepository.getAllOrganizations()
        val userOrgs =
            allOrganizations.filter { it.createdBy == currentUserId }.map { it.id to it.name }

        updateState { copyCommon(userOrganizations = userOrgs) }
      } catch (e: Exception) {
        Log.e("BaseCreateEventViewModel", "Error loading user organizations", e)
      }
    }
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

    if (!s.title.isNotBlank()) {
      return false
    }

    // Validate flash event constraints
    if (s.isFlash) {
      // Flash events: validate duration (0 < duration <= max)
      val totalMinutes = s.flashDurationHours * 60 + s.flashDurationMinutes
      if (totalMinutes <= 0) {
        Log.w("BaseCreateEventViewModel", "Flash event duration must be greater than 0")
        return false
      }
      val totalHours = s.flashDurationHours + (s.flashDurationMinutes.toDouble() / 60.0)
      if (totalHours > C.FlashEvent.MAX_DURATION_HOURS) {
        Log.w(
            "BaseCreateEventViewModel",
            "Flash event duration exceeds maximum of ${C.FlashEvent.MAX_DURATION_HOURS} hours")
        return false
      }
    } else {
      // Normal events: validate dates
      val startDate = s.startDate
      val endDate = s.endDate
      if (startDate == null || endDate == null) {
        return false
      }
    }

    return true
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

  /**
   * Pre-fills the form with data from an existing event as a template. Unlike prefill(), this does
   */
  open fun prefillFromTemplate(event: Event) {
    // Default implementation does nothing. Subclasses should override.}
  }

  /** Loads an existing event by UID and pre-fills the form as a template. */
  fun loadEventAsTemplate(eventUid: String) {
    viewModelScope.launch {
      try {
        val event = eventRepository.getEvent(eventUid)
        prefillFromTemplate(event)
      } catch (e: Throwable) {
        if (e is java.util.concurrent.CancellationException) throw e
        e.printStackTrace()
      }
    }
  }

  protected abstract fun buildEvent(uid: String, ownerId: String, bannerPath: String?): Event

  /** Helper function to create timestamp from date and time. Override in subclasses if needed. */
  protected open fun timestampFrom(date: LocalDate, time: LocalTime): Timestamp {
    val instant =
        java.time.LocalDateTime.of(date, time).atZone(java.time.ZoneId.systemDefault()).toInstant()
    return Timestamp(instant)
  }

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

        // For flash events, set start time to now and calculate end time from duration
        val s = uiState.value
        if (s.isFlash && editingEventUid == null) {
          val now = Timestamp.now()
          val nowLocal =
              now.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()

          // Calculate end time from duration
          val durationMinutes = s.flashDurationHours * 60 + s.flashDurationMinutes
          val endLocal = nowLocal.plusMinutes(durationMinutes.toLong())

          updateState {
            copyCommon(
                startDate = nowLocal.toLocalDate(),
                startTime = nowLocal.toLocalTime(),
                endDate = endLocal.toLocalDate(),
                endTime = endLocal.toLocalTime())
          }
        }

        // Build event (computation only)
        val event = buildEvent(eventUid, currentUserId, bannerPath)

        if (editingEventUid != null) {
          eventRepository.editEvent(eventUid, event)
        } else {
          eventRepository.addEvent(event)

          // Send notifications for flash events
          if (s.isFlash) {
            sendFlashEventNotifications(event, currentUserId)
          }
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

  /**
   * Sends notifications to friends/followers when a flash event is created.
   *
   * @param event The flash event that was just created.
   * @param ownerId The ID of the event owner (user or organization).
   */
  private suspend fun sendFlashEventNotifications(event: Event, ownerId: String) {
    try {
      val recipients = getNotificationRecipients(ownerId)
      val notificationIdPrefix = "flash_${event.uid}_"

      for ((index, userId) in recipients.withIndex()) {
        try {
          val notificationId = "${notificationIdPrefix}user_${userId}_$index"
          val notification =
              Notification.EventStarting(
                  id = notificationId,
                  userId = userId,
                  eventId = event.uid,
                  eventTitle = event.title,
                  eventStart = event.start,
                  timestamp = Timestamp.now(),
                  isRead = false)

          notificationRepository.createNotification(
              notification,
              onSuccess = {
                Log.d("BaseCreateEventViewModel", "Sent flash event notification to user $userId")
              },
              onFailure = { e ->
                Log.w(
                    "BaseCreateEventViewModel",
                    "Failed to send notification to user $userId: ${e.message}")
              })
        } catch (e: Exception) {
          Log.e("BaseCreateEventViewModel", "Error creating notification for user $userId", e)
        }
      }
    } catch (e: Exception) {
      Log.e("BaseCreateEventViewModel", "Error sending flash event notifications", e)
    }
  }

  /**
   * Gets the list of user IDs who should receive notifications for a flash event. If owner is an
   * organization, returns followers. If owner is a user, returns friends.
   *
   * @param ownerId The ID of the event owner.
   * @return List of user IDs to notify.
   */
  private suspend fun getNotificationRecipients(ownerId: String): List<String> {
    return try {
      // Check if ownerId is an organization
      val organization = organizationRepository.getOrganizationById(ownerId)
      if (organization != null) {
        // Owner is an organization - get followers
        userRepository.getOrganizationFollowers(ownerId)
      } else {
        // Owner is a user - get friends
        friendsRepository.getFriends(ownerId)
      }
    } catch (e: Exception) {
      Log.e("BaseCreateEventViewModel", "Error getting notification recipients", e)
      emptyList()
    }
  }

  /**
   * Calculates flash duration hours and minutes from event start and end timestamps. This helper
   * function eliminates code duplication between Private and Public ViewModels.
   *
   * @param event The event to calculate duration from.
   * @return Pair of (hours, minutes) for the flash event duration, or (1, 0) if not a flash event.
   */
  protected fun calculateFlashDuration(event: Event): Pair<Int, Int> {
    return if (event.isFlash) {
      val durationMs =
          (event.end?.toDate()?.time ?: event.start.toDate().time) - event.start.toDate().time
      val totalMinutes = (durationMs / (1000 * 60)).toInt()
      Pair(totalMinutes / 60, totalMinutes % 60)
    } else {
      Pair(1, 0)
    }
  }
}

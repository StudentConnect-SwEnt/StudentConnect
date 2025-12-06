package com.github.se.studentconnect.ui.eventcreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

abstract class BaseCreateEventViewModel<S>(
    protected val eventRepository: EventRepository = EventRepositoryProvider.repository,
    protected val mediaRepository: MediaRepository = MediaRepositoryProvider.repository
) : ViewModel() {

  protected val _navigateToEvent = MutableSharedFlow<String>()
  val navigateToEvent: SharedFlow<String> = _navigateToEvent.asSharedFlow()

  protected var editingEventUid: String? = null

  // Hooks for subclasses to access/update state
  protected abstract fun getCurrentState(): S

  protected abstract fun updateIsSaving(isSaving: Boolean)

  protected abstract fun updateFinishedSaving(finished: Boolean)

  protected abstract fun updateBannerAfterSave(path: String?)

  protected abstract fun validateState(): Boolean

  protected abstract suspend fun buildEvent(
      uid: String,
      ownerId: String,
      bannerPath: String?
  ): Event

  protected abstract suspend fun resolveBannerImagePath(eventUid: String): String?

  open fun loadEvent(eventUid: String) {
    editingEventUid = eventUid
  }

  fun resetFinishedSaving() {
    updateFinishedSaving(false)
    updateIsSaving(false)
  }

  fun saveEvent() {
    if (!validateState()) return

    val currentUserId = Firebase.auth.currentUser?.uid
    checkNotNull(currentUserId)

    updateIsSaving(true)
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

        updateBannerAfterSave(bannerPath)
        updateFinishedSaving(true)
        updateIsSaving(false)
        _navigateToEvent.emit(eventUid)
      } catch (e: Exception) {
        updateIsSaving(false)
        updateFinishedSaving(false)
      }
    }
  }
}

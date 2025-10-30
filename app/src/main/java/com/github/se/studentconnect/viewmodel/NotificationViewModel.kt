package com.github.se.studentconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for notifications */
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
)

/** ViewModel for managing notifications in the home screen */
class NotificationViewModel
@Inject
constructor(
    private val repository: NotificationRepository = NotificationRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(NotificationUiState())
  val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

  private var stopListening: (() -> Unit)? = null

  init {
    startListeningToNotifications()
  }

  /** Starts listening to real-time notification updates */
  private fun startListeningToNotifications() {
    val userId = Firebase.auth.currentUser?.uid ?: return

    _uiState.update { it.copy(isLoading = true) }

    stopListening =
        repository.listenToNotifications(userId) { notifications ->
          val unreadCount = notifications.count { !it.isRead }
          _uiState.update {
            it.copy(notifications = notifications, unreadCount = unreadCount, isLoading = false)
          }
        }
  }

  /**
   * Marks a notification as read
   *
   * @param notificationId The ID of the notification to mark as read
   */
  fun markAsRead(notificationId: String) {
    viewModelScope.launch {
      repository.markAsRead(
          notificationId,
          onSuccess = {
            // Update will come through the listener
          },
          onFailure = { e ->
            // Log error but don't update UI - listener will handle state
            android.util.Log.e("NotificationViewModel", "Failed to mark as read", e)
          })
    }
  }

  /** Marks all notifications as read */
  fun markAllAsRead() {
    val userId = Firebase.auth.currentUser?.uid ?: return

    viewModelScope.launch {
      repository.markAllAsRead(
          userId,
          onSuccess = {
            // Update will come through the listener
          },
          onFailure = { e ->
            android.util.Log.e("NotificationViewModel", "Failed to mark all as read", e)
          })
    }
  }

  /**
   * Deletes a notification
   *
   * @param notificationId The ID of the notification to delete
   */
  fun deleteNotification(notificationId: String) {
    viewModelScope.launch {
      repository.deleteNotification(
          notificationId,
          onSuccess = {
            // Update will come through the listener
          },
          onFailure = { e ->
            android.util.Log.e("NotificationViewModel", "Failed to delete notification", e)
          })
    }
  }

  /** Refreshes notifications manually */
  fun refresh() {
    val userId = Firebase.auth.currentUser?.uid ?: return

    _uiState.update { it.copy(isLoading = true) }

    repository.getNotifications(
        userId,
        onSuccess = { notifications ->
          val unreadCount = notifications.count { !it.isRead }
          _uiState.update {
            it.copy(notifications = notifications, unreadCount = unreadCount, isLoading = false)
          }
        },
        onFailure = { e ->
          android.util.Log.e("NotificationViewModel", "Failed to load notifications", e)
          _uiState.update { it.copy(isLoading = false) }
        })
  }

  override fun onCleared() {
    super.onCleared()
    stopListening?.invoke()
  }
}

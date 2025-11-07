package com.github.se.studentconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.notification.Notification
import com.github.se.studentconnect.model.notification.NotificationRepository
import com.github.se.studentconnect.model.notification.NotificationRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
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
  private var currentUserId: String? = null

  init {
    startListeningToNotifications()
  }

  /** Starts listening to real-time notification updates */
  private fun startListeningToNotifications() {
    val userId = getCurrentUserId() ?: return

    // Stop existing listener if any
    stopListening?.invoke()
    stopListening = null

    // Update current user ID
    currentUserId = userId

    _uiState.update { it.copy(isLoading = true) }

    stopListening =
        repository.listenToNotifications(userId) { notifications ->
          val unreadCount = notifications.count { !it.isRead }
          _uiState.update {
            it.copy(notifications = notifications, unreadCount = unreadCount, isLoading = false)
          }
        }
  }

  /** Gets the current user ID with proper validation */
  private fun getCurrentUserId(): String? {
    return AuthenticationProvider.currentUser.takeIf { it.isNotEmpty() }
  }

  /** Restarts the notification listener (useful after auth changes) */
  fun restartListener() {
    val newUserId = getCurrentUserId()
    if (newUserId != currentUserId) {
      startListeningToNotifications()
    }
  }

  /**
   * Marks a notification as read
   *
   * @param notificationId The ID of the notification to mark as read
   */
  fun markAsRead(notificationId: String) {
    // Optimistically update UI
    val currentNotifications = _uiState.value.notifications
    val updatedNotifications =
        currentNotifications.map { notification ->
          if (notification.id == notificationId) {
            when (notification) {
              is Notification.FriendRequest -> notification.copy(isRead = true)
              is Notification.EventStarting -> notification.copy(isRead = true)
            }
          } else {
            notification
          }
        }

    val newUnreadCount = updatedNotifications.count { !it.isRead }
    _uiState.update { it.copy(notifications = updatedNotifications, unreadCount = newUnreadCount) }

    viewModelScope.launch {
      repository.markAsRead(
          notificationId,
          onSuccess = {
            // UI already updated optimistically
          },
          onFailure = { e ->
            android.util.Log.e(TAG, "Failed to mark as read", e)
            // Revert optimistic update
            _uiState.update {
              it.copy(
                  notifications = currentNotifications,
                  unreadCount = currentNotifications.count { !it.isRead })
            }
          })
    }
  }

  /** Marks all notifications as read */
  fun markAllAsRead() {
    val userId = getCurrentUserId() ?: return

    // Optimistically update UI
    val currentNotifications = _uiState.value.notifications
    val updatedNotifications =
        currentNotifications.map { notification ->
          when (notification) {
            is Notification.FriendRequest -> notification.copy(isRead = true)
            is Notification.EventStarting -> notification.copy(isRead = true)
          }
        }

    _uiState.update { it.copy(notifications = updatedNotifications, unreadCount = 0) }

    viewModelScope.launch {
      repository.markAllAsRead(
          userId,
          onSuccess = {
            // UI already updated optimistically
          },
          onFailure = { e ->
            android.util.Log.e(TAG, "Failed to mark all as read", e)
            // Revert optimistic update
            _uiState.update {
              it.copy(
                  notifications = currentNotifications,
                  unreadCount = currentNotifications.count { !it.isRead })
            }
          })
    }
  }

  /**
   * Deletes a notification
   *
   * @param notificationId The ID of the notification to delete
   */
  fun deleteNotification(notificationId: String) {
    // Optimistically update UI
    val currentNotifications = _uiState.value.notifications
    val updatedNotifications = currentNotifications.filter { it.id != notificationId }
    val newUnreadCount = updatedNotifications.count { !it.isRead }

    _uiState.update { it.copy(notifications = updatedNotifications, unreadCount = newUnreadCount) }

    viewModelScope.launch {
      repository.deleteNotification(
          notificationId,
          onSuccess = {
            // UI already updated optimistically
          },
          onFailure = { e ->
            android.util.Log.e(TAG, "Failed to delete notification", e)
            // Revert optimistic update
            _uiState.update {
              it.copy(
                  notifications = currentNotifications,
                  unreadCount = currentNotifications.count { !it.isRead })
            }
          })
    }
  }

  /** Refreshes notifications manually */
  fun refresh() {
    val userId = getCurrentUserId() ?: return

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
          android.util.Log.e(TAG, "Failed to load notifications", e)
          _uiState.update { it.copy(isLoading = false) }
        })
  }

  override fun onCleared() {
    super.onCleared()
    stopListening?.invoke()
    stopListening = null
    currentUserId = null
  }

  companion object {
    private const val TAG = "NotificationViewModel"
  }
}

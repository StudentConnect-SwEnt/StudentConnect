package com.github.se.studentconnect.ui.screen.visitorProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FriendRequestStatus {
  IDLE,
  SENDING,
  SENT,
  ALREADY_FRIENDS,
  ALREADY_SENT,
  ERROR
}

data class VisitorProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null,
    val friendRequestStatus: FriendRequestStatus = FriendRequestStatus.IDLE,
    val friendRequestMessage: String? = null
)

class VisitorProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val friendsRepository: FriendsRepository = FriendsRepositoryProvider.repository,
    private val getString: (resId: Int) -> String = { id -> "" }
) : ViewModel() {

  private val _uiState = MutableStateFlow(VisitorProfileUiState())
  val uiState: StateFlow<VisitorProfileUiState> = _uiState.asStateFlow()

  // Holds the active friendship observer so we can cancel it when switching profiles
  private var friendshipObserverJob: Job? = null

  fun loadProfile(userId: String, forceRefresh: Boolean = false) {
    val currentState = _uiState.value
    if (!forceRefresh &&
        currentState.user?.userId == userId &&
        !currentState.isLoading &&
        currentState.errorMessage == null) {
      return
    }

    // Cancel any previous friendship observer when loading a new profile
    friendshipObserverJob?.cancel()
    friendshipObserverJob = null

    _uiState.update { it.copy(isLoading = true, errorMessage = null) }

    viewModelScope.launch {
      try {
        val user = userRepository.getUserById(userId)
        if (user != null) {
          _uiState.update { it.copy(isLoading = false, user = user, errorMessage = null) }
          // Check if already friends or request sent
          checkFriendshipStatus(userId)

          // Start observing friendship changes so UI updates if the other accepts
          subscribeToFriendshipUpdates(userId)
        } else {
          _uiState.update {
            it.copy(
                isLoading = false,
                user = null,
                errorMessage = getString(R.string.error_profile_not_found))
          }
        }
      } catch (throwable: Throwable) {
        _uiState.update {
          it.copy(
              isLoading = false,
              user = null,
              errorMessage = throwable.message ?: getString(R.string.error_failed_to_load_profile))
        }
      }
    }
  }

  private suspend fun checkFriendshipStatus(userId: String) {
    try {
      val currentUserId = AuthenticationProvider.currentUser

      // Check if already friends
      val friends = friendsRepository.getFriends(currentUserId)
      if (friends.contains(userId)) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS,
              friendRequestMessage = getString(R.string.text_already_friends))
        }
        return
      }

      // Check if request already sent
      val sentRequests = friendsRepository.getSentRequests(currentUserId)
      if (sentRequests.contains(userId)) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ALREADY_SENT,
              friendRequestMessage = getString(R.string.friend_request_already_sent))
        }
        return
      }

      // Check if they sent us a request (we should accept instead)
      val pendingRequests = friendsRepository.getPendingRequests(currentUserId)
      if (pendingRequests.contains(userId)) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.IDLE,
              friendRequestMessage = getString(R.string.user_sent_you_request))
        }
        return
      }

      // Ready to send request
      _uiState.update {
        it.copy(friendRequestStatus = FriendRequestStatus.IDLE, friendRequestMessage = null)
      }
    } catch (e: Exception) {
      // If checking fails, just leave it as idle
      _uiState.update {
        it.copy(friendRequestStatus = FriendRequestStatus.IDLE, friendRequestMessage = null)
      }
    }
  }

  // Observe remote friendship changes and update UI when friendship is created/removed
  private fun subscribeToFriendshipUpdates(viewedUserId: String) {
    val currentUserId = AuthenticationProvider.currentUser
    if (currentUserId.isEmpty()) return

    // Cancel previous observer if any
    friendshipObserverJob?.cancel()

    friendshipObserverJob =
        viewModelScope.launch {
          try {
            friendsRepository.observeFriendship(currentUserId, viewedUserId).collect { areFriends ->
              if (areFriends) {
                _uiState.update {
                  it.copy(
                      friendRequestStatus = FriendRequestStatus.ALREADY_FRIENDS,
                      friendRequestMessage = getString(R.string.text_already_friends))
                }
              } else {
                // Re-evaluate other statuses when friendship no longer exists
                checkFriendshipStatus(viewedUserId)
              }
            }
          } catch (e: Exception) {
            // Ignore errors from the observer - keep current UI state
          }
        }
  }

  fun sendFriendRequest() {
    val userId = _uiState.value.user?.userId ?: return
    val currentUserId = AuthenticationProvider.currentUser

    if (currentUserId.isEmpty()) {
      _uiState.update {
        it.copy(
            friendRequestStatus = FriendRequestStatus.ERROR,
            friendRequestMessage = getString(R.string.must_be_logged_in_send_friend_request))
      }
      return
    }

    if (userId == currentUserId) {
      _uiState.update {
        it.copy(
            friendRequestStatus = FriendRequestStatus.ERROR,
            friendRequestMessage = getString(R.string.cannot_send_request_to_self))
      }
      return
    }

    _uiState.update {
      it.copy(friendRequestStatus = FriendRequestStatus.SENDING, friendRequestMessage = null)
    }

    viewModelScope.launch {
      try {
        friendsRepository.sendFriendRequest(currentUserId, userId)
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.SENT,
              friendRequestMessage = getString(R.string.friend_request_sent_success))
        }
      } catch (e: IllegalArgumentException) {
        // Handle specific errors from the repository using localized strings
        val message = e.message ?: ""
        val (status, uiMessage) =
            when {
              message.contains("already friends") ->
                  FriendRequestStatus.ALREADY_FRIENDS to getString(R.string.text_already_friends)
              message.contains("already sent") ->
                  FriendRequestStatus.ALREADY_SENT to
                      getString(R.string.friend_request_already_sent)
              else -> FriendRequestStatus.ERROR to getString(R.string.failed_to_send_friend_request)
            }
        _uiState.update { it.copy(friendRequestStatus = status, friendRequestMessage = uiMessage) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ERROR,
              friendRequestMessage = e.message ?: getString(R.string.failed_to_send_friend_request))
        }
      }
    }
  }

  // New: cancel an outgoing friend request so user can resend later
  fun cancelFriendRequest() {
    val userId = _uiState.value.user?.userId ?: return
    val currentUserId = AuthenticationProvider.currentUser

    if (currentUserId.isEmpty()) {
      _uiState.update {
        it.copy(
            friendRequestStatus = FriendRequestStatus.ERROR,
            friendRequestMessage = getString(R.string.must_be_logged_in_cancel_friend_request))
      }
      return
    }

    _uiState.update {
      it.copy(friendRequestStatus = FriendRequestStatus.SENDING, friendRequestMessage = null)
    }

    viewModelScope.launch {
      try {
        friendsRepository.cancelFriendRequest(currentUserId, userId)
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.IDLE,
              friendRequestMessage = getString(R.string.friend_request_cancelled))
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ERROR,
              friendRequestMessage =
                  e.message ?: getString(R.string.failed_to_cancel_friend_request))
        }
      }
    }
  }

  // New: allow removing an existing friend from the visited profile
  fun removeFriend() {
    val userId = _uiState.value.user?.userId ?: return
    val currentUserId = AuthenticationProvider.currentUser

    if (currentUserId.isEmpty()) {
      _uiState.update {
        it.copy(
            friendRequestStatus = FriendRequestStatus.ERROR,
            friendRequestMessage = getString(R.string.must_be_logged_in_remove_friend))
      }
      return
    }

    // Indicate an in-progress operation
    _uiState.update {
      it.copy(friendRequestStatus = FriendRequestStatus.SENDING, friendRequestMessage = null)
    }

    viewModelScope.launch {
      try {
        // Ask repository to remove the friendship
        friendsRepository.removeFriend(currentUserId, userId)

        // After successful removal, reset to idle and show a message. The friendship observer
        // will detect the change and update statuses accordingly.
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.IDLE,
              friendRequestMessage = getString(R.string.friend_removed))
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ERROR,
              friendRequestMessage = e.message ?: getString(R.string.failed_to_remove_friend))
        }
      }
    }
  }

  fun clearFriendRequestMessage() {
    _uiState.update { it.copy(friendRequestMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    friendshipObserverJob?.cancel()
  }
}

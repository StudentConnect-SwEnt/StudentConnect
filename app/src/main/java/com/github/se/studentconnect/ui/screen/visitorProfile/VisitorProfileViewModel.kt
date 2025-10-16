package com.github.se.studentconnect.ui.screen.visitorProfile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
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
    private val context: Context? = null
) : ViewModel() {

  private val _uiState = MutableStateFlow(VisitorProfileUiState())
  val uiState: StateFlow<VisitorProfileUiState> = _uiState.asStateFlow()

  fun loadProfile(userId: String, forceRefresh: Boolean = false) {
    val currentState = _uiState.value
    if (!forceRefresh &&
        currentState.user?.userId == userId &&
        !currentState.isLoading &&
        currentState.errorMessage == null) {
      return
    }

    _uiState.update { it.copy(isLoading = true, errorMessage = null) }

    viewModelScope.launch {
      try {
        val user = userRepository.getUserById(userId)
        if (user != null) {
          _uiState.update { it.copy(isLoading = false, user = user, errorMessage = null) }
          // Check if already friends or request sent
          checkFriendshipStatus(userId)
        } else {
          _uiState.update {
            it.copy(isLoading = false, user = null, errorMessage = "Profile not found.")
          }
        }
      } catch (throwable: Throwable) {
        _uiState.update {
          it.copy(
              isLoading = false,
              user = null,
              errorMessage = throwable.message ?: "Failed to load profile.")
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
              friendRequestMessage = "Already friends")
        }
        return
      }

      // Check if request already sent
      val sentRequests = friendsRepository.getSentRequests(currentUserId)
      if (sentRequests.contains(userId)) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ALREADY_SENT,
              friendRequestMessage = "Friend request already sent")
        }
        return
      }

      // Check if they sent us a request (we should accept instead)
      val pendingRequests = friendsRepository.getPendingRequests(currentUserId)
      if (pendingRequests.contains(userId)) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.IDLE,
              friendRequestMessage = "This user sent you a friend request")
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

  fun sendFriendRequest() {
    val userId = _uiState.value.user?.userId ?: return
    val currentUserId = AuthenticationProvider.currentUser

    if (currentUserId.isEmpty()) {
      _uiState.update {
        it.copy(
            friendRequestStatus = FriendRequestStatus.ERROR,
            friendRequestMessage = "You must be logged in to send friend requests")
      }
      return
    }

    if (userId == currentUserId) {
      _uiState.update {
        it.copy(
            friendRequestStatus = FriendRequestStatus.ERROR,
            friendRequestMessage = "Cannot send friend request to yourself")
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
              friendRequestMessage = "Friend request sent successfully!")
        }
      } catch (e: IllegalArgumentException) {
        // Handle specific errors from the repository
        val message = e.message ?: "Failed to send friend request"
        val status =
            when {
              message.contains("already friends") -> FriendRequestStatus.ALREADY_FRIENDS
              message.contains("already sent") -> FriendRequestStatus.ALREADY_SENT
              else -> FriendRequestStatus.ERROR
            }
        _uiState.update { it.copy(friendRequestStatus = status, friendRequestMessage = message) }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              friendRequestStatus = FriendRequestStatus.ERROR,
              friendRequestMessage = e.message ?: "Failed to send friend request")
        }
      }
    }
  }

  fun clearFriendRequestMessage() {
    _uiState.update { it.copy(friendRequestMessage = null) }
  }
}

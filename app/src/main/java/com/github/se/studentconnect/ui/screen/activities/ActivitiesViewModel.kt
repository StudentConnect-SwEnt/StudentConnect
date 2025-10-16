package com.github.se.studentconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import com.github.se.studentconnect.ui.screen.activities.CarouselDisplayItem
import com.github.se.studentconnect.ui.screen.activities.EventCarouselItem
import com.github.se.studentconnect.ui.screen.activities.EventTab
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.activities.InvitationCarouselItem
import com.github.se.studentconnect.ui.screen.activities.InvitationStatus
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivitiesUiState(
    val items: List<CarouselDisplayItem> = emptyList(),
    val selectedTab: EventTab = EventTab.Upcoming,
    val isLoading: Boolean = true
)

class ActivitiesViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(ActivitiesUiState())
  val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

  fun onTabSelected(tab: EventTab) {
    _uiState.update { it.copy(selectedTab = tab) }
    refreshEvents(AuthenticationProvider.currentUser)
  }

  fun acceptInvitation(invitation: Invitation) {
    viewModelScope.launch {
      val currentUserUid = AuthenticationProvider.currentUser
      userRepository.acceptInvitation(invitation.eventId, currentUserUid)
      eventRepository.addParticipantToEvent(invitation.eventId, EventParticipant(currentUserUid))

      _uiState.update { currentState ->
        val updatedItems =
            currentState.items.filterNot { item ->
              item is InvitationCarouselItem && item.invitation.eventId == invitation.eventId
            }
        currentState.copy(items = updatedItems)
      }
    }
  }

  fun declineInvitation(invitation: Invitation) {
    viewModelScope.launch {
      val currentUserUid = AuthenticationProvider.currentUser
      userRepository.declineInvitation(invitation.eventId, currentUserUid)

      _uiState.update { currentState ->
        val updatedItems =
            currentState.items.map { item ->
              if (item is InvitationCarouselItem && item.invitation.eventId == invitation.eventId) {
                item.copy(invitation = item.invitation.copy(status = InvitationStatus.Declined))
              } else {
                item
              }
            }
        currentState.copy(items = updatedItems)
      }
    }
  }

  fun refreshEvents(userUid: String?) {
    if (userUid == null) {
      _uiState.update { it.copy(items = emptyList(), isLoading = false) }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      val now = Timestamp.now()
      val items: List<CarouselDisplayItem> =
          when (uiState.value.selectedTab) {
            EventTab.Upcoming -> {
              val joinedEvents = userRepository.getJoinedEvents(userUid)
              joinedEvents
                  .mapNotNull { eventId ->
                    try {
                      eventRepository.getEvent(eventId)
                    } catch (e: Exception) {
                      null
                    }
                  }
                  .filter { event ->
                    val endTime =
                        event.end
                            ?: run {
                              val cal = Calendar.getInstance()
                              cal.time = event.start.toDate()
                              cal.add(Calendar.HOUR_OF_DAY, 5)
                              Timestamp(cal.time)
                            }
                    endTime > now
                  }
                  .map { EventCarouselItem(it) }
            }
            EventTab.Invitations -> {
              val invitations = userRepository.getInvitations(userUid)
              invitations.mapNotNull { invitation ->
                try {
                  val event = eventRepository.getEvent(invitation.eventId)
                  val sender = userRepository.getUserById(invitation.from)
                  InvitationCarouselItem(
                      invitation = invitation,
                      event = event,
                      invitedBy = sender?.firstName ?: "Anonymous")
                } catch (e: Exception) {
                  null
                }
              }
            }
            EventTab.Past -> {
              val joinedEvents = userRepository.getJoinedEvents(userUid)
              joinedEvents
                  .mapNotNull { eventId ->
                    try {
                      eventRepository.getEvent(eventId)
                    } catch (e: Exception) {
                      null
                    }
                  }
                  .filter { event -> event.start < now }
                  .map { EventCarouselItem(it) }
            }
          }
      _uiState.update { it.copy(items = items, isLoading = false) }
    }
  }
}

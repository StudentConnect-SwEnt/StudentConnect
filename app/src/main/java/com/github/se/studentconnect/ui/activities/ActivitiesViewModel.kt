package com.github.se.studentconnect.ui.activities

import androidx.lifecycle.ViewModel

class ActivitiesViewModel :
    ViewModel(
        // localEventRepository : EventLocalRepository = EventLocalRepository()
        // userRepository: UserRepository = UserRepository()
        ) {

  fun leaveEvent(eventId: String) {
    // userRepository.leaveEvent(eventId)
    // localEventRepository.removeParticipantFromEvent(eventId,Firebase.auth.currentUser?.uid)
  }

  fun getJoinedEvents() {
    // localEventRepository.getEventsAttendedByUser(Firebase.auth.currentUser?.uid)
  }
}

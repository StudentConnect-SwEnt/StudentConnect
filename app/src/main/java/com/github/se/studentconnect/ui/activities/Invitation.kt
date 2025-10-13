package com.github.se.studentconnect.ui.activities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

enum class InvitationStatus {
  Pending,
  Accepted,
  Declined
}

data class Invitation(
    val eventId: String = "",
    val from: String = "",
    val status: InvitationStatus = InvitationStatus.Pending,
    @ServerTimestamp val timestamp: Timestamp? = null
)

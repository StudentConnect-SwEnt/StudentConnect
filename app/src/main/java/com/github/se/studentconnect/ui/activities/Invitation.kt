package com.github.se.studentconnect.ui.activities

import com.google.firebase.Timestamp

enum class InvitationStatus {
  Pending,
  Accepted,
  Declined
}

// -------------------------------
// DATA CLASSES
// -------------------------------
data class Invitation(
    val eventId: String = "",
    val from: String = "",
    val status: InvitationStatus = InvitationStatus.Pending,
    val timestamp: Timestamp?
)

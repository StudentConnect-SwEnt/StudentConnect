package com.github.se.studentconnect.model.event

import com.google.firebase.Timestamp

/** A data class representing an event participant. */
data class EventParticipant(
    val uid: String,
    val joinedAt: Timestamp,
)

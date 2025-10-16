package com.github.se.studentconnect.model.event

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/** A data class representing an event participant. */
data class EventParticipant(val uid: String, @ServerTimestamp val joinedAt: Timestamp? = null)

package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp

/**
 * A class representing an event.
 *
 * Note on ownership:
 * - `ownerId`: The user ID of the person who created the event (always required)
 * - `organizationId`: The organization ID if the event is owned by an organization (optional)
 *
 * For personal events: organizationId = null, ownerId = creator For organization events:
 * organizationId = org ID, ownerId = creator (who created it on behalf of org)
 */
sealed class Event {
  abstract val uid: String
  abstract val ownerId: String // User who created the event
  abstract val organizationId:
      String? // Organization that owns the event (null for personal events)
  abstract val title: String
  abstract val description: String
  abstract val imageUrl: String? // optional image url (in Firebase Storage)
  abstract val location: Location? // optional location
  abstract val start: Timestamp
  abstract val end: Timestamp? // optional end time
  abstract val maxCapacity: UInt? // optional maximum capacity (max number of participants)
  abstract val participationFee: UInt? // optional participation fee
  abstract val isFlash: Boolean

  open fun toMap(): Map<String, Any?> =
      mapOf(
          "uid" to uid,
          "ownerId" to ownerId,
          "organizationId" to organizationId,
          "title" to title,
          "description" to description,
          "imageUrl" to imageUrl,
          "location" to
              location?.let {
                mapOf("latitude" to it.latitude, "longitude" to it.longitude, "name" to it.name)
              },
          "start" to start,
          "end" to end,
          "maxCapacity" to maxCapacity?.toLong(),
          "participationFee" to participationFee?.toLong(),
          "isFlash" to isFlash,
      )

  /** A data class representing a private event. */
  data class Private(
      override val uid: String,
      override val ownerId: String,
      override val organizationId: String? = null,
      override val title: String,
      override val description: String,
      override val imageUrl: String? = null,
      override val location: Location? = null,
      override val start: Timestamp,
      override val end: Timestamp? = null,
      override val maxCapacity: UInt? = null,
      override val participationFee: UInt? = null,
      override val isFlash: Boolean,
  ) : Event() {
    override fun toMap(): Map<String, Any?> {
      return super.toMap() + ("type" to "private")
    }
  }

  /** A data class representing a public event. */
  data class Public(
      override val uid: String,
      override val ownerId: String,
      override val organizationId: String? = null,
      override val title: String,
      override val description: String,
      override val imageUrl: String? = null,
      override val location: Location? = null,
      override val start: Timestamp,
      override val end: Timestamp? = null,
      override val maxCapacity: UInt? = null,
      override val participationFee: UInt? = null,
      override val isFlash: Boolean,
      val subtitle: String,
      val tags: List<String> = emptyList(),
      val website: String? = null, // optional website
  ) : Event() {
    override fun toMap(): Map<String, Any?> =
        super.toMap() +
            mapOf("subtitle" to subtitle, "tags" to tags, "website" to website, "type" to "public")
  }
}

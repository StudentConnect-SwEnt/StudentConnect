// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

import android.util.Log
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.activities.InvitationStatus
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

private const val ONLY_OWNER_CAN_PERFORM_THIS_ACTION =
    "Only the owner of the event can perform this action"

/**
 * Implementation of [EventRepository] using Firebase Firestore.
 *
 * @property db The Firestore database instance.
 */
class EventRepositoryFirestore(private val db: FirebaseFirestore) : EventRepository {
  companion object {
    const val EVENTS_COLLECTION_PATH = "events"
    const val PARTICIPANTS_COLLECTION_PATH = "participants"
    const val INVITATIONS_COLLECTION_PATH = "invitations"
  }

  override fun getNewUid(): String {
    // generate a Firestore unique ID
    return db.collection(EVENTS_COLLECTION_PATH).document().id
  }

  private fun eventFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Event {
    return when (val type = documentSnapshot.getString("type")) {
      "private" -> privateEventFromDocumentSnapshot(documentSnapshot)
      "public" -> publicEventFromDocumentSnapshot(documentSnapshot)
      else -> throw IllegalArgumentException("Unknown event type: $type")
    }
  }

  /**
   * Extracts common event fields from a DocumentSnapshot. This helper function eliminates code
   * duplication between private and public event parsing.
   */
  private fun extractCommonEventFields(documentSnapshot: DocumentSnapshot): CommonEventFields {
    val description = checkNotNull(documentSnapshot.getString("description"))
    val imageUrl = documentSnapshot.getString("imageUrl")
    val location =
        (documentSnapshot.get("location") as? Map<*, *>)?.let {
          Location(
              latitude = it["latitude"] as Double,
              longitude = it["longitude"] as Double,
              name = it["name"] as? String)
        }
    val start = checkNotNull(documentSnapshot.getTimestamp("start"))
    val end = documentSnapshot.getTimestamp("end")
    val maxCapacity = documentSnapshot.getLong("maxCapacity")?.toUInt()
    val participationFee = documentSnapshot.getLong("participationFee")?.toUInt()
    val isFlash = documentSnapshot.getBoolean("isFlash") ?: false

    return CommonEventFields(
        description = description,
        imageUrl = imageUrl,
        location = location,
        start = start,
        end = end,
        maxCapacity = maxCapacity,
        participationFee = participationFee,
        isFlash = isFlash)
  }

  /** Data class to hold common event fields extracted from Firestore. */
  private data class CommonEventFields(
      val description: String,
      val imageUrl: String?,
      val location: Location?,
      val start: com.google.firebase.Timestamp,
      val end: com.google.firebase.Timestamp?,
      val maxCapacity: UInt?,
      val participationFee: UInt?,
      val isFlash: Boolean
  )

  private fun privateEventFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Event.Private {
    val uid = documentSnapshot.id
    val ownerId = checkNotNull(documentSnapshot.getString("ownerId"))
    val title = checkNotNull(documentSnapshot.getString("title"))
    val commonFields = extractCommonEventFields(documentSnapshot)

    return Event.Private(
        uid = uid,
        ownerId = ownerId,
        title = title,
        description = commonFields.description,
        imageUrl = commonFields.imageUrl,
        location = commonFields.location,
        start = commonFields.start,
        end = commonFields.end,
        maxCapacity = commonFields.maxCapacity,
        participationFee = commonFields.participationFee,
        isFlash = commonFields.isFlash)
  }

  private fun publicEventFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Event.Public {
    val uid = documentSnapshot.id
    val ownerId = checkNotNull(documentSnapshot.getString("ownerId"))
    val title = checkNotNull(documentSnapshot.getString("title"))
    val subtitle = checkNotNull(documentSnapshot.getString("subtitle"))
    val commonFields = extractCommonEventFields(documentSnapshot)
    val tags = documentSnapshot.get("tags") as? List<String> ?: emptyList()
    val website = documentSnapshot.getString("website")

    return Event.Public(
        uid = uid,
        ownerId = ownerId,
        title = title,
        subtitle = subtitle,
        description = commonFields.description,
        imageUrl = commonFields.imageUrl,
        location = commonFields.location,
        start = commonFields.start,
        end = commonFields.end,
        maxCapacity = commonFields.maxCapacity,
        participationFee = commonFields.participationFee,
        isFlash = commonFields.isFlash,
        tags = tags,
        website = website)
  }

  private suspend fun getEventDocument(eventUid: String): DocumentSnapshot {
    val documentSnapshot = db.collection(EVENTS_COLLECTION_PATH).document(eventUid).get().await()
    require(documentSnapshot.exists()) { "Event $eventUid does not exist" }
    return documentSnapshot
  }

  private fun getCurrentUserId(): String {
    return Firebase.auth.currentUser?.uid
        ?: throw IllegalAccessException("User must be logged in for this action")
  }

  private fun ensureUserIsOwner(eventSnapshot: DocumentSnapshot) {
    val ownerId = eventSnapshot.getString("ownerId")
    val currentUserId = getCurrentUserId()
    Log.e("EventRepositoryFirestore", "OwnerId: $ownerId, CurrentUserId: $currentUserId")
    if (ownerId != currentUserId) {
      throw IllegalAccessException(ONLY_OWNER_CAN_PERFORM_THIS_ACTION)
    }
  }

  private suspend fun ensureUserCanViewEvent(
      eventRef: DocumentReference,
      eventSnapshot: DocumentSnapshot
  ) {
    if (eventSnapshot.getString("type") == "private") {
      val currentUserId = getCurrentUserId()
      val ownerId = eventSnapshot.getString("ownerId")

      if (ownerId != currentUserId) {
        val isParticipant =
            eventRef
                .collection(PARTICIPANTS_COLLECTION_PATH)
                .document(currentUserId)
                .get()
                .await()
                .exists()
        val isInvited =
            eventRef
                .collection(INVITATIONS_COLLECTION_PATH)
                .document(currentUserId)
                .get()
                .await()
                .exists()

        if (!isParticipant && !isInvited) {
          throw IllegalAccessException("User is not authorized to view this private event")
        }
      }
    }
  }

  override suspend fun getAllVisibleEvents(): List<Event> {
    val currentUserId = getCurrentUserId()
    val querySnapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()
    return querySnapshot.documents.mapNotNull { doc ->
      try {
        val event = eventFromDocumentSnapshot(doc)

        if (event is Event.Public) {
          Log.d(
              "EventRepositoryFirestore",
              "Successfully parsed public event: ${event.uid} - ${event.title}")
          return@mapNotNull event
        }

        if (event is Event.Private) {
          // Owner can always see their own events; use the parsed event.ownerId to avoid an extra
          // read
          if (event.ownerId == currentUserId) {
            Log.d(
                "EventRepositoryFirestore",
                "Successfully parsed private event (owner): ${event.uid} - ${event.title}")
            return@mapNotNull event
          }

          val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(event.uid)

          // Check if user is a participant (joined or has accepted invitation)
          val isParticipant =
              eventRef
                  .collection(PARTICIPANTS_COLLECTION_PATH)
                  .document(currentUserId)
                  .get()
                  .await()
                  .exists()

          if (isParticipant) {
            Log.d(
                "EventRepositoryFirestore",
                "Successfully parsed private event (participant): ${event.uid} - ${event.title}")
            return@mapNotNull event
          }

          Log.d("EventRepositoryFirestore", "Skipping private event (no access): ${event.uid}")
          return@mapNotNull null
        }

        event
      } catch (e: FirebaseFirestoreException) {
        Log.e(
            "EventRepositoryFirestore",
            "FirebaseFirestoreException parsing event ${doc.id}: ${e.message}",
            e)
        null
      } catch (e: Exception) {
        Log.e("EventRepositoryFirestore", "Exception parsing event ${doc.id}: ${e.message}", e)
        null
      }
    }
  }

  override suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event> {
    return getAllVisibleEvents().filter(predicate)
  }

  override suspend fun getEventsByOrganization(organizationId: String): List<Event> {
    val querySnapshot =
        db.collection(EVENTS_COLLECTION_PATH).whereEqualTo("ownerId", organizationId).get().await()

    return querySnapshot.documents.mapNotNull { doc ->
      try {
        eventFromDocumentSnapshot(doc)
      } catch (e: Exception) {
        Log.e("EventRepositoryFirestore", "Failed to parse event for organization", e)
        null
      }
    }
  }

  override suspend fun getEventsByOwner(userId: String): List<Event> {
    val querySnapshot =
        db.collection(EVENTS_COLLECTION_PATH).whereEqualTo("ownerId", userId).get().await()

    return querySnapshot.documents.mapNotNull { doc ->
      try {
        eventFromDocumentSnapshot(doc)
      } catch (e: Exception) {
        Log.e("EventRepositoryFirestore", "Failed to parse event for owner", e)
        null
      }
    }
  }

  override suspend fun getEvent(eventUid: String): Event {
    val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(eventUid)
    val documentSnapshot = getEventDocument(eventUid)
    ensureUserCanViewEvent(eventRef, documentSnapshot)
    return eventFromDocumentSnapshot(documentSnapshot)
  }

  private fun eventParticipantFromDocumentSnapshot(
      documentSnapshot: DocumentSnapshot
  ): EventParticipant {
    val uid = checkNotNull(documentSnapshot.getString("uid"))
    val joinedAt = documentSnapshot.getTimestamp("joinedAt")

    return EventParticipant(uid = uid, joinedAt = joinedAt)
  }

  override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
    val documentSnapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(PARTICIPANTS_COLLECTION_PATH)
            .get()
            .await()
    return documentSnapshot.documents.map(::eventParticipantFromDocumentSnapshot)
  }

  override suspend fun addEvent(event: Event) {
    val docRef = db.collection(EVENTS_COLLECTION_PATH).document(event.uid)
    val data = event.toMap()
    docRef.set(data).await()
  }

  override suspend fun editEvent(eventUid: String, newEvent: Event) {
    require(eventUid == newEvent.uid)

    val currentUserId = getCurrentUserId()
    if (newEvent.ownerId != currentUserId) {
      throw IllegalAccessException(ONLY_OWNER_CAN_PERFORM_THIS_ACTION)
    }

    try {
      val eventSnapshot = getEventDocument(eventUid)
      ensureUserIsOwner(eventSnapshot)

      val data = newEvent.toMap()
      db.collection(EVENTS_COLLECTION_PATH).document(eventUid).set(data).await()
    } catch (e: FirebaseFirestoreException) {
      if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        throw IllegalAccessException(ONLY_OWNER_CAN_PERFORM_THIS_ACTION)
      }
      throw e
    }
  }

  override suspend fun deleteEvent(eventUid: String) {
    try {
      val eventSnapshot = getEventDocument(eventUid)
      ensureUserIsOwner(eventSnapshot)
      db.collection(EVENTS_COLLECTION_PATH).document(eventUid).delete().await()
    } catch (e: FirebaseFirestoreException) {
      when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
          throw IllegalAccessException(ONLY_OWNER_CAN_PERFORM_THIS_ACTION)
        }
        FirebaseFirestoreException.Code.NOT_FOUND -> {
          throw IllegalArgumentException("Event $eventUid does not exist")
        }
        else -> throw e
      }
    }
  }

  override suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant) {
    val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(eventUid)
    val eventSnapshot = eventRef.get().await()

    require(eventSnapshot.exists()) { "Event $eventUid does not exist" }

    val participantRef = eventRef.collection(PARTICIPANTS_COLLECTION_PATH).document(participant.uid)

    // Check if participant already exists
    val participantSnapshot = participantRef.get().await()
    check(!participantSnapshot.exists()) {
      "Participant ${participant.uid} is already in event $eventUid"
    }

    val participantData = mapOf("uid" to participant.uid, "joinedAt" to participant.joinedAt)

    try {
      participantRef.set(participantData).await()
      // Verify the participant was actually added by checking again
      val verifySnapshot = participantRef.get().await()
      check(verifySnapshot.exists()) {
        "Failed to add participant ${participant.uid} to event $eventUid"
      }
    } catch (e: FirebaseFirestoreException) {
      check(e.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        "Participant ${participant.uid} is already in event $eventUid or permission denied"
      }
      throw e
    }
  }

  override suspend fun addInvitationToEvent(
      eventUid: String,
      invitedUser: String,
      currentUserId: String
  ) {
    val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(eventUid)
    val eventSnapshot = eventRef.get().await()
    require(eventSnapshot.exists()) { "Event $eventUid does not exist" }

    val owner = eventSnapshot.getString("ownerId")
    if (owner != currentUserId)
        throw IllegalAccessException("Only the owner of the event can invite users")
    eventRef
        .collection(INVITATIONS_COLLECTION_PATH)
        .document(invitedUser)
        .set(
            Invitation(eventId = eventUid, from = currentUserId, status = InvitationStatus.Pending))
        .await()
  }

  override suspend fun getEventInvitations(eventUid: String): List<String> {
    val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(eventUid)
    val snapshot = eventRef.collection(INVITATIONS_COLLECTION_PATH).get().await()
    return snapshot.documents.mapNotNull { it.id }
  }

  override suspend fun removeInvitationFromEvent(
      eventUid: String,
      invitedUser: String,
      currentUserId: String
  ) {
    val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(eventUid)
    val eventSnapshot = eventRef.get().await()
    require(eventSnapshot.exists()) { "Event $eventUid does not exist" }

    val owner = eventSnapshot.getString("ownerId")
    if (owner != currentUserId)
        throw IllegalAccessException("Only the owner of the event can invite users")

    eventRef.collection(INVITATIONS_COLLECTION_PATH).document(invitedUser).delete().await()
  }

  override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {
    if (participantUid != Firebase.auth.currentUser?.uid)
        throw IllegalAccessException("Users can only remove themselves from events")
    val participantRef =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(PARTICIPANTS_COLLECTION_PATH)
            .document(participantUid)

    participantRef.delete().await()
  }

  override suspend fun getEventStatistics(eventUid: String, followerCount: Int): EventStatistics {
    val participants = getEventParticipants(eventUid)
    val totalAttendees = participants.size

    // Fetch user data in batches using whereIn (max 30 IDs per query)
    val participantMap = participants.associateBy { it.uid }
    val userDataList = mutableListOf<Triple<String?, String?, com.google.firebase.Timestamp?>>()

    participants
        .map { it.uid }
        .chunked(30)
        .forEach { uidBatch ->
          try {
            val querySnapshot =
                db.collection("users")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), uidBatch)
                    .get()
                    .await()

            querySnapshot.documents.forEach { doc ->
              val joinedAt = participantMap[doc.id]?.joinedAt
              userDataList.add(
                  Triple(doc.getString("birthday"), doc.getString("university"), joinedAt))
            }
          } catch (e: Exception) {
            Log.e("EventRepositoryFirestore", "Error fetching user batch", e)
          }
        }

    // Calculate age distribution
    val ageGroups = mutableMapOf<String, Int>()
    AgeGroups.all.forEach { ageGroups[it] = 0 }
    ageGroups[AgeGroups.UNKNOWN] = 0

    userDataList.forEach { (birthday, _, _) ->
      val age = AgeGroups.calculateAge(birthday)
      val group = AgeGroups.getAgeGroup(age)
      ageGroups[group] = (ageGroups[group] ?: 0) + 1
    }

    val ageDistribution =
        ageGroups
            .filter { it.value > 0 }
            .map { (range, count) ->
              AgeGroupData(
                  ageRange = range,
                  count = count,
                  percentage =
                      if (totalAttendees > 0) (count.toFloat() / totalAttendees) * 100f else 0f)
            }
            .sortedByDescending { it.count }

    // Calculate campus distribution
    val campusGroups = mutableMapOf<String, Int>()
    userDataList.forEach { (_, university, _) ->
      val campus = university ?: AgeGroups.UNKNOWN // UI layer maps to R.string.stats_unknown_campus
      campusGroups[campus] = (campusGroups[campus] ?: 0) + 1
    }

    val campusDistribution =
        campusGroups
            .map { (name, count) ->
              CampusData(
                  campusName = name,
                  count = count,
                  percentage =
                      if (totalAttendees > 0) (count.toFloat() / totalAttendees) * 100f else 0f)
            }
            .sortedByDescending { it.count }

    // Calculate join rate over time
    val joinRateOverTime = calculateJoinRateOverTime(participants)

    // Calculate attendees/followers rate
    val attendeesFollowersRate = calculateAttendeesFollowersRate(totalAttendees, followerCount)

    return EventStatistics(
        eventId = eventUid,
        totalAttendees = totalAttendees,
        ageDistribution = ageDistribution,
        campusDistribution = campusDistribution,
        joinRateOverTime = joinRateOverTime,
        followerCount = followerCount,
        attendeesFollowersRate = attendeesFollowersRate)
  }
}

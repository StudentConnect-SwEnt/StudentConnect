// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

import android.util.Log
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.screen.activities.Invitation
import com.github.se.studentconnect.ui.screen.activities.InvitationStatus
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

  private fun privateEventFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Event.Private {
    val uid = documentSnapshot.id
    val ownerId = checkNotNull(documentSnapshot.getString("ownerId"))
    val title = checkNotNull(documentSnapshot.getString("title"))
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

    return Event.Private(
        uid = uid,
        ownerId = ownerId,
        title = title,
        description = description,
        imageUrl = imageUrl,
        location = location,
        start = start,
        end = end,
        maxCapacity = maxCapacity,
        participationFee = participationFee,
        isFlash = isFlash)
  }

  private fun publicEventFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Event.Public {
    val uid = documentSnapshot.id
    val ownerId = checkNotNull(documentSnapshot.getString("ownerId"))
    val title = checkNotNull(documentSnapshot.getString("title"))
    val subtitle = checkNotNull(documentSnapshot.getString("subtitle"))
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
    val tags = documentSnapshot.get("tags") as? List<String> ?: emptyList()
    val website = documentSnapshot.getString("website")

    return Event.Public(
        uid = uid,
        ownerId = ownerId,
        title = title,
        subtitle = subtitle,
        description = description,
        imageUrl = imageUrl,
        location = location,
        start = start,
        end = end,
        maxCapacity = maxCapacity,
        participationFee = participationFee,
        isFlash = isFlash,
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
    // TODO: filter based on if the currently logged in user can see the event or not; for now, gets
    //  all events
    val querySnapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()
    Log.d(
        "EventRepositoryFirestore",
        "Fetched ${querySnapshot.documents.size} event documents from Firestore")

    return querySnapshot.documents.mapNotNull { doc ->
      try {
        val event = eventFromDocumentSnapshot(doc)
        Log.d(
            "EventRepositoryFirestore",
            "Successfully parsed event: ${event.uid} - ${event.title} - Location: ${event.location}")
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
    check(!(participantSnapshot.exists())) {
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
}

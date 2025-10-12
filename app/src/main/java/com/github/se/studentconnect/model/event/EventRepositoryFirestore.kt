// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.location.Location
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
              name = it["name"] as String)
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
              name = it["name"] as String)
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

  override suspend fun getAllVisibleEvents(): List<Event> {
    // TODO: filter based on if the currently logged in user can see the event or not; for now, gets
    // all events
    val querySnapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()
    return querySnapshot.documents.map(::eventFromDocumentSnapshot)
  }

  override suspend fun getAllVisibleEventsSatisfying(predicate: (Event) -> Boolean): List<Event> {
    return getAllVisibleEvents().filter(predicate)
  }

  override suspend fun getEvent(eventUid: String): Event {
    val documentSnapshot = db.collection(EVENTS_COLLECTION_PATH).document(eventUid).get().await()
    return eventFromDocumentSnapshot(documentSnapshot)
  }

  private fun eventParticipantFromDocumentSnapshot(
      documentSnapshot: DocumentSnapshot
  ): EventParticipant {
    val uid = checkNotNull(documentSnapshot.getString("uid"))
    val joinedAt = checkNotNull(documentSnapshot.getTimestamp("joinedAt"))

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
    val data = newEvent.toMap()
    db.collection(EVENTS_COLLECTION_PATH).document(eventUid).set(data).await()
  }

  override suspend fun deleteEvent(eventUid: String) {
    db.collection(EVENTS_COLLECTION_PATH).document(eventUid).delete().await()
  }

  override suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant) {
    val eventRef = db.collection(EVENTS_COLLECTION_PATH).document(eventUid)
    val eventSnapshot = eventRef.get().await()

    // check if event exists
    if (!eventSnapshot.exists()) throw IllegalArgumentException("Event $eventUid does not exist")

    val participantRef = eventRef.collection(PARTICIPANTS_COLLECTION_PATH).document(participant.uid)

    val participantSnapshot = participantRef.get().await()

    // check if already joined
    if (participantSnapshot.exists())
        throw IllegalStateException("Participant ${participant.uid} is already in event $eventUid")

    participantRef.set(participant).await()
  }

  override suspend fun addInvitationToEvent(eventUid: String, invitedUser: User) {
    db.collection(EVENTS_COLLECTION_PATH)
        .document(eventUid)
        .collection(INVITATIONS_COLLECTION_PATH)
        .document(invitedUser.userId)
        .set(invitedUser.toMap())
        .await()
  }

  override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {
    val participantRef =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(PARTICIPANTS_COLLECTION_PATH)
            .document(participantUid)

    participantRef.delete().await()
  }
}

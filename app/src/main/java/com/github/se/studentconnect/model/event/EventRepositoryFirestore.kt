// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

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
  }

  override fun getNewUid(): String {
    // generate a Firestore unique ID
    return db.collection(EVENTS_COLLECTION_PATH).document().id
  }

  private fun eventFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Event {
    val type = documentSnapshot.getString("type")
    checkNotNull(type)

    return when (type) {
      "private" ->
          documentSnapshot.toObject(Event.Private::class.java)!!.copy(uid = documentSnapshot.id)
      "public" ->
          documentSnapshot.toObject(Event.Public::class.java)!!.copy(uid = documentSnapshot.id)
      else -> throw IllegalArgumentException("Unknown event type: $type")
    }
  }

  override suspend fun getAllVisibleEvents(): List<Event> {
    // TODO: filter based on if the currently logged in user can see the event or not; for now, gets
    // all events
    val querySnapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()
    return querySnapshot.documents.map(::eventFromDocumentSnapshot)
  }

  override suspend fun getEvent(eventUid: String): Event {
    val documentSnapshot = db.collection(EVENTS_COLLECTION_PATH).document(eventUid).get().await()
    return eventFromDocumentSnapshot(documentSnapshot)
  }

  override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
    val documentSnapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(PARTICIPANTS_COLLECTION_PATH)
            .get()
            .await()
    return documentSnapshot.documents.mapNotNull { it.toObject(EventParticipant::class.java) }
  }

  override suspend fun getEventsAttendedByUser(userUid: String): List<Event> {
    // TODO: for now, gets all events
    return getAllVisibleEvents()
  }

  override suspend fun addEvent(event: Event) {
    val docRef = db.collection(EVENTS_COLLECTION_PATH).document(event.uid)
    docRef.set(event.toMap()).await()
  }

  override suspend fun editEvent(eventUid: String, newEvent: Event) {
    require(eventUid == newEvent.uid)
    db.collection(EVENTS_COLLECTION_PATH).document(eventUid).set(newEvent).await()
  }

  override suspend fun deleteEvent(eventUid: String) {
    db.collection(EVENTS_COLLECTION_PATH).document(eventUid).delete().await()
  }

  override suspend fun addParticipantToEvent(eventUid: String, participant: EventParticipant) {
    val participantRef =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(PARTICIPANTS_COLLECTION_PATH)
            .document(participant.uid)

    val snapshot = participantRef.get().await()
    // check if already joined
    if (snapshot.exists())
        throw IllegalStateException("Participant ${participant.uid} is already in event $eventUid")

    participantRef.set(participant).await()
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

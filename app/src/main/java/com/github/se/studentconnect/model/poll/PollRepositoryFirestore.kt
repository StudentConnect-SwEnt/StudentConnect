package com.github.se.studentconnect.model.poll

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firestore implementation of [PollRepository].
 *
 * Polls are stored as subcollections under events: events/{eventUid}/polls/{pollUid} Votes are
 * stored as subcollections under polls: events/{eventUid}/polls/{pollUid}/votes/{userId}
 *
 * @property db The Firestore database instance
 */
class PollRepositoryFirestore(private val db: FirebaseFirestore) : PollRepository {

  companion object {
    const val EVENTS_COLLECTION_PATH = "events"
    const val POLLS_COLLECTION_PATH = "polls"
    const val VOTES_COLLECTION_PATH = "votes"
    const val PARTICIPANTS_COLLECTION_PATH = "participants"
  }

  override fun getNewUid(): String {
    // Generate a unique ID without creating any documents
    return db.collection(POLLS_COLLECTION_PATH).document().id
  }

  private fun getCurrentUserId(): String {
    return Firebase.auth.currentUser?.uid
        ?: throw IllegalAccessException("User must be logged in to perform this action")
  }

  private suspend fun ensureUserIsEventOwner(eventUid: String) {
    val currentUserId = getCurrentUserId()
    val eventDoc = db.collection(EVENTS_COLLECTION_PATH).document(eventUid).get().await()

    require(eventDoc.exists()) { "Event $eventUid does not exist" }

    val ownerId = eventDoc["ownerId"] as? String
    if (ownerId != currentUserId) {
      throw IllegalAccessException("Only the event owner can perform this action")
    }
  }

  private suspend fun ensureUserIsParticipant(eventUid: String, userId: String) {
    val participantDoc =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(PARTICIPANTS_COLLECTION_PATH)
            .document(userId)
            .get()
            .await()

    if (!participantDoc.exists()) {
      throw IllegalAccessException("Only event participants can vote")
    }
  }

  private fun pollFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): Poll {
    val uid = documentSnapshot.id
    val eventUid = checkNotNull(documentSnapshot["eventUid"] as? String)
    val question = checkNotNull(documentSnapshot["question"] as? String)
    val createdAt = documentSnapshot["createdAt"] as? Timestamp ?: Timestamp.now()
    val isActive = documentSnapshot["isActive"] as? Boolean ?: true

    @Suppress("UNCHECKED_CAST")
    val optionsData = documentSnapshot["options"] as? List<Map<String, Any>> ?: emptyList()
    val options =
        optionsData.map { optionMap ->
          PollOption(
              optionId = optionMap["optionId"] as? String ?: error("Missing optionId"),
              text = optionMap["text"] as? String ?: error("Missing text"),
              // Firestore stores numbers as Long, safe cast to Int
              voteCount = (optionMap["voteCount"] as? Long)?.toInt() ?: 0)
        }

    return Poll(
        uid = uid,
        eventUid = eventUid,
        question = question,
        options = options,
        createdAt = createdAt,
        isActive = isActive)
  }

  private fun pollVoteFromDocumentSnapshot(documentSnapshot: DocumentSnapshot): PollVote {
    val userId = documentSnapshot.id
    val pollUid = checkNotNull(documentSnapshot["pollUid"] as? String)
    val optionId = checkNotNull(documentSnapshot["optionId"] as? String)
    val votedAt = documentSnapshot["votedAt"] as? Timestamp ?: Timestamp.now()

    return PollVote(userId = userId, pollUid = pollUid, optionId = optionId, votedAt = votedAt)
  }

  override suspend fun createPoll(poll: Poll) {
    ensureUserIsEventOwner(poll.eventUid)

    val pollRef =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(poll.eventUid)
            .collection(POLLS_COLLECTION_PATH)
            .document(poll.uid)

    pollRef.set(poll.toMap()).await()
    Log.d("PollRepositoryFirestore", "Created poll ${poll.uid} for event ${poll.eventUid}")
  }

  override suspend fun getActivePolls(eventUid: String): List<Poll> {
    val querySnapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(POLLS_COLLECTION_PATH)
            .whereEqualTo("isActive", true)
            .get()
            .await()

    return querySnapshot.documents.mapNotNull { doc ->
      try {
        pollFromDocumentSnapshot(doc)
      } catch (e: Exception) {
        Log.e("PollRepositoryFirestore", "Error parsing poll ${doc.id}: ${e.message}", e)
        null
      }
    }
  }

  override suspend fun getPoll(eventUid: String, pollUid: String): Poll? {
    val doc =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(POLLS_COLLECTION_PATH)
            .document(pollUid)
            .get()
            .await()

    return if (doc.exists()) {
      try {
        pollFromDocumentSnapshot(doc)
      } catch (e: Exception) {
        Log.e("PollRepositoryFirestore", "Error parsing poll $pollUid: ${e.message}", e)
        null
      }
    } else {
      null
    }
  }

  override suspend fun submitVote(eventUid: String, vote: PollVote) {
    val currentUserId = getCurrentUserId()
    require(vote.userId == currentUserId) { "Can only submit votes for yourself" }

    val pollRef =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(POLLS_COLLECTION_PATH)
            .document(vote.pollUid)

    val pollDoc = pollRef.get().await()
    check(pollDoc.exists()) { "Poll ${vote.pollUid} not found" }

    val poll = pollFromDocumentSnapshot(pollDoc)
    require(poll.eventUid == eventUid) { "Poll ${vote.pollUid} does not belong to event $eventUid" }
    require(poll.isActive) { "Poll is no longer active" }

    ensureUserIsParticipant(eventUid, currentUserId)

    val voteRef = pollRef.collection(VOTES_COLLECTION_PATH).document(currentUserId)

    // Check if user has already voted
    val existingVote = voteRef.get().await()
    check(!existingVote.exists()) { "User has already voted on this poll" }

    // Submit vote and increment option count
    try {
      db.runTransaction { transaction ->
            // Read poll data before performing any writes
            val pollSnapshot = transaction[pollRef]
            @Suppress("UNCHECKED_CAST")
            val currentOptions = pollSnapshot["options"] as? List<Map<String, Any>> ?: emptyList()
            val updatedOptions =
                currentOptions.map { optionMap ->
                  if (optionMap["optionId"] == vote.optionId) {
                    // Firestore stores numbers as Long, safe cast to Int
                    val currentCount = (optionMap["voteCount"] as? Long)?.toInt() ?: 0
                    optionMap + ("voteCount" to currentCount + 1)
                  } else {
                    optionMap
                  }
                }

            transaction[voteRef] = vote.toMap()
            transaction.update(pollRef, "options", updatedOptions)
          }
          .await()

      Log.d("PollRepositoryFirestore", "User $currentUserId voted on poll ${vote.pollUid}")
    } catch (e: Exception) {
      Log.e("PollRepositoryFirestore", "Failed to submit vote: ${e.message}", e)
      throw IllegalStateException("Failed to submit vote: ${e.message}", e)
    }
  }

  override suspend fun getUserVote(eventUid: String, pollUid: String, userId: String): PollVote? {
    val doc =
        db.collection(EVENTS_COLLECTION_PATH)
            .document(eventUid)
            .collection(POLLS_COLLECTION_PATH)
            .document(pollUid)
            .collection(VOTES_COLLECTION_PATH)
            .document(userId)
            .get()
            .await()

    return if (doc.exists()) {
      try {
        pollVoteFromDocumentSnapshot(doc)
      } catch (e: Exception) {
        Log.e("PollRepositoryFirestore", "Error parsing vote: ${e.message}", e)
        null
      }
    } else {
      null
    }
  }

  override suspend fun closePoll(eventUid: String, pollUid: String) {
    ensureUserIsEventOwner(eventUid)

    db.collection(EVENTS_COLLECTION_PATH)
        .document(eventUid)
        .collection(POLLS_COLLECTION_PATH)
        .document(pollUid)
        .update("isActive", false)
        .await()

    Log.d("PollRepositoryFirestore", "Closed poll $pollUid")
  }

  override suspend fun deletePoll(eventUid: String, pollUid: String) {
    ensureUserIsEventOwner(eventUid)

    db.collection(EVENTS_COLLECTION_PATH)
        .document(eventUid)
        .collection(POLLS_COLLECTION_PATH)
        .document(pollUid)
        .delete()
        .await()

    Log.d("PollRepositoryFirestore", "Deleted poll $pollUid")
  }
}

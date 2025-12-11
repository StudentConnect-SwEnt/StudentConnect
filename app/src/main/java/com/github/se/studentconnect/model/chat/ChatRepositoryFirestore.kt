package com.github.se.studentconnect.model.chat

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Firestore implementation of ChatRepository.
 *
 * This repository uses Firebase Firestore to store and retrieve chat messages and typing statuses
 * in real-time.
 *
 * Collections structure: - /events/{eventId}/messages/{messageId} - Chat messages -
 * /events/{eventId}/typing/{userId} - Typing status
 */
class ChatRepositoryFirestore(private val db: FirebaseFirestore) : ChatRepository {

  companion object {
    private const val TAG = "ChatRepositoryFirestore"
    private const val EVENTS_COLLECTION = "events"
    private const val MESSAGES_SUBCOLLECTION = "messages"
    private const val TYPING_SUBCOLLECTION = "typing"
    private const val TYPING_TIMEOUT_SECONDS = 5L // Consider typing expired after 5 seconds
  }

  override fun observeMessages(eventId: String): Flow<List<ChatMessage>> = callbackFlow {
    val messagesRef =
        db.collection(EVENTS_COLLECTION)
            .document(eventId)
            .collection(MESSAGES_SUBCOLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)

    val subscription =
        messagesRef.addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Error observing messages for event $eventId", error)
            trySend(emptyList())
            return@addSnapshotListener
          }

          if (snapshot != null) {
            val messages =
                snapshot.documents.mapNotNull { doc ->
                  try {
                    ChatMessage.fromMap(doc.data ?: emptyMap())
                  } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message ${doc.id}", e)
                    null
                  }
                }
            trySend(messages)
          } else {
            trySend(emptyList())
          }
        }

    awaitClose { subscription.remove() }
  }

  override fun sendMessage(
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENTS_COLLECTION)
        .document(message.eventId)
        .collection(MESSAGES_SUBCOLLECTION)
        .document(message.messageId)
        .set(message.toMap())
        .addOnSuccessListener {
          Log.d(TAG, "Message ${message.messageId} sent successfully")
          onSuccess()
        }
        .addOnFailureListener { exception ->
          Log.e(TAG, "Error sending message ${message.messageId}", exception)
          onFailure(exception)
        }
  }

  override fun observeTypingUsers(eventId: String): Flow<List<TypingStatus>> = callbackFlow {
    val typingRef =
        db.collection(EVENTS_COLLECTION).document(eventId).collection(TYPING_SUBCOLLECTION)

    val subscription =
        typingRef.addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.e(TAG, "Error observing typing status for event $eventId", error)
            trySend(emptyList())
            return@addSnapshotListener
          }

          if (snapshot != null) {
            val now = Timestamp.now()
            val typingUsers =
                snapshot.documents
                    .mapNotNull { doc ->
                      try {
                        TypingStatus.fromMap(doc.data ?: emptyMap())
                      } catch (e: Exception) {
                        Log.e(TAG, "Error parsing typing status ${doc.id}", e)
                        null
                      }
                    }
                    .filter { status ->
                      // Only include users who are typing and whose status is recent
                      status.isTyping &&
                          (now.seconds - status.lastUpdate.seconds) < TYPING_TIMEOUT_SECONDS
                    }
            trySend(typingUsers)
          } else {
            trySend(emptyList())
          }
        }

    awaitClose { subscription.remove() }
  }

  override fun updateTypingStatus(
      typingStatus: TypingStatus,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(EVENTS_COLLECTION)
        .document(typingStatus.eventId)
        .collection(TYPING_SUBCOLLECTION)
        .document(typingStatus.userId)
        .set(typingStatus.toMap())
        .addOnSuccessListener {
          Log.d(TAG, "Typing status updated for user ${typingStatus.userId}")
          onSuccess()
        }
        .addOnFailureListener { exception ->
          Log.e(TAG, "Error updating typing status for user ${typingStatus.userId}", exception)
          onFailure(exception)
        }
  }

  override fun getNewMessageId(): String {
    return db.collection(EVENTS_COLLECTION)
        .document()
        .collection(MESSAGES_SUBCOLLECTION)
        .document()
        .id
  }
}

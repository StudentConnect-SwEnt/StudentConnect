package com.github.se.studentconnect.model.poll

import com.google.firebase.Timestamp

/**
 * Represents a poll option with its unique identifier, text, and vote count.
 *
 * @property optionId Unique identifier for the option
 * @property text The text content of the option
 * @property voteCount Number of votes this option has received
 */
data class PollOption(val optionId: String, val text: String, val voteCount: Int = 0) {
  fun toMap(): Map<String, Any> =
      mapOf("optionId" to optionId, "text" to text, "voteCount" to voteCount)
}

/**
 * Represents a poll created by an event organizer.
 *
 * @property uid Unique identifier for the poll
 * @property eventUid The event this poll belongs to
 * @property question The poll question
 * @property options List of available options
 * @property createdAt Timestamp when the poll was created
 * @property isActive Whether the poll is currently active
 */
data class Poll(
    val uid: String,
    val eventUid: String,
    val question: String,
    val options: List<PollOption>,
    val createdAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
) {
  fun toMap(): Map<String, Any> =
      mapOf(
          "uid" to uid,
          "eventUid" to eventUid,
          "question" to question,
          "options" to options.map { it.toMap() },
          "createdAt" to createdAt,
          "isActive" to isActive)
}

/**
 * Represents a user's vote on a poll.
 *
 * @property userId The user who voted
 * @property pollUid The poll being voted on
 * @property optionId The selected option
 * @property votedAt Timestamp when the vote was cast
 */
data class PollVote(
    val userId: String,
    val pollUid: String,
    val optionId: String,
    val votedAt: Timestamp = Timestamp.now()
) {
  fun toMap(): Map<String, Any> =
      mapOf("userId" to userId, "pollUid" to pollUid, "optionId" to optionId, "votedAt" to votedAt)
}

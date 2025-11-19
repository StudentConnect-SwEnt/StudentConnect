package com.github.se.studentconnect.model.poll

/**
 * Repository interface for managing polls and votes.
 *
 * Polls are stored as subcollections under event documents in Firestore: events/{eventUid}/polls
 */
interface PollRepository {

  /**
   * Generates and returns a new unique identifier for a poll.
   *
   * @return A newly generated unique poll identifier.
   */
  fun getNewUid(): String

  /**
   * Creates a new poll for an event. Only the event owner can create polls.
   *
   * @param poll The poll to create
   * @throws IllegalAccessException if the current user is not the event owner
   */
  suspend fun createPoll(poll: Poll)

  /**
   * Retrieves all active polls for a specific event.
   *
   * @param eventUid The event identifier
   * @return List of active polls
   */
  suspend fun getActivePolls(eventUid: String): List<Poll>

  /**
   * Retrieves a specific poll by its UID.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   * @return The poll, or null if not found
   */
  suspend fun getPoll(eventUid: String, pollUid: String): Poll?

  /**
   * Submits a vote for a poll. Only event participants can vote.
   *
   * @param eventUid The event the poll belongs to
   * @param vote The vote to submit
   * @throws IllegalAccessException if the user is not a participant
   * @throws IllegalStateException if the user has already voted
   */
  suspend fun submitVote(eventUid: String, vote: PollVote)

  /**
   * Retrieves the current user's vote for a specific poll.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   * @param userId The user identifier
   * @return The user's vote, or null if they haven't voted
   */
  suspend fun getUserVote(eventUid: String, pollUid: String, userId: String): PollVote?

  /**
   * Closes a poll, preventing further votes. Only the event owner can close polls.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   * @throws IllegalAccessException if the current user is not the event owner
   */
  suspend fun closePoll(eventUid: String, pollUid: String)

  /**
   * Deletes a poll. Only the event owner can delete polls.
   *
   * @param eventUid The event identifier
   * @param pollUid The poll identifier
   * @throws IllegalAccessException if the current user is not the event owner
   */
  suspend fun deletePoll(eventUid: String, pollUid: String)
}

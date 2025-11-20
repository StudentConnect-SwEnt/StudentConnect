package com.github.se.studentconnect.model.poll

import java.util.UUID

class PollRepositoryLocal : PollRepository {
  private val polls = mutableMapOf<String, Poll>()
  private val votes = mutableMapOf<String, MutableMap<String, PollVote>>()

  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun createPoll(poll: Poll) {
    polls[poll.uid] = poll
    votes[poll.uid] = mutableMapOf()
  }

  override suspend fun getPoll(eventUid: String, pollUid: String): Poll? {
    return polls[pollUid]?.takeIf { it.eventUid == eventUid }
  }

  override suspend fun getActivePolls(eventUid: String): List<Poll> {
    return polls.values.filter { it.eventUid == eventUid && it.isActive }
  }

  override suspend fun updatePoll(poll: Poll) {
    if (polls.containsKey(poll.uid)) {
      polls[poll.uid] = poll
    }
  }

  override suspend fun closePoll(eventUid: String, pollUid: String) {
    polls[pollUid]?.let { poll ->
      if (poll.eventUid == eventUid) {
        polls[pollUid] = poll.copy(isActive = false)
      }
    }
  }

  override suspend fun submitVote(eventUid: String, vote: PollVote) {
    val poll = polls[vote.pollUid]
    if (poll?.eventUid == eventUid) {
      val pollVotes = votes.getOrPut(vote.pollUid) { mutableMapOf() }
      if (pollVotes.containsKey(vote.userId)) {
        throw IllegalStateException("User has already voted on this poll")
      }
      pollVotes[vote.userId] = vote

      // Update vote count
      val updatedOptions =
          poll.options.map { option ->
            if (option.optionId == vote.optionId) {
              option.copy(voteCount = option.voteCount + 1)
            } else {
              option
            }
          }
      polls[vote.pollUid] = poll.copy(options = updatedOptions)
    }
  }

  override suspend fun getUserVote(eventUid: String, pollUid: String, userId: String): PollVote? {
    val poll = polls[pollUid]
    return if (poll?.eventUid == eventUid) {
      votes[pollUid]?.get(userId)
    } else {
      null
    }
  }

  override suspend fun deletePoll(eventUid: String, pollUid: String) {
    polls[pollUid]?.let { poll ->
      if (poll.eventUid == eventUid) {
        polls.remove(pollUid)
        votes.remove(pollUid)
      }
    }
  }

  fun clear() {
    polls.clear()
    votes.clear()
  }
}

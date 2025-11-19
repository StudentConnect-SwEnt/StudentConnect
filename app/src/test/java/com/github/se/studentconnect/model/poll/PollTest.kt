package com.github.se.studentconnect.model.poll

import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.*
import org.junit.Test

class PollTest {

  private val now = Timestamp(Date())

  @Test
  fun pollOption_toMap_correctlyConvertsToMap() {
    val option = PollOption(optionId = "opt1", text = "Option 1", voteCount = 5)

    val map = option.toMap()

    assertEquals("opt1", map["optionId"])
    assertEquals("Option 1", map["text"])
    assertEquals(5, map["voteCount"])
  }

  @Test
  fun pollOption_defaultVoteCount_isZero() {
    val option = PollOption(optionId = "opt1", text = "Option 1")

    assertEquals(0, option.voteCount)
  }

  @Test
  fun poll_toMap_correctlyConvertsToMap() {
    val options =
        listOf(
            PollOption("opt1", "Option 1", 3),
            PollOption("opt2", "Option 2", 7),
        )
    val poll =
        Poll(
            uid = "poll123",
            eventUid = "event456",
            question = "What is your favorite color?",
            options = options,
            createdAt = now,
            isActive = true)

    val map = poll.toMap()

    assertEquals("poll123", map["uid"])
    assertEquals("event456", map["eventUid"])
    assertEquals("What is your favorite color?", map["question"])
    assertEquals(now, map["createdAt"])
    assertEquals(true, map["isActive"])

    @Suppress("UNCHECKED_CAST") val optionMaps = map["options"] as List<Map<String, Any>>
    assertEquals(2, optionMaps.size)
    assertEquals("opt1", optionMaps[0]["optionId"])
    assertEquals("Option 1", optionMaps[0]["text"])
    assertEquals(3, optionMaps[0]["voteCount"])
  }

  @Test
  fun poll_defaultValues_areCorrect() {
    val options = listOf(PollOption("opt1", "Option 1"))
    val poll =
        Poll(uid = "poll123", eventUid = "event456", question = "Test question?", options = options)

    assertTrue(poll.isActive)
    assertNotNull(poll.createdAt)
  }

  @Test
  fun pollVote_toMap_correctlyConvertsToMap() {
    val vote =
        PollVote(
            userId = "user123",
            pollUid = "poll456",
            optionId = "opt1",
            votedAt = now,
        )

    val map = vote.toMap()

    assertEquals("user123", map["userId"])
    assertEquals("poll456", map["pollUid"])
    assertEquals("opt1", map["optionId"])
    assertEquals(now, map["votedAt"])
  }

  @Test
  fun pollVote_defaultVotedAt_isSet() {
    val vote = PollVote(userId = "user123", pollUid = "poll456", optionId = "opt1")

    assertNotNull(vote.votedAt)
  }

  @Test
  fun poll_emptyOptions_createsValidPoll() {
    val poll =
        Poll(
            uid = "poll123", eventUid = "event456", question = "Empty poll?", options = emptyList())

    assertEquals(0, poll.options.size)
    assertTrue(poll.isActive)
  }

  @Test
  fun poll_multipleOptions_allStoredCorrectly() {
    val options =
        listOf(
            PollOption("opt1", "Red", 1),
            PollOption("opt2", "Blue", 2),
            PollOption("opt3", "Green", 3),
            PollOption("opt4", "Yellow", 0))

    val poll =
        Poll(
            uid = "poll123", eventUid = "event456", question = "Favorite color?", options = options)

    assertEquals(4, poll.options.size)
    assertEquals("Red", poll.options[0].text)
    assertEquals(1, poll.options[0].voteCount)
    assertEquals("Yellow", poll.options[3].text)
    assertEquals(0, poll.options[3].voteCount)
  }

  @Test
  fun pollOption_equality_worksCorrectly() {
    val opt1 = PollOption("opt1", "Option 1", 5)
    val opt2 = PollOption("opt1", "Option 1", 5)
    val opt3 = PollOption("opt2", "Option 1", 5)

    assertEquals(opt1, opt2)
    assertNotEquals(opt1, opt3)
  }

  @Test
  fun poll_equality_worksCorrectly() {
    val options = listOf(PollOption("opt1", "Option 1"))
    val poll1 =
        Poll(uid = "poll123", eventUid = "event456", question = "Question?", options = options)
    val poll2 =
        Poll(uid = "poll123", eventUid = "event456", question = "Question?", options = options)
    val poll3 =
        Poll(uid = "poll999", eventUid = "event456", question = "Question?", options = options)

    assertEquals(poll1.uid, poll2.uid)
    assertNotEquals(poll1.uid, poll3.uid)
  }
}

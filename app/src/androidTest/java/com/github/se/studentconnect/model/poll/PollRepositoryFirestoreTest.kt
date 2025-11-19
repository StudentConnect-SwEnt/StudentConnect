package com.github.se.studentconnect.model.poll

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PollRepositoryFirestoreTest : FirestoreStudentConnectTest() {
  private val now = Timestamp(Date())
  private lateinit var auth: FirebaseAuth
  private lateinit var eventRepository: EventRepository
  private lateinit var pollRepository: PollRepository

  private var ownerId = ""
  private var participantId = ""
  private var otherId = ""
  private lateinit var testEvent: Event.Public

  @Before
  override fun setUp() {
    super.setUp()
    auth = FirebaseEmulator.auth
    eventRepository = EventRepositoryProvider.repository
    pollRepository = PollRepositoryProvider.repository

    runBlocking {
      // Create test users
      val users = mapOf("owner" to "123456", "participant" to "123456", "other" to "123456")

      for ((email, password) in users) {
        try {
          val result = auth.createUserWithEmailAndPassword("${email}@test.com", password).await()
          when (email) {
            "owner" -> ownerId = result.user?.uid ?: ""
            "participant" -> participantId = result.user?.uid ?: ""
            "other" -> otherId = result.user?.uid ?: ""
          }
        } catch (_: FirebaseAuthUserCollisionException) {
          val result = auth.signInWithEmailAndPassword("${email}@test.com", password).await()
          when (email) {
            "owner" -> ownerId = result.user?.uid ?: ""
            "participant" -> participantId = result.user?.uid ?: ""
            "other" -> otherId = result.user?.uid ?: ""
          }
        }
      }

      // Sign in as owner
      auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

      // Create a test event
      testEvent =
          Event.Public(
              uid = eventRepository.getNewUid(),
              ownerId = ownerId,
              title = "Test Event",
              subtitle = "Test Subtitle",
              description = "Test Description",
              imageUrl = null,
              location = Location(0.0, 0.0, "Test Location"),
              start = now,
              end = null,
              maxCapacity = 10u,
              participationFee = 0u,
              tags = listOf("test"),
              website = null,
              isFlash = false)

      eventRepository.addEvent(testEvent)

      // Add participant to event
      auth.signInWithEmailAndPassword("participant@test.com", "123456").await()
      eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))
      auth.signInWithEmailAndPassword("owner@test.com", "123456").await()
    }
  }

  @Test
  fun getNewUid_returnsNonEmptyString() {
    val uid = pollRepository.getNewUid()

    assertNotNull(uid)
    assertTrue(uid.isNotEmpty())
  }

  @Test
  fun getNewUid_returnsDifferentUidsOnMultipleCalls() {
    val uid1 = pollRepository.getNewUid()
    val uid2 = pollRepository.getNewUid()

    assertNotEquals(uid1, uid2)
  }

  @Test
  fun createPoll_asOwner_succeeds() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test Question?",
            options = listOf(PollOption("opt1", "Option 1", 0), PollOption("opt2", "Option 2", 0)),
            createdAt = now,
            isActive = true)

    pollRepository.createPoll(poll)

    val retrievedPoll = pollRepository.getPoll(testEvent.uid, poll.uid)
    assertNotNull(retrievedPoll)
    assertEquals(poll.question, retrievedPoll?.question)
    assertEquals(2, retrievedPoll?.options?.size)
  }

  @Test
  fun createPoll_asNonOwner_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test Question?",
            options = listOf(PollOption("opt1", "Option 1")),
            isActive = true)

    try {
      pollRepository.createPoll(poll)
      fail("Expected IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message?.contains("event owner") == true)
    }
  }

  @Test
  fun getActivePolls_returnsOnlyActivePolls() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val activePoll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Active Poll?",
            options = listOf(PollOption("opt1", "Yes"), PollOption("opt2", "No")),
            isActive = true)

    val inactivePoll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Inactive Poll?",
            options = listOf(PollOption("opt1", "Yes"), PollOption("opt2", "No")),
            isActive = false)

    pollRepository.createPoll(activePoll)
    pollRepository.createPoll(inactivePoll)

    val activePolls = pollRepository.getActivePolls(testEvent.uid)

    assertTrue(activePolls.any { it.uid == activePoll.uid })
    assertFalse(activePolls.any { it.uid == inactivePoll.uid })
  }

  @Test
  fun getPoll_existingPoll_returnsCorrectPoll() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test Question?",
            options = listOf(PollOption("opt1", "Option 1"), PollOption("opt2", "Option 2")))

    pollRepository.createPoll(poll)

    val retrieved = pollRepository.getPoll(testEvent.uid, poll.uid)

    assertNotNull(retrieved)
    assertEquals(poll.uid, retrieved?.uid)
    assertEquals(poll.question, retrieved?.question)
    assertEquals(2, retrieved?.options?.size)
  }

  @Test
  fun getPoll_nonExistentPoll_returnsNull() = runBlocking {
    val retrieved = pollRepository.getPoll(testEvent.uid, "nonexistent")

    assertNull(retrieved)
  }

  @Test
  fun submitVote_asParticipant_succeeds() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Vote Test?",
            options = listOf(PollOption("opt1", "Yes"), PollOption("opt2", "No")))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote =
        PollVote(userId = participantId, pollUid = poll.uid, optionId = "opt1", votedAt = now)

    pollRepository.submitVote(testEvent.uid, vote)

    val userVote = pollRepository.getUserVote(testEvent.uid, poll.uid, participantId)
    assertNotNull(userVote)
    assertEquals("opt1", userVote?.optionId)
  }

  @Test
  fun submitVote_asNonParticipant_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Vote Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("other@test.com", "123456").await()

    val vote = PollVote(userId = otherId, pollUid = poll.uid, optionId = "opt1")

    try {
      pollRepository.submitVote(testEvent.uid, vote)
      fail("Expected IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message?.contains("participants") == true)
    }
  }

  @Test
  fun submitVote_duplicateVote_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Vote Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote = PollVote(userId = participantId, pollUid = poll.uid, optionId = "opt1")
    pollRepository.submitVote(testEvent.uid, vote)

    try {
      pollRepository.submitVote(testEvent.uid, vote)
      fail("Expected IllegalStateException")
    } catch (e: IllegalStateException) {
      assertTrue(e.message?.contains("already voted") == true)
    }
  }

  @Test
  fun submitVote_incrementsVoteCount() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Vote Count Test?",
            options = listOf(PollOption("opt1", "Yes", 0), PollOption("opt2", "No", 0)))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote = PollVote(userId = participantId, pollUid = poll.uid, optionId = "opt1")
    pollRepository.submitVote(testEvent.uid, vote)

    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()
    val updatedPoll = pollRepository.getPoll(testEvent.uid, poll.uid)

    assertNotNull(updatedPoll)
    val option1 = updatedPoll?.options?.find { it.optionId == "opt1" }
    assertEquals(1, option1?.voteCount)
  }

  @Test
  fun submitVote_onInactivePoll_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Inactive Poll?",
            options = listOf(PollOption("opt1", "Yes")),
            isActive = false)
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote = PollVote(userId = participantId, pollUid = poll.uid, optionId = "opt1")

    try {
      pollRepository.submitVote(testEvent.uid, vote)
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("no longer active") == true)
    }
  }

  @Test
  fun submitVote_wrongUserId_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote = PollVote(userId = "wrongUserId", pollUid = poll.uid, optionId = "opt1")

    try {
      pollRepository.submitVote(testEvent.uid, vote)
      fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message?.contains("yourself") == true)
    }
  }

  @Test
  fun getUserVote_afterVoting_returnsVote() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "User Vote Test?",
            options = listOf(PollOption("opt1", "Yes"), PollOption("opt2", "No")))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote = PollVote(userId = participantId, pollUid = poll.uid, optionId = "opt2")
    pollRepository.submitVote(testEvent.uid, vote)

    val retrievedVote = pollRepository.getUserVote(testEvent.uid, poll.uid, participantId)

    assertNotNull(retrievedVote)
    assertEquals(participantId, retrievedVote?.userId)
    assertEquals("opt2", retrievedVote?.optionId)
  }

  @Test
  fun getUserVote_beforeVoting_returnsNull() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    val vote = pollRepository.getUserVote(testEvent.uid, poll.uid, participantId)

    assertNull(vote)
  }

  @Test
  fun closePoll_asOwner_succeeds() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Close Test?",
            options = listOf(PollOption("opt1", "Yes")),
            isActive = true)
    pollRepository.createPoll(poll)

    pollRepository.closePoll(testEvent.uid, poll.uid)

    val closedPoll = pollRepository.getPoll(testEvent.uid, poll.uid)
    assertNotNull(closedPoll)
    assertFalse(closedPoll?.isActive ?: true)
  }

  @Test
  fun closePoll_asNonOwner_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test?",
            options = listOf(PollOption("opt1", "Yes")),
            isActive = true)
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    try {
      pollRepository.closePoll(testEvent.uid, poll.uid)
      fail("Expected IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message?.contains("event owner") == true)
    }
  }

  @Test
  fun deletePoll_asOwner_succeeds() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Delete Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    pollRepository.deletePoll(testEvent.uid, poll.uid)

    val deletedPoll = pollRepository.getPoll(testEvent.uid, poll.uid)
    assertNull(deletedPoll)
  }

  @Test
  fun deletePoll_asNonOwner_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    try {
      pollRepository.deletePoll(testEvent.uid, poll.uid)
      fail("Expected IllegalAccessException")
    } catch (e: IllegalAccessException) {
      assertTrue(e.message?.contains("event owner") == true)
    }
  }

  @Test
  fun getActivePolls_emptyEventPolls_returnsEmptyList() = runBlocking {
    val activePolls = pollRepository.getActivePolls(testEvent.uid)

    assertTrue(activePolls.isEmpty())
  }

  @Test
  fun submitVote_nonExistentPoll_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()

    val vote = PollVote(userId = participantId, pollUid = "nonexistent", optionId = "opt1")

    try {
      pollRepository.submitVote(testEvent.uid, vote)
      fail("Expected IllegalStateException")
    } catch (e: IllegalStateException) {
      assertTrue(e.message?.contains("not found") == true)
    }
  }

  @Test
  fun submitVote_wrongEventId_throwsException() = runBlocking {
    auth.signInWithEmailAndPassword("owner@test.com", "123456").await()

    // Create first event with a poll
    val poll =
        Poll(
            uid = pollRepository.getNewUid(),
            eventUid = testEvent.uid,
            question = "Test?",
            options = listOf(PollOption("opt1", "Yes")))
    pollRepository.createPoll(poll)

    // Create a second event where participant is also a member
    val wrongEvent =
        Event.Public(
            uid = eventRepository.getNewUid(),
            ownerId = ownerId,
            title = "Wrong Event",
            subtitle = "Wrong Subtitle",
            description = "Wrong Description",
            imageUrl = null,
            location = Location(0.0, 0.0, "Wrong Location"),
            start = now,
            end = null,
            maxCapacity = 10u,
            participationFee = 0u,
            tags = listOf("test"),
            website = null,
            isFlash = false)
    eventRepository.addEvent(wrongEvent)

    // Add participant to the wrong event
    auth.signInWithEmailAndPassword("participant@test.com", "123456").await()
    eventRepository.addParticipantToEvent(wrongEvent.uid, EventParticipant(participantId))

    val vote = PollVote(userId = participantId, pollUid = poll.uid, optionId = "opt1")

    try {
      pollRepository.submitVote(wrongEvent.uid, vote)
      fail("Expected IllegalStateException")
    } catch (e: IllegalStateException) {
      assertTrue(
          e.message?.contains("not found") == true ||
              e.message?.contains("does not belong") == true)
    }
  }
}

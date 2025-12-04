// Portions of this code were generated with the help of ChatGPT
package com.github.se.studentconnect.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryFirestoreTest : FirestoreStudentConnectTest() {
  private val now = Timestamp(Date())
  private lateinit var auth: FirebaseAuth
  // Provide access to the user repository used by tests
  private val userRepository: UserRepository
    get() = UserRepositoryProvider.repository

  private val repository: EventRepository
    get() = EventRepositoryProvider.repository

  private var ownerId = ""
  private var participantId = ""
  private var invitedId = ""
  private var otherId = ""

  @Before
  override fun setUp() {
    super.setUp()
    auth = FirebaseEmulator.auth
    runBlocking {
      val users =
          mapOf(
              "owner" to "123456",
              "participant" to "123456",
              "invited" to "123456",
              "other" to "123456")

      for ((email, password) in users) {
        try {
          val result = auth.createUserWithEmailAndPassword("${email}@test.com", password).await()
          // Stocker le vrai UID Firebase
          when (email) {
            "owner" -> ownerId = result.user?.uid ?: ""
            "participant" -> participantId = result.user?.uid ?: ""
            "invited" -> invitedId = result.user?.uid ?: ""
            "other" -> otherId = result.user?.uid ?: ""
          }
        } catch (_: FirebaseAuthUserCollisionException) {
          // If the account already exists, sign in and get the UID
          val result = auth.signInWithEmailAndPassword("${email}@test.com", password).await()
          when (email) {
            "owner" -> ownerId = result.user?.uid ?: ""
            "participant" -> participantId = result.user?.uid ?: ""
            "invited" -> invitedId = result.user?.uid ?: ""
            "other" -> otherId = result.user?.uid ?: ""
          }
        }
      }

      // Fallback: If any UID is still empty, try explicit sign-in (CI race condition workaround)
      if (ownerId.isBlank()) {
        ownerId =
            auth.signInWithEmailAndPassword("owner@test.com", "123456").await().user?.uid ?: ""
      }
      if (participantId.isBlank()) {
        participantId =
            auth.signInWithEmailAndPassword("participant@test.com", "123456").await().user?.uid
                ?: ""
      }
      if (invitedId.isBlank()) {
        invitedId =
            auth.signInWithEmailAndPassword("invited@test.com", "123456").await().user?.uid ?: ""
      }
      if (otherId.isBlank()) {
        otherId =
            auth.signInWithEmailAndPassword("other@test.com", "123456").await().user?.uid ?: ""
      }

      // Validate that all UIDs were successfully obtained
      val missing = mutableListOf<String>()
      if (ownerId.isBlank()) missing.add("owner")
      if (participantId.isBlank()) missing.add("participant")
      if (invitedId.isBlank()) missing.add("invited")
      if (otherId.isBlank()) missing.add("other")

      if (missing.isNotEmpty()) {
        throw IllegalStateException(
            "Failed to create test users in setUp. Missing UIDs for: ${missing.joinToString(", ")}. " +
                "This causes 'Invalid document reference' errors with paths like 'events/{id}/invitations' (3 segments).")
      }

      auth.signOut()
    }
  }

  @After
  override fun tearDown() {
    if (this::auth.isInitialized) {
      runBlocking { auth.signOut() }
    }
    super.tearDown()
  }

  private fun signIn(email: String) {
    runBlocking { auth.signInWithEmailAndPassword("$email@test.com", "123456").await() }
  }

  private fun getCurrentUserId(): String =
      auth.currentUser?.uid ?: throw IllegalStateException("No user is signed in.")

  // --- UIDs ---
  @Test
  fun getNewUid_returnsUniqueIds() {
    val id1 = repository.getNewUid()
    val id2 = repository.getNewUid()
    Assert.assertNotEquals(id1, id2)
    Assert.assertTrue(id1.isNotBlank())
  }

  // --- Add / Get Events ---
  @Test
  fun addAndGetPublicEvent_withAllFields() {
    runBlocking {
      signIn("owner")
      val currentUid = getCurrentUserId()
      val event =
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = currentUid,
              title = "Concert",
              subtitle = "Epic!",
              description = "desc",
              imageUrl = "http://img.com/pic.png",
              location = Location(46.5, 6.6, "EPFL"),
              start = now,
              end = now,
              maxCapacity = 50u,
              participationFee = 10u,
              isFlash = true,
              tags = listOf("music", "fun"),
              website = "https://concert.example.com")

      repository.addEvent(event)
      val loaded = repository.getEvent(event.uid) as Event.Public

      Assert.assertEquals(event.uid, loaded.uid)
      Assert.assertEquals("Concert", loaded.title)
    }
  }

  @Test
  fun addAndGetPrivateEvent_asOwner() {
    runBlocking {
      signIn("owner")
      val event =
          Event.Private(
              uid = repository.getNewUid(),
              ownerId = getCurrentUserId(),
              title = "Party",
              description = "secret",
              start = now,
              isFlash = false)
      repository.addEvent(event)
      val loaded = repository.getEvent(event.uid) as Event.Private
      Assert.assertEquals("Party", loaded.title)
    }
  }

  @Test
  fun getPrivateEvent_asOtherUser_throws() {
    assertThrows(FirebaseFirestoreException::class.java) {
      runBlocking {
        signIn("owner")
        val currentOwnerId = getCurrentUserId()
        val event =
            Event.Private(
                uid = repository.getNewUid(),
                ownerId = currentOwnerId,
                title = "Top Secret",
                description = "desc",
                start = now,
                isFlash = false)
        repository.addEvent(event)

        signIn("other")
        repository.getEvent(event.uid) // Denied by Firestore rules
      }
    }
  }

  @Test
  fun getAllEvents_returnsMultiple() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val e1 =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Party",
              "secret",
              start = now,
              isFlash = false)
      val e2 =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Hackathon",
              "fun",
              "Hack all day!",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e1)
      repository.addEvent(e2)

      val events = repository.getAllVisibleEvents()
      Assert.assertEquals(2, events.size)
    }
  }

  @Test(expected = FirebaseFirestoreException::class)
  fun getEvent_nonExistent_throws() {
    runBlocking {
      signIn("other")
      repository.getEvent("does-not-exist")
    }
  }

  // --- Edit Events ---
  @Test(expected = IllegalArgumentException::class)
  fun editEvent_withMismatchedUid_throws() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val e =
          Event.Private(
              repository.getNewUid(), currentOwnerId, "Title", "d", start = now, isFlash = false)
      repository.addEvent(e)
      repository.editEvent("different", e)
    }
  }

  @Test
  fun editEvent_byOwner_updates() {
    runBlocking {
      signIn("owner")
      val e =
          Event.Private(
              repository.getNewUid(),
              getCurrentUserId(),
              "Draft",
              "d",
              start = now,
              isFlash = false)
      repository.addEvent(e)
      val updated = e.copy(title = "Updated")
      repository.editEvent(e.uid, updated)
      val loaded = repository.getEvent(e.uid)
      Assert.assertEquals("Updated", loaded.title)
    }
  }

  @Test
  fun editEvent_byNonOwner_throws() {
    assertThrows(IllegalAccessException::class.java) {
      runBlocking {
        signIn("owner")
        val currentOwnerId = getCurrentUserId()
        val e =
            Event.Private(
                repository.getNewUid(), currentOwnerId, "Draft", "d", start = now, isFlash = false)
        repository.addEvent(e)

        signIn("other")
        val updated = e.copy(title = "Updated")
        repository.editEvent(e.uid, updated)
      }
    }
  }

  // --- Delete Events ---
  @Test
  fun deleteEvent_byOwner_removes() {
    runBlocking {
      signIn("owner")
      val e =
          Event.Private(
              repository.getNewUid(), getCurrentUserId(), "Temp", "d", start = now, isFlash = false)
      repository.addEvent(e)
      repository.deleteEvent(e.uid)

      assertThrows(FirebaseFirestoreException::class.java) {
        runBlocking { repository.getEvent(e.uid) }
      }
    }
  }

  @Test(expected = IllegalAccessException::class)
  fun deleteEvent_nonExistent_throws() {
    runBlocking {
      signIn("owner")
      repository.deleteEvent("fake-id")
    }
  }

  @Test
  fun deleteEvent_byNonOwner_throws() {
    assertThrows(IllegalAccessException::class.java) {
      runBlocking {
        signIn("owner")
        val currentOwnerId = getCurrentUserId()
        val e =
            Event.Private(
                repository.getNewUid(), currentOwnerId, "Temp", "d", isFlash = false, start = now)
        repository.addEvent(e)

        signIn("other")
        repository.deleteEvent(e.uid)
      }
    }
  }

  // --- Participants ---
  @Test
  fun addParticipant_thenGetParticipants() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val e =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Concert",
              "Epic!",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)

      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      val p = EventParticipant(currentParticipantId, now)
      repository.addParticipantToEvent(e.uid, p)

      val participants = repository.getEventParticipants(e.uid)
      Assert.assertEquals(1, participants.size)
    }
  }

  @Test
  fun addParticipantTwice_throws() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val e =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Concert",
              "Epic!",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)

      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      val p = EventParticipant(currentParticipantId, now)
      repository.addParticipantToEvent(e.uid, p)

      assertThrows(IllegalStateException::class.java) {
        runBlocking { repository.addParticipantToEvent(e.uid, p) } // second time ÔåÆ throws
      }
    }
  }

  @Test(expected = FirebaseFirestoreException::class)
  fun addParticipant_nonExistentEvent_throws() {
    runBlocking {
      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      repository.addParticipantToEvent("nope", EventParticipant(currentParticipantId, now))
    }
  }

  @Test
  fun getParticipants_whenNone_returnsEmpty() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val e =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Empty",
              "Nothing",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)
      signIn("other")
      Assert.assertTrue(repository.getEventParticipants(e.uid).isEmpty())
    }
  }

  @Test(expected = IllegalAccessException::class)
  fun removeParticipant_byOtherUser_throws() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val eventId = repository.getNewUid()
      val e =
          Event.Public(
              eventId,
              currentOwnerId,
              "Concert",
              "",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)

      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      val p = EventParticipant(currentParticipantId, now)
      repository.addParticipantToEvent(eventId, p)

      signIn("other")
      repository.removeParticipantFromEvent(eventId, currentParticipantId)
    }
  }

  // --- Invitations ---
  @Test(expected = FirebaseFirestoreException::class)
  fun addInvitation_toNonExistentEvent_throws() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      repository.addInvitationToEvent("fake-event", invitedId, currentOwnerId)
    }
  }

  // --- Event typing ---
  @Test(expected = IllegalArgumentException::class)
  fun eventFromDocument_withUnknownType_throws() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val badUid = repository.getNewUid()
      FirebaseEmulator.firestore
          .collection("events")
          .document(badUid)
          .set(
              mapOf(
                  "type" to "nonsense",
                  "ownerId" to currentOwnerId,
                  "title" to "any",
                  "description" to "any",
                  "start" to now,
                  "isFlash" to false))
          .await()

      repository.getEvent(badUid) // should throw
    }
  }

  // --- Event filtering ---
  @Test
  fun getAllVisibleEventsSatisfying_withPredicate_matchingSubset() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val e1 =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Party",
              "s",
              start = now,
              isFlash = false)
      val e2 =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Public Concert",
              "fun",
              "Exciting!",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e1)
      repository.addEvent(e2)

      val results = repository.getAllVisibleEventsSatisfying { it is Event.Public }

      Assert.assertEquals(1, results.size)
      Assert.assertEquals("Public Concert", results.first().title)
    }
  }

  // --- Additional Coverage Tests --
  @Test
  fun getPrivateEvent_asParticipant_succeeds() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val privateEvent =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "VIP Party",
              "members only",
              start = now,
              isFlash = false)
      repository.addEvent(privateEvent)

      // Owner invites participant
      repository.addInvitationToEvent(privateEvent.uid, participantId, currentOwnerId)
      userRepository.addInvitationToUser(privateEvent.uid, participantId, currentOwnerId)

      // Sign in as participant and accept invitation
      signIn("participant")
      val currentParticipantId = getCurrentUserId()

      // Accept the invitation - this will add the participant to the event
      userRepository.acceptInvitation(privateEvent.uid, currentParticipantId)

      // Participant should be able to get the private event (after accepting invitation)
      val loaded = repository.getEvent(privateEvent.uid) as Event.Private
      Assert.assertEquals("VIP Party", loaded.title)
    }
  }

  @Test
  fun addInvitation_byOwner_succeeds() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val event =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(event)

      repository.addInvitationToEvent(event.uid, invitedId, currentOwnerId)

      // Verify invitation was added by checking Firestore directly
      val invitationDoc =
          FirebaseEmulator.firestore
              .collection("events")
              .document(event.uid)
              .collection("invitations")
              .document(invitedId)
              .get()
              .await()

      Assert.assertTrue(invitationDoc.exists())
    }
  }

  @Test
  fun addInvitation_byNonOwner_throws() {
    assertThrows(IllegalAccessException::class.java) {
      runBlocking {
        signIn("owner")
        val currentOwnerId = getCurrentUserId()
        val event =
            Event.Public(
                repository.getNewUid(),
                currentOwnerId,
                "Event",
                "Sub",
                "desc",
                null,
                now,
                isFlash = false,
                subtitle = "")
        repository.addEvent(event)

        signIn("other")
        val currentOtherId = getCurrentUserId()
        repository.addInvitationToEvent(event.uid, invitedId, currentOtherId)
      }
    }
  }

  @Test
  fun removeParticipant_bySelf_succeeds() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val event =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(event)

      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      repository.addParticipantToEvent(event.uid, EventParticipant(currentParticipantId, now))

      // Verify participant was added
      var participants = repository.getEventParticipants(event.uid)
      Assert.assertEquals(1, participants.size)

      // Remove self
      repository.removeParticipantFromEvent(event.uid, currentParticipantId)

      // Verify participant was removed
      participants = repository.getEventParticipants(event.uid)
      Assert.assertEquals(0, participants.size)
    }
  }

  @Test
  fun addEvent_withAllOptionalFields_succeeds() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val event =
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = currentOwnerId,
              title = "Full Event",
              subtitle = "Complete",
              description = "All fields present",
              imageUrl = "https://example.com/image.png",
              location = Location(46.5191, 6.5668, "EPFL"),
              start = now,
              end = Timestamp(Date(now.seconds * 1000 + 3600000)),
              maxCapacity = 100u,
              participationFee = 50u,
              isFlash = true,
              tags = listOf("tech", "networking", "fun"),
              website = "https://example.com")

      repository.addEvent(event)
      val loaded = repository.getEvent(event.uid) as Event.Public

      Assert.assertEquals("Full Event", loaded.title)
      Assert.assertEquals("https://example.com/image.png", loaded.imageUrl)
      Assert.assertNotNull(loaded.location)
      Assert.assertEquals("EPFL", loaded.location?.name)
      Assert.assertNotNull(loaded.end)
      Assert.assertEquals(100u.toLong(), loaded.maxCapacity?.toLong())
      Assert.assertEquals(50u.toLong(), loaded.participationFee?.toLong())
      Assert.assertTrue(loaded.isFlash)
      Assert.assertEquals(3, loaded.tags.size)
      Assert.assertEquals("https://example.com", loaded.website)
    }
  }

  @Test
  fun addEvent_withMinimalFields_succeeds() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val event =
          Event.Private(
              uid = repository.getNewUid(),
              ownerId = currentOwnerId,
              title = "Minimal Event",
              description = "Only required fields",
              start = now,
              isFlash = false)

      repository.addEvent(event)
      val loaded = repository.getEvent(event.uid) as Event.Private

      Assert.assertEquals("Minimal Event", loaded.title)
      Assert.assertNull(loaded.imageUrl)
      Assert.assertNull(loaded.location)
      Assert.assertNull(loaded.end)
      Assert.assertNull(loaded.maxCapacity)
      Assert.assertNull(loaded.participationFee)
      Assert.assertFalse(loaded.isFlash)
    }
  }

  @Test
  fun getAllVisibleEvents_withMalformedEvent_skipsIt() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      // Add a valid event
      val validEvent =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Valid",
              "Good",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(validEvent)

      // Add a malformed event directly to Firestore (missing required field)
      val malformedUid = repository.getNewUid()
      FirebaseEmulator.firestore
          .collection("events")
          .document(malformedUid)
          .set(
              mapOf(
                  "type" to "public", "ownerId" to currentOwnerId, "title" to "Broken"
                  // Missing description, start, etc.
                  ))
          .await()

      // getAllVisibleEvents should skip the malformed event and return only valid ones
      val events = repository.getAllVisibleEvents()
      Assert.assertTrue(events.any { it.uid == validEvent.uid })
      Assert.assertFalse(events.any { it.uid == malformedUid })
    }
  }

  // --- Tests for 100% coverage of lines 200-241 ---

  @Test
  fun getAllVisibleEvents_withOnlyPublicEvents_returnsAll() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      val event1 =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Event 1",
              "Sub1",
              "desc1",
              null,
              now,
              isFlash = false,
              subtitle = "")
      val event2 =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Event 2",
              "Sub2",
              "desc2",
              null,
              now,
              isFlash = false,
              subtitle = "")

      repository.addEvent(event1)
      repository.addEvent(event2)

      val events = repository.getAllVisibleEvents()
      Assert.assertEquals(2, events.size)
      Assert.assertTrue(events.all { it is Event.Public })
    }
  }

  @Test
  fun getAllVisibleEvents_asOwner_seesOwnPrivateEvents() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      val privateEvent =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "My Private Event",
              "secret",
              start = now,
              isFlash = false)
      val publicEvent =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "My Public Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")

      repository.addEvent(privateEvent)
      repository.addEvent(publicEvent)

      val events = repository.getAllVisibleEvents()
      Assert.assertEquals(2, events.size)
      Assert.assertTrue(events.any { it.uid == privateEvent.uid && it is Event.Private })
      Assert.assertTrue(events.any { it.uid == publicEvent.uid && it is Event.Public })
    }
  }

  @Test
  fun getAllVisibleEvents_asNonParticipant_doesNotSeeOthersPrivateEvents() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      val privateEvent =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Owner Private",
              "secret",
              start = now,
              isFlash = false)
      val publicEvent =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Public Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")

      repository.addEvent(privateEvent)
      repository.addEvent(publicEvent)

      // Switch to other user (not participant)
      signIn("other")
      val events = repository.getAllVisibleEvents()

      // Should only see the public event
      Assert.assertEquals(1, events.size)
      Assert.assertEquals("Public Event", events.first().title)
      Assert.assertTrue(events.first() is Event.Public)
    }
  }

  @Test
  fun getAllVisibleEvents_withFirebaseExceptionDuringParsing_skipsCorruptedEvent() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      // Add a valid public event
      val validEvent =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Valid Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(validEvent)

      // Add a corrupted private event with invalid data
      val corruptedUid = repository.getNewUid()
      FirebaseEmulator.firestore
          .collection("events")
          .document(corruptedUid)
          .set(
              mapOf(
                  "type" to "private", "ownerId" to currentOwnerId
                  // Missing required fields like title, description, start
                  ))
          .await()

      // Should only return the valid event, skipping the corrupted one
      val events = repository.getAllVisibleEvents()
      Assert.assertTrue(events.any { it.uid == validEvent.uid })
      Assert.assertFalse(events.any { it.uid == corruptedUid })
    }
  }

  @Test
  fun getAllVisibleEvents_asParticipant_seesPrivateEventTheyJoined() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      // Owner creates a private event
      val privateEvent =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Exclusive Party",
              "Members only",
              start = now,
              isFlash = false)
      repository.addEvent(privateEvent)

      // Owner invites participant
      repository.addInvitationToEvent(privateEvent.uid, participantId, currentOwnerId)
      userRepository.addInvitationToUser(privateEvent.uid, participantId, currentOwnerId)

      // Sign in as participant and accept invitation
      signIn("participant")
      val currentParticipantId = getCurrentUserId()

      // Accept the invitation
      userRepository.acceptInvitation(privateEvent.uid, currentParticipantId)
      // Add participant to event (as done in production code)
      repository.addParticipantToEvent(
          privateEvent.uid, EventParticipant(currentParticipantId, now))

      // Participant should now see the private event in getAllVisibleEvents
      val events = repository.getAllVisibleEvents()
      Assert.assertTrue(events.any { it.uid == privateEvent.uid && it is Event.Private })

      val loadedEvent = events.first { it.uid == privateEvent.uid } as Event.Private
      Assert.assertEquals("Exclusive Party", loadedEvent.title)
    }
  }

  @Test
  fun getAllVisibleEvents_asParticipant_seesMultiplePrivateEventsTheyJoined() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      // Owner creates multiple private events
      val privateEvent1 =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Event 1",
              "desc1",
              start = now,
              isFlash = false)
      val privateEvent2 =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Event 2",
              "desc2",
              start = now,
              isFlash = false)
      val privateEvent3 =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Event 3",
              "desc3",
              start = now,
              isFlash = false)

      repository.addEvent(privateEvent1)
      repository.addEvent(privateEvent2)
      repository.addEvent(privateEvent3)

      // Owner invites participant to events 1 and 2
      repository.addInvitationToEvent(privateEvent1.uid, participantId, currentOwnerId)
      userRepository.addInvitationToUser(privateEvent1.uid, participantId, currentOwnerId)

      repository.addInvitationToEvent(privateEvent2.uid, participantId, currentOwnerId)
      userRepository.addInvitationToUser(privateEvent2.uid, participantId, currentOwnerId)

      // Sign in as participant and accept invitations for events 1 and 2
      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      userRepository.acceptInvitation(privateEvent1.uid, currentParticipantId)
      repository.addParticipantToEvent(
          privateEvent1.uid, EventParticipant(currentParticipantId, now))

      userRepository.acceptInvitation(privateEvent2.uid, currentParticipantId)
      repository.addParticipantToEvent(
          privateEvent2.uid, EventParticipant(currentParticipantId, now))

      // Participant should see only the events they accepted (events 1 and 2, not 3)
      val events = repository.getAllVisibleEvents()
      val privateEventUids = events.filter { it is Event.Private }.map { it.uid }

      Assert.assertTrue(privateEventUids.contains(privateEvent1.uid))
      Assert.assertTrue(privateEventUids.contains(privateEvent2.uid))
      Assert.assertFalse(privateEventUids.contains(privateEvent3.uid))
    }
  }

  @Test
  fun getAllVisibleEvents_mixOfPublicAndPrivateEvents_filtersCorrectly() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      // Owner creates public and private events
      val publicEvent =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Public Concert",
              "Sub",
              "Everyone welcome",
              null,
              now,
              isFlash = false,
              subtitle = "")

      val privateEventJoined =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Party - Joined",
              "VIP only",
              start = now,
              isFlash = false)

      val privateEventNotJoined =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Party - Not Joined",
              "Super VIP",
              start = now,
              isFlash = false)

      repository.addEvent(publicEvent)
      repository.addEvent(privateEventJoined)
      repository.addEvent(privateEventNotJoined)

      // Owner invites participant to only one private event
      repository.addInvitationToEvent(privateEventJoined.uid, participantId, currentOwnerId)
      userRepository.addInvitationToUser(privateEventJoined.uid, participantId, currentOwnerId)

      // Sign in as participant and accept invitation
      signIn("participant")
      val currentParticipantId = getCurrentUserId()
      userRepository.acceptInvitation(privateEventJoined.uid, currentParticipantId)
      repository.addParticipantToEvent(
          privateEventJoined.uid, EventParticipant(currentParticipantId, now))

      // Participant should see: public event + private event they joined
      val events = repository.getAllVisibleEvents()
      val eventUids = events.map { it.uid }

      Assert.assertEquals(2, events.size)
      Assert.assertTrue(eventUids.contains(publicEvent.uid))
      Assert.assertTrue(eventUids.contains(privateEventJoined.uid))
      Assert.assertFalse(eventUids.contains(privateEventNotJoined.uid))
    }
  }

  @Test
  fun getAllVisibleEvents_asInvitedUser_doesNotSeePrivateEventBeforeAccepting() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      // Owner creates a private event
      val privateEvent =
          Event.Private(
              repository.getNewUid(),
              currentOwnerId,
              "Private Party",
              "VIP only",
              start = now,
              isFlash = false)
      repository.addEvent(privateEvent)

      // Owner invites participant but they haven't accepted yet
      repository.addInvitationToEvent(privateEvent.uid, participantId, currentOwnerId)
      userRepository.addInvitationToUser(privateEvent.uid, participantId, currentOwnerId)

      // Sign in as participant (without accepting)
      signIn("participant")

      // Participant should NOT see the private event until they accept
      val events = repository.getAllVisibleEvents()
      val eventUids = events.map { it.uid }

      Assert.assertFalse(eventUids.contains(privateEvent.uid))
    }
  }

  // Tests for getEventsByOrganization
  @Test
  fun getEventsByOrganization_returnsEventsOwnedByOrganization() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val orgId = "org123"

      // Create the organization first
      FirebaseEmulator.firestore
          .collection("organizations")
          .document(orgId)
          .set(
              mapOf(
                  "id" to orgId,
                  "name" to "Test Organization",
                  "type" to "Association",
                  "createdBy" to currentOwnerId,
                  "memberUids" to listOf(currentOwnerId)))
          .await()

      val event1 =
          Event.Public(
              repository.getNewUid(),
              orgId,
              "Org Event 1",
              "Sub1",
              "desc1",
              null,
              now,
              isFlash = false,
              subtitle = "")
      val event2 =
          Event.Public(
              repository.getNewUid(),
              orgId,
              "Org Event 2",
              "Sub2",
              "desc2",
              null,
              now,
              isFlash = false,
              subtitle = "")

      // Create another organization for the "other" event
      val otherOrgId = "other_org"
      signIn("other")
      val otherOwnerId = getCurrentUserId()
      FirebaseEmulator.firestore
          .collection("organizations")
          .document(otherOrgId)
          .set(
              mapOf(
                  "id" to otherOrgId,
                  "name" to "Other Organization",
                  "type" to "Association",
                  "createdBy" to otherOwnerId,
                  "memberUids" to listOf(otherOwnerId)))
          .await()

      val otherEvent =
          Event.Public(
              repository.getNewUid(),
              otherOrgId,
              "Other Org Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")

      repository.addEvent(otherEvent)

      signIn("owner")
      repository.addEvent(event1)
      repository.addEvent(event2)

      val orgEvents = repository.getEventsByOrganization(orgId)

      Assert.assertEquals(2, orgEvents.size)
      Assert.assertTrue(orgEvents.any { it.uid == event1.uid })
      Assert.assertTrue(orgEvents.any { it.uid == event2.uid })
      Assert.assertFalse(orgEvents.any { it.uid == otherEvent.uid })
    }
  }

  @Test
  fun getEventsByOrganization_returnsEmptyListWhenNoEvents() {
    runBlocking {
      signIn("owner")
      val orgId = "org_without_events"

      val events = repository.getEventsByOrganization(orgId)

      Assert.assertTrue(events.isEmpty())
    }
  }

  @Test
  fun getEventsByOrganization_returnsPublicAndPrivateEvents() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val orgId = currentOwnerId // Using user as organization owner

      val publicEvent =
          Event.Public(
              repository.getNewUid(),
              orgId,
              "Public Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      val privateEvent =
          Event.Private(
              repository.getNewUid(), orgId, "Private Event", "desc", start = now, isFlash = false)

      repository.addEvent(publicEvent)
      repository.addEvent(privateEvent)

      val orgEvents = repository.getEventsByOrganization(orgId)

      Assert.assertEquals(2, orgEvents.size)
      Assert.assertTrue(orgEvents.any { it is Event.Public && it.uid == publicEvent.uid })
      Assert.assertTrue(orgEvents.any { it is Event.Private && it.uid == privateEvent.uid })
    }
  }

  @Test
  fun getEventsByOrganization_handlesMalformedEvents() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val orgId = currentOwnerId

      // Add a valid event
      val validEvent =
          Event.Public(
              repository.getNewUid(),
              orgId,
              "Valid Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(validEvent)

      // Add a malformed event directly to Firestore
      val malformedUid = repository.getNewUid()
      FirebaseEmulator.firestore
          .collection("events")
          .document(malformedUid)
          .set(
              mapOf(
                  "type" to "public", "ownerId" to orgId, "title" to "Broken"
                  // Missing required fields
                  ))
          .await()

      val orgEvents = repository.getEventsByOrganization(orgId)

      // Should only return the valid event, skipping the malformed one
      Assert.assertTrue(orgEvents.any { it.uid == validEvent.uid })
      Assert.assertFalse(orgEvents.any { it.uid == malformedUid })
    }
  }

  @Test
  fun getEventsByOrganization_filtersCorrectlyByOwnerId() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()

      signIn("other")
      val otherOwnerId = getCurrentUserId()

      signIn("owner")

      // Create events with different owners
      val event1 =
          Event.Public(
              repository.getNewUid(),
              currentOwnerId,
              "Owner Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")
      val event2 =
          Event.Public(
              repository.getNewUid(),
              otherOwnerId,
              "Other Event",
              "Sub",
              "desc",
              null,
              now,
              isFlash = false,
              subtitle = "")

      repository.addEvent(event1)

      signIn("other")
      repository.addEvent(event2)

      // Get events for first owner
      val ownerEvents = repository.getEventsByOrganization(currentOwnerId)
      Assert.assertEquals(1, ownerEvents.size)
      Assert.assertEquals(event1.uid, ownerEvents[0].uid)

      // Get events for other owner
      val otherEvents = repository.getEventsByOrganization(otherOwnerId)
      Assert.assertEquals(1, otherEvents.size)
      Assert.assertEquals(event2.uid, otherEvents[0].uid)
    }
  }

  @Test
  fun getEventsByOrganization_returnsEventsWithAllFields() {
    runBlocking {
      signIn("owner")
      val currentOwnerId = getCurrentUserId()
      val orgId = currentOwnerId

      val fullEvent =
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = orgId,
              title = "Full Event",
              subtitle = "Complete",
              description = "All fields present",
              imageUrl = "https://example.com/image.png",
              location = Location(46.5191, 6.5668, "EPFL"),
              start = now,
              end = Timestamp(Date(now.seconds * 1000 + 3600000)),
              maxCapacity = 100u,
              participationFee = 50u,
              isFlash = true,
              tags = listOf("tech", "networking"),
              website = "https://example.com")

      repository.addEvent(fullEvent)

      val orgEvents = repository.getEventsByOrganization(orgId)

      Assert.assertEquals(1, orgEvents.size)
      val retrievedEvent = orgEvents[0] as Event.Public

      Assert.assertEquals("Full Event", retrievedEvent.title)
      Assert.assertEquals("Complete", retrievedEvent.subtitle)
      Assert.assertEquals("All fields present", retrievedEvent.description)
      Assert.assertEquals("https://example.com/image.png", retrievedEvent.imageUrl)
      Assert.assertNotNull(retrievedEvent.location)
      Assert.assertEquals("EPFL", retrievedEvent.location?.name)
      Assert.assertNotNull(retrievedEvent.end)
      Assert.assertEquals(100u.toLong(), retrievedEvent.maxCapacity?.toLong())
      Assert.assertEquals(50u.toLong(), retrievedEvent.participationFee?.toLong())
      Assert.assertTrue(retrievedEvent.isFlash)
      Assert.assertEquals(2, retrievedEvent.tags.size)
      Assert.assertEquals("https://example.com", retrievedEvent.website)
    }
  }
}

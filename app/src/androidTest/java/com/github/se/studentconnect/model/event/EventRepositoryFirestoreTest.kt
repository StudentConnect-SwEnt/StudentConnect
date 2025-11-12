// Portions of this code were generated with the help of ChatGPT
package com.github.se.studentconnect.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.location.Location
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

  // --- Test User IDs - Ces variables vont stocker les vrais UIDs Firebase ---
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
          val result = auth.createUserWithEmailAndPassword("$email@test.com", password).await()
          // Stocker le vrai UID Firebase
          when (email) {
            "owner" -> ownerId = result.user?.uid ?: ""
            "participant" -> participantId = result.user?.uid ?: ""
            "invited" -> invitedId = result.user?.uid ?: ""
            "other" -> otherId = result.user?.uid ?: ""
          }
        } catch (e: FirebaseAuthUserCollisionException) {
          // L'utilisateur existe déjà, récupérer son UID
          val result = auth.signInWithEmailAndPassword("$email@test.com", password).await()
          when (email) {
            "owner" -> ownerId = result.user?.uid ?: ""
            "participant" -> participantId = result.user?.uid ?: ""
            "invited" -> invitedId = result.user?.uid ?: ""
            "other" -> otherId = result.user?.uid ?: ""
          }
        }
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
              ownerId = currentUid, // Utiliser le vrai UID
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
        runBlocking { repository.addParticipantToEvent(e.uid, p) } // second time → throws
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
}

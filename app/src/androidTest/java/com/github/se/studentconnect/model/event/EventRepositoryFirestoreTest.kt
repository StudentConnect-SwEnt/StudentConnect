// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import java.util.Date
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryFirestoreTest : FirestoreStudentConnectTest() {

  private val now = Timestamp(Date())
  private lateinit var auth: FirebaseAuth

  // --- Test User UIDs ---
  private val ownerId = "owner"
  private val participantId = "participant"
  private val invitedId = "invited"
  private val otherId = "other"

  @Before
  override fun setUp() {
    super.setUp()
    auth = FirebaseEmulator.auth
    // Create test users, ignoring errors if they already exist.
    runBlocking {
      val users =
          mapOf(
              ownerId to "123456",
              participantId to "123456",
              invitedId to "123456",
              otherId to "123456")
      for ((uid, password) in users) {
        try {
          auth.createUserWithEmailAndPassword("$uid@test.com", password).await()
        } catch (e: FirebaseAuthUserCollisionException) {
          // User already exists, which is fine for our test setup.
        }
      }
    }
  }

  @After
  override fun tearDown() {
    runBlocking { auth.signOut() }
    // Call the parent's tearDown to clear Firestore data.
    super.tearDown()
  }

  private fun signIn(uid: String) {
    runBlocking { auth.signInWithEmailAndPassword("$uid@test.com", "123456").await() }
  }

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
      val event =
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = ownerId,
              title = "Concert",
              subtitle = "Epic!",
              description = "desc",
              imageUrl = "http://img.com/pic.png",
              location = Location(46.5, 6.6, "EPFL"),
              start = now,
              end = now,
              maxCapacity = 50u,
              participationFee = 10u,
              isFlash = true, // Keeping true here as in original valid test
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
      signIn(ownerId)
      val event =
          Event.Private(
              uid = repository.getNewUid(),
              ownerId = Firebase.auth.currentUser?.uid!!,
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
  fun getAllEvents_returnsMultiple() {
    runBlocking {
      val e1 =
          Event.Private(
              repository.getNewUid(), ownerId, "Party", "secret", start = now, isFlash = false)
      val e2 =
          Event.Public(
              repository.getNewUid(),
              ownerId,
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

  @Test(expected = IllegalArgumentException::class)
  fun getEvent_nonExistent_throws() {
    runBlocking {
      signIn(otherId)
      repository.getEvent("does-not-exist")
    }
  }

  // --- Edit Events ---
  @Test(expected = IllegalArgumentException::class)
  fun editEvent_withMismatchedUid_throws() {
    runBlocking {
      signIn(ownerId)
      val e =
          Event.Private(repository.getNewUid(), ownerId, "Title", "d", start = now, isFlash = false)
      repository.addEvent(e)
      repository.editEvent("different", e)
    }
  }

  @Test
  fun editEvent_byOwner_updates() {
    runBlocking {
      signIn(ownerId)
      val e =
          Event.Private(
              repository.getNewUid(),
              Firebase.auth.currentUser?.uid!!,
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

  @Test(expected = IllegalAccessException::class)
  fun editEvent_byNonOwner_throws() {
    runBlocking {
      val e =
          Event.Private(repository.getNewUid(), ownerId, "Draft", "d", start = now, isFlash = false)
      repository.addEvent(e)

      signIn(otherId)
      val updated = e.copy(title = "Updated")
      repository.editEvent(e.uid, updated) // Should throw
    }
  }

  // --- Delete Events ---
  @Test
  fun deleteEvent_byOwner_removes() {
    runBlocking {
      signIn(ownerId)
      val e =
          Event.Private(
              repository.getNewUid(),
              Firebase.auth.currentUser?.uid!!,
              "Temp",
              "d",
              start = now,
              isFlash = false)
      repository.addEvent(e)
      repository.deleteEvent(e.uid)
      Assert.assertTrue(repository.getAllVisibleEvents().isEmpty())
    }
  }

  @Test(expected = IllegalArgumentException::class)
  fun deleteEvent_nonExistent_throws() {
    runBlocking {
      signIn(ownerId)
      repository.deleteEvent("fake-id")
    }
  }

  @Test(expected = IllegalAccessException::class)
  fun deleteEvent_byNonOwner_throws() {
    runBlocking {
      val e =
          Event.Private(
              repository.getNewUid(),
              Firebase.auth.currentUser?.uid!!,
              "Temp",
              "d",
              isFlash = false,
              start = now)
      repository.addEvent(e)
      signIn(otherId)
      repository.deleteEvent(e.uid) // Should throw
    }
  }

  // --- Participants ---
  @Test
  fun addParticipant_thenGetParticipants() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Concert",
              "Epic!",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)
      val p = EventParticipant(participantId, now)
      repository.addParticipantToEvent(e.uid, p)
      val participants = repository.getEventParticipants(e.uid)
      Assert.assertEquals(1, participants.size)
    }
  }

  @Test(expected = IllegalStateException::class)
  fun addParticipantTwice_throws() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Concert",
              "Epic!",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)
      val p = EventParticipant(participantId, now)
      repository.addParticipantToEvent(e.uid, p)
      repository.addParticipantToEvent(e.uid, p) // second time → throws
    }
  }

  @Test(expected = IllegalArgumentException::class)
  fun addParticipant_nonExistentEvent_throws() {
    runBlocking { repository.addParticipantToEvent("nope", EventParticipant(participantId, now)) }
  }

  @Test
  fun getParticipants_whenNone_returnsEmpty() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Empty",
              "Nothing",
              "d",
              null,
              now,
              isFlash = false,
              subtitle = "")
      repository.addEvent(e)
      Assert.assertTrue(repository.getEventParticipants(e.uid).isEmpty())
    }
  }

  @Test(expected = IllegalAccessException::class)
  fun removeParticipant_byOtherUser_throws() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              ownerId,
              "Concert",
              "Epic!",
              "d",
              null,
              now,
              subtitle = "",
              isFlash = false)
      repository.addEvent(e)
      val p = EventParticipant(participantId, now)
      repository.addParticipantToEvent(e.uid, p)

      signIn(otherId) // Sign in as a different user
      repository.removeParticipantFromEvent(e.uid, participantId) // Should throw
    }
  }

  // --- Invitations ---
  @Test(expected = IllegalAccessException::class)
  fun addInvitation_byNonOwner_throws() {
    runBlocking {
      val e =
          Event.Private(
              repository.getNewUid(), ownerId, "Private Party", "d", start = now, isFlash = false)
      repository.addEvent(e)

      signIn(otherId) // Sign in as non-owner
      repository.addInvitationToEvent(e.uid, invitedId, otherId) // Should throw
    }
  }

  @Test(expected = IllegalArgumentException::class)
  fun addInvitation_toNonExistentEvent_throws() {
    runBlocking {
      signIn(ownerId)
      repository.addInvitationToEvent("fake-event", invitedId, ownerId)
    }
  }

  // --- Event typing ---
  @Test(expected = IllegalArgumentException::class)
  fun eventFromDocument_withUnknownType_throws() {
    runBlocking {
      val badUid = repository.getNewUid()
      FirebaseEmulator.firestore
          .collection("events")
          .document(badUid)
          .set(
              mapOf(
                  "type" to "nonsense",
                  "ownerId" to "any",
                  "title" to "any",
                  "description" to "any",
                  "start" to now,
                  "isFlash" to false))
          .await()

      signIn(ownerId)
      repository.getEvent(badUid) // should throw
    }
  }

  // --- Event filtering ---
  @Test
  fun getAllVisibleEventsSatisfying_withPredicate_matchingSubset() {
    runBlocking {
      val e1 =
          Event.Private(
              repository.getNewUid(), ownerId, "Private Party", "s", start = now, isFlash = false)
      val e2 =
          Event.Public(
              repository.getNewUid(),
              ownerId,
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
}

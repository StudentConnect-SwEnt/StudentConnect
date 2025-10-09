// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.model.event

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.utils.FirebaseEmulator
import com.github.se.studentconnect.utils.FirestoreStudentConnectTest
import com.google.firebase.Timestamp
import java.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRepositoryFirestoreTest : FirestoreStudentConnectTest() {

  private val now = Timestamp(Date())

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
              ownerId = "owner1",
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
      Assert.assertEquals("Epic!", loaded.subtitle)
      Assert.assertEquals("http://img.com/pic.png", loaded.imageUrl)
      Assert.assertEquals("EPFL", loaded.location?.name)
      Assert.assertEquals(50u, loaded.maxCapacity!!)
      Assert.assertEquals(10u, loaded.participationFee!!)
      Assert.assertEquals(listOf("music", "fun"), loaded.tags)
      Assert.assertEquals("https://concert.example.com", loaded.website)
      Assert.assertTrue(loaded.isFlash)
    }
  }

  @Test
  fun addAndGetPrivateEvent() {
    runBlocking {
      val event =
          Event.Private(
              uid = repository.getNewUid(),
              ownerId = "o",
              title = "Party",
              description = "secret",
              imageUrl = null,
              location = null,
              start = now,
              end = null,
              maxCapacity = null,
              participationFee = null,
              isFlash = false)
      repository.addEvent(event)
      val loaded = repository.getEvent(event.uid) as Event.Private
      Assert.assertEquals("Party", loaded.title)
      Assert.assertNull(loaded.imageUrl)
      Assert.assertNull(loaded.location)
    }
  }

  @Test
  fun getAllEvents_whenEmpty_returnsEmpty() {
    runBlocking { Assert.assertTrue(repository.getAllVisibleEvents().isEmpty()) }
  }

  @Test
  fun getAllEvents_returnsMultiple() {
    runBlocking {
      val e1 =
          Event.Private(
              repository.getNewUid(),
              "o",
              "Party",
              "secret",
              null,
              null,
              now,
              null,
              null,
              null,
              false)
      val e2 =
          Event.Public(
              repository.getNewUid(),
              "o2",
              "Hackathon",
              "fun",
              null,
              null,
              now,
              null,
              null,
              null,
              false,
              "Hack all day!",
              listOf("tech"))
      repository.addEvent(e1)
      repository.addEvent(e2)
      val events = repository.getAllVisibleEvents()
      Assert.assertEquals(2, events.size)
    }
  }

  @Test(expected = Exception::class)
  fun getEvent_nonExistent_throws() {
    runBlocking { repository.getEvent("does-not-exist") }
  }

  // --- Edit Events ---
  @Test(expected = IllegalArgumentException::class)
  fun editEvent_withMismatchedUid_throws() {
    runBlocking {
      val e =
          Event.Private(
              repository.getNewUid(),
              "o",
              "Title",
              "desc",
              null,
              null,
              now,
              null,
              null,
              null,
              false)
      repository.addEvent(e)
      repository.editEvent("different", e)
    }
  }

  @Test
  fun editEvent_withValidUid_updates() {
    runBlocking {
      val e =
          Event.Private(
              repository.getNewUid(), "o", "Draft", "d", null, null, now, null, null, null, false)
      repository.addEvent(e)
      val updated = e.copy(title = "Updated")
      repository.editEvent(e.uid, updated)
      val loaded = repository.getEvent(e.uid)
      Assert.assertEquals("Updated", loaded.title)
    }
  }

  // --- Delete Events ---
  @Test
  fun deleteEvent_removes() {
    runBlocking {
      val e =
          Event.Private(
              repository.getNewUid(), "o", "Temp", "d", null, null, now, null, null, null, false)
      repository.addEvent(e)
      repository.deleteEvent(e.uid)
      Assert.assertTrue(repository.getAllVisibleEvents().isEmpty())
    }
  }

  @Test
  fun deleteEvent_nonExistent_noCrash() {
    runBlocking {
      repository.deleteEvent("fake-id")
      Assert.assertTrue(repository.getAllVisibleEvents().isEmpty())
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
              "d",
              null,
              null,
              now,
              null,
              null,
              null,
              false,
              "Epic!")
      repository.addEvent(e)
      val p = EventParticipant("user1", now)
      repository.addParticipantToEvent(e.uid, p)
      val participants = repository.getEventParticipants(e.uid)
      Assert.assertEquals(1, participants.size)
      Assert.assertEquals("user1", participants[0].uid)
    }
  }

  @Test(expected = Exception::class)
  fun addParticipantTwice_throws() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Concert",
              "d",
              null,
              null,
              now,
              null,
              null,
              null,
              false,
              "Epic!")
      repository.addEvent(e)
      val p = EventParticipant("user1", now)
      repository.addParticipantToEvent(e.uid, p)
      repository.addParticipantToEvent(e.uid, p) // second time → throws
    }
  }

  @Test
  fun addParticipant_nonExistentEvent_fails() {
    runBlocking {
      try {
        repository.addParticipantToEvent("nope", EventParticipant("u", now))
        Assert.fail("Expected failure")
      } catch (_: Exception) {
        // expected
      }
    }
  }

  @Test
  fun getParticipants_whenNone_returnsEmpty() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Empty",
              "d",
              null,
              null,
              now,
              null,
              null,
              null,
              false,
              "Nothing")
      repository.addEvent(e)
      Assert.assertTrue(repository.getEventParticipants(e.uid).isEmpty())
    }
  }

  @Test
  fun removeParticipant_removes() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Concert",
              "d",
              null,
              null,
              now,
              null,
              null,
              null,
              false,
              "Epic!")
      repository.addEvent(e)
      val p = EventParticipant("user1", now)
      repository.addParticipantToEvent(e.uid, p)
      repository.removeParticipantFromEvent(e.uid, "user1")
      Assert.assertTrue(repository.getEventParticipants(e.uid).isEmpty())
    }
  }

  @Test
  fun removeParticipant_notPresent_noCrash() {
    runBlocking {
      val e =
          Event.Public(
              repository.getNewUid(),
              "o",
              "Concert",
              "d",
              null,
              null,
              now,
              null,
              null,
              null,
              false,
              "Epic!")
      repository.addEvent(e)
      repository.removeParticipantFromEvent(e.uid, "not-there")
      Assert.assertTrue(repository.getEventParticipants(e.uid).isEmpty())
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
          .set(mapOf("type" to "nonsense"))
          .await()
      repository.getEvent(badUid) // should throw
    }
  }
}

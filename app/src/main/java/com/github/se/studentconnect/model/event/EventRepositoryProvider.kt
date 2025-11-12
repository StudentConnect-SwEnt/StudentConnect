// Fake events were created by Gemini
package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.runBlocking

/**
 * Provides instances of EventRepository. Allows switching between Firestore and a local, in-memory
 * repository for testing.
 *
 * The repository mode is controlled by `AuthenticationProvider.local`:
 * - local = true: Uses in-memory repository for testing
 * - local = false: Uses Firestore for production
 */
object EventRepositoryProvider {
  private val firestoreRepository: EventRepository = EventRepositoryFirestore(Firebase.firestore)
  private val localRepository: EventRepository = EventRepositoryLocal()

  private val ROLEX_LOCATION = Location(46.5186, 6.5681, "Rolex Learning Center")

  val fakeEvents: List<Event> = createFakeEvents()

  /** The currently active repository. Automatically syncs with AuthenticationProvider.local */
  var repository: EventRepository

  init {
    runBlocking { fakeEvents.forEach { event -> localRepository.addEvent(event) } }
    val useLocal = com.github.se.studentconnect.repository.AuthenticationProvider.local
    repository = if (useLocal) localRepository else firestoreRepository
  }

  fun createFakeEvents(): List<Event> {
    val LIVE_AT_EPFL = "Live at EPFL!"
    return listOf(
        // Event 1
        Event.Public(
            uid = "event-killer-concert-01",
            ownerId = "ownerId1",
            title = "The Killers Concert",
            description = "...",
            location = Location(46.5191, 2.33333, "somewhere"),
            start = date(300),
            isFlash = false,
            participationFee = 12u,
            subtitle = LIVE_AT_EPFL),
        // Event 2
        Event.Public(
            uid = "event-sql-workshop-02",
            ownerId = "user-charlie-03",
            title = "SQL Workshop",
            description = "...",
            location = Location(46.5204, 6.5654, "SwissTech Convention Center"),
            start = date(86400),
            isFlash = false,
            participationFee = 12u,
            subtitle = LIVE_AT_EPFL),
        // Event 3
        Event.Public(
            uid = "event-balelec-03",
            ownerId = "user-charlie-02",
            title = "Balélec Festival 2025",
            description = "...",
            location = Location(46.5191, 6.5668, "EPFL, Lausanne"),
            start = date(1_209_600),
            isFlash = false,
            subtitle = LIVE_AT_EPFL),
        // Event 4
        Event.Public(
            uid = "event-rlc-study-04",
            ownerId = "ownerId4",
            title = "RLC Study Jam",
            description = "...",
            location = ROLEX_LOCATION,
            start = date(604_800),
            isFlash = false,
            subtitle = LIVE_AT_EPFL),
        // Event 5
        Event.Public(
            uid = "event-to-join",
            ownerId = "ownerId4",
            title = "event-to-join",
            description = "...",
            location = ROLEX_LOCATION,
            start = date(8000),
            isFlash = false,
            subtitle = LIVE_AT_EPFL),
    )
  }

  fun date(seconds: Long): Timestamp =
      Timestamp(
          Date.from(
              LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()))
}

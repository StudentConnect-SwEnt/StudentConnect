// Fake events were created by Gemini
package com.github.se.studentconnect.model.event

import com.github.se.studentconnect.model.location.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.runBlocking

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository = EventRepositoryFirestore(Firebase.firestore)
  private val repository2: EventRepository = EventRepositoryLocal()

  var repository: EventRepository = repository2

  init {
    runBlocking {
      // Event 1
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId1",
              title = "The Killers Concert",
              description =
                  "Get ready for an epic night! The iconic The Killers are hitting the stage with all their energy, blasting out their legendary hits and fresh new tracks. Huge vibes, a crowd going wild, and sing-along anthems all night long.",
              imageUrl = null,
              location = Location(46.5191, 6.5668, "EPFL"),
              start = date(300),
              end = null,
              maxCapacity = 2000u,
              participationFee = 75u,
              isFlash = false,
              subtitle = "Live at the EPFL Campus",
              tags = listOf(),
              website = "https://www.balelec.ch/fr"))
      // Event 2
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId2",
              title = "SQL Workshop",
              description =
                  "Join our workshop to master SQL! This session is perfect for beginners and those looking to refresh their skills. We will cover everything from basic queries to more advanced topics. Laptops are required.",
              imageUrl = null,
              location = Location(46.5204, 6.5654, "SwissTech Convention Center"),
              start = date(86400), // 1 day from now
              end = null,
              maxCapacity = 50u,
              participationFee = 10u,
              isFlash = false,
              subtitle = "Data Science Student Association",
              tags = listOf(),
              website = "https://www.balelec.ch/fr"))

      // Event 3
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId3",
              title = "Balélec Festival 2025",
              subtitle = "Biggest Student Festival in Europe",
              description =
                  "Experience the legendary Balélec Festival at EPFL! Multiple stages, diverse music genres from electronic to rock, food trucks, and an unforgettable atmosphere. Don't miss out on the biggest student-run open-air festival in Europe.",
              location = Location(46.5191, 6.5668, "EPFL, Lausanne"),
              start = date(1_209_600),
              isFlash = false,
              tags = listOf(),
              website = "https://www.balelec.ch/fr"))

      // Event 4
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId4",
              title = "RLC Study Jam",
              subtitle = "Collaborative Study Session",
              description =
                  "Finals are coming! Join our collaborative study session at the Rolex Learning Center. Find study partners, share notes, and enjoy free coffee and snacks to keep you going. Let's ace those exams together!",
              location = Location(46.5186, 6.5681, "Rolex Learning Center"),
              start = date(604_800),
              maxCapacity = 150u,
              isFlash = false,
              tags = listOf(),
              website = null))

      // Event 5
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId5",
              title = "Startup Champions Seed Night",
              subtitle = "EPFL's Premier Pitching Event",
              description =
                  "Witness the next generation of innovators at the Startup Champions Seed Night. EPFL's most promising startups will pitch their ideas to a panel of investors and a live audience. Networking session to follow.",
              location = Location(46.5204, 6.5654, "SwissTech Convention Center"),
              start = date(2_592_000),
              isFlash = false,
              tags = listOf(),
              website = ""))

      // Event 6
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId6",
              title = "O'Week Beach Volleyball",
              subtitle = "Fun and Sun by the Lake",
              description =
                  "Kick off the new semester with some friendly beach volleyball action! Join us by the lake for a casual tournament. All skill levels are welcome. Music, BBQ, and good vibes guaranteed.",
              location = Location(46.5165, 6.5819, "Plage de Dorigny, Lausanne"),
              start = date(4_838_400),
              isFlash = false,
              tags = listOf(),
              website = null))

      // Event 7
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId7",
              title = "Movie Night Under the Stars",
              subtitle = "Open-Air Cinema",
              description =
                  "Bring a blanket and join us for an open-air movie screening on the EPFL campus. We'll be showing a classic blockbuster. Popcorn is on us! The movie will be announced via a poll on our social media.",
              location = Location(46.5195, 6.5670, "Esplanade, EPFL"),
              start = date(950_400),
              isFlash = false,
              tags = listOf(),
              website = null))

      // Event 8
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId8",
              title = "International Food Festival",
              subtitle = "A Taste of the World",
              description =
                  "Celebrate the cultural diversity at EPFL! Various student associations will be sharing delicious traditional food from their home countries. Come for the food, stay for the cultural performances and music.",
              location = Location(46.5196, 6.5654, "Place Cosandey, EPFL"),
              start = date(400),
              isFlash = false,
              tags = listOf(),
              website = null))

      // Event 9
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId9",
              title = "Robotics Club Demo Day",
              subtitle = "Meet the Machines",
              description =
                  "Come see what the Robotics Club has been building all semester! Live demonstrations of autonomous drones, line-following robots, and maybe even a robotic arm that can play chess. A great event for all tech enthusiasts.",
              location = Location(46.5212, 6.5658, "ME Building, EPFL"),
              start = date(3_110_400),
              isFlash = false,
              tags = listOf(),
              website = null))

      // Event 10
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId10",
              title = "SAT Sailing Regatta",
              subtitle = "Race on Lake Geneva",
              description =
                  "The annual SAT Sailing Regatta is here! Whether you're an experienced sailor or a curious beginner, come and enjoy a day of competitive sailing on the beautiful Lake Geneva. Boats and skippers provided for teams.",
              location = Location(46.5173, 6.5811, "Centre Nautique de Dorigny"),
              start = date(4_320_000),
              isFlash = false,
              tags = listOf(),
              website = null))

      // Event 11
      repository.addEvent(
          Event.Public(
              uid = repository.getNewUid(),
              ownerId = "ownerId11",
              title = "Board Games Night",
              subtitle = "Unplug and Play",
              description =
                  "Take a break from the screens and join us for a cozy board games night. We have a huge collection from Catan and Ticket to Ride to modern classics. A perfect way to relax and meet new people.",
              location = Location(46.5209, 6.5683, "CO Building, EPFL"),
              start = date(345_600),
              isFlash = false,
              tags = listOf(),
              website = null))
    }
  }

  private fun date(seconds: Long): Timestamp =
      Timestamp(
          Date.from(
              LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant()))
}

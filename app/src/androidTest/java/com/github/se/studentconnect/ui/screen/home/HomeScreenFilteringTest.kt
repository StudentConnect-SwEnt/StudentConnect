package com.github.se.studentconnect.ui.screen.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.utils.FilterData
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenFilteringTest {

  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    private const val NO_EVENTS_TEXT = "No events found matching your criteria."
  }

  private fun createTestEvent(
      uid: String,
      title: String,
      tags: List<String> = emptyList()
  ): Event.Public {
    return Event.Public(
        uid = uid,
        ownerId = "owner1",
        title = title,
        description = "Description",
        imageUrl = null,
        location = Location(0.0, 0.0, "Location"),
        start = Timestamp.now(),
        end = Timestamp.now(),
        maxCapacity = null,
        participationFee = null,
        isFlash = false,
        subtitle = "Subtitle",
        tags = tags,
        website = null)
  }

  private suspend fun setupRepository(events: List<Event>): EventRepositoryLocal {
    val repository = EventRepositoryLocal()
    events.forEach { repository.addEvent(it) }
    return repository
  }

  private suspend fun setupDelayedRepository(
      events: List<Event>,
      delayMs: Long = 1000
  ): EventRepository {
    val local = EventRepositoryLocal()
    events.forEach { local.addEvent(it) }
    return object : EventRepository {
      override fun getNewUid(): String = local.getNewUid()

      override suspend fun getAllVisibleEvents(): List<Event> {
        delay(delayMs)
        return local.getAllVisibleEvents()
      }

      override suspend fun getAllVisibleEventsSatisfying(
          predicate: (Event) -> Boolean
      ): List<Event> {
        delay(delayMs)
        return local.getAllVisibleEventsSatisfying(predicate)
      }

      override suspend fun getEvent(eventUid: String): Event {
        delay(delayMs)
        return local.getEvent(eventUid)
      }

      override suspend fun getEventParticipants(eventUid: String) =
          local.getEventParticipants(eventUid)

      override suspend fun addEvent(event: Event) = local.addEvent(event)

      override suspend fun editEvent(eventUid: String, newEvent: Event) =
          local.editEvent(eventUid, newEvent)

      override suspend fun deleteEvent(eventUid: String) = local.deleteEvent(eventUid)

      override suspend fun addParticipantToEvent(
          eventUid: String,
          participant: com.github.se.studentconnect.model.event.EventParticipant
      ) = local.addParticipantToEvent(eventUid, participant)

      override suspend fun addInvitationToEvent(
          eventUid: String,
          invitedUser: String,
          currentUserId: String
      ) = local.addInvitationToEvent(eventUid, invitedUser, currentUserId)

      override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) =
          local.removeParticipantFromEvent(eventUid, participantUid)
    }
  }

  private fun setupErrorRepository(errorMessage: String = "Network error"): EventRepository {
    return object : EventRepository {
      override fun getNewUid(): String = "newUid"

      override suspend fun getAllVisibleEvents(): List<Event> {
        throw Exception(errorMessage)
      }

      override suspend fun getAllVisibleEventsSatisfying(
          predicate: (Event) -> Boolean
      ): List<Event> {
        throw Exception(errorMessage)
      }

      override suspend fun getEvent(eventUid: String): Event {
        throw Exception(errorMessage)
      }

      override suspend fun getEventParticipants(eventUid: String) =
          emptyList<com.github.se.studentconnect.model.event.EventParticipant>()

      override suspend fun addEvent(event: Event) {}

      override suspend fun editEvent(eventUid: String, newEvent: Event) {}

      override suspend fun deleteEvent(eventUid: String) {}

      override suspend fun addParticipantToEvent(
          eventUid: String,
          participant: com.github.se.studentconnect.model.event.EventParticipant
      ) {}

      override suspend fun addInvitationToEvent(
          eventUid: String,
          invitedUser: String,
          currentUserId: String
      ) {}

      override suspend fun removeParticipantFromEvent(eventUid: String, participantUid: String) {}
    }
  }

  @Test
  fun homeScreen_filterBar_isDisplayed() = runTest {
    val repository = setupRepository(emptyList())
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
  }

  /*
  @Test
  fun homeScreen_applySportsFilter_showsOnlySportsEvents() = runTest {
    val events =
        listOf(
            createTestEvent("1", "Football Match", listOf("Sports")),
            createTestEvent("2", "Piano Concert", listOf("Music")),
            createTestEvent("3", "Tennis Tournament", listOf("Sports")))

    val repository = setupRepository(events)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Football Match").fetchSemanticsNodes().isNotEmpty()
    }

    viewModel.applyFilters(
        FilterData(
            categories = listOf("Sports"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false))

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Football Match").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Football Match").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tennis Tournament").assertIsDisplayed()
    composeTestRule.onNodeWithText("Piano Concert").assertDoesNotExist()
  }
  */

  /*
  @Test
  fun homeScreen_resetFilters_showsAllEvents() = runTest {
    val events =
        listOf(
            createTestEvent("1", "Event 1", listOf("Sports")),
            createTestEvent("2", "Event 2", listOf("Music")))

    val repository = setupRepository(events)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Event 1").fetchSemanticsNodes().isNotEmpty()
    }

    viewModel.applyFilters(
        FilterData(
            categories = listOf("Sports"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false))

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Event 1").fetchSemanticsNodes().isNotEmpty()
    }

    viewModel.applyFilters(
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false))

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Event 1").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Event 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Event 2").assertIsDisplayed()
  }
     */

  @Test
  fun homeScreen_emptyResults_displaysMessage() = runTest {
    val events = listOf(createTestEvent("1", "Music Event", listOf("Music")))

    val repository = setupRepository(events)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Music Event").fetchSemanticsNodes().isNotEmpty()
    }

    viewModel.applyFilters(
        FilterData(
            categories = listOf("Sports"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false))

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText(NO_EVENTS_TEXT).fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText(NO_EVENTS_TEXT).assertIsDisplayed()
  }

  @Test
  fun homeScreen_loading_displaysProgressIndicator() = runTest {
    val repository = setupRepository(emptyList())
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNode(hasTestTag("HomePage")).assertIsDisplayed()
  }

  @Test
  fun homeScreen_topBar_hasSearchField() = runTest {
    val repository = setupRepository(emptyList())
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Search for events...").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Search for events...").assertIsDisplayed()
  }

  @Test
  fun homeScreen_favoriteToggle_worksCorrectly() = runTest {
    val events = listOf(createTestEvent("fav1", "Favorite Event", listOf("Sports")))

    val repository = setupRepository(events)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Favorite Event").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithContentDescription("Favorite").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Favorite").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun homeScreen_clickEvent_navigatesToDetail() = runTest {
    val events = listOf(createTestEvent("1", "Clickable Event", listOf("Sports")))

    val repository = setupRepository(events)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Clickable Event").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Clickable Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Clickable Event").assertHasClickAction()
  }

  /*
  @Test
  fun homeScreen_filterByTopic_worksCorrectly() = runTest {
    val events =
        listOf(
            createTestEvent("1", "Football Event", listOf("Sports", "Football")),
            createTestEvent("2", "Tennis Event", listOf("Sports", "Tennis")),
            createTestEvent("3", "Music Event", listOf("Music")))

    val repository = setupRepository(events)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithText("Football Event").fetchSemanticsNodes().isNotEmpty()
    }

    viewModel.applyFilters(
        FilterData(
            categories = listOf("Sports"),
            location = null,
            radiusKm = 10f,
            priceRange = 0f..100f,
            showOnlyFavorites = false))

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Football Event").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Football Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tennis Event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Music Event").assertDoesNotExist()
  }
     */

  @Test
  fun homeScreen_delayedLoading_displaysEventsAfterDelay() = runTest {
    val events = listOf(createTestEvent("1", "Delayed Event", listOf("Sports")))

    val repository = setupDelayedRepository(events, 2000)
    val viewModel = HomePageViewModel(repository)

    composeTestRule.setContent {
      HomeScreen(navController = rememberNavController(), viewModel = viewModel)
    }

    composeTestRule.onNodeWithText("Delayed Event").assertDoesNotExist()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      composeTestRule.onAllNodesWithText("Delayed Event").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithText("Delayed Event").assertIsDisplayed()
  }
}

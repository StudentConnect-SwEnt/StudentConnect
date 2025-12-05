package com.github.se.studentconnect.ui.screen.home

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.friends.FriendsRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.organization.OrganizationRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.github.se.studentconnect.ui.utils.FilterData
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HomePageViewModelTemporalFilterTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: HomePageViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var organizationRepository: OrganizationRepositoryLocal
  private lateinit var friendsRepository: FriendsRepositoryLocal

  private val epflLocation = Location(46.5191, 6.5668, "EPFL")

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    organizationRepository = OrganizationRepositoryLocal()
    friendsRepository = FriendsRepositoryLocal()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun applyFilters_filtersPastEvents() = runTest {
    // Arrange - Create past event
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -2) // 2 days ago
    val pastDate = Timestamp(calendar.time)

    val pastEvent =
        Event.Public(
            uid = "event-past",
            title = "Past Event",
            subtitle = "Already happened",
            description = "Event that happened 2 days ago",
            start = pastDate,
            end = pastDate,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(pastEvent)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply filters
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Past event should be filtered out
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.none { it.uid == "event-past" })
    assertEquals(0, filteredEvents.size)
  }

  @Test
  fun applyFilters_includesFutureEvents() = runTest {
    // Arrange - Create future event
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 5) // 5 days from now
    val futureDate = Timestamp(calendar.time)

    val futureEvent =
        Event.Public(
            uid = "event-future",
            title = "Future Event",
            subtitle = "Coming soon",
            description = "Event happening in 5 days",
            start = futureDate,
            end = futureDate,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(futureEvent)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply filters
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Future event should be included
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-future" })
    assertEquals(1, filteredEvents.size)
  }

  @Test
  fun applyFilters_includesLiveEvents() = runTest {
    // Arrange - Create live event (started in the past, ends in the future)
    val startCalendar = Calendar.getInstance()
    startCalendar.add(Calendar.HOUR, -1) // Started 1 hour ago
    val startDate = Timestamp(startCalendar.time)

    val endCalendar = Calendar.getInstance()
    endCalendar.add(Calendar.HOUR, 2) // Ends in 2 hours
    val endDate = Timestamp(endCalendar.time)

    val liveEvent =
        Event.Public(
            uid = "event-live",
            title = "Live Event",
            subtitle = "Happening now",
            description = "Event currently in progress",
            start = startDate,
            end = endDate,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(liveEvent)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply filters
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Live event should be included
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-live" })
    assertEquals(1, filteredEvents.size)
  }

  @Test
  fun applyFilters_includesEventWithNoEndTime_whenStartIsInFuture() = runTest {
    // Arrange - Create event with no end time, start in future
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 3) // 3 days from now
    val futureDate = Timestamp(calendar.time)

    val eventNoEndTime =
        Event.Public(
            uid = "event-no-end",
            title = "Event No End Time",
            subtitle = "No end specified",
            description = "Event with no end time",
            start = futureDate,
            end = null,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(eventNoEndTime)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply filters
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Event with no end time should be included if start is in future
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.any { it.uid == "event-no-end" })
    assertEquals(1, filteredEvents.size)
  }

  @Test
  fun applyFilters_filtersEventWithNoEndTime_whenStartIsInPast() = runTest {
    // Arrange - Create event with no end time, start in past
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -3) // 3 days ago
    val pastDate = Timestamp(calendar.time)

    val eventNoEndTime =
        Event.Public(
            uid = "event-past-no-end",
            title = "Past Event No End Time",
            subtitle = "No end specified",
            description = "Past event with no end time",
            start = pastDate,
            end = null,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(eventNoEndTime)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply filters
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Event with no end time should be filtered if start is in past
    val filteredEvents = viewModel.uiState.value.events
    assertTrue(filteredEvents.none { it.uid == "event-past-no-end" })
    assertEquals(0, filteredEvents.size)
  }

  @Test
  fun applyFilters_filtersMixOfPastPresentAndFutureEvents() = runTest {
    // Arrange - Create multiple events at different times
    val calendar = Calendar.getInstance()

    // Past event
    calendar.add(Calendar.DAY_OF_MONTH, -5)
    val pastEvent =
        Event.Public(
            uid = "event-past",
            title = "Past Event",
            subtitle = "Past",
            description = "Past",
            start = Timestamp(calendar.time),
            end = Timestamp(calendar.time),
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    // Live event
    calendar.time = Date()
    calendar.add(Calendar.HOUR, -1)
    val liveStart = Timestamp(calendar.time)
    calendar.time = Date()
    calendar.add(Calendar.HOUR, 1)
    val liveEnd = Timestamp(calendar.time)
    val liveEvent =
        Event.Public(
            uid = "event-live",
            title = "Live Event",
            subtitle = "Live",
            description = "Live",
            start = liveStart,
            end = liveEnd,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner2",
            isFlash = false,
            tags = listOf("Technology"))

    // Future event
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_MONTH, 7)
    val futureEvent =
        Event.Public(
            uid = "event-future",
            title = "Future Event",
            subtitle = "Future",
            description = "Future",
            start = Timestamp(calendar.time),
            end = Timestamp(calendar.time),
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner3",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(pastEvent)
    eventRepository.addEvent(liveEvent)
    eventRepository.addEvent(futureEvent)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply filters
    val filterData =
        FilterData(
            categories = emptyList(),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Should only include live and future events
    val filteredEvents = viewModel.uiState.value.events
    assertEquals(2, filteredEvents.size)
    assertTrue(filteredEvents.none { it.uid == "event-past" })
    assertTrue(filteredEvents.any { it.uid == "event-live" })
    assertTrue(filteredEvents.any { it.uid == "event-future" })
  }

  @Test
  fun applyFilters_temporalFilterWorksWith_otherFilters() = runTest {
    // Arrange - Create future events with different properties
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 3)
    val futureDate = Timestamp(calendar.time)

    val futureTechEvent =
        Event.Public(
            uid = "event-future-tech",
            title = "Future Tech Event",
            subtitle = "Technology",
            description = "Future tech event",
            start = futureDate,
            end = futureDate,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner1",
            isFlash = false,
            tags = listOf("Technology"))

    val futureCultureEvent =
        Event.Public(
            uid = "event-future-culture",
            title = "Future Culture Event",
            subtitle = "Culture",
            description = "Future culture event",
            start = futureDate,
            end = futureDate,
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner2",
            isFlash = false,
            tags = listOf("Culture"))

    // Past tech event
    calendar.time = Date()
    calendar.add(Calendar.DAY_OF_MONTH, -3)
    val pastTechEvent =
        Event.Public(
            uid = "event-past-tech",
            title = "Past Tech Event",
            subtitle = "Technology",
            description = "Past tech event",
            start = Timestamp(calendar.time),
            end = Timestamp(calendar.time),
            location = epflLocation,
            website = "https://event.com",
            ownerId = "owner3",
            isFlash = false,
            tags = listOf("Technology"))

    eventRepository.addEvent(futureTechEvent)
    eventRepository.addEvent(futureCultureEvent)
    eventRepository.addEvent(pastTechEvent)

    viewModel =
        HomePageViewModel(
            eventRepository,
            userRepository,
            null,
            null,
            organizationRepository,
            null,
            friendsRepository)
    advanceUntilIdle()

    // Act - Apply temporal + category filters
    val filterData =
        FilterData(
            categories = listOf("Technology"),
            location = null,
            radiusKm = 100f,
            priceRange = 0f..100f,
            showOnlyFavorites = false)
    viewModel.applyFilters(filterData)
    advanceUntilIdle()

    // Assert - Should only include future tech event
    val filteredEvents = viewModel.uiState.value.events
    assertEquals(1, filteredEvents.size)
    assertTrue(filteredEvents.any { it.uid == "event-future-tech" })
    assertTrue(filteredEvents.none { it.uid == "event-future-culture" })
    assertTrue(filteredEvents.none { it.uid == "event-past-tech" })
  }
}

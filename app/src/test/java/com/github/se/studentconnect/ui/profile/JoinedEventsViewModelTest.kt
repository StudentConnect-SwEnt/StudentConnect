package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.repository.AuthenticationProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.screen.profile.EventFilter
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class JoinedEventsViewModelTest {

  private lateinit var viewModel: JoinedEventsViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private val testDispatcher = StandardTestDispatcher()

  private val testUserId = "test_user_123"
  private val event1Id = "event1"
  private val event2Id = "event2"
  private val event3Id = "event3"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    mockEventRepository = mockk(relaxed = true)
    mockUserRepository = mockk(relaxed = true)

    // Mock AuthenticationProvider
    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId

    viewModel = JoinedEventsViewModel(mockEventRepository, mockUserRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `initial state is loading with past filter`() {
    val state = viewModel.uiState.value
    assert(state.isLoading)
    assert(state.selectedFilter == EventFilter.Past)
    assert(state.allEvents.isEmpty())
    assert(state.filteredEvents.isEmpty())
    assert(state.searchQuery.isEmpty())
  }

  @Test
  fun `loadJoinedEvents successfully loads events`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id)
    val pastEvent = createMockPublicEvent(event1Id, "Past Event", daysAgo = 5)
    val upcomingEvent = createMockPublicEvent(event2Id, "Upcoming Event", daysFromNow = 5)
    val ownedEvent = createMockPublicEvent(event3Id, "Owned Event", daysFromNow = 10)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(ownedEvent)
    coEvery { mockEventRepository.getEvent(event1Id) } returns pastEvent
    coEvery { mockEventRepository.getEvent(event2Id) } returns upcomingEvent
    coEvery { mockEventRepository.getEvent(event3Id) } returns ownedEvent

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(!state.isLoading)
    assert(state.allEvents.size == 3)
    assert(state.allEvents.any { it.uid == event1Id })
    assert(state.allEvents.any { it.uid == event2Id })
    assert(state.allEvents.any { it.uid == event3Id })
  }

  @Test
  fun `loadJoinedEvents filters past events by default`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id)
    val pastEvent = createMockPublicEvent(event1Id, "Past Event", daysAgo = 5)
    val upcomingEvent = createMockPublicEvent(event2Id, "Upcoming Event", daysFromNow = 5)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns pastEvent
    coEvery { mockEventRepository.getEvent(event2Id) } returns upcomingEvent

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(state.filteredEvents.size == 1)
    assert(state.filteredEvents[0].uid == event1Id)
  }

  @Test
  fun `updateFilter switches to upcoming events`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id)
    val pastEvent = createMockPublicEvent(event1Id, "Past Event", daysAgo = 5)
    val upcomingEvent = createMockPublicEvent(event2Id, "Upcoming Event", daysFromNow = 5)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns pastEvent
    coEvery { mockEventRepository.getEvent(event2Id) } returns upcomingEvent

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // When
    viewModel.updateFilter(EventFilter.Upcoming)
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(state.selectedFilter == EventFilter.Upcoming)
    assert(state.filteredEvents.size == 1)
    assert(state.filteredEvents[0].uid == event2Id)
  }

  @Test
  fun `updateSearchQuery filters events by title`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id, event3Id)
    val party1 = createMockPublicEvent(event1Id, "Party Night", daysAgo = 1)
    val party2 = createMockPublicEvent(event2Id, "House Party", daysAgo = 2)
    val meeting = createMockPublicEvent(event3Id, "Team Meeting", daysAgo = 3)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns party1
    coEvery { mockEventRepository.getEvent(event2Id) } returns party2
    coEvery { mockEventRepository.getEvent(event3Id) } returns meeting

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // When
    viewModel.updateSearchQuery("party")
    advanceTimeBy(350) // Wait for debounce
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(state.searchQuery == "party")
    assert(state.filteredEvents.size == 2)
    assert(state.filteredEvents.all { it.title.contains("Party", ignoreCase = true) })
  }

  @Test
  fun `search is case insensitive`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id)
    val event = createMockPublicEvent(event1Id, "PARTY NIGHT", daysAgo = 1)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns event

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // When
    viewModel.updateSearchQuery("party")
    advanceTimeBy(350)
    advanceUntilIdle()

    // Then
    assert(viewModel.uiState.value.filteredEvents.size == 1)
  }

  @Test
  fun `events are sorted by start date descending`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id, event3Id)
    val oldest = createMockPublicEvent(event1Id, "Old Event", daysAgo = 10)
    val middle = createMockPublicEvent(event2Id, "Middle Event", daysAgo = 5)
    val newest = createMockPublicEvent(event3Id, "New Event", daysAgo = 1)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns oldest
    coEvery { mockEventRepository.getEvent(event2Id) } returns middle
    coEvery { mockEventRepository.getEvent(event3Id) } returns newest

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(state.filteredEvents[0].uid == event3Id) // Newest first
    assert(state.filteredEvents[1].uid == event2Id)
    assert(state.filteredEvents[2].uid == event1Id)
  }

  @Test
  fun `handles events without end time`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id)
    val eventWithoutEnd = Event.Public(
        uid = event1Id,
        ownerId = testUserId,
        title = "Event Without End",
        subtitle = "",
        description = "",
        start = createTimestamp(daysAgo = 5),
        end = null, // No end time
        isFlash = false
    )

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns eventWithoutEnd

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then - Should calculate end time as start + 3 hours
    val state = viewModel.uiState.value
    assert(state.filteredEvents.size == 1) // Should be in past events
  }

  @Test
  fun `handles repository errors gracefully`() = runTest {
    // Given
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } throws Exception("Network error")

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(!state.isLoading)
    assert(state.allEvents.isEmpty())
  }

  @Test
  fun `handles event fetch errors for individual events`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id)
    val validEvent = createMockPublicEvent(event1Id, "Valid Event", daysAgo = 1)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns validEvent
    coEvery { mockEventRepository.getEvent(event2Id) } throws Exception("Event not found")

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then - Should only include valid event
    val state = viewModel.uiState.value
    assert(state.allEvents.size == 1)
    assert(state.allEvents[0].uid == event1Id)
  }

  @Test
  fun `does not load events when user is not authenticated`() = runTest {
    // Given
    every { AuthenticationProvider.currentUser } returns ""

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    coVerify(exactly = 0) { mockUserRepository.getJoinedEvents(any()) }
  }

  @Test
  fun `combines joined and owned events without duplicates`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id, event2Id)
    val ownedEvent = createMockPublicEvent(event2Id, "Owned Event", daysAgo = 1) // event2Id in both

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(ownedEvent)
    coEvery { mockEventRepository.getEvent(event1Id) } returns createMockPublicEvent(event1Id, "Event 1", daysAgo = 1)
    coEvery { mockEventRepository.getEvent(event2Id) } returns ownedEvent

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(state.allEvents.size == 2) // No duplicates
    coVerify(exactly = 1) { mockEventRepository.getEvent(event2Id) } // Fetched only once
  }

  @Test
  fun `private events are handled correctly`() = runTest {
    // Given
    val joinedEventIds = listOf(event1Id)
    val privateEvent = Event.Private(
        uid = event1Id,
        ownerId = testUserId,
        title = "Private Party",
        description = "Secret event",
        start = createTimestamp(daysAgo = 2),
        end = createTimestamp(daysAgo = 1),
        isFlash = false
    )

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns joinedEventIds
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns privateEvent

    // When
    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assert(state.allEvents.size == 1)
    assert(state.allEvents[0] is Event.Private)
  }

  // Helper functions
  private fun createMockPublicEvent(
      uid: String,
      title: String,
      daysAgo: Int = 0,
      daysFromNow: Int = 0
  ): Event.Public {
    val start = if (daysAgo > 0) createTimestamp(daysAgo = daysAgo)
    else createTimestamp(daysFromNow = daysFromNow)

    val end = if (daysAgo > 0) createTimestamp(daysAgo = daysAgo - 1)
    else createTimestamp(daysFromNow = daysFromNow + 1)

    return Event.Public(
        uid = uid,
        ownerId = testUserId,
        title = title,
        subtitle = "Subtitle for $title",
        description = "Description for $title",
        start = start,
        end = end,
        isFlash = false
    )
  }

  private fun createTimestamp(daysAgo: Int = 0, daysFromNow: Int = 0): Timestamp {
    val cal = Calendar.getInstance()
    if (daysAgo > 0) {
      cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
    } else if (daysFromNow > 0) {
      cal.add(Calendar.DAY_OF_YEAR, daysFromNow)
    }
    return Timestamp(cal.time)
  }
}

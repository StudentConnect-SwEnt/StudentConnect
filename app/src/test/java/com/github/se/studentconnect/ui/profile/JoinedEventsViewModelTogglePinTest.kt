package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.screen.profile.EventFilter
import com.google.firebase.Timestamp
import io.mockk.*
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinedEventsViewModelTogglePinTest {

  private lateinit var viewModel: JoinedEventsViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private val testDispatcher = StandardTestDispatcher()

  private val testUserId = "test_user_123"
  private val event1Id = "event1"
  private val event2Id = "event2"
  private val event3Id = "event3"
  private val event4Id = "event4"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    mockEventRepository = mockk(relaxed = true)
    mockUserRepository = mockk(relaxed = true)

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId

    // Default setup
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns emptyList()
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()

    viewModel = JoinedEventsViewModel(mockEventRepository, mockUserRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun togglePinEvent_pinsEventSuccessfully() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, event1Id) } just Runs

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    coVerify { mockUserRepository.addPinnedEvent(testUserId, event1Id) }
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
  }

  @Test
  fun togglePinEvent_unpinsEventSuccessfully() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf(event1Id)
    coEvery { mockUserRepository.removePinnedEvent(testUserId, event1Id) } just Runs

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    coVerify { mockUserRepository.removePinnedEvent(testUserId, event1Id) }
    assert(!viewModel.uiState.value.pinnedEventIds.contains(event1Id))
  }

  @Test
  fun togglePinEvent_pinsTwoEventsSuccessfully() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, any()) } just Runs

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    viewModel.togglePinEvent(event2Id, "Max pinned reached")
    advanceUntilIdle()

    coVerify { mockUserRepository.addPinnedEvent(testUserId, event1Id) }
    coVerify { mockUserRepository.addPinnedEvent(testUserId, event2Id) }
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
    assert(viewModel.uiState.value.pinnedEventIds.contains(event2Id))
  }

  @Test
  fun togglePinEvent_pinsThreeEventsSuccessfully() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, any()) } just Runs

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    viewModel.togglePinEvent(event2Id, "Max pinned reached")
    advanceUntilIdle()

    viewModel.togglePinEvent(event3Id, "Max pinned reached")
    advanceUntilIdle()

    assert(viewModel.uiState.value.pinnedEventIds.size == 3)
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
    assert(viewModel.uiState.value.pinnedEventIds.contains(event2Id))
    assert(viewModel.uiState.value.pinnedEventIds.contains(event3Id))
  }

  @Test
  fun togglePinEvent_showsSnackbar_whenTryingToPinFourthEvent() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf(event1Id, event2Id, event3Id)

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    val maxPinnedMessage = "Maximum 3 pinned events. Please unpin an event to pin this one."
    viewModel.togglePinEvent(event4Id, maxPinnedMessage)
    advanceUntilIdle()

    coVerify(exactly = 0) { mockUserRepository.addPinnedEvent(testUserId, event4Id) }
    assert(viewModel.snackbarMessage.value == maxPinnedMessage)
    assert(!viewModel.uiState.value.pinnedEventIds.contains(event4Id))
  }

  @Test
  fun clearSnackbarMessage_clearsMessage() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf(event1Id, event2Id, event3Id)

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event4Id, "Max pinned reached")
    advanceUntilIdle()

    assert(viewModel.snackbarMessage.value != null)

    viewModel.clearSnackbarMessage()

    assert(viewModel.snackbarMessage.value == null)
  }

  @Test
  fun togglePinEvent_unpinAndPinNewEvent() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf(event1Id, event2Id, event3Id)
    coEvery { mockUserRepository.removePinnedEvent(testUserId, any()) } just Runs
    coEvery { mockUserRepository.addPinnedEvent(testUserId, any()) } just Runs

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Unpin event1
    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    // Pin event4
    viewModel.togglePinEvent(event4Id, "Max pinned reached")
    advanceUntilIdle()

    coVerify { mockUserRepository.removePinnedEvent(testUserId, event1Id) }
    coVerify { mockUserRepository.addPinnedEvent(testUserId, event4Id) }

    val pinnedIds = viewModel.uiState.value.pinnedEventIds
    assert(!pinnedIds.contains(event1Id))
    assert(pinnedIds.contains(event2Id))
    assert(pinnedIds.contains(event3Id))
    assert(pinnedIds.contains(event4Id))
  }

  @Test
  fun togglePinEvent_handlesRepositoryError_whenPinning() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, event1Id) } throws
        Exception("Network error")

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    // Should reload pinned IDs to sync with server
    coVerify { mockUserRepository.getPinnedEvents(testUserId) }
  }

  @Test
  fun togglePinEvent_handlesRepositoryError_whenUnpinning() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf(event1Id)
    coEvery { mockUserRepository.removePinnedEvent(testUserId, event1Id) } throws
        Exception("Network error")

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    // Should reload pinned IDs to sync with server
    coVerify(atLeast = 2) { mockUserRepository.getPinnedEvents(testUserId) }
  }

  @Test
  fun togglePinEvent_doesNothing_whenUserNotAuthenticated() = runTest {
    every { AuthenticationProvider.currentUser } returns ""

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    coVerify(exactly = 0) { mockUserRepository.addPinnedEvent(any(), any()) }
    coVerify(exactly = 0) { mockUserRepository.removePinnedEvent(any(), any()) }
  }

  @Test
  fun loadJoinedEvents_loadsPinnedEventIds() = runTest {
    val pinnedIds = listOf(event1Id, event2Id)
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns pinnedIds
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns emptyList()

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    assert(viewModel.uiState.value.pinnedEventIds == pinnedIds)
  }

  @Test
  fun togglePinEvent_updatesStateOptimistically() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, event1Id) } coAnswers
        {
          kotlinx.coroutines.delay(1000) // Simulate slow network
        }

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceTimeBy(100) // Before network completes

    // State should be updated optimistically
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
  }

  @Test
  fun togglePinEvent_maintainsPinnedStateAcrossFilters() = runTest {
    val pastEvent = createMockPublicEvent(event1Id, "Past Event", daysAgo = 5)
    val upcomingEvent = createMockPublicEvent(event2Id, "Upcoming Event", daysFromNow = 5)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns listOf(event1Id, event2Id)
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf(event1Id)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns pastEvent
    coEvery { mockEventRepository.getEvent(event2Id) } returns upcomingEvent

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
    assert(viewModel.uiState.value.selectedFilter == EventFilter.Past)

    // Switch to upcoming filter
    viewModel.updateFilter(EventFilter.Upcoming)
    advanceUntilIdle()

    // Pinned state should be maintained
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
  }

  @Test
  fun togglePinEvent_worksWithSearchQuery() = runTest {
    val event1 = createMockPublicEvent(event1Id, "Party Night", daysAgo = 1)
    val event2 = createMockPublicEvent(event2Id, "Meeting", daysAgo = 2)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns listOf(event1Id, event2Id)
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, any()) } just Runs
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(event1Id) } returns event1
    coEvery { mockEventRepository.getEvent(event2Id) } returns event2

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    viewModel.updateSearchQuery("party")
    advanceTimeBy(350) // Wait for debounce
    advanceUntilIdle()

    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()

    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
    assert(viewModel.uiState.value.filteredEvents.size == 1)
  }

  @Test
  fun togglePinEvent_canPinSameEventMultipleTimes_afterUnpinning() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.addPinnedEvent(testUserId, event1Id) } just Runs
    coEvery { mockUserRepository.removePinnedEvent(testUserId, event1Id) } just Runs

    viewModel.loadJoinedEvents()
    advanceUntilIdle()

    // Pin event1
    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))

    // Unpin event1
    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()
    assert(!viewModel.uiState.value.pinnedEventIds.contains(event1Id))

    // Pin event1 again
    viewModel.togglePinEvent(event1Id, "Max pinned reached")
    advanceUntilIdle()
    assert(viewModel.uiState.value.pinnedEventIds.contains(event1Id))
  }

  @Test
  fun viewModel_initializesWithEmptyPinnedEventIds() = runTest {
    assert(viewModel.uiState.value.pinnedEventIds.isEmpty())
  }

  // Helper functions
  private fun createMockPublicEvent(
      uid: String,
      title: String,
      daysAgo: Int = 0,
      daysFromNow: Int = 0
  ): Event.Public {
    val start =
        if (daysAgo > 0) createTimestamp(daysAgo = daysAgo)
        else createTimestamp(daysFromNow = daysFromNow)

    val end =
        if (daysAgo > 0) createTimestamp(daysAgo = daysAgo - 1)
        else createTimestamp(daysFromNow = daysFromNow + 1)

    return Event.Public(
        uid = uid,
        ownerId = testUserId,
        title = title,
        subtitle = "Subtitle for $title",
        description = "Description for $title",
        start = start,
        end = end,
        isFlash = false)
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

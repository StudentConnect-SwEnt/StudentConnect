package com.github.se.studentconnect.ui.profile

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.mockk
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ProfileScreenViewModelPinnedEventsTest {

  private lateinit var testDispatcher: TestDispatcher
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockFriendsRepository: FriendsRepository
  private lateinit var mockEventRepository: EventRepository
  private lateinit var viewModel: ProfileScreenViewModel

  private val testUserId = "user123"
  private val testUser =
      User(
          userId = testUserId,
          username = "testuser",
          firstName = "Test",
          lastName = "User",
          email = "test@example.com",
          university = "EPFL",
          hobbies = listOf("coding"),
          pinnedEventIds = emptyList())

  @Before
  fun setUp() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    mockUserRepository = mockk(relaxed = true)
    mockFriendsRepository = mockk(relaxed = true)
    mockEventRepository = mockk(relaxed = true)

    coEvery { mockUserRepository.getUserById(testUserId) } returns testUser
    coEvery { mockFriendsRepository.getFriends(testUserId) } returns emptyList()
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns emptyList()
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadPinnedEvents_returnsEmptyList_whenUserHasNoPinnedEvents() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    assertTrue(viewModel.pinnedEvents.value.isEmpty())
  }

  @Test
  fun loadPinnedEvents_loadsOnePinnedEvent() = runTest {
    val event = createTestEvent("event1", "Test Event 1")
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf("event1")
    coEvery { mockEventRepository.getEvent("event1") } returns event

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0].uid)
    assertEquals("Test Event 1", pinnedEvents[0].title)
  }

  @Test
  fun loadPinnedEvents_loadsTwoPinnedEvents() = runTest {
    val event1 = createTestEvent("event1", "Test Event 1")
    val event2 = createTestEvent("event2", "Test Event 2")

    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2")
    coEvery { mockEventRepository.getEvent("event1") } returns event1
    coEvery { mockEventRepository.getEvent("event2") } returns event2

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(2, pinnedEvents.size)
    assertTrue(pinnedEvents.any { it.uid == "event1" })
    assertTrue(pinnedEvents.any { it.uid == "event2" })
  }

  @Test
  fun loadPinnedEvents_loadsMaximumThreePinnedEvents() = runTest {
    val event1 = createTestEvent("event1", "Test Event 1")
    val event2 = createTestEvent("event2", "Test Event 2")
    val event3 = createTestEvent("event3", "Test Event 3")

    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2", "event3")
    coEvery { mockEventRepository.getEvent("event1") } returns event1
    coEvery { mockEventRepository.getEvent("event2") } returns event2
    coEvery { mockEventRepository.getEvent("event3") } returns event3

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(3, pinnedEvents.size)
    assertTrue(pinnedEvents.any { it.uid == "event1" })
    assertTrue(pinnedEvents.any { it.uid == "event2" })
    assertTrue(pinnedEvents.any { it.uid == "event3" })
  }

  @Test
  fun loadPinnedEvents_handlesEventFetchError() = runTest {
    val event1 = createTestEvent("event1", "Test Event 1")

    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2")
    coEvery { mockEventRepository.getEvent("event1") } returns event1
    coEvery { mockEventRepository.getEvent("event2") } throws Exception("Event not found")

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0].uid)
  }

  @Test
  fun loadPinnedEvents_handlesAllEventsFetchError() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2")
    coEvery { mockEventRepository.getEvent(any()) } throws Exception("Network error")

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertTrue(pinnedEvents.isEmpty())
  }

  @Test
  fun loadPinnedEvents_handlesRepositoryError() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } throws Exception("Database error")

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertTrue(pinnedEvents.isEmpty())
  }

  @Test
  fun loadPinnedEvents_canBeCalledManually() = runTest {
    val event1 = createTestEvent("event1", "Test Event 1")
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    assertTrue(viewModel.pinnedEvents.value.isEmpty())

    // Update mock to return pinned events
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf("event1")
    coEvery { mockEventRepository.getEvent("event1") } returns event1

    viewModel.loadPinnedEvents()
    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0].uid)
  }

  @Test
  fun loadPinnedEvents_loadsBothPublicAndPrivateEvents() = runTest {
    val publicEvent =
        Event.Public(
            uid = "event1",
            ownerId = testUserId,
            title = "Public Event",
            subtitle = "Public Subtitle",
            description = "Public event description",
            start = createTimestamp(daysAgo = 5),
            end = createTimestamp(daysAgo = 4),
            isFlash = false)

    val privateEvent =
        Event.Private(
            uid = "event2",
            ownerId = testUserId,
            title = "Private Event",
            description = "Private event description",
            start = createTimestamp(daysAgo = 3),
            end = createTimestamp(daysAgo = 2),
            isFlash = false)

    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2")
    coEvery { mockEventRepository.getEvent("event1") } returns publicEvent
    coEvery { mockEventRepository.getEvent("event2") } returns privateEvent

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(2, pinnedEvents.size)
    assertTrue(pinnedEvents.any { it is Event.Public && it.uid == "event1" })
    assertTrue(pinnedEvents.any { it is Event.Private && it.uid == "event2" })
  }

  @Test
  fun loadPinnedEvents_maintainsOrderFromRepository() = runTest {
    val event1 = createTestEvent("event1", "First Event")
    val event2 = createTestEvent("event2", "Second Event")
    val event3 = createTestEvent("event3", "Third Event")

    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2", "event3")
    coEvery { mockEventRepository.getEvent("event1") } returns event1
    coEvery { mockEventRepository.getEvent("event2") } returns event2
    coEvery { mockEventRepository.getEvent("event3") } returns event3

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals("event1", pinnedEvents[0].uid)
    assertEquals("event2", pinnedEvents[1].uid)
    assertEquals("event3", pinnedEvents[2].uid)
  }

  @Test
  fun viewModel_initializesPinnedEventsOnCreation() = runTest {
    val event = createTestEvent("event1", "Test Event")
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf("event1")
    coEvery { mockEventRepository.getEvent("event1") } returns event

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    assertEquals(1, viewModel.pinnedEvents.value.size)
  }

  @Test
  fun loadUserProfile_doesNotAffectPinnedEvents() = runTest {
    val event = createTestEvent("event1", "Test Event")
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns listOf("event1")
    coEvery { mockEventRepository.getEvent("event1") } returns event

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    assertEquals(1, viewModel.pinnedEvents.value.size)

    // Call loadUserProfile
    viewModel.loadUserProfile()
    advanceUntilIdle()

    // Pinned events should remain unchanged
    assertEquals(1, viewModel.pinnedEvents.value.size)
    assertEquals("event1", viewModel.pinnedEvents.value[0].uid)
  }

  @Test
  fun loadPinnedEvents_filtersNullEvents() = runTest {
    val event1 = createTestEvent("event1", "Test Event 1")

    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns
        listOf("event1", "event2", "event3")
    coEvery { mockEventRepository.getEvent("event1") } returns event1
    coEvery { mockEventRepository.getEvent("event2") } returns null
    coEvery { mockEventRepository.getEvent("event3") } throws Exception("Not found")

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    advanceUntilIdle()

    val pinnedEvents = viewModel.pinnedEvents.value
    assertEquals(1, pinnedEvents.size)
    assertEquals("event1", pinnedEvents[0].uid)
  }

  @Test
  fun loadPinnedEvents_handlesEmptyEventIdList() = runTest {
    coEvery { mockUserRepository.getPinnedEvents(testUserId) } returns emptyList()

    viewModel =
        ProfileScreenViewModel(
            userRepository = mockUserRepository,
            friendsRepository = mockFriendsRepository,
            eventRepository = mockEventRepository,
            currentUserId = testUserId)

    viewModel.loadPinnedEvents()
    advanceUntilIdle()

    assertTrue(viewModel.pinnedEvents.value.isEmpty())
  }

  // Helper functions
  private fun createTestEvent(uid: String, title: String): Event.Public {
    return Event.Public(
        uid = uid,
        ownerId = testUserId,
        title = title,
        subtitle = "Subtitle for $title",
        description = "Description for $title",
        start = createTimestamp(daysAgo = 5),
        end = createTimestamp(daysAgo = 4),
        isFlash = false)
  }

  private fun createTimestamp(daysAgo: Int): Timestamp {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
    return Timestamp(cal.time)
  }
}

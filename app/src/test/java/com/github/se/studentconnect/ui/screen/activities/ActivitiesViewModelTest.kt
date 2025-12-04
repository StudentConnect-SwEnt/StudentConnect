package com.github.se.studentconnect.ui.screen.activities

import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.activities.InvitationStatus
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesViewModelTest {

  private lateinit var viewModel: ActivitiesViewModel
  private lateinit var mockEventRepository: EventRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockGetString: (Int) -> String
  private val testDispatcher = StandardTestDispatcher()

  private val testUserId = "user-123"
  private val testEventId = "event-123"
  private val testEventId2 = "event-456"
  private val testOwnerId = "owner-789"

  private val futureTime = Timestamp(System.currentTimeMillis() / 1000 + 86400, 0) // 1 day from now
  private val pastTime = Timestamp(System.currentTimeMillis() / 1000 - 86400, 0) // 1 day ago
  private val futureEndTime = Timestamp(System.currentTimeMillis() / 1000 + 90000, 0)
  private val pastEndTime = Timestamp(System.currentTimeMillis() / 1000 - 82800, 0)

  private val testUpcomingEvent =
      Event.Public(
          uid = testEventId,
          title = "Upcoming Event",
          description = "Test upcoming event",
          ownerId = testOwnerId,
          start = futureTime,
          end = futureEndTime,
          location = Location(46.5197, 6.5668, "EPFL"),
          maxCapacity = 100u,
          tags = listOf("Test"),
          website = "https://test.com",
          imageUrl = null,
          isFlash = false,
          subtitle = "Test Event")

  private val testPastEvent =
      Event.Public(
          uid = testEventId2,
          title = "Past Event",
          description = "Test past event",
          ownerId = testOwnerId,
          start = pastTime,
          end = pastEndTime,
          location = Location(46.5197, 6.5668, "EPFL"),
          maxCapacity = 50u,
          tags = listOf("Past"),
          website = null,
          imageUrl = null,
          isFlash = false,
          subtitle = "Past Event")

  private val testInvitation =
      Invitation(
          eventId = testEventId,
          from = testOwnerId,
          status = InvitationStatus.Pending,
          timestamp = Timestamp.now())

  private val testUser =
      User(
          userId = testUserId,
          firstName = "John",
          lastName = "Doe",
          email = "john@test.com",
          username = "johndoe",
          university = "EPFL",
          bio = "Test user")

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    mockEventRepository = mockk(relaxed = true)
    mockUserRepository = mockk(relaxed = true)
    mockGetString = mockk(relaxed = true)

    mockkObject(AuthenticationProvider)
    every { AuthenticationProvider.currentUser } returns testUserId

    // Mock getString responses
    every { mockGetString(R.string.anonymous) } returns "Anonymous"
    every { mockGetString(R.string.error_failed_to_load_events) } returns
        "Failed to load events: %s"
    every { mockGetString(R.string.error_unknown) } returns "Unknown error"

    // Default mock responses
    coEvery { mockUserRepository.getJoinedEvents(any()) } returns emptyList()
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockUserRepository.getInvitations(any()) } returns emptyList()
    coEvery { mockUserRepository.getUserById(any()) } returns testUser
    coEvery { mockEventRepository.getEvent(any()) } returns testUpcomingEvent
    coEvery { mockUserRepository.acceptInvitation(any(), any()) } just Runs
    coEvery { mockUserRepository.declineInvitation(any(), any()) } just Runs
    coEvery { mockEventRepository.addParticipantToEvent(any(), any()) } just Runs

    viewModel = ActivitiesViewModel(mockEventRepository, mockUserRepository, mockGetString)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `initial state is correct`() {
    val state = viewModel.uiState.value
    assertTrue(state.items.isEmpty())
    assertEquals(EventTab.Upcoming, state.selectedTab)
    assertTrue(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `onTabSelected changes selected tab`() = runTest {
    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    assertEquals(EventTab.Invitations, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `onTabSelected triggers refresh with correct tab`() = runTest {
    coEvery { mockUserRepository.getInvitations(testUserId) } returns emptyList()

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    coVerify { mockUserRepository.getInvitations(testUserId) }
  }

  @Test
  fun `refreshEvents with null userUid sets empty items and not loading`() = runTest {
    viewModel.refreshEvents(null)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.items.isEmpty())
    assertFalse(state.isLoading)
  }

  @Test
  fun `refreshEvents for Upcoming tab loads joined and owned events`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns listOf(testEventId)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(testUpcomingEvent)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent

    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    assertTrue(state.items.first() is EventCarouselItem)
    assertFalse(state.isLoading)
  }

  @Test
  fun `refreshEvents for Upcoming tab filters out past events`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns emptyList()
    coEvery { mockEventRepository.getAllVisibleEvents() } returns
        listOf(testUpcomingEvent, testPastEvent)

    every { AuthenticationProvider.currentUser } returns testOwnerId

    viewModel.refreshEvents(testOwnerId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    val eventItem = state.items.first() as EventCarouselItem
    assertEquals("Upcoming Event", eventItem.event.title)
  }

  @Test
  fun `refreshEvents for Upcoming tab includes events without end time`() = runTest {
    val eventWithoutEndTime = testUpcomingEvent.copy(end = null)
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns emptyList()
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(eventWithoutEndTime)

    every { AuthenticationProvider.currentUser } returns testOwnerId

    viewModel.refreshEvents(testOwnerId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
  }

  @Test
  fun `refreshEvents for Invitations tab loads invitations`() = runTest {
    coEvery { mockUserRepository.getInvitations(testUserId) } returns listOf(testInvitation)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns testUser

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    assertTrue(state.items.first() is InvitationCarouselItem)
  }

  @Test
  fun `refreshEvents for Invitations tab handles missing sender gracefully`() = runTest {
    coEvery { mockUserRepository.getInvitations(testUserId) } returns listOf(testInvitation)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns null

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    val invitationItem = state.items.first() as InvitationCarouselItem
    assertEquals("Anonymous", invitationItem.invitedBy)
  }

  @Test
  fun `refreshEvents for Invitations tab handles failed event fetch`() = runTest {
    coEvery { mockUserRepository.getInvitations(testUserId) } returns listOf(testInvitation)
    coEvery { mockEventRepository.getEvent(testEventId) } throws Exception("Event not found")

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.items.isEmpty())
  }

  @Test
  fun `refreshEvents for Past tab loads past events`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns listOf(testEventId2)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(testEventId2) } returns testPastEvent

    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    val eventItem = state.items.first() as EventCarouselItem
    assertEquals("Past Event", eventItem.event.title)
  }

  @Test
  fun `refreshEvents for Past tab includes owned events`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns emptyList()
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(testPastEvent)
    coEvery { mockEventRepository.getEvent(testPastEvent.uid) } returns testPastEvent

    every { AuthenticationProvider.currentUser } returns testOwnerId

    viewModel.refreshEvents(testOwnerId)
    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
  }

  @Test
  fun `refreshEvents for Past tab filters out future events`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns
        listOf(testEventId, testEventId2)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockEventRepository.getEvent(testEventId2) } returns testPastEvent

    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    val eventItem = state.items.first() as EventCarouselItem
    assertEquals("Past Event", eventItem.event.title)
  }

  @Test
  fun `refreshEvents handles getAllVisibleEvents exception`() = runTest {
    coEvery { mockEventRepository.getAllVisibleEvents() } throws Exception("Network error")

    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.errorMessage?.contains("Failed to load events") == true)
  }

  @Test
  fun `refreshEvents handles getEvent exception for joined events`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns
        listOf(testEventId, testEventId2)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockEventRepository.getEvent(testEventId2) } throws Exception("Not found")

    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
  }

  @Test
  fun `acceptInvitation calls repository methods and removes invitation from list`() = runTest {
    coEvery { mockUserRepository.getInvitations(testUserId) } returns listOf(testInvitation)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns testUser

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertEquals(1, initialState.items.size)

    viewModel.acceptInvitation(testInvitation)
    advanceUntilIdle()

    coVerify { mockUserRepository.acceptInvitation(testEventId, testUserId) }
    coVerify {
      mockEventRepository.addParticipantToEvent(testEventId, EventParticipant(testUserId))
    }

    val finalState = viewModel.uiState.value
    assertTrue(finalState.items.isEmpty())
  }

  @Test
  fun `declineInvitation updates invitation status to Declined`() = runTest {
    coEvery { mockUserRepository.getInvitations(testUserId) } returns listOf(testInvitation)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns testUser

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    viewModel.declineInvitation(testInvitation)
    advanceUntilIdle()

    coVerify { mockUserRepository.declineInvitation(testEventId, testUserId) }

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    val invitationItem = state.items.first() as InvitationCarouselItem
    assertEquals(InvitationStatus.Declined, invitationItem.invitation.status)
  }

  @Test
  fun `clearErrorMessage clears error from state`() = runTest {
    coEvery { mockEventRepository.getAllVisibleEvents() } throws Exception("Test error")

    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.errorMessage != null)

    viewModel.clearErrorMessage()

    assertNull(viewModel.uiState.value.errorMessage)
  }

  @Test
  fun `refreshEvents cancels previous job when called again`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } coAnswers
        {
          kotlinx.coroutines.delay(1000)
          emptyList()
        }

    viewModel.refreshEvents(testUserId)
    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    // Should not throw any exceptions due to cancellation
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun `refreshEvents only updates state if tab hasn't changed`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns listOf(testEventId)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(testUpcomingEvent)

    viewModel.refreshEvents(testUserId)

    // Change tab before refresh completes
    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    // State should reflect Past tab, not Upcoming
    assertEquals(EventTab.Past, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun `refreshEvents loads joined events not in visible events list`() = runTest {
    val privateEvent =
        Event.Private(
            uid = "private-123",
            title = "Private Event",
            description = "Private",
            ownerId = testUserId,
            start = futureTime,
            end = futureEndTime,
            location = null,
            maxCapacity = null,
            imageUrl = null,
            isFlash = false)

    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns
        listOf(testEventId, "private-123")
    coEvery { mockEventRepository.getAllVisibleEvents() } returns listOf(testUpcomingEvent)
    coEvery { mockEventRepository.getEvent("private-123") } returns privateEvent

    viewModel.refreshEvents(testUserId)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.items.size)
  }

  @Test
  fun `refreshEvents sets isLoading to true during loading`() = runTest {
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } coAnswers
        {
          kotlinx.coroutines.delay(100)
          emptyList()
        }

    viewModel.refreshEvents(testUserId)

    // Check that loading is true before completion
    assertTrue(viewModel.uiState.value.isLoading)

    advanceUntilIdle()

    // Check that loading is false after completion
    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun `refreshEvents for Past tab handles events without end time`() = runTest {
    val eventWithoutEnd = testPastEvent.copy(end = null)
    coEvery { mockUserRepository.getJoinedEvents(testUserId) } returns listOf(testEventId2)
    coEvery { mockEventRepository.getAllVisibleEvents() } returns emptyList()
    coEvery { mockEventRepository.getEvent(testEventId2) } returns eventWithoutEnd

    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    // Event should be filtered as past because its start is in the past; vérifier qu'il est bien
    // dans la liste
    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
  }

  @Test
  fun `multiple invitations are loaded correctly`() = runTest {
    val invitation2 = testInvitation.copy(eventId = testEventId2)
    coEvery { mockUserRepository.getInvitations(testUserId) } returns
        listOf(testInvitation, invitation2)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockEventRepository.getEvent(testEventId2) } returns testPastEvent
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns testUser

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(2, state.items.size)
  }

  @Test
  fun `acceptInvitation only removes the accepted invitation`() = runTest {
    val invitation2 = testInvitation.copy(eventId = testEventId2)
    coEvery { mockUserRepository.getInvitations(testUserId) } returns
        listOf(testInvitation, invitation2)
    coEvery { mockEventRepository.getEvent(testEventId) } returns testUpcomingEvent
    coEvery { mockEventRepository.getEvent(testEventId2) } returns testPastEvent
    coEvery { mockUserRepository.getUserById(testOwnerId) } returns testUser

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    assertEquals(2, viewModel.uiState.value.items.size)

    viewModel.acceptInvitation(testInvitation)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.items.size)
    val remainingItem = state.items.first() as InvitationCarouselItem
    assertEquals(testEventId2, remainingItem.invitation.eventId)
  }
}

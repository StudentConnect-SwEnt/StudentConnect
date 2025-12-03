package com.github.se.studentconnect.ui.screen.activities

import com.github.se.studentconnect.model.activities.Invitation
import com.github.se.studentconnect.model.activities.InvitationStatus
import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.user.UserRepository
import com.google.firebase.Timestamp
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivitiesViewModelTest {

  private lateinit var viewModel: ActivitiesViewModel
  private lateinit var eventRepository: EventRepository
  private lateinit var userRepository: UserRepository
  private val testDispatcher = StandardTestDispatcher()

  private val mockUser = "user123"
  private val mockEvent =
      Event.Public(
          uid = "event123",
          ownerId = "owner123",
          title = "Test Event",
          subtitle = "Test Subtitle",
          description = "Test Description",
          imageUrl = null,
          location = null,
          start = Timestamp.Companion.now(),
          end = null,
          maxCapacity = 50u,
          participationFee = null,
          isFlash = false,
          tags = emptyList(),
          website = null)

  private val mockInvitation =
      Invitation(
          eventId = "event456",
          from = "inviter123",
          status = InvitationStatus.Pending,
          timestamp = Timestamp.Companion.now())

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)

    eventRepository = mockk(relaxed = true)
    userRepository = mockk(relaxed = true)

    // Set test user ID for AuthenticationProvider
    AuthenticationProvider.testUserId = mockUser

    viewModel = ActivitiesViewModel(eventRepository, userRepository)
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun initialStateIsCorrect() {
    val state = viewModel.uiState.value

    Assert.assertEquals(EventTab.Upcoming, state.selectedTab)
    Assert.assertTrue(state.items.isEmpty())
    Assert.assertTrue(state.isLoading)
  }

  @Test
  fun onTabSelectedUpdatesSelectedTab() = runTest {
    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    Assert.assertEquals(EventTab.Invitations, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun onTabSelectedTriggersRefreshEvents() = runTest {
    coEvery { userRepository.getJoinedEvents(any()) } returns emptyList()

    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    coVerify { userRepository.getJoinedEvents(mockUser) }
  }

  @Test
  fun refreshEventsWithNullUserClearsItems() = runTest {
    viewModel.refreshEvents(null)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertTrue(state.items.isEmpty())
    Assert.assertFalse(state.isLoading)
  }

  @Test
  fun refreshEventsLoadsUpcomingEventsCorrectly() = runTest {
    val futureEvent = mockEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0))

    coEvery { userRepository.getJoinedEvents(mockUser) } returns listOf(futureEvent.uid)
    coEvery { eventRepository.getEvent(futureEvent.uid) } returns futureEvent

    viewModel.onTabSelected(EventTab.Upcoming)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertFalse(state.isLoading)
    Assert.assertEquals(1, state.items.size)
    Assert.assertTrue(state.items[0] is EventCarouselItem)
    Assert.assertEquals(futureEvent.uid, state.items[0].uid)
  }

  @Test
  fun refreshEventsFiltersOutPastEventsForUpcomingTab() = runTest {
    val pastEvent =
        mockEvent.copy(
            start = Timestamp(System.currentTimeMillis() / 1000 - 7200, 0),
            end = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0))

    coEvery { userRepository.getJoinedEvents(mockUser) } returns listOf(pastEvent.uid)
    coEvery { eventRepository.getEvent(pastEvent.uid) } returns pastEvent

    viewModel.onTabSelected(EventTab.Upcoming)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertTrue(state.items.isEmpty())
  }

  @Test
  fun refreshEventsUsesDefaultEndTimeWhenNotSpecified() = runTest {
    val eventWithoutEnd =
        mockEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0), end = null)

    coEvery { userRepository.getJoinedEvents(mockUser) } returns listOf(eventWithoutEnd.uid)
    coEvery { eventRepository.getEvent(eventWithoutEnd.uid) } returns eventWithoutEnd

    viewModel.onTabSelected(EventTab.Upcoming)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertEquals(1, state.items.size)
  }

  @Test
  fun refreshEventsLoadsInvitationsCorrectly() = runTest {
    coEvery { userRepository.getInvitations(mockUser) } returns listOf(mockInvitation)
    coEvery { eventRepository.getEvent(mockInvitation.eventId) } returns
        mockEvent.copy(uid = mockInvitation.eventId)
    coEvery { userRepository.getUserById(mockInvitation.from) } returns
        mockk { every { firstName } returns "John" }

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertFalse(state.isLoading)
    Assert.assertEquals(1, state.items.size)
    Assert.assertTrue(state.items[0] is InvitationCarouselItem)

    val invitationItem = state.items[0] as InvitationCarouselItem
    Assert.assertEquals(mockInvitation.eventId, invitationItem.uid)
    Assert.assertEquals("John", invitationItem.invitedBy)
  }

  @Test
  fun refreshEventsShowsAnonymousWhenUserNotFound() = runTest {
    coEvery { userRepository.getInvitations(mockUser) } returns listOf(mockInvitation)
    coEvery { eventRepository.getEvent(mockInvitation.eventId) } returns
        mockEvent.copy(uid = mockInvitation.eventId)
    coEvery { userRepository.getUserById(mockInvitation.from) } returns null

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    val invitationItem = state.items[0] as InvitationCarouselItem
    Assert.assertEquals("Anonymous", invitationItem.invitedBy)
  }

  @Test
  fun refreshEventsLoadsPastEventsCorrectly() = runTest {
    val pastEvent =
        mockEvent.copy(
            start = Timestamp(System.currentTimeMillis() / 1000 - 7200, 0), // 2 hours ago
            end = Timestamp(System.currentTimeMillis() / 1000 - 3600, 0) // 1 hour ago
            )

    coEvery { userRepository.getJoinedEvents(mockUser) } returns listOf(pastEvent.uid)
    coEvery { eventRepository.getEvent(pastEvent.uid) } returns pastEvent
    coEvery { eventRepository.getAllVisibleEvents() } returns emptyList()

    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertFalse(state.isLoading)
    Assert.assertEquals(1, state.items.size)
    Assert.assertTrue(state.items[0] is EventCarouselItem)
  }

  @Test
  fun refreshEventsFiltersOutFutureEventsForPastTab() = runTest {
    val futureEvent = mockEvent.copy(start = Timestamp(System.currentTimeMillis() / 1000 + 3600, 0))

    coEvery { userRepository.getJoinedEvents(mockUser) } returns listOf(futureEvent.uid)
    coEvery { eventRepository.getEvent(futureEvent.uid) } returns futureEvent

    coEvery { eventRepository.getAllVisibleEvents() } returns emptyList()
    viewModel.onTabSelected(EventTab.Past)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertTrue(state.items.isEmpty())
  }

  @Test
  fun refreshEventsHandlesExceptionsGracefully() = runTest {
    coEvery { userRepository.getJoinedEvents(mockUser) } returns listOf("event1", "event2")
    coEvery { eventRepository.getEvent("event1") } throws Exception("Network error")
    coEvery { eventRepository.getEvent("event2") } returns mockEvent.copy(uid = "event2")

    viewModel.onTabSelected(EventTab.Upcoming)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    // Should only contain the successfully loaded event
    Assert.assertEquals(1, state.items.size)
    Assert.assertEquals("event2", state.items[0].uid)
  }

  @Test
  fun acceptInvitationAddsParticipantAndRemovesInvitation() = runTest {
    val invitationItem =
        InvitationCarouselItem(
            invitation = mockInvitation,
            event = mockEvent.copy(uid = mockInvitation.eventId),
            invitedBy = "John")

    // Set initial state with invitation
    coEvery { userRepository.getInvitations(mockUser) } returns listOf(mockInvitation)
    coEvery { eventRepository.getEvent(mockInvitation.eventId) } returns
        mockEvent.copy(uid = mockInvitation.eventId)
    coEvery { userRepository.getUserById(mockInvitation.from) } returns
        mockk { every { firstName } returns "John" }

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    // Accept invitation
    coEvery { userRepository.acceptInvitation(any(), any()) } just Runs
    coEvery { eventRepository.addParticipantToEvent(any(), any()) } just Runs

    viewModel.acceptInvitation(mockInvitation)
    advanceUntilIdle()

    coVerify { userRepository.acceptInvitation(mockInvitation.eventId, mockUser) }
    coVerify {
      eventRepository.addParticipantToEvent(mockInvitation.eventId, EventParticipant(mockUser))
    }

    // Invitation should be removed from items
    val state = viewModel.uiState.value
    Assert.assertTrue(state.items.none { it.uid == mockInvitation.eventId })
  }

  @Test
  fun declineInvitationUpdatesInvitationStatus() = runTest {
    val invitationItem =
        InvitationCarouselItem(
            invitation = mockInvitation,
            event = mockEvent.copy(uid = mockInvitation.eventId),
            invitedBy = "John")

    // Set initial state with invitation
    coEvery { userRepository.getInvitations(mockUser) } returns listOf(mockInvitation)
    coEvery { eventRepository.getEvent(mockInvitation.eventId) } returns
        mockEvent.copy(uid = mockInvitation.eventId)
    coEvery { userRepository.getUserById(mockInvitation.from) } returns
        mockk { every { firstName } returns "John" }

    viewModel.onTabSelected(EventTab.Invitations)
    advanceUntilIdle()

    // Decline invitation
    coEvery { userRepository.declineInvitation(any(), any()) } just Runs

    viewModel.declineInvitation(mockInvitation)
    advanceUntilIdle()

    coVerify { userRepository.declineInvitation(mockInvitation.eventId, mockUser) }

    // Invitation status should be updated
    val state = viewModel.uiState.value
    val updatedItem =
        state.items.find { it.uid == mockInvitation.eventId } as? InvitationCarouselItem
    Assert.assertNotNull(updatedItem)
    Assert.assertEquals(InvitationStatus.Declined, updatedItem?.invitation?.status)
  }

  @Test
  fun multipleEventsAreLoadedCorrectly() = runTest {
    val event1 = mockEvent.copy(uid = "event1", title = "Event 1")
    val event2 = mockEvent.copy(uid = "event2", title = "Event 2")
    val event3 = mockEvent.copy(uid = "event3", title = "Event 3")

    coEvery { userRepository.getJoinedEvents(mockUser) } returns
        listOf(event1.uid, event2.uid, event3.uid)
    coEvery { eventRepository.getEvent(event1.uid) } returns event1
    coEvery { eventRepository.getEvent(event2.uid) } returns event2
    coEvery { eventRepository.getEvent(event3.uid) } returns event3

    viewModel.onTabSelected(EventTab.Upcoming)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    Assert.assertEquals(3, state.items.size)
  }
}

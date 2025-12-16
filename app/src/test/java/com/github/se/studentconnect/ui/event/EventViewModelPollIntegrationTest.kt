package com.github.se.studentconnect.ui.event

import com.github.se.studentconnect.model.authentication.AuthenticationProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.poll.Poll
import com.github.se.studentconnect.model.poll.PollOption
import com.github.se.studentconnect.model.poll.PollRepositoryLocal
import com.github.se.studentconnect.model.user.UserRepositoryLocal
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
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
class EventViewModelPollIntegrationTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: EventViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var pollRepository: PollRepositoryLocal
  private lateinit var friendsRepository: FriendsRepository

  private val testEvent =
      Event.Public(
          uid = "test-event-123",
          title = "Test Event",
          subtitle = "Test Subtitle",
          description = "Test Description",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(0.0, 0.0, "Test Location"),
          website = "https://test.com",
          ownerId = "owner123",
          isFlash = false)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    pollRepository = PollRepositoryLocal()
    friendsRepository = FriendsRepositoryLocal()
    viewModel = EventViewModel(eventRepository, userRepository, pollRepository, friendsRepository)
    AuthenticationProvider.testUserId = "test-user-id"
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    AuthenticationProvider.testUserId = null
  }

  @Test
  fun fetchActivePolls_updatesUiStateWithPolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    val poll1 =
        Poll(
            uid = "poll1",
            eventUid = testEvent.uid,
            question = "Question 1?",
            options = listOf(PollOption("opt1", "Option 1", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    val poll2 =
        Poll(
            uid = "poll2",
            eventUid = testEvent.uid,
            question = "Question 2?",
            options = listOf(PollOption("opt1", "Option A", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    pollRepository.createPoll(poll1)
    pollRepository.createPoll(poll2)

    // Act
    viewModel.fetchActivePolls(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(2, uiState.activePolls.size)
    assertTrue(uiState.activePolls.any { it.uid == "poll1" })
    assertTrue(uiState.activePolls.any { it.uid == "poll2" })
  }

  @Test
  fun fetchActivePolls_onlyReturnsActivePolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    val activePoll =
        Poll(
            uid = "poll1",
            eventUid = testEvent.uid,
            question = "Active Question?",
            options = listOf(PollOption("opt1", "Option 1", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    val inactivePoll =
        Poll(
            uid = "poll2",
            eventUid = testEvent.uid,
            question = "Inactive Question?",
            options = listOf(PollOption("opt1", "Option A", 0)),
            createdAt = Timestamp.now(),
            isActive = false)
    pollRepository.createPoll(activePoll)
    pollRepository.createPoll(inactivePoll)

    // Act
    viewModel.fetchActivePolls(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.activePolls.size)
    assertEquals("poll1", uiState.activePolls[0].uid)
    assertTrue(uiState.activePolls[0].isActive)
  }

  @Test
  fun fetchActivePolls_onlyReturnsEventPolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    val otherEvent = testEvent.copy(uid = "other-event")
    eventRepository.addEvent(otherEvent)

    val eventPoll =
        Poll(
            uid = "poll1",
            eventUid = testEvent.uid,
            question = "Event Question?",
            options = listOf(PollOption("opt1", "Option 1", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    val otherEventPoll =
        Poll(
            uid = "poll2",
            eventUid = otherEvent.uid,
            question = "Other Event Question?",
            options = listOf(PollOption("opt1", "Option A", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    pollRepository.createPoll(eventPoll)
    pollRepository.createPoll(otherEventPoll)

    // Act
    viewModel.fetchActivePolls(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(1, uiState.activePolls.size)
    assertEquals("poll1", uiState.activePolls[0].uid)
    assertEquals(testEvent.uid, uiState.activePolls[0].eventUid)
  }

  @Test
  fun fetchActivePolls_returnsEmptyWhenNoPolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.fetchActivePolls(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.activePolls.isEmpty())
  }

  @Test
  fun fetchActivePolls_doesNotCrashOnError() = runTest {
    // Arrange - use non-existent event
    val nonExistentEventUid = "non-existent-event"

    // Act - should not crash
    viewModel.fetchActivePolls(nonExistentEventUid)
    advanceUntilIdle()

    // Assert - state should still be valid
    val uiState = viewModel.uiState.value
    assertNotNull(uiState)
  }

  @Test
  fun fetchEvent_asJoinedUser_automaticallyFetchesActivePolls() = runTest {
    // Arrange
    val currentUserId = "test-user-id"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    val poll =
        Poll(
            uid = "poll1",
            eventUid = testEvent.uid,
            question = "Test Question?",
            options = listOf(PollOption("opt1", "Option 1", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    pollRepository.createPoll(poll)

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.isJoined)
    assertEquals(1, uiState.activePolls.size)
    assertEquals("poll1", uiState.activePolls[0].uid)
  }

  @Test
  fun joinEvent_automaticallyFetchesActivePolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    val poll =
        Poll(
            uid = "poll1",
            eventUid = testEvent.uid,
            question = "Test Question?",
            options = listOf(PollOption("opt1", "Option 1", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    pollRepository.createPoll(poll)

    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Act
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    viewModel.joinEvent(testEvent.uid, context)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.isJoined)
    assertEquals(1, uiState.activePolls.size)
  }

  @Test
  fun fetchEvent_asNonParticipant_doesNotFetchActivePolls() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    val poll =
        Poll(
            uid = "poll1",
            eventUid = testEvent.uid,
            question = "Test Question?",
            options = listOf(PollOption("opt1", "Option 1", 0)),
            createdAt = Timestamp.now(),
            isActive = true)
    pollRepository.createPoll(poll)

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isJoined)
    assertTrue(uiState.activePolls.isEmpty())
  }

  @Test
  fun pollDialogState_initiallyHidden() {
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showCreatePollDialog)
  }

  @Test
  fun showCreatePollDialog_thenHide_updatesStateCorrectly() {
    // Act - show
    viewModel.showCreatePollDialog()
    var uiState = viewModel.uiState.value
    assertTrue(uiState.showCreatePollDialog)

    // Act - hide
    viewModel.hideCreatePollDialog()
    uiState = viewModel.uiState.value

    // Assert
    assertFalse(uiState.showCreatePollDialog)
  }

  @Test
  fun multiplePollDialogToggles_workCorrectly() {
    // Show, hide, show, hide
    viewModel.showCreatePollDialog()
    assertTrue(viewModel.uiState.value.showCreatePollDialog)

    viewModel.hideCreatePollDialog()
    assertFalse(viewModel.uiState.value.showCreatePollDialog)

    viewModel.showCreatePollDialog()
    assertTrue(viewModel.uiState.value.showCreatePollDialog)

    viewModel.hideCreatePollDialog()
    assertFalse(viewModel.uiState.value.showCreatePollDialog)
  }
}

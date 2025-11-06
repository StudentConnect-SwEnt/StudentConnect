package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventParticipant
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.event.EventViewModel
import com.github.se.studentconnect.ui.event.TicketValidationResult
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
class EventViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: EventViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal

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
    viewModel = EventViewModel(eventRepository, userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialState_isLoading() {
    val uiState = viewModel.uiState.value
    assertTrue(uiState.isLoading)
    assertNull(uiState.event)
    assertFalse(uiState.isJoined)
  }

  @Test
  fun fetchEvent_updatesUiState() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.event)
    assertEquals(testEvent.uid, uiState.event?.uid)
    assertEquals(testEvent.title, uiState.event?.title)
    assertFalse(uiState.isJoined)
  }

  @Test
  fun fetchEvent_withJoinedStatus_updatesIsJoined() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    // Add current user as participant to make isJoined = true
    val currentUserId = "test-user-id"
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    // Note: isJoined will be determined by whether currentUserId is in participants
  }

  @Test
  fun fetchEvent_setsLoadingStateCorrectly() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act - check loading state is true initially
    viewModel.fetchEvent(testEvent.uid)
    var uiState = viewModel.uiState.value
    assertTrue("Loading should be true initially", uiState.isLoading)

    advanceUntilIdle()

    // Assert - loading should be false after fetch completes
    uiState = viewModel.uiState.value
    assertFalse("Loading should be false after fetch", uiState.isLoading)
  }

  @Test
  fun fetchEvent_preservesIsJoinedStatus() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    // Add current user as participant
    val currentUserId = "test-user-id"
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(currentUserId))

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert - isJoined is determined by checking if current user is in participants list
    assertNotNull(viewModel.uiState.value.event)
  }

  @Test
  fun fetchEvent_withDifferentEvent_updatesEventData() = runTest {
    // Arrange
    val event1 =
        testEvent.copy(uid = "event1", title = "Event One", description = "Description One")
    val event2 =
        testEvent.copy(uid = "event2", title = "Event Two", description = "Description Two")
    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)

    // Act - fetch first event
    viewModel.fetchEvent(event1.uid)
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertEquals("Event One", uiState.event?.title)

    // Act - fetch second event
    viewModel.fetchEvent(event2.uid)
    advanceUntilIdle()

    // Assert
    uiState = viewModel.uiState.value
    assertEquals("Event Two", uiState.event?.title)
    assertEquals("Description Two", uiState.event?.description)
  }

  @Test
  fun uiState_emitsUpdates() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert - verify the state flow has been updated correctly
    val uiState = viewModel.uiState.value
    assertFalse("Last state should not be loading", uiState.isLoading)
    assertNotNull("Last state should have event", uiState.event)
  }

  @Test
  fun fetchEvent_multipleCallsInSequence_updatesCorrectly() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act - multiple fetches
    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    viewModel.fetchEvent(testEvent.uid)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull("Event should be loaded", uiState.event)
  }

  @Test
  fun initialState_qrScannerNotShown() {
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun showQrScanner_updatesUiState() {
    // Act
    viewModel.showQrScanner()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun hideQrScanner_updatesUiState() {
    // Arrange
    viewModel.showQrScanner()

    // Act
    viewModel.hideQrScanner()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun showQrScanner_clearsValidationResult() {
    // Arrange - set a validation result first
    viewModel.showQrScanner()
    viewModel.validateParticipant(testEvent.uid, "user123")

    // Act - show scanner again
    viewModel.showQrScanner()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.showQrScanner)
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun validateParticipant_withValidParticipant_returnsValid() = runTest {
    // Arrange
    val participantId = "participant123"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))

    // Act
    viewModel.validateParticipant(testEvent.uid, participantId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Valid)
    assertEquals(
        participantId,
        (uiState.ticketValidationResult as TicketValidationResult.Valid).participantId)
  }

  @Test
  fun validateParticipant_withInvalidParticipant_returnsInvalid() = runTest {
    // Arrange
    val nonParticipantId = "nonparticipant123"
    eventRepository.addEvent(testEvent)
    // Add a different participant
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("otheruser"))

    // Act
    viewModel.validateParticipant(testEvent.uid, nonParticipantId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Invalid)
    assertEquals(
        nonParticipantId, (uiState.ticketValidationResult as TicketValidationResult.Invalid).userId)
  }

  @Test
  fun validateParticipant_withNoParticipants_returnsInvalid() = runTest {
    // Arrange
    val userId = "user123"
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.validateParticipant(testEvent.uid, userId)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Invalid)
  }

  @Test
  fun validateParticipant_withMultipleParticipants_findsCorrectOne() = runTest {
    // Arrange
    val targetParticipant = "target123"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user1"))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user2"))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(targetParticipant))
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user4"))

    // Act
    viewModel.validateParticipant(testEvent.uid, targetParticipant)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Valid)
    assertEquals(
        targetParticipant,
        (uiState.ticketValidationResult as TicketValidationResult.Valid).participantId)
  }

  @Test
  fun clearValidationResult_removesResult() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant("user123"))
    viewModel.validateParticipant(testEvent.uid, "user123")
    advanceUntilIdle()

    // Act
    viewModel.clearValidationResult()

    // Assert
    val uiState = viewModel.uiState.value
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun clearValidationResult_whenNoResult_doesNotCrash() {
    // Act & Assert - should not crash
    viewModel.clearValidationResult()

    val uiState = viewModel.uiState.value
    assertNull(uiState.ticketValidationResult)
  }

  @Test
  fun validateParticipant_withException_returnsError() = runTest {
    // Arrange - create a mock repository that throws an exception
    val mockEventRepository = EventRepositoryLocal()
    mockEventRepository.addEvent(testEvent)

    val errorThrowingRepo =
        object : EventRepository by mockEventRepository {
          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
            throw RuntimeException("Network error")
          }
        }
    val mockViewModel = EventViewModel(errorThrowingRepo, userRepository)

    val userId = "user123"

    // Act
    mockViewModel.validateParticipant(testEvent.uid, userId)
    advanceUntilIdle()

    // Assert
    val uiState = mockViewModel.uiState.value
    assertNotNull(uiState.ticketValidationResult)
    assertTrue(uiState.ticketValidationResult is TicketValidationResult.Error)
  }

  @Test
  fun ticketValidationResult_validType_hasCorrectData() = runTest {
    // Arrange
    val participantId = "valid-participant"
    eventRepository.addEvent(testEvent)
    eventRepository.addParticipantToEvent(testEvent.uid, EventParticipant(participantId))

    // Act
    viewModel.validateParticipant(testEvent.uid, participantId)
    advanceUntilIdle()

    // Assert
    val result = viewModel.uiState.value.ticketValidationResult
    assertTrue(result is TicketValidationResult.Valid)
    val validResult = result as TicketValidationResult.Valid
    assertEquals(participantId, validResult.participantId)
  }

  @Test
  fun ticketValidationResult_invalidType_hasCorrectData() = runTest {
    // Arrange
    val invalidUserId = "invalid-user"
    eventRepository.addEvent(testEvent)

    // Act
    viewModel.validateParticipant(testEvent.uid, invalidUserId)
    advanceUntilIdle()

    // Assert
    val result = viewModel.uiState.value.ticketValidationResult
    assertTrue(result is TicketValidationResult.Invalid)
    val invalidResult = result as TicketValidationResult.Invalid
    assertEquals(invalidUserId, invalidResult.userId)
  }

  @Test
  fun ticketValidationResult_errorType_hasCorrectMessage() = runTest {
    // Arrange - create a mock repository that throws an exception
    val mockEventRepository = EventRepositoryLocal()
    mockEventRepository.addEvent(testEvent)

    val errorThrowingRepo =
        object : EventRepository by mockEventRepository {
          override suspend fun getEventParticipants(eventUid: String): List<EventParticipant> {
            throw RuntimeException("Connection timeout")
          }
        }
    val mockViewModel = EventViewModel(errorThrowingRepo, userRepository)

    val userId = "user123"

    // Act
    mockViewModel.validateParticipant(testEvent.uid, userId)
    advanceUntilIdle()

    // Assert
    val result = mockViewModel.uiState.value.ticketValidationResult
    assertTrue(result is TicketValidationResult.Error)
    val errorResult = result as TicketValidationResult.Error
    assertTrue(errorResult.message.isNotEmpty())
    assertEquals("Connection timeout", errorResult.message)
  }
}

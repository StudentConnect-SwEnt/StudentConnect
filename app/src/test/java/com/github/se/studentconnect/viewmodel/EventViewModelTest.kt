package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
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
    viewModel.fetchEvent(testEvent.uid, isJoined = false)
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

    // Act
    viewModel.fetchEvent(testEvent.uid, isJoined = true)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertTrue(uiState.isJoined)
  }

  @Test
  fun fetchEvent_setsLoadingStateCorrectly() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent)

    // Act - check loading state is true initially
    viewModel.fetchEvent(testEvent.uid, isJoined = false)
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

    // Act
    viewModel.fetchEvent(testEvent.uid, isJoined = true)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue("isJoined should be preserved", uiState.isJoined)
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
    viewModel.fetchEvent(event1.uid, isJoined = false)
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertEquals("Event One", uiState.event?.title)

    // Act - fetch second event
    viewModel.fetchEvent(event2.uid, isJoined = false)
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
    viewModel.fetchEvent(testEvent.uid, isJoined = false)
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
    viewModel.fetchEvent(testEvent.uid, isJoined = false)
    advanceUntilIdle()

    viewModel.fetchEvent(testEvent.uid, isJoined = true)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue("Should be marked as joined", uiState.isJoined)
    assertNotNull("Event should be loaded", uiState.event)
  }
}

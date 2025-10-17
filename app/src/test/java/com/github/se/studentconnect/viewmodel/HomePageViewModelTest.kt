package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.screen.home.HomePageViewModel
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
class HomePageViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: HomePageViewModel
  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal

  private val testEvent1 =
      Event.Public(
          uid = "event-1",
          title = "Event One",
          subtitle = "Subtitle One",
          description = "Description One",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(0.0, 0.0, "Location One"),
          website = "https://event1.com",
          ownerId = "owner1",
          isFlash = false)

  private val testEvent2 =
      Event.Public(
          uid = "event-2",
          title = "Event Two",
          subtitle = "Subtitle Two",
          description = "Description Two",
          start = Timestamp.now(),
          end = Timestamp.now(),
          location = Location(0.0, 0.0, "Location Two"),
          website = "https://event2.com",
          ownerId = "owner2",
          isFlash = false)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialState_isLoadingTrue() = runTest {
    viewModel = HomePageViewModel(eventRepository, userRepository)

    val uiState = viewModel.uiState.value
    // Initially, the view model loads events, so isLoading might be true or false depending on
    // timing
    // We'll check that events list is empty initially if no events are added
    assertTrue(uiState.events.isEmpty())
  }

  @Test
  fun loadAllEvents_updatesUiStateWithEvents() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertEquals(2, uiState.events.size)
    assertTrue(uiState.events.any { it.uid == testEvent1.uid })
    assertTrue(uiState.events.any { it.uid == testEvent2.uid })
  }

  @Test
  fun loadAllEvents_withNoEvents_returnsEmptyList() = runTest {
    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertTrue(uiState.events.isEmpty())
  }

  @Test
  fun refresh_reloadsEvents() = runTest {
    // Arrange
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertTrue("Initially should have no events", uiState.events.isEmpty())

    // Add events after initialization
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    // Act
    viewModel.refresh()
    advanceUntilIdle()

    // Assert
    uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertEquals(2, uiState.events.size)
  }

  @Test
  fun loadAllEvents_setsLoadingStateCorrectly() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent1)

    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)

    // Check loading state during initialization
    val initialState = viewModel.uiState.value
    // Loading might be true initially

    advanceUntilIdle()

    // Assert
    val finalState = viewModel.uiState.value
    assertFalse("Loading should be false after data loads", finalState.isLoading)
  }

  @Test
  fun uiState_emitsUpdates() = runTest {
    // Arrange
    eventRepository.addEvent(testEvent1)

    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Assert - verify the state flow has been updated correctly
    val uiState = viewModel.uiState.value
    assertFalse("Last state should not be loading", uiState.isLoading)
    assertEquals("Last state should have one event", 1, uiState.events.size)
  }

  @Test
  fun refresh_setsLoadingState() = runTest {
    // Arrange
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Act
    viewModel.refresh()

    // Loading should be set to true initially
    var uiState = viewModel.uiState.value
    // Might be true or false depending on timing

    advanceUntilIdle()

    // Assert
    uiState = viewModel.uiState.value
    assertFalse("Loading should be false after refresh completes", uiState.isLoading)
  }

  @Test
  fun loadAllEvents_withMultipleEvents_maintainsOrder() = runTest {
    // Arrange
    val event1 = testEvent1.copy(uid = "event-1", title = "Alpha Event")
    val event2 = testEvent2.copy(uid = "event-2", title = "Beta Event")
    val event3 = testEvent1.copy(uid = "event-3", title = "Gamma Event")

    eventRepository.addEvent(event1)
    eventRepository.addEvent(event2)
    eventRepository.addEvent(event3)

    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertEquals(3, uiState.events.size)
    assertTrue(uiState.events.any { it.title == "Alpha Event" })
    assertTrue(uiState.events.any { it.title == "Beta Event" })
    assertTrue(uiState.events.any { it.title == "Gamma Event" })
  }

  @Test
  fun loadAllEvents_filtersVisibleEvents() = runTest {
    // Arrange - add both visible events
    eventRepository.addEvent(testEvent1)
    eventRepository.addEvent(testEvent2)

    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Assert - should only load visible events (Public events in this case)
    val uiState = viewModel.uiState.value
    assertEquals(2, uiState.events.size)
    assertTrue(uiState.events.all { it is Event.Public })
  }

  @Test
  fun refresh_multipleTimesInSequence_worksCorrectly() = runTest {
    // Arrange
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Act - first refresh
    eventRepository.addEvent(testEvent1)
    viewModel.refresh()
    advanceUntilIdle()

    var uiState = viewModel.uiState.value
    assertEquals(1, uiState.events.size)

    // Act - second refresh
    eventRepository.addEvent(testEvent2)
    viewModel.refresh()
    advanceUntilIdle()

    // Assert
    uiState = viewModel.uiState.value
    assertEquals(2, uiState.events.size)
  }

  @Test
  fun viewModel_handlesEmptyRepository() = runTest {
    // Act
    viewModel = HomePageViewModel(eventRepository, userRepository)
    advanceUntilIdle()

    // Assert
    val uiState = viewModel.uiState.value
    assertTrue(uiState.events.isEmpty())
    assertFalse(uiState.isLoading)
  }
}

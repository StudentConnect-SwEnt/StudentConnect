package com.github.se.studentconnect.viewmodel

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepositoryLocal
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.UserRepositoryLocal
import com.github.se.studentconnect.ui.screen.home.HomePageViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomePageViewModelCalendarTest {

  private lateinit var eventRepository: EventRepositoryLocal
  private lateinit var userRepository: UserRepositoryLocal
  private lateinit var viewModel: HomePageViewModel
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    eventRepository = EventRepositoryLocal()
    userRepository = UserRepositoryLocal()
    viewModel = HomePageViewModel(eventRepository, userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun showCalendar_setsCalendarVisibleToTrue() = runTest {
    // Initially calendar should not be visible
    assertFalse(viewModel.uiState.value.isCalendarVisible)

    // Show calendar
    viewModel.showCalendar()

    // Calendar should now be visible
    assertTrue(viewModel.uiState.value.isCalendarVisible)
  }

  @Test
  fun hideCalendar_setsCalendarVisibleToFalse() = runTest {
    // First show calendar
    viewModel.showCalendar()
    assertTrue(viewModel.uiState.value.isCalendarVisible)

    // Hide calendar
    viewModel.hideCalendar()

    // Calendar should now be hidden
    assertFalse(viewModel.uiState.value.isCalendarVisible)
  }

  @Test
  fun onDateSelected_setsSelectedDateAndScrollTarget() = runTest {
    val testDate = Date()
    
    // Initially no date should be selected
    assertNull(viewModel.uiState.value.selectedDate)
    assertNull(viewModel.uiState.value.scrollToDate)

    // Select a date
    viewModel.onDateSelected(testDate)

    // Selected date and scroll target should be set
    assertEquals(testDate, viewModel.uiState.value.selectedDate)
    assertEquals(testDate, viewModel.uiState.value.scrollToDate)
    
    // Calendar should be hidden after selection
    assertFalse(viewModel.uiState.value.isCalendarVisible)
  }

  @Test
  fun clearScrollTarget_clearsScrollToDate() = runTest {
    val testDate = Date()
    
    // Set a scroll target
    viewModel.onDateSelected(testDate)
    assertEquals(testDate, viewModel.uiState.value.scrollToDate)

    // Clear scroll target
    viewModel.clearScrollTarget()

    // Scroll target should be cleared
    assertNull(viewModel.uiState.value.scrollToDate)
  }

  @Test
  fun getEventsForDate_withNoEvents_returnsEmptyList() = runTest {
    val testDate = Date()
    
    // Get events for a date with no events
    val eventsForDate = viewModel.getEventsForDate(testDate)

    // Should return empty list
    assertTrue(eventsForDate.isEmpty())
  }

  @Test
  fun calendarStateManagement_worksCorrectly() = runTest {
    val testDate = Date()
    
    // Test complete calendar workflow
    assertFalse(viewModel.uiState.value.isCalendarVisible)
    assertNull(viewModel.uiState.value.selectedDate)
    assertNull(viewModel.uiState.value.scrollToDate)

    // Show calendar
    viewModel.showCalendar()
    assertTrue(viewModel.uiState.value.isCalendarVisible)

    // Select date
    viewModel.onDateSelected(testDate)
    assertFalse(viewModel.uiState.value.isCalendarVisible)
    assertEquals(testDate, viewModel.uiState.value.selectedDate)
    assertEquals(testDate, viewModel.uiState.value.scrollToDate)

    // Clear scroll target
    viewModel.clearScrollTarget()
    assertNull(viewModel.uiState.value.scrollToDate)
    assertEquals(testDate, viewModel.uiState.value.selectedDate) // Should still be set
  }
}

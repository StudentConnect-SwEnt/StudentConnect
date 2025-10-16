package com.github.se.studentconnect.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class HomePageUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val isCalendarVisible: Boolean = false,
    val selectedDate: Date? = null,
    val scrollToDate: Date? = null,
)

class HomePageViewModel
@Inject
constructor(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    // maybe will be used after for recommendations
    private val userRepositoryLocal: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(HomePageUiState())
  val uiState: StateFlow<HomePageUiState> = _uiState.asStateFlow()

  init {
    loadAllEvents()
  }

  private fun loadAllEvents() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      val allEvents = eventRepository.getAllVisibleEvents()
      _uiState.update { it.copy(events = allEvents, isLoading = false) }
    }
  }

  fun refresh() {
    loadAllEvents()
  }

  /**
   * Shows the calendar modal.
   */
  fun showCalendar() {
    _uiState.update { it.copy(isCalendarVisible = true) }
  }

  /**
   * Hides the calendar modal.
   */
  fun hideCalendar() {
    _uiState.update { it.copy(isCalendarVisible = false) }
  }

  /**
   * Handles date selection from the calendar.
   * Closes the calendar and sets the scroll target date.
   */
  fun onDateSelected(date: Date) {
    _uiState.update { 
      it.copy(
        selectedDate = date,
        scrollToDate = date,
        isCalendarVisible = false
      ) 
    }
  }

  /**
   * Clears the scroll target date after scrolling is complete.
   */
  fun clearScrollTarget() {
    _uiState.update { it.copy(scrollToDate = null) }
  }

  /**
   * Gets events for a specific date.
   */
  fun getEventsForDate(date: Date): List<Event> {
    val calendar = Calendar.getInstance()
    calendar.time = date
    
    return _uiState.value.events.filter { event ->
      val eventCalendar = Calendar.getInstance()
      eventCalendar.time = event.start.toDate()
      
      eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
      eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
      eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
    }
  }
}

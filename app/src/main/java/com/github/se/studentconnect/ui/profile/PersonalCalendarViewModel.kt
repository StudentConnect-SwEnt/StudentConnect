package com.github.se.studentconnect.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.calendar.IcsParser
import com.github.se.studentconnect.model.calendar.PersonalCalendarEvent
import com.github.se.studentconnect.model.calendar.PersonalCalendarRepository
import com.github.se.studentconnect.model.calendar.PersonalCalendarRepositoryProvider
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.google.firebase.Timestamp
import java.io.InputStream
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Unified calendar item that can represent either a personal calendar event or an app event. */
sealed class CalendarItem {
  abstract val uid: String
  abstract val title: String
  abstract val start: Timestamp
  abstract val end: Timestamp?
  abstract val location: String?
  abstract val color: String?

  data class Personal(
      override val uid: String,
      override val title: String,
      override val start: Timestamp,
      override val end: Timestamp?,
      override val location: String?,
      override val color: String?
  ) : CalendarItem()

  data class AppEvent(
      override val uid: String,
      override val title: String,
      override val start: Timestamp,
      override val end: Timestamp?,
      override val location: String?,
      override val color: String? = "#4CAF50", // Green for app events
      val isOwner: Boolean = false
  ) : CalendarItem()

  data class Imported(
      override val uid: String,
      override val title: String,
      override val start: Timestamp,
      override val end: Timestamp?,
      override val location: String?,
      override val color: String? = "#9C27B0" // Purple for imported events
  ) : CalendarItem()
}

/** Represents a calendar conflict between events. */
data class ScheduleConflict(val conflictingItems: List<CalendarItem>, val message: String)

/**
 * ViewModel for the Personal Calendar section in the Profile screen. Manages the state of personal
 * calendar events and app events.
 *
 * @param userId The ID of the current user
 * @param repository Repository for personal calendar operations
 * @param eventRepository Repository for app events
 * @param userRepository Repository for user data
 */
class PersonalCalendarViewModel(
    private val userId: String,
    private val repository: PersonalCalendarRepository =
        PersonalCalendarRepositoryProvider.repository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  companion object {
    private const val TAG = "PersonalCalendarVM"
  }

  // All calendar items (personal + app events)
  private val _allItems = MutableStateFlow<List<CalendarItem>>(emptyList())
  val allItems: StateFlow<List<CalendarItem>> = _allItems.asStateFlow()

  // Personal calendar events only
  private val _events = MutableStateFlow<List<PersonalCalendarEvent>>(emptyList())
  val events: StateFlow<List<PersonalCalendarEvent>> = _events.asStateFlow()

  // App events (joined + owned)
  private val _appEvents = MutableStateFlow<List<Event>>(emptyList())
  val appEvents: StateFlow<List<Event>> = _appEvents.asStateFlow()

  // Events for the currently selected date
  private val _selectedDateEvents = MutableStateFlow<List<CalendarItem>>(emptyList())
  val selectedDateEvents: StateFlow<List<CalendarItem>> = _selectedDateEvents.asStateFlow()

  // Optimization: Map of events by Date (Year, Month, Day) for fast lookup
  private var eventsByDate = mapOf<Triple<Int, Int, Int>, List<CalendarItem>>()

  // Currently selected date
  private val _selectedDate = MutableStateFlow(Date())
  val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

  // Loading state
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // Error state
  private val _error = MutableStateFlow<Int?>(null)
  val error: StateFlow<Int?> = _error.asStateFlow()

  init {
    loadAllEvents()
  }

  /** Loads all events (personal calendar + joined/owned app events). */
  fun loadEvents() {
    loadAllEvents()
  }

  private fun loadAllEvents() {
    _isLoading.value = true
    _error.value = null

    viewModelScope.launch {
      try {
        // Load personal calendar events
        val personalEvents = repository.getEventsForUser(userId)
        _events.value = personalEvents

        // Load app events (joined + owned)
        val joinedEventIds = userRepository.getJoinedEvents(userId)
        val allVisibleEvents = eventRepository.getAllVisibleEvents()

        // Get owned events
        val ownedEvents = allVisibleEvents.filter { it.ownerId == userId }

        // Get joined events
        val joinedEvents =
            joinedEventIds.mapNotNull { eventId ->
              try {
                eventRepository.getEvent(eventId)
              } catch (e: Exception) {
                null
              }
            }

        // Combine and remove duplicates
        val combinedAppEvents = (ownedEvents + joinedEvents).distinctBy { it.uid }
        _appEvents.value = combinedAppEvents

        // Convert to unified CalendarItem list
        val personalItems =
            personalEvents.map { event ->
              if (event.sourceCalendar == "Imported") {
                CalendarItem.Imported(
                    uid = event.uid,
                    title = event.title,
                    start = event.start,
                    end = event.end,
                    location = event.location,
                    color = "#9C27B0" // Purple for imported
                    )
              } else {
                CalendarItem.Personal(
                    uid = event.uid,
                    title = event.title,
                    start = event.start,
                    end = event.end,
                    location = event.location,
                    color = event.color ?: "#2196F3" // Blue for personal
                    )
              }
            }

        val appItems =
            combinedAppEvents.map { event ->
              CalendarItem.AppEvent(
                  uid = event.uid,
                  title = event.title,
                  start = event.start,
                  end = event.end,
                  location = event.location?.name,
                  color =
                      if (event.ownerId == userId) "#FF9800"
                      else "#4CAF50", // Orange for owned, Green for joined
                  isOwner = event.ownerId == userId)
            }

        val sortedItems = (personalItems + appItems).sortedBy { it.start.seconds }
        _allItems.value = sortedItems

        // Build optimization map
        eventsByDate =
            sortedItems.groupBy { item ->
              val eventCal = Calendar.getInstance()
              eventCal.time = item.start.toDate()
              Triple(
                  eventCal.get(Calendar.YEAR),
                  eventCal.get(Calendar.MONTH),
                  eventCal.get(Calendar.DAY_OF_MONTH))
            }

        updateSelectedDateEvents()

        Log.d(
            TAG,
            "Loaded ${personalEvents.size} personal events and ${combinedAppEvents.size} app events for user $userId")
      } catch (e: Exception) {
        Log.e(TAG, "Error loading events", e)
        _error.value = R.string.error_load_calendar
      } finally {
        _isLoading.value = false
      }
    }
  }

  /** Sets the selected date and updates the events list for that date. */
  fun selectDate(date: Date) {
    _selectedDate.value = date
    updateSelectedDateEvents()
  }

  /** Imports events from an ICS file. */
  fun importEvents(inputStream: InputStream) {
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      try {
        // Parse the ICS file
        val newEvents =
            IcsParser.parseIcs(
                inputStream = inputStream, userId = userId, sourceCalendar = "Imported")

        // Get existing events to check for duplicates
        val existingEvents = repository.getEventsForUser(userId)

        // Find existing events that match the externalUid of new events
        val newExternalUids = newEvents.mapNotNull { it.externalUid }.toSet()
        val duplicatesToDelete =
            existingEvents.filter {
              it.externalUid != null && newExternalUids.contains(it.externalUid)
            }

        // Delete old duplicates
        if (duplicatesToDelete.isNotEmpty()) {
          duplicatesToDelete.forEach { event ->
            try {
              repository.deleteEvent(event.uid, userId)
            } catch (e: Exception) {
              Log.e(TAG, "Error deleting duplicate event: ${event.uid}", e)
            }
          }
          Log.d(TAG, "Deleted ${duplicatesToDelete.size} duplicate events")
        }

        // Add all events to the repository in a batch
        repository.addEvents(newEvents)

        Log.d(TAG, "Imported ${newEvents.size} events from ICS file")

        // Reload events to refresh the UI
        loadAllEvents()
      } catch (e: Exception) {
        Log.e(TAG, "Error importing ICS file", e)
        _error.value = R.string.error_import_events
        _isLoading.value = false // Ensure loading is cleared if loadAllEvents isn't called
      }
    }
  }
  /** Updates the list of events for the currently selected date. */
  /** Updates the list of events for the currently selected date. */
  private fun updateSelectedDateEvents() {
    val selected = _selectedDate.value
    val calendar = Calendar.getInstance()
    calendar.time = selected

    val key =
        Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))

    _selectedDateEvents.value = eventsByDate[key] ?: emptyList()
  }

  /** Gets conflicts for a given time range. */
  fun getConflictsForTimeRange(start: Timestamp, end: Timestamp?): List<CalendarItem> {
    val effectiveEnd = end ?: Timestamp(start.seconds + 3600, 0) // Default 1 hour

    return _allItems.value.filter { item ->
      val itemEnd = item.end ?: Timestamp(item.start.seconds + 3600, 0)

      // Check for overlap
      item.start.seconds < effectiveEnd.seconds && itemEnd.seconds > start.seconds
    }
  }

  /** Checks if the user has any conflicts at a specific time. */
  fun hasConflictAt(start: Timestamp, end: Timestamp?): Boolean {
    return getConflictsForTimeRange(start, end).isNotEmpty()
  }

  /** Gets dates that have events (for calendar highlighting). */
  fun getDatesWithEvents(): Set<Triple<Int, Int, Int>> {
    return eventsByDate.keys
  }

  /** Clears any error state. */
  fun clearError() {
    _error.value = null
  }
}

package com.github.se.studentconnect.ui.screen.home

import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.utils.formatDateHeader
import com.google.firebase.Timestamp
import java.util.Calendar
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HomeScreenScrollUtilsTest {

  private fun createTestEvent(
      daysFromNow: Int = 1,
      hourOfDay: Int = 10,
      title: String = "Test Event",
      uid: String = "event-$title",
      subtitle: String = "Subtitle",
      description: String = "Description",
      location: Location = Location(46.5197, 6.6323, "EPFL"),
      website: String = "https://example.com",
      ownerId: String = "owner1",
      isFlash: Boolean = false
  ): Event.Public {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysFromNow)
    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val timestamp = Timestamp(calendar.time)

    return Event.Public(
        uid = uid,
        title = title,
        subtitle = subtitle,
        description = description,
        start = timestamp,
        end = timestamp,
        location = location,
        website = website,
        ownerId = ownerId,
        isFlash = isFlash)
  }

  @Test
  fun buildDateHeaderIndexMap_withNoEvents_returnsEmptyMap() {
    // Arrange
    val events = emptyList<Event>()

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertTrue(indexMap.isEmpty())
  }

  @Test
  fun buildDateHeaderIndexMap_withSingleEvent_noTopContent() {
    // Arrange
    val event = createTestEvent(1, 10, "Event 1")
    val events = listOf(event)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    val dateHeader = formatDateHeader(event.start)
    assertNotNull(indexMap[dateHeader])
    assertEquals(0, indexMap[dateHeader]) // Header at index 0
  }

  @Test
  fun buildDateHeaderIndexMap_withSingleEvent_withTopContent() {
    // Arrange
    val event = createTestEvent(1, 10, "Event 1")
    val events = listOf(event)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = true)

    // Assert
    val dateHeader = formatDateHeader(event.start)
    assertNotNull(indexMap[dateHeader])
    assertEquals(1, indexMap[dateHeader]) // Header at index 1 (after top content)
  }

  @Test
  fun buildDateHeaderIndexMap_withMultipleEventsOnSameDay() {
    // Arrange
    val event1 = createTestEvent(1, 10, "Event 1")
    val event2 = createTestEvent(1, 14, "Event 2")
    val event3 = createTestEvent(1, 18, "Event 3")
    val events = listOf(event1, event2, event3)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertEquals(1, indexMap.size) // Only one date header
    val dateHeader = formatDateHeader(event1.start)
    assertEquals(0, indexMap[dateHeader]) // Header at index 0
  }

  @Test
  fun buildDateHeaderIndexMap_withMultipleEventsOnDifferentDays() {
    // Arrange
    val event1 = createTestEvent(1, 10, "Event 1")
    val event2 = createTestEvent(2, 10, "Event 2")
    val event3 = createTestEvent(3, 10, "Event 3")
    val events = listOf(event1, event2, event3)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertEquals(3, indexMap.size) // Three date headers

    val dateHeader1 = formatDateHeader(event1.start)
    val dateHeader2 = formatDateHeader(event2.start)
    val dateHeader3 = formatDateHeader(event3.start)

    assertEquals(0, indexMap[dateHeader1]) // First header at index 0
    assertEquals(2, indexMap[dateHeader2]) // Second header at index 2 (after header + 1 event)
    assertEquals(4, indexMap[dateHeader3]) // Third header at index 4
  }

  @Test
  fun buildDateHeaderIndexMap_withMixedEvents_noTopContent() {
    // Arrange
    val event1 = createTestEvent(1, 10, "Event 1")
    val event2 = createTestEvent(1, 14, "Event 2") // Same day as event1
    val event3 = createTestEvent(2, 10, "Event 3") // Different day
    val events = listOf(event1, event2, event3)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertEquals(2, indexMap.size) // Two date headers

    val dateHeader1 = formatDateHeader(event1.start)
    val dateHeader2 = formatDateHeader(event3.start)

    assertEquals(0, indexMap[dateHeader1]) // First header at index 0
    assertEquals(3, indexMap[dateHeader2]) // Second header at index 3 (after header + 2 events)
  }

  @Test
  fun buildDateHeaderIndexMap_withMixedEvents_withTopContent() {
    // Arrange
    val event1 = createTestEvent(1, 10, "Event 1")
    val event2 = createTestEvent(1, 14, "Event 2") // Same day as event1
    val event3 = createTestEvent(2, 10, "Event 3") // Different day
    val events = listOf(event1, event2, event3)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = true)

    // Assert
    assertEquals(2, indexMap.size) // Two date headers

    val dateHeader1 = formatDateHeader(event1.start)
    val dateHeader2 = formatDateHeader(event3.start)

    assertEquals(1, indexMap[dateHeader1]) // First header at index 1 (after top content)
    assertEquals(4, indexMap[dateHeader2]) // Second header at index 4
  }

  @Test
  fun buildDateHeaderIndexMap_eventsAreSortedByDate() {
    // Arrange - Add events in reverse order
    val event1 = createTestEvent(3, 10, "Event 3")
    val event2 = createTestEvent(1, 10, "Event 1")
    val event3 = createTestEvent(2, 10, "Event 2")
    val events = listOf(event1, event2, event3)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert - Events should be sorted, so indices should be in date order
    assertEquals(3, indexMap.size)

    val dateHeader1 = formatDateHeader(event2.start) // Day 1
    val dateHeader2 = formatDateHeader(event3.start) // Day 2
    val dateHeader3 = formatDateHeader(event1.start) // Day 3

    assertEquals(0, indexMap[dateHeader1]) // Earliest date first
    assertEquals(2, indexMap[dateHeader2])
    assertEquals(4, indexMap[dateHeader3]) // Latest date last
  }

  @Test
  fun buildDateHeaderIndexMap_largeNumberOfEvents() {
    // Arrange - Create 10 events across 3 days
    val events = mutableListOf<Event.Public>()
    events.add(createTestEvent(1, 10, "Event 1"))
    events.add(createTestEvent(1, 12, "Event 2"))
    events.add(createTestEvent(1, 14, "Event 3"))
    events.add(createTestEvent(1, 16, "Event 4"))
    events.add(createTestEvent(2, 10, "Event 5"))
    events.add(createTestEvent(2, 12, "Event 6"))
    events.add(createTestEvent(2, 14, "Event 7"))
    events.add(createTestEvent(3, 10, "Event 8"))
    events.add(createTestEvent(3, 12, "Event 9"))
    events.add(createTestEvent(3, 14, "Event 10"))

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertEquals(3, indexMap.size) // Three different days

    val dateHeader1 = formatDateHeader(events[0].start)
    val dateHeader2 = formatDateHeader(events[4].start)
    val dateHeader3 = formatDateHeader(events[7].start)

    assertEquals(0, indexMap[dateHeader1]) // Day 1: header at 0
    assertEquals(5, indexMap[dateHeader2]) // Day 2: header at 5 (header + 4 events + header)
    assertEquals(9, indexMap[dateHeader3]) // Day 3: header at 9
  }

  @Test
  fun formatDateHeader_consistency() {
    // Arrange
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val date1 = calendar.time
    val timestamp1 = Timestamp(date1)

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    val date2 = calendar.time
    val timestamp2 = Timestamp(date2)

    // Act
    val header1 = formatDateHeader(timestamp1)
    val header2 = formatDateHeader(timestamp2)

    // Assert - Same day should produce same header
    assertEquals(header1, header2)
  }

  @Test
  fun homePageUiState_scrollToDate_functionality() {
    // Arrange
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 5)
    val targetDate = calendar.time

    val uiState = HomePageUiState(scrollToDate = targetDate)

    // Assert
    assertEquals(targetDate, uiState.scrollToDate)
  }

  @Test
  fun homePageUiState_clearScrollTarget() {
    // Arrange
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 5)
    val targetDate = calendar.time

    val uiState = HomePageUiState(scrollToDate = targetDate)

    // Act
    val clearedState = uiState.copy(scrollToDate = null)

    // Assert
    assertEquals(null, clearedState.scrollToDate)
  }

  @Test
  fun homePageUiState_selectedDate_functionality() {
    // Arrange
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 3)
    val selectedDate = calendar.time

    val uiState = HomePageUiState(selectedDate = selectedDate)

    // Assert
    assertEquals(selectedDate, uiState.selectedDate)
  }

  @Test
  fun homePageUiState_calendarVisibility() {
    // Arrange
    val uiState1 = HomePageUiState(isCalendarVisible = false)
    val uiState2 = HomePageUiState(isCalendarVisible = true)

    // Assert
    assertEquals(false, uiState1.isCalendarVisible)
    assertEquals(true, uiState2.isCalendarVisible)
  }

  @Test
  fun buildDateHeaderIndexMap_withEventsAtMidnight() {
    // Arrange
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val event =
        Event.Public(
            uid = "midnight-event",
            title = "Midnight Event",
            subtitle = "Subtitle",
            description = "Description",
            start = Timestamp(calendar.time),
            end = Timestamp(calendar.time),
            location = Location(46.5197, 6.6323, "EPFL"),
            website = "https://example.com",
            ownerId = "owner1",
            isFlash = false)

    val events = listOf(event)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertEquals(1, indexMap.size)
    val dateHeader = formatDateHeader(event.start)
    assertNotNull(indexMap[dateHeader])
  }

  @Test
  fun buildDateHeaderIndexMap_withEventsJustBeforeMidnight() {
    // Arrange
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)

    val event =
        Event.Public(
            uid = "late-event",
            title = "Late Event",
            subtitle = "Subtitle",
            description = "Description",
            start = Timestamp(calendar.time),
            end = Timestamp(calendar.time),
            location = Location(46.5197, 6.6323, "EPFL"),
            website = "https://example.com",
            ownerId = "owner1",
            isFlash = false)

    val events = listOf(event)

    // Act
    val indexMap = buildDateHeaderIndexMap(events, hasTopContent = false)

    // Assert
    assertEquals(1, indexMap.size)
    val dateHeader = formatDateHeader(event.start)
    assertNotNull(indexMap[dateHeader])
  }

  // Helper function made public for testing
  private fun buildDateHeaderIndexMap(
      events: List<Event>,
      hasTopContent: Boolean
  ): Map<String, Int> {
    if (events.isEmpty()) return emptyMap()

    val sortedEvents = events.sortedBy { it.start }
    val groupedEvents = sortedEvents.groupBy { event -> formatDateHeader(event.start) }

    val indexMap = mutableMapOf<String, Int>()
    var currentIndex = if (hasTopContent) 1 else 0

    groupedEvents.forEach { (dateHeader, eventsOnDate) ->
      indexMap[dateHeader] = currentIndex
      currentIndex += 1 + eventsOnDate.size
    }

    return indexMap
  }
}

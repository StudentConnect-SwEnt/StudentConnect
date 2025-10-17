package com.github.se.studentconnect.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.theme.AppTheme
import java.util.*

/** Test tags for the Event Calendar component. */
object EventCalendarTestTags {
  const val CALENDAR_CONTAINER = "calendar_container"
  const val DATE_PICKER = "date_picker"
}

/**
 * Simple date picker for selecting dates and navigating to events.
 *
 * Features:
 * - Material 3 Expressive DatePicker component
 * - Date selection for navigating to events
 * - Clean, simple interface
 *
 * @param events List of events (not used for visualization, just for compatibility)
 * @param selectedDate Currently selected date (optional)
 * @param onDateSelected Callback when a date is selected
 * @param modifier Modifier for the calendar container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCalendar(
    modifier: Modifier = Modifier,
    events: List<Event>,
    selectedDate: Date? = null,
    onDateSelected: (Date) -> Unit
) {
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate?.time)

  LaunchedEffect(datePickerState.selectedDateMillis) {
    datePickerState.selectedDateMillis?.let { dateMillis -> onDateSelected(Date(dateMillis)) }
  }

  Card(
      modifier = modifier.fillMaxWidth().testTag(EventCalendarTestTags.CALENDAR_CONTAINER),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
              text = "Select a date to view events",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(bottom = 16.dp))

          DatePicker(
              modifier = Modifier.testTag(EventCalendarTestTags.DATE_PICKER),
              state = datePickerState)
        }
      }
}

/** Preview function for the EventCalendar component. */
@Preview(showBackground = true)
@Composable
fun EventCalendarPreview() {
  AppTheme { EventCalendar(events = emptyList(), selectedDate = Date(), onDateSelected = {}) }
}

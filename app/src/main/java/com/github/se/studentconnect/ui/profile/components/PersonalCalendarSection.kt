package com.github.se.studentconnect.ui.profile.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.ui.profile.CalendarItem
import com.github.se.studentconnect.ui.profile.PersonalCalendarViewModel
import java.text.SimpleDateFormat
import java.util.*

/** Test tags for automated testing of personal calendar section components. */
object PersonalCalendarSectionTestTags {
  const val SECTION = "personal_calendar_section"
  const val TITLE = "personal_calendar_title"
  const val CALENDAR_GRID = "personal_calendar_grid"
  const val EVENTS_LIST = "personal_calendar_events_list"
  const val EMPTY_STATE = "personal_calendar_empty_state"
  const val MONTH_NAVIGATION = "personal_calendar_month_navigation"
  const val PREV_MONTH_BUTTON = "personal_calendar_prev_month"
  const val NEXT_MONTH_BUTTON = "personal_calendar_next_month"
  const val MONTH_YEAR_TEXT = "personal_calendar_month_year"
  const val VIEW_MODE_TOGGLE = "personal_calendar_view_mode_toggle"
  const val WEEK_VIEW = "personal_calendar_week_view"

  fun dayCell(day: Int) = "personal_calendar_day_$day"

  fun eventItem(eventUid: String) = "personal_calendar_event_$eventUid"
}

/**
 * Personal Calendar Section displaying a monthly or weekly calendar view with events.
 *
 * @param viewModel ViewModel for personal calendar data
 * @param modifier Modifier to be applied to the section
 * @param onEventClick Callback invoked when an event is clicked
 */
@Composable
fun PersonalCalendarSection(
    viewModel: PersonalCalendarViewModel,
    modifier: Modifier = Modifier,
    onEventClick: ((CalendarItem) -> Unit)? = null
) {
  val allItems by viewModel.allItems.collectAsState()
  val selectedDate by viewModel.selectedDate.collectAsState()
  val selectedDateEvents by viewModel.selectedDateEvents.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  val context = LocalContext.current
  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
          val inputStream = context.contentResolver.openInputStream(it)
          if (inputStream != null) {
            viewModel.importEvents(inputStream)
          }
        }
      }

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  val sectionVerticalPadding = screenWidth * 0.04f
  val titleHorizontalPadding = screenWidth * 0.04f
  val titleBottomSpacing = screenWidth * 0.03f

  var displayedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
  var displayedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
  var weekStartDate by remember { mutableStateOf(getWeekStartDate(Date())) }

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = sectionVerticalPadding)
              .testTag(PersonalCalendarSectionTestTags.SECTION)) {
        // Header with title and view mode toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = titleHorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "My Calendar",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag(PersonalCalendarSectionTestTags.TITLE))

              // Import Button
              IconButton(
                  onClick = { launcher.launch("text/calendar") },
                  modifier = Modifier.testTag("import_ics_button")) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Import Schedule",
                        tint = MaterialTheme.colorScheme.primary)
                  }
            }

        Spacer(modifier = Modifier.height(titleBottomSpacing))

        // Calendar Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = titleHorizontalPadding),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
              Column(modifier = Modifier.padding(16.dp)) {
                if (isLoading) {
                  Box(
                      modifier = Modifier.fillMaxWidth().height(300.dp),
                      contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                      }
                } else {
                  MonthView(
                      month = displayedMonth,
                      year = displayedYear,
                      selectedDate = selectedDate,
                      items = allItems,
                      onDateSelected = { viewModel.selectDate(it) },
                      onPreviousMonth = {
                        if (displayedMonth == 0) {
                          displayedMonth = 11
                          displayedYear--
                        } else {
                          displayedMonth--
                        }
                      },
                      onNextMonth = {
                        if (displayedMonth == 11) {
                          displayedMonth = 0
                          displayedYear++
                        } else {
                          displayedMonth++
                        }
                      })
                }
              }
            }

        Spacer(modifier = Modifier.height(16.dp))
        SelectedDateEventsSection(
            selectedDate = selectedDate,
            events = selectedDateEvents,
            onEventClick = onEventClick,
            modifier = Modifier.padding(horizontal = titleHorizontalPadding))

        // Legend
        Spacer(modifier = Modifier.height(12.dp))
        CalendarLegend(modifier = Modifier.padding(horizontal = titleHorizontalPadding))
      }
}

@Composable
private fun CalendarLegend(modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
    LegendItem(color = Color(0xFF4CAF50), label = "Joined")
    LegendItem(color = Color(0xFFFF9800), label = "Created")
    LegendItem(color = Color(0xFF9C27B0), label = "Imported")
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

// ==================== MONTH VIEW ====================

@Composable
private fun MonthView(
    month: Int,
    year: Int,
    selectedDate: Date,
    items: List<CalendarItem>,
    onDateSelected: (Date) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
  Column {
    MonthNavigationHeader(
        month = month, year = year, onPreviousMonth = onPreviousMonth, onNextMonth = onNextMonth)
    Spacer(modifier = Modifier.height(12.dp))
    DayOfWeekHeaders()
    Spacer(modifier = Modifier.height(8.dp))
    CalendarGrid(
        month = month,
        year = year,
        selectedDate = selectedDate,
        items = items,
        onDateSelected = onDateSelected)
  }
}

@Composable
private fun MonthNavigationHeader(
    month: Int,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
  val calendar = Calendar.getInstance()
  calendar.set(Calendar.MONTH, month)
  calendar.set(Calendar.YEAR, year)

  val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  val monthYearText = monthFormat.format(calendar.time)

  Row(
      modifier = Modifier.fillMaxWidth().testTag(PersonalCalendarSectionTestTags.MONTH_NAVIGATION),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.testTag(PersonalCalendarSectionTestTags.PREV_MONTH_BUTTON)) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = "Previous month")
            }

        Text(
            text = monthYearText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag(PersonalCalendarSectionTestTags.MONTH_YEAR_TEXT))

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.testTag(PersonalCalendarSectionTestTags.NEXT_MONTH_BUTTON)) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = "Next month")
            }
      }
}

@Composable
private fun DayOfWeekHeaders() {
  val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    daysOfWeek.forEach { day ->
      Text(
          text = day,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center)
    }
  }
}

@Composable
private fun CalendarGrid(
    month: Int,
    year: Int,
    selectedDate: Date,
    items: List<CalendarItem>,
    onDateSelected: (Date) -> Unit
) {
  val calendar = Calendar.getInstance()
  calendar.set(year, month, 1)

  val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
  val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

  val days = mutableListOf<Int?>()
  repeat(firstDayOfWeek) { days.add(null) }
  for (day in 1..daysInMonth) {
    days.add(day)
  }
  while (days.size % 7 != 0) {
    days.add(null)
  }

  val selectedCalendar = Calendar.getInstance()
  selectedCalendar.time = selectedDate
  val selectedDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)
  val selectedMonth = selectedCalendar.get(Calendar.MONTH)
  val selectedYear = selectedCalendar.get(Calendar.YEAR)

  // Get dates with events and their colors
  val datesWithEvents =
      items.groupBy { item ->
        val eventCal = Calendar.getInstance()
        eventCal.time = item.start.toDate()
        Triple(
            eventCal.get(Calendar.DAY_OF_MONTH),
            eventCal.get(Calendar.MONTH),
            eventCal.get(Calendar.YEAR))
      }

  val todayCalendar = Calendar.getInstance()
  val today = todayCalendar.get(Calendar.DAY_OF_MONTH)
  val todayMonth = todayCalendar.get(Calendar.MONTH)
  val todayYear = todayCalendar.get(Calendar.YEAR)

  Column(
      modifier = Modifier.fillMaxWidth().testTag(PersonalCalendarSectionTestTags.CALENDAR_GRID)) {
        days.chunked(7).forEach { week ->
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            week.forEach { day ->
              if (day != null) {
                val isSelected =
                    day == selectedDay && month == selectedMonth && year == selectedYear
                val eventsForDay = datesWithEvents[Triple(day, month, year)] ?: emptyList()
                val isToday = day == today && month == todayMonth && year == todayYear

                DayCell(
                    day = day,
                    isSelected = isSelected,
                    eventsForDay = eventsForDay,
                    isToday = isToday,
                    onClick = {
                      val newDate = Calendar.getInstance()
                      newDate.set(year, month, day)
                      onDateSelected(newDate.time)
                    })
              } else {
                Box(modifier = Modifier.size(40.dp))
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
      }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    eventsForDay: List<CalendarItem>,
    isToday: Boolean,
    onClick: () -> Unit
) {
  val backgroundColor =
      when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
      }

  val textColor =
      when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
      }

  Box(
      modifier =
          Modifier.size(40.dp)
              .clip(CircleShape)
              .background(backgroundColor)
              .clickable(onClick = onClick)
              .then(
                  if (isToday && !isSelected) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                  } else Modifier)
              .testTag(PersonalCalendarSectionTestTags.dayCell(day)),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = day.toString(),
              style = MaterialTheme.typography.bodyMedium,
              color = textColor,
              fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal)
          if (eventsForDay.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              // Show up to 3 event indicators with different colors
              eventsForDay.take(3).forEach { item ->
                val dotColor =
                    when {
                      isSelected -> MaterialTheme.colorScheme.onPrimary
                      else ->
                          try {
                            Color(android.graphics.Color.parseColor(item.color ?: "#4CAF50"))
                          } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                          }
                    }
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(dotColor))
              }
            }
          }
        }
      }
}

// ==================== ICS IMPORT ====================

@Composable
private fun IcsImportSection(onImportIcs: (Uri) -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val pickFileLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onImportIcs(it) }
      }

  Column(
      modifier = modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Import Calendar Events",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        Text(
            text = "You can import events from an .ics file to add them to your calendar.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Button(
            onClick = { pickFileLauncher.launch("text/calendar") },
            modifier = Modifier.fillMaxWidth(0.7f),
            contentPadding = PaddingValues(vertical = 12.dp)) {
              Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Import ICS")
              Spacer(modifier = Modifier.width(8.dp))
              Text("Import .ics File")
            }
      }
}

// ==================== SELECTED DATE EVENTS ====================

@Composable
private fun SelectedDateEventsSection(
    selectedDate: Date,
    events: List<CalendarItem>,
    onEventClick: ((CalendarItem) -> Unit)?,
    modifier: Modifier = Modifier
) {
  val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

  Column(modifier = modifier) {
    Text(
        text = dateFormat.format(selectedDate),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(8.dp))

    if (events.isEmpty()) {
      EmptyEventsState()
    } else {
      Column(
          modifier = Modifier.testTag(PersonalCalendarSectionTestTags.EVENTS_LIST),
          verticalArrangement = Arrangement.spacedBy(8.dp)) {
            events.forEach { item ->
              CalendarEventCard(item = item, onClick = { onEventClick?.invoke(item) })
            }
          }
    }
  }
}

@Composable
private fun EmptyEventsState() {
  Card(
      modifier = Modifier.fillMaxWidth().testTag(PersonalCalendarSectionTestTags.EMPTY_STATE),
      shape = RoundedCornerShape(12.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                  text = "No events scheduled for this day",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
}

@Composable
private fun CalendarEventCard(item: CalendarItem, onClick: () -> Unit) {
  val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
  val startTime = timeFormat.format(item.start.toDate())
  val endTime = item.end?.let { timeFormat.format(it.toDate()) }

  val eventColor =
      try {
        Color(android.graphics.Color.parseColor(item.color ?: "#4CAF50"))
      } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
      }

  val eventTypeLabel =
      when (item) {
        is CalendarItem.AppEvent -> if (item.isOwner) "Created" else "Joined"
        is CalendarItem.Personal -> "Personal"
        is CalendarItem.Imported -> "Imported"
      }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .clickable(onClick = onClick)
              .testTag(PersonalCalendarSectionTestTags.eventItem(item.uid)),
      shape = RoundedCornerShape(12.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
              // Color indicator
              Box(
                  modifier =
                      Modifier.width(4.dp)
                          .height(56.dp)
                          .clip(RoundedCornerShape(2.dp))
                          .background(eventColor))

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = item.title,
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.SemiBold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.weight(1f))

                      // Event type badge
                      Surface(
                          shape = RoundedCornerShape(12.dp),
                          color = eventColor.copy(alpha = 0.2f)) {
                            Text(
                                text = eventTypeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = eventColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                          }
                    }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.AccessTime,
                      contentDescription = null,
                      modifier = Modifier.size(14.dp),
                      tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      text = if (endTime != null) "$startTime - $endTime" else startTime,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                }

                item.location?.let { location ->
                  Spacer(modifier = Modifier.height(2.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                  }
                }
              }
            }
      }
}

// ==================== UTILITY FUNCTIONS ====================

private fun getWeekStartDate(date: Date): Date {
  val calendar = Calendar.getInstance()
  calendar.time = date
  calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
  calendar.set(Calendar.HOUR_OF_DAY, 0)
  calendar.set(Calendar.MINUTE, 0)
  calendar.set(Calendar.SECOND, 0)
  calendar.set(Calendar.MILLISECOND, 0)
  return calendar.time
}

private fun getWeekDays(weekStart: Date): List<Date> {
  val days = mutableListOf<Date>()
  val calendar = Calendar.getInstance()
  calendar.time = weekStart
  repeat(7) {
    days.add(calendar.time)
    calendar.add(Calendar.DAY_OF_MONTH, 1)
  }
  return days
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
  val cal1 = Calendar.getInstance()
  cal1.time = date1
  val cal2 = Calendar.getInstance()
  cal2.time = date2
  return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
      cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

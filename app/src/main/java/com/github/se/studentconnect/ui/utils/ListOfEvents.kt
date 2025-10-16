package com.github.se.studentconnect.ui.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.theme.AppTheme
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * The main screen composable that displays a vertical list of event cards.
 *
 * @param navController The navigation controller used for navigating to the event detail view.
 * @param events The list of events to display.
 */
@Composable
fun EventListScreen(navController: NavHostController, events: List<Event>, hasJoined: Boolean) {
  val groupedEvents = events.groupBy { event -> formatDateHeader(event.start) }

  LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp)) {
        groupedEvents.forEach { (dateHeader, eventsOnDate) ->
          item {
            Text(
                text = dateHeader,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp))
          }
          // Display each event card for the current date group
          items(eventsOnDate) { event ->
            EventCard(
                event = event,
                onClick = { navController.navigate(Route.eventView(event.uid, hasJoined)) })
          }
        }
      }
}

/**
 * A composable that displays a single event card, styled to match the example image.
 *
 * @param event The event data to display.
 * @param onClick The action to perform when the card is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
  var isLiked by remember { mutableStateOf(false) }

  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      shape = RoundedCornerShape(16.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Image(
                imageVector = Icons.Default.Image,
                contentDescription = event.title,
                modifier =
                    Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop)

            // Favorite Button with semi-transparent background
            Box(
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(color = Color.Black.copy(alpha = 0.2f), shape = CircleShape)) {
                  IconButton(onClick = { isLiked = !isLiked }) {
                    Icon(
                        imageVector =
                            if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint =
                            if (isLiked) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
          }
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.location?.name ?: "No location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "${formatDate(event.start)} | ${formatTime(event.start)}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error,
              )
              Spacer(modifier = Modifier.width(8.dp))
              event.participationFee?.let { fee ->
                Text(
                    text = "$fee €",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            if (event is Event.Public)
                event.tags.let { tags ->
                  if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                          tags.take(3).forEach { tag -> Chip(tag = tag) }
                        }
                  }
                }
          }
        }
      }
}

@Composable
fun Chip(tag: String) {
  Box(
      modifier =
          Modifier.background(
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(
            text = tag.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            letterSpacing = 1.sp)
      }
}

private fun formatDate(timestamp: Timestamp): String {
  val eventCalendar = Calendar.getInstance().apply { time = timestamp.toDate() }
  val today = Calendar.getInstance()

  return if (eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
      eventCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
    "Today "
  } else {
    SimpleDateFormat("d MMM", Locale.getDefault()).format(timestamp.toDate())
  }
}

private fun formatTime(timestamp: Timestamp): String {
  return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())
}

fun formatDateHeader(timestamp: Timestamp): String {
  val eventCalendar = Calendar.getInstance().apply { time = timestamp.toDate() }
  val today = Calendar.getInstance()
  val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

  return when {
    eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        eventCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "TODAY"
    eventCalendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
        eventCalendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> "TOMORROW"
    else -> SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(timestamp.toDate()).uppercase()
  }
}

/** A preview function to display the EventListScreen in a dark theme. */
@Preview(showBackground = true)
@Composable
fun EventListScreenPreview() {
  val calendar =
      Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
      }
  val eventTimestamp = Timestamp(calendar.time)

  val mockEvents =
      listOf(
          Event.Public(
              uid = "event-get-in-step-01",
              ownerId = "Balelec",
              title = "Get In Step",
              description = "An unforgettable night on campus with top DJs!",
              location = Location(0.0, 0.0, "EPFL"),
              start = eventTimestamp,
              participationFee = 31u,
              isFlash = false,
              subtitle = "Lausanne's biggest night!",
              tags = listOf("Tag 1", "Tag 2 "),
              imageUrl = null,
          ))

  AppTheme() {
    Surface(modifier = Modifier.fillMaxSize()) {
      EventListScreen(navController = rememberNavController(), events = mockEvents, false)
    }
  }
}

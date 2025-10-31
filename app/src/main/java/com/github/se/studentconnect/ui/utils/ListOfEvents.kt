package com.github.se.studentconnect.ui.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.navigation.Route
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * The main screen composable that displays a vertical list of event cards.
 *
 * @param navController The navigation controller used for navigating to the event detail view.
 * @param events The list of events to display.
 * @param hasJoined Indicates if the user has joined the events.
 * @param listState The LazyListState for controlling scroll position.
 * @param favoriteEventIds A set of event IDs that are marked as favorites by the user
 * @param onFavoriteToggle A callback function to handle favorite toggling for an event.
 */
@Composable
fun EventListScreen(
    navController: NavHostController,
    events: List<Event>,
    hasJoined: Boolean,
    listState: LazyListState = rememberLazyListState(),
    favoriteEventIds: Set<String> = emptySet(),
    onFavoriteToggle: (String) -> Unit = {}
) {
  if (events.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
      Text("No events found matching your criteria.", style = MaterialTheme.typography.bodyLarge)
    }
    return
  }

  val sortedEvents = events.sortedBy { it.start }
  val groupedEvents = sortedEvents.groupBy { event -> formatDateHeader(event.start) }

  LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize().testTag("event_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
        groupedEvents.forEach { (dateHeader, eventsOnDate) ->
          item {
            Text(
                text = dateHeader,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))
          }
          items(eventsOnDate, key = { it.uid }) { event ->
            val isFavorite = event.uid in favoriteEventIds
            EventCard(
                event = event,
                isFavorite = isFavorite,
                onFavoriteToggle = onFavoriteToggle,
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
 * @param isFavorite Indicates if the event is marked as a favorite.
 * @param onFavoriteToggle A callback function to handle favorite toggling for the event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(
    event: Event,
    isFavorite: Boolean,
    onFavoriteToggle: (String) -> Unit,
    onClick: () -> Unit
) {
  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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

            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                        .clip(CircleShape)) {
                  IconButton(
                      onClick = { onFavoriteToggle(event.uid) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector =
                                if (isFavorite) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                text = event.location?.name ?: "Location not specified",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Text(
                      text = "${formatDate(event.start)} | ${formatTime(event.start)}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.error,
                  )
                  Text(
                      text =
                          if (event.participationFee == null || event.participationFee == 0u) "Free"
                          else "${event.participationFee} €",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            if (event is Event.Public && event.tags.isNotEmpty()) {
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.fillMaxWidth()) {
                    event.tags.take(3).forEach { tag -> Chip(tag = tag) }
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
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            letterSpacing = 1.sp)
      }
}

private fun formatDate(timestamp: Timestamp): String {
  val eventCalendar = Calendar.getInstance().apply { time = timestamp.toDate() }
  val today = Calendar.getInstance()

  return if (eventCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
      eventCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
    "Today"
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

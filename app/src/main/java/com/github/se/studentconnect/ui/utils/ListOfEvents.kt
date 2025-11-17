package com.github.se.studentconnect.ui.utils

import android.util.Log
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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.profile.loadBitmapFromUri
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers

private const val MAX_LINES_FOR_ADDRESS_TEXT = 1

/**
 * The main screen composable that displays a vertical list of event cards.
 *
 * @param navController The navigation controller used for navigating to the event detail view.
 * @param events The list of events to display.
 * @param hasJoined Indicates if the user has joined the events.
 * @param listState The LazyListState for controlling scroll position.
 * @param favoriteEventIds A set of event IDs that are marked as favorites by the user
 * @param onFavoriteToggle A callback function to handle favorite toggling for an event.
 * @param topContent Optional composable content to display at the top of the list (e.g., filters).
 */
@Composable
fun EventListScreen(
    navController: NavHostController,
    events: List<Event>,
    hasJoined: Boolean,
    listState: LazyListState = rememberLazyListState(),
    favoriteEventIds: Set<String> = emptySet(),
    onFavoriteToggle: (String) -> Unit = {},
    topContent: (@Composable () -> Unit)? = null
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
        topContent?.let { header -> item(key = "event_list_header") { header() } }
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
  var localFavorite by remember { mutableStateOf(isFavorite) }

  LaunchedEffect(isFavorite) { localFavorite = isFavorite }

  val now = Timestamp.now()
  val endTime =
      event.end
          ?: run {
            val cal = Calendar.getInstance()
            cal.time = event.start.toDate()
            cal.add(Calendar.HOUR_OF_DAY, 3)
            Timestamp(cal.time)
          }
  val isLive = now >= event.start && now < endTime

  val context = LocalContext.current
  val repository = MediaRepositoryProvider.repository
  val profileId = event.imageUrl
  val imageBitmap by
      produceState<ImageBitmap?>(initialValue = null, profileId, repository) {
        value =
            profileId?.let { id ->
              runCatching { repository.download(id) }
                  .onFailure { Log.e("EventCardImage", "Failed to download event image: $id", it) }
                  .getOrNull()
                  ?.let { loadBitmapFromUri(context, it, Dispatchers.IO) }
            }
      }
  Card(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("event_card_${event.uid}"),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            if (imageBitmap != null) {
              Image(
                  bitmap = imageBitmap!!,
                  contentDescription =
                      stringResource(R.string.content_description_event_card_picture),
                  modifier =
                      Modifier.fillMaxSize()
                          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                  contentScale = ContentScale.Crop)
            } else {
              Image(
                  imageVector = Icons.Default.Image,
                  contentDescription = event.title,
                  modifier =
                      Modifier.fillMaxSize()
                          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                  contentScale = ContentScale.Crop)
            }

            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                        .clip(CircleShape)) {
                  IconButton(
                      onClick = {
                        localFavorite = !localFavorite
                        onFavoriteToggle(event.uid)
                      },
                      modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector =
                                if (localFavorite) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                }

            if (isLive) {
              Row(
                  modifier =
                      Modifier.align(Alignment.TopStart)
                          .padding(8.dp)
                          .background(Color.Red.copy(alpha = 0.9f), shape = CircleShape)
                          .padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Circle,
                        contentDescription = "Live Icon",
                        tint = Color.White,
                        modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("event_card_title_${event.uid}"))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatShortAddress(event.location?.name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = MAX_LINES_FOR_ADDRESS_TEXT,
                overflow = TextOverflow.Ellipsis)
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
    createGregorianFormatter("d MMM").format(timestamp.toDate())
  }
}

private fun formatTime(timestamp: Timestamp): String {
  return createGregorianFormatter("HH:mm").format(timestamp.toDate())
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
    else ->
        createGregorianFormatter("EEEE d MMMM", Locale.FRENCH)
            .format(timestamp.toDate())
            .uppercase()
  }
}

private fun createGregorianFormatter(
    pattern: String,
    locale: Locale = Locale.getDefault()
): SimpleDateFormat =
    SimpleDateFormat(pattern, locale).apply {
      isLenient = false
      calendar = GregorianCalendar().apply { gregorianChange = Date(Long.MIN_VALUE) }
    }

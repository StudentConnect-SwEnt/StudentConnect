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
import androidx.compose.material.icons.filled.Bolt
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
import com.github.se.studentconnect.model.organization.Organization
import com.github.se.studentconnect.model.organization.OrganizationRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepositoryProvider
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.screen.home.OrganizationData
import com.github.se.studentconnect.ui.screen.home.OrganizationSuggestions
import com.github.se.studentconnect.ui.theme.Dimensions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers

/**
 * Shared composable for displaying live event badge (flash icon or LIVE text). This eliminates code
 * duplication between EventCard and CarouselCard.
 *
 * @param isLive Whether the event is currently live.
 * @param isFlash Whether the event is a flash event.
 * @param modifier Modifier for the badge container.
 */
@Composable
fun LiveEventBadge(isLive: Boolean, isFlash: Boolean, modifier: Modifier = Modifier) {
  if (!isLive) return

  if (isFlash) {
    // Flash event: show flash/storm icon
    Box(
        modifier =
            modifier
                .background(
                    Color(C.FlashEvent.BADGE_COLOR.toInt()).copy(alpha = C.FlashEvent.BADGE_ALPHA),
                    shape = CircleShape)
                .padding(C.FlashEvent.BADGE_PADDING_DP.dp)) {
          Icon(
              imageVector = Icons.Filled.Bolt,
              contentDescription = stringResource(R.string.content_description_flash_event),
              tint = MaterialTheme.colorScheme.onError,
              modifier = Modifier.size(C.FlashEvent.ICON_SIZE_DP.dp))
        }
  } else {
    // Regular live event: show LIVE badge
    Row(
        modifier =
            modifier
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = C.FlashEvent.BADGE_ALPHA),
                    shape = CircleShape)
                .padding(
                    horizontal = Dimensions.LiveBadgeHorizontalPadding,
                    vertical = Dimensions.LiveBadgeVerticalPadding),
        verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Filled.Circle,
              contentDescription = stringResource(R.string.content_description_live_icon),
              tint = MaterialTheme.colorScheme.onError,
              modifier = Modifier.size(Dimensions.LiveBadgeIconSize))
          Spacer(modifier = Modifier.width(Dimensions.LiveBadgeSpacerWidth))
          Text(
              text = stringResource(R.string.event_label_live),
              color = MaterialTheme.colorScheme.onError,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold)
        }
  }
}

private const val MAX_LINES_FOR_ADDRESS_TEXT = 1

/**
 * Configuration for favorite events functionality.
 *
 * @param favoriteEventIds A set of event IDs that are marked as favorites by the user.
 * @param onFavoriteToggle A callback function to handle favorite toggling for an event.
 */
data class FavoritesConfig(
    val favoriteEventIds: Set<String> = emptySet(),
    val onFavoriteToggle: (String) -> Unit = {}
)

/**
 * Configuration for organization suggestions functionality.
 *
 * @param organizations List of organizations to display as suggestions.
 * @param onOrganizationClick Callback when an organization is clicked.
 */
data class OrganizationSuggestionsConfig(
    val organizations: List<OrganizationData> = emptyList(),
    val onOrganizationClick: (String) -> Unit = {}
)

/**
 * The main screen composable that displays a vertical list of event cards.
 *
 * @param navController The navigation controller used for navigating to the event detail view.
 * @param events The list of events to display.
 * @param hasJoined Indicates if the user has joined the events.
 * @param listState The LazyListState for controlling scroll position.
 * @param favoritesConfig Configuration for favorites functionality.
 * @param topContent Optional composable content to display at the top of the list (e.g., filters).
 * @param organizationSuggestionsConfig Configuration for organization suggestions.
 */
@Composable
fun EventListScreen(
    navController: NavHostController,
    events: List<Event>,
    hasJoined: Boolean,
    listState: LazyListState = rememberLazyListState(),
    favoritesConfig: FavoritesConfig = FavoritesConfig(),
    topContent: (@Composable () -> Unit)? = null,
    organizationSuggestionsConfig: OrganizationSuggestionsConfig = OrganizationSuggestionsConfig()
) {
  if (events.isEmpty()) {
    Box(
        modifier = Modifier.fillMaxSize().padding(Dimensions.SpacingNormal),
        contentAlignment = Alignment.Center) {
          Text(
              "No events found matching your criteria.", style = MaterialTheme.typography.bodyLarge)
        }
    return
  }

  val sortedEvents = events.sortedBy { it.start }
  val groupedEvents = sortedEvents.groupBy { event -> formatDateHeader(event.start) }

  // Calculate random insertion point for organizations (between 1st and 2nd date group if there are
  // at least 2)
  val dateGroups = groupedEvents.keys.toList()
  val orgInsertionIndex =
      remember(dateGroups.size, organizationSuggestionsConfig.organizations.isNotEmpty()) {
        if (organizationSuggestionsConfig.organizations.isNotEmpty() && dateGroups.size >= 2) {
          Random.nextInt(1, minOf(dateGroups.size, 3)) // Insert after 1st or 2nd date group
        } else {
          -1 // Don't insert
        }
      }

  LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize().testTag("event_list"),
      contentPadding =
          PaddingValues(
              start = Dimensions.SpacingNormal,
              end = Dimensions.SpacingNormal,
              top = Dimensions.SpacingNormal,
              bottom = Dimensions.SpacingNormal)) {
        topContent?.let { header -> item(key = "event_list_header") { header() } }

        groupedEvents.entries.forEachIndexed { index, (dateHeader, eventsOnDate) ->
          item(key = "date_header_$dateHeader") {
            Text(
                text = dateHeader,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))
          }
          items(eventsOnDate, key = { it.uid }) { event ->
            val isFavorite = event.uid in favoritesConfig.favoriteEventIds
            EventCard(
                event = event,
                isFavorite = isFavorite,
                onFavoriteToggle = favoritesConfig.onFavoriteToggle,
                onClick = { navController.navigate(Route.eventView(event.uid, hasJoined)) })
          }

          // Insert organization suggestions after this date group if it matches the insertion index
          if (index == orgInsertionIndex) {
            item(key = "organization_suggestions") {
              OrganizationSuggestions(
                  organizations = organizationSuggestionsConfig.organizations,
                  onOrganizationClick = organizationSuggestionsConfig.onOrganizationClick,
                  modifier = Modifier.padding(vertical = Dimensions.SpacingNormal))
            }
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
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  LaunchedEffect(event) { imageBitmap = loadBitmapFromEvent(context, event) }

  // Fetch event creator information
  val userRepository = UserRepositoryProvider.repository
  val organizationRepository = OrganizationRepositoryProvider.repository

  // Fetch user creator (for personal events)
  val creator by
      produceState<User?>(initialValue = null, event.ownerId, event.organizationId) {
        // Only fetch user if it's not an organization event
        if (event.organizationId == null) {
          value =
              runCatching { userRepository.getUserById(event.ownerId) }
                  .onFailure {
                    Log.e("EventCard", "Failed to fetch creator for event ${event.uid}", it)
                  }
                  .getOrNull()
        } else {
          value = null
        }
      }

  // Fetch organization creator (for organization events)
  val organization by
      produceState<Organization?>(initialValue = null, event.organizationId) {
        value =
            event.organizationId?.let { orgId ->
              runCatching { organizationRepository.getOrganizationById(orgId) }
                  .onFailure {
                    Log.e("EventCard", "Failed to fetch organization for event ${event.uid}", it)
                  }
                  .getOrNull()
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
          Box(modifier = Modifier.fillMaxWidth().height(Dimensions.EventCardImageHeight)) {
            if (imageBitmap != null) {
              Image(
                  bitmap = imageBitmap!!,
                  contentDescription =
                      stringResource(R.string.content_description_event_card_picture),
                  modifier =
                      Modifier.fillMaxSize()
                          .clip(
                              RoundedCornerShape(
                                  topStart = Dimensions.EventCardCornerRadius,
                                  topEnd = Dimensions.EventCardCornerRadius)),
                  contentScale = ContentScale.Crop)
            } else {
              Image(
                  imageVector = Icons.Default.Image,
                  contentDescription = event.title,
                  modifier =
                      Modifier.fillMaxSize()
                          .clip(
                              RoundedCornerShape(
                                  topStart = Dimensions.EventCardCornerRadius,
                                  topEnd = Dimensions.EventCardCornerRadius)),
                  contentScale = ContentScale.Crop)
            }

            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(color = Color.White.copy(alpha = 0.6f), shape = CircleShape)
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
                            tint = if (localFavorite) Color.Red else Color.Black,
                        )
                      }
                }

            LiveEventBadge(
                isLive = isLive,
                isFlash = event.isFlash,
                modifier =
                    Modifier.align(Alignment.TopStart)
                        .padding(C.FlashEvent.BADGE_OUTER_PADDING_DP.dp))
          }
          Column(modifier = Modifier.padding(Dimensions.EventCardContentPadding)) {
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
            // Display organization name if it's an organization event, otherwise display user name
            organization?.let { org ->
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                  text = "by ${org.name}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("event_card_creator_${event.uid}"))
            }
                ?: creator?.let { user ->
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                      text =
                          stringResource(
                              R.string.text_event_creator_by, user.firstName, user.lastName),
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag("event_card_creator_${event.uid}"))
                }
            Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
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
              Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
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
                  shape = RoundedCornerShape(Dimensions.EventCardChipCornerRadius))
              .padding(
                  horizontal = Dimensions.EventCardChipHorizontalPadding,
                  vertical = Dimensions.EventCardChipVerticalPadding)) {
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
        createGregorianFormatter("EEEE d MMMM", Locale.ENGLISH)
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

package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.profile.JoinedEventsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

// Test tags for UI testing
object JoinedEventsScreenTestTags {
  const val JOINED_EVENTS_SCREEN = "joined_events_screen"
  const val TOP_APP_BAR = "joined_events_top_app_bar"
  const val BACK_BUTTON = "joined_events_back_button"
  const val SEARCH_BAR = "joined_events_search_bar"
  const val TAB_ROW = "joined_events_tab_row"
  const val EVENT_LIST = "joined_events_list"
  const val EMPTY_STATE = "joined_events_empty_state"

  fun eventCard(eventUid: String) = "event_card_$eventUid"

  fun tab(title: String) = "tab_$title"
}

// Filter options to show past or upcoming events
enum class EventFilter {
  Past,
  Upcoming
}

// Main screen showing all events the user has joined
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinedEventsScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: JoinedEventsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedFilter = uiState.selectedFilter
  val filteredEvents = uiState.filteredEvents
  val isLoading = uiState.isLoading

  // Load events when the screen first appears
  LaunchedEffect(Unit) { viewModel.loadJoinedEvents() }

  val titleMyEvents = stringResource(R.string.title_my_events)
  val backToProfileDescription = stringResource(R.string.content_description_back_to_profile)

  Scaffold(
      modifier = Modifier.testTag(JoinedEventsScreenTestTags.JOINED_EVENTS_SCREEN),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text(titleMyEvents) },
            navigationIcon = {
              IconButton(
                  onClick = onNavigateBack,
                  modifier = Modifier.testTag(JoinedEventsScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backToProfileDescription)
                  }
            },
            modifier = Modifier.fillMaxWidth().testTag(JoinedEventsScreenTestTags.TOP_APP_BAR))
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          SearchBar(
              query = searchQuery,
              onQueryChange = viewModel::updateSearchQuery,
              modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium))

          FilterTabs(
              selectedFilter = selectedFilter, onFilterSelected = { viewModel.updateFilter(it) })

          Spacer(modifier = Modifier.height(spacing.extraSmall))

          // Swipe down to refresh the list
          PullToRefreshBox(
              isRefreshing = isLoading,
              onRefresh = { viewModel.loadJoinedEvents() },
              modifier = Modifier.fillMaxSize()) {
                when {
                  filteredEvents.isEmpty() && !isLoading -> {
                    EmptyEventsState(selectedFilter = selectedFilter)
                  }
                  else -> {
                    EventsList(
                        events = filteredEvents,
                        onEventClick = { event ->
                          navController.navigate(Route.eventView(event.uid, true))
                        })
                  }
                }
              }
        }
      }
}

// Search bar to filter events by name
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  val searchPlaceholder = stringResource(R.string.placeholder_search_events_joined)
  val searchDescription = stringResource(R.string.content_description_search)

  TextField(
      value = query,
      onValueChange = onQueryChange,
      modifier =
          modifier.testTag(JoinedEventsScreenTestTags.SEARCH_BAR).padding(vertical = spacing.small),
      placeholder = { Text(searchPlaceholder) },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = searchDescription)
      },
      singleLine = true,
      shape = MaterialTheme.shapes.large)
}

// Tabs to switch between past and upcoming events
@Composable
private fun FilterTabs(selectedFilter: EventFilter, onFilterSelected: (EventFilter) -> Unit) {
  val pastTitle = stringResource(R.string.filter_past)
  val upcomingTitle = stringResource(R.string.filter_upcoming)
  val tabs = mapOf(EventFilter.Past to pastTitle, EventFilter.Upcoming to upcomingTitle)
  val selectedIndex = tabs.keys.indexOf(selectedFilter)

  PrimaryTabRow(
      selectedTabIndex = selectedIndex,
      modifier = Modifier.fillMaxWidth().testTag(JoinedEventsScreenTestTags.TAB_ROW)) {
        tabs.forEach { (filter, title) ->
          Tab(
              selected = selectedFilter == filter,
              onClick = { onFilterSelected(filter) },
              text = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
              modifier = Modifier.testTag(JoinedEventsScreenTestTags.tab(title)))
        }
      }
}

// Scrollable list of event cards
@Composable
private fun EventsList(events: List<Event>, onEventClick: (Event) -> Unit) {
  LazyColumn(
      modifier = Modifier.fillMaxSize().testTag(JoinedEventsScreenTestTags.EVENT_LIST),
      contentPadding = PaddingValues(horizontal = spacing.medium),
      verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        items(items = events, key = { it.uid }) { event ->
          EventCard(event = event, onClick = { onEventClick(event) })
        }
      }
}

// Individual event card showing event details
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
  val formattedDate = dateFormat.format(event.start.toDate())
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate sizes relative to screen width
  val cardHeight = screenWidth * 0.35f
  val cornerRadius = screenWidth * 0.04f
  val cardElevation = screenWidth * 0.01f

  Card(
      onClick = onClick,
      modifier =
          Modifier.fillMaxWidth()
              .height(cardHeight)
              .testTag(JoinedEventsScreenTestTags.eventCard(event.uid)),
      shape = RoundedCornerShape(cornerRadius),
      elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primary)))) {
              EventCardContent(event = event, formattedDate = formattedDate)
            }
      }
}

@Composable
private fun EventCardContent(event: Event, formattedDate: String) {
  val eventImageDescription = stringResource(R.string.content_description_event_image)
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate sizes relative to screen width
  val contentPadding = screenWidth * 0.04f
  val contentSpacing = screenWidth * 0.04f
  val imageSize = screenWidth * 0.25f
  val imageCornerRadius = screenWidth * 0.03f

  Row(
      modifier = Modifier.fillMaxSize().padding(contentPadding),
      horizontalArrangement = Arrangement.spacedBy(contentSpacing)) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = eventImageDescription,
            modifier =
                Modifier.size(imageSize)
                    .clip(RoundedCornerShape(imageCornerRadius))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            tint = MaterialTheme.colorScheme.onPrimary)

        EventCardDetails(event = event, formattedDate = formattedDate)
      }
}

@Composable
private fun RowScope.EventCardDetails(event: Event, formattedDate: String) {
  Column(
      modifier = Modifier.weight(1f).fillMaxHeight(),
      verticalArrangement = Arrangement.SpaceBetween) {
        EventCardHeader(event = event)
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium)
      }
}

@Composable
private fun EventCardHeader(event: Event) {
  val isPrivate = event is Event.Private
  val publicEventDescription = stringResource(R.string.content_description_public_event)
  val privateEventDescription = stringResource(R.string.content_description_private_event)

  Column {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top) {
          Text(
              text = event.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f))

          EventTypeIcon(
              isPrivate = isPrivate,
              publicDescription = publicEventDescription,
              privateDescription = privateEventDescription)
        }

    EventSubtitle(event = event)
  }
}

@Composable
private fun EventTypeIcon(
    isPrivate: Boolean,
    publicDescription: String,
    privateDescription: String
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate sizes relative to screen width
  val iconSize = screenWidth * 0.06f
  val iconPadding = screenWidth * 0.01f

  Icon(
      painter = painterResource(if (isPrivate) R.drawable.ic_lock else R.drawable.ic_web),
      contentDescription = if (isPrivate) privateDescription else publicDescription,
      tint = MaterialTheme.colorScheme.onPrimary,
      modifier =
          Modifier.size(iconSize)
              .background(
                  MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f), shape = CircleShape)
              .padding(iconPadding))
}

@Composable
private fun EventSubtitle(event: Event) {
  if (event is Event.Public && event.subtitle.isNotBlank()) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val subtitleSpacing = screenWidth * 0.01f

    Spacer(modifier = Modifier.height(subtitleSpacing))
    Text(
        text = event.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis)
  }
}

// Message shown when there are no events
@Composable
private fun EmptyEventsState(selectedFilter: EventFilter) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val emptyStatePadding = screenWidth * 0.08f

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(emptyStatePadding)
              .testTag(JoinedEventsScreenTestTags.EMPTY_STATE),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        val title =
            when (selectedFilter) {
              EventFilter.Past -> stringResource(R.string.text_no_past_joined_events)
              EventFilter.Upcoming -> stringResource(R.string.text_no_upcoming_joined_events)
            }

        val description =
            when (selectedFilter) {
              EventFilter.Past -> stringResource(R.string.text_no_past_joined_events_description)
              EventFilter.Upcoming ->
                  stringResource(R.string.text_no_upcoming_joined_events_description)
            }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(spacing.extraSmall))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

// Spacing values used throughout the screen
private val spacing: JoinedEventsSpacing
  @Composable get() = JoinedEventsSpacing

private object JoinedEventsSpacing {
  val extraSmall = 8.dp
  val small = 12.dp
  val medium = 16.dp
}

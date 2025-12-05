package com.github.se.studentconnect.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import java.text.SimpleDateFormat
import java.util.Locale

/** Test tags for automated testing of pinned events section components. */
object PinnedEventsSectionTestTags {
  const val SECTION = "pinned_events_section"
  const val TITLE = "pinned_events_title"
  const val EMPTY_STATE = "pinned_events_empty_state"
  const val HORIZONTAL_LIST = "pinned_events_horizontal_list"

  /**
   * Generates a test tag for a pinned event card.
   *
   * @param eventUid The unique identifier of the event
   * @return Test tag string for the event card
   */
  fun eventCard(eventUid: String) = "pinned_event_card_$eventUid"
}

/**
 * Section showing up to 3 pinned events in a horizontal list.
 *
 * @param pinnedEvents List of pinned events to display
 * @param onEventClick Callback invoked when an event card is clicked
 * @param modifier Modifier to be applied to the section
 */
@Composable
fun PinnedEventsSection(
    pinnedEvents: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Dynamic spacing based on screen width
  val sectionVerticalPadding = screenWidth * 0.04f
  val titleHorizontalPadding = screenWidth * 0.04f
  val titleBottomSpacing = screenWidth * 0.03f

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(vertical = sectionVerticalPadding)
              .testTag(PinnedEventsSectionTestTags.SECTION)) {
        Text(
            text = stringResource(R.string.pinned_events_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.padding(horizontal = titleHorizontalPadding)
                    .testTag(PinnedEventsSectionTestTags.TITLE))

        Spacer(modifier = Modifier.height(titleBottomSpacing))

        if (pinnedEvents.isEmpty()) {
          EmptyPinnedEventsState()
        } else {
          PinnedEventsHorizontalList(pinnedEvents = pinnedEvents, onEventClick = onEventClick)
        }
      }
}

// Empty state when no events are pinned
@Composable
private fun EmptyPinnedEventsState() {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Dynamic padding based on screen width
  val emptyStateHorizontalPadding = screenWidth * 0.04f
  val emptyStateVerticalPadding = screenWidth * 0.06f

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .padding(
                  horizontal = emptyStateHorizontalPadding, vertical = emptyStateVerticalPadding)
              .testTag(PinnedEventsSectionTestTags.EMPTY_STATE),
      contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.pinned_events_empty_state),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

// Horizontal scrolling list of pinned events
@Composable
private fun PinnedEventsHorizontalList(pinnedEvents: List<Event>, onEventClick: (Event) -> Unit) {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Dynamic spacing and padding based on screen width
  val horizontalSpacing = screenWidth * 0.03f
  val contentHorizontalPadding = screenWidth * 0.04f

  LazyRow(
      modifier = Modifier.fillMaxWidth().testTag(PinnedEventsSectionTestTags.HORIZONTAL_LIST),
      horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
      contentPadding = PaddingValues(horizontal = contentHorizontalPadding)) {
        items(items = pinnedEvents, key = { it.uid }) { event ->
          PinnedEventCard(event = event, onClick = { onEventClick(event) })
        }
      }
}

// Compact event card for pinned events
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinnedEventCard(event: Event, onClick: () -> Unit) {
  val dateFormatPattern = stringResource(R.string.date_format_pinned_event)
  val dateFormat = SimpleDateFormat(dateFormatPattern, Locale.getDefault())
  val formattedDate = dateFormat.format(event.start.toDate())
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate sizes relative to screen width
  val cardWidth = screenWidth * 0.7f
  val cardHeight = screenWidth * 0.35f
  val cornerRadius = screenWidth * 0.04f
  val cardElevation = screenWidth * 0.01f

  Card(
      onClick = onClick,
      modifier =
          Modifier.width(cardWidth)
              .height(cardHeight)
              .testTag(PinnedEventsSectionTestTags.eventCard(event.uid)),
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
              PinnedEventCardContent(event = event, formattedDate = formattedDate)
            }
      }
}

@Composable
private fun PinnedEventCardContent(event: Event, formattedDate: String) {
  val eventImageDescription = stringResource(R.string.content_description_event_image)
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp

  // Calculate sizes relative to screen width
  val contentPadding = screenWidth * 0.04f
  val imageSize = screenWidth * 0.2f
  val imageCornerRadius = screenWidth * 0.03f
  val contentSpacing = screenWidth * 0.03f

  Row(
      modifier = Modifier.fillMaxSize().padding(contentPadding),
      horizontalArrangement = Arrangement.spacedBy(contentSpacing)) {
        // Event image placeholder
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = eventImageDescription,
            modifier =
                Modifier.size(imageSize)
                    .clip(RoundedCornerShape(imageCornerRadius))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            tint = MaterialTheme.colorScheme.onPrimary)

        // Event details
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween) {
              // Title
              Text(
                  text = event.title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis)

              // Date
              Text(
                  text = formattedDate,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                  fontWeight = FontWeight.Medium)
            }
      }
}

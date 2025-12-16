package com.github.se.studentconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.resources.C
import java.text.SimpleDateFormat
import java.util.Locale

// Button dimensions
private val BUTTON_CORNER_RADIUS = 24.dp
private val BUTTON_HORIZONTAL_PADDING = 16.dp
private val BUTTON_VERTICAL_PADDING = 12.dp
private val BUTTON_ICON_SIZE = 20.dp
private val BUTTON_ICON_SPACING = 8.dp

// Dialog dimensions
private val DIALOG_CORNER_RADIUS = 16.dp
private val DIALOG_MAX_HEIGHT = 500.dp
private val DIALOG_PADDING = 16.dp
private val DIALOG_TONAL_ELEVATION = 8.dp
private val DIALOG_HEADER_ICON_SIZE = 24.dp
private val DIALOG_HEADER_SPACING = 16.dp

// Card dimensions
private val CARD_HEIGHT = 72.dp
private val CARD_SPACING = 8.dp
private val CARD_CORNER_RADIUS = 12.dp
private val CARD_PADDING = 12.dp
private val CARD_CONTENT_SPACING = 4.dp
private val CARD_SELECTED_ELEVATION = 2.dp
private val CARD_UNSELECTED_ELEVATION = 0.dp

// Loading/empty state dimensions
private val LOADING_INDICATOR_SIZE = 24.dp
private val LOADING_TEXT_SPACING = 8.dp
private val LIST_CONTENT_PADDING = 4.dp

// Checkmark dimensions
private val CHECKMARK_BOX_SIZE = 24.dp
private val CHECKMARK_ICON_SIZE = 14.dp
private val CHECKMARK_SPACING = 8.dp

// Date format pattern
private const val DATE_FORMAT_PATTERN = "MMM d, yyyy"

/**
 * Represents the UI state for the event selection component.
 *
 * This sealed interface provides a type-safe way to represent loading, success, and error states
 * when fetching events that the user has joined.
 */
sealed interface EventSelectionState {
  /** The list of events available in this state. */
  val events: List<Event>

  /** Error message if the state represents a failure, null otherwise. */
  val error: String?

  /** Indicates that events are currently being loaded. */
  data class Loading(
      override val events: List<Event> = emptyList(),
      override val error: String? = null
  ) : EventSelectionState

  /** Indicates that events were successfully loaded. */
  data class Success(override val events: List<Event>, override val error: String? = null) :
      EventSelectionState

  /** Indicates that an error occurred while loading events. */
  data class Error(override val events: List<Event> = emptyList(), override val error: String?) :
      EventSelectionState
}

/**
 * A composable that displays a button to select an event to link to a story.
 *
 * When clicked, it opens a dialog showing the user's available events (both joined events and
 * events created by the user). The user can select one event to link to their story, or deselect
 * the currently selected event.
 *
 * @param state The current state of event loading (Loading, Success, or Error)
 * @param selectedEvent The currently selected event, or null if none selected
 * @param onEventSelected Callback invoked when an event is selected or deselected
 * @param onLoadEvents Callback to trigger loading of events when the dialog opens
 * @param modifier Optional modifier for the trigger button
 */
@Composable
fun EventSelectionDropdown(
    state: EventSelectionState,
    selectedEvent: Event?,
    onEventSelected: (Event?) -> Unit,
    onLoadEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
  var showDialog by remember { mutableStateOf(false) }

  EventSelectionTriggerButton(
      selectedEvent = selectedEvent,
      modifier = modifier,
      onClick = {
        showDialog = true
        // TODO: Event caching and duplicate call guarding will be implemented in a future PR
        // to avoid reloading events on every dialog open.
        onLoadEvents()
      })

  if (showDialog) {
    EventSelectionDialog(
        state = state,
        selectedEvent = selectedEvent,
        onEventSelected = onEventSelected,
        onDismiss = { showDialog = false })
  }
}

@Composable
private fun EventSelectionTriggerButton(
    selectedEvent: Event?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
  val label = stringResource(R.string.event_selection_button_label)
  val baseDescription = stringResource(R.string.content_description_event_selection_button)
  val noEventDescription = stringResource(R.string.event_selection_no_event)

  val semanticsDescription =
      if (selectedEvent != null) "$baseDescription: ${selectedEvent.title}" else noEventDescription

  Surface(
      modifier =
          modifier
              .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
              .clickable { onClick() }
              .semantics { contentDescription = semanticsDescription }
              .testTag(C.Tag.event_selection_button),
      shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
      color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = BUTTON_HORIZONTAL_PADDING, vertical = BUTTON_VERTICAL_PADDING),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  Icons.Default.Event,
                  contentDescription = null,
                  modifier = Modifier.size(BUTTON_ICON_SIZE),
                  tint = MaterialTheme.colorScheme.primary)
              Spacer(Modifier.width(BUTTON_ICON_SPACING))
              Text(
                  text = selectedEvent?.title ?: label,
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                      if (selectedEvent != null) MaterialTheme.colorScheme.onSurface
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            }
      }
}

@Composable
private fun EventSelectionDialog(
    state: EventSelectionState,
    selectedEvent: Event?,
    onEventSelected: (Event?) -> Unit,
    onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = DIALOG_MAX_HEIGHT)
                .testTag(C.Tag.event_selection_dropdown),
        shape = RoundedCornerShape(DIALOG_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = DIALOG_TONAL_ELEVATION) {
          Column(Modifier.padding(DIALOG_PADDING)) {
            EventSelectionDialogHeader(onDismiss = onDismiss)
            Spacer(Modifier.height(DIALOG_HEADER_SPACING))
            EventSelectionDialogContent(
                state = state,
                selectedEvent = selectedEvent,
                onEventSelected = onEventSelected,
                onDismiss = onDismiss)
          }
        }
  }
}

@Composable
private fun EventSelectionDialogHeader(onDismiss: () -> Unit) {
  Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
    Text(
        stringResource(R.string.event_selection_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
    IconButton(
        onClick = onDismiss,
        modifier = Modifier.size(DIALOG_HEADER_ICON_SIZE).testTag(C.Tag.event_selection_close)) {
          Icon(
              Icons.Default.Close,
              stringResource(R.string.content_description_close_event_selection))
        }
  }
}

@Composable
private fun EventSelectionDialogContent(
    state: EventSelectionState,
    selectedEvent: Event?,
    onEventSelected: (Event?) -> Unit,
    onDismiss: () -> Unit
) {
  when (state) {
    is EventSelectionState.Loading -> EventSelectionLoadingState()
    is EventSelectionState.Error -> EventSelectionErrorState(state.error)
    is EventSelectionState.Success ->
        EventSelectionSuccessState(
            events = state.events,
            selectedEvent = selectedEvent,
            onEventSelected = onEventSelected,
            onDismiss = onDismiss)
  }
}

@Composable
private fun EventSelectionLoadingState() {
  Box(
      Modifier.fillMaxWidth().height(CARD_HEIGHT).testTag(C.Tag.event_selection_loading),
      Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(Modifier.size(LOADING_INDICATOR_SIZE))
          Spacer(Modifier.height(LOADING_TEXT_SPACING))
          Text(
              text = stringResource(R.string.event_selection_loading),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
}

@Composable
private fun EventSelectionErrorState(message: String?) {
  Box(
      Modifier.fillMaxWidth().height(CARD_HEIGHT).testTag(C.Tag.event_selection_error),
      Alignment.Center) {
        Text(
            text = message ?: stringResource(R.string.event_selection_error),
            color = MaterialTheme.colorScheme.error)
      }
}

@Composable
private fun EventSelectionSuccessState(
    events: List<Event>,
    selectedEvent: Event?,
    onEventSelected: (Event?) -> Unit,
    onDismiss: () -> Unit
) {
  if (events.isEmpty()) {
    Box(
        Modifier.fillMaxWidth().height(CARD_HEIGHT).testTag(C.Tag.event_selection_empty),
        Alignment.Center) {
          Text(
              stringResource(R.string.event_selection_empty),
              color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
  } else {
    LazyColumn(
        modifier = Modifier.testTag(C.Tag.event_selection_list),
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING),
        contentPadding = PaddingValues(vertical = LIST_CONTENT_PADDING)) {
          itemsIndexed(events) { index, event ->
            val isSelected = selectedEvent?.uid == event.uid
            EventCard(
                event = event,
                isSelected = isSelected,
                onClick = {
                  onEventSelected(if (isSelected) null else event)
                  onDismiss()
                },
                testTagIndex = index)
          }
        }
  }
}

@Composable
private fun EventCard(event: Event, isSelected: Boolean, onClick: () -> Unit, testTagIndex: Int) {
  val bgColor =
      if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      }

  val cardContentDescription =
      stringResource(R.string.content_description_event_card_select, event.title)

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .height(CARD_HEIGHT)
              .clickable(onClick = onClick)
              .semantics { contentDescription = cardContentDescription }
              .testTag("${C.Tag.event_selection_card_prefix}_$testTagIndex"),
      shape = RoundedCornerShape(CARD_CORNER_RADIUS),
      colors = CardDefaults.cardColors(containerColor = bgColor),
      elevation =
          CardDefaults.cardElevation(
              defaultElevation =
                  if (isSelected) CARD_SELECTED_ELEVATION else CARD_UNSELECTED_ELEVATION)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(CARD_CONTENT_SPACING))
                Text(
                    text =
                        SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
                            .format(event.start.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (isSelected) {
                Spacer(Modifier.width(CHECKMARK_SPACING))
                Box(
                    Modifier.size(CHECKMARK_BOX_SIZE)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    Alignment.Center) {
                      Icon(
                          Icons.Default.Check,
                          contentDescription =
                              stringResource(R.string.content_description_selected_event_checkmark),
                          modifier = Modifier.size(CHECKMARK_ICON_SIZE),
                          tint = MaterialTheme.colorScheme.onPrimary)
                    }
              }
            }
      }
}

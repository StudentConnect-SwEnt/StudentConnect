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
import androidx.compose.foundation.lazy.items
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

private val BUTTON_CORNER_RADIUS = 24.dp
private val DIALOG_CORNER_RADIUS = 16.dp
private val DIALOG_MAX_HEIGHT = 500.dp
private val CARD_HEIGHT = 72.dp
private val CARD_SPACING = 8.dp

/** UI state for event selection. */
sealed class EventSelectionState {
  data object Loading : EventSelectionState()

  data class Success(val events: List<Event>) : EventSelectionState()

  data class Error(val message: String? = null) : EventSelectionState()
}

/** Button + dialog for selecting an event to link to a story. */
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
        // TODO: Cache loaded events in ViewModel to avoid reloading on every dialog open.
        // This will be addressed in a future PR when implementing event caching.
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  Icons.Default.Event,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = MaterialTheme.colorScheme.primary)
              Spacer(Modifier.width(8.dp))
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
        tonalElevation = 8.dp) {
          Column(Modifier.padding(16.dp)) {
            EventSelectionDialogHeader(onDismiss = onDismiss)
            Spacer(Modifier.height(16.dp))
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
        onClick = onDismiss, modifier = Modifier.size(24.dp).testTag(C.Tag.event_selection_close)) {
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
    is EventSelectionState.Error -> EventSelectionErrorState(state.message)
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
          CircularProgressIndicator(Modifier.size(24.dp))
          Spacer(Modifier.height(8.dp))
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
        contentPadding = PaddingValues(vertical = 4.dp)) {
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
private fun EventCard(
    event: Event, isSelected: Boolean, onClick: () -> Unit, testTagIndex: Int = -1
) {
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
              .testTag(
                  if (testTagIndex >= 0) "${C.Tag.event_selection_card_prefix}_$testTagIndex"
                  else "${C.Tag.event_selection_card_prefix}_${event.uid}"),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = bgColor),
      elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(
                    text =
                        SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            .format(event.start.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    Alignment.Center) {
                      Icon(
                          Icons.Default.Check,
                          contentDescription =
                              stringResource(R.string.content_description_selected_event_checkmark),
                          modifier = Modifier.size(14.dp),
                          tint = MaterialTheme.colorScheme.onPrimary)
                    }
              }
            }
      }
}

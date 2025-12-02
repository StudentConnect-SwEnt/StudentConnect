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

  // Trigger button
  Surface(
      modifier =
          modifier
              .clip(RoundedCornerShape(BUTTON_CORNER_RADIUS))
              .clickable {
                showDialog = true
                onLoadEvents()
              }
              .testTag(C.Tag.event_selection_button),
      shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
      color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  Icons.Default.Event,
                  null,
                  Modifier.size(20.dp),
                  MaterialTheme.colorScheme.primary)
              Spacer(Modifier.width(8.dp))
              Text(
                  text =
                      selectedEvent?.title ?: stringResource(R.string.event_selection_button_label),
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                      if (selectedEvent != null) MaterialTheme.colorScheme.onSurface
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            }
      }

  // Dialog
  if (showDialog) {
    Dialog(onDismissRequest = { showDialog = false }) {
      Surface(
          modifier =
              Modifier.fillMaxWidth()
                  .heightIn(max = DIALOG_MAX_HEIGHT)
                  .testTag(C.Tag.event_selection_dropdown),
          shape = RoundedCornerShape(DIALOG_CORNER_RADIUS),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp) {
            Column(Modifier.padding(16.dp)) {
              // Header
              Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.event_selection_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.size(24.dp).testTag(C.Tag.event_selection_close)) {
                      Icon(
                          Icons.Default.Close,
                          stringResource(R.string.content_description_close_event_selection))
                    }
              }
              Spacer(Modifier.height(16.dp))

              // Content
              when (state) {
                is EventSelectionState.Loading -> {
                  Box(
                      Modifier.fillMaxWidth()
                          .height(CARD_HEIGHT)
                          .testTag(C.Tag.event_selection_loading),
                      Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                      }
                }
                is EventSelectionState.Error -> {
                  Box(
                      Modifier.fillMaxWidth()
                          .height(CARD_HEIGHT)
                          .testTag(C.Tag.event_selection_error),
                      Alignment.Center) {
                        Text(
                            stringResource(R.string.event_selection_error),
                            color = MaterialTheme.colorScheme.error)
                      }
                }
                is EventSelectionState.Success -> {
                  if (state.events.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth()
                            .height(CARD_HEIGHT)
                            .testTag(C.Tag.event_selection_empty),
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
                          items(state.events, key = { it.uid }) { event ->
                            val isSelected = selectedEvent?.uid == event.uid
                            EventCard(
                                event = event,
                                isSelected = isSelected,
                                onClick = {
                                  onEventSelected(if (isSelected) null else event)
                                  showDialog = false
                                })
                          }
                        }
                  }
                }
              }
            }
          }
    }
  }
}

@Composable
private fun EventCard(event: Event, isSelected: Boolean, onClick: () -> Unit) {
  val bgColor =
      if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .height(CARD_HEIGHT)
              .clickable(onClick = onClick)
              .testTag("${C.Tag.event_selection_card_prefix}_${event.uid}"),
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
                          null,
                          Modifier.size(14.dp),
                          MaterialTheme.colorScheme.onPrimary)
                    }
              }
            }
      }
}

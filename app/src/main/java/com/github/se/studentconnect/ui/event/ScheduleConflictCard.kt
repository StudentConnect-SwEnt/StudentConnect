package com.github.se.studentconnect.ui.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.ui.profile.CalendarItem
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/** Test tags for the schedule conflict card. */
object ScheduleConflictCardTestTags {
  const val CARD = "schedule_conflict_card"
  const val NO_CONFLICT_CARD = "no_conflict_card"
  const val CONFLICT_HEADER = "conflict_header"
  const val EXPAND_BUTTON = "conflict_expand_button"
  const val CONFLICT_LIST = "conflict_list"

  fun conflictItem(uid: String) = "conflict_item_$uid"
}

/**
 * A beautiful card that shows schedule conflicts for an event. Displays if the user is free or busy
 * during the event time.
 *
 * @param eventStart Start time of the event
 * @param eventEnd End time of the event (optional)
 * @param conflictingItems List of items that conflict with this event
 * @param modifier Modifier for the card
 * @param onConflictClick Callback when a conflicting item is clicked
 */
@Composable
fun ScheduleConflictCard(
    eventStart: Timestamp,
    eventEnd: Timestamp?,
    conflictingItems: List<CalendarItem>,
    isCheckingConflicts: Boolean = false,
    modifier: Modifier = Modifier,
    onConflictClick: ((CalendarItem) -> Unit)? = null
) {
  var isExpanded by remember { mutableStateOf(false) }
  val hasConflicts = conflictingItems.isNotEmpty()

  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .testTag(
                  if (hasConflicts) ScheduleConflictCardTestTags.CARD
                  else ScheduleConflictCardTestTags.NO_CONFLICT_CARD),
      shape = RoundedCornerShape(16.dp),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (hasConflicts && !isCheckingConflicts) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                  } else if (isCheckingConflicts) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                  }),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Header
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .then(
                          if (hasConflicts && !isCheckingConflicts)
                              Modifier.clickable { isExpanded = !isExpanded }
                          else Modifier)
                      .testTag(ScheduleConflictCardTestTags.CONFLICT_HEADER),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)) {
                      // Status Icon with gradient background
                      Box(
                          modifier =
                              Modifier.size(44.dp)
                                  .clip(CircleShape)
                                  .background(
                                      if (isCheckingConflicts) {
                                        Brush.linearGradient(
                                            colors = listOf(Color.Gray, Color.LightGray))
                                      } else if (hasConflicts) {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53)))
                                      } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)))
                                      }),
                          contentAlignment = Alignment.Center) {
                            if (isCheckingConflicts) {
                              CircularProgressIndicator(
                                  modifier = Modifier.size(24.dp),
                                  color = Color.White,
                                  strokeWidth = 2.dp)
                            } else {
                              Icon(
                                  imageVector =
                                      if (hasConflicts) Icons.Default.Warning
                                      else Icons.Default.CheckCircle,
                                  contentDescription = null,
                                  tint = Color.White,
                                  modifier = Modifier.size(24.dp))
                            }
                          }

                      Spacer(modifier = Modifier.width(12.dp))

                      Column {
                        Text(
                            text =
                                if (isCheckingConflicts) "Checking availability..."
                                else if (hasConflicts) "Schedule Conflict" else "You're Free!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (isCheckingConflicts) {
                                  MaterialTheme.colorScheme.onSurface
                                } else if (hasConflicts) {
                                  MaterialTheme.colorScheme.error
                                } else {
                                  MaterialTheme.colorScheme.primary
                                })
                        Text(
                            text =
                                if (isCheckingConflicts) {
                                  "Please wait while we check your calendar"
                                } else if (hasConflicts) {
                                  "${conflictingItems.size} event${if (conflictingItems.size > 1) "s" else ""} at the same time"
                                } else {
                                  "No conflicts with your schedule"
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }

                if (hasConflicts) {
                  IconButton(
                      onClick = { isExpanded = !isExpanded },
                      modifier = Modifier.testTag(ScheduleConflictCardTestTags.EXPAND_BUTTON)) {
                        Icon(
                            imageVector =
                                if (isExpanded) Icons.Default.ExpandLess
                                else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                }
              }

          // Expanded content with conflicting items
          AnimatedVisibility(
              visible = isExpanded && hasConflicts,
              enter = expandVertically(),
              exit = shrinkVertically()) {
                Column(
                    modifier =
                        Modifier.padding(top = 12.dp)
                            .testTag(ScheduleConflictCardTestTags.CONFLICT_LIST)) {
                      HorizontalDivider(
                          modifier = Modifier.padding(bottom = 12.dp),
                          color = MaterialTheme.colorScheme.outlineVariant)

                      Text(
                          text = "Conflicting events:",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.padding(bottom = 8.dp))

                      conflictingItems.forEach { item ->
                        ConflictingEventItem(
                            item = item,
                            onClick = { onConflictClick?.invoke(item) },
                            modifier =
                                Modifier.padding(bottom = 8.dp)
                                    .testTag(ScheduleConflictCardTestTags.conflictItem(item.uid)))
                      }
                    }
              }
        }
      }
}

@Composable
private fun ConflictingEventItem(
    item: CalendarItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        is CalendarItem.AppEvent -> if (item.isOwner) "Your Event" else "Joined"
        is CalendarItem.Personal -> "Personal"
        is CalendarItem.Imported -> "Imported"
      }

  Card(
      modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
              // Color indicator
              Box(
                  modifier =
                      Modifier.width(4.dp)
                          .height(40.dp)
                          .clip(RoundedCornerShape(2.dp))
                          .background(eventColor))

              Spacer(modifier = Modifier.width(12.dp))

              // Event icon
              Box(
                  modifier =
                      Modifier.size(36.dp)
                          .clip(CircleShape)
                          .background(eventColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = eventColor,
                        modifier = Modifier.size(20.dp))
                  }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = item.title,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.SemiBold,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.weight(1f))

                      Surface(
                          shape = RoundedCornerShape(8.dp),
                          color = eventColor.copy(alpha = 0.15f)) {
                            Text(
                                text = eventTypeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = eventColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                          }
                    }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.AccessTime,
                      contentDescription = null,
                      modifier = Modifier.size(12.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                      text = if (endTime != null) "$startTime - $endTime" else startTime,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
              }
            }
      }
}

/** Compact version of the schedule conflict indicator for use in event cards. */
@Composable
fun ScheduleConflictBadge(
    hasConflict: Boolean,
    conflictCount: Int = 0,
    modifier: Modifier = Modifier
) {
  if (hasConflict) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFF6B6B).copy(alpha = 0.15f)) {
          Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Schedule conflict",
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF6B6B))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text =
                        if (conflictCount > 0)
                            "$conflictCount conflict${if (conflictCount > 1) "s" else ""}"
                        else "Busy",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.Medium)
              }
        }
  } else {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
          Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Free",
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Free",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium)
              }
        }
  }
}

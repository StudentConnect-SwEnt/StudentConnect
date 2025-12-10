package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import java.time.LocalDate
import java.time.LocalTime

// --- Constants ---
private val FIELD_SPACING = 20.dp

// --- Helper Data Classes to reduce parameter count ---

/** Holds state values for Date/Time fields. */
data class DateTimeState(
    val startDate: String,
    val startTime: LocalTime,
    val endDate: String,
    val endTime: LocalTime
)

/** Holds callbacks for Date/Time fields. */
data class DateTimeCallbacks(
    val onStartDateChange: (LocalDate?) -> Unit,
    val onStartTimeChange: (LocalTime) -> Unit,
    val onEndDateChange: (LocalDate?) -> Unit,
    val onEndTimeChange: (LocalTime) -> Unit
)

/** Holds state values for Participants and Fee fields. */
data class ParticipantsFeeState(
    val numberOfParticipants: String,
    val hasParticipationFee: Boolean,
    val participationFee: String,
    val isFlash: Boolean
)

/** Holds callbacks for Participants and Fee fields. */
data class ParticipantsFeeCallbacks(
    val onParticipantsChange: (String) -> Unit,
    val onHasFeeChange: (Boolean) -> Unit,
    val onFeeStringChange: (String) -> Unit,
    val onIsFlashChange: (Boolean) -> Unit
)

// --- Composables ---

/** Composable for Title and Description input fields. */
@Composable
fun EventTitleAndDescriptionFields(
    title: String,
    onTitleChange: (String) -> Unit,
    titleTag: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionTag: String,
    onFocusChange: (Boolean) -> Unit,
    titleError: String? = null
) {
  FormTextField(
      modifier =
          Modifier.fillMaxWidth().testTag(titleTag).onFocusChanged { onFocusChange(it.isFocused) },
      label = stringResource(R.string.event_label_title),
      placeholder = stringResource(R.string.event_placeholder_title),
      value = title,
      onValueChange = onTitleChange,
      errorText = titleError,
      required = true)

  FormTextField(
      modifier =
          Modifier.fillMaxWidth().testTag(descriptionTag).onFocusChanged {
            onFocusChange(it.isFocused)
          },
      label = stringResource(R.string.event_label_description),
      placeholder = stringResource(R.string.event_placeholder_description),
      value = description,
      onValueChange = onDescriptionChange,
  )
}

/** Composable for selecting an event banner image. */
@Composable
fun EventBannerField(
    bannerImageUri: Uri?,
    bannerImagePath: String?,
    onImageSelected: (Uri) -> Unit,
    onRemoveImage: () -> Unit,
    pickerTag: String,
    removeButtonTag: String
) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                  text = stringResource(R.string.event_label_banner),
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
              PicturePickerCard(
                  modifier = Modifier.fillMaxWidth().testTag(pickerTag),
                  style = PicturePickerStyle.Banner,
                  existingImagePath = bannerImagePath,
                  selectedImageUri = bannerImageUri,
                  onImageSelected = onImageSelected,
                  placeholderText = stringResource(R.string.event_placeholder_banner),
                  overlayText = stringResource(R.string.instruction_tap_to_change_photo),
                  imageDescription = stringResource(R.string.event_label_banner))
              Text(
                  text = stringResource(R.string.event_text_banner_help),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
              OutlinedButton(
                  onClick = onRemoveImage,
                  enabled = bannerImageUri != null || bannerImagePath != null,
                  modifier = Modifier.fillMaxWidth().testTag(removeButtonTag)) {
                    Text(stringResource(R.string.event_button_remove_banner))
                  }
            }
      }
}

/** Composable for Location input. */
@Composable
fun EventLocationField(
    location: Location?,
    onLocationChange: (Location?) -> Unit,
    testTag: String
) {
  LocationTextField(
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      label = stringResource(R.string.event_label_location),
      placeholder = stringResource(R.string.event_placeholder_location),
      selectedLocation = location,
      onLocationChange = onLocationChange)
}

/**
 * Composable for Start and End Date/Time selection. Uses [DateTimeState] and [DateTimeCallbacks] to
 * group parameters.
 */
@Composable
fun EventDateTimeFields(
    state: DateTimeState,
    callbacks: DateTimeCallbacks,
    startDateTag: String,
    startTimeTag: String,
    endDateTag: String,
    endTimeTag: String
) {
  val wideFieldWeight = 0.7f

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DateTextField(
        modifier = Modifier.weight(wideFieldWeight).testTag(startDateTag),
        label = stringResource(R.string.event_label_start_date),
        placeholder = stringResource(R.string.event_placeholder_date),
        initialValue = state.startDate,
        onDateChange = callbacks.onStartDateChange,
        required = true)

    TimePicker(
        modifier = Modifier.testTag(startTimeTag),
        time = state.startTime,
        onTimeChange = callbacks.onStartTimeChange)
  }

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DateTextField(
        modifier = Modifier.weight(wideFieldWeight).testTag(endDateTag),
        label = stringResource(R.string.event_label_end_date),
        placeholder = stringResource(R.string.event_placeholder_date),
        initialValue = state.endDate,
        onDateChange = callbacks.onEndDateChange,
        required = true)

    TimePicker(
        modifier = Modifier.testTag(endTimeTag),
        time = state.endTime,
        onTimeChange = callbacks.onEndTimeChange)
  }
}

/** Composable for Flash Event toggle. Shown before date/time fields. */
@Composable
fun FlashEventToggle(isFlash: Boolean, onIsFlashChange: (Boolean) -> Unit, flashSwitchTag: String) {
  val wideFieldWeight = 0.7f

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier = Modifier.weight(wideFieldWeight),
        text = stringResource(R.string.event_label_flash_event),
    )

    Switch(
        modifier = Modifier.testTag(flashSwitchTag),
        checked = isFlash,
        onCheckedChange = onIsFlashChange,
    )
  }
}

/** Composable for Flash Event duration picker (hours and minutes). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashEventDurationFields(
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    hoursTag: String,
    minutesTag: String
) {
  val hourOptions = (0..C.FlashEvent.MAX_DURATION_HOURS.toInt()).map { it.toString() }
  val allMinuteOptions = listOf(0, 15, 30, 45)
  // When 5 hours is selected, only allow 0 minutes (max duration is exactly 5 hours)
  val minuteOptions =
      if (hours == C.FlashEvent.MAX_DURATION_HOURS.toInt()) {
            listOf(0)
          } else {
            allMinuteOptions
          }
          .map { it.toString() }

  var hoursExpanded by remember { mutableStateOf(false) }
  var minutesExpanded by remember { mutableStateOf(false) }

  Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(FIELD_SPACING)) {
        Text(
            text = stringResource(R.string.event_label_flash_duration),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          // Hours dropdown
          ExposedDropdownMenuBox(
              expanded = hoursExpanded,
              onExpandedChange = { hoursExpanded = it },
              modifier = Modifier.weight(0.5f).testTag(hoursTag)) {
                OutlinedTextField(
                    value = hours.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.event_label_flash_duration_hours)) },
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = hoursExpanded)
                    },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable))
                ExposedDropdownMenu(
                    expanded = hoursExpanded, onDismissRequest = { hoursExpanded = false }) {
                      hourOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                              val selectedHours = option.toInt()
                              onHoursChange(selectedHours)
                              // If selecting max hours, reset minutes to 0
                              if (selectedHours == C.FlashEvent.MAX_DURATION_HOURS.toInt() &&
                                  minutes > 0) {
                                onMinutesChange(0)
                              }
                              hoursExpanded = false
                            })
                      }
                    }
              }

          // Minutes dropdown
          ExposedDropdownMenuBox(
              expanded = minutesExpanded,
              onExpandedChange = { minutesExpanded = it },
              modifier = Modifier.weight(0.5f).testTag(minutesTag)) {
                OutlinedTextField(
                    value = minutes.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.event_label_flash_duration_minutes)) },
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = minutesExpanded)
                    },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable))
                ExposedDropdownMenu(
                    expanded = minutesExpanded, onDismissRequest = { minutesExpanded = false }) {
                      minuteOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                              val selectedMinutes = option.toInt()
                              // If selecting non-zero minutes when at max hours, reset hours first
                              if (hours == C.FlashEvent.MAX_DURATION_HOURS.toInt() &&
                                  selectedMinutes > 0) {
                                onHoursChange(C.FlashEvent.MAX_DURATION_HOURS.toInt() - 1)
                              }
                              onMinutesChange(selectedMinutes)
                              minutesExpanded = false
                            })
                      }
                    }
              }
        }

        Text(
            text = stringResource(R.string.event_label_flash_starts_immediately),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

/**
 * Composable for Participants, Fees, and Flash settings. Uses [ParticipantsFeeState] and
 * [ParticipantsFeeCallbacks] to group parameters.
 */
@Composable
fun EventParticipantsAndFeesFields(
    state: ParticipantsFeeState,
    callbacks: ParticipantsFeeCallbacks,
    participantsTag: String,
    feeSwitchTag: String,
    feeInputTag: String,
    onFocusChange: (Boolean) -> Unit
) {
  val wideFieldWeight = 0.7f

  FormTextField(
      modifier =
          Modifier.fillMaxWidth().testTag(participantsTag).onFocusChanged {
            onFocusChange(it.isFocused)
          },
      label = stringResource(R.string.event_label_participants),
      value = state.numberOfParticipants,
      onValueChange = callbacks.onParticipantsChange,
  )

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(FIELD_SPACING),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    FormTextField(
        modifier =
            Modifier.weight(wideFieldWeight).testTag(feeInputTag).onFocusChanged {
              onFocusChange(it.isFocused)
            },
        label = stringResource(R.string.event_label_fees),
        value = state.participationFee,
        onValueChange = callbacks.onFeeStringChange,
        enabled = state.hasParticipationFee,
    )

    Switch(
        modifier = Modifier.testTag(feeSwitchTag),
        checked = state.hasParticipationFee,
        onCheckedChange = {
          callbacks.onHasFeeChange(it)
          if (!it) callbacks.onFeeStringChange("")
        },
    )
  }
}

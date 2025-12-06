package com.github.se.studentconnect.ui.eventcreation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import com.github.se.studentconnect.ui.utils.DialogNotImplemented
import java.time.LocalDate
import java.time.LocalTime

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

@Composable
fun EventDateTimeFields(
    startDate: String,
    onStartDateChange: (LocalDate?) -> Unit,
    startDateTag: String,
    startTime: LocalTime,
    onStartTimeChange: (LocalTime) -> Unit,
    startTimeTag: String,
    endDate: String,
    onEndDateChange: (LocalDate?) -> Unit,
    endDateTag: String,
    endTime: LocalTime,
    onEndTimeChange: (LocalTime) -> Unit,
    endTimeTag: String
) {
  val wideFieldWeight = 0.7f

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DateTextField(
        modifier = Modifier.weight(wideFieldWeight).testTag(startDateTag),
        label = stringResource(R.string.event_label_start_date),
        placeholder = stringResource(R.string.event_placeholder_date),
        initialValue = startDate,
        onDateChange = onStartDateChange,
        required = true)

    TimePicker(
        modifier = Modifier.testTag(startTimeTag),
        time = startTime,
        onTimeChange = onStartTimeChange)
  }

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DateTextField(
        modifier = Modifier.weight(wideFieldWeight).testTag(endDateTag),
        label = stringResource(R.string.event_label_end_date),
        placeholder = stringResource(R.string.event_placeholder_date),
        initialValue = endDate,
        onDateChange = onEndDateChange,
        required = true)

    TimePicker(
        modifier = Modifier.testTag(endTimeTag), time = endTime, onTimeChange = onEndTimeChange)
  }
}

@Composable
fun EventParticipantsAndFeesFields(
    numberOfParticipantsString: String,
    onParticipantsChange: (String) -> Unit,
    participantsTag: String,
    hasParticipationFee: Boolean,
    onHasFeeChange: (Boolean) -> Unit,
    feeSwitchTag: String,
    participationFeeString: String,
    onFeeStringChange: (String) -> Unit,
    feeInputTag: String,
    isFlash: Boolean,
    onIsFlashChange: (Boolean) -> Unit,
    flashSwitchTag: String,
    onFocusChange: (Boolean) -> Unit
) {
  val wideFieldWeight = 0.7f

  FormTextField(
      modifier =
          Modifier.fillMaxWidth().testTag(participantsTag).onFocusChanged {
            onFocusChange(it.isFocused)
          },
      label = stringResource(R.string.event_label_participants),
      value = numberOfParticipantsString,
      onValueChange = onParticipantsChange,
  )

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    FormTextField(
        modifier =
            Modifier.weight(wideFieldWeight).testTag(feeInputTag).onFocusChanged {
              onFocusChange(it.isFocused)
            },
        label = stringResource(R.string.event_label_fees),
        value = participationFeeString,
        onValueChange = onFeeStringChange,
        enabled = hasParticipationFee,
    )

    Switch(
        modifier = Modifier.testTag(feeSwitchTag),
        checked = hasParticipationFee,
        onCheckedChange = {
          onHasFeeChange(it)
          if (!it) onFeeStringChange("")
        },
    )
  }

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(20.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier = Modifier.weight(wideFieldWeight),
        text = stringResource(R.string.event_label_flash_event),
    )

    val context = LocalContext.current
    Switch(
        modifier = Modifier.testTag(flashSwitchTag),
        checked = isFlash,
        onCheckedChange = { DialogNotImplemented(context) },
    )
  }
}

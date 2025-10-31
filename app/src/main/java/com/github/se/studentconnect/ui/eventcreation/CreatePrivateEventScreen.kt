// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.ui.theme.AppTheme
import java.time.format.DateTimeFormatter

object CreatePrivateEventScreenTestTags {
  const val TITLE_INPUT = "titleInput"
  const val DESCRIPTION_INPUT = "descriptionInput"
  const val LOCATION_INPUT = "locationInput"
  const val START_DATE_INPUT = "startDateInput"
  const val START_TIME_BUTTON = "startTimeButton"
  const val END_DATE_INPUT = "endDateInput"
  const val END_TIME_BUTTON = "endTimeButton"
  const val NUMBER_OF_PARTICIPANTS_INPUT = "numberOfParticipantsInput"
  const val PARTICIPATION_FEE_INPUT = "participationFeeInput"
  const val PARTICIPATION_FEE_SWITCH = "participationFeeSwitch"
  const val FLASH_EVENT_SWITCH = "flashEventSwitch"
  const val SAVE_BUTTON = "saveButton"
}

@Composable
fun CreatePrivateEventScreen(
    modifier: Modifier = Modifier,
    existingEventId: String? = null,
    // TODO: pass NavController here
    createPrivateEventViewModel: CreatePrivateEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId) {
    existingEventId?.let { createPrivateEventViewModel.loadEvent(it) }
  }

  val createPrivateEventUiState by createPrivateEventViewModel.uiState.collectAsState()
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

  val startDateInitial = createPrivateEventUiState.startDate?.format(dateFormatter) ?: ""
  val endDateInitial = createPrivateEventUiState.endDate?.format(dateFormatter) ?: ""
  val locationInitial =
      createPrivateEventUiState.location?.let { it.name ?: "${it.latitude}, ${it.longitude}" } ?: ""

  val canSave =
      createPrivateEventUiState.title.isNotBlank() &&
          createPrivateEventUiState.startDate != null &&
          createPrivateEventUiState.endDate != null

  LaunchedEffect(createPrivateEventUiState.finishedSaving) {
    if (createPrivateEventUiState.finishedSaving) {
      // TODO: navigate out of this page
    }
  }

  Column(
      modifier =
          modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FormTextField(
        modifier = Modifier.fillMaxWidth().testTag(CreatePrivateEventScreenTestTags.TITLE_INPUT),
        label = "Title",
        placeholder = "My new event",
        value = createPrivateEventUiState.title,
        onValueChange = { createPrivateEventViewModel.updateTitle(it) },
        errorText =
            if (createPrivateEventUiState.title.isBlank()) "Title cannot be blank" else null)

    FormTextField(
        modifier =
            Modifier.fillMaxWidth().testTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT),
        label = "Description",
        placeholder = "Describe your event",
        value = createPrivateEventUiState.description,
        onValueChange = { createPrivateEventViewModel.updateDescription(it) },
    )

    LocationTextField(
        modifier = Modifier.fillMaxWidth().testTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT),
        label = "Location",
        placeholder = "Enter the event's location",
        initialValue = locationInitial,
        onLocationChange = { createPrivateEventViewModel.updateLocation(it) })

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DateTextField(
          modifier =
              Modifier.weight(0.7f).testTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT),
          label = "Start of the event",
          placeholder = "DD/MM/YYYY",
          initialValue = startDateInitial,
          onDateChange = { createPrivateEventViewModel.updateStartDate(it) },
      )

      TimePicker(
          modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON),
          time = createPrivateEventUiState.startTime,
          onTimeChange = { createPrivateEventViewModel.updateStartTime(it) })
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DateTextField(
          modifier = Modifier.weight(0.7f).testTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT),
          label = "End of the event",
          placeholder = "DD/MM/YYYY",
          initialValue = endDateInitial,
          onDateChange = { createPrivateEventViewModel.updateEndDate(it) },
      )

      TimePicker(
          modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON),
          time = createPrivateEventUiState.endTime,
          onTimeChange = { createPrivateEventViewModel.updateEndTime(it) })
    }

    FormTextField(
        modifier =
            Modifier.fillMaxWidth()
                .testTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT),
        label = "Number of participants",
        value = createPrivateEventUiState.numberOfParticipantsString,
        onValueChange = { createPrivateEventViewModel.updateNumberOfParticipantsString(it) },
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      FormTextField(
          modifier =
              Modifier.weight(0.7f)
                  .testTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT),
          label = "Participation fees",
          value = createPrivateEventUiState.participationFeeString,
          onValueChange = { createPrivateEventViewModel.updateParticipationFeeString(it) },
          enabled = createPrivateEventUiState.hasParticipationFee,
      )

      Switch(
          modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH),
          checked = createPrivateEventUiState.hasParticipationFee,
          onCheckedChange = {
            createPrivateEventViewModel.updateHasParticipationFee(it)
            if (!it) createPrivateEventViewModel.updateParticipationFeeString("")
          },
      )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          modifier = Modifier.weight(0.7f),
          text = "Flash Event",
      )

      Switch(
          modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH),
          checked = createPrivateEventUiState.isFlash,
          onCheckedChange = { createPrivateEventViewModel.updateIsFlash(it) },
      )
    }

    Button(
        modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON),
        enabled = canSave,
        onClick = { createPrivateEventViewModel.saveEvent() },
    ) {
      Icon(
          imageVector = Icons.Default.SaveAlt,
          contentDescription = "Save",
          modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.size(6.dp))
      Text("Save")
    }
  }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun CreatePrivateEventScreenPreview() {
  AppTheme { CreatePrivateEventScreen() }
}

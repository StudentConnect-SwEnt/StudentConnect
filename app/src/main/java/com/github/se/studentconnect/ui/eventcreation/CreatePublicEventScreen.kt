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
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.ui.theme.AppTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CreatePublicEventScreenTestTags {
  const val TITLE_INPUT = "titleInput"
  const val SUBTITLE_INPUT = "subtitleInput"
  const val DESCRIPTION_INPUT = "descriptionInput"
  const val LOCATION_INPUT = "locationInput"
  const val START_DATE_INPUT = "startDateInput"
  const val START_TIME_BUTTON = "startTimeButton"
  const val END_DATE_INPUT = "endDateInput"
  const val END_TIME_BUTTON = "endTimeButton"
  const val NUMBER_OF_PARTICIPANTS_INPUT = "numberOfParticipantsInput"
  const val WEBSITE_INPUT = "websiteInput"
  const val PARTICIPATION_FEE_INPUT = "participationFeeInput"
  const val PARTICIPATION_FEE_SWITCH = "participationFeeSwitch"
  const val FLASH_EVENT_SWITCH = "flashEventSwitch"
  const val SAVE_BUTTON = "saveButton"
}

@Composable
fun CreatePublicEventScreen(
    modifier: Modifier = Modifier,
    existingEvent: Event.Public? = null,
    // TODO: pass NavController here
    createPublicEventViewModel: CreatePublicEventViewModel = viewModel(),
) {
  val eventId = existingEvent?.uid

  LaunchedEffect(eventId) { existingEvent?.let { createPublicEventViewModel.prefill(it) } }

  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

  val startDateInitial =
      remember(eventId) {
        existingEvent
            ?.start
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
            ?.format(dateFormatter) ?: ""
      }

  val endDateInitial =
      remember(eventId) {
        val endTimestamp = existingEvent?.end ?: existingEvent?.start
        endTimestamp
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
            ?.format(dateFormatter) ?: ""
      }

  val locationInitial =
      remember(eventId) {
        existingEvent?.location?.let { it.name ?: "${it.latitude}, ${it.longitude}" } ?: ""
      }

  val createPublicEventUiState by createPublicEventViewModel.uiState.collectAsState()

  val canSave =
      createPublicEventUiState.title.isNotBlank() &&
          createPublicEventUiState.startDate != null &&
          createPublicEventUiState.endDate != null

  LaunchedEffect(createPublicEventUiState.finishedSaving) {
    if (createPublicEventUiState.finishedSaving) {
      // TODO: navigate out of this page
    }
  }

  Column(
      modifier =
          modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    /*
    val color = MaterialTheme.colorScheme.onSecondary
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(120.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f), 0f)
                )
                drawRoundRect(
                    color = color,
                    size = size,
                    style = stroke,
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Text("Upload a picture for your Event Page")
        }
    }
    */

    FormTextField(
        modifier = Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.TITLE_INPUT),
        label = "Title",
        placeholder = "My new event",
        value = createPublicEventUiState.title,
        onValueChange = { createPublicEventViewModel.updateTitle(it) },
        errorText = if (createPublicEventUiState.title.isBlank()) "Title cannot be blank" else null)

    FormTextField(
        modifier = Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT),
        label = "Subtitle",
        placeholder = "Optional supporting text",
        value = createPublicEventUiState.subtitle,
        onValueChange = { createPublicEventViewModel.updateSubtitle(it) },
    )

    FormTextField(
        modifier =
            Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT),
        label = "Description",
        placeholder = "Describe your event",
        value = createPublicEventUiState.description,
        onValueChange = { createPublicEventViewModel.updateDescription(it) },
    )

    LocationTextField(
        modifier = Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
        label = "Location",
        placeholder = "Enter the event's location",
        initialValue = locationInitial,
        onLocationChange = { createPublicEventViewModel.updateLocation(it) })

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DateTextField(
          modifier =
              Modifier.weight(0.7f).testTag(CreatePublicEventScreenTestTags.START_DATE_INPUT),
          label = "Start of the event",
          placeholder = "DD/MM/YYYY",
          initialValue = startDateInitial,
          onDateChange = { createPublicEventViewModel.updateStartDate(it) },
      )

      TimePicker(
          modifier = Modifier.testTag(CreatePublicEventScreenTestTags.START_TIME_BUTTON),
          time = createPublicEventUiState.startTime,
          onTimeChange = { createPublicEventViewModel.updateStartTime(it) })
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DateTextField(
          modifier = Modifier.weight(0.7f).testTag(CreatePublicEventScreenTestTags.END_DATE_INPUT),
          label = "End of the event",
          placeholder = "DD/MM/YYYY",
          initialValue = endDateInitial,
          onDateChange = { createPublicEventViewModel.updateEndDate(it) },
      )

      TimePicker(
          modifier = Modifier.testTag(CreatePublicEventScreenTestTags.END_TIME_BUTTON),
          time = createPublicEventUiState.endTime,
          onTimeChange = { createPublicEventViewModel.updateEndTime(it) })
    }

    FormTextField(
        modifier =
            Modifier.fillMaxWidth()
                .testTag(CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT),
        label = "Number of participants",
        value = createPublicEventUiState.numberOfParticipantsString,
        onValueChange = { createPublicEventViewModel.updateNumberOfParticipantsString(it) },
    )

    FormTextField(
        modifier = Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT),
        label = "Event website",
        value = createPublicEventUiState.website,
        onValueChange = { createPublicEventViewModel.updateWebsite(it) },
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      FormTextField(
          modifier =
              Modifier.weight(0.7f)
                  .testTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT),
          label = "Participation fees",
          value = createPublicEventUiState.participationFeeString,
          onValueChange = { createPublicEventViewModel.updateParticipationFeeString(it) },
          enabled = createPublicEventUiState.hasParticipationFee,
      )

      Switch(
          modifier = Modifier.testTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH),
          checked = createPublicEventUiState.hasParticipationFee,
          onCheckedChange = {
            createPublicEventViewModel.updateHasParticipationFee(it)
            if (!it) createPublicEventViewModel.updateParticipationFeeString("")
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
          modifier = Modifier.testTag(CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH),
          checked = createPublicEventUiState.isFlash,
          onCheckedChange = { createPublicEventViewModel.updateIsFlash(it) },
      )
    }

    Button(
        modifier = Modifier.testTag(CreatePublicEventScreenTestTags.SAVE_BUTTON),
        enabled = canSave,
        onClick = { createPublicEventViewModel.saveEvent() },
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
fun CreatePublicEventScreenPreview() {
  AppTheme { CreatePublicEventScreen() }
}

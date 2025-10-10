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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.ui.theme.AppTheme

@Composable
fun CreatePrivateEventScreen(
    modifier: Modifier = Modifier,
    // TODO: pass NavController here
    createPrivateEventViewModel: CreatePrivateEventViewModel = viewModel(),
) {
  val createPrivateEventUiState by createPrivateEventViewModel.uiState.collectAsState()

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
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FormTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Title",
        placeholder = "My new event",
        value = createPrivateEventUiState.title,
        onValueChange = { createPrivateEventViewModel.updateTitle(it) },
        errorText =
            if (createPrivateEventUiState.title.isBlank()) "Title cannot be blank" else null)

    FormTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Description",
        placeholder = "Describe your event",
        value = createPrivateEventUiState.description,
        onValueChange = { createPrivateEventViewModel.updateDescription(it) },
    )

    LocationTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Location",
        placeholder = "Enter the event's location",
        initialValue = "",
        onLocationChange = { createPrivateEventViewModel.updateLocation(it) })

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DateTextField(
          modifier = Modifier.weight(0.7f),
          label = "Start of the event",
          placeholder = "DD/MM/YYYY",
          initialValue = "",
          onDateChange = { createPrivateEventViewModel.updateStartDate(it) },
      )

      TimePicker(
          createPrivateEventUiState.startTime, { createPrivateEventViewModel.updateStartTime(it) })
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      DateTextField(
          modifier = Modifier.weight(0.7f),
          label = "End of the event",
          placeholder = "DD/MM/YYYY",
          initialValue = "",
          onDateChange = { createPrivateEventViewModel.updateEndDate(it) },
      )

      TimePicker(
          createPrivateEventUiState.endTime, { createPrivateEventViewModel.updateEndTime(it) })
    }

    FormTextField(
        modifier = Modifier.fillMaxWidth(),
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
          modifier = Modifier.weight(0.7f),
          label = "Participation fees",
          value = createPrivateEventUiState.participationFeeString,
          onValueChange = { createPrivateEventViewModel.updateParticipationFeeString(it) },
          enabled = createPrivateEventUiState.hasParticipationFee,
      )

      Switch(
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
          checked = createPrivateEventUiState.isFlash,
          onCheckedChange = { createPrivateEventViewModel.updateIsFlash(it) },
      )
    }

    Button(
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

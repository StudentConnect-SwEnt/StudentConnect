// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.ui.theme.AppTheme

@Composable
fun CreatePublicEventScreen(
    modifier: Modifier = Modifier,
    createPublicEventViewModel: CreatePublicEventViewModel = viewModel(),
) {
  val createPublicEventUiState by createPublicEventViewModel.uiState.collectAsState()

  Column(
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
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
        modifier = Modifier.fillMaxWidth(),
        label = "Title",
        placeholder = "My new event",
        value = createPublicEventUiState.title,
        onValueChange = { createPublicEventViewModel.updateTitle(it) },
    )

    FormTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Subtitle",
        placeholder = "Optional supporting text",
        value = createPublicEventUiState.subtitle,
        onValueChange = { createPublicEventViewModel.updateSubtitle(it) },
    )

    FormTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Description",
        placeholder = "Describe your event",
        value = createPublicEventUiState.description,
        onValueChange = { createPublicEventViewModel.updateDescription(it) },
    )

    LocationTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Location",
        placeholder = "Enter the event's location",
        initialValue = "",
        onLocationChange = { createPublicEventViewModel.updateLocation(it) }
    )

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
          onDateChange = { createPublicEventViewModel.updateStartDate(it) },
      )

      TimePicker(
          createPublicEventUiState.startTime, { createPublicEventViewModel.updateStartTime(it) })
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
            onDateChange = { createPublicEventViewModel.updateEndDate(it) },
        )

      TimePicker(createPublicEventUiState.endTime, { createPublicEventViewModel.updateEndTime(it) })
    }

    FormTextField(
        modifier = Modifier.fillMaxWidth(),
        label = "Number of participants",
        value = createPublicEventUiState.numberOfParticipantsString,
        onValueChange = { createPublicEventViewModel.updateNumberOfParticipantsString(it) },
    )

    FormTextField(
        modifier = Modifier.fillMaxWidth(),
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
          modifier = Modifier.weight(0.7f),
          label = "Participation fees",
          value = createPublicEventUiState.participationFeeString,
          onValueChange = { createPublicEventViewModel.updateParticipationFeeString(it) },
      )

      Switch(
          checked = createPublicEventUiState.hasParticipationFee,
          onCheckedChange = { createPublicEventViewModel.updateHasParticipationFee(it) },
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
          checked = createPublicEventUiState.isFlash,
          onCheckedChange = { createPublicEventViewModel.updateIsFlash(it) },
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun CreatePublicEventScreenPreview() {
  AppTheme { CreatePublicEventScreen() }
}

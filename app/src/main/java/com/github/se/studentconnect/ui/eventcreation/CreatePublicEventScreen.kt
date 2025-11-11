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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import com.github.se.studentconnect.ui.theme.AppTheme
import java.time.format.DateTimeFormatter

object CreatePublicEventScreenTestTags {
  const val TOP_APP_BAR = "topAppBar"
  const val BACK_BUTTON = "backButton"
  const val SCAFFOLD = "scaffold"
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
  const val BANNER_PICKER = "bannerPicker"
  const val REMOVE_BANNER_BUTTON = "removeBannerButton"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicEventScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    existingEventId: String? = null,
    createPublicEventViewModel: CreatePublicEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId) {
    existingEventId?.let { createPublicEventViewModel.loadEvent(it) }
  }

  val createPublicEventUiState by createPublicEventViewModel.uiState.collectAsState()
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
  val startDateInitial = createPublicEventUiState.startDate?.format(dateFormatter) ?: ""
  val endDateInitial = createPublicEventUiState.endDate?.format(dateFormatter) ?: ""
  val locationInitial =
      createPublicEventUiState.location?.let { it.name ?: "${it.latitude}, ${it.longitude}" } ?: ""

  val canSave =
      createPublicEventUiState.title.isNotBlank() &&
          createPublicEventUiState.startDate != null &&
          createPublicEventUiState.endDate != null &&
          !createPublicEventUiState.isSaving

  LaunchedEffect(createPublicEventUiState.finishedSaving) {
    if (createPublicEventUiState.finishedSaving) {
      navController?.popBackStack()
      createPublicEventViewModel.resetFinishedSaving()
    }
  }

  Scaffold(
      modifier = modifier.testTag(CreatePublicEventScreenTestTags.SCAFFOLD),
      topBar = {
        TopAppBar(
            modifier = Modifier.testTag(CreatePublicEventScreenTestTags.TOP_APP_BAR),
            title = {
              Text(
                  text =
                      if (existingEventId != null) "Edit Public Event" else "Create Public Event")
            },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag(CreatePublicEventScreenTestTags.BACK_BUTTON),
                  onClick = { navController?.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      floatingActionButton = {
        FloatingActionButton(
            modifier = Modifier.testTag(CreatePublicEventScreenTestTags.SAVE_BUTTON).width(120.dp),
            onClick = { createPublicEventViewModel.saveEvent() },
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
          Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null)
                Text("Save", style = MaterialTheme.typography.titleMedium)
              }
        }
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
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
              modifier =
                  Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.TITLE_INPUT),
              label = "Title",
              placeholder = "My new event",
              value = createPublicEventUiState.title,
              onValueChange = { createPublicEventViewModel.updateTitle(it) },
              errorText =
                  if (createPublicEventUiState.title.isBlank()) "Title cannot be blank" else null,
              required = true)

          FormTextField(
              modifier =
                  Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT),
              label = "Subtitle",
              placeholder = "Optional supporting text",
              value = createPublicEventUiState.subtitle,
              onValueChange = { createPublicEventViewModel.updateSubtitle(it) },
          )

          FormTextField(
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT),
              label = "Description",
              placeholder = "Describe your event",
              value = createPublicEventUiState.description,
              onValueChange = { createPublicEventViewModel.updateDescription(it) },
          )

          Card(
              modifier = Modifier.fillMaxWidth(),
              colors =
                  CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      Text(
                          text = "Event Banner",
                          style =
                              MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.SemiBold))
                      PicturePickerCard(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(CreatePublicEventScreenTestTags.BANNER_PICKER),
                          style = PicturePickerStyle.Banner,
                          existingImagePath = createPublicEventUiState.bannerImagePath,
                          selectedImageUri = createPublicEventUiState.bannerImageUri,
                          onImageSelected = { uri ->
                            createPublicEventViewModel.updateBannerImageUri(uri)
                          },
                          placeholderText = "Upload a banner for your event page",
                          overlayText = "Tap to change banner",
                          imageDescription = "Event banner")
                      Text(
                          text = "A banner makes your public event stand out in search results.",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      OutlinedButton(
                          onClick = { createPublicEventViewModel.removeBannerImage() },
                          enabled =
                              createPublicEventUiState.bannerImageUri != null ||
                                  createPublicEventUiState.bannerImagePath != null,
                          modifier =
                              Modifier.fillMaxWidth()
                                  .testTag(CreatePublicEventScreenTestTags.REMOVE_BANNER_BUTTON)) {
                            Text("Remove banner")
                          }
                    }
              }

          LocationTextField(
              modifier =
                  Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.LOCATION_INPUT),
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
                required = true)

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
                modifier =
                    Modifier.weight(0.7f).testTag(CreatePublicEventScreenTestTags.END_DATE_INPUT),
                label = "End of the event",
                placeholder = "DD/MM/YYYY",
                initialValue = endDateInitial,
                onDateChange = { createPublicEventViewModel.updateEndDate(it) },
                required = true)

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
              modifier =
                  Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT),
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
                modifier =
                    Modifier.testTag(CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH),
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

          // Add some bottom padding to avoid FAB overlap
          Spacer(modifier = Modifier.size(80.dp))
        }
      }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun CreatePublicEventScreenPreview() {
  AppTheme { CreatePublicEventScreen() }
}

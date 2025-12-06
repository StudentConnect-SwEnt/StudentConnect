package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.Activities
import com.github.se.studentconnect.ui.components.TopicChipGrid
import com.github.se.studentconnect.ui.navigation.Route
import java.time.format.DateTimeFormatter

object CreatePublicEventScreenTestTags {
  const val TOP_APP_BAR = "topAppBar"
  const val BACK_BUTTON = "backButton"
  const val SCAFFOLD = "scaffold"
  const val SCROLL_COLUMN = "scrollColumn"
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
  const val TAG_SELECTOR = "tagSelector"
}

@Composable
fun CreatePublicEventScreen(
    navController: NavHostController?,
    existingEventId: String? = null,
    createPublicEventViewModel: CreatePublicEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId) {
    if (existingEventId != null) createPublicEventViewModel.loadEvent(existingEventId)
  }

  val uiState by createPublicEventViewModel.uiState.collectAsState()
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

  LaunchedEffect(Unit) {
    createPublicEventViewModel.navigateToEvent.collect { eventId ->
      navController?.navigate(Route.ACTIVITIES) { popUpTo(Route.HOME) { inclusive = false } }
      navController?.navigate(Route.eventView(eventId, true))
      createPublicEventViewModel.resetFinishedSaving()
    }
  }

  val canSave =
      uiState.title.isNotBlank() &&
          uiState.startDate != null &&
          uiState.endDate != null &&
          !uiState.isSaving

  var selectedTagCategory by rememberSaveable {
    mutableStateOf(Activities.filterOptions.firstOrNull() ?: "")
  }

  CreateEventShell(
      navController = navController,
      title =
          if (existingEventId != null) stringResource(R.string.title_edit_public_event)
          else stringResource(R.string.title_create_public_event),
      canSave = canSave,
      onSave = { createPublicEventViewModel.saveEvent() },
      scaffoldTestTag = CreatePublicEventScreenTestTags.SCAFFOLD,
      topBarTestTag = CreatePublicEventScreenTestTags.TOP_APP_BAR,
      backButtonTestTag = CreatePublicEventScreenTestTags.BACK_BUTTON,
      scrollColumnTestTag = CreatePublicEventScreenTestTags.SCROLL_COLUMN,
      saveButtonTestTag = CreatePublicEventScreenTestTags.SAVE_BUTTON) { onFocusChange ->

        // Title
        FormTextField(
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(CreatePublicEventScreenTestTags.TITLE_INPUT)
                    .onFocusChanged { onFocusChange(it.isFocused) },
            label = stringResource(R.string.event_label_title),
            placeholder = stringResource(R.string.event_placeholder_title),
            value = uiState.title,
            onValueChange = createPublicEventViewModel::updateTitle,
            errorText =
                if (uiState.title.isBlank()) stringResource(R.string.event_error_title_blank)
                else null,
            required = true)

        // Subtitle (Specific to Public)
        FormTextField(
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(CreatePublicEventScreenTestTags.SUBTITLE_INPUT)
                    .onFocusChanged { onFocusChange(it.isFocused) },
            label = stringResource(R.string.event_label_subtitle),
            placeholder = stringResource(R.string.event_placeholder_subtitle),
            value = uiState.subtitle,
            onValueChange = createPublicEventViewModel::updateSubtitle,
        )

        // Description
        FormTextField(
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(CreatePublicEventScreenTestTags.DESCRIPTION_INPUT)
                    .onFocusChanged { onFocusChange(it.isFocused) },
            label = stringResource(R.string.event_label_description),
            placeholder = stringResource(R.string.event_placeholder_description),
            value = uiState.description,
            onValueChange = createPublicEventViewModel::updateDescription,
        )

        // Banner
        EventBannerField(
            bannerImageUri = uiState.bannerImageUri,
            bannerImagePath = uiState.bannerImagePath,
            onImageSelected = createPublicEventViewModel::updateBannerImageUri,
            onRemoveImage = createPublicEventViewModel::removeBannerImage,
            pickerTag = CreatePublicEventScreenTestTags.BANNER_PICKER,
            removeButtonTag = CreatePublicEventScreenTestTags.REMOVE_BANNER_BUTTON)

        // Tags (Specific to Public)
        Column(
            modifier =
                Modifier.fillMaxWidth().testTag(CreatePublicEventScreenTestTags.TAG_SELECTOR),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = stringResource(R.string.event_label_tags),
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface)

              val availableTagOptions =
                  Activities.experienceTopics[selectedTagCategory] ?: emptyList()
              TopicChipGrid(
                  tags = availableTagOptions,
                  selectedTags = uiState.tags.toSet(),
                  onTagToggle = { tag ->
                    val updatedTags =
                        if (uiState.tags.contains(tag)) uiState.tags - tag else uiState.tags + tag
                    createPublicEventViewModel.updateTags(updatedTags)
                  },
                  modifier = Modifier.fillMaxWidth(),
                  filterOptions = Activities.filterOptions,
                  selectedFilter = selectedTagCategory,
                  onFilterSelected = { selectedTagCategory = it })
            }

        // Common Bottom Fields
        EventLocationField(
            location = uiState.location,
            onLocationChange = createPublicEventViewModel::updateLocation,
            testTag = CreatePublicEventScreenTestTags.LOCATION_INPUT)

        EventDateTimeFields(
            startDate = uiState.startDate?.format(dateFormatter) ?: "",
            onStartDateChange = createPublicEventViewModel::updateStartDate,
            startDateTag = CreatePublicEventScreenTestTags.START_DATE_INPUT,
            startTime = uiState.startTime,
            onStartTimeChange = createPublicEventViewModel::updateStartTime,
            startTimeTag = CreatePublicEventScreenTestTags.START_TIME_BUTTON,
            endDate = uiState.endDate?.format(dateFormatter) ?: "",
            onEndDateChange = createPublicEventViewModel::updateEndDate,
            endDateTag = CreatePublicEventScreenTestTags.END_DATE_INPUT,
            endTime = uiState.endTime,
            onEndTimeChange = createPublicEventViewModel::updateEndTime,
            endTimeTag = CreatePublicEventScreenTestTags.END_TIME_BUTTON)

        EventParticipantsAndFeesFields(
            numberOfParticipantsString = uiState.numberOfParticipantsString,
            onParticipantsChange = createPublicEventViewModel::updateNumberOfParticipantsString,
            participantsTag = CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT,
            hasParticipationFee = uiState.hasParticipationFee,
            onHasFeeChange = createPublicEventViewModel::updateHasParticipationFee,
            feeSwitchTag = CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH,
            participationFeeString = uiState.participationFeeString,
            onFeeStringChange = createPublicEventViewModel::updateParticipationFeeString,
            feeInputTag = CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT,
            isFlash = uiState.isFlash,
            onIsFlashChange = createPublicEventViewModel::updateIsFlash,
            flashSwitchTag = CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH,
            onFocusChange = onFocusChange)

        // Website (Specific to Public)
        FormTextField(
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(CreatePublicEventScreenTestTags.WEBSITE_INPUT)
                    .onFocusChanged { onFocusChange(it.isFocused) },
            label = stringResource(R.string.event_label_website),
            value = uiState.website,
            onValueChange = createPublicEventViewModel::updateWebsite,
        )
      }
}

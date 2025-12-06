package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.navigation.Route
import java.time.format.DateTimeFormatter

object CreatePrivateEventScreenTestTags {
  const val SCAFFOLD = "scaffold"
  const val TOP_APP_BAR = "topAppBar"
  const val BACK_BUTTON = "backButton"
  const val SCROLL_COLUMN = "scrollColumn"
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
  const val BANNER_PICKER = "bannerPicker"
  const val REMOVE_BANNER_BUTTON = "removeBannerButton"
}

@Composable
fun CreatePrivateEventScreen(
    navController: NavHostController?,
    existingEventId: String? = null,
    createPrivateEventViewModel: CreatePrivateEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId) {
    if (existingEventId != null) createPrivateEventViewModel.loadEvent(existingEventId)
  }

  val uiState by createPrivateEventViewModel.uiState.collectAsState()
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

  LaunchedEffect(Unit) {
    createPrivateEventViewModel.navigateToEvent.collect { eventId ->
      navController?.navigate(Route.ACTIVITIES) { popUpTo(Route.HOME) { inclusive = false } }
      navController?.navigate(Route.eventView(eventId, true))
      createPrivateEventViewModel.resetFinishedSaving()
    }
  }

  val canSave =
      uiState.title.isNotBlank() &&
          uiState.startDate != null &&
          uiState.endDate != null &&
          !uiState.isSaving

  CreateEventShell(
      navController = navController,
      title =
          if (existingEventId != null) stringResource(R.string.title_edit_private_event)
          else stringResource(R.string.title_create_private_event),
      canSave = canSave,
      onSave = { createPrivateEventViewModel.saveEvent() },
      scaffoldTestTag = CreatePrivateEventScreenTestTags.SCAFFOLD,
      topBarTestTag = CreatePrivateEventScreenTestTags.TOP_APP_BAR,
      backButtonTestTag = CreatePrivateEventScreenTestTags.BACK_BUTTON,
      scrollColumnTestTag = CreatePrivateEventScreenTestTags.SCROLL_COLUMN,
      saveButtonTestTag = CreatePrivateEventScreenTestTags.SAVE_BUTTON) { onFocusChange ->
        EventTitleAndDescriptionFields(
            title = uiState.title,
            onTitleChange = createPrivateEventViewModel::updateTitle,
            titleTag = CreatePrivateEventScreenTestTags.TITLE_INPUT,
            description = uiState.description,
            onDescriptionChange = createPrivateEventViewModel::updateDescription,
            descriptionTag = CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT,
            onFocusChange = onFocusChange,
            titleError =
                if (uiState.title.isBlank()) stringResource(R.string.event_error_title_blank)
                else null)

        EventBannerField(
            bannerImageUri = uiState.bannerImageUri,
            bannerImagePath = uiState.bannerImagePath,
            onImageSelected = createPrivateEventViewModel::updateBannerImageUri,
            onRemoveImage = createPrivateEventViewModel::removeBannerImage,
            pickerTag = CreatePrivateEventScreenTestTags.BANNER_PICKER,
            removeButtonTag = CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)

        EventLocationField(
            location = uiState.location,
            onLocationChange = createPrivateEventViewModel::updateLocation,
            testTag = CreatePrivateEventScreenTestTags.LOCATION_INPUT)

        EventDateTimeFields(
            startDate = uiState.startDate?.format(dateFormatter) ?: "",
            onStartDateChange = createPrivateEventViewModel::updateStartDate,
            startDateTag = CreatePrivateEventScreenTestTags.START_DATE_INPUT,
            startTime = uiState.startTime,
            onStartTimeChange = createPrivateEventViewModel::updateStartTime,
            startTimeTag = CreatePrivateEventScreenTestTags.START_TIME_BUTTON,
            endDate = uiState.endDate?.format(dateFormatter) ?: "",
            onEndDateChange = createPrivateEventViewModel::updateEndDate,
            endDateTag = CreatePrivateEventScreenTestTags.END_DATE_INPUT,
            endTime = uiState.endTime,
            onEndTimeChange = createPrivateEventViewModel::updateEndTime,
            endTimeTag = CreatePrivateEventScreenTestTags.END_TIME_BUTTON)

        EventParticipantsAndFeesFields(
            numberOfParticipantsString = uiState.numberOfParticipantsString,
            onParticipantsChange = createPrivateEventViewModel::updateNumberOfParticipantsString,
            participantsTag = CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT,
            hasParticipationFee = uiState.hasParticipationFee,
            onHasFeeChange = createPrivateEventViewModel::updateHasParticipationFee,
            feeSwitchTag = CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH,
            participationFeeString = uiState.participationFeeString,
            onFeeStringChange = createPrivateEventViewModel::updateParticipationFeeString,
            feeInputTag = CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT,
            isFlash = uiState.isFlash,
            onIsFlashChange = createPrivateEventViewModel::updateIsFlash,
            flashSwitchTag = CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH,
            onFocusChange = onFocusChange)
      }
}

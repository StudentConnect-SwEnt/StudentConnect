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

/** Constant Test Tags for the Private Event Screen */
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

/**
 * Screen for creating or editing a Private Event.
 *
 * @param navController Navigation controller to handle screen transitions.
 * @param existingEventId The ID of the event to edit, or null if creating a new event.
 * @param createPrivateEventViewModel The ViewModel managing state for private events.
 */
@Composable
fun CreatePrivateEventScreen(
    navController: NavHostController?,
    existingEventId: String? = null,
    templateEventId: String? = null,
    createPrivateEventViewModel: CreatePrivateEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId, templateEventId) {
    when {
      existingEventId != null -> createPrivateEventViewModel.loadEvent(existingEventId)
      templateEventId != null -> createPrivateEventViewModel.loadEventAsTemplate(templateEventId)
    }
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

  // Group test tags for the shell
  val shellTestTags =
      CreateEventShellTestTags(
          scaffold = CreatePrivateEventScreenTestTags.SCAFFOLD,
          topBar = CreatePrivateEventScreenTestTags.TOP_APP_BAR,
          backButton = CreatePrivateEventScreenTestTags.BACK_BUTTON,
          scrollColumn = CreatePrivateEventScreenTestTags.SCROLL_COLUMN,
          saveButton = CreatePrivateEventScreenTestTags.SAVE_BUTTON)

  CreateEventShell(
      navController = navController,
      title =
          if (existingEventId != null) stringResource(R.string.title_edit_private_event)
          else stringResource(R.string.title_create_private_event),
      canSave = canSave,
      onSave = { createPrivateEventViewModel.saveEvent() },
      testTags = shellTestTags) { onFocusChange ->

        // Title and Description
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

        // Banner Image
        EventBannerField(
            bannerImageUri = uiState.bannerImageUri,
            bannerImagePath = uiState.bannerImagePath,
            onImageSelected = createPrivateEventViewModel::updateBannerImageUri,
            onRemoveImage = createPrivateEventViewModel::removeBannerImage,
            pickerTag = CreatePrivateEventScreenTestTags.BANNER_PICKER,
            removeButtonTag = CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)

        // Location
        EventLocationField(
            location = uiState.location,
            onLocationChange = createPrivateEventViewModel::updateLocation,
            testTag = CreatePrivateEventScreenTestTags.LOCATION_INPUT)

        // Date and Time (Grouped into State and Callbacks)
        EventDateTimeFields(
            state =
                DateTimeState(
                    startDate = uiState.startDate?.format(dateFormatter) ?: "",
                    startTime = uiState.startTime,
                    endDate = uiState.endDate?.format(dateFormatter) ?: "",
                    endTime = uiState.endTime),
            callbacks =
                DateTimeCallbacks(
                    onStartDateChange = createPrivateEventViewModel::updateStartDate,
                    onStartTimeChange = createPrivateEventViewModel::updateStartTime,
                    onEndDateChange = createPrivateEventViewModel::updateEndDate,
                    onEndTimeChange = createPrivateEventViewModel::updateEndTime),
            startDateTag = CreatePrivateEventScreenTestTags.START_DATE_INPUT,
            startTimeTag = CreatePrivateEventScreenTestTags.START_TIME_BUTTON,
            endDateTag = CreatePrivateEventScreenTestTags.END_DATE_INPUT,
            endTimeTag = CreatePrivateEventScreenTestTags.END_TIME_BUTTON)

        // Participants and Fees (Grouped into State and Callbacks)
        EventParticipantsAndFeesFields(
            state =
                ParticipantsFeeState(
                    numberOfParticipants = uiState.numberOfParticipantsString,
                    hasParticipationFee = uiState.hasParticipationFee,
                    participationFee = uiState.participationFeeString,
                    isFlash = uiState.isFlash),
            callbacks =
                ParticipantsFeeCallbacks(
                    onParticipantsChange =
                        createPrivateEventViewModel::updateNumberOfParticipantsString,
                    onHasFeeChange = createPrivateEventViewModel::updateHasParticipationFee,
                    onFeeStringChange = createPrivateEventViewModel::updateParticipationFeeString,
                    onIsFlashChange = createPrivateEventViewModel::updateIsFlash),
            participantsTag = CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT,
            feeSwitchTag = CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH,
            feeInputTag = CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT,
            flashSwitchTag = CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH,
            onFocusChange = onFocusChange)
      }
}

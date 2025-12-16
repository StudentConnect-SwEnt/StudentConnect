package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.material3.SnackbarHost
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.navigation.Route
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

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
  const val FLASH_DURATION_HOURS = "flashDurationHours"
  const val FLASH_DURATION_MINUTES = "flashDurationMinutes"
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
  val offlineMessageRes by createPrivateEventViewModel.offlineMessageRes.collectAsState()
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
  val context = LocalContext.current
  var showGeminiDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    createPrivateEventViewModel.navigateToEvent.collect { eventId ->
      navController?.navigate(Route.ACTIVITIES) { popUpTo(Route.HOME) { inclusive = false } }
      navController?.navigate(Route.eventView(eventId, true))
      createPrivateEventViewModel.resetFinishedSaving()
    }
  }

  LaunchedEffect(Unit) {
    createPrivateEventViewModel.snackbarMessage.collect { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  // Show snackbar for offline messages
  LaunchedEffect(offlineMessageRes) {
    offlineMessageRes?.let { messageRes ->
      coroutineScope.launch {
        snackbarHostState.showSnackbar(context.getString(messageRes))
        createPrivateEventViewModel.clearOfflineMessage()
      }
    }
  }

  val canSave =
      uiState.title.isNotBlank() &&
          ((uiState.isFlash &&
              uiState.flashDurationHours * 60 + uiState.flashDurationMinutes > 0) ||
              (!uiState.isFlash && uiState.startDate != null && uiState.endDate != null)) &&
          !uiState.isSaving

  // Group test tags for the shell
  val shellTestTags =
      CreateEventShellTestTags(
          scaffold = CreatePrivateEventScreenTestTags.SCAFFOLD,
          topBar = CreatePrivateEventScreenTestTags.TOP_APP_BAR,
          backButton = CreatePrivateEventScreenTestTags.BACK_BUTTON,
          scrollColumn = CreatePrivateEventScreenTestTags.SCROLL_COLUMN,
          saveButton = CreatePrivateEventScreenTestTags.SAVE_BUTTON)

    Box(modifier = Modifier.fillMaxSize()) {
        CreateEventShell(
          navController = navController,
          title =
              if (existingEventId != null) stringResource(R.string.title_edit_private_event)
              else stringResource(R.string.title_create_private_event),
          canSave = canSave,
          onSave = { createPrivateEventViewModel.saveEvent(context) },
          testTags = shellTestTags,
          snackbarHost = { SnackbarHost(snackbarHostState) }) { onFocusChange ->

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
            removeButtonTag = CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON,
            isGenerating = uiState.isGeneratingBanner,
            onGeminiClick = { showGeminiDialog = true })

        val context = LocalContext.current
        if (showGeminiDialog) {
          GeminiPromptDialog(
              onDismiss = { showGeminiDialog = false },
              onGenerate = { prompt ->
                createPrivateEventViewModel.generateBanner(context, prompt)
                showGeminiDialog = false
              },
              isLoading = uiState.isGeneratingBanner)
        }

          // Location
          EventLocationField(
              location = uiState.location,
              onLocationChange = createPrivateEventViewModel::updateLocation,
              testTag = CreatePrivateEventScreenTestTags.LOCATION_INPUT,
              snackbarHostState = snackbarHostState)

          // Flash Event Toggle (before date/time fields)
          FlashEventToggle(
              isFlash = uiState.isFlash,
              onIsFlashChange = createPrivateEventViewModel::updateIsFlash,
              flashSwitchTag = CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH)

          // Conditional: Show duration picker for flash events, date/time for normal events
          if (uiState.isFlash) {
            FlashEventDurationFields(
                hours = uiState.flashDurationHours,
                minutes = uiState.flashDurationMinutes,
                onHoursChange = createPrivateEventViewModel::updateFlashDurationHours,
                onMinutesChange = createPrivateEventViewModel::updateFlashDurationMinutes,
                hoursTag = CreatePrivateEventScreenTestTags.FLASH_DURATION_HOURS,
                minutesTag = CreatePrivateEventScreenTestTags.FLASH_DURATION_MINUTES)
          } else {
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
          }

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
              onFocusChange = onFocusChange)
        }

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

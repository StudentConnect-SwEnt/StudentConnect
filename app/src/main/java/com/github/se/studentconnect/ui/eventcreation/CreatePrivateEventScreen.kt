package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
  const val FLASH_DURATION_HOURS = "flashDurationHours"
  const val FLASH_DURATION_MINUTES = "flashDurationMinutes"
  const val SAVE_BUTTON = "saveButton"
  const val BANNER_PICKER = "bannerPicker"
  const val REMOVE_BANNER_BUTTON = "removeBannerButton"
  const val CREATE_AS_ORG_SWITCH = "createAsOrgSwitch"
  const val SELECT_ORG_DROPDOWN = "selectOrgDropdown"
}

/**
 * Screen for creating or editing a Private Event.
 *
 * @param navController Navigation controller to handle screen transitions.
 * @param existingEventId The ID of the event to edit, or null if creating a new event.
 * @param createPrivateEventViewModel The ViewModel managing state for private events.
 */
@androidx.compose.material3.ExperimentalMaterial3Api
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

        // Organization Selection (Only show if user owns organizations)
        if (uiState.userOrganizations.isNotEmpty()) {
          androidx.compose.foundation.layout.Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.event_label_create_as_organization),
                          style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                          color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                      Switch(
                          checked = uiState.createAsOrganization,
                          onCheckedChange = createPrivateEventViewModel::updateCreateAsOrganization,
                          modifier =
                              Modifier.testTag(
                                  CreatePrivateEventScreenTestTags.CREATE_AS_ORG_SWITCH))
                    }

                // Organization Dropdown (shown when toggle is on)
                if (uiState.createAsOrganization) {
                  var expanded by remember { mutableStateOf(false) }
                  val selectedOrgName =
                      uiState.userOrganizations
                          .find { it.first == uiState.selectedOrganizationId }
                          ?.second ?: ""

                  ExposedDropdownMenuBox(
                      expanded = expanded,
                      onExpandedChange = { expanded = !expanded },
                      modifier =
                          Modifier.fillMaxWidth()
                              .testTag(CreatePrivateEventScreenTestTags.SELECT_ORG_DROPDOWN)) {
                        OutlinedTextField(
                            value = selectedOrgName,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                              Text(stringResource(R.string.event_label_select_organization))
                            },
                            trailingIcon = {
                              ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors())

                        ExposedDropdownMenu(
                            expanded = expanded, onDismissRequest = { expanded = false }) {
                              uiState.userOrganizations.forEach { (orgId, orgName) ->
                                DropdownMenuItem(
                                    text = { Text(orgName) },
                                    onClick = {
                                      createPrivateEventViewModel.updateSelectedOrganizationId(
                                          orgId)
                                      expanded = false
                                    },
                                    modifier = Modifier.testTag("orgDropdownItem_$orgId"))
                              }
                            }
                      }
                }
              }
        }

        // Location
        EventLocationField(
            location = uiState.location,
            onLocationChange = createPrivateEventViewModel::updateLocation,
            testTag = CreatePrivateEventScreenTestTags.LOCATION_INPUT)

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
}

package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

/** Constant Test Tags for the Public Event Screen */
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
  const val FLASH_DURATION_HOURS = "flashDurationHours"
  const val FLASH_DURATION_MINUTES = "flashDurationMinutes"
  const val SAVE_BUTTON = "saveButton"
  const val BANNER_PICKER = "bannerPicker"
  const val REMOVE_BANNER_BUTTON = "removeBannerButton"
  const val TAG_SELECTOR = "tagSelector"
  const val CREATE_AS_ORG_SWITCH = "createAsOrgSwitch"
  const val ORG_DROPDOWN = "orgDropdown"
}

/**
 * Screen for creating or editing a Public Event.
 *
 * Features include:
 * - Dynamic banner image upload
 * - Date/time selection with validation
 * - Location picker integration
 * - Optional participation fees and participant limits
 * - Animated save button that adapts based on scroll position and keyboard state
 * - Auto-save on focus loss for text fields
 *
 * @param navController Navigation controller for screen navigation
 * @param existingEventId Optional event ID for editing an existing event
 * @param templateEventId Optional template event ID for creating an event from a template
 * @param createPublicEventViewModel ViewModel managing the event creation/editing state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicEventScreen(
    navController: NavHostController?,
    existingEventId: String? = null,
    templateEventId: String? = null,
    createPublicEventViewModel: CreatePublicEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId, templateEventId) {
    handlePrefill(existingEventId, templateEventId, createPublicEventViewModel)
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
          ((uiState.isFlash &&
              uiState.flashDurationHours * 60 + uiState.flashDurationMinutes > 0) ||
              (!uiState.isFlash && uiState.startDate != null && uiState.endDate != null)) &&
          !uiState.isSaving

  var selectedTagCategory by rememberSaveable {
    mutableStateOf(Activities.filterOptions.firstOrNull() ?: "")
  }

  val shellTestTags =
      CreateEventShellTestTags(
          scaffold = CreatePublicEventScreenTestTags.SCAFFOLD,
          topBar = CreatePublicEventScreenTestTags.TOP_APP_BAR,
          backButton = CreatePublicEventScreenTestTags.BACK_BUTTON,
          scrollColumn = CreatePublicEventScreenTestTags.SCROLL_COLUMN,
          saveButton = CreatePublicEventScreenTestTags.SAVE_BUTTON)

  CreateEventShell(
      navController = navController,
      title =
          if (existingEventId != null) stringResource(R.string.title_edit_public_event)
          else stringResource(R.string.title_create_public_event),
      canSave = canSave,
      onSave = { createPublicEventViewModel.saveEvent() },
      testTags = shellTestTags) { onFocusChange ->

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

        // Organization Creation Toggle (only shown if user owns organizations)
        if (uiState.userOrganizations.isNotEmpty()) {
          Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.event_label_create_as_organization),
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.onSurface)
                      Switch(
                          checked = uiState.createAsOrganization,
                          onCheckedChange = createPublicEventViewModel::updateCreateAsOrganization,
                          modifier =
                              Modifier.testTag(
                                  CreatePublicEventScreenTestTags.CREATE_AS_ORG_SWITCH))
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
                              .testTag(CreatePublicEventScreenTestTags.ORG_DROPDOWN)) {
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
                                      createPublicEventViewModel.updateSelectedOrganizationId(orgId)
                                      expanded = false
                                    },
                                    modifier = Modifier.testTag("orgDropdownItem_$orgId"))
                              }
                            }
                      }
                }
              }
        }

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

        // Flash Event Toggle (before date/time fields)
        FlashEventToggle(
            isFlash = uiState.isFlash,
            onIsFlashChange = createPublicEventViewModel::updateIsFlash,
            flashSwitchTag = CreatePublicEventScreenTestTags.FLASH_EVENT_SWITCH)

        // Conditional: Show duration picker for flash events, date/time for normal events
        if (uiState.isFlash) {
          FlashEventDurationFields(
              hours = uiState.flashDurationHours,
              minutes = uiState.flashDurationMinutes,
              onHoursChange = createPublicEventViewModel::updateFlashDurationHours,
              onMinutesChange = createPublicEventViewModel::updateFlashDurationMinutes,
              hoursTag = CreatePublicEventScreenTestTags.FLASH_DURATION_HOURS,
              minutesTag = CreatePublicEventScreenTestTags.FLASH_DURATION_MINUTES)
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
                      onStartDateChange = createPublicEventViewModel::updateStartDate,
                      onStartTimeChange = createPublicEventViewModel::updateStartTime,
                      onEndDateChange = createPublicEventViewModel::updateEndDate,
                      onEndTimeChange = createPublicEventViewModel::updateEndTime),
              startDateTag = CreatePublicEventScreenTestTags.START_DATE_INPUT,
              startTimeTag = CreatePublicEventScreenTestTags.START_TIME_BUTTON,
              endDateTag = CreatePublicEventScreenTestTags.END_DATE_INPUT,
              endTimeTag = CreatePublicEventScreenTestTags.END_TIME_BUTTON)
        }

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
                        createPublicEventViewModel::updateNumberOfParticipantsString,
                    onHasFeeChange = createPublicEventViewModel::updateHasParticipationFee,
                    onFeeStringChange = createPublicEventViewModel::updateParticipationFeeString,
                    onIsFlashChange = createPublicEventViewModel::updateIsFlash),
            participantsTag = CreatePublicEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT,
            feeSwitchTag = CreatePublicEventScreenTestTags.PARTICIPATION_FEE_SWITCH,
            feeInputTag = CreatePublicEventScreenTestTags.PARTICIPATION_FEE_INPUT,
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

private fun handlePrefill(
    existingEventId: String?,
    templateEventId: String?,
    viewModel: CreatePublicEventViewModel
) {
  when {
    existingEventId != null -> viewModel.loadEvent(existingEventId)
    templateEventId != null -> viewModel.loadEventAsTemplate(templateEventId)
  }
}

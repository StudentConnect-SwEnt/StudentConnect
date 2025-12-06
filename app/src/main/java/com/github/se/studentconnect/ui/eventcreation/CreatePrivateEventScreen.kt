// Portions of this code were generated with the help of ChatGPT

package com.github.se.studentconnect.ui.eventcreation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.studentconnect.R
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import com.github.se.studentconnect.ui.navigation.Route
import com.github.se.studentconnect.ui.utils.DialogNotImplemented
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

private val SAVE_BUTTON_CONTAINER = 90.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePrivateEventScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController? = null,
    existingEventId: String? = null,
    createPrivateEventViewModel: CreatePrivateEventViewModel = viewModel(),
) {
  LaunchedEffect(existingEventId) {
    if (existingEventId != null) {
      createPrivateEventViewModel.loadEvent(existingEventId)
    }
  }

  val createPrivateEventUiState by createPrivateEventViewModel.uiState.collectAsState()
  val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

  val startDateInitial = createPrivateEventUiState.startDate?.format(dateFormatter) ?: ""
  val endDateInitial = createPrivateEventUiState.endDate?.format(dateFormatter) ?: ""

  val canSave =
      createPrivateEventUiState.title.isNotBlank() &&
          createPrivateEventUiState.startDate != null &&
          createPrivateEventUiState.endDate != null &&
          !createPrivateEventUiState.isSaving

  LaunchedEffect(Unit) {
    createPrivateEventViewModel.navigateToEvent.collect { eventId ->
      if (navController != null) {
        // Navigate to Activities
        // Pop up to HOME to ensure a clean stack
        navController.navigate(Route.ACTIVITIES) { popUpTo(Route.HOME) { inclusive = false } }
        // Show the card of this event (Event View) on top
        navController.navigate(Route.eventView(eventId, true))
      }
      createPrivateEventViewModel.resetFinishedSaving()
    }
  }

  val scrollState = rememberScrollState()
  val scrollBottomThresholdPx =
      50 // Distance from the bottom where the save button should start its final animation
  val isAtBottom by remember {
    derivedStateOf { scrollState.value >= scrollState.maxValue - scrollBottomThresholdPx }
  }

  // Track if any text field has focus
  var isAnyFieldFocused by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  // Handle back button / escape key to clear focus and hide keyboard
  BackHandler(enabled = isAnyFieldFocused) {
    focusManager.clearFocus()
    keyboardController?.hide()
  }

  // When focus changes, ensure keyboard state matches
  LaunchedEffect(isAnyFieldFocused) {
    if (!isAnyFieldFocused) {
      keyboardController?.hide()
    }
  }

  val buttonWidthFraction by
      animateFloatAsState(
          targetValue = if (isAtBottom) 0.9f else 0.35f,
          animationSpec = tween(durationMillis = 300),
          label = "buttonWidthFraction")

  Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.SCAFFOLD),
        topBar = {
          TopAppBar(
              modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.TOP_APP_BAR),
              title = {
                Text(
                    text =
                        if (existingEventId != null)
                            stringResource(R.string.title_edit_private_event)
                        else stringResource(R.string.title_create_private_event))
              },
              navigationIcon = {
                IconButton(
                    modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.BACK_BUTTON),
                    onClick = { navController?.popBackStack() }) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = stringResource(R.string.content_description_back))
                    }
              })
        }) { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .verticalScroll(scrollState)
                      .padding(paddingValues)
                      .padding(horizontal = 16.dp)
                      .testTag(CreatePrivateEventScreenTestTags.SCROLL_COLUMN),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            FormTextField(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(CreatePrivateEventScreenTestTags.TITLE_INPUT)
                        .onFocusChanged { isAnyFieldFocused = it.isFocused },
                label = stringResource(R.string.event_label_title),
                placeholder = stringResource(R.string.event_placeholder_title),
                value = createPrivateEventUiState.title,
                onValueChange = { createPrivateEventViewModel.updateTitle(it) },
                errorText =
                    if (createPrivateEventUiState.title.isBlank())
                        stringResource(R.string.event_error_title_blank)
                    else null,
                required = true)

            FormTextField(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(CreatePrivateEventScreenTestTags.DESCRIPTION_INPUT)
                        .onFocusChanged { isAnyFieldFocused = it.isFocused },
                label = stringResource(R.string.event_label_description),
                placeholder = stringResource(R.string.event_placeholder_description),
                value = createPrivateEventUiState.description,
                onValueChange = { createPrivateEventViewModel.updateDescription(it) },
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                  Column(
                      modifier = Modifier.padding(16.dp),
                      verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.event_label_banner),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold))
                        PicturePickerCard(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag(CreatePrivateEventScreenTestTags.BANNER_PICKER),
                            style = PicturePickerStyle.Banner,
                            existingImagePath = createPrivateEventUiState.bannerImagePath,
                            selectedImageUri = createPrivateEventUiState.bannerImageUri,
                            onImageSelected = { uri ->
                              createPrivateEventViewModel.updateBannerImageUri(uri)
                            },
                            placeholderText = stringResource(R.string.event_placeholder_banner),
                            overlayText = stringResource(R.string.instruction_tap_to_change_photo),
                            imageDescription = stringResource(R.string.event_label_banner))
                        Text(
                            text = stringResource(R.string.event_text_banner_help),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(
                            onClick = { createPrivateEventViewModel.removeBannerImage() },
                            enabled =
                                createPrivateEventUiState.bannerImageUri != null ||
                                    createPrivateEventUiState.bannerImagePath != null,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag(
                                        CreatePrivateEventScreenTestTags.REMOVE_BANNER_BUTTON)) {
                              Text(stringResource(R.string.event_button_remove_banner))
                            }
                      }
                }

            LocationTextField(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(CreatePrivateEventScreenTestTags.LOCATION_INPUT),
                label = stringResource(R.string.event_label_location),
                placeholder = stringResource(R.string.event_placeholder_location),
                selectedLocation = createPrivateEventUiState.location,
                onLocationChange = { createPrivateEventViewModel.updateLocation(it) })

            val wideFieldWeight = 0.7f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              DateTextField(
                  modifier =
                      Modifier.weight(wideFieldWeight)
                          .testTag(CreatePrivateEventScreenTestTags.START_DATE_INPUT),
                  label = stringResource(R.string.event_label_start_date),
                  placeholder = stringResource(R.string.event_placeholder_date),
                  initialValue = startDateInitial,
                  onDateChange = { createPrivateEventViewModel.updateStartDate(it) },
                  required = true)

              TimePicker(
                  modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.START_TIME_BUTTON),
                  time = createPrivateEventUiState.startTime,
                  onTimeChange = { createPrivateEventViewModel.updateStartTime(it) })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              DateTextField(
                  modifier =
                      Modifier.weight(wideFieldWeight)
                          .testTag(CreatePrivateEventScreenTestTags.END_DATE_INPUT),
                  label = stringResource(R.string.event_label_end_date),
                  placeholder = stringResource(R.string.event_placeholder_date),
                  initialValue = endDateInitial,
                  onDateChange = { createPrivateEventViewModel.updateEndDate(it) },
                  required = true)

              TimePicker(
                  modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.END_TIME_BUTTON),
                  time = createPrivateEventUiState.endTime,
                  onTimeChange = { createPrivateEventViewModel.updateEndTime(it) })
            }

            FormTextField(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(CreatePrivateEventScreenTestTags.NUMBER_OF_PARTICIPANTS_INPUT)
                        .onFocusChanged { isAnyFieldFocused = it.isFocused },
                label = stringResource(R.string.event_label_participants),
                value = createPrivateEventUiState.numberOfParticipantsString,
                onValueChange = {
                  createPrivateEventViewModel.updateNumberOfParticipantsString(it)
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              FormTextField(
                  modifier =
                      Modifier.weight(wideFieldWeight)
                          .testTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_INPUT)
                          .onFocusChanged { isAnyFieldFocused = it.isFocused },
                  label = stringResource(R.string.event_label_fees),
                  value = createPrivateEventUiState.participationFeeString,
                  onValueChange = { createPrivateEventViewModel.updateParticipationFeeString(it) },
                  enabled = createPrivateEventUiState.hasParticipationFee,
              )

              Switch(
                  modifier =
                      Modifier.testTag(CreatePrivateEventScreenTestTags.PARTICIPATION_FEE_SWITCH),
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
                  modifier = Modifier.weight(wideFieldWeight),
                  text = stringResource(R.string.event_label_flash_event),
              )

              val context = LocalContext.current
              Switch(
                  modifier = Modifier.testTag(CreatePrivateEventScreenTestTags.FLASH_EVENT_SWITCH),
                  checked = createPrivateEventUiState.isFlash,
                  onCheckedChange = { DialogNotImplemented(context) },
              )
            }

            // Add bottom padding to avoid save button overlap
            Spacer(modifier = Modifier.size(100.dp))
          }
        }

    // Floating Save Button
    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .height(SAVE_BUTTON_CONTAINER)
                  .padding(horizontal = 16.dp, vertical = 16.dp)) {
            Surface(
                modifier =
                    Modifier.testTag(CreatePrivateEventScreenTestTags.SAVE_BUTTON)
                        .fillMaxWidth(buttonWidthFraction)
                        .align(if (isAtBottom) Alignment.Center else Alignment.CenterEnd)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp)),
                color =
                    if (canSave) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp,
                enabled = canSave,
                onClick = { if (canSave) createPrivateEventViewModel.saveEvent() }) {
                  Row(
                      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.button_save),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary)
                      }
                }
          }
    }
  }
}

package com.github.se.studentconnect.ui.screen.profile.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.components.ProfileSaveButton
import com.github.se.studentconnect.ui.profile.edit.EditBirthdayViewModel

/**
 * Screen for editing user birthday with Material 3 DatePicker.
 *
 * @param userId The ID of the user whose birthday is being edited
 * @param userRepository Repository for user data operations
 * @param onNavigateBack Callback to navigate back to profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBirthdayScreen(
    userId: String,
    userRepository: UserRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  val viewModel: EditBirthdayViewModel = viewModel { EditBirthdayViewModel(userRepository, userId) }

  val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
  val birthdayString by viewModel.birthdayString.collectAsState()
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // Create DatePickerState without initialSelectedDateMillis to avoid ghost circle
  val datePickerState = rememberDatePickerState()

  // Track if we've set the initial date from ViewModel
  val hasSetInitialDate = remember { mutableStateOf(false) }

  // Initialize DatePicker with the loaded birthday only once
  LaunchedEffect(selectedDateMillis) {
    if (selectedDateMillis != null && !hasSetInitialDate.value) {
      datePickerState.selectedDateMillis = selectedDateMillis
      hasSetInitialDate.value = true
    }
  }

  // Sync DatePicker state with ViewModel when user selects a date
  // Only sync if we've already set the initial date (to avoid overwriting on first load)
  LaunchedEffect(datePickerState.selectedDateMillis, hasSetInitialDate.value) {
    if (hasSetInitialDate.value &&
        datePickerState.selectedDateMillis != null &&
        datePickerState.selectedDateMillis != selectedDateMillis) {
      viewModel.updateSelectedDate(datePickerState.selectedDateMillis)
    }
  }

  // Handle UI state changes
  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is EditBirthdayViewModel.UiState.Success -> {
        // Navigate back immediately after successful save
        onNavigateBack()
        // Reset state after navigation to avoid cancelling LaunchedEffect
        viewModel.resetState()
      }
      is EditBirthdayViewModel.UiState.Error -> {
        snackbarHostState.showSnackbar(state.message)
        viewModel.resetState()
      }
      else -> {}
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = stringResource(R.string.title_edit_birthday),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back))
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
              // Main content area
              Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    // Instructions
                    Text(
                        text = stringResource(R.string.instruction_select_date_of_birth),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp))

                    // DatePicker
                    DatePicker(
                        state = datePickerState,
                        title = {
                          Text(
                              text = stringResource(R.string.instruction_pick_date),
                              modifier = Modifier.padding(16.dp))
                        })
                  }

              // Save Button
              ProfileSaveButton(
                  onClick = { viewModel.saveBirthday() },
                  isLoading = uiState is EditBirthdayViewModel.UiState.Loading,
                  enabled =
                      uiState !is EditBirthdayViewModel.UiState.Loading &&
                          datePickerState.selectedDateMillis != null,
                  text = stringResource(R.string.button_save))
            }
      }
}

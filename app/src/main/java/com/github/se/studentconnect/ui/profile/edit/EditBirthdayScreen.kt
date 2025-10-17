package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository

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

  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

  // Sync DatePicker state with ViewModel
  LaunchedEffect(datePickerState.selectedDateMillis) {
    if (datePickerState.selectedDateMillis != selectedDateMillis) {
      viewModel.updateSelectedDate(datePickerState.selectedDateMillis)
    }
  }

  // Handle UI state changes
  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is EditBirthdayViewModel.UiState.Success -> {
        snackbarHostState.showSnackbar(state.message)
        viewModel.resetState()
        // Navigate back after successful save
        kotlinx.coroutines.delay(500)
        onNavigateBack()
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
                  text = "Edit Birthday",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        text = "Select your date of birth",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp))

                    // Current birthday display
                    if (birthdayString != null) {
                      Card(
                          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                          colors =
                              CardDefaults.cardColors(
                                  containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                  Text(
                                      text = "Current Birthday",
                                      style = MaterialTheme.typography.labelMedium,
                                      color = MaterialTheme.colorScheme.onPrimaryContainer)
                                  Spacer(modifier = Modifier.height(4.dp))
                                  Text(
                                      text = birthdayString!!,
                                      style = MaterialTheme.typography.headlineSmall,
                                      fontWeight = FontWeight.Bold,
                                      color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                          }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DatePicker
                    DatePicker(
                        state = datePickerState,
                        title = { Text(text = "Pick a date", modifier = Modifier.padding(16.dp)) })
                  }

              // Save Button
              Button(
                  onClick = { viewModel.saveBirthday() },
                  modifier = Modifier.fillMaxWidth().height(56.dp),
                  enabled =
                      uiState !is EditBirthdayViewModel.UiState.Loading &&
                          datePickerState.selectedDateMillis != null) {
                    if (uiState is EditBirthdayViewModel.UiState.Loading) {
                      CircularProgressIndicator(
                          modifier = Modifier.size(24.dp),
                          color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                      Text(
                          text = "Save",
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.SemiBold)
                    }
                  }
            }
      }
}

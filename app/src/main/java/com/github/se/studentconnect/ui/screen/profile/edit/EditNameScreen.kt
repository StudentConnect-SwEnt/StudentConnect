package com.github.se.studentconnect.ui.screen.profile.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.components.ProfileSaveButton
import com.github.se.studentconnect.ui.profile.edit.BaseEditViewModel
import com.github.se.studentconnect.ui.profile.edit.EditNameViewModel

/**
 * Screen for editing user name (first name and last name).
 *
 * @param userId The ID of the user whose name is being edited
 * @param userRepository Repository for user data operations
 * @param onNavigateBack Callback to navigate back to profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameScreen(
    userId: String,
    userRepository: UserRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  val viewModel: EditNameViewModel = viewModel { EditNameViewModel(userRepository, userId) }

  val firstName by viewModel.firstName.collectAsState()
  val lastName by viewModel.lastName.collectAsState()
  val uiState by viewModel.uiState.collectAsState()
  val firstNameError by viewModel.firstNameError.collectAsState()
  val lastNameError by viewModel.lastNameError.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // Handle UI state changes
  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is BaseEditViewModel.UiState.Success -> {
        // Navigate back immediately after successful save
        onNavigateBack()
        // Reset state after navigation to avoid cancelling LaunchedEffect
        viewModel.resetState()
      }
      is BaseEditViewModel.UiState.Error -> {
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
                  text = stringResource(R.string.title_edit_name),
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
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Instructions
                    Text(
                        text = stringResource(R.string.instruction_enter_name),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp))

                    // First Name TextField
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { viewModel.updateFirstName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_first_name)) },
                        placeholder = { Text(stringResource(R.string.placeholder_first_name)) },
                        isError = firstNameError != null,
                        supportingText = {
                          if (firstNameError != null) {
                            Text(text = firstNameError!!, color = MaterialTheme.colorScheme.error)
                          }
                        },
                        enabled = uiState !is BaseEditViewModel.UiState.Loading,
                        singleLine = true,
                        keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words))

                    // Last Name TextField
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { viewModel.updateLastName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_last_name)) },
                        placeholder = { Text(stringResource(R.string.placeholder_last_name)) },
                        isError = lastNameError != null,
                        supportingText = {
                          if (lastNameError != null) {
                            Text(text = lastNameError!!, color = MaterialTheme.colorScheme.error)
                          }
                        },
                        enabled = uiState !is BaseEditViewModel.UiState.Loading,
                        singleLine = true,
                        keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words))
                  }

              // Save Button
              ProfileSaveButton(
                  onClick = { viewModel.saveName() },
                  isLoading = uiState is BaseEditViewModel.UiState.Loading,
                  enabled =
                      uiState !is BaseEditViewModel.UiState.Loading &&
                          firstName.trim().isNotEmpty() &&
                          lastName.trim().isNotEmpty(),
                  text = stringResource(R.string.button_save))
            }
      }
}

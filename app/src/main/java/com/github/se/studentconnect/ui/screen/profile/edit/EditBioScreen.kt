package com.github.se.studentconnect.ui.screen.profile.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.components.BioTextField
import com.github.se.studentconnect.ui.components.BioTextFieldStyle
import com.github.se.studentconnect.ui.profile.ProfileConstants
import com.github.se.studentconnect.ui.profile.edit.BaseEditViewModel
import com.github.se.studentconnect.ui.profile.edit.EditBioViewModel
import kotlinx.coroutines.delay

/**
 * Screen for editing user bio. Provides a multi-line text field with character limit and
 * validation.
 *
 * @param userId The ID of the user whose bio is being edited
 * @param userRepository Repository for user data operations
 * @param onNavigateBack Callback to navigate back to profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBioScreen(
    userId: String,
    userRepository: UserRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  val viewModel: EditBioViewModel = viewModel { EditBioViewModel(userRepository, userId) }

  val bioText by viewModel.bioText.collectAsState()
  val uiState by viewModel.uiState.collectAsState()
  val characterCount by viewModel.characterCount.collectAsState()
  val validationError by viewModel.validationError.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // Handle UI state changes
  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is BaseEditViewModel.UiState.Success -> {
        snackbarHostState.showSnackbar(state.message)
        viewModel.resetState()
        // Navigate back after successful save
        delay(500)
        onNavigateBack()
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
                  text = "Edit Bio",
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
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Instructions
                    Text(
                        text = ProfileConstants.INSTRUCTION_TELL_ABOUT_YOURSELF,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp))

                    // Bio TextField using reusable component
                    BioTextField(
                        value = bioText,
                        onValueChange = { newText -> viewModel.updateBioText(newText) },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        placeholder = ProfileConstants.PLACEHOLDER_BIO,
                        enabled = uiState !is BaseEditViewModel.UiState.Loading,
                        isError = validationError != null,
                        errorMessage = validationError,
                        showCharacterCount = true,
                        maxCharacters = ProfileConstants.MAX_BIO_LENGTH,
                        style = BioTextFieldStyle.Outlined)
                  }

              // Save Button
              Button(
                  onClick = { viewModel.saveBio() },
                  modifier = Modifier.fillMaxWidth().height(56.dp),
                  enabled =
                      uiState !is BaseEditViewModel.UiState.Loading &&
                          bioText.trim().isNotEmpty() &&
                          characterCount <= ProfileConstants.MAX_BIO_LENGTH) {
                    if (uiState is BaseEditViewModel.UiState.Loading) {
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

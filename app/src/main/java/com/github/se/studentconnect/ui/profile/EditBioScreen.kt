package com.github.se.studentconnect.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.let

/**
 * Screen for editing user bio. Provides a multi-line text field for bio editing with character
 * limit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBioScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: EditBioViewModel = viewModel { EditBioViewModel(userRepository, userId) },
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val bioText by viewModel.bioText.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()
  val isValid by viewModel.isValid.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Show success/error messages
  LaunchedEffect(successMessage) {
    successMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearSuccessMessage()
      // Navigate back after successful save
      onNavigateBack()
    }
  }

  LaunchedEffect(errorMessage) {
    errorMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearErrorMessage()
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Edit Bio",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      modifier = modifier) { paddingValues ->
        when {
          user == null -> {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator()
                }
          }
          else -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {
                  // Bio Text Field
                  OutlinedTextField(
                      value = bioText,
                      onValueChange = { viewModel.updateBioText(it) },
                      label = { Text("Tell us about yourself...") },
                      placeholder = {
                        Text(
                            "Share your interests, hobbies, or anything you'd like others to know about you.")
                      },
                      modifier = Modifier.fillMaxWidth().weight(1f),
                      minLines = 5,
                      maxLines = 10,
                      isError = !isValid,
                      supportingText = {
                        Column {
                          if (!isValid) {
                            Text(
                                text = "Bio cannot exceed 500 characters",
                                color = MaterialTheme.colorScheme.error)
                          }
                          Text(
                              text = "${viewModel.getCharacterCount()}/500 characters",
                              color =
                                  if (viewModel.getRemainingCharacters() < 50) {
                                    MaterialTheme.colorScheme.error
                                  } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                  })
                        }
                      },
                      keyboardOptions =
                          KeyboardOptions(
                              capitalization = KeyboardCapitalization.Sentences,
                              imeAction = ImeAction.Done),
                      keyboardActions =
                          KeyboardActions(
                              onDone = {
                                keyboardController?.hide()
                                if (isValid) {
                                  viewModel.saveBio()
                                }
                              }))

                  // Save Button
                  Button(
                      onClick = { viewModel.saveBio() },
                      enabled = !isLoading && isValid,
                      modifier = Modifier.fillMaxWidth()) {
                        if (isLoading) {
                          CircularProgressIndicator(
                              modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                        }
                        Text(text = if (isLoading) "Saving..." else "Save Bio")
                      }

                  // Instructions
                  Text(
                      text =
                          "Write a brief description about yourself. This will help other students get to know you better.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
          }
        }
      }
}

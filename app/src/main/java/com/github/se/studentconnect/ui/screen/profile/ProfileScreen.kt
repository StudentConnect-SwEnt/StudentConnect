package com.github.se.studentconnect.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.joinToString
import kotlin.let

/** Profile screen showing read-only user information. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserId: String,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: ProfileViewModel = viewModel { ProfileViewModel(userRepository, currentUserId) },
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val editingField by viewModel.editingField.collectAsState()
  val loadingFields by viewModel.loadingFields.collectAsState()
  val fieldErrors by viewModel.fieldErrors.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  // Show success messages
  LaunchedEffect(successMessage) {
    successMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearSuccessMessage()
    }
  }

  Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, modifier = modifier) { paddingValues
    ->
    when (val currentUser = user) {
      null -> {
        // Loading state
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
      }
      else -> {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
              // Profile Picture and Name Section
              ProfileHeaderSection(user = currentUser)

              // Profile Details Card
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          // University Field
                          EditableProfileField(
                              label = "University",
                              value = currentUser.university,
                              isEditing = editingField == EditingField.University,
                              isLoading = loadingFields.contains(EditingField.University),
                              errorMessage = fieldErrors[EditingField.University],
                              onEditClick = { /* disabled for now */},
                              onSave = { newValue -> viewModel.updateUniversity(newValue) },
                              onCancel = { viewModel.cancelEditing() },
                              isEditable = false)

                          // Country Field
                          EditableProfileField(
                              label = "Country",
                              value = currentUser.country ?: "",
                              isEditing = editingField == EditingField.Country,
                              isLoading = loadingFields.contains(EditingField.Country),
                              errorMessage = fieldErrors[EditingField.Country],
                              onEditClick = { /* disabled for now */},
                              onSave = { newValue -> viewModel.updateCountry(newValue) },
                              onCancel = { viewModel.cancelEditing() })

                          // Birthday Field
                          EditableProfileField(
                              label = "Birthday",
                              value = currentUser.birthday ?: "",
                              isEditing = editingField == EditingField.Birthday,
                              isLoading = loadingFields.contains(EditingField.Birthday),
                              errorMessage = fieldErrors[EditingField.Birthday],
                              onEditClick = { /* disabled for now */},
                              onSave = { newValue -> viewModel.updateBirthday(newValue) },
                              onCancel = { viewModel.cancelEditing() })

                          // Activities Field
                          EditableProfileFieldMultiline(
                              label = "Favourite Activities",
                              value = currentUser.hobbies.joinToString(", "),
                              isEditing = editingField == EditingField.Activities,
                              isLoading = loadingFields.contains(EditingField.Activities),
                              errorMessage = fieldErrors[EditingField.Activities],
                              onEditClick = { /* disabled for now */},
                              onSave = { newValue -> viewModel.updateActivities(newValue) },
                              onCancel = { viewModel.cancelEditing() })

                          // Bio Field
                          EditableProfileFieldMultiline(
                              label = "More About Me",
                              value = currentUser.bio ?: "",
                              isEditing = editingField == EditingField.Bio,
                              isLoading = loadingFields.contains(EditingField.Bio),
                              errorMessage = fieldErrors[EditingField.Bio],
                              onEditClick = { /* disabled for now */},
                              onSave = { newValue -> viewModel.updateBio(newValue) },
                              onCancel = { viewModel.cancelEditing() })
                        }
                  }
            }
      }
    }
  }
}

/** Profile header section with profile picture and name. */
@Composable
private fun ProfileHeaderSection(user: User, modifier: Modifier = Modifier) {
  Column(
      modifier = modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Profile Picture
        Box(modifier = Modifier.size(120.dp)) {
          // Profile picture placeholder
          Box(
              modifier =
                  Modifier.size(120.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
              }

          IconButton(
              onClick = { /* disabled for now*/},
              modifier =
                  Modifier.size(32.dp)
                      .align(Alignment.BottomEnd)
                      .background(MaterialTheme.colorScheme.surface, CircleShape)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Picture",
                    tint = MaterialTheme.colorScheme.onSurface)
              }
        }

        // Name (read-only, icon shown but disabled)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = user.getFullName(),
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface)

              IconButton(onClick = { /* disabled for now */}) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
      }
}

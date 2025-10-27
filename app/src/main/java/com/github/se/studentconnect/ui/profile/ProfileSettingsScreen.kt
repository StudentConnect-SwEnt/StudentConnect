package com.github.se.studentconnect.ui.profile

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.joinToString
import kotlin.let

/**
 * Profile Settings screen showing user information with edit functionality. This is the detailed
 * settings/edit profile screen with inline editing capabilities.
 *
 * @param currentUserId The ID of the current user
 * @param userRepository Repository for user data operations
 * @param viewModel ViewModel for profile screen
 * @param onNavigateToEditPicture Callback to navigate to edit profile picture screen
 * @param onNavigateToEditName Callback to navigate to edit name screen
 * @param onNavigateToEditBio Callback to navigate to edit bio screen
 * @param onNavigateToEditActivities Callback to navigate to edit activities screen
 * @param onNavigateToEditBirthday Callback to navigate to edit birthday screen
 * @param onNavigateToEditNationality Callback to navigate to edit nationality screen
 * @param onNavigateBack Callback to navigate back to profile view
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    currentUserId: String,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: ProfileViewModel = viewModel { ProfileViewModel(userRepository, currentUserId) },
    onNavigateToEditPicture: ((String) -> Unit)? = null,
    onNavigateToEditName: ((String) -> Unit)? = null,
    onNavigateToEditBio: ((String) -> Unit)? = null,
    onNavigateToEditActivities: ((String) -> Unit)? = null,
    onNavigateToEditBirthday: ((String) -> Unit)? = null,
    onNavigateToEditNationality: ((String) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val editingField by viewModel.editingField.collectAsState()
  val loadingFields by viewModel.loadingFields.collectAsState()
  val fieldErrors by viewModel.fieldErrors.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val lifecycleOwner = LocalLifecycleOwner.current

  // Show success messages
  LaunchedEffect(successMessage) {
    successMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearSuccessMessage()
    }
  }

  // Initial load
  LaunchedEffect(Unit) { viewModel.loadUserProfile() }

  // Reload data when screen becomes visible again (after returning from edit screens)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.loadUserProfile()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
              ProfileHeaderSection(
                  user = currentUser,
                  onEditPicture = { onNavigateToEditPicture?.invoke(currentUserId) },
                  onEditName = { onNavigateToEditName?.invoke(currentUserId) })

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
                              onEditClick = { /* disabled */},
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
                              onEditClick = { /*disabled for now*/ },
                              onSave = { newValue -> viewModel.updateCountry(newValue) },
                              onCancel = { viewModel.cancelEditing() })

                          // Birthday Field
                          EditableProfileField(
                              label = "Birthday",
                              value = currentUser.birthday ?: "",
                              isEditing = editingField == EditingField.Birthday,
                              isLoading = loadingFields.contains(EditingField.Birthday),
                              errorMessage = fieldErrors[EditingField.Birthday],
                              onEditClick = { /*Disabled for now*/},
                              onSave = { newValue -> viewModel.updateBirthday(newValue) },
                              onCancel = { viewModel.cancelEditing() })

                          // Activities Field
                          EditableProfileFieldMultiline(
                              label = "Favourite Activities",
                              value = currentUser.hobbies.joinToString(", "),
                              isEditing = editingField == EditingField.Activities,
                              isLoading = loadingFields.contains(EditingField.Activities),
                              errorMessage = fieldErrors[EditingField.Activities],
                              onEditClick = { /*Disabled for now*/},
                              onSave = { newValue -> viewModel.updateActivities(newValue) },
                              onCancel = { viewModel.cancelEditing() })

                          // Bio Field
                          EditableProfileFieldMultiline(
                              label = "More About Me",
                              value = currentUser.bio ?: "",
                              isEditing = editingField == EditingField.Bio,
                              isLoading = loadingFields.contains(EditingField.Bio),
                              errorMessage = fieldErrors[EditingField.Bio],
                              onEditClick = { /*Disabled for now*/},
                              onSave = { newValue -> viewModel.updateBio(newValue) },
                              onCancel = { viewModel.cancelEditing() })
                        }
                  }
            }
      }
    }
  }
}

/**
 * Profile header section with profile picture and name.
 *
 * @param user The user whose profile is being displayed
 * @param onEditPicture Callback to navigate to edit profile picture screen
 * @param onEditName Callback to navigate to edit name screen
 * @param modifier Modifier for the composable
 */
@Composable
private fun ProfileHeaderSection(
    user: User,
    onEditPicture: (() -> Unit)? = null,
    onEditName: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
              onClick = { onEditPicture?.invoke() },
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

        // Name with edit button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = user.getFullName(),
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface)

              IconButton(onClick = { onEditName?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
      }
}

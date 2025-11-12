package com.github.se.studentconnect.ui.screen.profile.edit

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import com.github.se.studentconnect.ui.components.ProfileSaveButton
import com.github.se.studentconnect.ui.profile.edit.EditProfilePictureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Screen for editing profile picture. Shows current profile picture and options to change it.
 *
 * @param userId The ID of the user whose profile picture is being edited
 * @param userRepository Repository for user data operations
 * @param viewModel ViewModel for edit profile picture screen
 * @param onNavigateBack Callback to navigate back to profile screen
 * @param modifier Modifier for the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePictureScreen(
    userId: String,
    userRepository: UserRepository,
    viewModel: EditProfilePictureViewModel = viewModel {
      EditProfilePictureViewModel(userRepository, userId)
    },
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
  val isLoading by viewModel.isLoading.collectAsState()
  val user by viewModel.user.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var userModified by remember { mutableStateOf(false) }
  var currentImagePath by remember { mutableStateOf<String?>(null) }
  val repository = MediaRepositoryProvider.repository
  var isUploading by remember { mutableStateOf(false) }

  LaunchedEffect(user?.profilePictureUrl) {
    if (!userModified) {
      selectedImageUri = null
    }
    currentImagePath = user?.profilePictureUrl
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Edit Profile Picture",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = { onNavigateBack?.invoke() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      modifier = modifier) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              val hasPhoto = selectedImageUri != null || currentImagePath != null

              Card(
                  modifier = Modifier.wrapContentWidth(),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          PicturePickerCard(
                              style = PicturePickerStyle.Avatar,
                              existingImagePath = currentImagePath,
                              selectedImageUri = selectedImageUri,
                              onImageSelected = { uri ->
                                selectedImageUri = uri
                                userModified = true
                              },
                              placeholderText = "Upload/Take your profile photo",
                              overlayText = "Tap to change photo",
                              imageDescription = "Profile Picture")

                          Text(
                              text =
                                  if (hasPhoto) {
                                    "Preview of your new profile picture"
                                  } else {
                                    "Tap above to choose a profile photo"
                                  },
                              style = MaterialTheme.typography.bodyMedium,
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                              textAlign = TextAlign.Center)
                        }
                  }

              // Action Buttons Section
              Card(
                  modifier = Modifier.fillMaxSize(),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          OutlinedButton(
                              onClick = {
                                selectedImageUri = null
                                currentImagePath = null
                                userModified = true
                              },
                              enabled =
                                  !isLoading &&
                                      !isUploading &&
                                      (selectedImageUri != null || currentImagePath != null),
                              modifier = Modifier.fillMaxWidth()) {
                                Text("Remove Photo")
                              }

                          // Save Button
                          ProfileSaveButton(
                              onClick = {
                                if (isLoading || isUploading || !userModified) return@ProfileSaveButton
                                coroutineScope.launch {
                                  isUploading = true
                                  try {
                                    val workingUri = selectedImageUri
                                    if (workingUri == null) {
                                      viewModel.updateProfilePicture(null)
                                      userModified = false
                                      selectedImageUri = null
                                      currentImagePath = null
                                      onNavigateBack?.invoke()
                                    } else {
                                      val uploadedId =
                                          uploadProfilePicture(
                                              repository = repository,
                                              userId = userId,
                                              uri = workingUri)
                                      if (uploadedId != null) {
                                        viewModel.updateProfilePicture(uploadedId)
                                        currentImagePath = uploadedId
                                        selectedImageUri = null
                                        userModified = false
                                        onNavigateBack?.invoke()
                                      } else {
                                        snackbarHostState.showSnackbar("Failed to upload photo")
                                      }
                                    }
                                  } finally {
                                    isUploading = false
                                  }
                                }
                              },
                              isLoading = isLoading || isUploading,
                              enabled = !isLoading && !isUploading && userModified,
                              text = "Save")
                        }
                  }
            }
      }
}

private suspend fun uploadProfilePicture(
    repository: MediaRepository,
    userId: String,
    uri: Uri
): String? {
  return try {
    withContext(Dispatchers.IO) { repository.upload(uri, "users/$userId/profile") }
  } catch (exception: Exception) {
    android.util.Log.e(
        "EditProfilePictureScreen",
        "Failed to upload profile image for user $userId with uri $uri",
        exception)
    null
  }
}

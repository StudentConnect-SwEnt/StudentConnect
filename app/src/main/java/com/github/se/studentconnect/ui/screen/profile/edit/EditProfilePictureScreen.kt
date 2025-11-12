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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.resources.AndroidResourceProvider
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
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
      val context = LocalContext.current
      EditProfilePictureViewModel(userRepository, userId, AndroidResourceProvider(context))
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
                  text = stringResource(R.string.screen_title_edit_profile_picture),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = { onNavigateBack?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
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
                              placeholderText =
                                  stringResource(R.string.placeholder_upload_take_photo),
                              overlayText =
                                  stringResource(R.string.placeholder_tap_to_change_photo),
                              imageDescription =
                                  stringResource(R.string.content_description_profile_picture))

                          Text(
                              text =
                                  if (hasPhoto) {
                                    stringResource(R.string.instruction_preview_new_profile_picture)
                                  } else {
                                    stringResource(R.string.instruction_tap_to_choose_photo)
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
                                Text(stringResource(R.string.button_remove_photo))
                              }

                          // Save Button
                          Button(
                              onClick = {
                                if (isLoading || isUploading || !userModified) return@Button
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
                                        snackbarHostState.showSnackbar(
                                            stringResource(R.string.error_failed_to_upload_photo))
                                      }
                                    }
                                  } finally {
                                    isUploading = false
                                  }
                                }
                              },
                              enabled = !isLoading && !isUploading && userModified,
                              modifier = Modifier.fillMaxWidth()) {
                                if (isLoading || isUploading) {
                                  CircularProgressIndicator(
                                      modifier = Modifier.size(16.dp),
                                      color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                  Text(stringResource(R.string.button_save_changes))
                                }
                              }
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

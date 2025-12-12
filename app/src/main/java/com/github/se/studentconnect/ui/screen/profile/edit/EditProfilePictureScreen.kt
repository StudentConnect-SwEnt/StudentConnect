package com.github.se.studentconnect.ui.screen.profile.edit

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.service.ImageUploadWorker
import com.github.se.studentconnect.ui.components.PicturePickerCard
import com.github.se.studentconnect.ui.components.PicturePickerStyle
import com.github.se.studentconnect.ui.components.ProfileSaveButton
import com.github.se.studentconnect.ui.profile.edit.EditProfilePictureViewModel
import java.io.File
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

  val context = LocalContext.current
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
                  text = stringResource(R.string.title_edit_profile_picture),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(onClick = { onNavigateBack?.invoke() }) {
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
                                  stringResource(R.string.placeholder_upload_profile_photo),
                              overlayText =
                                  stringResource(R.string.instruction_tap_to_change_photo),
                              imageDescription =
                                  stringResource(R.string.content_description_profile_picture))

                          Text(
                              text =
                                  if (hasPhoto) {
                                    stringResource(R.string.instruction_preview_profile_picture)
                                  } else {
                                    stringResource(R.string.instruction_choose_profile_photo)
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
                          ProfileSaveButton(
                              onClick = {
                                if (isLoading || isUploading || !userModified)
                                    return@ProfileSaveButton
                                coroutineScope.launch {
                                  isUploading = true
                                  try {
                                    val workingUri = selectedImageUri
                                    val previousImagePath = currentImagePath
                                    if (workingUri == null) {
                                      viewModel.updateProfilePicture(null)
                                      userModified = false
                                      selectedImageUri = null
                                      currentImagePath = null
                                      onNavigateBack?.invoke()
                                    } else {
                                      val storagePath = "users/$userId/profile"

                                      // Try immediate upload if we have network; otherwise stage
                                      // for
                                      // background upload.
                                      val uploadedId =
                                          if (isNetworkAvailable(context)) {
                                            uploadProfilePicture(
                                                repository = repository,
                                                userId = userId,
                                                uri = workingUri,
                                                storagePath = storagePath)
                                          } else null

                                      if (uploadedId != null) {
                                        viewModel.updateProfilePicture(uploadedId)
                                        currentImagePath = uploadedId
                                        selectedImageUri = null
                                        userModified = false
                                        onNavigateBack?.invoke()
                                      } else {
                                        val stagedLocalUrl =
                                            handleStagedProfilePicture(
                                                context = context,
                                                userId = userId,
                                                uri = workingUri,
                                                storagePath = storagePath,
                                                existingImageUrl = previousImagePath,
                                                viewModel = viewModel)
                                        if (stagedLocalUrl == null) {
                                          snackbarHostState.showSnackbar(
                                              context.getString(
                                                  R.string.error_failed_to_upload_photo))
                                        } else {
                                          currentImagePath = stagedLocalUrl
                                          selectedImageUri = null
                                          userModified = false
                                          onNavigateBack?.invoke()
                                        }
                                      }
                                    }
                                  } finally {
                                    isUploading = false
                                  }
                                }
                              },
                              isLoading = isLoading || isUploading,
                              enabled = !isLoading && !isUploading && userModified,
                              text = stringResource(R.string.button_save))
                        }
                  }
            }
      }
}

private suspend fun uploadProfilePicture(
    repository: MediaRepository,
    userId: String,
    uri: Uri,
    storagePath: String
): String? {
  return try {
    repository.upload(uri, storagePath)
  } catch (exception: Exception) {
    Log.e(
        "EditProfilePictureScreen",
        "Failed to upload profile image for user $userId with uri $uri",
        exception)
    null
  }
}

@VisibleForTesting
internal fun enqueueProfilePictureUpload(
    context: Context,
    userId: String,
    filePath: String,
    storagePath: String,
    existingImageUrl: String?,
    workManager: WorkManager = WorkManager.getInstance(context)
) {
  val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
  val workRequest =
      OneTimeWorkRequestBuilder<ImageUploadWorker>()
          .setConstraints(constraints)
          .setInputData(
              workDataOf(
                  ImageUploadWorker.KEY_DOCUMENT_ID to userId,
                  ImageUploadWorker.KEY_FILE_PATH to filePath,
                  ImageUploadWorker.KEY_STORAGE_PATH to storagePath,
                  ImageUploadWorker.KEY_EXISTING_IMAGE_URL to existingImageUrl,
                  ImageUploadWorker.KEY_COLLECTION_PATH to "users",
                  ImageUploadWorker.KEY_FIELD_NAME to "profilePictureUrl"))
          .addTag("profile_picture_upload_$userId")
          .build()

  workManager.enqueueUniqueWork(
      "profile_picture_upload_$userId", ExistingWorkPolicy.REPLACE, workRequest)
}

private fun isNetworkAvailable(context: Context): Boolean {
  val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
  val network = cm.activeNetwork ?: return false
  val capabilities = cm.getNetworkCapabilities(network) ?: return false
  return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@VisibleForTesting
internal suspend fun stageProfilePicture(
    context: Context,
    uri: Uri,
    userId: String,
    contentResolver: ContentResolver = context.contentResolver,
    filesDir: File = context.filesDir,
    mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
): String? =
    withContext(kotlinx.coroutines.Dispatchers.IO) {
      val dir = File(filesDir, "pending_profile_pictures").apply { mkdirs() }
      val extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ?: "jpg"
      val target = File(dir, "${userId}_${System.currentTimeMillis()}.$extension")
      try {
        contentResolver.openInputStream(uri)?.use { input ->
          target.outputStream().use { output -> input.copyTo(output) }
        } ?: return@withContext null
        target.absolutePath
      } catch (e: Exception) {
        if (e is java.util.concurrent.CancellationException) throw e
        val deleted = target.delete()
        if (!deleted) {
          Log.d("EditProfilePictureScreen", "Failed to delete staged profile picture")
        }
        null
      }
    }

@VisibleForTesting
internal suspend fun handleStagedProfilePicture(
    context: Context,
    userId: String,
    uri: Uri,
    storagePath: String,
    existingImageUrl: String?,
    viewModel: EditProfilePictureViewModel,
    workManager: WorkManager = WorkManager.getInstance(context)
): String? {
  val stagedPath = stageProfilePicture(context, uri, userId)
  if (stagedPath == null) return null
  val localUrl = "file://$stagedPath"
  viewModel.updateProfilePicture(localUrl)
  enqueueProfilePictureUpload(
      context = context,
      userId = userId,
      filePath = stagedPath,
      storagePath = storagePath,
      existingImageUrl = existingImageUrl,
      workManager = workManager)
  return localUrl
}

package com.github.se.studentconnect.ui.screen.profile.edit

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
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
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var initialImageUri by remember { mutableStateOf<Uri?>(null) }
  var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
  var userModified by remember { mutableStateOf(false) }
  val repository = MediaRepositoryProvider.repository
  val pickMediaLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
          selectedImageUri = uri
          userModified = true
        }
      }
  val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 16f), 0f) }
  var isUploading by remember { mutableStateOf(false) }

  LaunchedEffect(user?.profilePictureUrl, repository) {
    val profileId = user?.profilePictureUrl
    val previousInitial = initialImageUri
    val downloadedUri =
        profileId?.let {
          runCatching { withContext(Dispatchers.IO) { repository.download(it) } }
              .onFailure {
                android.util.Log.e(
                    "EditProfilePictureScreen", "Failed to download profile image: $profileId", it)
              }
              .getOrNull()
        }

    val newInitial =
        when {
          profileId.isNullOrBlank() -> null
          downloadedUri != null -> downloadedUri
          else -> previousInitial
        }

    initialImageUri = newInitial
    if (!userModified) {
      selectedImageUri = newInitial
    }
  }

  LaunchedEffect(selectedImageUri) {
    imageBitmap =
        if (selectedImageUri != null) loadBitmapFromUri(context, selectedImageUri!!) else null
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
              // Current Profile Picture Section
              Card(
                  modifier = Modifier.wrapContentWidth(),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          // Profile Picture Display - Centered
                          Box(
                              modifier =
                                  Modifier.size(140.dp)
                                      .clip(CircleShape)
                                      .background(
                                          MaterialTheme.colorScheme.surfaceVariant.copy(
                                              alpha = 0.2f))
                                      .clickable {
                                        pickMediaLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly))
                                      }
                                      .drawDashedCircleBorder(
                                          color =
                                              MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                  alpha = 0.5f),
                                          pathEffect = dashEffect,
                                          padding = 8.dp),
                              contentAlignment = Alignment.Center) {
                                if (imageBitmap != null) {
                                  Image(
                                      bitmap = imageBitmap!!,
                                      contentDescription = "Selected profile photo",
                                      modifier = Modifier.fillMaxSize(),
                                      contentScale = ContentScale.Crop)
                                  Text(
                                      text = "Tap to change photo",
                                      style =
                                          MaterialTheme.typography.bodySmall.copy(
                                              color = MaterialTheme.colorScheme.onSurfaceVariant),
                                      textAlign = TextAlign.Center,
                                      modifier =
                                          Modifier.align(Alignment.BottomCenter)
                                              .padding(bottom = 12.dp)
                                              .background(
                                                  color =
                                                      MaterialTheme.colorScheme.surface.copy(
                                                          alpha = 0.9f),
                                                  shape = RoundedCornerShape(12.dp))
                                              .padding(horizontal = 10.dp, vertical = 4.dp))
                                } else {
                                  Icon(
                                      imageVector = Icons.Default.Person,
                                      contentDescription = "Profile Picture",
                                      modifier = Modifier.size(60.dp),
                                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                              }

                          Text(
                              text =
                                  if (imageBitmap != null) {
                                    "Preview of your new profile picture"
                                  } else {
                                    "Tap above to choose a profile photo"
                                  },
                              style = MaterialTheme.typography.bodyMedium,
                              color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                imageBitmap = null
                                userModified = true
                              },
                              enabled =
                                  !isLoading &&
                                      !isUploading &&
                                      (selectedImageUri != null || initialImageUri != null),
                              modifier = Modifier.fillMaxWidth()) {
                                Text("Remove Photo")
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
                                      initialImageUri = null
                                      userModified = false
                                      selectedImageUri = null
                                      imageBitmap = null
                                      onNavigateBack?.invoke()
                                    } else {
                                      val uploadedId =
                                          uploadProfilePicture(
                                              repository = repository,
                                              userId = userId,
                                              uri = workingUri)
                                      if (uploadedId != null) {
                                        viewModel.updateProfilePicture(uploadedId)
                                        initialImageUri = workingUri
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
                              enabled = !isLoading && !isUploading && userModified,
                              modifier = Modifier.fillMaxWidth()) {
                                if (isLoading || isUploading) {
                                  CircularProgressIndicator(
                                      modifier = Modifier.size(16.dp),
                                      color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                  Text("Save Changes")
                                }
                              }
                        }
                  }
            }
      }
}

private fun Modifier.drawDashedCircleBorder(
    color: Color,
    pathEffect: PathEffect,
    padding: Dp,
    strokeWidth: Dp = 3.dp
): Modifier =
    this.then(
        Modifier.drawBehind {
          val strokePx = strokeWidth.toPx()
          val paddingPx = padding.toPx()
          val radius = (size.minDimension / 2f) - paddingPx - strokePx / 2f
          if (radius > 0f) {
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = strokePx, pathEffect = pathEffect))
          }
        })

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

private suspend fun loadBitmapFromUri(context: Context, uri: Uri): ImageBitmap? =
    withContext(Dispatchers.IO) {
      try {
        when (uri.scheme?.lowercase()) {
          "file" -> uri.path?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
          else ->
              context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
              }
        }
      } catch (_: Exception) {
        null
      }
    }

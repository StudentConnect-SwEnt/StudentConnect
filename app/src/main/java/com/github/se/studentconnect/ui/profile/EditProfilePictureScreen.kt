package com.github.se.studentconnect.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.let

/**
 * Screen for editing profile picture. Allows users to upload photos from gallery or take new
 * pictures.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePictureScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: EditProfilePictureViewModel = viewModel {
      EditProfilePictureViewModel(userRepository, userId)
    },
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  // Simplified image picker launcher (without external dependencies)
  val imagePickerLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadProfilePicture(it, context) }
      }

  // Show success/error messages
  LaunchedEffect(successMessage) {
    successMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearSuccessMessage()
      // Navigate back after successful upload
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
                  text = "Edit Profile Picture",
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
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)) {
                  // Current Profile Picture
                  Card(
                      modifier = Modifier.size(200.dp),
                      colors =
                          CardDefaults.cardColors(
                              containerColor = MaterialTheme.colorScheme.surfaceContainer),
                      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Box(
                            modifier =
                                Modifier.fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center) {
                              if (isLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                              } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                              }
                            }
                      }

                  // Action Buttons
                  Column(
                      verticalArrangement = Arrangement.spacedBy(16.dp),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        // Upload Photo Button
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()) {
                              Icon(
                                  imageVector = Icons.Default.PhotoLibrary,
                                  contentDescription = null,
                                  modifier = Modifier.padding(end = 8.dp))
                              Text("Upload Photo")
                            }

                        // Take Picture Button
                        OutlinedButton(
                            onClick = {
                              Toast.makeText(
                                      context, "Camera feature coming soon!", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()) {
                              Icon(
                                  imageVector = Icons.Default.CameraAlt,
                                  contentDescription = null,
                                  modifier = Modifier.padding(end = 8.dp))
                              Text("Take Picture")
                            }

                        // Remove Picture Button (only show if user has a profile picture)
                        if (currentUser.profilePictureUrl != null) {
                          OutlinedButton(
                              onClick = { viewModel.removeProfilePicture() },
                              enabled = !isLoading,
                              modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = "Remove Picture",
                                    color = MaterialTheme.colorScheme.error)
                              }
                        }
                      }

                  // Instructions
                  Text(
                      text =
                          "Select a photo from your gallery to update your profile picture. (Demo mode - image upload is simulated)",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(horizontal = 16.dp))
                }
          }
        }
      }
}

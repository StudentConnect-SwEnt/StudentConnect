package com.github.se.studentconnect.ui.screen.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.profile.edit.EditProfilePictureViewModel

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

  val snackbarHostState = remember { SnackbarHostState() }

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
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Navigate back")
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
                                  Modifier.size(120.dp)
                                      .clip(CircleShape)
                                      .background(MaterialTheme.colorScheme.primaryContainer),
                              contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default profile picture placeholder",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                              }

                          Text(
                              text = "No profile picture set",
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
                          Text(
                              text = "Change Profile Picture",
                              style = MaterialTheme.typography.titleMedium,
                              fontWeight = FontWeight.Bold)

                          // Camera Button
                          OutlinedButton(
                              onClick = { /* Camera functionality not yet implemented */},
                              modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera icon",
                                    modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Take Photo")
                              }

                          // Gallery Button
                          OutlinedButton(
                              onClick = { /* Gallery functionality not yet implemented */},
                              modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery icon",
                                    modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Choose from Gallery")
                              }

                          Spacer(modifier = Modifier.height(16.dp))

                          // Save Button
                          Button(
                              onClick = { /* Save functionality not yet implemented */},
                              modifier = Modifier.fillMaxWidth()) {
                                if (isLoading) {
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

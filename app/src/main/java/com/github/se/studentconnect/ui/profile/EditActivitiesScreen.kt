package com.github.se.studentconnect.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.repository.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.let

/**
 * Screen for editing user activities/hobbies. Provides a searchable list of activities with
 * multi-select functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivitiesScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    userRepository: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
    viewModel: EditActivitiesViewModel = viewModel {
      EditActivitiesViewModel(userRepository, userId)
    },
    modifier: Modifier = Modifier
) {
  val user by viewModel.user.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val filteredActivities by viewModel.filteredActivities.collectAsState()
  val selectedActivities by viewModel.selectedActivities.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

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
                  text = "Select Your Activities",
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
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  // Search Bar
                  OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { viewModel.updateSearchQuery(it) },
                      label = { Text("Search activities...") },
                      leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                      },
                      modifier = Modifier.fillMaxWidth(),
                      singleLine = true)

                  // Selected Count
                  Text(
                      text = "${viewModel.getSelectedCount()} activities selected",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Medium)

                  // Activities List
                  LazyColumn(
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.weight(1f)) {
                        items(filteredActivities) { activity ->
                          ActivityItem(
                              activity = activity,
                              isSelected = viewModel.isActivitySelected(activity),
                              onToggle = { viewModel.toggleActivitySelection(activity) })
                        }
                      }

                  // Save Button
                  Button(
                      onClick = { viewModel.saveActivities() },
                      enabled = !isLoading,
                      modifier = Modifier.fillMaxWidth()) {
                        if (isLoading) {
                          CircularProgressIndicator(
                              modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                        }
                        Text(text = if (isLoading) "Saving..." else "Save Activities")
                      }
                }
          }
        }
      }
}

/** Composable for individual activity item. */
@Composable
private fun ActivityItem(
    activity: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier = modifier.fillMaxWidth().clickable { onToggle() },
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                  } else {
                    MaterialTheme.colorScheme.surfaceContainer
                  }),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = activity,
                  style = MaterialTheme.typography.bodyLarge,
                  color =
                      if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      })

              // Selection indicator
              Box(
                  modifier =
                      Modifier.size(24.dp)
                          .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                          .background(
                              if (isSelected) {
                                MaterialTheme.colorScheme.primary
                              } else {
                                MaterialTheme.colorScheme.outline
                              }),
                  contentAlignment = Alignment.Center) {
                    if (isSelected) {
                      Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Selected",
                          modifier = Modifier.size(16.dp),
                          tint = MaterialTheme.colorScheme.onPrimary)
                    }
                  }
            }
      }
}

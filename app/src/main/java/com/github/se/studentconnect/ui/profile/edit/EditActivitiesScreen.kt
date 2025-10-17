package com.github.se.studentconnect.ui.profile.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.repository.UserRepository

/**
 * Screen for selecting user activities/hobbies. Provides searchable multi-select list with custom
 * activity support.
 *
 * @param userId The ID of the user whose activities are being edited
 * @param userRepository Repository for user data operations
 * @param onNavigateBack Callback to navigate back to profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivitiesScreen(
    userId: String,
    userRepository: UserRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
  val viewModel: EditActivitiesViewModel = viewModel {
    EditActivitiesViewModel(userRepository, userId)
  }

  val searchQuery by viewModel.searchQuery.collectAsState()
  val filteredActivities by viewModel.filteredActivities.collectAsState()
  val selectedActivities by viewModel.selectedActivities.collectAsState()
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // Handle UI state changes
  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is EditActivitiesViewModel.UiState.Success -> {
        snackbarHostState.showSnackbar(state.message)
        viewModel.resetState()
        // Navigate back after successful save
        kotlinx.coroutines.delay(500)
        onNavigateBack()
      }
      is EditActivitiesViewModel.UiState.Error -> {
        snackbarHostState.showSnackbar(state.message)
        viewModel.resetState()
      }
      else -> {}
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  text = "Select Your Activities",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      modifier = modifier) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Search Bar
          OutlinedTextField(
              value = searchQuery,
              onValueChange = { viewModel.updateSearchQuery(it) },
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              placeholder = { Text("Search activities...") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
              singleLine = true,
              shape = RoundedCornerShape(24.dp))

          // Selected count
          if (selectedActivities.isNotEmpty()) {
            Text(
                text =
                    "${selectedActivities.size} ${if (selectedActivities.size == 1) "activity" else "activities"} selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
          }

          // Activities List
          LazyColumn(
              modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredActivities) { activity ->
                  ActivityItem(
                      activity = activity,
                      isSelected = viewModel.isActivitySelected(activity),
                      onToggle = { viewModel.toggleActivity(activity) },
                      enabled = uiState !is EditActivitiesViewModel.UiState.Loading)
                }
              }

          // Save Button
          Button(
              onClick = { viewModel.saveActivities() },
              modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
              enabled =
                  uiState !is EditActivitiesViewModel.UiState.Loading &&
                      selectedActivities.isNotEmpty()) {
                if (uiState is EditActivitiesViewModel.UiState.Loading) {
                  CircularProgressIndicator(
                      modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                  Text(
                      text = "Save Activities",
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = FontWeight.SemiBold)
                }
              }
        }
      }
}

/** Individual activity item with checkbox. */
@Composable
private fun ActivityItem(
    activity: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Surface(
      onClick = { if (enabled) onToggle() },
      modifier = modifier.fillMaxWidth(),
      enabled = enabled,
      shape = RoundedCornerShape(12.dp),
      color =
          if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
      tonalElevation = if (isSelected) 4.dp else 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = activity,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                  color =
                      if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      })

              if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp))
              }
            }
      }
}

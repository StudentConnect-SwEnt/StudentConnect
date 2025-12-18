package com.github.se.studentconnect.ui.screen.profile.edit

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.user.UserRepository
import com.github.se.studentconnect.ui.components.ProfileSaveButton
import com.github.se.studentconnect.ui.profile.edit.EditActivitiesViewModel
import com.github.se.studentconnect.ui.utils.TopSnackbarHost

/**
 * Screen for selecting user activities/hobbies with searchable multi-select interface.
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
  val context = LocalContext.current

  // Handle snackbar messages
  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { message -> snackbarHostState.showSnackbar(message) }
  }

  LaunchedEffect(uiState) {
    when (val state = uiState) {
      is EditActivitiesViewModel.UiState.Success -> {
        // Navigate back immediately after successful save
        onNavigateBack()
        // Reset state after navigation to avoid cancelling LaunchedEffect
        viewModel.resetState()
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
                  text = stringResource(R.string.title_select_activities),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_back))
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface))
      },
      snackbarHost = { TopSnackbarHost(hostState = snackbarHostState) },
      modifier = modifier) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Search Bar
          OutlinedTextField(
              value = searchQuery,
              onValueChange = { viewModel.updateSearchQuery(it) },
              modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("searchField"),
              placeholder = { Text(stringResource(R.string.placeholder_search_activities)) },
              leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.content_description_search))
              },
              singleLine = true,
              shape = RoundedCornerShape(24.dp))

          if (selectedActivities.isNotEmpty()) {
            Text(
                text =
                    stringResource(
                        if (selectedActivities.size == 1) R.string.text_activities_selected
                        else R.string.text_activities_selected_plural,
                        selectedActivities.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
          }

          val isLoading = uiState is EditActivitiesViewModel.UiState.Loading

          LazyColumn(
              modifier = Modifier.weight(1f).padding(horizontal = 16.dp).testTag("activitiesList"),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredActivities) { activity ->
                  ActivityItem(
                      activity = activity,
                      isSelected = selectedActivities.contains(activity),
                      onToggle = { viewModel.toggleActivity(activity) },
                      enabled = !isLoading,
                      modifier = Modifier.testTag("activityItem_$activity"))
                }
              }

          ProfileSaveButton(
              onClick = { viewModel.saveActivities(context) },
              isLoading = isLoading,
              enabled = !isLoading,
              text = stringResource(R.string.button_save),
              modifier = Modifier.padding(16.dp).testTag("saveButton"))
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
  val backgroundColor =
      if (isSelected) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surfaceVariant
  val textColor =
      if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
      else MaterialTheme.colorScheme.onSurfaceVariant

  Surface(
      onClick = { if (enabled) onToggle() },
      modifier = modifier.fillMaxWidth(),
      enabled = enabled,
      shape = RoundedCornerShape(12.dp),
      color = backgroundColor,
      tonalElevation = if (isSelected) 4.dp else 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = activity,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                  color = textColor)

              if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.content_description_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp))
              }
            }
      }
}

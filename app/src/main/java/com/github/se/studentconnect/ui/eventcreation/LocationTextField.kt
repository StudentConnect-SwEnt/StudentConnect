package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.location.LocationRepository
import com.github.se.studentconnect.model.location.LocationRepositoryProvider
import com.github.se.studentconnect.utils.NetworkUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

data class LocationTextFieldUiState(
    val locationSuggestions: List<Location> = emptyList(),
    val isLoadingLocationSuggestions: Boolean = false,
)

@OptIn(FlowPreview::class)
class LocationTextFieldViewModel(
    private val locationRepository: LocationRepository = LocationRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(LocationTextFieldUiState())
  val uiState: StateFlow<LocationTextFieldUiState> = _uiState.asStateFlow()

  private val _offlineMessageRes = MutableStateFlow<Int?>(null)
  /** Resource ID for offline message to display. */
  val offlineMessageRes: StateFlow<Int?> = _offlineMessageRes.asStateFlow()

  private val queryFlow = MutableStateFlow("")

  private val numLocationSuggestions = 5
  private val stopTypingTime: Long = 500

  init {
    viewModelScope.launch {
      queryFlow
          .debounce(stopTypingTime) // only update after stopped typing
          .filter { it.isNotBlank() }
          .distinctUntilChanged() // ignore same value twice
          .collect { query ->
            // set as loading
            _uiState.value = uiState.value.copy(isLoadingLocationSuggestions = true)

            try {
              val suggestions = locationRepository.search(query).take(numLocationSuggestions)
              _uiState.value =
                  uiState.value.copy(
                      locationSuggestions = suggestions, isLoadingLocationSuggestions = false)
            } catch (_: Exception) {
              // reset loading flag on error
              _uiState.value =
                  uiState.value.copy(
                      locationSuggestions = listOf(), isLoadingLocationSuggestions = false)
            }
          }
    }
  }

  fun updateLocationSuggestions(locationString: String, isNetworkAvailable: Boolean) {
    // Show message if offline (location search requires network, so we prevent the attempt)
    if (!isNetworkAvailable) {
      _offlineMessageRes.value = R.string.offline_no_internet_try_later
      _uiState.value =
          uiState.value.copy(locationSuggestions = listOf(), isLoadingLocationSuggestions = false)
      return
    }

    _offlineMessageRes.value = null

    if (locationString.isBlank()) {
      _uiState.value =
          uiState.value.copy(locationSuggestions = listOf(), isLoadingLocationSuggestions = false)
    }
    queryFlow.value = locationString
  }

  fun clearOfflineMessage() {
    _offlineMessageRes.value = null
  }

  fun clearSuggestions() {
    _uiState.value =
        uiState.value.copy(locationSuggestions = listOf(), isLoadingLocationSuggestions = false)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTextField(
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    selectedLocation: Location? = null,
    onLocationChange: (Location?) -> Unit,
    locationTextFieldViewModel: LocationTextFieldViewModel = viewModel(),
    snackbarHostState: SnackbarHostState? = null,
) {
  val locationTextFieldUiState by locationTextFieldViewModel.uiState.collectAsState()
  val offlineMessageRes by locationTextFieldViewModel.offlineMessageRes.collectAsState()
  val locationSuggestions = locationTextFieldUiState.locationSuggestions
  val isLoadingLocationSuggestions = locationTextFieldUiState.isLoadingLocationSuggestions
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)

  // Show snackbar for offline messages
  LaunchedEffect(offlineMessageRes) {
    offlineMessageRes?.let { messageRes ->
      snackbarHostState?.let { hostState ->
        coroutineScope.launch {
          hostState.showSnackbar(
              message = context.getString(messageRes), duration = SnackbarDuration.Short)
          locationTextFieldViewModel.clearOfflineMessage()
        }
      }
    }
  }

  var userSelectedLocation by remember { mutableStateOf(selectedLocation) }
  var hasActiveQuery by remember { mutableStateOf(false) }
  var dropdownVisible by remember { mutableStateOf(false) }

  var locationFieldValue by remember {
    val initialText = selectedLocation?.toInputLabel().orEmpty()
    mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length)))
  }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(selectedLocation) {
    if (selectedLocation != null && selectedLocation != userSelectedLocation) {
      val text = selectedLocation.toInputLabel()
      locationFieldValue = TextFieldValue(text, TextRange(text.length))
      userSelectedLocation = selectedLocation
      dropdownVisible = false
      if (hasActiveQuery) hasActiveQuery = false
      locationTextFieldViewModel.clearSuggestions()
    } else if (selectedLocation == null && userSelectedLocation != null && !hasActiveQuery) {
      userSelectedLocation = null
      locationFieldValue = TextFieldValue("", TextRange.Zero)
      dropdownVisible = false
      locationTextFieldViewModel.clearSuggestions()
    }
  }

  val locationString = locationFieldValue.text
  val trimmedQuery = locationString.trim()

  // update suggestions every time the location string changes
  LaunchedEffect(trimmedQuery, hasActiveQuery, isNetworkAvailable) {
    if (!hasActiveQuery || trimmedQuery.isBlank()) {
      locationTextFieldViewModel.clearSuggestions()
      return@LaunchedEffect
    }
    locationTextFieldViewModel.updateLocationSuggestions(trimmedQuery, isNetworkAvailable)
  }

  val locationDropdownMenuIsExpanded =
      dropdownVisible && (locationSuggestions.isNotEmpty() || isLoadingLocationSuggestions)

  ExposedDropdownMenuBox(
      expanded = locationDropdownMenuIsExpanded,
      onExpandedChange = {},
  ) {
    ExposedDropdownMenu(
        expanded = locationDropdownMenuIsExpanded, onDismissRequest = { dropdownVisible = false }) {
          if (isLoadingLocationSuggestions && locationSuggestions.isEmpty()) {
            DropdownMenuItem(enabled = false, text = { Text("Searching...") }, onClick = {})
          }

          for (locationSuggestion in locationSuggestions) {
            DropdownMenuItem(
                text = { Text(locationSuggestion.toInputLabel()) },
                onClick = {
                  val selectedName = locationSuggestion.toInputLabel()
                  locationFieldValue =
                      TextFieldValue(
                          text = selectedName, selection = TextRange(selectedName.length))
                  dropdownVisible = false // reset interaction
                  if (hasActiveQuery) hasActiveQuery = false
                  userSelectedLocation = locationSuggestion
                  locationTextFieldViewModel.clearSuggestions() // clear stale suggestions
                  onLocationChange(locationSuggestion)
                })
          }
        }

    FormTextField(
        modifier =
            modifier.menuAnchor(MenuAnchorType.PrimaryEditable).focusRequester(focusRequester),
        value = locationFieldValue,
        onValueChange = {
          val newText = it.text
          locationFieldValue = it
          val hasText = newText.isNotBlank()
          dropdownVisible = hasText && isNetworkAvailable
          hasActiveQuery = hasText
          if (userSelectedLocation != null) {
            userSelectedLocation = null
            onLocationChange(null)
          }
        },
        label = label,
        placeholder = placeholder,
        enabled = isNetworkAvailable,
        errorText =
            if (locationFieldValue.text.isNotBlank() && userSelectedLocation == null)
                "Select a location from the suggestions"
            else null,
        trailingIcon = {
          val shouldShowClear = locationFieldValue.text.isNotBlank() || userSelectedLocation != null
          if (!shouldShowClear) return@FormTextField

          IconButton(
              modifier = Modifier.size(32.dp),
              colors =
                  IconButtonDefaults.iconButtonColors(
                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
              onClick = {
                locationFieldValue = TextFieldValue("", TextRange.Zero)
                dropdownVisible = false
                hasActiveQuery = false
                userSelectedLocation = null
                onLocationChange(null)
                locationTextFieldViewModel.clearSuggestions()
                focusRequester.requestFocus()
              }) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear location")
              }
        })
  }
}

private fun Location?.toInputLabel(): String {
  if (this == null) return ""
  return name?.takeIf { it.isNotBlank() } ?: "${latitude}, ${longitude}"
}

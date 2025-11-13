package com.github.se.studentconnect.ui.eventcreation

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.model.location.LocationRepository
import com.github.se.studentconnect.model.location.LocationRepositoryProvider
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

  private val queryFlow = MutableStateFlow("")

  private val numLocationSuggestions = 10
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

  fun updateLocationSuggestions(locationString: String) {
    if (locationString.isBlank()) {
      _uiState.value =
          uiState.value.copy(locationSuggestions = listOf(), isLoadingLocationSuggestions = false)
    }
    queryFlow.value = locationString
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
) {
  val locationTextFieldUiState by locationTextFieldViewModel.uiState.collectAsState()
  val locationSuggestions = locationTextFieldUiState.locationSuggestions
  val isLoadingLocationSuggestions = locationTextFieldUiState.isLoadingLocationSuggestions

  var userSelectedLocation by remember { mutableStateOf(selectedLocation) }
  var hasActiveQuery by remember { mutableStateOf(false) }
  var dropdownVisible by remember { mutableStateOf(false) }

  var locationFieldValue by
      remember {
        val initialText = selectedLocation?.toInputLabel().orEmpty()
        mutableStateOf(TextFieldValue(initialText, TextRange(initialText.length)))
      }

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
      locationFieldValue = TextFieldValue("", TextRange(0))
      dropdownVisible = false
      locationTextFieldViewModel.clearSuggestions()
    }
  }

  val locationString = locationFieldValue.text
  val trimmedQuery = locationString.trim()

  // update suggestions every time the location string changes
  LaunchedEffect(trimmedQuery, hasActiveQuery) {
    if (!hasActiveQuery || trimmedQuery.isBlank()) {
      locationTextFieldViewModel.clearSuggestions()
      return@LaunchedEffect
    }
    locationTextFieldViewModel.updateLocationSuggestions(trimmedQuery)
  }

  val locationDropdownMenuIsExpanded =
      dropdownVisible &&
          (locationSuggestions.isNotEmpty() || isLoadingLocationSuggestions)

  ExposedDropdownMenuBox(
      expanded = locationDropdownMenuIsExpanded,
      onExpandedChange = {},
  ) {
    ExposedDropdownMenu(
        expanded = locationDropdownMenuIsExpanded, onDismissRequest = { dropdownVisible = false }) {
      if (isLoadingLocationSuggestions && locationSuggestions.isEmpty()) {
        DropdownMenuItem(
            enabled = false,
            text = { Text("Searching...") },
            onClick = {})
      }

      for (locationSuggestion in locationSuggestions) {
        DropdownMenuItem(
            text = { Text(locationSuggestion.toInputLabel()) },
            onClick = {
              val selectedName = locationSuggestion.toInputLabel()
              locationFieldValue =
                  TextFieldValue(text = selectedName, selection = TextRange(selectedName.length))
              dropdownVisible = false // reset interaction
              if (hasActiveQuery) hasActiveQuery = false
              userSelectedLocation = locationSuggestion
              locationTextFieldViewModel.clearSuggestions() // clear stale suggestions
              onLocationChange(locationSuggestion)
            })
      }
    }

    FormTextField(
        modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
        value = locationFieldValue,
        onValueChange = {
          val newText = it.text
          locationFieldValue = it
          val hasText = newText.isNotBlank()
          dropdownVisible = hasText
          hasActiveQuery = hasText
          if (userSelectedLocation != null) {
            userSelectedLocation = null
            onLocationChange(null)
          }
        },
        label = label,
        placeholder = placeholder,
        errorText =
            if (userSelectedLocation == null) "Select a location from the suggestions" else null)
  }
}

private fun Location?.toInputLabel(): String {
  if (this == null) return ""
  return name?.takeIf { it.isNotBlank() } ?: "${latitude}, ${longitude}"
}

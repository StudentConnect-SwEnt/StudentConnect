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
              // TODO: log error somewhere?
            }
          }
    }
  }

  fun updateLocationSuggestions(locationString: String) {
    queryFlow.value = locationString

    if (locationString.isBlank())
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
    initialValue: String = "",
    onLocationChange: (Location?) -> Unit,
    locationTextFieldViewModel: LocationTextFieldViewModel = viewModel(),
) {
  val locationTextFieldUiState by locationTextFieldViewModel.uiState.collectAsState()
  val locationSuggestions = locationTextFieldUiState.locationSuggestions
  val isLoadingLocationSuggestions = locationTextFieldUiState.isLoadingLocationSuggestions

  var locationHasBeenInteractedWith by remember { mutableStateOf(false) }
  val locationDropdownMenuIsExpanded =
      locationHasBeenInteractedWith && locationSuggestions.isNotEmpty()

  var locationFieldValue by remember {
    mutableStateOf(TextFieldValue(initialValue, TextRange(initialValue.length)))
  }

  val locationString = locationFieldValue.text

  // update suggestions every time the location string changes
  LaunchedEffect(locationString) {
    locationTextFieldViewModel.updateLocationSuggestions(locationString)
  }

  LaunchedEffect(locationString, locationSuggestions, isLoadingLocationSuggestions) {
    // don't update while it is loading suggestions
    if (isLoadingLocationSuggestions) return@LaunchedEffect

    val location =
        if (locationString.isBlank() || locationSuggestions.isEmpty()) null
        else locationSuggestions[0]

    onLocationChange(location)
  }

  ExposedDropdownMenuBox(
      expanded = locationDropdownMenuIsExpanded,
      onExpandedChange = {},
  ) {
    ExposedDropdownMenu(
        expanded = locationDropdownMenuIsExpanded,
        onDismissRequest = { locationHasBeenInteractedWith = false }, // reset interaction
    ) {
      for (locationSuggestion in locationSuggestions) {
        DropdownMenuItem(
            text = {
              Text(
                  locationSuggestion.name
                      ?: "(${locationSuggestion.latitude}, ${locationSuggestion.longitude})")
            },
            onClick = {
              locationFieldValue =
                  TextFieldValue(
                      text = locationSuggestion.name ?: "",
                      selection = TextRange(locationSuggestion.name?.length ?: 0))
              locationHasBeenInteractedWith = false // reset interaction
            })
      }
    }

    FormTextField(
        modifier = modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
        value = locationFieldValue,
        onValueChange = {
          locationFieldValue = it
          locationHasBeenInteractedWith = true
        },
        label = label,
        placeholder = placeholder,
    )
  }
}

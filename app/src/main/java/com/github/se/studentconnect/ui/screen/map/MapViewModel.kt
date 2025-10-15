package com.github.se.studentconnect.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.map.LocationConfig
import com.github.se.studentconnect.model.map.LocationRepository
import com.github.se.studentconnect.model.map.LocationResult
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val searchText: String = "",
    val isEventsView: Boolean = true,
    val hasLocationPermission: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val targetLocation: Point? = null,
    val shouldAnimateToLocation: Boolean = false,
    val events: List<Event> = emptyList()
)

object MapConfiguration {

  object Coordinates {
    const val EPFL_LONGITUDE = 6.6283
    const val EPFL_LATITUDE = 46.5089
  }

  object Zoom {
    const val INITIAL = 6.0
    const val DEFAULT = 10.0
    const val TARGET = 10.0
    const val LOCATE_USER = 10.0
  }

  object Animation {
    const val INITIAL_DURATION_MS = 2000L
    const val TARGET_DURATION_MS = 2500L
    const val LOCATE_USER_DURATION_MS = 1500L
  }

  object Camera {
    const val BEARING = 0.0
    const val PITCH = 0.0
  }
}

sealed class MapViewEvent {
  object ToggleView : MapViewEvent()

  object LocateUser : MapViewEvent()

  object ClearError : MapViewEvent()

  object ClearLocationAnimation : MapViewEvent()

  data class UpdateSearchText(val text: String) : MapViewEvent()

  data class SetLocationPermission(val granted: Boolean) : MapViewEvent()

  data class SetTargetLocation(val latitude: Double, val longitude: Double, val zoom: Double) :
      MapViewEvent()
}

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapUiState())
  val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

  init {
    loadEvents()
  }

  private fun loadEvents() {
    viewModelScope.launch {
      try {
        val events = eventRepository.getAllVisibleEvents()
        _uiState.value = _uiState.value.copy(events = events)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(errorMessage = "Failed to load events: ${e.message}")
      }
    }
  }

  fun onEvent(event: MapViewEvent) {
    when (event) {
      is MapViewEvent.ToggleView -> {
        _uiState.value = _uiState.value.copy(isEventsView = !_uiState.value.isEventsView)
      }
      is MapViewEvent.UpdateSearchText -> {
        _uiState.value = _uiState.value.copy(searchText = event.text)
      }
      is MapViewEvent.SetLocationPermission -> {
        _uiState.value =
            _uiState.value.copy(
                hasLocationPermission = event.granted,
                errorMessage = if (!event.granted) LocationConfig.PERMISSION_REQUIRED else null)
      }
      is MapViewEvent.LocateUser -> {
        locateUser()
      }
      is MapViewEvent.ClearError -> {
        _uiState.value = _uiState.value.copy(errorMessage = null)
      }
      is MapViewEvent.ClearLocationAnimation -> {
        _uiState.value = _uiState.value.copy(shouldAnimateToLocation = false)
      }
      is MapViewEvent.SetTargetLocation -> {
        val targetPoint = Point.fromLngLat(event.longitude, event.latitude)
        _uiState.value = _uiState.value.copy(targetLocation = targetPoint)
      }
    }
  }

  private fun locateUser() {
    if (!_uiState.value.hasLocationPermission) {
      _uiState.value =
          _uiState.value.copy(errorMessage = LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE)
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)

      when (val result = locationRepository.getCurrentLocation()) {
        is LocationResult.Success -> {
          val userPoint = Point.fromLngLat(result.location.longitude, result.location.latitude)
          _uiState.value =
              _uiState.value.copy(
                  targetLocation = userPoint, shouldAnimateToLocation = true, isLoading = false)
        }
        is LocationResult.Error -> {
          _uiState.value = _uiState.value.copy(errorMessage = result.message, isLoading = false)
        }
        is LocationResult.PermissionDenied -> {
          _uiState.value =
              _uiState.value.copy(
                  errorMessage = LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE, isLoading = false)
        }
        is LocationResult.Timeout -> {
          _uiState.value =
              _uiState.value.copy(errorMessage = LocationConfig.LOCATION_TIMEOUT, isLoading = false)
        }
        is LocationResult.LocationDisabled -> {
          _uiState.value =
              _uiState.value.copy(
                  errorMessage = LocationConfig.LOCATION_DISABLED, isLoading = false)
        }
      }
    }
  }

  suspend fun animateToTarget(
      mapViewportState: MapViewportState,
      latitude: Double,
      longitude: Double,
      zoom: Double
  ) {
    val targetPoint = Point.fromLngLat(longitude, latitude)
    mapViewportState.flyTo(
        cameraOptions {
          center(targetPoint)
          zoom(zoom)
          bearing(MapConfiguration.Camera.BEARING)
          pitch(MapConfiguration.Camera.PITCH)
        },
        MapAnimationOptions.mapAnimationOptions {
          duration(MapConfiguration.Animation.TARGET_DURATION_MS)
        })
  }

  suspend fun animateToUserLocation(mapViewportState: MapViewportState) {
    _uiState.value.targetLocation?.let { targetPoint ->
      mapViewportState.flyTo(
          cameraOptions {
            center(targetPoint)
            zoom(MapConfiguration.Zoom.LOCATE_USER)
            bearing(MapConfiguration.Camera.BEARING)
            pitch(MapConfiguration.Camera.PITCH)
          },
          MapAnimationOptions.mapAnimationOptions {
            duration(MapConfiguration.Animation.LOCATE_USER_DURATION_MS)
          })
    }
  }
}

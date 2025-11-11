package com.github.se.studentconnect.ui.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.event.EventRepository
import com.github.se.studentconnect.model.event.EventRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendLocation
import com.github.se.studentconnect.model.friends.FriendsLocationRepository
import com.github.se.studentconnect.model.friends.FriendsLocationRepositoryProvider
import com.github.se.studentconnect.model.friends.FriendsRepository
import com.github.se.studentconnect.model.friends.FriendsRepositoryProvider
import com.github.se.studentconnect.repository.LocationConfig
import com.github.se.studentconnect.repository.LocationRepository
import com.github.se.studentconnect.repository.LocationResult
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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
    val events: List<Event> = emptyList(),
    val friendLocations: Map<String, FriendLocation> = emptyMap(),
    val selectedEvent: Event? = null,
    val selectedEventLocation: Point? = null,
    val shouldAnimateToSelectedEvent: Boolean = false
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

  object ClearEventSelectionAnimation : MapViewEvent()

  data class UpdateSearchText(val text: String) : MapViewEvent()

  data class SetLocationPermission(val granted: Boolean) : MapViewEvent()

  data class SetTargetLocation(val latitude: Double, val longitude: Double, val zoom: Double) :
      MapViewEvent()

  data class SelectEvent(val eventUid: String?) : MapViewEvent()
}

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val friendsRepository: FriendsRepository = FriendsRepositoryProvider.repository,
    private val friendsLocationRepository: FriendsLocationRepository =
        FriendsLocationRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapUiState())
  val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

  init {
    // Only load events if the current state has no events
    if (_uiState.value.events.isEmpty()) {
      loadEvents()
    }
    // Start observing friend locations
    observeFriendLocations()
  }

  override fun onCleared() {
    super.onCleared()
    // Stop listening when ViewModel is cleared
    friendsLocationRepository.stopListening()
  }

  private fun loadEvents() {
    viewModelScope.launch {
      try {
        val events = eventRepository.getAllVisibleEvents()
        val eventsWithLocation = events.filter { it.location != null }
        android.util.Log.d(
            "MapViewModel",
            "Loaded ${events.size} events, ${eventsWithLocation.size} have locations")
        _uiState.value = _uiState.value.copy(events = events)
      } catch (e: Exception) {
        android.util.Log.e("MapViewModel", "Failed to load events", e)
        _uiState.value = _uiState.value.copy(errorMessage = "Failed to load events: ${e.message}")
      }
    }
  }

  /**
   * Observes real-time location updates for the current user's friends. This runs continuously in
   * the background and updates the UI state whenever a friend's location changes.
   */
  private fun observeFriendLocations() {
    viewModelScope.launch {
      try {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId == null) {
          android.util.Log.d("MapViewModel", "No authenticated user, skipping friend locations")
          return@launch
        }

        // Get the list of friends
        val friendIds = friendsRepository.getFriends(currentUserId)
        android.util.Log.d("MapViewModel", "Observing ${friendIds.size} friends' locations")

        if (friendIds.isEmpty()) {
          return@launch
        }

        // Collect friend location updates
        friendsLocationRepository.observeFriendLocations(currentUserId, friendIds).collect {
            locations ->
          _uiState.value = _uiState.value.copy(friendLocations = locations)
          android.util.Log.d(
              "MapViewModel", "Updated friend locations: ${locations.size} friends visible")
        }
      } catch (e: Exception) {
        android.util.Log.e("MapViewModel", "Failed to observe friend locations", e)
      }
    }
  }

  /**
   * Shares the current user's location with friends. This should be called periodically when the
   * user has the map open and wants to share their location.
   */
  fun shareCurrentLocation() {
    if (!_uiState.value.hasLocationPermission) {
      android.util.Log.d("MapViewModel", "Cannot share location without permission")
      return
    }

    viewModelScope.launch {
      try {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId == null) {
          android.util.Log.d("MapViewModel", "No authenticated user, cannot share location")
          return@launch
        }

        when (val result = locationRepository.getCurrentLocation()) {
          is LocationResult.Success -> {
            friendsLocationRepository.updateUserLocation(
                currentUserId, result.location.latitude, result.location.longitude)
            android.util.Log.d(
                "MapViewModel",
                "Shared location: (${result.location.latitude}, ${result.location.longitude})")
          }
          else -> {
            android.util.Log.d("MapViewModel", "Could not get current location to share")
          }
        }
      } catch (e: Exception) {
        android.util.Log.e("MapViewModel", "Failed to share location", e)
      }
    }
  }

  /**
   * Stops sharing the current user's location with friends. This should be called when the user
   * closes the map or explicitly stops sharing.
   */
  fun stopSharingLocation() {
    viewModelScope.launch {
      try {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId != null) {
          friendsLocationRepository.removeUserLocation(currentUserId)
          android.util.Log.d("MapViewModel", "Stopped sharing location")
        }
      } catch (e: Exception) {
        android.util.Log.e("MapViewModel", "Failed to stop sharing location", e)
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
      is MapViewEvent.SelectEvent -> {
        val selectedEvent =
            event.eventUid?.let { uid -> _uiState.value.events.find { it.uid == uid } }
        val selectedLocation =
            selectedEvent?.location?.let { location ->
              Point.fromLngLat(location.longitude, location.latitude)
            }
        _uiState.value =
            _uiState.value.copy(
                selectedEvent = selectedEvent,
                selectedEventLocation = selectedLocation,
                shouldAnimateToSelectedEvent = selectedEvent != null)
      }
      is MapViewEvent.ClearEventSelectionAnimation -> {
        _uiState.value = _uiState.value.copy(shouldAnimateToSelectedEvent = false)
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

  suspend fun animateToSelectedEvent(mapViewportState: MapViewportState) {
    _uiState.value.selectedEventLocation?.let { eventLocation ->
      mapViewportState.flyTo(
          cameraOptions {
            center(eventLocation)
            // Keep current zoom or use a comfortable zoom level
            zoom(mapViewportState.cameraState?.zoom?.coerceAtLeast(MapConfiguration.Zoom.TARGET))
            bearing(MapConfiguration.Camera.BEARING)
            pitch(MapConfiguration.Camera.PITCH)
          },
          MapAnimationOptions.mapAnimationOptions {
            duration(800L) // Shorter animation for event selection
          })
    }
  }
}

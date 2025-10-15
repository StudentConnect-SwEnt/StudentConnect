package com.github.se.studentconnect.ui.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.BuildConfig
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.map.LocationRepositoryImpl
import com.github.se.studentconnect.model.map.RequestLocationPermission
import com.github.se.studentconnect.resources.C
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

/**
 * Main map screen composable that displays an interactive map with event markers.
 *
 * Features:
 * - Toggle between Events and Friends view
 * - Search locations
 * - Event marker clustering for clean visualization
 * - User location tracking (with permission)
 *
 * @param targetLatitude Optional latitude to animate the map to on load
 * @param targetLongitude Optional longitude to animate the map to on load
 * @param targetZoom Zoom level for the target location (default: MapConfiguration.Zoom.TARGET)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    targetLatitude: Double? = null,
    targetLongitude: Double? = null,
    targetZoom: Double = MapConfiguration.Zoom.TARGET,
) {
  val context = LocalContext.current
  val actualViewModel: MapViewModel = viewModel { MapViewModel(LocationRepositoryImpl(context)) }
  val uiState by actualViewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      zoom(MapConfiguration.Zoom.INITIAL)
      center(
          Point.fromLngLat(
              MapConfiguration.Coordinates.EPFL_LONGITUDE,
              MapConfiguration.Coordinates.EPFL_LATITUDE))
      bearing(MapConfiguration.Camera.BEARING)
      pitch(MapConfiguration.Camera.PITCH)
    }
    flyTo(
        cameraOptions {
          center(
              Point.fromLngLat(
                  MapConfiguration.Coordinates.EPFL_LONGITUDE,
                  MapConfiguration.Coordinates.EPFL_LATITUDE))
          zoom(MapConfiguration.Zoom.DEFAULT)
          bearing(MapConfiguration.Camera.BEARING)
          pitch(MapConfiguration.Camera.PITCH)
        },
        MapAnimationOptions.mapAnimationOptions {
          duration(MapConfiguration.Animation.INITIAL_DURATION_MS)
        })
  }

  RequestLocationPermission(
      onPermissionGranted = { actualViewModel.onEvent(MapViewEvent.SetLocationPermission(true)) },
      onPermissionDenied = { actualViewModel.onEvent(MapViewEvent.SetLocationPermission(false)) })

  LaunchedEffect(targetLatitude, targetLongitude) {
    if (targetLatitude != null && targetLongitude != null) {
      actualViewModel.onEvent(
          MapViewEvent.SetTargetLocation(targetLatitude, targetLongitude, targetZoom))
      actualViewModel.animateToTarget(mapViewportState, targetLatitude, targetLongitude, targetZoom)
    }
  }

  LaunchedEffect(uiState.shouldAnimateToLocation) {
    if (uiState.shouldAnimateToLocation && uiState.targetLocation != null) {
      actualViewModel.animateToUserLocation(mapViewportState)
      actualViewModel.onEvent(MapViewEvent.ClearLocationAnimation)
    }
  }

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      actualViewModel.onEvent(MapViewEvent.ClearError)
    }
  }

  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopAppBar(
          title = {
            Text(
                text = "Map",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
          },
          modifier = Modifier.semantics { testTag = C.Tag.map_top_app_bar })

      SearchBar(
          searchText = uiState.searchText,
          onSearchTextChange = { actualViewModel.onEvent(MapViewEvent.UpdateSearchText(it)) },
          modifier =
              Modifier.fillMaxWidth()
                  .padding(horizontal = Padding.CONTENT, vertical = Padding.VERTICAL_SPACING))

      MapContainer(
          mapViewportState = mapViewportState,
          hasLocationPermission = uiState.hasLocationPermission,
          isEventsView = uiState.isEventsView,
          events = uiState.events,
          onToggleView = { actualViewModel.onEvent(MapViewEvent.ToggleView) },
          onLocateUser = { actualViewModel.onEvent(MapViewEvent.LocateUser) },
          modifier =
              Modifier.fillMaxSize()
                  .padding(
                      start = Padding.CONTENT,
                      end = Padding.CONTENT,
                      bottom = Padding.CONTENT,
                      top = Padding.VERTICAL_SPACING)
                  .semantics { testTag = C.Tag.map_container })
    }

    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

/**
 * Search bar composable for location search functionality.
 *
 * @param searchText Current search text value
 * @param onSearchTextChange Callback invoked when search text changes
 * @param modifier Modifier to be applied to the search bar
 */
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  TextField(
      value = searchText,
      onValueChange = onSearchTextChange,
      placeholder = {
        Text(text = "Search locations...", color = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      leadingIcon = {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      modifier = modifier.semantics { testTag = C.Tag.map_search_field },
      shape = RoundedCornerShape(Corner.RADIUS),
      singleLine = true,
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedTextColor = MaterialTheme.colorScheme.onSurface,
              unfocusedTextColor = MaterialTheme.colorScheme.onSurface))
}

/**
 * Container for the Mapbox map with event markers and action buttons.
 *
 * Handles:
 * - Map rendering with Mapbox SDK
 * - Event marker display with clustering
 * - User location puck
 * - Dynamic layer management based on view state
 *
 * @param mapViewportState Viewport state for camera control
 * @param hasLocationPermission Whether location permission is granted
 * @param isEventsView Whether currently in events view (vs friends view)
 * @param events List of events to display as markers
 * @param onToggleView Callback to toggle between views
 * @param onLocateUser Callback to center map on user location
 * @param modifier Modifier to be applied to the container
 */
@Composable
private fun MapContainer(
    mapViewportState: MapViewportState,
    hasLocationPermission: Boolean,
    isEventsView: Boolean,
    events: List<Event>,
    onToggleView: () -> Unit,
    onLocateUser: () -> Unit,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Box(modifier = modifier.clip(RoundedCornerShape(Corner.MAP_RADIUS))) {
    if (BuildConfig.USE_MOCK_MAP || isInAndroidTest()) {
      TestMapboxMap()
    } else {
      MapboxMap(
          modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.map_screen },
          mapViewportState = mapViewportState,
          scaleBar = {},
          logo = {},
          attribution = {},
          compass = {}) {
            MapEffect(true) { mapView ->
              mapView.location.updateSettings {
                locationPuck = createDefault2DPuck(withBearing = true)
                puckBearingEnabled = true
                enabled = true
                pulsingEnabled = true
              }
            }

            // Add event markers with clustering - only in events view
            MapEffect(isEventsView, events) { mapView ->
              mapView.mapboxMap.getStyle { style ->
                EventMarkers.removeExistingEventLayers(style)

                if (isEventsView && events.isNotEmpty()) {
                  EventMarkers.addEventMarkerIcon(context, style)
                  val features = EventMarkers.createEventFeatures(events)
                  EventMarkers.addEventSource(style, features)
                  EventMarkers.addClusterLayers(style)
                  EventMarkers.addIndividualMarkerLayer(style)
                }
              }
            }
          }
    }

    MapActionButtons(
        hasLocationPermission = hasLocationPermission,
        isEventsView = isEventsView,
        onLocateUser = onLocateUser,
        onToggleView = onToggleView)
  }
}

/**
 * Floating action buttons overlaid on the map for user interactions.
 *
 * Displays:
 * - "Locate Me" button (if location permission granted)
 * - "Toggle View" button (Events/Friends)
 *
 * @param hasLocationPermission Whether to show the location button
 * @param isEventsView Current view state for button icon
 * @param onLocateUser Callback to center map on user
 * @param onToggleView Callback to toggle view
 */
@Composable
private fun BoxScope.MapActionButtons(
    hasLocationPermission: Boolean,
    isEventsView: Boolean,
    onLocateUser: () -> Unit,
    onToggleView: () -> Unit
) {
  if (hasLocationPermission) {
    FloatingActionButton(
        onClick = onLocateUser,
        modifier =
            Modifier.align(Alignment.BottomStart)
                .padding(Padding.CONTENT)
                .size(Size.FAB)
                .semantics { testTag = C.Tag.map_locate_user_fab },
        shape = RoundedCornerShape(Corner.RADIUS),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = Elevation.DEFAULT)) {
          Icon(
              imageVector = Icons.Default.MyLocation,
              contentDescription = "Center on my location",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.Companion.size(Size.ICON))
        }
  }

  FloatingActionButton(
      onClick = onToggleView,
      modifier =
          Modifier.align(Alignment.BottomEnd).padding(Padding.CONTENT).size(Size.FAB).semantics {
            testTag = C.Tag.map_toggle_view_fab
          },
      shape = RoundedCornerShape(Corner.RADIUS),
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = Elevation.DEFAULT)) {
        Icon(
            painter =
                painterResource(
                    id = if (isEventsView) R.drawable.ic_map_events else R.drawable.ic_map_friends),
            contentDescription = if (isEventsView) "Events View" else "Friends View",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.Companion.size(Size.LARGE_ICON))
      }
}

/**
 * Detects if the app is running in an Android instrumentation test environment. This allows us to
 * use the mock map during Android tests without changing build config.
 */
private fun isInAndroidTest(): Boolean {
  return try {
    Class.forName("androidx.test.espresso.Espresso")
    true
  } catch (e: ClassNotFoundException) {
    false
  }
}

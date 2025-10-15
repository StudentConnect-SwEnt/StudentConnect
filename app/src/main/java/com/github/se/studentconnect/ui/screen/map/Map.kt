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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.BuildConfig
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.map.LocationRepositoryImpl
import com.github.se.studentconnect.model.map.RequestLocationPermission
import com.github.se.studentconnect.resources.C
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.has
import com.mapbox.maps.extension.style.expressions.dsl.generated.not
import com.mapbox.maps.extension.style.expressions.dsl.generated.toString
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

/** UI dimension constants for padding and spacing */
object Padding {
  val CONTENT: Dp = 16.dp
  val VERTICAL_SPACING: Dp = 8.dp
}

/** UI dimension constants for component sizes */
object Size {
  val FAB: Dp = 56.dp
  val ICON: Dp = 24.dp
  val LARGE_ICON: Dp = 32.dp
}

/** UI dimension constants for corner radii */
object Corner {
  val RADIUS: Dp = 12.dp
  val MAP_RADIUS: Dp = 16.dp
}

/** UI dimension constants for elevation */
object Elevation {
  val DEFAULT: Dp = 0.dp
}

/** Configuration constants for event markers and clustering */
private object EventMarkerConfig {
  const val ICON_ID = "event_marker_icon"
  const val SOURCE_ID = "event_source"
  const val LAYER_ID = "event_layer"
  const val CLUSTER_LAYER_ID = "event_cluster_layer"
  const val CLUSTER_COUNT_LAYER_ID = "event_cluster_count_layer"

  const val COLOR = "#EF4444"
  const val ICON_SIZE = 1.5
  const val CLUSTER_RADIUS_PX = 30
  const val CLUSTER_MAX_ZOOM = 16
  const val CLUSTER_CIRCLE_RADIUS = 20.0
  const val CLUSTER_STROKE_WIDTH = 2.0
  const val CLUSTER_STROKE_COLOR = "#FFFFFF"
  const val CLUSTER_TEXT_SIZE = 14.0
  const val CLUSTER_TEXT_COLOR = "#FFFFFF"

  val CLUSTER_TEXT_FONTS = listOf("DIN Offc Pro Bold", "Arial Unicode MS Bold")
}

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
                removeExistingEventLayers(style)

                if (isEventsView && events.isNotEmpty()) {
                  addEventMarkerIcon(context, style)
                  val features = createEventFeatures(events)
                  addEventSource(style, features)
                  addClusterLayers(style)
                  addIndividualMarkerLayer(style)
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

// ========================================
// Event Marker Helper Functions
// ========================================

/**
 * Removes existing event marker layers and sources from the map style. This ensures clean state
 * before adding new markers.
 */
private fun removeExistingEventLayers(style: com.mapbox.maps.Style) {
  val layersToRemove =
      listOf(
          EventMarkerConfig.CLUSTER_COUNT_LAYER_ID,
          EventMarkerConfig.CLUSTER_LAYER_ID,
          EventMarkerConfig.LAYER_ID)

  layersToRemove.forEach { layerId ->
    if (style.styleLayerExists(layerId)) {
      style.removeStyleLayer(layerId)
    }
  }

  if (style.styleSourceExists(EventMarkerConfig.SOURCE_ID)) {
    style.removeStyleSource(EventMarkerConfig.SOURCE_ID)
  }
}

/** Adds the event marker icon to the map style with the configured color tint. */
private fun addEventMarkerIcon(context: android.content.Context, style: com.mapbox.maps.Style) {
  val markerIcon = ContextCompat.getDrawable(context, R.drawable.ic_location)
  markerIcon?.let { drawable ->
    drawable.setTint(android.graphics.Color.parseColor(EventMarkerConfig.COLOR))
    if (!style.hasStyleImage(EventMarkerConfig.ICON_ID)) {
      style.addImage(EventMarkerConfig.ICON_ID, drawable.toBitmap())
    }
  }
}

/**
 * Creates GeoJSON features from events that have location data. Each feature includes the event's
 * title and UID as properties.
 */
private fun createEventFeatures(events: List<Event>): List<Feature> {
  return events.mapNotNull { event ->
    event.location?.let { location ->
      Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude)).apply {
        addStringProperty("title", event.title)
        addStringProperty("uid", event.uid)
      }
    }
  }
}

/** Adds a GeoJSON source with clustering enabled to the map style. */
private fun addEventSource(style: com.mapbox.maps.Style, features: List<Feature>) {
  val featureCollection = FeatureCollection.fromFeatures(features)
  style.addSource(
      geoJsonSource(EventMarkerConfig.SOURCE_ID) {
        featureCollection(featureCollection)
        cluster(true)
        clusterRadius(EventMarkerConfig.CLUSTER_RADIUS_PX.toLong())
        clusterMaxZoom(EventMarkerConfig.CLUSTER_MAX_ZOOM.toLong())
      })
}

/**
 * Adds cluster circle and count text layers to the map style. These layers display when multiple
 * events are grouped together.
 */
private fun addClusterLayers(style: com.mapbox.maps.Style) {
  // Add cluster circle layer
  style.addLayer(
      circleLayer(EventMarkerConfig.CLUSTER_LAYER_ID, EventMarkerConfig.SOURCE_ID) {
        circleColor(EventMarkerConfig.COLOR)
        circleRadius(EventMarkerConfig.CLUSTER_CIRCLE_RADIUS)
        circleStrokeWidth(EventMarkerConfig.CLUSTER_STROKE_WIDTH)
        circleStrokeColor(EventMarkerConfig.CLUSTER_STROKE_COLOR)
        filter(has { literal("point_count") })
      })

  // Add cluster count text layer
  style.addLayer(
      symbolLayer(EventMarkerConfig.CLUSTER_COUNT_LAYER_ID, EventMarkerConfig.SOURCE_ID) {
        textField(toString { get { literal("point_count") } })
        textSize(EventMarkerConfig.CLUSTER_TEXT_SIZE)
        textColor(EventMarkerConfig.CLUSTER_TEXT_COLOR)
        textFont(EventMarkerConfig.CLUSTER_TEXT_FONTS)
        filter(has { literal("point_count") })
      })
}

/** Adds a symbol layer for individual event markers (non-clustered points). */
private fun addIndividualMarkerLayer(style: com.mapbox.maps.Style) {
  style.addLayer(
      symbolLayer(EventMarkerConfig.LAYER_ID, EventMarkerConfig.SOURCE_ID) {
        iconImage(EventMarkerConfig.ICON_ID)
        iconAllowOverlap(true)
        iconAnchor(IconAnchor.BOTTOM)
        iconSize(EventMarkerConfig.ICON_SIZE)
        filter(not { has { literal("point_count") } })
      })
}

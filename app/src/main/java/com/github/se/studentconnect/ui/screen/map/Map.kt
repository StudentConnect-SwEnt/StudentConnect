package com.github.se.studentconnect.ui.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.BuildConfig
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.github.se.studentconnect.model.friends.FriendLocation
import com.github.se.studentconnect.model.map.LocationRepositoryImpl
import com.github.se.studentconnect.model.map.RequestLocationPermission
import com.github.se.studentconnect.resources.C
import com.github.se.studentconnect.ui.utils.TopSnackbarHost
import com.google.firebase.Timestamp
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import java.text.SimpleDateFormat
import java.util.Locale

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
 * @param targetEventUid Optional event UID to automatically select and display on load
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    targetLatitude: Double? = null,
    targetLongitude: Double? = null,
    targetZoom: Double = MapConfiguration.Zoom.TARGET,
    targetEventUid: String? = null,
) {
  val context = LocalContext.current
  val actualViewModel: MapViewModel = viewModel { MapViewModel(LocationRepositoryImpl(context)) }
  val uiState by actualViewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  androidx.compose.runtime.DisposableEffect(Unit) {
    onDispose {
      actualViewModel.stopSharingLocation()
      FriendMarkers.clearCaches()
    }
  }

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

  LaunchedEffect(targetLatitude, targetLongitude, targetEventUid) {
    if (targetLatitude != null && targetLongitude != null) {
      actualViewModel.onEvent(
          MapViewEvent.SetTargetLocation(targetLatitude, targetLongitude, targetZoom))
      if (targetEventUid == null) {
        actualViewModel.animateToTarget(
            mapViewportState, targetLatitude, targetLongitude, targetZoom)
      }
    }
  }

  LaunchedEffect(uiState.shouldAnimateToLocation) {
    if (uiState.shouldAnimateToLocation && uiState.targetLocation != null) {
      actualViewModel.animateToUserLocation(mapViewportState)
      actualViewModel.onEvent(MapViewEvent.ClearLocationAnimation)
    }
  }

  LaunchedEffect(uiState.shouldAnimateToSelectedEvent) {
    if (uiState.shouldAnimateToSelectedEvent && uiState.selectedEventLocation != null) {
      actualViewModel.animateToSelectedEvent(mapViewportState)
      actualViewModel.onEvent(MapViewEvent.ClearEventSelectionAnimation)
    }
  }

  LaunchedEffect(targetEventUid, uiState.events) {
    if (targetEventUid != null && uiState.events.isNotEmpty()) {
      actualViewModel.onEvent(MapViewEvent.SelectEvent(targetEventUid))
    }
  }

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      actualViewModel.onEvent(MapViewEvent.ClearError)
    }
  }

  LaunchedEffect(uiState.hasLocationPermission, uiState.isEventsView) {
    if (uiState.hasLocationPermission && !uiState.isEventsView) {
      while (true) {
        actualViewModel.shareCurrentLocation()
        kotlinx.coroutines.delay(LocationSharingConfig.UPDATE_INTERVAL_MS)
      }
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
          friendLocations = uiState.friendLocations,
          selectedEvent = uiState.selectedEvent,
          selectedEventLocation = uiState.selectedEventLocation,
          onToggleView = { actualViewModel.onEvent(MapViewEvent.ToggleView) },
          onLocateUser = { actualViewModel.onEvent(MapViewEvent.LocateUser) },
          onEventSelected = { eventUid ->
            actualViewModel.onEvent(MapViewEvent.SelectEvent(eventUid))
          },
          userRepository = actualViewModel.getUserRepository(),
          modifier =
              Modifier.fillMaxSize()
                  .padding(
                      start = Padding.CONTENT,
                      end = Padding.CONTENT,
                      bottom = Padding.CONTENT,
                      top = Padding.VERTICAL_SPACING)
                  .semantics { testTag = C.Tag.map_container })
    }

    TopSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
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
internal fun SearchBar(
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

/** Map container with event/friend markers and user location tracking. */
@Composable
internal fun MapContainer(
    mapViewportState: MapViewportState,
    hasLocationPermission: Boolean,
    isEventsView: Boolean,
    events: List<Event>,
    friendLocations: Map<String, FriendLocation>,
    selectedEvent: Event?,
    selectedEventLocation: Point?,
    onToggleView: () -> Unit,
    onLocateUser: () -> Unit,
    onEventSelected: (String?) -> Unit,
    userRepository: com.github.se.studentconnect.model.user.UserRepository,
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val previousEventsView = remember { mutableStateOf<Boolean?>(null) }
  val previousEvents = remember { mutableStateOf<List<Event>?>(null) }
  val previousFriendLocations = remember { mutableStateOf<Map<String, FriendLocation>?>(null) }
  val friendLayersInitialized = remember { mutableStateOf(false) }
  val isAnimatingToEvent = remember { mutableStateOf(false) }

  LaunchedEffect(selectedEvent) {
    if (selectedEvent != null) {
      isAnimatingToEvent.value = true
      kotlinx.coroutines.delay(1000)
      isAnimatingToEvent.value = false
    }
  }

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

              mapView.mapboxMap.addOnMapClickListener { point ->
                val screenCoordinate = mapView.mapboxMap.pixelForCoordinate(point)
                mapView.mapboxMap.queryRenderedFeatures(
                    com.mapbox.maps.RenderedQueryGeometry(screenCoordinate),
                    com.mapbox.maps.RenderedQueryOptions(
                        listOf(EventMarkerConfig.LAYER_ID), null)) { expected ->
                      expected.value?.let { features ->
                        if (features.isNotEmpty()) {
                          val eventUid =
                              features[0]
                                  .queriedFeature
                                  .feature
                                  .getStringProperty(EventMarkerConfig.PROP_UID)
                          onEventSelected(eventUid)
                        } else {
                          onEventSelected(null)
                        }
                      } ?: onEventSelected(null)
                    }
                true
              }
            }

            MapEffect(selectedEvent, selectedEventLocation) { mapView ->
              if (selectedEvent != null && selectedEventLocation != null) {
                mapView.mapboxMap.subscribeCameraChanged { _ ->
                  if (!isAnimatingToEvent.value) {
                    val currentCenter = mapView.mapboxMap.cameraState.center
                    val latDiff = currentCenter.latitude() - selectedEventLocation.latitude()
                    val lonDiff = currentCenter.longitude() - selectedEventLocation.longitude()
                    val distance = kotlin.math.sqrt(latDiff * latDiff + lonDiff * lonDiff)
                    if (distance > 0.0005) {
                      onEventSelected(null)
                    }
                  }
                }
              }
            }

            MapEffect(isEventsView, events) { mapView ->
              val hasChanged =
                  previousEventsView.value != isEventsView || previousEvents.value != events
              if (hasChanged) {
                previousEventsView.value = isEventsView
                previousEvents.value = events
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

            MapEffect(!isEventsView, friendLocations) { mapView ->
              val locationsChanged = previousFriendLocations.value != friendLocations

              android.util.Log.d(
                  "MapContainer",
                  "Friend map effect - isEventsView: $isEventsView, layersInitialized: ${friendLayersInitialized.value}, locationsChanged: $locationsChanged, friends: ${friendLocations.size}")

              mapView.mapboxMap.getStyle { style ->
                if (!isEventsView) {
                  // We're in friends view
                  if (!friendLayersInitialized.value) {
                    // First time in friends view - initialize layers
                    android.util.Log.d("MapContainer", "Initializing friend marker layers")
                    FriendMarkers.removeExistingFriendLayers(style)
                    val features = FriendMarkers.createFriendFeatures(friendLocations)
                    FriendMarkers.addFriendSource(style, features)
                    FriendMarkers.addFriendMarkerLayer(style)
                    // Preload profile images asynchronously
                    FriendMarkers.preloadFriendData(context, style, friendLocations, userRepository)
                    friendLayersInitialized.value = true
                  } else if (locationsChanged) {
                    // Still in friends view, just update the data (no layer recreation)
                    android.util.Log.d(
                        "MapContainer",
                        "Updating friend locations (${friendLocations.size} friends)")
                    val features = FriendMarkers.createFriendFeatures(friendLocations)
                    FriendMarkers.updateFriendSource(style, features)
                    // Preload any new friends
                    FriendMarkers.preloadFriendData(context, style, friendLocations, userRepository)
                  }
                } else if (friendLayersInitialized.value) {
                  // Just switched to events view - clean up friend layers
                  android.util.Log.d("MapContainer", "Removing friend marker layers")
                  FriendMarkers.removeExistingFriendLayers(style)
                  friendLayersInitialized.value = false
                }
              }

              // Update tracking state AFTER processing to ensure proper change detection
              previousFriendLocations.value = friendLocations
            }
          }
    }

    selectedEvent?.let { event ->
      EventInfoCard(
          event = event,
          onClose = { onEventSelected(null) },
          modifier = Modifier.align(Alignment.TopCenter).padding(Padding.CONTENT))
    }

    MapActionButtons(
        hasLocationPermission = hasLocationPermission,
        isEventsView = isEventsView,
        onLocateUser = onLocateUser,
        onToggleView = onToggleView)
  }
}

/** Map action buttons for location and view toggling. */
@Composable
internal fun BoxScope.MapActionButtons(
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

/** Event info card with callout pointer. */
@Composable
internal fun EventInfoCard(event: Event, onClose: () -> Unit, modifier: Modifier = Modifier) {
  Column(
      modifier = modifier.fillMaxWidth(0.85f).semantics { testTag = C.Tag.map_event_info_card },
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Main card content
        Card(
            shape = RoundedCornerShape(Corner.RADIUS),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
              Column(modifier = Modifier.padding(Padding.CONTENT)) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = event.title,
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.weight(1f))
                      IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                      }
                    }

                Spacer(modifier = Modifier.height(Padding.VERTICAL_SPACING))

                if (event is Event.Public) {
                  Text(
                      text = event.subtitle,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.height(Padding.VERTICAL_SPACING))
                }

                event.location?.let { location ->
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Size.ICON))
                    Spacer(modifier = Modifier.width(Padding.VERTICAL_SPACING))
                    Text(
                        text = location.name ?: "Unknown location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                  }
                  Spacer(modifier = Modifier.height(Padding.VERTICAL_SPACING))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.AccessTime,
                      contentDescription = "Time",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(Size.ICON))
                  Spacer(modifier = Modifier.width(Padding.VERTICAL_SPACING))
                  Text(
                      text = formatTimestamp(event.start),
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface)
                }

                event.maxCapacity?.let { capacity ->
                  Spacer(modifier = Modifier.height(Padding.VERTICAL_SPACING))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Capacity",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Size.ICON))
                    Spacer(modifier = Modifier.width(Padding.VERTICAL_SPACING))
                    Text(
                        text = "Max: $capacity people",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                  }
                }
              }
            }

        Box(
            modifier =
                Modifier.size(16.dp, 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape =
                            GenericShape { size, _ ->
                              moveTo(0f, 0f)
                              lineTo(size.width / 2f, size.height)
                              lineTo(size.width, 0f)
                              close()
                            }))
      }
}

internal fun formatTimestamp(timestamp: Timestamp): String {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
  return dateFormat.format(timestamp.toDate())
}

internal fun isInAndroidTest(): Boolean {
  return try {
    // Check if we're actually running in a test, not just if test classes exist
    val testClass = Class.forName("androidx.test.platform.app.InstrumentationRegistry")
    val method = testClass.getMethod("getInstrumentation")
    method.invoke(null) != null
  } catch (e: Exception) {
    false
  }
}

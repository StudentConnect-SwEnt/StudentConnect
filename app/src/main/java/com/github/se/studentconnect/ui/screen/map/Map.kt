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
import com.github.se.studentconnect.repository.LocationRepositoryImpl
import com.github.se.studentconnect.repository.RequestLocationPermission
import com.github.se.studentconnect.resources.C
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
      // Only animate to coordinates if we're not also selecting an event
      // (event selection will handle the animation)
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

  // Automatically select event when targetEventUid is provided
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

  // Share location periodically when in friends view with permission
  LaunchedEffect(uiState.hasLocationPermission, uiState.isEventsView) {
    if (uiState.hasLocationPermission && !uiState.isEventsView) {
      while (true) {
        actualViewModel.shareCurrentLocation()
        kotlinx.coroutines.delay(LocationSharingConfig.UPDATE_INTERVAL_MS)
      }
    }
  }

  // Stop sharing location when leaving the screen
  androidx.compose.runtime.DisposableEffect(Unit) {
    onDispose { actualViewModel.stopSharingLocation() }
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
 * Container for the Mapbox map with event markers, friend markers, and action buttons.
 *
 * Handles:
 * - Map rendering with Mapbox SDK
 * - Event marker display with clustering
 * - Friend location marker display (real-time)
 * - User location puck
 * - Dynamic layer management based on view state
 *
 * @param mapViewportState Viewport state for camera control
 * @param hasLocationPermission Whether location permission is granted
 * @param isEventsView Whether currently in events view (vs friends view)
 * @param events List of events to display as markers
 * @param friendLocations Map of friend locations to display as markers
 * @param selectedEvent Currently selected event to display info for
 * @param selectedEventLocation Geographic location of the selected event
 * @param onToggleView Callback to toggle between views
 * @param onLocateUser Callback to center map on user location
 * @param onEventSelected Callback when an event marker is clicked
 * @param modifier Modifier to be applied to the container
 */
@Composable
private fun MapContainer(
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
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  // Track previous state to avoid unnecessary updates
  val previousEventsView = remember { mutableStateOf<Boolean?>(null) }
  val previousEvents = remember { mutableStateOf<List<Event>?>(null) }
  val previousFriendLocations = remember { mutableStateOf<Map<String, FriendLocation>?>(null) }
  // Track when an event is initially selected to avoid dismissing during animation
  val isAnimatingToEvent = remember { mutableStateOf(false) }

  // Set animation flag when event is selected and reset after animation completes
  LaunchedEffect(selectedEvent) {
    if (selectedEvent != null) {
      isAnimatingToEvent.value = true
      kotlinx.coroutines.delay(1000) // Wait for animation to complete
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

              // Add click listener for event markers
              mapView.mapboxMap.addOnMapClickListener { point ->
                val screenCoordinate = mapView.mapboxMap.pixelForCoordinate(point)

                // Query rendered features synchronously using the gesture plugin
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
                          // Clicked on map, not on a marker - clear selection
                          onEventSelected(null)
                        }
                      }
                          ?: run {
                            // Error querying features - clear selection
                            onEventSelected(null)
                          }
                    }
                true
              }
            }

            // Add camera change listener to dismiss info card when user scrolls away
            MapEffect(selectedEvent, selectedEventLocation) { mapView ->
              if (selectedEvent != null && selectedEventLocation != null) {
                mapView.mapboxMap.subscribeCameraChanged { _ ->
                  // Skip camera change checks during initial animation
                  if (!isAnimatingToEvent.value) {
                    // When camera moves and we have a selected event, check if we've moved far
                    // enough
                    val currentCenter = mapView.mapboxMap.cameraState.center

                    // Calculate distance between current center and selected event location
                    val latDiff = currentCenter.latitude() - selectedEventLocation.latitude()
                    val lonDiff = currentCenter.longitude() - selectedEventLocation.longitude()
                    val distance = kotlin.math.sqrt(latDiff * latDiff + lonDiff * lonDiff)

                    // Dismiss if user scrolled more than a threshold distance
                    val dismissThreshold = 0.0005
                    if (distance > dismissThreshold) {
                      onEventSelected(null)
                    }
                  }
                }
              }
            }

            // Add event markers with clustering - only in events view
            // Check if state actually changed before updating
            MapEffect(isEventsView, events) { mapView ->
              val hasChanged =
                  previousEventsView.value != isEventsView || previousEvents.value != events

              if (hasChanged) {
                android.util.Log.d(
                    "MapContainer",
                    "Map state changed - isEventsView: $isEventsView, events count: ${events.size}")
                previousEventsView.value = isEventsView
                previousEvents.value = events

                mapView.mapboxMap.getStyle { style ->
                  EventMarkers.removeExistingEventLayers(style)

                  if (isEventsView && events.isNotEmpty()) {
                    android.util.Log.d("MapContainer", "Adding event markers to map")
                    EventMarkers.addEventMarkerIcon(context, style)
                    val features = EventMarkers.createEventFeatures(events)
                    if (features.isEmpty()) {
                      android.util.Log.w(
                          "MapContainer",
                          "No features created from ${events.size} events - they may be missing location data")
                    }
                    EventMarkers.addEventSource(style, features)
                    EventMarkers.addClusterLayers(style)
                    EventMarkers.addIndividualMarkerLayer(style)
                  } else {
                    android.util.Log.d(
                        "MapContainer",
                        "Not showing markers - isEventsView: $isEventsView, events.isEmpty(): ${events.isEmpty()}")
                  }
                }
              }
            }

            // Add friend markers - only in friends view
            // Initialize layers once when entering friends view, then only update data
            MapEffect(!isEventsView, friendLocations) { mapView ->
              val viewChanged = previousEventsView.value != isEventsView
              val locationsChanged = previousFriendLocations.value != friendLocations

              android.util.Log.d(
                  "MapContainer",
                  "Friend map effect - viewChanged: $viewChanged, locationsChanged: $locationsChanged, friends: ${friendLocations.size}")
              previousFriendLocations.value = friendLocations

              mapView.mapboxMap.getStyle { style ->
                if (!isEventsView) {
                  // We're in friends view
                  if (viewChanged) {
                    // Just switched to friends view - initialize layers once
                    android.util.Log.d("MapContainer", "Initializing friend marker layers")
                    FriendMarkers.removeExistingFriendLayers(style)
                    FriendMarkers.addFriendMarkerIcon(context, style)
                    val features = FriendMarkers.createFriendFeatures(friendLocations)
                    FriendMarkers.addFriendSource(style, features)
                    FriendMarkers.addFriendMarkerLayer(style)
                  } else if (locationsChanged) {
                    // Still in friends view, just update the data (no layer recreation)
                    android.util.Log.d(
                        "MapContainer",
                        "Updating friend locations (${friendLocations.size} friends)")
                    val features = FriendMarkers.createFriendFeatures(friendLocations)
                    FriendMarkers.updateFriendSource(style, features)
                  }
                } else if (viewChanged) {
                  // Just switched to events view - clean up friend layers
                  android.util.Log.d("MapContainer", "Removing friend marker layers")
                  FriendMarkers.removeExistingFriendLayers(style)
                }
              }
            }
          }
    }

    // Display event info card when an event is selected
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
 * Event info card that displays basic event information when a marker is clicked. Styled like a
 * callout bubble with a pointer at the bottom to indicate connection to the marker.
 *
 * @param event The event to display information for
 * @param onClose Callback when the close button is clicked
 * @param modifier Modifier to be applied to the card
 */
@Composable
private fun EventInfoCard(event: Event, onClose: () -> Unit, modifier: Modifier = Modifier) {
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

                // Event subtitle (for public events)
                if (event is Event.Public) {
                  Text(
                      text = event.subtitle,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.height(Padding.VERTICAL_SPACING))
                }

                // Location
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

                // Start time
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

                // Max capacity
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

        // Pointer/arrow pointing down to the marker
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

/**
 * Formats a Firebase Timestamp to a readable date/time string.
 *
 * @param timestamp The timestamp to format
 * @return Formatted string like "Jan 15, 2025 2:30 PM"
 */
private fun formatTimestamp(timestamp: Timestamp): String {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
  return dateFormat.format(timestamp.toDate())
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

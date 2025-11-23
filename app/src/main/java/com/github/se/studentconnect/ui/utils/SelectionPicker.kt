package com.github.se.studentconnect.ui.screen.filters

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.LocationRepositoryImpl
import com.github.se.studentconnect.ui.screen.map.EventMarkers
import com.github.se.studentconnect.ui.screen.map.MapConfiguration
import com.github.se.studentconnect.ui.screen.map.MapViewEvent
import com.github.se.studentconnect.ui.screen.map.MapViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import kotlin.math.*
import kotlinx.coroutines.launch

/**
 * A lightweight composable showing a map that centers on [selectedPoint] and invokes [onMapClick]
 * when the user taps the map.
 *
 * @param modifier Modifier applied to the map container.
 * @param selectedPoint The point to center/highlight on the map; may be null.
 * @param mapViewportState Optional external viewport state to control camera.
 * @param onMapClick Callback invoked with the geographic point where the user clicked.
 */
@Composable
fun LocationPickerMapComponent(
    modifier: Modifier = Modifier,
    selectedPoint: Point?,
    mapViewportState: MapViewportState? = null,
    onMapClick: (Point) -> Unit
) {
  val viewportState =
      mapViewportState
          ?: rememberMapViewportState {
            setCameraOptions {
              center(
                  selectedPoint
                      ?: Point.fromLngLat(
                          MapConfiguration.Coordinates.EPFL_LONGITUDE,
                          MapConfiguration.Coordinates.EPFL_LATITUDE))
              zoom(MapConfiguration.Zoom.DEFAULT)
            }
          }

  MapboxMap(
      modifier = modifier,
      mapViewportState = viewportState,
      scaleBar = {},
      logo = {},
      attribution = {},
      compass = {},
      onMapClickListener =
          OnMapClickListener { point ->
            onMapClick(point)
            true
          }) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Dialog that allows the user to pick a location on a map and select a radius. The dialog shows an
 * interactive Mapbox map (or a test map when [useTestMap] is true), a radius slider and action
 * buttons. When the user confirms, [onLocationSelected] is invoked with the selected [Location] and
 * radius in kilometers.
 *
 * @param initialLocation Optional initial selected location shown when the dialog opens.
 * @param initialRadius Initial radius in kilometers.
 * @param onDismiss Called when the dialog should be dismissed.
 * @param onLocationSelected Called when the user confirms the selection.
 * @param useTestMap If true, uses a lightweight test map composable instead of MapboxMap.
 */
fun LocationPickerDialog(
    initialLocation: Location?,
    initialRadius: Float,
    onDismiss: () -> Unit,
    onLocationSelected: (Location, Float) -> Unit,
    useTestMap: Boolean = false
) {
  val context = LocalContext.current

  val mapViewModelFactory =
      remember(context) {
        object : ViewModelProvider.Factory {
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
              @Suppress("UNCHECKED_CAST") return MapViewModel(LocationRepositoryImpl(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
          }
        }
      }

  val mapViewModel: MapViewModel = viewModel(factory = mapViewModelFactory)
  val uiState by mapViewModel.uiState.collectAsState()
  val coroutineScope = rememberCoroutineScope()

  var currentRadius by remember { mutableFloatStateOf(initialRadius) }
  var selectedPoint by remember {
    mutableStateOf(initialLocation?.let { Point.fromLngLat(it.longitude, it.latitude) })
  }
  var selectedLocation by remember { mutableStateOf(initialLocation) }

  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      center(
          initialLocation?.let { Point.fromLngLat(it.longitude, it.latitude) }
              ?: Point.fromLngLat(
                  MapConfiguration.Coordinates.EPFL_LONGITUDE,
                  MapConfiguration.Coordinates.EPFL_LATITUDE))
      zoom(MapConfiguration.Zoom.DEFAULT)
    }
  }

  // Localized strings used in the dialog
  val myPositionLabel = stringResource(R.string.location_my_position)
  val selectedLocationLabel = stringResource(R.string.location_selected_location)
  val noLocationSelectedLabel = stringResource(R.string.text_no_location_selected)
  val selectedText = stringResource(R.string.text_selected)
  val cancelLabel = stringResource(R.string.button_cancel)
  val applyLabel = stringResource(R.string.button_apply)

  LaunchedEffect(uiState.targetLocation) {
    uiState.targetLocation?.let { point ->
      selectedPoint = point
      selectedLocation =
          Location(
              latitude = point.latitude(), longitude = point.longitude(), name = myPositionLabel)
      if (!useTestMap) {
        mapViewportState.setCameraOptions {
          center(point)
          zoom(MapConfiguration.Zoom.TARGET)
        }
      }
    }
  }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center) {
              Card(
                  modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f),
                  shape = RoundedCornerShape(16.dp),
                  elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                      Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        if (useTestMap) {
                          LocationPickerMapComponent(
                              modifier = Modifier.fillMaxSize().testTag("location_picker_map"),
                              selectedPoint = selectedPoint,
                              onMapClick = { point ->
                                selectedPoint = point
                                selectedLocation =
                                    Location(
                                        latitude = point.latitude(),
                                        longitude = point.longitude(),
                                        name = selectedLocationLabel)
                                mapViewModel.onEvent(
                                    MapViewEvent.SetTargetLocation(
                                        point.latitude(),
                                        point.longitude(),
                                        MapConfiguration.Zoom.TARGET))
                              })
                        } else {
                          MapboxMap(
                              modifier = Modifier.fillMaxSize(),
                              mapViewportState = mapViewportState,
                              onMapClickListener =
                                  OnMapClickListener { point ->
                                    selectedPoint = point
                                    selectedLocation =
                                        Location(
                                            latitude = point.latitude(),
                                            longitude = point.longitude(),
                                            name = selectedLocationLabel)
                                    mapViewModel.onEvent(
                                        MapViewEvent.SetTargetLocation(
                                            point.latitude(),
                                            point.longitude(),
                                            MapConfiguration.Zoom.TARGET))
                                    true
                                  }) {
                                key(selectedPoint, currentRadius) {
                                  if (selectedPoint != null) {
                                    val circlePoints =
                                        createCirclePoints(
                                            selectedPoint!!, currentRadius.toDouble(), 64)

                                    // Filled polygon (semi-transparent)
                                    val colorFill =
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    PolygonAnnotation(points = listOf(circlePoints)) {
                                      fillColor = colorFill
                                      fillOutlineColor = colorFill
                                    }

                                    // Outline of the circle
                                    PolylineAnnotation(points = circlePoints) {
                                      lineColor = Color.Black
                                      lineWidth = 1.5
                                    }
                                  }
                                }

                                // Add a MapEffect to draw the marker
                                MapEffect(selectedPoint) { mapView ->
                                  selectedPoint?.let { point ->
                                    mapView.mapboxMap.getStyle { style ->
                                      EventMarkers.addEventMarkerIcon(context, style)

                                      EventMarkers.addClickMarker(
                                          style = style,
                                          point = point,
                                          title = selectedLocation?.name ?: selectedText)
                                    }
                                  }
                                }
                              }
                        }
                      }

                      Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            selectedLocation?.name ?: noLocationSelectedLabel,
                            style = MaterialTheme.typography.titleMedium)
                        selectedLocation?.let {
                          Text(
                              "(${String.format("%.4f", it.latitude)}, ${
                                    String.format(
                                        "%.4f",
                                        it.longitude
                                    )
                                })",
                              style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            stringResource(R.string.filter_radius, currentRadius.toInt()),
                            style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = currentRadius,
                            onValueChange = { currentRadius = it },
                            valueRange = 1f..100f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedLocation != null)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End) {
                              TextButton(onClick = onDismiss) { Text(cancelLabel) }
                              Spacer(modifier = Modifier.width(8.dp))
                              Button(
                                  onClick = {
                                    selectedLocation?.let { loc ->
                                      coroutineScope.launch {
                                        try {
                                          onLocationSelected(loc, currentRadius)
                                          onDismiss()
                                        } catch (e: Exception) {
                                          Log.e(
                                              "LocationPickerDialog",
                                              "onLocationSelected failed",
                                              e)
                                        }
                                      }
                                    }
                                  },
                                  enabled = selectedLocation != null,
                                  contentPadding = PaddingValues(horizontal = 16.dp)) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = applyLabel,
                                        modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(applyLabel)
                                  }
                            }
                      }
                    }
                  }
            }
      }
}

private const val EARTH_RADIUS_KM = 6371.0

/**
 * Computes the destination point given a [center] point, a bearing in degrees and a distance in
 * kilometers using the haversine-based great-circle formula.
 *
 * @param center Starting point.
 * @param bearing Bearing in degrees (0 = north, 90 = east).
 * @param distanceKm Distance from the center in kilometers.
 * @return A new [Point] representing the destination coordinates.
 */
fun getDestinationPoint(center: Point, bearing: Double, distanceKm: Double): Point {
  val brng = Math.toRadians(bearing)
  val lat1 = Math.toRadians(center.latitude())
  val lon1 = Math.toRadians(center.longitude())
  val d = distanceKm / EARTH_RADIUS_KM // angular distance

  val lat2 = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(brng))
  val lon2 = lon1 + atan2(sin(brng) * sin(d) * cos(lat1), cos(d) - sin(lat1) * sin(lat2))

  return Point.fromLngLat(Math.toDegrees(lon2), Math.toDegrees(lat2))
}

/**
 * Creates a list of points describing a circle around [center] with radius [radiusKm]. The
 * resulting list contains [steps] points evenly distributed around the circle and repeats the first
 * point at the end to close the polygon.
 *
 * @param center Center of the circle.
 * @param radiusKm Radius in kilometers.
 * @param steps Number of points to approximate the circle (default 64).
 * @return List of [Point] approximating the circle polygon.
 */
fun createCirclePoints(center: Point, radiusKm: Double, steps: Int = 64): List<Point> {
  return (0..steps).map { i ->
    val bearing = (360.0 / steps) * i
    getDestinationPoint(center, bearing, radiusKm)
  }
}

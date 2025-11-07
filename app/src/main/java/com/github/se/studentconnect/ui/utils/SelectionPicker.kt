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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.studentconnect.model.location.Location
import com.github.se.studentconnect.repository.LocationRepositoryImpl
import com.github.se.studentconnect.ui.screen.map.MapConfiguration
import com.github.se.studentconnect.ui.screen.map.MapViewEvent
import com.github.se.studentconnect.ui.screen.map.MapViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import kotlinx.coroutines.launch

/**
 * Composable for the map component in LocationPickerDialog. This can be replaced with a test
 * version during testing.
 */
@Composable
fun LocationPickerMapComponent(
    modifier: Modifier = Modifier,
    selectedPoint: Point?,
    mapViewportState: com.mapbox.maps.extension.compose.animation.viewport.MapViewportState? = null,
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
          }) {
        if (selectedPoint != null) {
          PointAnnotation(point = selectedPoint)
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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

  // use a remembered coroutine scope to call the selection callback without blocking UI
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

  LaunchedEffect(uiState.targetLocation) {
    uiState.targetLocation?.let { point ->
      selectedPoint = point
      selectedLocation =
          Location(latitude = point.latitude(), longitude = point.longitude(), name = "My position")
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
                          // Use component provided above or test equivalent if available
                          LocationPickerMapComponent(
                              modifier = Modifier.fillMaxSize().testTag("location_picker_map"),
                              selectedPoint = selectedPoint,
                              onMapClick = { point ->
                                selectedPoint = point
                                selectedLocation =
                                    Location(
                                        latitude = point.latitude(),
                                        longitude = point.longitude(),
                                        name = "Selected Location")
                                mapViewModel.onEvent(
                                    MapViewEvent.SetTargetLocation(
                                        point.latitude(),
                                        point.longitude(),
                                        MapConfiguration.Zoom.TARGET))
                              })
                        } else {
                          // Use real Mapbox map
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
                                            name = "")
                                    mapViewModel.onEvent(
                                        MapViewEvent.SetTargetLocation(
                                            point.latitude(),
                                            point.longitude(),
                                            MapConfiguration.Zoom.TARGET))
                                    true
                                  }) {
                                if (selectedPoint != null) {
                                  PointAnnotation(point = selectedPoint!!)
                                }
                              }
                        }
                      }

                      Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            selectedLocation?.name ?: "No location selected",
                            style = MaterialTheme.typography.titleMedium)
                        selectedLocation?.let {
                          Text(
                              "(${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)})",
                              style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Radius : ${currentRadius.toInt()} km",
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
                              TextButton(onClick = onDismiss) { Text("Cancel") }
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
                                        contentDescription = "Apply",
                                        modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Apply")
                                  }
                            }
                      }
                    }
                  }
            }
      }
}

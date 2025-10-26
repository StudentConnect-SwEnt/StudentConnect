package com.github.se.studentconnect.ui.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag as semanticTestTag
import com.github.se.studentconnect.resources.C
import com.mapbox.geojson.Point

@Composable
fun TestMapboxMap(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
  Box(
      modifier =
          modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).semantics {
            semanticTestTag = C.Tag.map_screen
          },
      contentAlignment = Alignment.Center) {
        Text(
            text = "Test Map View",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
      }
}

/**
 * Test version of the location picker map that simulates map clicks without using real Mapbox
 */
@Composable
fun TestLocationPickerMap(
    modifier: Modifier = Modifier,
    selectedPoint: Point?,
    onMapClick: (Point) -> Unit,
    content: @Composable () -> Unit = {}
) {
  Box(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .testTag("location_picker_map")
              .clickable {
                // Simulate a click at a default location (EPFL coordinates)
                onMapClick(Point.fromLngLat(6.5668, 46.5191))
              },
      contentAlignment = Alignment.Center) {
        Text(
            text = "Test Location Picker Map\n${selectedPoint?.let { "Selected: ${String.format("%.4f", it.latitude())}, ${String.format("%.4f", it.longitude())}" } ?: "Tap to select"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("map_status_text"))
        content()
      }
}

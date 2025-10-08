package com.github.se.studentconnect.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.github.se.studentconnect.resources.C
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen() {
  var searchText by remember { mutableStateOf("") }
  
  Column(
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
  ) {
    // App Bar
    TopAppBar(
        title = {
          Text(
              text = "Map",
              style = MaterialTheme.typography.headlineMedium
          )
        }
    )
    
    // Search Bar
    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Search locations...") },
        leadingIcon = {
          Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        )
    )
    
    // Map Container with rounded corners and padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
      MapboxMap(
          modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.map_screen },
          mapViewportState = rememberMapViewportState {
            setCameraOptions(
                CameraOptions.Builder()
                    .zoom(12.0)
                    .center(Point.fromLngLat(6.6283, 46.5089))
                    .pitch(0.0)
                    .bearing(0.0)
                    .build())
          },
          scaleBar = { },
          logo = { },
          attribution = { })
      
      // Floating Action Button for group view
      FloatingActionButton(
          onClick = { /* TODO: Handle group view */ },
          modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(16.dp)
              .size(56.dp)
              .shadow(8.dp, CircleShape),
          shape = CircleShape,
          containerColor = MaterialTheme.colorScheme.primary
      ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Group View",
            tint = MaterialTheme.colorScheme.onPrimary
        )
      }
    }
  }
}
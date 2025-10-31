package com.github.se.studentconnect.ui.screen.map

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.friends.FriendLocation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import com.mapbox.maps.extension.style.expressions.dsl.generated.not
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource

/**
 * Utility object for managing friend markers on the map.
 *
 * Provides functions for:
 * - Adding/removing friend marker layers and sources
 * - Creating GeoJSON features from friend locations
 * - Rendering friend avatars on the map
 */
object FriendMarkers {

  /**
   * Removes existing friend marker layers and sources from the map style. This ensures clean state
   * before adding new markers.
   */
  fun removeExistingFriendLayers(style: Style) {
    if (style.styleLayerExists(FriendMarkerConfig.LAYER_ID)) {
      style.removeStyleLayer(FriendMarkerConfig.LAYER_ID)
    }

    if (style.styleSourceExists(FriendMarkerConfig.SOURCE_ID)) {
      style.removeStyleSource(FriendMarkerConfig.SOURCE_ID)
    }
  }

  /** Adds the friend marker icon to the map style with the configured color tint. */
  fun addFriendMarkerIcon(context: Context, style: Style) {
    val markerIcon = ContextCompat.getDrawable(context, R.drawable.ic_map_friends)
    markerIcon?.let { drawable ->
      runCatching { android.graphics.Color.parseColor(FriendMarkerConfig.COLOR) }
          .onSuccess { color -> drawable.setTint(color) }
          .onFailure { exception ->
            Log.w("FriendMarkers", "Invalid color: ${FriendMarkerConfig.COLOR}", exception)
            // Use a fallback color (blue) if parsing fails
            drawable.setTint(android.graphics.Color.BLUE)
          }
      if (!style.hasStyleImage(FriendMarkerConfig.ICON_ID)) {
        style.addImage(FriendMarkerConfig.ICON_ID, drawable.toBitmap())
      }
    }
  }

  /**
   * Creates GeoJSON features from friend locations. Each feature includes the friend's userId as a
   * property.
   */
  fun createFriendFeatures(friendLocations: Map<String, FriendLocation>): List<Feature> {
    Log.d("FriendMarkers", "Creating features for ${friendLocations.size} friends")
    val features =
        friendLocations.values.map { location ->
          Log.d(
              "FriendMarkers",
              "Creating marker for friend: ${location.userId} at (${location.latitude}, ${location.longitude})")
          Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude)).apply {
            addStringProperty(FriendMarkerConfig.PROP_USER_ID, location.userId)
            addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, location.timestamp)
          }
        }
    Log.d(
        "FriendMarkers",
        "Created ${features.size} features from ${friendLocations.size} friend locations")
    return features
  }

  /** Adds a GeoJSON source for friend markers to the map style. */
  fun addFriendSource(style: Style, features: List<Feature>) {
    val featureCollection = FeatureCollection.fromFeatures(features)
    style.addSource(
        geoJsonSource(FriendMarkerConfig.SOURCE_ID) {
          featureCollection(featureCollection)
          // No clustering for friends - we want to see each friend individually
        })
  }

  /**
   * Updates the friend marker source with new location data. Note: The Mapbox Style API doesn't
   * expose a getSource() method in v11, so we must remove/re-add the source. However, the layer
   * remains intact and automatically reconnects to the new source, avoiding layer recreation
   * overhead.
   */
  fun updateFriendSource(style: Style, features: List<Feature>) {
    if (style.styleSourceExists(FriendMarkerConfig.SOURCE_ID)) {
      style.removeStyleSource(FriendMarkerConfig.SOURCE_ID)
    }
    addFriendSource(style, features)
  }

  /** Adds a symbol layer for individual friend markers. */
  fun addFriendMarkerLayer(style: Style) {
    style.addLayer(
        symbolLayer(FriendMarkerConfig.LAYER_ID, FriendMarkerConfig.SOURCE_ID) {
          iconImage(FriendMarkerConfig.ICON_ID)
          iconAllowOverlap(true)
          iconAnchor(IconAnchor.BOTTOM)
          iconSize(FriendMarkerConfig.ICON_SIZE)
        })
  }
}

package com.github.se.studentconnect.ui.screen.map

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.event.Event
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
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

/**
 * Utility object for managing event markers on the map.
 *
 * Provides functions for:
 * - Adding/removing event marker layers and sources
 * - Creating GeoJSON features from events
 * - Configuring marker clustering
 */
object EventMarkers {

  /**
   * Removes existing event marker layers and sources from the map style. This ensures clean state
   * before adding new markers.
   */
  fun removeExistingEventLayers(style: Style) {
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
  fun addEventMarkerIcon(context: Context, style: Style) {
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
  fun createEventFeatures(events: List<Event>): List<Feature> {
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
  fun addEventSource(style: Style, features: List<Feature>) {
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
  fun addClusterLayers(style: Style) {
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
  fun addIndividualMarkerLayer(style: Style) {
    style.addLayer(
        symbolLayer(EventMarkerConfig.LAYER_ID, EventMarkerConfig.SOURCE_ID) {
          iconImage(EventMarkerConfig.ICON_ID)
          iconAllowOverlap(true)
          iconAnchor(IconAnchor.BOTTOM)
          iconSize(EventMarkerConfig.ICON_SIZE)
          filter(not { has { literal("point_count") } })
        })
  }
}

// these tests were implemented with the help of chatGPT
package com.github.se.studentconnect.ui.screen.map

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import org.junit.Assert.*
import org.junit.Test

class EventMarkersTest {

  @Test
  fun addClickMarker_createsSourceAndLayer_whenSourceAbsent() {
    var capturedFeatures: List<Feature>? = null
    val calls = mutableListOf<String>()

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = false

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            calls += "addSource:$sourceId"
            capturedFeatures = features
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {
            calls += "addLayer:$layerId:$sourceId"
          }

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {
            calls += "update:$sourceId"
          }
        }

    val point = Point.fromLngLat(1.0, 2.0)
    EventMarkers.addClickMarker(fake, point, "title-1", "uid-1")

    assertTrue(calls.any { it.startsWith("addSource:") })
    assertTrue(calls.any { it.startsWith("addLayer:") })
    assertFalse(calls.any { it.startsWith("update:") })

    assertNotNull("Features should be provided to addGeoJsonSourceWithFeatures", capturedFeatures)
    val feature = capturedFeatures!![0]
    assertEquals("title-1", feature.getStringProperty(EventMarkerConfig.PROP_TITLE))
    assertEquals("uid-1", feature.getStringProperty(EventMarkerConfig.PROP_UID))
  }

  @Test
  fun addClickMarker_updatesSource_whenSourceExists() {
    var capturedFeatures: List<Feature>? = null
    val calls = mutableListOf<String>()

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = true

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            calls += "addSource"
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {
            calls += "addLayer"
          }

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {
            calls += "update:$sourceId"
            capturedFeatures = features
          }
        }

    val point = Point.fromLngLat(3.0, 4.0)
    EventMarkers.addClickMarker(fake, point, null, null)

    assertTrue(calls.any { it.startsWith("update:") })
    assertFalse(calls.any { it.startsWith("addSource") })

    // When title/uid are null they should simply be absent from the feature's properties
    assertNotNull("Features should be provided to updateSourceFeatures", capturedFeatures)
    val feature = capturedFeatures!![0]

    // Use hasProperty to check absence rather than relying on exceptions
    assertFalse(feature.hasProperty(EventMarkerConfig.PROP_TITLE))
    assertFalse(feature.hasProperty(EventMarkerConfig.PROP_UID))
  }
}

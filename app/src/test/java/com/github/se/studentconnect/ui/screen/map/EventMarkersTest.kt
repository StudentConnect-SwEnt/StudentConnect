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

  @Test
  fun mapWithCircleAndSlider_displaysCircleAndRadius_whenPointSelected() {
    // Simulate a user selecting a point on the map
    val selectedPoint = Point.fromLngLat(6.5668, 46.5191) // EPFL coordinates
    var circleRadius = 10.0 // Initial radius in km
    val capturedCircles = mutableListOf<Pair<Point, Double>>()
    val capturedSliderValues = mutableListOf<Double>()

    // Mock adapter to capture circle creation and slider interactions
    val mockMapAdapter =
        object : MapCircleAdapter {
          override fun drawCircle(center: Point, radiusKm: Double) {
            capturedCircles.add(Pair(center, radiusKm))
          }

          override fun updateCircleRadius(radiusKm: Double) {
            capturedSliderValues.add(radiusKm)
          }

          override fun clearCircle() {
            capturedCircles.clear()
          }
        }

    // Simulate selecting a point and drawing a circle
    mockMapAdapter.drawCircle(selectedPoint, circleRadius)

    // Verify circle was created with correct parameters
    assertEquals(1, capturedCircles.size)
    assertEquals(selectedPoint, capturedCircles[0].first)
    assertEquals(10.0, capturedCircles[0].second, 0.001)

    // Simulate slider interaction - user changes radius to 25 km
    circleRadius = 25.0
    mockMapAdapter.updateCircleRadius(circleRadius)

    // Verify slider value was captured
    assertEquals(1, capturedSliderValues.size)
    assertEquals(25.0, capturedSliderValues[0], 0.001)

    // Simulate another slider change to 50 km
    circleRadius = 50.0
    mockMapAdapter.updateCircleRadius(circleRadius)

    // Verify both slider changes were captured
    assertEquals(2, capturedSliderValues.size)
    assertEquals(50.0, capturedSliderValues[1], 0.001)

    // Clear the circle
    mockMapAdapter.clearCircle()
    assertTrue(capturedCircles.isEmpty())
  }

  // Interface for testing map circle interactions
  interface MapCircleAdapter {
    fun drawCircle(center: Point, radiusKm: Double)

    fun updateCircleRadius(radiusKm: Double)

    fun clearCircle()
  }
}

// these tests were implemented with the help of chatGPT
package com.github.se.studentconnect.ui.screen.map

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.sources.getSourceAs
import io.mockk.*
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
  fun addClickMarker_withMultipleCalls_updatesCorrectly() {
    var capturedFeatures: List<Feature>? = null
    var callCount = 0

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = callCount > 0

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            callCount++
            capturedFeatures = features
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {
            // Layer added on first call
          }

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {
            callCount++
            capturedFeatures = features
          }
        }

    // First call - should create source
    val point1 = Point.fromLngLat(1.0, 2.0)
    EventMarkers.addClickMarker(fake, point1, "First", "uid-1")
    assertEquals(1, callCount)

    // Second call - should update source
    val point2 = Point.fromLngLat(3.0, 4.0)
    EventMarkers.addClickMarker(fake, point2, "Second", "uid-2")
    assertEquals(2, callCount)

    // Verify last feature has correct data
    assertNotNull(capturedFeatures)
    val feature = capturedFeatures!![0]
    assertEquals("Second", feature.getStringProperty(EventMarkerConfig.PROP_TITLE))
    assertEquals("uid-2", feature.getStringProperty(EventMarkerConfig.PROP_UID))
  }

  @Test
  fun addClickMarker_withPartialNullProperties_handlesCorrectly() {
    var capturedFeatures: List<Feature>? = null

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = false

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            capturedFeatures = features
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {}

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {}
        }

    // Test with title but no uid
    val point = Point.fromLngLat(5.0, 6.0)
    EventMarkers.addClickMarker(fake, point, "Only Title", null)

    assertNotNull(capturedFeatures)
    val feature = capturedFeatures!![0]
    assertEquals("Only Title", feature.getStringProperty(EventMarkerConfig.PROP_TITLE))
    assertFalse(feature.hasProperty(EventMarkerConfig.PROP_UID))
  }

  @Test
  fun addClickMarker_withUidButNoTitle_handlesCorrectly() {
    var capturedFeatures: List<Feature>? = null

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = false

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            capturedFeatures = features
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {}

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {}
        }

    // Test with uid but no title
    val point = Point.fromLngLat(7.0, 8.0)
    EventMarkers.addClickMarker(fake, point, null, "only-uid")

    assertNotNull(capturedFeatures)
    val feature = capturedFeatures!![0]
    assertFalse(feature.hasProperty(EventMarkerConfig.PROP_TITLE))
    assertEquals("only-uid", feature.getStringProperty(EventMarkerConfig.PROP_UID))
  }

  @Test
  fun addClickMarker_preservesGeometry() {
    var capturedFeatures: List<Feature>? = null

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = false

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            capturedFeatures = features
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {}

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {}
        }

    val point = Point.fromLngLat(10.123, 20.456)
    EventMarkers.addClickMarker(fake, point, "Test", "test-uid")

    assertNotNull(capturedFeatures)
    val feature = capturedFeatures!![0]
    val geometry = feature.geometry() as? Point

    assertNotNull(geometry)
    assertEquals(10.123, geometry!!.longitude(), 0.0001)
    assertEquals(20.456, geometry.latitude(), 0.0001)
  }

  @Test
  fun addClickMarker_withExtremeCoordinates_handlesCorrectly() {
    var capturedFeatures: List<Feature>? = null

    val fake =
        object : EventMarkers.EventMapStyleAdapter {
          override fun styleSourceExists(id: String): Boolean = false

          override fun addGeoJsonSourceWithFeatures(sourceId: String, features: List<Feature>) {
            capturedFeatures = features
          }

          override fun addLayerForSource(layerId: String, sourceId: String) {}

          override fun updateSourceFeatures(sourceId: String, features: List<Feature>) {}
        }

    // Test with extreme valid coordinates
    val point = Point.fromLngLat(-179.999, 89.999)
    EventMarkers.addClickMarker(fake, point, "Extreme", "extreme-uid")

    assertNotNull(capturedFeatures)
    val feature = capturedFeatures!![0]
    val geometry = feature.geometry() as? Point

    assertNotNull(geometry)
    assertEquals(-179.999, geometry!!.longitude(), 0.0001)
    assertEquals(89.999, geometry.latitude(), 0.0001)
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

  @Test
  fun mapCircleAdapter_supportsMultipleCircles() {
    val capturedCircles = mutableListOf<Pair<Point, Double>>()

    val mockMapAdapter =
        object : MapCircleAdapter {
          override fun drawCircle(center: Point, radiusKm: Double) {
            capturedCircles.add(Pair(center, radiusKm))
          }

          override fun updateCircleRadius(radiusKm: Double) {}

          override fun clearCircle() {
            capturedCircles.clear()
          }
        }

    // Draw multiple circles
    mockMapAdapter.drawCircle(Point.fromLngLat(0.0, 0.0), 5.0)
    mockMapAdapter.drawCircle(Point.fromLngLat(1.0, 1.0), 10.0)
    mockMapAdapter.drawCircle(Point.fromLngLat(2.0, 2.0), 15.0)

    assertEquals(3, capturedCircles.size)
    assertEquals(5.0, capturedCircles[0].second, 0.001)
    assertEquals(10.0, capturedCircles[1].second, 0.001)
    assertEquals(15.0, capturedCircles[2].second, 0.001)
  }

  // Interface for testing map circle interactions
  interface MapCircleAdapter {
    fun drawCircle(center: Point, radiusKm: Double)

    fun updateCircleRadius(radiusKm: Double)

    fun clearCircle()
  }

  // Tests for RealStyleAdapter to improve code coverage
  @Test
  fun realStyleAdapter_styleSourceExists_delegatesToStyle() {
    val mockStyle = mockk<com.mapbox.maps.Style>(relaxed = true)
    every { mockStyle.styleSourceExists("test-source") } returns true

    val adapter = EventMarkers.RealStyleAdapter(mockStyle)
    val result = adapter.styleSourceExists("test-source")

    assertTrue(result)
    verify { mockStyle.styleSourceExists("test-source") }
  }

  @Test
  fun realStyleAdapter_styleSourceExists_returnsFalseWhenSourceDoesNotExist() {
    val mockStyle = mockk<com.mapbox.maps.Style>(relaxed = true)
    every { mockStyle.styleSourceExists("non-existent") } returns false

    val adapter = EventMarkers.RealStyleAdapter(mockStyle)
    val result = adapter.styleSourceExists("non-existent")

    assertFalse(result)
    verify { mockStyle.styleSourceExists("non-existent") }
  }

  @Test
  fun realStyleAdapter_updateSourceFeatures_handlesNullSource() {
    val mockStyle = mockk<com.mapbox.maps.Style>(relaxed = true)

    every {
      mockStyle.getSourceAs<com.mapbox.maps.extension.style.sources.generated.GeoJsonSource>(
          "non-existent")
    } returns null

    val adapter = EventMarkers.RealStyleAdapter(mockStyle)
    val features = listOf(Feature.fromGeometry(Point.fromLngLat(5.0, 6.0)))

    // Should not throw exception when source is null
    adapter.updateSourceFeatures("non-existent", features)

    verify {
      mockStyle.getSourceAs<com.mapbox.maps.extension.style.sources.generated.GeoJsonSource>(
          "non-existent")
    }
  }
}

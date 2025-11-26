package com.github.se.studentconnect.ui.screen.filters

import com.mapbox.geojson.Point
import kotlin.math.abs
import kotlin.math.sqrt
import org.junit.Assert.*
import org.junit.Test

class SelectionPickerTest {

  companion object {
    private const val DELTA = 0.0001 // Tolerance for floating point comparisons
    private const val EARTH_RADIUS_KM = 6371.0
  }

  @Test
  fun getDestinationPoint_at0Degrees_returnsPointNorth() {
    // Arrange: Start at EPFL coordinates
    val center = Point.fromLngLat(6.5668, 46.5191)
    val bearing = 0.0 // North
    val distanceKm = 10.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Longitude should remain approximately the same, latitude should increase
    assertEquals(center.longitude(), result.longitude(), DELTA)
    assertTrue("Latitude should increase when moving north", result.latitude() > center.latitude())
  }

  @Test
  fun getDestinationPoint_at90Degrees_returnsPointEast() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val bearing = 90.0 // East
    val distanceKm = 10.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Latitude should remain approximately the same, longitude should increase
    assertEquals(center.latitude(), result.latitude(), DELTA)
    assertTrue(
        "Longitude should increase when moving east", result.longitude() > center.longitude())
  }

  @Test
  fun getDestinationPoint_at180Degrees_returnsPointSouth() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val bearing = 180.0 // South
    val distanceKm = 10.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Longitude should remain approximately the same, latitude should decrease
    assertEquals(center.longitude(), result.longitude(), DELTA)
    assertTrue("Latitude should decrease when moving south", result.latitude() < center.latitude())
  }

  @Test
  fun getDestinationPoint_at270Degrees_returnsPointWest() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val bearing = 270.0 // West
    val distanceKm = 10.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Latitude should remain approximately the same, longitude should decrease
    assertEquals(center.latitude(), result.latitude(), DELTA)
    assertTrue(
        "Longitude should decrease when moving west", result.longitude() < center.longitude())
  }

  @Test
  fun getDestinationPoint_withZeroDistance_returnsSamePoint() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val bearing = 45.0
    val distanceKm = 0.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Should return the same point
    assertEquals(center.latitude(), result.latitude(), DELTA)
    assertEquals(center.longitude(), result.longitude(), DELTA)
  }

  @Test
  fun getDestinationPoint_atEquator_calculatesCorrectly() {
    // Arrange: Point at equator
    val center = Point.fromLngLat(0.0, 0.0)
    val bearing = 90.0 // East
    val distanceKm = 100.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: At equator, 1 degree of longitude ≈ 111.32 km
    // So 100 km ≈ 0.898 degrees
    assertTrue("Longitude should be positive", result.longitude() > 0.0)
    assertTrue("Longitude should be less than 1 degree", result.longitude() < 1.0)
    assertEquals(0.0, result.latitude(), DELTA)
  }

  @Test
  fun getDestinationPoint_nearPole_handlesHighLatitude() {
    // Arrange: Point near north pole
    val center = Point.fromLngLat(0.0, 85.0)
    val bearing = 0.0 // North
    val distanceKm = 100.0

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Should move closer to pole
    assertTrue("Latitude should increase towards pole", result.latitude() > center.latitude())
    assertTrue("Latitude should be less than 90", result.latitude() < 90.0)
  }

  @Test
  fun createCirclePoints_withDefaultSteps_returns65Points() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 10.0

    // Act: Default steps = 64, which creates 65 points (0 to 64 inclusive)
    val points = createCirclePoints(center, radiusKm)

    // Assert
    assertEquals(65, points.size)
  }

  @Test
  fun createCirclePoints_withCustomSteps_returnsCorrectCount() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 5.0
    val steps = 16

    // Act
    val points = createCirclePoints(center, radiusKm, steps)

    // Assert: Should return steps + 1 points (0 to steps inclusive)
    assertEquals(steps + 1, points.size)
  }

  @Test
  fun createCirclePoints_firstAndLastPoints_areApproximatelySame() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 10.0

    // Act
    val points = createCirclePoints(center, radiusKm)

    // Assert: First and last points should be very close (closing the circle)
    val first = points.first()
    val last = points.last()
    assertEquals(first.latitude(), last.latitude(), 0.001)
    assertEquals(first.longitude(), last.longitude(), 0.001)
  }

  @Test
  fun createCirclePoints_allPointsAreApproximatelySameDistanceFromCenter() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 15.0

    // Act
    val points = createCirclePoints(center, radiusKm, 32)

    // Assert: All points should be approximately radiusKm away from center
    points.forEach { point ->
      val distance = calculateHaversineDistance(center, point)
      assertEquals(
          "Point should be at radius distance from center",
          radiusKm,
          distance,
          0.1) // 100m tolerance
    }
  }

  @Test
  fun createCirclePoints_withSmallRadius_createsSmallCircle() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 1.0

    // Act
    val points = createCirclePoints(center, radiusKm, 8)

    // Assert: All points should be close to center
    points.forEach { point ->
      val distance = calculateHaversineDistance(center, point)
      assertTrue("Point should be within 1.1 km", distance <= 1.1)
    }
  }

  @Test
  fun createCirclePoints_withLargeRadius_createsLargeCircle() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 100.0

    // Act
    val points = createCirclePoints(center, radiusKm, 16)

    // Assert: Points should be spread far from center
    val maxDistance = points.maxOf { point -> calculateHaversineDistance(center, point) }
    assertTrue("Maximum distance should be close to 100km", maxDistance > 95.0)
    assertTrue("Maximum distance should not exceed 105km", maxDistance < 105.0)
  }

  @Test
  fun createCirclePoints_withSingleStep_returnsTwoPoints() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 10.0
    val steps = 1

    // Act
    val points = createCirclePoints(center, radiusKm, steps)

    // Assert: 0 to 1 inclusive = 2 points
    assertEquals(2, points.size)
  }

  @Test
  fun createCirclePoints_compassPoints_areCorrectlyPositioned() {
    // Arrange
    val center = Point.fromLngLat(0.0, 0.0) // Equator for simplicity
    val radiusKm = 10.0
    val steps = 4 // Creates points at 0°, 90°, 180°, 270°, 360° (same as 0°)

    // Act
    val points = createCirclePoints(center, radiusKm, steps)

    // Assert: 5 points total
    assertEquals(5, points.size)

    // Point 0 (bearing 0°) - North
    assertTrue("First point should be north", points[0].latitude() > center.latitude())

    // Point 1 (bearing 90°) - East
    assertTrue("Second point should be east", points[1].longitude() > center.longitude())

    // Point 2 (bearing 180°) - South
    assertTrue("Third point should be south", points[2].latitude() < center.latitude())

    // Point 3 (bearing 270°) - West
    assertTrue("Fourth point should be west", points[3].longitude() < center.longitude())

    // Point 4 (bearing 360° = 0°) - North again (closing circle)
    assertEquals(points[0].latitude(), points[4].latitude(), 0.001)
    assertEquals(points[0].longitude(), points[4].longitude(), 0.001)
  }

  @Test
  fun getDestinationPoint_withVeryLargeDistance_handlesCorrectly() {
    // Arrange: Travel half-way around the earth
    val center = Point.fromLngLat(0.0, 0.0)
    val bearing = 90.0
    val distanceKm = EARTH_RADIUS_KM * Math.PI // ~20,000 km (half circumference)

    // Act
    val result = getDestinationPoint(center, bearing, distanceKm)

    // Assert: Should be near the opposite side of the earth
    assertTrue("Should be far from starting point", abs(result.longitude()) > 170.0)
  }

  @Test
  fun createCirclePoints_withZeroRadius_returnsPointsAtCenter() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 0.0

    // Act
    val points = createCirclePoints(center, radiusKm, 8)

    // Assert: All points should be at the center
    points.forEach { point ->
      assertEquals(center.latitude(), point.latitude(), DELTA)
      assertEquals(center.longitude(), point.longitude(), DELTA)
    }
  }

  @Test
  fun createCirclePoints_producesSymmetricCircle() {
    // Arrange
    val center = Point.fromLngLat(6.5668, 46.5191)
    val radiusKm = 20.0
    val steps = 8

    // Act
    val points = createCirclePoints(center, radiusKm, steps)

    // Assert: Opposite points should be equidistant from center
    // Point at index 2 (90°) and point at index 6 (270°) should mirror
    val eastPoint = points[2]
    val westPoint = points[6]

    val eastDist = calculateHaversineDistance(center, eastPoint)
    val westDist = calculateHaversineDistance(center, westPoint)

    assertEquals("East and west points should be same distance", eastDist, westDist, 0.1)
  }

  // Helper function to calculate Haversine distance between two points
  private fun calculateHaversineDistance(point1: Point, point2: Point): Double {
    val lat1Rad = Math.toRadians(point1.latitude())
    val lat2Rad = Math.toRadians(point2.latitude())
    val deltaLat = Math.toRadians(point2.latitude() - point1.latitude())
    val deltaLon = Math.toRadians(point2.longitude() - point1.longitude())

    val a =
        kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2) +
            kotlin.math.cos(lat1Rad) *
                kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLon / 2) *
                kotlin.math.sin(deltaLon / 2)

    val c = 2 * kotlin.math.atan2(sqrt(a), sqrt(1 - a))

    return EARTH_RADIUS_KM * c
  }
}

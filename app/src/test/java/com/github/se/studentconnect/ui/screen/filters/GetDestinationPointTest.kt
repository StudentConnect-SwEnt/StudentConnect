package com.github.se.studentconnect.ui.screen.filters

import com.mapbox.geojson.Point
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDestinationPointTest {

  private val TOLERANCE = 0.01

  @Test
  fun testGetDestinationPoint_zeroDistance_returnsSamePoint() {
    val center = Point.fromLngLat(6.5674, 46.5197)
    val result = getDestinationPoint(center, 0.0, 0.0)

    assertEquals(center.latitude(), result.latitude(), 1e-9)
    assertEquals(center.longitude(), result.longitude(), 1e-9)
  }

  @Test
  fun testGetDestinationPoint_zeroDistance_anyBearing_returnsSamePoint() {
    val center = Point.fromLngLat(2.3522, 48.8566)
    val bearings = listOf(0.0, 90.0, 180.0, 270.0, 45.0, 135.0)

    bearings.forEach { bearing ->
      val result = getDestinationPoint(center, bearing, 0.0)
      assertEquals(center.latitude(), result.latitude(), 1e-9)
      assertEquals(center.longitude(), result.longitude(), 1e-9)
    }
  }

  @Test
  fun testGetDestinationPoint_north_increasesLatitude() {
    val center = Point.fromLngLat(0.0, 0.0)
    val bearing = 0.0
    val distance = 100.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.latitude() > center.latitude()) {
      "Moving north should increase latitude. Expected > ${center.latitude()}, got ${result.latitude()}"
    }
    assertEquals(center.longitude(), result.longitude(), TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_south_decreasesLatitude() {
    val center = Point.fromLngLat(0.0, 10.0)
    val bearing = 180.0
    val distance = 100.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.latitude() < center.latitude()) {
      "Moving south should decrease latitude. Expected < ${center.latitude()}, got ${result.latitude()}"
    }
    assertEquals(center.longitude(), result.longitude(), TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_east_increasesLongitude() {
    val center = Point.fromLngLat(0.0, 0.0)
    val bearing = 90.0
    val distance = 100.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.longitude() > center.longitude()) {
      "Moving east should increase longitude. Expected > ${center.longitude()}, got ${result.longitude()}"
    }
    assertEquals(center.latitude(), result.latitude(), TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_west_decreasesLongitude() {
    val center = Point.fromLngLat(10.0, 0.0)
    val bearing = 270.0
    val distance = 100.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.longitude() < center.longitude()) {
      "Moving west should decrease longitude. Expected < ${center.longitude()}, got ${result.longitude()}"
    }
    assertEquals(center.latitude(), result.latitude(), TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_roundTrip_returnsToOrigin() {
    val center = Point.fromLngLat(6.5674, 46.5197)
    val distance = 50.0

    val northPoint = getDestinationPoint(center, 0.0, distance)
    val backToCenter = getDestinationPoint(northPoint, 180.0, distance)

    assertEquals(center.latitude(), backToCenter.latitude(), TOLERANCE)
    assertEquals(center.longitude(), backToCenter.longitude(), TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_largeDistance() {
    val center = Point.fromLngLat(0.0, 0.0)
    val bearing = 45.0
    val distance = 1000.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.latitude() > center.latitude())
    assert(result.longitude() > center.longitude())
    assert(abs(result.latitude()) > 5.0) { "Should move significantly north" }
    assert(abs(result.longitude()) > 5.0) { "Should move significantly east" }
  }

  @Test
  fun testGetDestinationPoint_allCardinalDirections() {
    val center = Point.fromLngLat(0.0, 45.0)
    val distance = 100.0
    val cardinals = mapOf(0.0 to "N", 90.0 to "E", 180.0 to "S", 270.0 to "W")

    cardinals.forEach { (bearing, direction) ->
      val result = getDestinationPoint(center, bearing, distance)

      val latDiff = abs(result.latitude() - center.latitude())
      val lngDiff = abs(result.longitude() - center.longitude())
      val totalMovement = latDiff + lngDiff

      assert(totalMovement > 0.1) {
        "Direction $direction ($bearing°) should move the point significantly. Got lat: ${result.latitude()}, lng: ${result.longitude()}"
      }
    }
  }

  @Test
  fun testGetDestinationPoint_negativeLatitude_southernHemisphere() {
    val center = Point.fromLngLat(18.4241, -33.9249)
    val bearing = 90.0
    val distance = 100.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.longitude() > center.longitude())
    assertEquals(center.latitude(), result.latitude(), TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_nearPoles_northPole() {
    val center = Point.fromLngLat(0.0, 85.0)
    val bearing = 180.0
    val distance = 500.0

    val result = getDestinationPoint(center, bearing, distance)

    assert(result.latitude() < center.latitude())
  }

  @Test
  fun testGetDestinationPoint_oppositeDirections_sameDistance() {
    val center = Point.fromLngLat(0.0, 0.0)
    val distance = 100.0

    val north = getDestinationPoint(center, 0.0, distance)
    val south = getDestinationPoint(center, 180.0, distance)
    val east = getDestinationPoint(center, 90.0, distance)
    val west = getDestinationPoint(center, 270.0, distance)

    assertEquals(
        abs(north.latitude() - center.latitude()),
        abs(south.latitude() - center.latitude()),
        TOLERANCE)
    assertEquals(
        abs(east.longitude() - center.longitude()),
        abs(west.longitude() - center.longitude()),
        TOLERANCE)
  }

  @Test
  fun testGetDestinationPoint_fullCircle_360degrees() {
    val center = Point.fromLngLat(6.5674, 46.5197)
    val distance = 10.0

    val result0 = getDestinationPoint(center, 0.0, distance)
    val result360 = getDestinationPoint(center, 360.0, distance)

    assertEquals(result0.latitude(), result360.latitude(), 1e-6)
    assertEquals(result0.longitude(), result360.longitude(), 1e-6)
  }

  @Test
  fun testGetDestinationPoint_verySmallDistance() {
    val center = Point.fromLngLat(6.5674, 46.5197)
    val bearing = 45.0
    val distance = 0.001

    val result = getDestinationPoint(center, bearing, distance)

    assertEquals(center.latitude(), result.latitude(), 0.0001)
    assertEquals(center.longitude(), result.longitude(), 0.0001)
  }
}

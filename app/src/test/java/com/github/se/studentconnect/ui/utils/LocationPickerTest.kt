package com.github.se.studentconnect.ui.utils

import com.github.se.studentconnect.model.location.Location
import org.junit.Assert.*
import org.junit.Test

class LocationPickerTest {

  @Test
  fun location_creationWithValidCoordinates_succeeds() {
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")

    assertEquals(46.5191, location.latitude, 0.0001)
    assertEquals(6.5668, location.longitude, 0.0001)
    assertEquals("EPFL", location.name)
  }

  @Test
  fun location_withEmptyName_isValid() {
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = "")

    assertEquals("", location.name)
    assertNotNull(location)
  }

  @Test
  fun location_equalsAndHashCode_workCorrectly() {
    val location1 = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")
    val location2 = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")

    assertEquals(location1, location2)
    assertEquals(location1.hashCode(), location2.hashCode())
  }

  @Test
  fun location_differentLocations_areNotEqual() {
    val location1 = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")
    val location2 = Location(latitude = 46.5192, longitude = 6.5669, name = "Not EPFL")

    assertNotEquals(location1, location2)
  }

  @Test
  fun location_coordinatesPrecision_isPreserved() {
    val latitude = 46.51912345
    val longitude = 6.56689876
    val location = Location(latitude = latitude, longitude = longitude, name = "Test")

    assertEquals(latitude, location.latitude, 0.00000001)
    assertEquals(longitude, location.longitude, 0.00000001)
  }

  @Test
  fun location_negativeCoordinates_areValid() {
    val location = Location(latitude = -46.5191, longitude = -6.5668, name = "South West")

    assertEquals(-46.5191, location.latitude, 0.0001)
    assertEquals(-6.5668, location.longitude, 0.0001)
  }

  @Test
  fun location_extremeCoordinates_areValid() {
    val location1 = Location(latitude = 90.0, longitude = 180.0, name = "North Pole")
    val location2 = Location(latitude = -90.0, longitude = -180.0, name = "South Pole")

    assertEquals(90.0, location1.latitude, 0.0001)
    assertEquals(180.0, location1.longitude, 0.0001)
    assertEquals(-90.0, location2.latitude, 0.0001)
    assertEquals(-180.0, location2.longitude, 0.0001)
  }

  @Test
  fun location_copy_createsNewInstance() {
    val original = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")
    val copy = original.copy()

    assertEquals(original, copy)
    assertNotSame(original, copy)
  }

  @Test
  fun location_copyWithModification_modifiesOnlySpecifiedFields() {
    val original = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")
    val modified = original.copy(name = "New Name")

    assertEquals(46.5191, modified.latitude, 0.0001)
    assertEquals(6.5668, modified.longitude, 0.0001)
    assertEquals("New Name", modified.name)
    assertNotEquals(original.name, modified.name)
  }

  @Test
  fun location_toString_containsAllFields() {
    val location = Location(latitude = 46.5191, longitude = 6.5668, name = "EPFL")
    val toString = location.toString()

    assertTrue(toString.contains("46.5191") || toString.contains("latitude"))
    assertTrue(toString.contains("6.5668") || toString.contains("longitude"))
    assertTrue(toString.contains("EPFL") || toString.contains("name"))
  }
}

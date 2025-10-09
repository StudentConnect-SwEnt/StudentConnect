package com.github.se.studentconnect.model.map

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// Simple tests that don't require Android framework mocking
@ExperimentalCoroutinesApi
class LocationRepositoryInterfaceTest {

  private lateinit var mockRepository: LocationRepository

  @Before
  fun setUp() {
    mockRepository = mockk()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun hasLocationPermission_interface_works() {
    every { mockRepository.hasLocationPermission() } returns true

    val result = mockRepository.hasLocationPermission()

    assertTrue(result)
    verify { mockRepository.hasLocationPermission() }
  }

  @Test
  fun getCurrentLocation_interface_returnsResult() = runTest {
    val mockLocation =
        mockk<Location> {
          every { latitude } returns 46.5089
          every { longitude } returns 6.6283
        }
    val expectedResult = LocationResult.Success(mockLocation)

    coEvery { mockRepository.getCurrentLocation() } returns expectedResult

    val result = mockRepository.getCurrentLocation()

    assertEquals(expectedResult, result)
  }

  @Test
  fun getCurrentLocation_interface_returnsPermissionDenied() = runTest {
    coEvery { mockRepository.getCurrentLocation() } returns LocationResult.PermissionDenied

    val result = mockRepository.getCurrentLocation()

    assertEquals(LocationResult.PermissionDenied, result)
  }

  @Test
  fun getCurrentLocation_interface_returnsError() = runTest {
    val errorMessage = "Location service unavailable"
    val expectedResult = LocationResult.Error(errorMessage)

    coEvery { mockRepository.getCurrentLocation() } returns expectedResult

    val result = mockRepository.getCurrentLocation()

    assertTrue(result is LocationResult.Error)
    val error = result as LocationResult.Error
    assertEquals(errorMessage, error.message)
  }

  @Test
  fun getCurrentLocation_interface_returnsTimeout() = runTest {
    coEvery { mockRepository.getCurrentLocation() } returns LocationResult.Timeout

    val result = mockRepository.getCurrentLocation()

    assertEquals(LocationResult.Timeout, result)
  }

  @Test
  fun getCurrentLocation_interface_returnsLocationDisabled() = runTest {
    coEvery { mockRepository.getCurrentLocation() } returns LocationResult.LocationDisabled

    val result = mockRepository.getCurrentLocation()

    assertEquals(LocationResult.LocationDisabled, result)
  }
}

class LocationPermissionRepositoryTest {

  private lateinit var context: Context
  private lateinit var permissionRepository: LocationPermissionRepositoryImpl

  @Before
  fun setUp() {
    context = mockk()
    permissionRepository = LocationPermissionRepositoryImpl()
    mockkStatic(ContextCompat::class)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun checkPermissionStatus_bothPermissionsGranted_returnsGrantedStatus() {
    every { ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) } returns
        PackageManager.PERMISSION_GRANTED
    every { ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) } returns
        PackageManager.PERMISSION_GRANTED

    val result = permissionRepository.checkPermissionStatus(context)

    assertEquals(PermissionStatus.GRANTED, result.status)
    assertTrue(result.hasFineLocation)
    assertTrue(result.hasCoarseLocation)
    assertTrue(result.hasAnyLocationPermission)
  }

  @Test
  fun checkPermissionStatus_onlyFineLocationGranted_returnsGrantedStatus() {
    every { ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) } returns
        PackageManager.PERMISSION_GRANTED
    every { ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED

    val result = permissionRepository.checkPermissionStatus(context)

    assertEquals(PermissionStatus.GRANTED, result.status)
    assertTrue(result.hasFineLocation)
    assertFalse(result.hasCoarseLocation)
    assertTrue(result.hasAnyLocationPermission)
  }

  @Test
  fun checkPermissionStatus_onlyCoarseLocationGranted_returnsGrantedStatus() {
    every { ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED
    every { ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) } returns
        PackageManager.PERMISSION_GRANTED

    val result = permissionRepository.checkPermissionStatus(context)

    assertEquals(PermissionStatus.GRANTED, result.status)
    assertFalse(result.hasFineLocation)
    assertTrue(result.hasCoarseLocation)
    assertTrue(result.hasAnyLocationPermission)
  }

  @Test
  fun checkPermissionStatus_noPermissionsGranted_returnsUnknownStatus() {
    every { ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED
    every { ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED

    val result = permissionRepository.checkPermissionStatus(context)

    assertEquals(PermissionStatus.UNKNOWN, result.status)
    assertFalse(result.hasFineLocation)
    assertFalse(result.hasCoarseLocation)
    assertFalse(result.hasAnyLocationPermission)
  }

  @Test
  fun hasLocationPermission_withPermission_returnsTrue() {
    every { ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) } returns
        PackageManager.PERMISSION_GRANTED
    every { ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED

    val result = permissionRepository.hasLocationPermission(context)

    assertTrue(result)
  }

  @Test
  fun hasLocationPermission_withoutPermission_returnsFalse() {
    every { ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED
    every { ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) } returns
        PackageManager.PERMISSION_DENIED

    val result = permissionRepository.hasLocationPermission(context)

    assertFalse(result)
  }
}

class LocationConfigTest {

  @Test
  fun locationConfig_hasCorrectConstants() {
    assertEquals("android.permission.ACCESS_FINE_LOCATION", LocationConfig.FINE_LOCATION)
    assertEquals("android.permission.ACCESS_COARSE_LOCATION", LocationConfig.COARSE_LOCATION)

    assertEquals(1000L, LocationConfig.HIGH_ACCURACY_INTERVAL_MS)
    assertEquals(5000L, LocationConfig.LOCATION_UPDATES_INTERVAL_MS)
    assertEquals(2000L, LocationConfig.MIN_UPDATE_INTERVAL_MS)
    assertEquals(10000L, LocationConfig.LOCATION_REQUEST_TIMEOUT_MS)
    assertEquals(60000L, LocationConfig.LOCATION_FRESHNESS_THRESHOLD_MS)

    assertEquals(1, LocationConfig.MAX_UPDATES_SINGLE_REQUEST)

    assertEquals(
        "Location permission required to show your position", LocationConfig.PERMISSION_REQUIRED)
    assertEquals("Location permission is required", LocationConfig.PERMISSION_REQUIRED_FOR_FEATURE)
    assertEquals("Location request timed out. Please try again.", LocationConfig.LOCATION_TIMEOUT)
    assertEquals(
        "Location services are disabled. Please enable them in settings.",
        LocationConfig.LOCATION_DISABLED)
  }
}

class PermissionStatusTest {

  @Test
  fun permissionStatus_enumValues() {
    val values = PermissionStatus.values()

    assertEquals(4, values.size)
    assertTrue(values.contains(PermissionStatus.UNKNOWN))
    assertTrue(values.contains(PermissionStatus.GRANTED))
    assertTrue(values.contains(PermissionStatus.DENIED))
    assertTrue(values.contains(PermissionStatus.REQUESTING))
  }
}

class LocationPermissionTest {

  @Test
  fun locationPermission_defaultValues() {
    val permission = LocationPermission()

    assertEquals(PermissionStatus.UNKNOWN, permission.status)
    assertFalse(permission.hasFineLocation)
    assertFalse(permission.hasCoarseLocation)
    assertFalse(permission.hasAnyLocationPermission)
  }

  @Test
  fun locationPermission_customValues() {
    val permission =
        LocationPermission(
            status = PermissionStatus.GRANTED, hasFineLocation = true, hasCoarseLocation = false)

    assertEquals(PermissionStatus.GRANTED, permission.status)
    assertTrue(permission.hasFineLocation)
    assertFalse(permission.hasCoarseLocation)
    assertTrue(permission.hasAnyLocationPermission)
  }

  @Test
  fun hasAnyLocationPermission_withFineLocation_returnsTrue() {
    val permission = LocationPermission(hasFineLocation = true, hasCoarseLocation = false)
    assertTrue(permission.hasAnyLocationPermission)
  }

  @Test
  fun hasAnyLocationPermission_withCoarseLocation_returnsTrue() {
    val permission = LocationPermission(hasFineLocation = false, hasCoarseLocation = true)
    assertTrue(permission.hasAnyLocationPermission)
  }

  @Test
  fun hasAnyLocationPermission_withBothLocations_returnsTrue() {
    val permission = LocationPermission(hasFineLocation = true, hasCoarseLocation = true)
    assertTrue(permission.hasAnyLocationPermission)
  }

  @Test
  fun hasAnyLocationPermission_withNoLocations_returnsFalse() {
    val permission = LocationPermission(hasFineLocation = false, hasCoarseLocation = false)
    assertFalse(permission.hasAnyLocationPermission)
  }
}

class LocationResultTest {

  @Test
  fun locationResult_success() {
    val mockLocation = mockk<Location>()
    val result = LocationResult.Success(mockLocation)

    assertEquals(mockLocation, result.location)
  }

  @Test
  fun locationResult_error() {
    val message = "Test error"
    val cause = Exception("Root cause")
    val result = LocationResult.Error(message, cause)

    assertEquals(message, result.message)
    assertEquals(cause, result.cause)
  }

  @Test
  fun locationResult_errorWithoutCause() {
    val message = "Test error"
    val result = LocationResult.Error(message)

    assertEquals(message, result.message)
    assertNull(result.cause)
  }

  @Test
  fun locationResult_permissionDenied() {
    val result = LocationResult.PermissionDenied
    assertTrue(result is LocationResult.PermissionDenied)
  }

  @Test
  fun locationResult_locationDisabled() {
    val result = LocationResult.LocationDisabled
    assertTrue(result is LocationResult.LocationDisabled)
  }

  @Test
  fun locationResult_timeout() {
    val result = LocationResult.Timeout
    assertTrue(result is LocationResult.Timeout)
  }
}

package com.github.se.studentconnect.model.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as GmsLocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

// Configuration
object LocationConfig {
  // Permissions
  const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
  const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

  // Request intervals (milliseconds)
  const val HIGH_ACCURACY_INTERVAL_MS = 1000L
  const val LOCATION_UPDATES_INTERVAL_MS = 5000L
  const val MIN_UPDATE_INTERVAL_MS = 2000L
  const val LOCATION_REQUEST_TIMEOUT_MS = 10000L
  const val LOCATION_FRESHNESS_THRESHOLD_MS = 60000L // 1 minute

  // Request settings
  const val MAX_UPDATES_SINGLE_REQUEST = 1

  // Error messages
  const val PERMISSION_REQUIRED = "Location permission required to show your position"
  const val PERMISSION_REQUIRED_FOR_FEATURE = "Location permission is required"
  const val LOCATION_TIMEOUT = "Location request timed out. Please try again."
  const val LOCATION_DISABLED = "Location services are disabled. Please enable them in settings."
}

// Permission State
enum class PermissionStatus {
  UNKNOWN,
  GRANTED,
  DENIED,
  REQUESTING
}

data class LocationPermission(
    val status: PermissionStatus = PermissionStatus.UNKNOWN,
    val hasFineLocation: Boolean = false,
    val hasCoarseLocation: Boolean = false
) {
  val hasAnyLocationPermission: Boolean
    get() = hasFineLocation || hasCoarseLocation
}

// Location Result
sealed class LocationResult {
  data class Success(val location: Location) : LocationResult()

  data class Error(val message: String, val cause: Throwable? = null) : LocationResult()

  object PermissionDenied : LocationResult()

  object LocationDisabled : LocationResult()

  object Timeout : LocationResult()
}

// Permission Repository
interface LocationPermissionRepository {
  fun checkPermissionStatus(context: Context): LocationPermission

  fun hasLocationPermission(context: Context): Boolean
}

class LocationPermissionRepositoryImpl : LocationPermissionRepository {
  override fun checkPermissionStatus(context: Context): LocationPermission {
    val hasFineLocation =
        ContextCompat.checkSelfPermission(context, LocationConfig.FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    val hasCoarseLocation =
        ContextCompat.checkSelfPermission(context, LocationConfig.COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    return LocationPermission(
        status =
            if (hasFineLocation || hasCoarseLocation) {
              PermissionStatus.GRANTED
            } else {
              PermissionStatus.UNKNOWN
            },
        hasFineLocation = hasFineLocation,
        hasCoarseLocation = hasCoarseLocation)
  }

  override fun hasLocationPermission(context: Context): Boolean {
    return checkPermissionStatus(context).hasAnyLocationPermission
  }
}

// Location Repository Interface
interface LocationRepository {
  suspend fun getCurrentLocation(): LocationResult

  fun getLocationUpdates(): Flow<LocationResult>

  fun hasLocationPermission(): Boolean
}

// Location Repository Implementation
class LocationRepositoryImpl(
    private val context: Context,
    private val permissionRepository: LocationPermissionRepository =
        LocationPermissionRepositoryImpl()
) : LocationRepository {

  private val fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

  override fun hasLocationPermission(): Boolean {
    return permissionRepository.hasLocationPermission(context)
  }

  @SuppressLint("MissingPermission")
  override suspend fun getCurrentLocation(): LocationResult {
    if (!hasLocationPermission()) {
      return LocationResult.PermissionDenied
    }

    return try {
      withTimeout(LocationConfig.LOCATION_REQUEST_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->
          fusedLocationClient.lastLocation
              .addOnSuccessListener { location: Location? ->
                if (location != null && isLocationRecent(location)) {
                  continuation.resume(LocationResult.Success(location))
                } else {
                  requestFreshLocation { result -> continuation.resume(result) }
                }
              }
              .addOnFailureListener { exception ->
                continuation.resume(LocationResult.Error("Failed to get location", exception))
              }
        }
      }
    } catch (e: TimeoutCancellationException) {
      LocationResult.Timeout
    } catch (e: SecurityException) {
      LocationResult.PermissionDenied
    } catch (e: Exception) {
      LocationResult.Error("Unexpected error occurred", e)
    }
  }

  @SuppressLint("MissingPermission")
  override fun getLocationUpdates(): Flow<LocationResult> = callbackFlow {
    if (!hasLocationPermission()) {
      trySend(LocationResult.PermissionDenied)
      close()
      return@callbackFlow
    }

    val locationRequest =
        LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, LocationConfig.LOCATION_UPDATES_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LocationConfig.MIN_UPDATE_INTERVAL_MS)
            .build()

    val locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: GmsLocationResult) {
            locationResult.lastLocation?.let { location ->
              trySend(LocationResult.Success(location))
            } ?: trySend(LocationResult.Error("No location in update"))
          }
        }

    try {
      fusedLocationClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper())
    } catch (e: SecurityException) {
      trySend(LocationResult.PermissionDenied)
      close(e)
    } catch (e: Exception) {
      trySend(LocationResult.Error("Failed to start location updates", e))
      close(e)
    }

    awaitClose { fusedLocationClient.removeLocationUpdates(locationCallback) }
  }

  @SuppressLint("MissingPermission")
  private fun requestFreshLocation(callback: (LocationResult) -> Unit) {
    val locationRequest =
        LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, LocationConfig.HIGH_ACCURACY_INTERVAL_MS)
            .setMaxUpdates(LocationConfig.MAX_UPDATES_SINGLE_REQUEST)
            .build()

    val locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: GmsLocationResult) {
            locationResult.lastLocation?.let { location ->
              callback(LocationResult.Success(location))
            } ?: callback(LocationResult.Error("No location received"))
            fusedLocationClient.removeLocationUpdates(this)
          }
        }

    try {
      fusedLocationClient.requestLocationUpdates(
          locationRequest, locationCallback, Looper.getMainLooper())
    } catch (e: SecurityException) {
      callback(LocationResult.PermissionDenied)
    } catch (e: Exception) {
      callback(LocationResult.Error("Failed to request location updates", e))
    }
  }

  private fun isLocationRecent(location: Location): Boolean {
    val currentTime = System.currentTimeMillis()
    val locationTime = location.time
    val timeDifference = currentTime - locationTime
    return timeDifference <= LocationConfig.LOCATION_FRESHNESS_THRESHOLD_MS
  }
}

// Composable Permission Handler
@Composable
fun RequestLocationPermission(
    permissionRepository: LocationPermissionRepository = LocationPermissionRepositoryImpl(),
    onPermissionResult: (LocationPermission) -> Unit
) {
  val context = LocalContext.current
  var permissionState by remember { mutableStateOf(LocationPermission()) }

  val permissions = arrayOf(LocationConfig.FINE_LOCATION, LocationConfig.COARSE_LOCATION)

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
            val fineLocationGranted = permissionsMap[LocationConfig.FINE_LOCATION] ?: false
            val coarseLocationGranted = permissionsMap[LocationConfig.COARSE_LOCATION] ?: false

            val newState =
                LocationPermission(
                    status =
                        if (fineLocationGranted || coarseLocationGranted) {
                          PermissionStatus.GRANTED
                        } else {
                          PermissionStatus.DENIED
                        },
                    hasFineLocation = fineLocationGranted,
                    hasCoarseLocation = coarseLocationGranted)

            permissionState = newState
            onPermissionResult(newState)
          }

  LaunchedEffect(Unit) {
    val currentState = permissionRepository.checkPermissionStatus(context)
    permissionState = currentState

    when {
      currentState.hasAnyLocationPermission -> {
        onPermissionResult(currentState)
      }
      currentState.status == PermissionStatus.UNKNOWN -> {
        permissionState = permissionState.copy(status = PermissionStatus.REQUESTING)
        permissionLauncher.launch(permissions)
      }
    }
  }
}

// Convenience Composable for simple permission handling
@Composable
fun RequestLocationPermission(onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
  RequestLocationPermission { permission ->
    if (permission.hasAnyLocationPermission) {
      onPermissionGranted()
    } else {
      onPermissionDenied()
    }
  }
}

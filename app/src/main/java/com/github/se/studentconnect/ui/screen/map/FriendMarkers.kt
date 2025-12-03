package com.github.se.studentconnect.ui.screen.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import android.util.LruCache
import androidx.compose.ui.graphics.asAndroidBitmap
import com.github.se.studentconnect.model.User
import com.github.se.studentconnect.model.friends.FriendLocation
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.repository.UserRepository
import com.github.se.studentconnect.ui.utils.loadBitmapFromUri
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Optimized utility object for managing friend markers on the map with profile image caching.
 *
 * Features:
 * - Circular profile images with clean design
 * - Purple dot fallback for users without profile images
 * - Green pulsing border for live users, dimmed for last-known locations
 * - LRU cache for profile images and user data to minimize network calls
 * - Async image loading using the same pattern as ProfileScreen
 * - Thread-safe operations
 */
object FriendMarkers {
  private const val TAG = "FriendMarkers"

  // Cache for profile image bitmaps (max 50 images, ~10MB)
  private val profileImageCache = LruCache<String, Bitmap>(50)

  // Cache for user data (max 200 users)
  private val userDataCache = LruCache<String, User>(200)

  // Coroutine scope for async operations
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  // Track which user IDs have icons added to the map style
  private val addedIconIds = mutableSetOf<String>()

  /** Removes existing friend marker layers and sources from the map style. */
  fun removeExistingFriendLayers(style: Style) {
    try {
      if (style.styleLayerExists(FriendMarkerConfig.LAYER_ID)) {
        style.removeStyleLayer(FriendMarkerConfig.LAYER_ID)
      }
      if (style.styleSourceExists(FriendMarkerConfig.SOURCE_ID)) {
        style.removeStyleSource(FriendMarkerConfig.SOURCE_ID)
      }
      addedIconIds.clear()
    } catch (e: Exception) {
      Log.e(TAG, "Error removing friend layers", e)
    }
  }

  /** Creates a circular marker bitmap (purple dot for default, or profile image). */
  private fun createMarkerBitmap(
      sizePixels: Int = 120,
      innerBitmap: Bitmap? = null,
      isLive: Boolean = false
  ): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = sizePixels / 2f

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (innerBitmap != null) {
      // Draw circular mask for profile image
      canvas.drawCircle(radius, radius, radius, paint)
      paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
      val scaledBitmap = Bitmap.createScaledBitmap(innerBitmap, sizePixels, sizePixels, true)
      canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
      paint.xfermode = null
      if (scaledBitmap != innerBitmap) {
        scaledBitmap.recycle()
      }
    } else {
      // Draw purple circle for users without profile image
      paint.style = Paint.Style.FILL
      paint.color = android.graphics.Color.parseColor(FriendMarkerConfig.COLOR)
      canvas.drawCircle(radius, radius, radius, paint)
    }

    // Add border (green for live, white for last-known)
    paint.apply {
      style = Paint.Style.STROKE
      strokeWidth = sizePixels * 0.10f
      color =
          if (isLive) {
            android.graphics.Color.parseColor("#10B981") // Green for live
          } else {
            android.graphics.Color.parseColor("#E5E7EB") // Light gray for last-known
          }
    }
    canvas.drawCircle(radius, radius, radius - paint.strokeWidth / 2, paint)

    return bitmap
  }

  /**
   * Loads a profile image for a user and adds it to the map style. Uses the same loading pattern as
   * ProfileScreen with MediaRepository.
   */
  private suspend fun loadAndAddUserIcon(
      context: Context,
      style: Style,
      userId: String,
      user: User?,
      isLive: Boolean
  ) =
      withContext(Dispatchers.Main) {
        try {
          val iconId = getIconIdForUser(userId, isLive)

          // Skip if already added and live status matches
          if (addedIconIds.contains(iconId) && style.hasStyleImage(iconId)) {
            return@withContext
          }

          // Check cache first (cache key includes live status)
          val cacheKey = "$userId-$isLive"
          profileImageCache.get(cacheKey)?.let { cachedBitmap ->
            if (!style.hasStyleImage(iconId)) {
              style.addImage(iconId, cachedBitmap)
              addedIconIds.add(iconId)
            }
            return@withContext
          }

          // Load image using MediaRepository (same as ProfileScreen)
          val bitmap =
              if (user?.profilePictureUrl != null) {
                try {
                  val mediaRepository = MediaRepositoryProvider.repository
                  val uri =
                      withContext(Dispatchers.IO) {
                        mediaRepository.download(user.profilePictureUrl!!)
                      }

                  val imageBitmap = loadBitmapFromUri(context, uri, Dispatchers.IO)
                  val androidBitmap = imageBitmap?.asAndroidBitmap()

                  if (androidBitmap != null) {
                    val markerBitmap =
                        createMarkerBitmap(innerBitmap = androidBitmap, isLive = isLive)
                    profileImageCache.put(cacheKey, markerBitmap)
                    markerBitmap
                  } else {
                    createMarkerBitmap(isLive = isLive).also { profileImageCache.put(cacheKey, it) }
                  }
                } catch (e: Exception) {
                  Log.w(TAG, "Failed to load profile image for $userId, using default", e)
                  createMarkerBitmap(isLive = isLive).also { profileImageCache.put(cacheKey, it) }
                }
              } else {
                createMarkerBitmap(isLive = isLive).also { profileImageCache.put(cacheKey, it) }
              }

          if (!style.hasStyleImage(iconId)) {
            style.addImage(iconId, bitmap)
            addedIconIds.add(iconId)
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error loading icon for user $userId", e)
        }
      }

  /** Generates a unique icon ID for each user based on live status. */
  private fun getIconIdForUser(userId: String, isLive: Boolean): String {
    val suffix = if (isLive) "live" else "lastknown"
    return "${FriendMarkerConfig.ICON_ID}_${userId}_$suffix"
  }

  /**
   * Creates GeoJSON features from friend locations. Each feature references the user's unique icon
   * with live status.
   */
  fun createFriendFeatures(friendLocations: Map<String, FriendLocation>): List<Feature> {
    return friendLocations.values.map { location ->
      Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude)).apply {
        addStringProperty(FriendMarkerConfig.PROP_USER_ID, location.userId)
        addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, location.timestamp)
        addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, location.isLive)
        addStringProperty(
            FriendMarkerConfig.PROP_ICON_ID, getIconIdForUser(location.userId, location.isLive))
      }
    }
  }

  /** Adds a GeoJSON source for friend markers to the map style. */
  fun addFriendSource(style: Style, features: List<Feature>) {
    try {
      val featureCollection = FeatureCollection.fromFeatures(features)
      style.addSource(
          geoJsonSource(FriendMarkerConfig.SOURCE_ID) { featureCollection(featureCollection) })
    } catch (e: Exception) {
      Log.e(TAG, "Error adding friend source", e)
    }
  }

  /** Updates the friend marker source with new location data. */
  fun updateFriendSource(style: Style, features: List<Feature>) {
    try {
      if (style.styleSourceExists(FriendMarkerConfig.SOURCE_ID)) {
        style.removeStyleSource(FriendMarkerConfig.SOURCE_ID)
      }
      addFriendSource(style, features)
    } catch (e: Exception) {
      Log.e(TAG, "Error updating friend source", e)
    }
  }

  /** Adds a symbol layer for individual friend markers with per-user icons. */
  fun addFriendMarkerLayer(style: Style) {
    try {
      style.addLayer(
          symbolLayer(FriendMarkerConfig.LAYER_ID, FriendMarkerConfig.SOURCE_ID) {
            iconImage(
                com.mapbox.maps.extension.style.expressions.dsl.generated.get {
                  literal(FriendMarkerConfig.PROP_ICON_ID)
                })
            iconAllowOverlap(true)
            iconAnchor(IconAnchor.CENTER)
            iconSize(FriendMarkerConfig.ICON_SIZE)
          })
    } catch (e: Exception) {
      Log.e(TAG, "Error adding friend marker layer", e)
    }
  }

  /**
   * Preloads user data and profile images for all friends. Call this when friend locations are
   * updated to ensure smooth marker rendering.
   */
  fun preloadFriendData(
      context: Context,
      style: Style,
      friendLocations: Map<String, FriendLocation>,
      userRepository: UserRepository
  ) {
    scope.launch {
      friendLocations.forEach { (userId, location) ->
        try {
          // Check cache first
          var user = userDataCache.get(userId)

          // Fetch if not cached
          if (user == null) {
            user = userRepository.getUserById(userId)
            user?.let { userDataCache.put(userId, it) }
          }

          // Load icon with live status
          loadAndAddUserIcon(context, style, userId, user, location.isLive)
        } catch (e: Exception) {
          Log.e(TAG, "Error preloading data for friend $userId", e)
        }
      }
    }
  }

  /** Clears all caches. Call when memory is low or user logs out. */
  fun clearCaches() {
    profileImageCache.evictAll()
    userDataCache.evictAll()
    addedIconIds.clear()
    Log.d(TAG, "Caches cleared")
  }
}

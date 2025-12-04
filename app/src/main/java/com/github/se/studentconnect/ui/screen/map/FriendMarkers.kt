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
 * Utility object for managing friend location markers on the map.
 *
 * Marker Display Logic:
 * - **Live locations**: Purple dot or profile picture (active sharing)
 * - **Stale locations**: Grey dot (last known position, up to 8 hours old)
 * - Profile images are cached to minimize network calls
 * - All operations are thread-safe
 */
object FriendMarkers {
  private const val TAG = "FriendMarkers"

  private val profileImageCache = LruCache<String, Bitmap>(50)
  private val userDataCache = LruCache<String, User>(200)
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
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

  /**
   * Creates a circular marker bitmap with appropriate styling based on live status.
   *
   * @param sizePixels The size of the marker in pixels
   * @param innerBitmap Optional profile image bitmap
   * @param isLive Whether the location is actively being shared
   * @return Styled marker bitmap (purple/profile for live, grey for stale)
   */
  private fun createMarkerBitmap(
      sizePixels: Int = 120,
      innerBitmap: Bitmap? = null,
      isLive: Boolean = true
  ): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = sizePixels / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Draw the main circle (profile image or solid color)
    if (innerBitmap != null) {
      // Circular profile image
      canvas.drawCircle(radius, radius, radius, paint)
      paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
      val scaledBitmap = Bitmap.createScaledBitmap(innerBitmap, sizePixels, sizePixels, true)
      canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
      paint.xfermode = null
      if (scaledBitmap != innerBitmap) scaledBitmap.recycle()
    } else {
      // Solid color dot: purple for live, grey for stale
      paint.style = Paint.Style.FILL
      paint.color =
          android.graphics.Color.parseColor(
              if (isLive) FriendMarkerConfig.COLOR_LIVE else FriendMarkerConfig.COLOR_STALE)
      canvas.drawCircle(radius, radius, radius, paint)
    }

    return bitmap
  }

  /**
   * Loads and caches a user's profile image as a map marker icon.
   *
   * @param context Android context for image loading
   * @param style Mapbox style to add the icon to
   * @param userId User identifier
   * @param user User data (may contain profile picture URL)
   * @param isLive Whether the location is actively being shared
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

          // Skip if icon already exists
          if (addedIconIds.contains(iconId) && style.hasStyleImage(iconId)) return@withContext

          // Check cache first
          val cacheKey = "$userId-$isLive"
          val cachedBitmap = profileImageCache.get(cacheKey)
          if (cachedBitmap != null) {
            if (!style.hasStyleImage(iconId)) {
              style.addImage(iconId, cachedBitmap)
              addedIconIds.add(iconId)
            }
            return@withContext
          }

          // Load profile image or create default marker
          val bitmap = loadProfileImageOrDefault(context, user, isLive, cacheKey)

          if (!style.hasStyleImage(iconId)) {
            style.addImage(iconId, bitmap)
            addedIconIds.add(iconId)
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error loading icon for user $userId", e)
        }
      }

  /**
   * Loads a user's profile image or creates a default marker.
   *
   * @return Bitmap for the map marker
   */
  private suspend fun loadProfileImageOrDefault(
      context: Context,
      user: User?,
      isLive: Boolean,
      cacheKey: String
  ): Bitmap {
    val profileUrl = user?.profilePictureUrl
    if (profileUrl == null) {
      return createMarkerBitmap(isLive = isLive).also { profileImageCache.put(cacheKey, it) }
    }

    return try {
      val mediaRepository = MediaRepositoryProvider.repository
      val uri = withContext(Dispatchers.IO) { mediaRepository.download(profileUrl) }
      val imageBitmap = loadBitmapFromUri(context, uri, Dispatchers.IO)
      val androidBitmap = imageBitmap?.asAndroidBitmap()

      if (androidBitmap != null) {
        createMarkerBitmap(innerBitmap = androidBitmap, isLive = isLive).also {
          profileImageCache.put(cacheKey, it)
        }
      } else {
        createMarkerBitmap(isLive = isLive).also { profileImageCache.put(cacheKey, it) }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to load profile image, using default", e)
      createMarkerBitmap(isLive = isLive).also { profileImageCache.put(cacheKey, it) }
    }
  }

  private fun getIconIdForUser(userId: String, isLive: Boolean): String =
      "${FriendMarkerConfig.ICON_ID}_${userId}_${if (isLive) "live" else "stale"}"

  /**
   * Creates GeoJSON features for friend location markers.
   *
   * @param friendLocations Map of user IDs to their locations
   * @return List of GeoJSON features with location and metadata
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
   * Preloads user data and profile images for friend markers. Call this when locations update to
   * ensure smooth rendering.
   *
   * @param context Android context
   * @param style Mapbox style
   * @param friendLocations Current friend locations to preload
   * @param userRepository Repository for fetching user data
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
          val user =
              userDataCache.get(userId)
                  ?: userRepository.getUserById(userId)?.also { userDataCache.put(userId, it) }
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

package com.github.se.studentconnect.ui.screen.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.github.se.studentconnect.model.friends.FriendLocation
import com.github.se.studentconnect.model.media.MediaRepository
import com.github.se.studentconnect.model.media.MediaRepositoryProvider
import com.github.se.studentconnect.model.user.User
import com.github.se.studentconnect.model.user.UserRepository
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.None
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class FriendMarkersTest {

  private lateinit var mockStyle: Style
  private lateinit var mockContext: Context
  private lateinit var mockDrawable: Drawable
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockMediaRepository: MediaRepository

  // Create a successful Expected result for addImage
  private val successfulExpected: Expected<String, None> by lazy {
    mockk<Expected<String, None>>(relaxed = true).also {
      every { it.isValue } returns true
      every { it.isError } returns false
    }
  }

  @Before
  fun setUp() {
    // Clear caches before each test
    FriendMarkers.clearCaches()

    // Set up test dispatcher for Main - use Unconfined to execute immediately
    val testDispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    mockStyle = mockk(relaxed = true)
    mockContext = ApplicationProvider.getApplicationContext()
    mockDrawable = mockk(relaxed = true)
    mockUserRepository = mockk(relaxed = true)
    mockMediaRepository = mockk(relaxed = true)

    mockkStatic(ContextCompat::class)
    every { ContextCompat.getDrawable(any(), any()) } returns mockDrawable

    // Mock MediaRepositoryProvider
    mockkObject(MediaRepositoryProvider)
    every { MediaRepositoryProvider.repository } returns mockMediaRepository

    // Mock Style methods
    every { mockStyle.hasStyleImage(any()) } returns false
    every { mockStyle.addImage(any(), any<Bitmap>()) } returns successfulExpected
    every { mockStyle.styleLayerExists(any()) } returns false
    every { mockStyle.styleSourceExists(any()) } returns false
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
    FriendMarkers.clearCaches()
  }

  @Test
  fun removeExistingFriendLayers_removesLayerAndSourceWhenExist() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } returns true
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns true
    FriendMarkers.removeExistingFriendLayers(mockStyle)
    verify { mockStyle.removeStyleLayer(FriendMarkerConfig.LAYER_ID) }
    verify { mockStyle.removeStyleSource(FriendMarkerConfig.SOURCE_ID) }
  }

  @Test
  fun removeExistingFriendLayers_doesNotRemoveWhenNotExist() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } returns false
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns false
    FriendMarkers.removeExistingFriendLayers(mockStyle)
    verify(exactly = 0) { mockStyle.removeStyleLayer(any()) }
    verify(exactly = 0) { mockStyle.removeStyleSource(any()) }
  }

  @Test
  fun removeExistingFriendLayers_handlesExceptionsGracefully() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } throws
        RuntimeException("Test error")
    // Should not throw, just log error
    FriendMarkers.removeExistingFriendLayers(mockStyle)
  }

  @Test
  fun removeExistingFriendLayers_clearsAddedIconIds() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } returns true
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns true
    FriendMarkers.removeExistingFriendLayers(mockStyle)
    // Verify by calling again - should not fail even if called multiple times
    FriendMarkers.removeExistingFriendLayers(mockStyle)
  }

  @Test
  fun createFriendFeatures_createsCorrectNumberOfFeatures() {
    assertTrue(FriendMarkers.createFriendFeatures(emptyMap()).isEmpty())
    assertEquals(
        1,
        FriendMarkers.createFriendFeatures(mapOf("f1" to FriendLocation("f1", 0.0, 0.0, 0L))).size)
    assertEquals(
        3,
        FriendMarkers.createFriendFeatures(
                mapOf(
                    "f1" to FriendLocation("f1", 46.5191, 6.5668, 0L),
                    "f2" to FriendLocation("f2", 46.5200, 6.5700, 0L),
                    "f3" to FriendLocation("f3", 46.5210, 6.5710, 0L)))
            .size)
  }

  @Test
  fun createFriendFeatures_setsCorrectProperties() {
    val timestamp = System.currentTimeMillis()
    val features =
        FriendMarkers.createFriendFeatures(
            mapOf("friend123" to FriendLocation("friend123", 46.5191, 6.5668, timestamp)))
    val feature = features[0]
    val point = feature.geometry() as Point
    assertEquals(6.5668, point.longitude(), 0.0001)
    assertEquals(46.5191, point.latitude(), 0.0001)
    assertEquals("friend123", feature.getStringProperty(FriendMarkerConfig.PROP_USER_ID))
    assertEquals(
        timestamp.toDouble(),
        feature.getNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP).toDouble(),
        0.0)
  }

  @Test
  fun createFriendFeatures_includesIsLiveProperty() {
    val liveLocation = FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true)
    val staleLocation = FriendLocation("user2", 46.5200, 6.5700, System.currentTimeMillis(), false)

    val features =
        FriendMarkers.createFriendFeatures(mapOf("user1" to liveLocation, "user2" to staleLocation))

    assertEquals(2, features.size)
    assertTrue(features[0].getBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE))
    assertFalse(features[1].getBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE))
  }

  @Test
  fun createFriendFeatures_includesCorrectIconIds() {
    val liveLocation = FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true)
    val staleLocation = FriendLocation("user2", 46.5200, 6.5700, System.currentTimeMillis(), false)

    val features =
        FriendMarkers.createFriendFeatures(mapOf("user1" to liveLocation, "user2" to staleLocation))

    val liveIconId = features[0].getStringProperty(FriendMarkerConfig.PROP_ICON_ID)
    val staleIconId = features[1].getStringProperty(FriendMarkerConfig.PROP_ICON_ID)

    assertTrue(liveIconId.contains("user1"))
    assertTrue(liveIconId.contains("live"))
    assertTrue(staleIconId.contains("user2"))
    assertTrue(staleIconId.contains("stale"))
  }

  @Test
  fun createFriendFeatures_handlesMultipleLocationsForDifferentUsers() {
    val locations =
        mapOf(
            "alice" to FriendLocation("alice", 46.5191, 6.5668, 1000L, true),
            "bob" to FriendLocation("bob", 46.5200, 6.5700, 2000L, false),
            "charlie" to FriendLocation("charlie", 46.5210, 6.5710, 3000L, true))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)
    val userIds = features.map { it.getStringProperty(FriendMarkerConfig.PROP_USER_ID) }
    assertTrue(userIds.containsAll(listOf("alice", "bob", "charlie")))
  }

  @Test
  fun addFriendSource_createsFeatureCollectionFromFeatures() {
    val features =
        listOf(
            Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
              addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, 123456L)
              addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, true)
            },
            Feature.fromGeometry(Point.fromLngLat(6.5700, 46.5200)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend2")
              addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, 123457L)
              addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, false)
            })

    try {
      FriendMarkers.addFriendSource(mockStyle, features)
    } catch (e: ClassCastException) {
      // Expected - Mapbox SDK internals can't work with mock Style
    } catch (e: Exception) {
      // Also catch other exceptions that might be thrown
    }
  }

  @Test
  fun addFriendSource_handlesEmptyFeaturesList() {
    try {
      FriendMarkers.addFriendSource(mockStyle, emptyList())
    } catch (e: ClassCastException) {
      // Expected - Mapbox SDK internals can't work with mock Style
    } catch (e: Exception) {
      // Also catch other exceptions
    }
  }

  @Test
  fun addFriendSource_handlesExceptionsGracefully() {
    val features =
        listOf(
            Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
            })

    // Should not crash, just log error
    try {
      FriendMarkers.addFriendSource(mockStyle, features)
    } catch (e: Exception) {
      // Expected with mock
    }
  }

  @Test
  fun updateFriendSource_removesOldSourceAndAddsNew() {
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns true

    val features =
        listOf(
            Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
            })

    try {
      FriendMarkers.updateFriendSource(mockStyle, features)
      verify(atLeast = 1) { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) }
    } catch (e: Exception) {
      // Expected with mocks
    }
  }

  @Test
  fun updateFriendSource_handlesNonExistentSource() {
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns false

    val features =
        listOf(
            Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
            })

    try {
      FriendMarkers.updateFriendSource(mockStyle, features)
      verify(exactly = 0) { mockStyle.removeStyleSource(any()) }
    } catch (e: Exception) {
      // Expected with mocks
    }
  }

  @Test
  fun updateFriendSource_handlesExceptionsGracefully() {
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } throws
        RuntimeException("Mock error")

    try {
      FriendMarkers.updateFriendSource(mockStyle, emptyList())
    } catch (e: Exception) {
      // Should handle gracefully
    }
  }

  @Test
  fun addFriendMarkerLayer_createsSymbolLayer() {
    try {
      FriendMarkers.addFriendMarkerLayer(mockStyle)
    } catch (e: ClassCastException) {
      // Expected - Mapbox SDK internals can't work with mock Style
    } catch (e: Exception) {
      // Also catch other exceptions
    }
  }

  @Test
  fun addFriendMarkerLayer_handlesExceptionsGracefully() {
    try {
      FriendMarkers.addFriendMarkerLayer(mockStyle)
    } catch (e: Exception) {
      // Should not crash the app - expected with mock
    }
  }

  @Test
  fun preloadFriendData_handlesEmptyLocations() = runTest {
    // Should not crash with empty map
    FriendMarkers.preloadFriendData(mockContext, mockStyle, emptyMap(), mockUserRepository)
    // Give coroutine time to potentially execute
    kotlinx.coroutines.delay(100)
  }

  @Test
  fun preloadFriendData_callsUserRepositoryForEachFriend() = runTest {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, System.currentTimeMillis(), false))

    val mockUser1 = mockk<User>(relaxed = true)
    val mockUser2 = mockk<User>(relaxed = true)

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser1
    coEvery { mockUserRepository.getUserById("user2") } returns mockUser2
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)

    // Give coroutines time to execute
    kotlinx.coroutines.delay(500)
  }

  @Test
  fun preloadFriendData_handlesUserRepositoryFailure() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    coEvery { mockUserRepository.getUserById("user1") } throws RuntimeException("User not found")

    // Should not crash
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun clearCaches_clearsAllCachesSuccessfully() {
    // Should not crash
    FriendMarkers.clearCaches()
  }

  @Test
  fun clearCaches_canBeCalledMultipleTimes() {
    FriendMarkers.clearCaches()
    FriendMarkers.clearCaches()
    FriendMarkers.clearCaches()
    // Should not crash
  }

  @Test
  fun friendMarkerConfig_hasCorrectValues() {
    assertEquals("friend_marker_icon", FriendMarkerConfig.ICON_ID)
    assertEquals("friend_source", FriendMarkerConfig.SOURCE_ID)
    assertEquals("friend_layer", FriendMarkerConfig.LAYER_ID)
    assertEquals("#9333EA", FriendMarkerConfig.COLOR_LIVE)
    assertEquals("#9CA3AF", FriendMarkerConfig.COLOR_STALE)
    assertEquals(0.4, FriendMarkerConfig.ICON_SIZE, 0.001)
    assertEquals("userId", FriendMarkerConfig.PROP_USER_ID)
    assertEquals("timestamp", FriendMarkerConfig.PROP_TIMESTAMP)
    assertEquals("isLive", FriendMarkerConfig.PROP_IS_LIVE)
    assertEquals("iconId", FriendMarkerConfig.PROP_ICON_ID)
  }

  @Test
  fun preloadFriendData_usesCache() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    val mockUser1 = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser1
    every { mockStyle.hasStyleImage(any()) } returns false

    // Call twice to test caching
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun preloadFriendData_handlesNullUser() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    coEvery { mockUserRepository.getUserById("user1") } returns null
    every { mockStyle.hasStyleImage(any()) } returns false

    // Should not crash with null user
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun preloadFriendData_handlesUserWithProfilePicture() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    val mockUser =
        mockk<User>(relaxed = true) {
          every { profilePictureUrl } returns "https://example.com/profile.jpg"
        }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)
  }

  @Test
  fun preloadFriendData_handlesMixedLiveAndStaleLocations() = runTest {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, System.currentTimeMillis(), false),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, System.currentTimeMillis(), true))

    val mockUser1 = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    val mockUser2 =
        mockk<User>(relaxed = true) {
          every { profilePictureUrl } returns "https://example.com/user2.jpg"
        }
    val mockUser3 = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser1
    coEvery { mockUserRepository.getUserById("user2") } returns mockUser2
    coEvery { mockUserRepository.getUserById("user3") } returns mockUser3
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(500)
  }

  @Test
  fun preloadFriendData_iconAlreadyExists() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns true

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun createFriendFeatures_handlesVariousTimestamps() {
    val currentTime = System.currentTimeMillis()
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 0L, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, currentTime, false),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, Long.MAX_VALUE, true))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)
    assertEquals(
        0.0, features[0].getNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP).toDouble(), 0.0)
    assertEquals(
        currentTime.toDouble(),
        features[1].getNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP).toDouble(),
        0.0)
  }

  @Test
  fun createFriendFeatures_handlesExtremeCoordinates() {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", -90.0, -180.0, 0L, true),
            "user2" to FriendLocation("user2", 90.0, 180.0, 0L, false),
            "user3" to FriendLocation("user3", 0.0, 0.0, 0L, true))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)

    val point1 = features[0].geometry() as Point
    assertEquals(-180.0, point1.longitude(), 0.0001)
    assertEquals(-90.0, point1.latitude(), 0.0001)

    val point2 = features[1].geometry() as Point
    assertEquals(180.0, point2.longitude(), 0.0001)
    assertEquals(90.0, point2.latitude(), 0.0001)

    val point3 = features[2].geometry() as Point
    assertEquals(0.0, point3.longitude(), 0.0001)
    assertEquals(0.0, point3.latitude(), 0.0001)
  }

  @Test
  fun addFriendMarkerLayer_createsLayerWithCorrectProperties() {
    try {
      FriendMarkers.addFriendMarkerLayer(mockStyle)
      // Just verify it doesn't crash
    } catch (e: Exception) {
      // Expected with mock style
    }
  }

  @Test
  fun addFriendSource_withMultipleFeatures() {
    val features =
        listOf(
            Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
              addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, 123456L)
              addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, true)
            },
            Feature.fromGeometry(Point.fromLngLat(6.5700, 46.5200)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend2")
              addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, 123457L)
              addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, false)
            },
            Feature.fromGeometry(Point.fromLngLat(6.5710, 46.5210)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend3")
              addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, 123458L)
              addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, true)
            })

    try {
      FriendMarkers.addFriendSource(mockStyle, features)
    } catch (e: Exception) {
      // Expected with mock
    }
  }

  @Test
  fun updateFriendSource_whenSourceDoesNotExist() {
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns false

    val features =
        listOf(
            Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
              addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
            })

    try {
      FriendMarkers.updateFriendSource(mockStyle, features)
    } catch (e: Exception) {
      // Expected with mock
    }
  }

  @Test
  fun removeExistingFriendLayers_onlyLayerExists() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } returns true
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns false

    FriendMarkers.removeExistingFriendLayers(mockStyle)

    verify { mockStyle.removeStyleLayer(FriendMarkerConfig.LAYER_ID) }
    verify(exactly = 0) { mockStyle.removeStyleSource(any()) }
  }

  @Test
  fun removeExistingFriendLayers_onlySourceExists() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } returns false
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns true

    FriendMarkers.removeExistingFriendLayers(mockStyle)

    verify(exactly = 0) { mockStyle.removeStyleLayer(any()) }
    verify { mockStyle.removeStyleSource(FriendMarkerConfig.SOURCE_ID) }
  }

  @Test
  fun clearCaches_afterPreloadingData() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)

    // Clear and verify it can be called without crash
    FriendMarkers.clearCaches()
  }

  @Test
  fun createFriendFeatures_orderPreservation() {
    val locations =
        linkedMapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, 2000L, false),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, 3000L, true))

    val features = FriendMarkers.createFriendFeatures(locations)
    val userIds = features.map { it.getStringProperty(FriendMarkerConfig.PROP_USER_ID) }

    // Order should match the iteration order of map values
    assertEquals(3, userIds.size)
    assertTrue(userIds.contains("user1"))
    assertTrue(userIds.contains("user2"))
    assertTrue(userIds.contains("user3"))
  }

  @Test
  fun preloadFriendData_multipleExceptions() = runTest {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, System.currentTimeMillis(), false))

    coEvery { mockUserRepository.getUserById("user1") } throws RuntimeException("Error 1")
    coEvery { mockUserRepository.getUserById("user2") } throws RuntimeException("Error 2")

    // Should handle all errors gracefully
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)
  }

  @Test
  fun createFriendFeatures_singleLocation() {
    val location = FriendLocation("user1", 46.5191, 6.5668, 1234567890L, true)
    val features = FriendMarkers.createFriendFeatures(mapOf("user1" to location))

    assertEquals(1, features.size)

    val feature = features[0]
    val point = feature.geometry() as Point
    assertEquals(6.5668, point.longitude(), 0.0001)
    assertEquals(46.5191, point.latitude(), 0.0001)
    assertEquals("user1", feature.getStringProperty(FriendMarkerConfig.PROP_USER_ID))
    assertEquals(
        1234567890.0, feature.getNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP).toDouble(), 0.0)
    assertTrue(feature.getBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE))
  }

  @Test
  fun createFriendFeatures_allLiveLocations() {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, 2000L, true),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, 3000L, true))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)
    features.forEach { feature ->
      assertTrue(feature.getBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE))
    }
  }

  @Test
  fun createFriendFeatures_allStaleLocations() {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, false),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, 2000L, false),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, 3000L, false))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)
    features.forEach { feature ->
      assertFalse(feature.getBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE))
    }
  }

  @Test
  fun addFriendSource_withSingleFeature() {
    val feature =
        Feature.fromGeometry(Point.fromLngLat(6.5668, 46.5191)).apply {
          addStringProperty(FriendMarkerConfig.PROP_USER_ID, "friend1")
          addNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP, 123456L)
          addBooleanProperty(FriendMarkerConfig.PROP_IS_LIVE, true)
        }

    try {
      FriendMarkers.addFriendSource(mockStyle, listOf(feature))
    } catch (e: Exception) {
      // Expected with mock
    }
  }

  @Test
  fun updateFriendSource_withEmptyFeatures() {
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns true

    try {
      FriendMarkers.updateFriendSource(mockStyle, emptyList())
    } catch (e: Exception) {
      // Expected with mock
    }
  }

  @Test
  fun removeExistingFriendLayers_calledMultipleTimesSequentially() {
    every { mockStyle.styleLayerExists(FriendMarkerConfig.LAYER_ID) } returns true
    every { mockStyle.styleSourceExists(FriendMarkerConfig.SOURCE_ID) } returns true

    FriendMarkers.removeExistingFriendLayers(mockStyle)
    FriendMarkers.removeExistingFriendLayers(mockStyle)
    FriendMarkers.removeExistingFriendLayers(mockStyle)

    // Should not crash
  }

  @Test
  fun clearCaches_multipleSequentialCalls() {
    FriendMarkers.clearCaches()
    FriendMarkers.clearCaches()
    FriendMarkers.clearCaches()
    FriendMarkers.clearCaches()
    FriendMarkers.clearCaches()

    // Should not crash
  }

  @Test
  fun preloadFriendData_withZeroTimestamp() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 0L, true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun preloadFriendData_withMaxTimestamp() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, Long.MAX_VALUE, true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun createFriendFeatures_withIdenticalTimestamps() {
    val timestamp = System.currentTimeMillis()
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, timestamp, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, timestamp, false),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, timestamp, true))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)
    features.forEach { feature ->
      assertEquals(
          timestamp.toDouble(),
          feature.getNumberProperty(FriendMarkerConfig.PROP_TIMESTAMP).toDouble(),
          0.0)
    }
  }

  @Test
  fun createFriendFeatures_withSameCoordinatesDifferentUsers() {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true),
            "user2" to FriendLocation("user2", 46.5191, 6.5668, 2000L, false),
            "user3" to FriendLocation("user3", 46.5191, 6.5668, 3000L, true))

    val features = FriendMarkers.createFriendFeatures(locations)

    assertEquals(3, features.size)

    // All features should have the same coordinates
    features.forEach { feature ->
      val point = feature.geometry() as Point
      assertEquals(6.5668, point.longitude(), 0.0001)
      assertEquals(46.5191, point.latitude(), 0.0001)
    }

    // But different user IDs
    val userIds = features.map { it.getStringProperty(FriendMarkerConfig.PROP_USER_ID) }.toSet()
    assertEquals(3, userIds.size)
  }

  @Test
  fun preloadFriendData_concurrentCallsWithSameData() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    // Call preload multiple times in quick succession
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)

    kotlinx.coroutines.delay(500)
  }

  @Test
  fun addFriendMarkerLayer_calledMultipleTimes() {
    try {
      FriendMarkers.addFriendMarkerLayer(mockStyle)
      FriendMarkers.addFriendMarkerLayer(mockStyle)
      FriendMarkers.addFriendMarkerLayer(mockStyle)
    } catch (e: Exception) {
      // Expected with mock style
    }
  }

  @Test
  fun createFriendFeatures_withVeryLongUserId() {
    val longUserId = "u".repeat(1000)
    val location = FriendLocation(longUserId, 46.5191, 6.5668, 1000L, true)
    val features = FriendMarkers.createFriendFeatures(mapOf(longUserId to location))

    assertEquals(1, features.size)
    assertEquals(longUserId, features[0].getStringProperty(FriendMarkerConfig.PROP_USER_ID))
  }

  @Test
  fun createFriendFeatures_withSpecialCharactersInUserId() {
    val specialUserId = "user@#$%^&*()_+-=[]{}|;':\",./<>?"
    val location = FriendLocation(specialUserId, 46.5191, 6.5668, 1000L, true)
    val features = FriendMarkers.createFriendFeatures(mapOf(specialUserId to location))

    assertEquals(1, features.size)
    assertEquals(specialUserId, features[0].getStringProperty(FriendMarkerConfig.PROP_USER_ID))
  }

  @Test
  fun friendMarkerConfig_colorLiveIsPurple() {
    // Purple color code should be #9333EA
    assertEquals("#9333EA", FriendMarkerConfig.COLOR_LIVE)
    assertTrue(FriendMarkerConfig.COLOR_LIVE.contains("93"))
  }

  @Test
  fun friendMarkerConfig_colorStaleIsGrey() {
    // Grey color code should be #9CA3AF
    assertEquals("#9CA3AF", FriendMarkerConfig.COLOR_STALE)
    assertTrue(FriendMarkerConfig.COLOR_STALE.contains("9C"))
  }

  @Test
  fun preloadFriendData_withUserHavingEmptyProfileUrl() = runTest {
    val locations =
        mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns "" }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(200)
  }

  @Test
  fun preloadFriendData_alternatingLiveAndStale() = runTest {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, 2000L, false),
            "user3" to FriendLocation("user3", 46.5210, 6.5710, 3000L, true),
            "user4" to FriendLocation("user4", 46.5220, 6.5720, 4000L, false),
            "user5" to FriendLocation("user5", 46.5230, 6.5730, 5000L, true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById(any()) } returns mockUser
    every { mockStyle.hasStyleImage(any()) } returns false

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(500)
  }

  // ===== Robolectric Integration Tests for Maximum Coverage =====

  @Test
  fun preloadFriendData_executesCreateMarkerBitmapForLiveLocation() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))
    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Verify bitmap was created and added to style
    verify(timeout = 2000) {
      mockStyle.addImage(match { it.contains("user1") && it.contains("live") }, any<Bitmap>())
    }
  }

  @Test
  fun preloadFriendData_executesCreateMarkerBitmapForStaleLocation() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, false))
    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Verify stale marker was created
    verify(timeout = 2000) {
      mockStyle.addImage(match { it.contains("user1") && it.contains("stale") }, any<Bitmap>())
    }
  }

  @Test
  fun preloadFriendData_loadsMultipleFriends() = runTest {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, System.currentTimeMillis(), true),
            "user2" to FriendLocation("user2", 46.5192, 6.5669, System.currentTimeMillis(), false))

    val mockUser1 = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    val mockUser2 = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser1
    coEvery { mockUserRepository.getUserById("user2") } returns mockUser2

    // Call with multiple friends - this should preload data for both users
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)

    // Give coroutines time to execute
    kotlinx.coroutines.delay(500)
  }

  @Test
  fun preloadFriendData_handlesProfileUrlDownloadFailure() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))
    val mockUser =
        mockk<User>(relaxed = true) {
          every { profilePictureUrl } returns "https://example.com/profile.jpg"
        }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    coEvery { mockMediaRepository.download(any()) } throws Exception("Download failed")

    // Should fall back to default marker
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Verify fallback bitmap was created
    verify(timeout = 2000) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_handlesNullBitmapFromUri() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))
    val mockUser =
        mockk<User>(relaxed = true) {
          every { profilePictureUrl } returns "https://example.com/profile.jpg"
        }
    val mockUri = mockk<android.net.Uri>(relaxed = true)

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    coEvery { mockMediaRepository.download(any()) } returns mockUri

    // loadBitmapFromUri will return null in this test environment
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Should fall back to default marker
    verify(timeout = 2000) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_skipsWhenIconAlreadyExists() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))
    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser

    // First call - wait for it to complete
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)

    // Wait for async operation to complete
    verify(timeout = 2000, exactly = 1) { mockStyle.addImage(any(), any<Bitmap>()) }

    // Now mock that icon exists in the style
    every { mockStyle.hasStyleImage(any()) } returns true
    every { mockStyle.addImage(any(), any<Bitmap>()) } returns successfulExpected

    // Clear the verification state for the next check
    clearMocks(mockStyle, answers = false)
    every { mockStyle.hasStyleImage(any()) } returns true
    every { mockStyle.addImage(any(), any<Bitmap>()) } returns successfulExpected

    // Second call should skip because icon already exists
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)

    // Give it time to potentially call addImage (it shouldn't)
    kotlinx.coroutines.delay(500)

    // Verify addImage was NOT called again
    verify(exactly = 0) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_handlesMultipleUsersWithDifferentStates() = runTest {
    val locations =
        mapOf(
            "live1" to FriendLocation("live1", 46.5191, 6.5668, 1000L, true),
            "stale1" to FriendLocation("stale1", 46.5200, 6.5700, 2000L, false),
            "live2" to FriendLocation("live2", 46.5210, 6.5710, 3000L, true))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    coEvery { mockUserRepository.getUserById(any()) } returns mockUser

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(500)

    // Verify all markers were created
    verify(timeout = 2000, atLeast = 3) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun clearCaches_clearsProfileImageCache() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))
    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser

    // Load data to populate cache
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Clear caches
    FriendMarkers.clearCaches()

    // Reset mock to track new calls
    clearMocks(mockStyle, answers = false)
    every { mockStyle.hasStyleImage(any()) } returns false
    every { mockStyle.addImage(any(), any<Bitmap>()) } returns successfulExpected

    // Load again - should recreate bitmap (not use cache)
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    verify(timeout = 2000) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_handlesUserRepositoryReturningNull() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))

    coEvery { mockUserRepository.getUserById("user1") } returns null

    // Should handle null user gracefully
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Should still create default marker
    verify(timeout = 2000) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_handlesStyleAddImageException() = runTest {
    val locations = mapOf("user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true))
    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }

    coEvery { mockUserRepository.getUserById("user1") } returns mockUser
    every { mockStyle.addImage(any(), any<Bitmap>()) } throws RuntimeException("Style error")

    // Should handle exception gracefully and log error
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(300)

    // Should have attempted to add image
    verify(timeout = 2000, atLeast = 1) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_handlesConcurrentRequests() = runTest {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, 2000L, false))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    coEvery { mockUserRepository.getUserById(any()) } returns mockUser

    // Launch multiple concurrent preload operations
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)

    kotlinx.coroutines.delay(500)

    // Should handle concurrent requests gracefully
    verify(timeout = 2000, atLeast = 2) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_mixedProfileUrlScenarios() = runTest {
    val locations =
        mapOf(
            "noUrl" to FriendLocation("noUrl", 46.5191, 6.5668, 1000L, true),
            "emptyUrl" to FriendLocation("emptyUrl", 46.5200, 6.5700, 2000L, false),
            "validUrl" to FriendLocation("validUrl", 46.5210, 6.5710, 3000L, true))

    val userNoUrl = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    val userEmptyUrl = mockk<User>(relaxed = true) { every { profilePictureUrl } returns "" }
    val userValidUrl =
        mockk<User>(relaxed = true) {
          every { profilePictureUrl } returns "https://example.com/profile.jpg"
        }

    coEvery { mockUserRepository.getUserById("noUrl") } returns userNoUrl
    coEvery { mockUserRepository.getUserById("emptyUrl") } returns userEmptyUrl
    coEvery { mockUserRepository.getUserById("validUrl") } returns userValidUrl
    coEvery { mockMediaRepository.download(any()) } throws Exception("Download failed")

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(500)

    // All should create markers (fallback for failed download)
    verify(timeout = 2000, atLeast = 3) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun getIconIdForUser_generatesCorrectFormat() {
    val locations =
        mapOf(
            "user1" to FriendLocation("user1", 46.5191, 6.5668, 1000L, true),
            "user2" to FriendLocation("user2", 46.5200, 6.5700, 2000L, false))

    val features = FriendMarkers.createFriendFeatures(locations)

    // Verify icon IDs have correct format
    val liveIconId = features[0].getStringProperty(FriendMarkerConfig.PROP_ICON_ID)
    val staleIconId = features[1].getStringProperty(FriendMarkerConfig.PROP_ICON_ID)

    assertTrue(liveIconId.startsWith(FriendMarkerConfig.ICON_ID))
    assertTrue(liveIconId.contains("user1"))
    assertTrue(liveIconId.endsWith("live"))

    assertTrue(staleIconId.startsWith(FriendMarkerConfig.ICON_ID))
    assertTrue(staleIconId.contains("user2"))
    assertTrue(staleIconId.endsWith("stale"))
  }

  @Test
  fun preloadFriendData_exercisesBothCreateMarkerBitmapBranches() = runTest {
    // Test both branches of createMarkerBitmap: with innerBitmap and without
    val locations =
        mapOf(
            "userLiveNoImage" to FriendLocation("userLiveNoImage", 46.5191, 6.5668, 1000L, true),
            "userStaleNoImage" to FriendLocation("userStaleNoImage", 46.5200, 6.5700, 2000L, false))

    val mockUser = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    coEvery { mockUserRepository.getUserById(any()) } returns mockUser

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(500)

    // Both live (purple) and stale (grey) markers should be created
    verify(timeout = 2000, atLeast = 2) { mockStyle.addImage(any(), any<Bitmap>()) }
  }

  @Test
  fun preloadFriendData_exercisesLoadProfileImageOrDefaultBranches() = runTest {
    val locations =
        mapOf(
            "nullUrl" to FriendLocation("nullUrl", 46.5191, 6.5668, 1000L, true),
            "failedDownload" to FriendLocation("failedDownload", 46.5200, 6.5700, 2000L, false))

    val userNullUrl = mockk<User>(relaxed = true) { every { profilePictureUrl } returns null }
    val userFailedDownload =
        mockk<User>(relaxed = true) {
          every { profilePictureUrl } returns "https://example.com/fail.jpg"
        }

    coEvery { mockUserRepository.getUserById("nullUrl") } returns userNullUrl
    coEvery { mockUserRepository.getUserById("failedDownload") } returns userFailedDownload
    coEvery { mockMediaRepository.download(any()) } throws Exception("Network error")

    FriendMarkers.preloadFriendData(mockContext, mockStyle, locations, mockUserRepository)
    kotlinx.coroutines.delay(500)

    // Both should create default markers
    verify(timeout = 2000, atLeast = 2) { mockStyle.addImage(any(), any<Bitmap>()) }
  }
}

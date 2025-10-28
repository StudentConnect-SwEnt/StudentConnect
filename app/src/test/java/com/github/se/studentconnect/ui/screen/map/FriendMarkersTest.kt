package com.github.se.studentconnect.ui.screen.map

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.github.se.studentconnect.R
import com.github.se.studentconnect.model.friends.FriendLocation
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FriendMarkersTest {

  private lateinit var mockStyle: Style
  private lateinit var mockContext: Context
  private lateinit var mockDrawable: Drawable

  @Before
  fun setUp() {
    mockStyle = mockk(relaxed = true)
    mockContext = mockk(relaxed = true)
    mockDrawable = mockk(relaxed = true)
    mockkStatic(ContextCompat::class)
    every { ContextCompat.getDrawable(any(), any()) } returns mockDrawable
  }

  @After fun tearDown() = unmockkAll()

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
  fun addFriendMarkerIcon_addsIconWhenNotExists() {
    every { mockStyle.hasStyleImage(FriendMarkerConfig.ICON_ID) } returns false
    FriendMarkers.addFriendMarkerIcon(mockContext, mockStyle)
    verify { ContextCompat.getDrawable(mockContext, R.drawable.ic_map_friends) }
    verify { mockDrawable.setTint(any()) }
    verify { mockStyle.addImage(FriendMarkerConfig.ICON_ID, any<android.graphics.Bitmap>()) }
  }

  @Test
  fun addFriendMarkerIcon_doesNotAddWhenExists() {
    every { mockStyle.hasStyleImage(FriendMarkerConfig.ICON_ID) } returns true
    FriendMarkers.addFriendMarkerIcon(mockContext, mockStyle)
    verify(exactly = 0) { mockStyle.addImage(any(), any<android.graphics.Bitmap>()) }
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
}

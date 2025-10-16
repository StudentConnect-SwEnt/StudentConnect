package com.github.se.studentconnect.ui.screen.map

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

class MapConstantsTest {

  @Test
  fun padding_hasCorrectContentPadding() {
    assertEquals(16.dp, Padding.CONTENT)
  }

  @Test
  fun padding_hasCorrectVerticalSpacing() {
    assertEquals(8.dp, Padding.VERTICAL_SPACING)
  }

  @Test
  fun size_hasCorrectFabSize() {
    assertEquals(56.dp, Size.FAB)
  }

  @Test
  fun size_hasCorrectIconSize() {
    assertEquals(24.dp, Size.ICON)
  }

  @Test
  fun size_hasCorrectLargeIconSize() {
    assertEquals(32.dp, Size.LARGE_ICON)
  }

  @Test
  fun corner_hasCorrectRadius() {
    assertEquals(12.dp, Corner.RADIUS)
  }

  @Test
  fun corner_hasCorrectMapRadius() {
    assertEquals(16.dp, Corner.MAP_RADIUS)
  }

  @Test
  fun elevation_hasCorrectDefault() {
    assertEquals(0.dp, Elevation.DEFAULT)
  }
}

class EventMarkerConfigTest {

  @Test
  fun iconId_hasCorrectValue() {
    assertEquals("event_marker_icon", EventMarkerConfig.ICON_ID)
  }

  @Test
  fun sourceId_hasCorrectValue() {
    assertEquals("event_source", EventMarkerConfig.SOURCE_ID)
  }

  @Test
  fun layerId_hasCorrectValue() {
    assertEquals("event_layer", EventMarkerConfig.LAYER_ID)
  }

  @Test
  fun clusterLayerId_hasCorrectValue() {
    assertEquals("event_cluster_layer", EventMarkerConfig.CLUSTER_LAYER_ID)
  }

  @Test
  fun clusterCountLayerId_hasCorrectValue() {
    assertEquals("event_cluster_count_layer", EventMarkerConfig.CLUSTER_COUNT_LAYER_ID)
  }

  @Test
  fun color_hasCorrectValue() {
    assertEquals("#EF4444", EventMarkerConfig.COLOR)
  }

  @Test
  fun color_isValidHexColor() {
    assertTrue("Color should start with #", EventMarkerConfig.COLOR.startsWith("#"))
    assertEquals("Color should have 7 characters (#RRGGBB)", 7, EventMarkerConfig.COLOR.length)
  }

  @Test
  fun iconSize_hasCorrectValue() {
    assertEquals(1.5, EventMarkerConfig.ICON_SIZE, 0.001)
  }

  @Test
  fun iconSize_isPositive() {
    assertTrue("Icon size should be positive", EventMarkerConfig.ICON_SIZE > 0)
  }

  @Test
  fun clusterRadiusPx_hasCorrectValue() {
    assertEquals(30, EventMarkerConfig.CLUSTER_RADIUS_PX)
  }

  @Test
  fun clusterRadiusPx_isPositive() {
    assertTrue("Cluster radius should be positive", EventMarkerConfig.CLUSTER_RADIUS_PX > 0)
  }

  @Test
  fun clusterMaxZoom_hasCorrectValue() {
    assertEquals(16, EventMarkerConfig.CLUSTER_MAX_ZOOM)
  }

  @Test
  fun clusterMaxZoom_isWithinValidRange() {
    assertTrue("Max zoom should be >= 0", EventMarkerConfig.CLUSTER_MAX_ZOOM >= 0)
    assertTrue("Max zoom should be <= 22", EventMarkerConfig.CLUSTER_MAX_ZOOM <= 22)
  }

  @Test
  fun clusterCircleRadius_hasCorrectValue() {
    assertEquals(20.0, EventMarkerConfig.CLUSTER_CIRCLE_RADIUS, 0.001)
  }

  @Test
  fun clusterCircleRadius_isPositive() {
    assertTrue(
        "Cluster circle radius should be positive", EventMarkerConfig.CLUSTER_CIRCLE_RADIUS > 0)
  }

  @Test
  fun clusterStrokeWidth_hasCorrectValue() {
    assertEquals(2.0, EventMarkerConfig.CLUSTER_STROKE_WIDTH, 0.001)
  }

  @Test
  fun clusterStrokeWidth_isNonNegative() {
    assertTrue(
        "Cluster stroke width should be non-negative", EventMarkerConfig.CLUSTER_STROKE_WIDTH >= 0)
  }

  @Test
  fun clusterStrokeColor_hasCorrectValue() {
    assertEquals("#FFFFFF", EventMarkerConfig.CLUSTER_STROKE_COLOR)
  }

  @Test
  fun clusterStrokeColor_isValidHexColor() {
    assertTrue(
        "Stroke color should start with #", EventMarkerConfig.CLUSTER_STROKE_COLOR.startsWith("#"))
    assertEquals(
        "Stroke color should have 7 characters (#RRGGBB)",
        7,
        EventMarkerConfig.CLUSTER_STROKE_COLOR.length)
  }

  @Test
  fun clusterTextSize_hasCorrectValue() {
    assertEquals(14.0, EventMarkerConfig.CLUSTER_TEXT_SIZE, 0.001)
  }

  @Test
  fun clusterTextSize_isPositive() {
    assertTrue("Cluster text size should be positive", EventMarkerConfig.CLUSTER_TEXT_SIZE > 0)
  }

  @Test
  fun clusterTextColor_hasCorrectValue() {
    assertEquals("#FFFFFF", EventMarkerConfig.CLUSTER_TEXT_COLOR)
  }

  @Test
  fun clusterTextColor_isValidHexColor() {
    assertTrue(
        "Text color should start with #", EventMarkerConfig.CLUSTER_TEXT_COLOR.startsWith("#"))
    assertEquals(
        "Text color should have 7 characters (#RRGGBB)",
        7,
        EventMarkerConfig.CLUSTER_TEXT_COLOR.length)
  }

  @Test
  fun clusterTextFonts_hasCorrectValues() {
    assertEquals(2, EventMarkerConfig.CLUSTER_TEXT_FONTS.size)
    assertEquals("DIN Offc Pro Bold", EventMarkerConfig.CLUSTER_TEXT_FONTS[0])
    assertEquals("Arial Unicode MS Bold", EventMarkerConfig.CLUSTER_TEXT_FONTS[1])
  }

  @Test
  fun clusterTextFonts_isNotEmpty() {
    assertTrue("Font list should not be empty", EventMarkerConfig.CLUSTER_TEXT_FONTS.isNotEmpty())
  }

  @Test
  fun clusterTextFonts_noNullEntries() {
    EventMarkerConfig.CLUSTER_TEXT_FONTS.forEach { font ->
      assertNotNull("Font entry should not be null", font)
      assertTrue("Font entry should not be blank", font.isNotBlank())
    }
  }

  @Test
  fun allLayerIds_areUnique() {
    val ids =
        setOf(
            EventMarkerConfig.LAYER_ID,
            EventMarkerConfig.CLUSTER_LAYER_ID,
            EventMarkerConfig.CLUSTER_COUNT_LAYER_ID)
    assertEquals("All layer IDs should be unique", 3, ids.size)
  }

  @Test
  fun allIds_areNotBlank() {
    assertTrue("Icon ID should not be blank", EventMarkerConfig.ICON_ID.isNotBlank())
    assertTrue("Source ID should not be blank", EventMarkerConfig.SOURCE_ID.isNotBlank())
    assertTrue("Layer ID should not be blank", EventMarkerConfig.LAYER_ID.isNotBlank())
    assertTrue(
        "Cluster layer ID should not be blank", EventMarkerConfig.CLUSTER_LAYER_ID.isNotBlank())
    assertTrue(
        "Cluster count layer ID should not be blank",
        EventMarkerConfig.CLUSTER_COUNT_LAYER_ID.isNotBlank())
  }

  @Test
  fun eventMarkerConfig_hasConsistentNamingConvention() {
    // All IDs should follow snake_case pattern
    assertTrue("Icon ID should follow naming pattern", EventMarkerConfig.ICON_ID.contains("_"))
    assertTrue("Source ID should follow naming pattern", EventMarkerConfig.SOURCE_ID.contains("_"))
    assertTrue("Layer ID should follow naming pattern", EventMarkerConfig.LAYER_ID.contains("_"))
  }
}

package com.github.se.studentconnect.ui.screen.map

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** UI dimension constants for padding and spacing */
object Padding {
  val CONTENT: Dp = 16.dp
  val VERTICAL_SPACING: Dp = 8.dp
}

/** UI dimension constants for component sizes */
object Size {
  val FAB: Dp = 56.dp
  val ICON: Dp = 24.dp
  val LARGE_ICON: Dp = 32.dp
}

/** UI dimension constants for corner radii */
object Corner {
  val RADIUS: Dp = 12.dp
  val MAP_RADIUS: Dp = 16.dp
}

/** UI dimension constants for elevation */
object Elevation {
  val DEFAULT: Dp = 0.dp
}

/** Configuration constants for event markers and clustering */
object EventMarkerConfig {
  const val ICON_ID = "event_marker_icon"
  const val SOURCE_ID = "event_source"
  const val LAYER_ID = "event_layer"
  const val CLUSTER_LAYER_ID = "event_cluster_layer"
  const val CLUSTER_COUNT_LAYER_ID = "event_cluster_count_layer"

  const val COLOR = "#EF4444"
  const val ICON_SIZE = 1.5
  const val CLUSTER_RADIUS_PX = 30
  const val CLUSTER_MAX_ZOOM = 16
  const val CLUSTER_CIRCLE_RADIUS = 20.0
  const val CLUSTER_STROKE_WIDTH = 2.0
  const val CLUSTER_STROKE_COLOR = "#FFFFFF"
  const val CLUSTER_TEXT_SIZE = 14.0
  const val CLUSTER_TEXT_COLOR = "#FFFFFF"

  val CLUSTER_TEXT_FONTS = listOf("DIN Offc Pro Bold", "Arial Unicode MS Bold")

  // GeoJSON property keys
  const val PROP_TITLE = "title"
  const val PROP_UID = "uid"
}

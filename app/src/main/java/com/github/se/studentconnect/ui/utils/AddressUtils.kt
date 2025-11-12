package com.github.se.studentconnect.ui.utils

/**
 * Formats a Nominatim display_name into a short address (max 50 chars).
 * Extracts road, neighbourhood/suburb, and city/town/village, or falls back to first two parts.
 */
fun formatShortAddress(displayName: String?): String {
  if (displayName == null) {
    return "Location not specified"
  }

  val parts = displayName.split(",").map { it.trim() }.filter { it.isNotEmpty() }

  if (parts.isEmpty()) {
    return displayName.take(50).let { if (it.length < displayName.length) "$it..." else it }
  }

  val addressParts = mutableListOf<String>()
  if (parts.isNotEmpty()) {
    addressParts.add(parts[0])
  }
  if (parts.size > 1) {
    addressParts.add(parts[1])
  }
  if (parts.size > 2) {
    addressParts.add(parts[2])
  }

  val shortAddress = if (addressParts.size >= 2) {
    addressParts.joinToString(", ")
  } else {
    parts.take(2).joinToString(", ")
  }

  return if (shortAddress.length > 50) {
    shortAddress.take(47) + "..."
  } else {
    shortAddress
  }
}


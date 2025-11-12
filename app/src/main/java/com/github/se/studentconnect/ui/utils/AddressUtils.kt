package com.github.se.studentconnect.ui.utils

private const val MAX_ADDRESS_LENGTH = 50
private const val MAX_ADDRESS_LENGTH_WITH_ELLIPSIS = 47
private const val FIRST_PART_INDEX = 0
private const val SECOND_PART_INDEX = 1
private const val THIRD_PART_INDEX = 2
private const val MIN_PARTS_FOR_JOIN = 2
private const val ELLIPSIS_LENGTH = 3

/**
 * Formats a Nominatim display_name into a short address (max 50 chars). Extracts road,
 * neighbourhood/suburb, and city/town/village, or falls back to first two parts.
 */
fun formatShortAddress(displayName: String?): String {
  if (displayName == null) {
    return "Location not specified"
  }

  val parts = displayName.split(",").map { it.trim() }.filter { it.isNotEmpty() }

  if (parts.isEmpty()) {
    return displayName.take(MAX_ADDRESS_LENGTH).let { if (it.length < displayName.length) "$it..." else it }
  }

  val addressParts = mutableListOf<String>()
  if (parts.isNotEmpty()) {
    addressParts.add(parts[FIRST_PART_INDEX])
  }
  if (parts.size > SECOND_PART_INDEX) {
    addressParts.add(parts[SECOND_PART_INDEX])
  }
  if (parts.size > THIRD_PART_INDEX) {
    addressParts.add(parts[THIRD_PART_INDEX])
  }

  val shortAddress =
      if (addressParts.size >= MIN_PARTS_FOR_JOIN) {
        addressParts.joinToString(", ")
      } else {
        parts.take(MIN_PARTS_FOR_JOIN).joinToString(", ")
      }

  return if (shortAddress.length > MAX_ADDRESS_LENGTH) {
    shortAddress.take(MAX_ADDRESS_LENGTH_WITH_ELLIPSIS) + "..."
  } else {
    shortAddress
  }
}

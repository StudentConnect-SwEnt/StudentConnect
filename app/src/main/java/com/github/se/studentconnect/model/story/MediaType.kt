package com.github.se.studentconnect.model.story

/**
 * Type-safe enum for story media types.
 */
enum class MediaType(val value: String) {
  IMAGE("image"),
  VIDEO("video");

  companion object {
    /**
     * Converts a string value to MediaType, defaulting to IMAGE if invalid.
     */
    fun fromString(value: String?): MediaType {
      return when (value) {
        IMAGE.value -> IMAGE
        VIDEO.value -> VIDEO
        else -> IMAGE // Default to image
      }
    }
  }
}


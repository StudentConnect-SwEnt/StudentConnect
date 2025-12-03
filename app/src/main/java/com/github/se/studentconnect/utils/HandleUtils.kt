package com.github.se.studentconnect.utils

import java.text.Normalizer

/**
 * Utility object for sanitizing and generating handles (usernames, organization handles, etc.)
 *
 * This ensures consistent handle generation across the app and prevents issues with special
 * characters, accents, and duplicates.
 */
object HandleUtils {

  /**
   * Sanitizes a string to create a valid handle by:
   * - Converting to lowercase
   * - Removing accents and diacritics
   * - Removing special characters (keeping only alphanumeric and underscores)
   * - Replacing spaces with underscores
   * - Collapsing multiple underscores into one
   * - Removing leading/trailing underscores
   *
   * @param input The input string to sanitize
   * @return A sanitized handle string
   */
  fun sanitizeHandle(input: String): String {
    // Normalize to NFD (decomposed form) to separate base characters from diacritics
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)

    // Remove diacritical marks (accents)
    val withoutAccents = normalized.replace("\\p{M}".toRegex(), "")

    // Convert to lowercase, replace spaces with underscores
    val lowercaseWithUnderscores = withoutAccents.lowercase().replace(" ", "_")

    // Keep only alphanumeric and underscores
    val alphanumericOnly = lowercaseWithUnderscores.replace("[^a-z0-9_]".toRegex(), "")

    // Collapse multiple underscores into one
    val collapsed = alphanumericOnly.replace("_+".toRegex(), "_")

    // Remove leading and trailing underscores
    return collapsed.trim('_')
  }

  /**
   * Generates a handle from an organization or user name.
   *
   * Note: This method does NOT guarantee uniqueness. The calling code should validate uniqueness
   * with the backend before persisting the handle.
   *
   * @param name The name to generate a handle from
   * @param prefix Optional prefix (e.g., "@" for organization handles)
   * @return A sanitized handle with optional prefix
   */
  fun generateHandle(name: String, prefix: String = ""): String {
    val sanitized = sanitizeHandle(name)
    return if (sanitized.isNotEmpty()) {
      "$prefix$sanitized"
    } else {
      // Fallback for completely invalid names
      "${prefix}org"
    }
  }
}

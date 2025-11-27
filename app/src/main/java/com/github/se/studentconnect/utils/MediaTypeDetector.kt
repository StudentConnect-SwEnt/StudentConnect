package com.github.se.studentconnect.utils

import android.content.Context
import android.net.Uri

/** Utility for detecting media type (image or video) from a URI. */
object MediaTypeDetector {
  /**
   * Detects if the URI points to an image or video.
   *
   * @param context The application context
   * @param uri The URI to check
   * @return "image" if it's an image, "video" if it's a video, "image" as default
   */
  suspend fun detectMediaType(context: Context, uri: Uri): String {
    return try {
      val mimeType = context.contentResolver.getType(uri) ?: return "image"

      when {
        mimeType.startsWith("image/") -> "image"
        mimeType.startsWith("video/") -> "video"
        else -> "image" // Default to image if unknown
      }
    } catch (e: Exception) {
      // Fallback: check file extension if MIME type detection fails
      val path = uri.path ?: return "image"
      when {
        path.contains(".mp4", ignoreCase = true) ||
            path.contains(".mov", ignoreCase = true) ||
            path.contains(".avi", ignoreCase = true) ||
            path.contains(".mkv", ignoreCase = true) ||
            path.contains(".webm", ignoreCase = true) -> "video"
        else -> "image"
      }
    }
  }
}

package com.github.se.studentconnect.utils

import android.content.Context
import android.net.Uri
import com.github.se.studentconnect.model.story.MediaType

/** Utility for detecting media type (image or video) from a URI. */
object MediaTypeDetector {
  /**
   * Detects if the URI points to an image or video.
   *
   * @param context The application context
   * @param uri The URI to check
   * @return MediaType.IMAGE if it's an image, MediaType.VIDEO if it's a video, MediaType.IMAGE as default
   */
  fun detectMediaType(context: Context, uri: Uri): MediaType {
    return try {
      val mimeType = context.contentResolver.getType(uri)

      // If MIME type is detected, use it
      if (mimeType != null) {
        return when {
          mimeType.startsWith("image/") -> MediaType.IMAGE
          mimeType.startsWith("video/") -> MediaType.VIDEO
          else -> MediaType.IMAGE // Default to image if unknown
        }
      }

      // If MIME type is null, fallback to file extension
      val path = uri.path ?: return MediaType.IMAGE
      detectMediaTypeFromPath(path)
    } catch (e: Exception) {
      // Fallback: check file extension if MIME type detection fails
      val path = uri.path ?: return MediaType.IMAGE
      detectMediaTypeFromPath(path)
    }
  }

  /**
   * Detects media type from file path based on extension.
   *
   * @param path The file path to check
   * @return MediaType.VIDEO if path contains a video extension, MediaType.IMAGE otherwise
   */
  private fun detectMediaTypeFromPath(path: String): MediaType {
    return when {
      path.contains(".mp4", ignoreCase = true) ||
          path.contains(".mov", ignoreCase = true) ||
          path.contains(".avi", ignoreCase = true) ||
          path.contains(".mkv", ignoreCase = true) ||
          path.contains(".webm", ignoreCase = true) -> MediaType.VIDEO
      else -> MediaType.IMAGE
    }
  }
}
